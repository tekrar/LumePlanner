package services;

import io.Mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Cell;
import model.POI;

import org.geojson.LngLatAlt;
import org.geojson.Point;

import util.HaversineDistance;

public class ComputeCellPathAndDistances {

	public Map<Integer, Map<String, Double>> run(Mongo dao, String poi_start, String poi_end) {
		Map<Integer, Map<String, Double>> result = new HashMap<>();

		Cell start_c = dao.retrieveCellofPOI(poi_start);
		POI start_p = dao.retrievePOI(poi_start);
		//logger.info("S:"+start_c.getId()+"("+start_p.getGeometry().getCoordinates().getLatitude()+","+start_p.getGeometry().getCoordinates().getLongitude()+")");

		Cell end_c = dao.retrieveCellofPOI(poi_end);
		POI end_p = dao.retrievePOI(poi_end);
		//logger.info("E:"+end_c.getId()+"("+end_p.getGeometry().getCoordinates().getLatitude()+","+end_p.getGeometry().getCoordinates().getLongitude()+")");

		List<Cell> visited = new ArrayList<>();
		visited.add(start_c);

		LinkedList<Cell> adjacencies = new LinkedList<>();
		adjacencies.add(start_c);

		Point last_point_touched = start_p.getGeometry();
		int num_cell_touched = -1;
		while (!adjacencies.contains(end_c)) {
			result.put(++num_cell_touched, new HashMap<String, Double>());
			double shortestDistance = Double.MAX_VALUE;
			LngLatAlt closestVertex = new LngLatAlt();
			Cell closestCell = new Cell();
			List<Cell> currentAdjancecies = null;
			while (adjacencies.size()>0) {
				Cell currentCell = adjacencies.removeFirst();
				for (LngLatAlt vertex : currentCell.getGeometry().getExteriorRing()) {
					double currentDistance = 0d;
					if (shortestDistance > ( currentDistance = HaversineDistance.haverDist(
							new double[]{vertex.getLatitude(), vertex.getLongitude()}, 
							new double[]{end_p.getGeometry().getCoordinates().getLatitude(), end_p.getGeometry().getCoordinates().getLongitude()}))) {
						shortestDistance = currentDistance;
						closestVertex = vertex;
						closestCell = currentCell;
					}
				}
			}
			currentAdjancecies = dao.retrieveAdjacentCells(new Point(closestVertex));
			visited.add(closestCell);
			//System.out.println("c:"+closestCell.getId());
			//System.out.println("p:("+closestVertex.getLatitude()+","+closestVertex.getLongitude()+")");

			result.get(num_cell_touched).put(closestCell.getId(), HaversineDistance.haverDist(
					new double[]{last_point_touched.getCoordinates().getLatitude(), last_point_touched.getCoordinates().getLongitude()}, 
					new double[]{closestVertex.getLatitude(), closestVertex.getLongitude()}));

			last_point_touched = new Point(closestVertex);

			for (Cell adj : currentAdjancecies) {
				if (adj.getRoads_length() > 0d && !visited.contains(adj)) {
					adjacencies.add(adj);
				}
			}

		}

		result.put(++num_cell_touched, new HashMap<String, Double>());

		System.err.println("---------->>>> "+end_c.getId());

		result.get(num_cell_touched).put(end_c.getId(), HaversineDistance.haverDist(
				new double[]{last_point_touched.getCoordinates().getLatitude(), last_point_touched.getCoordinates().getLongitude()}, 
				new double[]{end_p.getGeometry().getCoordinates().getLatitude(), end_p.getGeometry().getCoordinates().getLongitude()}));

		return result;
	}

}
