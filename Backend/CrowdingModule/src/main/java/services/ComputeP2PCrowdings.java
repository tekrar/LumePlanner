package services;

import io.Mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import util.RandomValue;
import util.TimeUtils;
import model.P2PCellPaths;
import model.POI;
import model.POI2POICrowding;
import model.UncertainValue;
public class ComputeP2PCrowdings {

	private Logger logger = Logger.getLogger(ComputeP2PCrowdings.class);

	public Map<String, Map<String, Map<Integer, Map<String, Double>>>> insertCellPaths(Mongo dao, List<POI> POIs) {
		
		Map<String, Map<String, Map<Integer, Map<String, Double>>>> output = new HashMap<>();
		
		try {

			Map<Integer, Map<String, Double>> map_path_distances = new HashMap<>();

			int cont = 0;
			for (POI from : POIs) {
				for (POI to : POIs) {
					if (!from.equals(to)) {
						if (!output.containsKey(from.getPlace_id())) {
							output.put(from.getPlace_id(), new HashMap<String, Map<Integer, Map<String, Double>>>());
						}

						cont+=1;
						if (cont%1000==0) logger.info(cont);
						//logger.info("From "+from.getPlace_id()+" To "+to.getPlace_id());
						map_path_distances = new ComputeCellPathAndDistances().run(dao, from.getPlace_id(), to.getPlace_id());
						output.get(from.getPlace_id()).put(to.getPlace_id(), map_path_distances);
						dao.insertP2PPaths(new P2PCellPaths(from.getPlace_id(), to.getPlace_id(), map_path_distances));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return output;
	}


	public List<POI2POICrowding> run(Mongo dao, Map<String, Map<String, Map<Integer, Map<String, Double>>>> p2p_map_path_distances, Map<String, List<UncertainValue>> grid_crowdings, Map<String, HashMap<String, List<UncertainValue>>> travel_times) {

		List<POI2POICrowding> result = new ArrayList<>();

		int cont = 0;
		for (String from : p2p_map_path_distances.keySet()) {
			for (String to : p2p_map_path_distances.get(from).keySet()) {
				if (!from.equals(to)) {

					Double travelling_distance = getTotalDistance(p2p_map_path_distances.get(from).get(to));

					//compute the distance covered in each cell
					List<Double> distance_rate = new ArrayList<>();
					for (Integer sequence_id : p2p_map_path_distances.get(from).get(to).keySet()) {
						for (String c : p2p_map_path_distances.get(from).get(to).get(sequence_id).keySet()) {
							distance_rate.add(sequence_id, p2p_map_path_distances.get(from).get(to).get(sequence_id).get(c)/travelling_distance);
						}
					}

					List<UncertainValue> crowdings = new ArrayList<UncertainValue>();
					for (long dep_time = 0l; dep_time<86400000l; dep_time+=900000) {
						Long arr_time_inCell = dep_time;
						Long dep_time_fromCell = dep_time;
						//logger.info("from:"+from.getPlace_id()+"\tto:"+to.getPlace_id()+"\ttime_slot:"+TimeUtils.getTimeSlot(arr_time_inCell));
						//logger.info("travel_times:"+travel_times.get(from.getPlace_id()).get(to.getPlace_id()).toString());

						Long travelling_time = ((long)RandomValue.get(travel_times.get(from).get(to).get(TimeUtils.getTimeSlot(arr_time_inCell))))*60*1000l; 
						//compute the travelling time per every cell
						List<Long> travel_byCell = new ArrayList<>();
						for (int sequence_id = 0; sequence_id<distance_rate.size(); sequence_id++) {
							travel_byCell.add(sequence_id, (long) (distance_rate.get(sequence_id)*travelling_time));
						}
						Double sum_crowdings = 0D;
						for (int i=0; i< travel_byCell.size(); i++) {
							dep_time_fromCell += travel_byCell.get(i);
							String current = p2p_map_path_distances.get(from).get(to).get(i).keySet().iterator().next();
							sum_crowdings += findCrowding(grid_crowdings, current, arr_time_inCell, dep_time_fromCell)*distance_rate.get(i);
							arr_time_inCell += travel_byCell.get(i);
						}
						crowdings.add((int)dep_time/900000, new UncertainValue(sum_crowdings/p2p_map_path_distances.get(from).get(to).size(), "N:"+sum_crowdings/p2p_map_path_distances.get(from).get(to).size()/10d));


					}
					cont+=1;
					if (cont%1000==0) logger.info(cont);
					POI2POICrowding current = new POI2POICrowding(from, to, crowdings); 
					dao.updateCrowding(current);
					result.add(current);


				}
			}
		}
		return result;
	}


	public Double findCrowding(Map<String, List<UncertainValue>> grid_crowdings, String current_cell, Long arr_time_inCell, Long dep_time_fromCell) {
		Long start_time = TimeUtils.getMillis15MRoundTime(arr_time_inCell);
		Long end_time = TimeUtils.getMillis15MRoundTime(dep_time_fromCell);
		if (end_time<start_time) end_time += 86400000L;
		int number_time_slots = 1 + ((int)(end_time - start_time))/900000;

		//logger.info(current.getId()+" ("+arr_time_inCell+ "-"+dep_time_fromCell+") ["+number_time_slots+"] ("+start_time+"-"+end_time+")");

		double max_mean = Double.MIN_VALUE;
		for (int i=0; i<number_time_slots; i++) {
			//logger.info("grid_crowding:"+current.getId()+"("+TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)+"):::"+grid_crowdings.get(current.getId()).get(TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)).getDistribution());
			double mean = RandomValue.get(grid_crowdings.get(current_cell).get(TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)));
			max_mean = (mean > max_mean) ? mean : max_mean; 
		}

		return max_mean;
	}




	private Double getTotalDistance(Map<Integer, Map<String, Double>> map_p_d) {
		double tot_distance = 0d;
		for (int i=0; i< map_p_d.size(); i++) {
			for (String c : map_p_d.get(i).keySet()) {
				tot_distance+=map_p_d.get(i).get(c);
			}
		}
		return tot_distance;
	}

}
