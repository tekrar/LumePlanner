package util;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.shapes.GHPlace;

import model.POI;
import model.Path;
import org.geojson.LineString;
import org.geojson.LngLatAlt;

import java.util.List;

public class TravelPath {
	
	public Path compute(GraphHopper hopper, POI o, POI d) {
		Path result = new Path();

		GHPlace from = new GHPlace(o.getGeometry().getCoordinates().getLatitude(), o.getGeometry().getCoordinates().getLongitude());
		GHPlace  to = new GHPlace(d.getGeometry().getCoordinates().getLatitude(), d.getGeometry().getCoordinates().getLongitude());
		GHRequest request = new GHRequest(from, to).setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);
		GHResponse response = hopper.route(request);



		result.setPoints(convertToLineString(response.getPoints().toGeoJson(false)));
		result.setLength(response.getDistance());
		return result;
	}

	private LineString convertToLineString(List<Double[]> points) {
		LineString lineString = new LineString();

		for (Double[] point : points) {
			lineString.add(new LngLatAlt(point[0], point[1]));
		}

		return lineString;
	}
}
