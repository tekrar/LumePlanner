package io;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import model.*;

import org.apache.log4j.Logger;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import services.ComputeDistances;
import services.FindGreedyPath;
import services.FindCrowdRelatedPath;
import services.FindShortestPath;
import services.GetPOIs;
import util.DisableSSLCertificateCheckUtil;
import util.Occupancies;
import util.PointWithinBBox;
import util.TimeUtils;
import util.TravelTime;
import util.TrustSelfSignedCertHttpClientFactory;

//import redis.clients.jedis.Jedis;

@Controller
@RequestMapping("/")
public class RESTController {

	private static Properties p;

	private static RestTemplate restTemplate;

	private boolean initialized = false;

	private static List<POI> activities = new ArrayList<POI>();

	private static List<POI> restaurants = new ArrayList<POI>();

	private String last_crowding_levels = ""+Long.MIN_VALUE;
	private static Map<String, HashMap<String, List<UncertainValue>>> crowding_levels = new HashMap<String, HashMap<String,List<UncertainValue>>>();

	private static Map<String, List<Integer>> occupancies = new HashMap<String, List<Integer>>();

	private static Map<String, HashMap<String, List<UncertainValue>>> travel_times = new HashMap<String, HashMap<String, List<UncertainValue>>>();

	private Logger logger = Logger.getLogger(RESTController.class);

	private Mongo dao;

	private GraphHopper hopper;

	private String data_path;// = this.getClass().getResource("/../data/").getPath();

	private static TreeMap<String, TreeMap<String, Double>> distances;

