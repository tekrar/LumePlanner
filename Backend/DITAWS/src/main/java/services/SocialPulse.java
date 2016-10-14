package services;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import org.apache.log4j.Logger;
import org.geojson.Point;

import au.com.bytecode.opencsv.CSVReader;

public class SocialPulse {


	private Properties p;

	private Logger logger = Logger.getLogger(SocialPulse.class);

	private String baseDir;

	//private static Mongo dao;

	public static void main(String args[]) throws IOException {
		SocialPulse s = new SocialPulse();
		s.baseDir =  "/home/andrea/workspace/DITAWS/target/DITA/WEB-INF/data/"+s.p.getProperty("data.dir");
		//s.getCentroids(s.loadClusters());
		//dao = new Mongo();
	}

	public SocialPulse() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		baseDir = this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath();
		//dao = new Mongo();
	}

//	public void writeCrowdPOI() {
//		CSVWriter csvWriter = null;
//		try {
//			csvWriter = new CSVWriter(new FileWriter(baseDir+"crowdings_socialPulse.csv"), ',', '\0');
//		
//		csvWriter.writeNext(("poi,lng,lat,crowding").split(","));
//		Map<String, Integer> stars = loadStars();
//		List<POI> activities = dao.retrieveActivities();
//
//		for (POI poi : activities) {
//			if (stars.containsKey(poi.getPlace_id())) {
//				csvWriter.writeNext((
//						poi.getPlace_id()+
//						","+poi.getGeometry().getCoordinates().getLongitude()+
//						","+poi.getGeometry().getCoordinates().getLatitude()+
//						","+(stars.get(poi.getPlace_id())+0d)/(activities.size()+0d)
//						).split(","));
//			} else {
//				csvWriter.writeNext((
//						poi.getPlace_id()+
//						","+poi.getGeometry().getCoordinates().getLongitude()+
//						","+poi.getGeometry().getCoordinates().getLatitude()+
//						","+1d/(activities.size()+0d)
//						).split(","));
//			}
//		}
//		csvWriter.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public Map<Integer,List<Point>> loadClusters() {

		Map<Integer,List<Point>> clusters = new HashMap<>();

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"clean-clusters.csv"), ',', '@', 0)) {

			String [] line;
			while (null != (line = csvReader.readNext())) {
				if (line[0].trim().equals("-1")) {
					continue;
				}
				if (!clusters.containsKey(Integer.parseInt(line[0]))) {
					clusters.put(Integer.parseInt(line[0]), new ArrayList<Point>());
				}
				clusters.get(Integer.parseInt(line[0])).add(new Point(Double.parseDouble(line[1].trim()), Double.parseDouble(line[2].trim())));
			}

		} catch (Exception e) {
			logger.info("Error on loading clusters");
			e.printStackTrace();
		}
		logger.info("Clusters loaded ("+clusters.size()+")");

		return clusters;
	}

	public Map<Integer, Point> getCentroids(Map<Integer,List<Point>> clusters) {
		Map<Integer, Point> centroids = new HashMap<>();   

		double centroidLng = 0d;
		double centroidLat = 0d;
		for (Integer cluster : clusters.keySet()) {
			for (Point point : clusters.get(cluster)) {
				centroidLng += point.getCoordinates().getLongitude();
				centroidLat += point.getCoordinates().getLatitude();
			}
			logger.info(cluster+","+new Point(centroidLng/clusters.get(cluster).size(), centroidLat/clusters.get(cluster).size()).getCoordinates().toString());
			centroids.put(cluster, new Point(centroidLng/clusters.get(cluster).size(), centroidLat/clusters.get(cluster).size()));
			centroidLng=0d;
			centroidLat=0d;
		}
		return centroids;
	}

	public Map<String, Integer> loadStars() {
		Map<String, Integer> ratings = new HashMap<>();

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"stars_from_twitter.csv"), ' ', '@', 1)) {

			String [] line;
			while (null != (line = csvReader.readNext())) {
				String [] ids = line[3].split(",");
				for (String id : ids) {
					ratings.put(id, Integer.parseInt(line[0]));
				}
			}

		} catch (Exception e) {
			logger.info("Error on loading ratings");
			e.printStackTrace();
		}
		logger.info("Ratings loaded ("+ratings.size()+")");

		return ratings;
	}


}
