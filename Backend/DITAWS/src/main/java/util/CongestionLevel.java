package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.POI;
import model.UncertainValue;

public class CongestionLevel {
	
	public static Map<String, HashMap<String, ArrayList<UncertainValue>>> initCongestionLevelFromPOIs(List<POI> POIs) {
		Map<String, HashMap<String, ArrayList<UncertainValue>>> result = new HashMap<String, HashMap<String, ArrayList<UncertainValue>>>();

		for (POI POIorig : POIs) {
			for (POI POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig.getPlace_id())==null) {
						result.put(POIorig.getPlace_id(), new HashMap<String, ArrayList<UncertainValue>>());
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(24));
					} else if (result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id())==null)
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(24));
					
					double congestion_level = Math.random();
										
					result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id()).add(time, new UncertainValue(congestion_level, "N:"+(congestion_level/10d)));
				}
			}
		}

		return result;
	}

	public static Map<String, HashMap<String, ArrayList<UncertainValue>>> getCongestionLevelFromPOIs(List<POI> POIs) {
		Map<String, HashMap<String, ArrayList<UncertainValue>>> result = new HashMap<String, HashMap<String, ArrayList<UncertainValue>>>();

		for (POI POIorig : POIs) {
			for (POI POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig.getPlace_id())==null) {
						result.put(POIorig.getPlace_id(), new HashMap<String, ArrayList<UncertainValue>>());
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(24));
					} else if (result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id())==null)
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(24));
					
					double congestion_level = Math.random();
										
					result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id()).add(time, new UncertainValue(congestion_level, "N:"+(congestion_level/10)));
				}
			}
		}

		return result;
	}
	
	public Map<String, HashMap<String, List<UncertainValue>>> getCongestionLevelFromIDs(List<String> POIs) {
		Map<String, HashMap<String, List<UncertainValue>>> result = new HashMap<String, HashMap<String, List<UncertainValue>>>();

		for (String POIorig : POIs) {
			for (String POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig)==null) {
						result.put(POIorig, new HashMap<String, List<UncertainValue>>());
						result.get(POIorig).put(POIdest, new ArrayList<UncertainValue>(24));
					} else if (result.get(POIorig).get(POIdest)==null)
						result.get(POIorig).put(POIdest, new ArrayList<UncertainValue>(24));
					
					double congestion_level;
					if (POIorig.equals(POIdest)) congestion_level = 0d;
					else congestion_level =  Math.random();
					
					result.get(POIorig).get(POIdest).add(time, new UncertainValue(congestion_level, "N:"+(congestion_level/10)));
				}
			}
		}

		return result;
	}

	public static Map<String, HashMap<String, ArrayList<UncertainValue>>> updateCongestionLevel(
			Map<String, HashMap<String, ArrayList<UncertainValue>>> congestion_levels, int time) {

		for (String orig : congestion_levels.keySet()) {
			for (String dest : congestion_levels.get(orig).keySet()) {
				
				if (orig.equals(dest)) continue;
				
				double congestion_level = Math.random();
				
				congestion_levels.get(orig).get(dest).set(time, new UncertainValue(congestion_level, "N:"+(congestion_level/10)));
			}
		}
		
		
		return congestion_levels;
	}
}