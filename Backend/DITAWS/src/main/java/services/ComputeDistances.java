package services;

import io.CityData;
import io.Mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import model.Distance;
import model.DistanceTo;
import model.POI;

import static util.Misc.haverDist;

public class ComputeDistances {

	
	public ComputeDistances() {}
	
	public static void main(String[] args) throws Exception  {
		//run();
	}

	public void run(Mongo dao, List<POI> POIsCollection) {
		Collections.sort(POIsCollection);
		List<Distance> distances = new ArrayList<Distance>();
		for (Iterator<POI> iterFrom = POIsCollection.iterator(); iterFrom.hasNext();) {
			POI from = iterFrom.next();
			List<DistanceTo> dTos = new ArrayList<DistanceTo>();
			for (Iterator<POI> iterTo = POIsCollection.iterator();iterTo.hasNext();) {
				POI to = iterTo.next();
				dTos.add(new DistanceTo(to.getPlace_id(), haverDist(
						new double[]{from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
						new double[]{to.getGeometry().getCoordinates().getLatitude(), to.getGeometry().getCoordinates().getLongitude()})));
			}
			distances.add(new Distance(from.getPlace_id(), dTos));

		}

		dao.insertDistances(distances);
	}

	public Distance runOnetoMany(CityData cityData, POI from, POI to_global, List<String> POIsCollection) {
		Collections.sort(POIsCollection);
		List<DistanceTo> dTos = new ArrayList<DistanceTo>();
		for (String next_id : POIsCollection) {
			if (!next_id.equals(from.getPlace_id())) {
				if (!next_id.equals("00")) {
					POI to = cityData.retrieveActivity(next_id);
					dTos.add(new DistanceTo(to.getPlace_id(), haverDist(
							new double[]{from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
							new double[]{to.getGeometry().getCoordinates().getLatitude(), to.getGeometry().getCoordinates().getLongitude()})));
				} else {
					dTos.add(new DistanceTo(to_global.getPlace_id(), haverDist(
							new double[]{from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
							new double[]{to_global.getGeometry().getCoordinates().getLatitude(), to_global.getGeometry().getCoordinates().getLongitude()})));
				}
			}
		}
		return new Distance(from.getPlace_id(), dTos);
	}




}
