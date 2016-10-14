package util;

import io.Mongo;

import java.io.IOException;
import java.util.*;

import services.ComputeDistances;
import model.POI;
import model.Timing;
import model.TimingTo;
import model.UncertainValue;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPlace;

import static util.Misc.haverDist;

public class TravelTime {

	private Properties p;


	public TravelTime() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	private Logger logger = Logger.getLogger(TravelTime.class);
	
//	public static void main (String args[]) {
//		new TravelTime().run();
//	}
//	
//	public void run() {
//		GraphHopper hopper = new GraphHopper().forServer();
//		hopper.setInMemory();
//
//		hopper.setOSMFile("src/main/webapp/WEB-INF/data/venice_12.297951,45.411191_12.406648,45.484229.osm");
//		hopper.setGraphHopperLocation("src/main/webapp/WEB-INF/data/venice.graph");
//		hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
//		hopper.importOrLoad();
//		
//		GHPlace from = new GHPlace(45.43954, 12.336597);
//		GHPlace to = new GHPlace(45.431442, 12.321817);
//		GHRequest request = new GHRequest(from, to).setAlgorithm("astarbi");
//		
//		System.out.println(hopper.route(request).getDistance());
//		System.out.println(hopper.route(request).getPoints().toString());
//	}

	public Map<String, HashMap<String, List<UncertainValue>>> initTravelTimeFromPOIs(Mongo dao, List<POI> POIs, String dataPath) {
		Map<String, HashMap<String, List<UncertainValue>>> result = new HashMap<String, HashMap<String, List<UncertainValue>>>();

//		GraphHopper hopper = new GraphHopper().forServer();
//		hopper.setInMemory();
//
//		hopper.setOSMFile(dataPath+"venice_12.297951,45.411191_12.406648,45.484229.osm");
//		hopper.setGraphHopperLocation(dataPath+"venice.graph");
//		hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
//		hopper.importOrLoad();

		//GHPlace from;
		//GHPlace to;
		//GHRequest request;

		for (POI POIorig : POIs) {
			//from = new GHPlace(POIorig.getLat(), POIorig.getLon());

			for (POI POIdest : POIs) {
				//to = new GHPlace(POIdest.getLat(), POIdest.getLon());
				for (int time = 0; time<96; time++) {
					//request = new GHRequest(from, to).setAlgorithm("astarbi");

					if (!result.containsKey(POIorig.getPlace_id())) {
						result.put(POIorig.getPlace_id(), new HashMap<String, List<UncertainValue>>());
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(96));
					} else if (!result.get(POIorig.getPlace_id()).containsKey(POIdest.getPlace_id()))
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<UncertainValue>(96));

					double travel_time = 0;

					if (!POIorig.equals(POIdest)) //I perform a travel time request
						//if (hopper.route(request).hasErrors()) //route not found
							travel_time = haverDist(
									new double[]{POIorig.getGeometry().getCoordinates().getLatitude(),  POIorig.getGeometry().getCoordinates().getLongitude()},
									new double[]{POIdest.getGeometry().getCoordinates().getLatitude(),  POIdest.getGeometry().getCoordinates().getLongitude()})
									/1.5d/60d;
						//else	
							//travel_time = (float)(hopper.route(request).getMillis()/1000/60f);
					else  //I store the visiting time of the POI
						travel_time = RandomValue.get(POIorig.getVisiting_time());

					result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id()).add(time, new UncertainValue(round(travel_time,5), "N:"+(round(travel_time,5)/10)));
				}
			}
		}
		//logger.info("travel_time size: "+result.size());
		dao.insertTimings(serializeTimings(result));
		return result;
	}



	private List<Timing> serializeTimings(Map<String, HashMap<String, List<UncertainValue>>> timings) {
		List<Timing> result = new ArrayList<>();
		
		for(String from : timings.keySet()) {
			List<TimingTo> t_to = new ArrayList<TimingTo>();
			for (String to : timings.get(from).keySet()) {
				t_to.add(new TimingTo(to, timings.get(from).get(to)));
			}
			result.add(new Timing(from, t_to));
		}
		
		return result;
	}

	public Map<String, HashMap<String, List<UncertainValue>>> getTravelTimeFromIDs(Mongo dao, List<String> POIs) {
		Map<String, HashMap<String, List<UncertainValue>>> result = new HashMap<String, HashMap<String, List<UncertainValue>>>();

		GraphHopper hopper = new GraphHopper().forServer();
		hopper.setInMemory();
		hopper.setOSMFile("src/main/webapp/WEB-INF/data/"+p.getProperty("data.dir")+"bbox.osm");
		hopper.setGraphHopperLocation("data/"+p.getProperty("data.dir")+"graph");
		hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
		hopper.importOrLoad();

		POI start;
		POI end = null;
		GHPlace from;
		GHPlace to;
		GHRequest request;

		for (String POIorig : POIs) {
			start = dao.retrieveActivity(POIorig);
			from = new GHPlace(
					start.getGeometry().getCoordinates().getLatitude(), 
					start.getGeometry().getCoordinates().getLongitude());

			for (String POIdest : POIs) {
				end = dao.retrieveActivity(POIdest);
				to = new GHPlace(
						end.getGeometry().getCoordinates().getLatitude(), 
						end.getGeometry().getCoordinates().getLongitude());
				for (int time = 0; time<96; time++) {
					request = new GHRequest(from, to).setAlgorithm("astarbi");

					if (result.get(POIorig)==null) {
						result.put(POIorig, new HashMap<String, List<UncertainValue>>());
						result.get(POIorig).put(POIdest, new ArrayList<UncertainValue>(96));
					} else if (result.get(POIorig).get(POIdest)==null)
						result.get(POIorig).put(POIdest, new ArrayList<UncertainValue>(96));

					double travel_time = 0;

					if (!from.equals(to)) //I perform a travel time request
						travel_time = hopper.route(request).getMillis()/1000/60d;
					else  //I store the visiting time of the POI
						travel_time = round(RandomValue.get(start.getVisiting_time()),5);

					result.get(POIorig).get(POIdest).add(time, new UncertainValue(travel_time, "N:"+(travel_time/10)));
				}
			}
		}
		return result;
	}



	private double round(double x, int position)
	{
		double a = x;
		double temp = Math.pow(10.0, position);
		a *= temp;
		a = Math.round(a);
		return (a / (double)temp);
	}
}
