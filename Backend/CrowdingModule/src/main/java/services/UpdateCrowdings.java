package services;

import io.Mongo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.RandomValue;
import util.TimeUtils;
import model.Cell;
import model.UncertainValue;

public class UpdateCrowdings {
	
	public static void main (String[] args) throws IOException {
		//new UpdateCrowdings().run("d", "99532292", "99532371", 900000L);
	}
	
	public ComputeCellPathAndDistances compute = new ComputeCellPathAndDistances();

	public boolean run(Mongo dao, String inc_dec, String poi_start, String poi_end, Long time_slot, Map<String, Map<String, Map<Integer, Map<String, Double>>>> cell_path_distances, Map<String, HashMap<String, List<UncertainValue>>> travel_times, List<Cell> grid) {

		try {
			//cell_path_distances = <position, <cell, distance_walked_in_cell>
			
			Long travellingTime = getTravelTime(poi_start, poi_end, time_slot, travel_times); 
					//dao.retrieveTravelTime(poi_start, poi_end, time_slot);
			
			//compute the total distance covered along the path
			double tot_distance = 0d;

			for (int i=0; i< cell_path_distances.get(poi_start).get(poi_end).size(); i++) {
				for (String c : cell_path_distances.get(poi_start).get(poi_end).get(i).keySet()) {
					tot_distance+=cell_path_distances.get(poi_start).get(poi_end).get(i).get(c);
				}
			}
			//compute the distance covered in each cell
			Map<Integer, Double> distance_rate = new HashMap<>();
			for (int i=0; i< cell_path_distances.get(poi_start).get(poi_end).size(); i++) {
				for (String c : cell_path_distances.get(poi_start).get(poi_end).get(i).keySet()) {
					distance_rate.put(i, cell_path_distances.get(poi_start).get(poi_end).get(i).get(c)/tot_distance);
				}
			}
			//compute the travelling time per every cell
			Map<Integer, Long> travel_byCell = new HashMap<>();
			for (Integer i : distance_rate.keySet()) {
				travel_byCell.put(i, (long) (distance_rate.get(i)*travellingTime));
			}
			
			//update the crowding values on the DB, cell by cell for the whole time length of the path
			Long arr_time_inCell = time_slot;
			Long dep_time_fromCell = time_slot;
			for (int i=0; i< travel_byCell.size(); i++) {
				dep_time_fromCell += travel_byCell.get(i);
				Cell current_cell = null;
				for (Cell cell : grid) {
					if (cell.getId().equals(cell_path_distances.get(poi_start).get(poi_end).get(i).keySet().iterator().next())) {
						current_cell = cell;
					}
				}
				//current_cell = dao.retrieveCell(cell_path_distances.get(poi_start).get(poi_end).get(i).keySet().iterator().next());
				
				dao.updateGridCrowdings(current_cell, arr_time_inCell, dep_time_fromCell, inc_dec);
				arr_time_inCell += travel_byCell.get(i);
				
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private Long getTravelTime(String poi_start, String poi_end, Long time_slot, Map<String, HashMap<String, List<UncertainValue>>> travel_times) {
		UncertainValue travel_time = null;
		try {
			
			travel_time = travel_times.get(poi_start).get(poi_end).get(TimeUtils.getTimeSlot(TimeUtils.getMillis15MRoundTime(time_slot)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ((long)RandomValue.get(travel_time))*60*1000l;
	}

}