	public RESTController() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));

			dao = new Mongo(p.getProperty("mongo.db"), p.getProperty("mongo.user"), p.getProperty("mongo.password"));
			hopper = new GraphHopper().forServer();


			data_path = this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath();
			hopper.setInMemory();
			hopper.setOSMFile(data_path+"bbox.osm");
			hopper.setGraphHopperLocation(data_path+"graph");
			hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
			hopper.importOrLoad();
		} catch(Exception e) {
			e.printStackTrace();
		}
		logger.info("Initialization for simulation experiment started");
	}

	public void setPath(String path) {
		data_path = path;
		hopper.setInMemory();
		hopper.setOSMFile(data_path+"bbox.osm");
		hopper.setGraphHopperLocation(data_path+"graph");
		hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
		hopper.importOrLoad();
	}

	public POI getActivity(String place_id) {
		for (POI current : activities) {
			if (current.getPlace_id().equals(place_id)) {
				return current;
			}
		}
		return null;
	}

	@RequestMapping(value = "signin", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody Integer performLogin(@RequestBody User user) {
		return dao.Login(user);
	}

	@RequestMapping(value = "signup", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean performSignup(@RequestBody User user) {
		return dao.Signup(user);
	}


	@RequestMapping(value = "selectcity", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean selectCity(@RequestBody CitySelection citySelection) {
		logger.info(citySelection.getEmail()+" ---- is in ---> "+citySelection.getCity());
		dao = new Mongo("lume-"+citySelection.getCity(), p.getProperty("mongo.user"), p.getProperty("mongo.password"));
		return true;
	}


	/**
	 * Retrieve POIs, compute haversine distances, integrate POIs with visiting times from venice
	 * @return Status of the initialisation process
	 * @throws IOException 
	 */
	public void init() {
		logger.info("Server initialization started");

		p = new Properties();


		//new SocialPulse().writeCrowdPOI();
		//logger.info("POI written");

		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
			DisableSSLCertificateCheckUtil.disableChecks();
			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(new TrustSelfSignedCertHttpClientFactory().getObject());
			restTemplate = new RestTemplate(requestFactory);
			//restTemplate = new RestTemplate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!dao.checkActivities()) {

			logger.info("/../data/"+p.getProperty("data.dir"));

			new GetPOIs().run(dao, this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath());
			logger.info("POIs collected from OSM API");
		}
		activities = dao.retrieveActivities();
		restaurants = dao.retrieveRestaurants();
		logger.info("Activities retrieved from Mongodb (count "+activities.size()+")");
		logger.info("Restaurants retrieved from Mongodb (count "+restaurants.size()+")");

		if (!dao.checkDistances()) {
			new ComputeDistances().run(dao, activities);
			logger.info("Distances among activities computed");
		}
		distances = dao.retrieveDistances();
		logger.info("Look-up table for distances initialized (count "+distances.size()+")");

		if (!dao.checkTravelTimes()) {
			travel_times = new TravelTime().initTravelTimeFromPOIs(dao, activities, this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath());
		} else {
			travel_times = dao.retrieveTravelTimes();
		}
		logger.info("Look-up table for travel times initialized (count "+travel_times.size()+")");

		occupancies = new Occupancies().init(activities);

		logger.info("Look-up table for POIs occupancies initialized (count "+occupancies.size()+")");

		initCrowdingModule();

		logger.info("Look-up table for congestion levels was initialized at "+last_crowding_levels);
		last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
		logger.info("Look-up table for congestion levels initialized at "+last_crowding_levels);

		initialized = true;



		logger.info("\n\n\n\t\t*********************************************\n"
				+ "\t\t*******Server successfully initialized*******\n"
				+ "\t\t*********************************************\n\n\n");

	}


	private boolean initCrowdingModule() {
		return restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"init", Boolean.class);
	}


	/**
	 * Load the POIs from the DB (if necessary) and compute the congestion_levels and the travel_times for the day
	 * @return Status of the update process
	 * @throws IOException 
	 */

//	@Scheduled(fixedRate = 3600000) //hourly
//	public void updateOccupancies() throws IOException {
//		if (!initialized) {
//			init();
//		} else {
//			DateFormat hourFormatter = new SimpleDateFormat("hh");
//			DateFormat minuteFormatter = new SimpleDateFormat("mm");
//			Date d = new Date();
//			hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
//			String hour = hourFormatter.format(d);
//			String minute = minuteFormatter.format(d);
//
//			logger.info("Server update request at "+hour+":"+minute);
//
//			hour = (Integer.parseInt(minute) > 30 ) ? ((hour.equals("23")) ? "00" : (Integer.parseInt(hour)+1)+"") : hour; 
//
//			occupancies = new Occupancies().update(hour, minute, occupancies);
//			logger.info("Look-up table for occupancies successfully updated");
//			//travel_times = DOCIT.getTravelTime(POIs);
//			//travel_times = new TravelTime().updateTravelTime(travel_times, Integer.parseInt(hour), this.getClass().getResource("/../data/").getPath());
//			//logger.info("Look-up table for travel times successfully updated");
//
//			logger.info("\n\n\n\t\t*********************************************\n"
//					+ "\t\t*********Server successfully updated*********\n"
//					+ "\t\t****************(occupancies)****************\n\n\n");
//		}
//	}

	@Scheduled(fixedRate = 900000) //every 15 minutes
	public void updateCongestions() throws IOException {
		if (!initialized) {
			init();
		} else {
			DateFormat hourFormatter = new SimpleDateFormat("hh");
			DateFormat minuteFormatter = new SimpleDateFormat("mm");
			Date d = new Date();
			hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			String hour = hourFormatter.format(d);
			String minute = minuteFormatter.format(d);

			logger.info("Server update request at "+hour+":"+minute);

			last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
			logger.info("Look-up table for congestion levels initialized at "+TimeUtils.getStringTime(Long.parseLong(last_crowding_levels)));

			//travel_times = DOCIT.getTravelTime(POIs);
			//travel_times = new TravelTime().updateTravelTime(travel_times, Integer.parseInt(hour), this.getClass().getResource("/../data/").getPath());
			logger.info("Look-up table for travel times successfully updated");

			logger.info("\n\n\n\t\t*********************************************\n"
					+ "\t\t*********Server successfully updated*********\n"
					+ "\t\t****************(congestions)****************\n\n\n");
		}
	}


	/**
	 * 
	 */
	@RequestMapping(value = "activities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI> sendActivities() {
		return dao.retrieveActivities();
	}

	/**
	 * Compute the visiting plan for the POIs 
	 * @param plan_request sent w/ POST containing the list of POIs selected from user in json format
	 * @return Suggested visiting sequence for the requested set of POIs
	 */

	@RequestMapping(value = "newplan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody VisitPlanAlternatives getNewVisitPlan(@RequestBody PlanRequest plan_request) {

		POI start = plan_request.getStart_place();
		POI end = plan_request.getEnd_place();
		String start_time = plan_request.getStart_time();
		List<String> pois = plan_request.getVisits();
		List<String> POIsList = new ArrayList<String>();
		POI departure = null;
		POI arrival = null;
		//List<Activity> result = new ArrayList<Activity>();
		if (start.getPlace_id().equals("0")) {
			if (!new PointWithinBBox().check(start.getGeometry().getCoordinates())) {
				//result.add(new Activity("0"));
				return new VisitPlanAlternatives(new VisitPlan(), new VisitPlan(), 
						new VisitPlan(new POI("0")), 0);
			}
			departure = new POI("0", 
					start.getGeometry().getCoordinates().getLatitude(), 
					start.getGeometry().getCoordinates().getLongitude(), 
					"Current Location");
		} else {
			//departure = dao.retrieveActivity(start.getPlace_id());
			departure = start;
		}
		if (end.getPlace_id().equals("0")) {
			if (!new PointWithinBBox().check(end.getGeometry().getCoordinates())) {
				//result.add(new Activity("00"));
				return new VisitPlanAlternatives(new VisitPlan(), new VisitPlan(),
						new VisitPlan(new POI("00")), 0);
			}
			arrival = new POI("00", 
					end.getGeometry().getCoordinates().getLatitude(), 
					end.getGeometry().getCoordinates().getLongitude(), 
					"Current Location");
		} else {
			//arrival = dao.retrieveActivity(end.getPlace_id());
			arrival = end;
		}
		//if start!=end insert both id in the list
		if (!start.getPlace_id().equals(end.getPlace_id()) && 
				!(start.getPlace_id().equals("0") && end.getPlace_id().equals("00"))) {
			POIsList.add(departure.getPlace_id());
		}
		//insert only one, otherwise
		POIsList.add(arrival.getPlace_id());

		for (String poi : pois) {
			POIsList.add(poi);
		}
		logger.info("USER: "+plan_request.getUser()+"   "+"TIME: "+plan_request.getStart_time());
		logger.info("PLAN REQUEST: "+POIsList.toString()+"\n");
		logger.info("DEP: "+departure.toString());
		logger.info("ARR: "+arrival.toString());

		VisitPlan greedy = new FindGreedyPath().newPlan(dao, plan_request.getUser(), departure, arrival, start_time, POIsList, activities, distances, travel_times, crowding_levels, occupancies, hopper);
		logger.info("Greedy computed " + greedy.getUser());
		VisitPlan shortest = new FindShortestPath().newPlan(dao, plan_request.getUser(), departure, arrival, start_time, POIsList, activities, distances, travel_times, crowding_levels, occupancies, hopper);
		logger.info("Shortest computed " + shortest.getUser());
		VisitPlan lesscrowded = new FindCrowdRelatedPath().newPlan(dao, plan_request.getUser(), departure, arrival, start_time, POIsList, activities, distances, travel_times, crowding_levels, occupancies, plan_request.getCrowd_preference(), hopper);
		logger.info("Crowd Related computed " + lesscrowded.getUser());

		logger.info("newPlan user:"+plan_request.getUser());
		return new VisitPlanAlternatives(greedy, shortest, lesscrowded, plan_request.getCrowd_preference());
	}


	@RequestMapping(value = "accept_plan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean acceptVisitPlan(@RequestBody VisitPlanAlternatives plans) {
		//		VisitPlan plan_accepted = plans.getCrowd_related();
		//		//insert new plan
		//		if (!dao.insertPlan(plans)) return false;
		//
		//		//if the visiting has started before (at least 1 visited activity)
		//		//the crowding and occupancies update is done at the previous step (see: addVisitedAndReplan)
		//		if (!plan_accepted.getVisited().isEmpty()) return true;
		//
		//		logger.info("VISITING PLAN ACCEPTED\n");
		//		//update crowding levels with respect to the plan
		//		String time = plan_accepted.getDeparture_time();
		//		String from = (plan_accepted.getDeparture().getPlace_id() =="0") ? dao.retrieveClosestActivity(plan_accepted.getDeparture()).getPlace_id() : plan_accepted.getDeparture().getPlace_id();
		//		String to="";
		//		String arr_t="";
		//		String dep_t = "";
		//		int warning_count = 0;
		//		for (int i=0; i<plan_accepted.getTo_visit().size(); i+=1) {
		//			to = plan_accepted.getTo_visit().get(i).getVisit().getPlace_id();
		//			arr_t = plan_accepted.getTo_visit().get(i).getArrival_time();
		//			dep_t = plan_accepted.getTo_visit().get(i).getDeparture_time();
		//			occupancies = new Occupancies().increase(to, arr_t, dep_t, occupancies);
		//			while (warning_count< 3 && !restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+from+"/"+to+"/"+TimeUtils.getMillis(time), Boolean.class)) {
		//				warning_count+=1;
		//			}
		//			if (warning_count==3) return false;
		//			warning_count=0;
		//			from = to;
		//			time = plan_accepted.getTo_visit().get(i).getDeparture_time();
		//		}
		//		to = (plan_accepted.getArrival().getPlace_id() =="00") ? dao.retrieveClosestActivity(plan_accepted.getArrival()).getPlace_id() : plan_accepted.getArrival().getPlace_id();
		//		while (warning_count< 3 && !restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+from+"/"+to+"/"+TimeUtils.getMillis(time), Boolean.class)) {
		//			warning_count+=1;
		//		}
		//		if (warning_count==3) return false;
		return acceptVisitPlanWithType(0, plans);
	}

	public boolean acceptVisitPlanWithType(int type, VisitPlanAlternatives plans) {
		if (!dao.insertPlan(plans)) return false;

		VisitPlan plan_accepted = null;

		switch (type) {
		case 1: // greedy
			plan_accepted = plans.getGreedy();
			break;
		case 2: // shortest
			plan_accepted = plans.getShortest();
			break;
		default:
			plan_accepted = plans.getCrowd_related();
		}
		//insert new plan


		//if the visiting has started before (at least 1 visited activity)
		//the crowding and occupancies update is done at the previous step (see: addVisitedAndReplan)
		if (!plan_accepted.getVisited().isEmpty()) return true;

		logger.info("VISITING PLAN ACCEPTED\n");
		logger.info("Departure:"+plan_accepted.getDeparture().toString());
		logger.info("Arrival:"+plan_accepted.getArrival().toString());
		//update crowding levels with respect to the plan
		String time = plan_accepted.getDeparture_time();
		String from = (plan_accepted.getDeparture().getPlace_id() =="0") ? dao.retrieveClosestActivity(plan_accepted.getDeparture()).getPlace_id() : plan_accepted.getDeparture().getPlace_id();
		String to="";
		String arr_t="";
		String dep_t = "";
		int warning_count = 0;
		for (int i=0; i<plan_accepted.getTo_visit().size(); i+=1) {
			to = plan_accepted.getTo_visit().get(i).getVisit().getPlace_id();
			arr_t = plan_accepted.getTo_visit().get(i).getArrival_time();
			dep_t = plan_accepted.getTo_visit().get(i).getDeparture_time();
			occupancies = new Occupancies().increase(to, arr_t, dep_t, occupancies);
			while (warning_count< 3 && !restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+from+"/"+to+"/"+TimeUtils.getMillis(time), Boolean.class)) {
				warning_count+=1;
			}
			if (warning_count==3) return false;
			warning_count=0;
			from = to;
			time = plan_accepted.getTo_visit().get(i).getDeparture_time();
		}
		to = (plan_accepted.getArrival().getPlace_id() =="00") ? dao.retrieveClosestActivity(plan_accepted.getArrival()).getPlace_id() : plan_accepted.getArrival().getPlace_id();
		while (warning_count< 3 && !restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+from+"/"+to+"/"+TimeUtils.getMillis(time), Boolean.class)) {
			warning_count+=1;
		}
		if (warning_count==3) {
			System.out.println("error contacting congestion module");
			return false;
		}
		return true;
	}


	@RequestMapping(value = "plan", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives getPlan(@RequestBody User user) {
		return dao.retrievePlan(user.getEmail());
	}


	@RequestMapping(value = "crowding_fdbk", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean setCrowdingFdbk(@RequestBody CrowdingFeedback fdbk) {
		logger.info("Crowding Feedback from "+fdbk.getUser());
		logger.info("on Path "+fdbk.getDeparture().getPlace_id()+"-"+fdbk.getArrival().getPlace_id()+" ("+fdbk.getDeparture_time()+") ");
		logger.info("Value "+fdbk.getChoice());

		UncertainValue value = restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding_fdbk/{user}/{departure}/{arrival}/{dep_time}/{choice}",
				UncertainValue.class,
				fdbk.getUser(),	
				fdbk.getDeparture().getPlace_id(), 
				fdbk.getArrival().getPlace_id(), 
				fdbk.getDeparture_time(), 
				fdbk.getChoice());
		return dao.updateUser(fdbk, value);
	}


	@RequestMapping(value = "visited", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives addVisitedAndReplan(@RequestBody Visit new_visited) {
//		VisitPlanAlternatives plans = dao.updatePlan(new_visited);
//		if (null == plans) return null;
//
//		logger.info(plans.toString());
//		VisitPlan v = plans.getCrowd_related();
//		logger.info(v.toString());
//		if (!v.getTo_visit().isEmpty()) {
//			List<String> pois = new ArrayList<String>();
//
//			for (Activity to_visit : v.getTo_visit()) {
//				pois.add(to_visit.getVisit().getPlace_id());
//			}
//			if (!v.getArrival().equals(new_visited.getVisited())) {
//				pois.add(new_visited.getVisited().getPlace_id());
//			}
//			pois.add(v.getArrival().getPlace_id());	
//
//			//String data_path = this.getClass().getResource("/../data/").getPath();
//
//			VisitPlan leastCrowded = new FindCrowdRelatedPath().updatePlan(dao, new_visited, plans.getCrowd_related(), pois, activities, distances, travel_times, crowding_levels, occupancies, plans.getCrowd_preference(), hopper);
//			for (int i=0; i<v.getTo_visit().size();i++) {
//				Activity to_visit = v.getTo_visit().get(i);
//				for (int j=0; j<leastCrowded.getTo_visit().size(); j++) {
//					boolean different_arr_time = false;
//					Activity to_visit_new = v.getTo_visit().get(j);
//					if (to_visit.getVisit().getPlace_id().equals(to_visit_new.getVisit().getPlace_id())) {
//						if (!to_visit.getArrival_time().equals(to_visit_new.getArrival_time())) {
//							if (i==0) { //decrease crowding level from last visited to first activity to visit
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+new_visited.getVisited().getPlace_id()+"/"+to_visit.getVisit().getPlace_id()+"/"+new_visited.getTime(), Boolean.class);
//								logger.info("Updated (dec) crowding level from "+new_visited.getVisited().getPlace_id()+" to "+to_visit.getVisit().getPlace_id()+" at "+TimeUtils.getStringTime(new_visited.getTime()));
//							} else {
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+v.getTo_visit().get(i-1).getVisit().getPlace_id()+"/"+to_visit.getVisit().getPlace_id()+"/"+TimeUtils.getMillis(v.getTo_visit().get(i-1).getDeparture_time()), Boolean.class);
//								logger.info("Updated (dec) crowding level from "+v.getTo_visit().get(i-1).getVisit().getPlace_id()+" to "+to_visit.getVisit().getPlace_id()+" at "+v.getTo_visit().get(i-1).getDeparture_time());
//							}
//							if (j==0) { //increase crowding level from last visited to first activity to visit
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+new_visited.getVisited().getPlace_id()+"/"+to_visit_new.getVisit().getPlace_id()+"/"+new_visited.getTime(), Boolean.class);
//								logger.info("Updated (inc) crowding level from "+new_visited.getVisited().getPlace_id()+" to "+to_visit_new.getVisit().getPlace_id()+" at "+TimeUtils.getStringTime(new_visited.getTime()));
//							} else {
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+leastCrowded.getTo_visit().get(j-1).getVisit().getPlace_id()+"/"+to_visit_new.getVisit().getPlace_id()+"/"+TimeUtils.getMillis(leastCrowded.getTo_visit().get(j-1).getDeparture_time()), Boolean.class);
//								logger.info("Updated (inc) crowding level from "+leastCrowded.getTo_visit().get(j-1).getVisit().getPlace_id()+" to "+to_visit_new.getVisit().getPlace_id()+" at "+leastCrowded.getTo_visit().get(j-1).getDeparture_time());
//							}
//							occupancies = new Occupancies().decrease(to_visit.getVisit().getPlace_id(), to_visit.getArrival_time(), to_visit.getDeparture_time(), occupancies);
//							logger.info("Updated (dec) occupancy from "+to_visit.getArrival_time()+" to "+to_visit.getDeparture_time()+" at "+to_visit.getVisit().getPlace_id());
//							occupancies = new Occupancies().increase(to_visit_new.getVisit().getPlace_id(), to_visit_new.getArrival_time(), to_visit_new.getDeparture_time(), occupancies);
//							logger.info("Updated (inc) occupancy from "+to_visit_new.getArrival_time()+" to "+to_visit_new.getDeparture_time()+" at "+to_visit_new.getVisit().getPlace_id());
//							different_arr_time = true;
//						}
//						if (!to_visit.getDeparture_time().equals(to_visit_new.getDeparture_time())) {
//							if (i==v.getTo_visit().size()-1) { //decrease crowding level from last activity to visit to the arrival location
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+to_visit.getVisit().getPlace_id()+"/"+v.getArrival().getPlace_id()+"/"+TimeUtils.getMillis(to_visit.getDeparture_time()), Boolean.class);
//								logger.info("Updated (dec) crowding level from "+to_visit.getVisit().getPlace_id()+" to "+v.getArrival().getPlace_id()+" at "+to_visit.getDeparture_time());
//							} else {
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+to_visit.getVisit().getPlace_id()+"/"+v.getTo_visit().get(i+1).getVisit().getPlace_id()+"/"+TimeUtils.getMillis(to_visit.getDeparture_time()), Boolean.class);
//								logger.info("Updated (dec) crowding level from "+to_visit.getVisit().getPlace_id()+" to "+v.getTo_visit().get(i+1).getVisit().getPlace_id()+" at "+to_visit.getDeparture_time());
//							}
//							if (j==leastCrowded.getTo_visit().size()-1) { //decrease crowding level from last activity to visit to the arrival location
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+to_visit_new.getVisit().getPlace_id()+"/"+leastCrowded.getArrival().getPlace_id()+"/"+TimeUtils.getMillis(to_visit_new.getDeparture_time()), Boolean.class);
//								logger.info("Updated (inc) crowding level from "+to_visit_new.getVisit().getPlace_id()+" to "+leastCrowded.getArrival().getPlace_id()+" at "+to_visit_new.getDeparture_time());
//							} else {
//								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+to_visit_new.getVisit().getPlace_id()+"/"+leastCrowded.getTo_visit().get(j+1).getVisit().getPlace_id()+"/"+TimeUtils.getMillis(to_visit_new.getDeparture_time()), Boolean.class);
//								logger.info("Updated (inc) crowding level from "+to_visit_new.getVisit().getPlace_id()+" to "+leastCrowded.getTo_visit().get(j+1).getVisit().getPlace_id()+" at "+to_visit_new.getDeparture_time());
//							}
//							if (!different_arr_time) {
//								occupancies = new Occupancies().decrease(to_visit.getVisit().getPlace_id(), to_visit.getArrival_time(), to_visit.getDeparture_time(), occupancies);
//								logger.info("Updated (dec) occupancy from "+to_visit.getArrival_time()+" to "+to_visit.getDeparture_time()+" at "+to_visit.getVisit().getPlace_id());
//								occupancies = new Occupancies().increase(to_visit_new.getVisit().getPlace_id(), to_visit_new.getArrival_time(), to_visit_new.getDeparture_time(), occupancies);
//								logger.info("Updated (inc) occupancy from "+to_visit_new.getArrival_time()+" to "+to_visit_new.getDeparture_time()+" at "+to_visit_new.getVisit().getPlace_id());
//							}
//						}
//					}
//				}
//			}
//			//restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+from+"/"+to+"/"+TimeUtils.getMillis(time), Boolean.class)
//			return new VisitPlanAlternatives(
//					new FindGreedyPath().updatePlan(dao, new_visited, plans.getGreedy(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
//					new FindShortestPath().updatePlan(dao, new_visited, plans.getShortest(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
//					leastCrowded, plans.getCrowd_preference());
//		}

		return addVisitedAndReplanWithType(0, new_visited);

	}


	public VisitPlanAlternatives addVisitedAndReplanWithType(int type, Visit new_visited) {
		VisitPlanAlternatives plans = dao.updatePlan(new_visited);
		if (null == plans) return null;

		//logger.info(plans.toString());
		VisitPlan currentP = null;
		

		switch (type) {
		case 1: // greedy
			currentP = plans.getGreedy();
			break;
		case 2: // shortest
			currentP = plans.getShortest();
			break;
		default: //0
			currentP = plans.getCrowd_related();			
		}

		//logger.info(v.toString());
		if (!currentP.getTo_visit().isEmpty()) {
			
			List<String> pois = new ArrayList<String>();

			for (Activity to_visit : currentP.getTo_visit()) {
				pois.add(to_visit.getVisit().getPlace_id());
			}
			if (!currentP.getArrival().equals(new_visited.getVisited())) {
				pois.add(new_visited.getVisited().getPlace_id());
			}
			pois.add(currentP.getArrival().getPlace_id());	

			VisitPlan newP = null;
			
			switch (type) {
			case 1: // greedy
				newP = new FindGreedyPath().updatePlan(dao, new_visited, plans.getGreedy(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper);
				break;
			case 2: // shortest
				newP = new FindShortestPath().updatePlan(dao, new_visited, plans.getShortest(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper);
				break;
			default: //0
				newP = new FindCrowdRelatedPath().updatePlan(dao, new_visited, plans.getCrowd_related(), pois, activities, distances, travel_times, crowding_levels, occupancies, plans.getCrowd_preference(), hopper);			
			}
			
			//VisitPlan leastCrowded = new FindCrowdRelatedPath().updatePlan(dao, new_visited, plans.getCrowd_related(), pois, activities, distances, travel_times, crowding_levels, occupancies, plans.getCrowd_preference(), hopper);
			for (int i=0; i<currentP.getTo_visit().size();i++) {
				Activity to_visit = currentP.getTo_visit().get(i);
				for (int j=0; j<newP.getTo_visit().size(); j++) {
					boolean different_arr_time = false;
					Activity to_visit_new = newP.getTo_visit().get(j);
					if (to_visit.getVisit().getPlace_id().equals(to_visit_new.getVisit().getPlace_id())) {
						if (!to_visit.getArrival_time().equals(to_visit_new.getArrival_time())) {
							if (i==0) { //decrease crowding level from last visited to first activity to visit
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+new_visited.getVisited().getPlace_id()+"/"+to_visit.getVisit().getPlace_id()+"/"+new_visited.getTime(), Boolean.class);
								logger.info("Updated (dec) crowding level from "+new_visited.getVisited().getPlace_id()+" to "+to_visit.getVisit().getPlace_id()+" at "+TimeUtils.getStringTime(new_visited.getTime()));
							} else {
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+currentP.getTo_visit().get(i-1).getVisit().getPlace_id()+"/"+to_visit.getVisit().getPlace_id()+"/"+TimeUtils.getMillis(currentP.getTo_visit().get(i-1).getDeparture_time()), Boolean.class);
								logger.info("Updated (dec) crowding level from "+currentP.getTo_visit().get(i-1).getVisit().getPlace_id()+" to "+to_visit.getVisit().getPlace_id()+" at "+currentP.getTo_visit().get(i-1).getDeparture_time());
							}
							if (j==0) { //increase crowding level from last visited to first activity to visit
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+new_visited.getVisited().getPlace_id()+"/"+to_visit_new.getVisit().getPlace_id()+"/"+new_visited.getTime(), Boolean.class);
								logger.info("Updated (inc) crowding level from "+new_visited.getVisited().getPlace_id()+" to "+to_visit_new.getVisit().getPlace_id()+" at "+TimeUtils.getStringTime(new_visited.getTime()));
							} else {
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+newP.getTo_visit().get(j-1).getVisit().getPlace_id()+"/"+to_visit_new.getVisit().getPlace_id()+"/"+TimeUtils.getMillis(newP.getTo_visit().get(j-1).getDeparture_time()), Boolean.class);
								logger.info("Updated (inc) crowding level from "+newP.getTo_visit().get(j-1).getVisit().getPlace_id()+" to "+to_visit_new.getVisit().getPlace_id()+" at "+newP.getTo_visit().get(j-1).getDeparture_time());
							}
							occupancies = new Occupancies().decrease(to_visit.getVisit().getPlace_id(), to_visit.getArrival_time(), to_visit.getDeparture_time(), occupancies);
							logger.info("Updated (dec) occupancy from "+to_visit.getArrival_time()+" to "+to_visit.getDeparture_time()+" at "+to_visit.getVisit().getPlace_id());
							occupancies = new Occupancies().increase(to_visit_new.getVisit().getPlace_id(), to_visit_new.getArrival_time(), to_visit_new.getDeparture_time(), occupancies);
							logger.info("Updated (inc) occupancy from "+to_visit_new.getArrival_time()+" to "+to_visit_new.getDeparture_time()+" at "+to_visit_new.getVisit().getPlace_id());
							different_arr_time = true;
						}
						if (!to_visit.getDeparture_time().equals(to_visit_new.getDeparture_time())) {
							if (i==currentP.getTo_visit().size()-1) { //decrease crowding level from last activity to visit to the arrival location
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+to_visit.getVisit().getPlace_id()+"/"+currentP.getArrival().getPlace_id()+"/"+TimeUtils.getMillis(to_visit.getDeparture_time()), Boolean.class);
								logger.info("Updated (dec) crowding level from "+to_visit.getVisit().getPlace_id()+" to "+currentP.getArrival().getPlace_id()+" at "+to_visit.getDeparture_time());
							} else {
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/d/"+to_visit.getVisit().getPlace_id()+"/"+currentP.getTo_visit().get(i+1).getVisit().getPlace_id()+"/"+TimeUtils.getMillis(to_visit.getDeparture_time()), Boolean.class);
								logger.info("Updated (dec) crowding level from "+to_visit.getVisit().getPlace_id()+" to "+currentP.getTo_visit().get(i+1).getVisit().getPlace_id()+" at "+to_visit.getDeparture_time());
							}
							if (j==newP.getTo_visit().size()-1) { //decrease crowding level from last activity to visit to the arrival location
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+to_visit_new.getVisit().getPlace_id()+"/"+newP.getArrival().getPlace_id()+"/"+TimeUtils.getMillis(to_visit_new.getDeparture_time()), Boolean.class);
								logger.info("Updated (inc) crowding level from "+to_visit_new.getVisit().getPlace_id()+" to "+newP.getArrival().getPlace_id()+" at "+to_visit_new.getDeparture_time());
							} else {
								restTemplate.getForObject(p.getProperty("crowdingmodule.url")+"crowding/i/"+to_visit_new.getVisit().getPlace_id()+"/"+newP.getTo_visit().get(j+1).getVisit().getPlace_id()+"/"+TimeUtils.getMillis(to_visit_new.getDeparture_time()), Boolean.class);
								logger.info("Updated (inc) crowding level from "+to_visit_new.getVisit().getPlace_id()+" to "+newP.getTo_visit().get(j+1).getVisit().getPlace_id()+" at "+to_visit_new.getDeparture_time());
							}
							if (!different_arr_time) {
								occupancies = new Occupancies().decrease(to_visit.getVisit().getPlace_id(), to_visit.getArrival_time(), to_visit.getDeparture_time(), occupancies);
								logger.info("Updated (dec) occupancy from "+to_visit.getArrival_time()+" to "+to_visit.getDeparture_time()+" at "+to_visit.getVisit().getPlace_id());
								occupancies = new Occupancies().increase(to_visit_new.getVisit().getPlace_id(), to_visit_new.getArrival_time(), to_visit_new.getDeparture_time(), occupancies);
								logger.info("Updated (inc) occupancy from "+to_visit_new.getArrival_time()+" to "+to_visit_new.getDeparture_time()+" at "+to_visit_new.getVisit().getPlace_id());
							}
						}
					}
				}
			}

			switch (type) {
			case 1: // greedy
				return new VisitPlanAlternatives(
						newP,
						new FindShortestPath().updatePlan(dao, new_visited, plans.getShortest(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
						new FindCrowdRelatedPath().updatePlan(dao, new_visited, plans.getCrowd_related(), pois, activities, distances, travel_times, crowding_levels, occupancies, plans.getCrowd_preference(), hopper),
						plans.getCrowd_preference());
			case 2: // shortest
				return new VisitPlanAlternatives(
						new FindGreedyPath().updatePlan(dao, new_visited, plans.getGreedy(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
						newP,
						new FindCrowdRelatedPath().updatePlan(dao, new_visited, plans.getCrowd_related(), pois, activities, distances, travel_times, crowding_levels, occupancies, plans.getCrowd_preference(), hopper),
						plans.getCrowd_preference());
			default: //0
				return new VisitPlanAlternatives(
						new FindGreedyPath().updatePlan(dao, new_visited, plans.getGreedy(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
						new FindShortestPath().updatePlan(dao, new_visited, plans.getShortest(), pois, activities, distances, travel_times, crowding_levels, occupancies, hopper),
						newP, plans.getCrowd_preference());			
			}
			
		}

		return plans;

	}

	@RequestMapping(value = "ov_crowding_fdbk", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean setOverallCrowdingFdbk(@RequestBody OverallFeedback fdbk) {
		logger.info("Overall Crowding Feedback from "+fdbk.getUser());
		logger.info("Value "+fdbk.getChoice());

		return dao.updateUserOv_Cr(fdbk);
	}

	@RequestMapping(value = "ov_plan_fdbk", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean setOverallPlanFdbk(@RequestBody OverallFeedback fdbk) {
		logger.info("Overall Plan Feedback from "+fdbk.getUser());
		logger.info("Value "+fdbk.getChoice());

		return dao.updateUserOv_Pl(fdbk);
	}

	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody User user) {
		logger.info("User "+user.getEmail()+" completed his visiting plan");
		return dao.deletePlan(user.getEmail());
	}

	public void askUpdate() {
		try {
			updateCongestions();
			//updateOccupancies();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public List<GridCrowding> askCrowdDump() {
		return dao.retrieveGridCrowding();
	}

}