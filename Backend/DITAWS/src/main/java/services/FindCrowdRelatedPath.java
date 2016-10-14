package services;

import io.Mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.graphhopper.GraphHopper;

import util.Graph;
import util.TimeUtils;
import util.TravelPath;
import model.Activity;
import model.Node;
import model.POI;
import model.Path;
import model.UncertainValue;
import model.Visit;
import model.VisitPlan;

public class FindCrowdRelatedPath {
	
	private Logger logger = Logger.getLogger(FindCrowdRelatedPath.class);

	private List<String> to_visit;

	public VisitPlan newPlan(Mongo dao, String user, POI departure, POI arrival, String start_time, 
			List<String> POIsList, List<POI> full_pois, TreeMap<String, TreeMap<String, Double>> distances, Map<String, HashMap<String, List<UncertainValue>>> travel_times, 
			Map<String, HashMap<String, List<UncertainValue>>> crowding_levels, 
			Map<String, List<Integer>> occupancies, double crowding_preference, GraphHopper hopper) {
		to_visit = new ArrayList<String>();
		for (String poi : POIsList) {
			to_visit.add(poi);
		}
		
		logger.info("user:"+user);
		logger.info("start:"+departure.getPlace_id());
		logger.info("end:"+arrival.getPlace_id());
		logger.info("start_time:"+start_time);
		
		/*
		 * COMPUTE CROWD RELATED PATH WITH CROWD PREFERENCE 
		 */

		try {
            List<Node> path = new PathFindingWithCrowding().AstarSearch(dao, departure, arrival, start_time, to_visit, distances, travel_times, crowding_levels, occupancies, 10, crowding_preference);
            logger.info("Crowd Related path with preferred <" + crowding_preference + "> cong.levels :" + path.get(0).getName() + "-" + path.get(path.size() - 1).getName() + ":" + path.get(path.size() - 1).getF_scores());

		/*
		 * ADAPT TRAVEL TIME OF THE PATH WITH TRUE CONGESTION LEVELS
		 */

            int cont = 0;
            String[] sequence = new String[(departure.getPlace_id().equals(arrival.getPlace_id()) || (departure.getPlace_id().equals("0") && arrival.getPlace_id().equals("00"))) ? to_visit.size() + 1 : to_visit.size()];
            for (Node n : path) {
                sequence[cont++] = n.getName();
                //logger.info("sequence["+(cont-1)+"]"+sequence[cont-1]);
            }
            Map<String[], Double> solution = new HashMap<>();
            solution.put(sequence, 0D);

            Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
                    departure.getPlace_id(),
                    arrival.getPlace_id(),
                    Graph.getSuccessorsList(solution));

            path = new PathFindingWithCrowding().AstarSearch(dao, departure, arrival, start_time, distances, graph, travel_times, crowding_levels, occupancies, 1d);
            logger.info("Crowd Related path with real cong.levels :" + path.get(0).getName() + "-" + path.get(path.size() - 1).getName() + ":" + path.get(path.size() - 1).getF_scores());

            POI from = null;
            POI to = null;
            double congestion = 0d;
            Path path_points = new Path();
            List<Activity> activities = new ArrayList<Activity>();
            for (Node n : path) {
                congestion += n.getCongestion_score();
                Activity current = new Activity();
                POI p = null;
                if (n.getName().equals("0") || n.getName().equals("00")) {
                    p = (n.getName().equals("0")) ? departure : arrival;
                } else {
                    //p = dao.retrieveActivity(n.getName());
                    for (POI poi : full_pois) {
                        if (n.getName().equals(poi.getPlace_id())) {
                            p = poi;
                            break;
                        }
                    }
                }
                current.setDeparture_time(TimeUtils.getStringTime(n.getDepartureTime()));
                current.setArrival_time(TimeUtils.getStringTime(n.getArrivalTime()));
                current.setVisit(p);
                if (from != null) {
                    to = p;
                }
                if (to != null) {
                	Path subpath = new TravelPath().compute(hopper, from, to);
                    path_points.addPoints(subpath.getPoints());
                    path_points.incrementLength(subpath.getLength());
                }
                from = p;
                activities.add(current);
            }

            String departure_time = activities.get(0).getDeparture_time();
            String arrival_time = activities.get(activities.size() - 1).getArrival_time();

            activities.remove(0);
            activities.remove(activities.size() - 1);

            return new VisitPlan(user, departure, arrival, departure_time, arrival_time, activities, new ArrayList<Activity>(), path_points, congestion / (activities.size() + 1d)); // +1 for the congestion is on the edges connecting activities

        } catch (Exception e) {
            // ugly but seems to work
            e.printStackTrace();
            return new VisitPlan();
        }
	}
	
	
	public VisitPlan updatePlan(Mongo dao, Visit last_visit, VisitPlan plan, List<String> POIsList, 
			List<POI> all_pois, TreeMap<String, TreeMap<String, Double>> distances, Map<String, HashMap<String, List<UncertainValue>>> travel_times, 
			Map<String, HashMap<String, List<UncertainValue>>> crowding_levels, 
			Map<String, List<Integer>> occupancies, double crowd_preference, GraphHopper hopper) {
		to_visit = new ArrayList<String>();
		for (String poi : POIsList) {
			to_visit.add(poi);
		}
		
		logger.info("user:"+plan.getUser());
		logger.info("start:"+plan.getDeparture().getPlace_id());
		logger.info("end:"+plan.getArrival().getPlace_id());
		logger.info("start_time:"+plan.getDeparture_time());
		
		/*
		 * COMPUTE CROWD RELATED PATH WITH CROWD PREFERENCE 
		 */
		
		POI from = null;
		POI to = null;
		List<Node> path = new ArrayList<Node>();
		List<Activity> activities = new ArrayList<Activity>();
		try {
			path = new PathFindingWithCrowding().AstarSearch(dao, last_visit.getVisited(), plan.getArrival(), TimeUtils.getStringTime(last_visit.getTime()%86400000L), to_visit, distances, travel_times, crowding_levels, occupancies, 10, crowd_preference);
			logger.info("Crowd Related path with preference <"+crowd_preference+"> :"+path.get(0).getName()+"-"+path.get(path.size()-1).getName()+":"+path.get(path.size()-1).getF_scores());
		} catch (Exception e) {
			e.printStackTrace();
			activities.add(new Activity("-1"));
			return new VisitPlan();
		}
		
		/*
		 * ADAPT TRAVEL TIME OF THE PATH WITH TRUE CONGESTION LEVELS
		 */
		
		int cont=0;
		String[] sequence = new String [(last_visit.getVisited().getPlace_id().equals(plan.getArrival().getPlace_id()) || (plan.getDeparture().getPlace_id().equals("0") && plan.getArrival().getPlace_id().equals("00"))) ? to_visit.size()+1 : to_visit.size()];
		for (Node n : path) {
			sequence[cont++] = n.getName();
		}
		Map<String[], Double> solution = new HashMap<>();
		solution.put(sequence, 0D);
		
		Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
				last_visit.getVisited().getPlace_id(),
				plan.getArrival().getPlace_id(),
				Graph.getSuccessorsList(solution));
		
		activities = new ArrayList<Activity>();
		try {
			path = new PathFindingWithCrowding().AstarSearch(dao, last_visit.getVisited(), plan.getArrival(), TimeUtils.getStringTime(last_visit.getTime()%86400000L), distances, graph, travel_times, crowding_levels, occupancies, 1d);
			logger.info("Crowd Related path with true cong.levels :"+path.get(0).getName()+"-"+path.get(path.size()-1).getName()+":"+path.get(path.size()-1).getF_scores());
		} catch (Exception e) {
			e.printStackTrace();
			activities.add(new Activity("-1"));
			return new VisitPlan();
		}
		
		from = null;
		to = null;
		double congestion = 0d;
		Path path_points = new Path();
		for (Node n : path) {
			congestion +=n.getCongestion_score();
			Activity current = new Activity();
			POI p = null;
			if (n.getName().equals("0") || n.getName().equals("00")) {
				p = (n.getName().equals("0")) ? last_visit.getVisited() : plan.getArrival();
			} else {
				for (POI poi : all_pois) {
					if (poi.getPlace_id().equals(n.getName())) {
						p = poi;
						break;
					}
				}
				//p = dao.retrieveActivity(n.getName());
			}
			current.setDeparture_time(TimeUtils.getStringTime(n.getDepartureTime()));
			current.setArrival_time(TimeUtils.getStringTime(n.getArrivalTime()));
			current.setVisit(p);
			if (from != null) {
				to = p;
			}
			if (to != null) {
				Path subpath = new TravelPath().compute(hopper, from, to);
				path_points.addPoints(subpath.getPoints());
				path_points.incrementLength(subpath.getLength());
			}
			from = p;
			activities.add(current);
		}

		activities.remove(0);
		activities.remove(activities.size()-1);
		//logger.info("activities:"+activities.toString());
		congestion = ((congestion/(activities.size()+1d))+(plan.getCrowding()*plan.getVisited().size()))/(plan.getVisited().size()+1d);
		plan.setTo_visit(activities);
		plan.setCrowding(congestion);
		
		plan.setPath(path_points);
		
		plan.computeHash();
		return plan;

	}

}
