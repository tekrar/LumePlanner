package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.POI;

public class Occupancies {

	public Map<String, List<Integer>> init(List<POI> activities) {
		Map<String, List<Integer>> output = new HashMap<>();

		for (POI activity : activities) {
			output.put(activity.getPlace_id(), new ArrayList<Integer>(96));
			for (int i=0; i<96; i++) {
				output.get(activity.getPlace_id()).add(0);
			}
		}

		return output;
	}

	public Map<String, List<Integer>> update(String hour, String minute, Map<String, List<Integer>> occupancies) {
		
		int minutes = TimeUtils.toMinutes(TimeUtils.getMillis(TimeUtils.getString15MRoundTime(Integer.parseInt(hour), Integer.parseInt(minute))));

		for (int cont=0; cont<4; cont++) { //restore the occupancies in the previous hour equals to 0 to all the activities
			for (String activity : occupancies.keySet()) {
				occupancies.get(activity).set(TimeUtils.getTimeSlot(minutes-15*cont), 0);
			}
		}
		return occupancies;
	}

	public Map<String, List<Integer>> increase(String to, String arr_t, String dep_t, Map<String, List<Integer>> occupancies) {
		int from_time_slot 	= TimeUtils.getTimeSlot(TimeUtils.toMinutes(TimeUtils.getMillis(arr_t)));
		int to_time_slot	= TimeUtils.getTimeSlot(TimeUtils.toMinutes(TimeUtils.getMillis(dep_t)));
		
		for (;from_time_slot<=to_time_slot;from_time_slot++) {
			occupancies.get(to).set(from_time_slot, occupancies.get(to).get(from_time_slot)+1);
		}
		
		return occupancies;
	}

	public Map<String, List<Integer>> decrease(String place_id, String arr_t, String dep_t, Map<String, List<Integer>> occupancies) {
		int from_time_slot 	= TimeUtils.getTimeSlot(TimeUtils.toMinutes(TimeUtils.getMillis(arr_t)));
		int to_time_slot	= TimeUtils.getTimeSlot(TimeUtils.toMinutes(TimeUtils.getMillis(dep_t)));
		
		for (;from_time_slot<=to_time_slot;from_time_slot++) {
			occupancies.get(place_id).set(from_time_slot, occupancies.get(place_id).get(from_time_slot)-1);
		}
		
		return occupancies;
	}

}
