package io;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import model.Cell;
import model.POI;
import model.POI2POICrowding;
import model.UncertainValue;

import org.apache.log4j.Logger;
import org.geojson.LngLatAlt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.bytecode.opencsv.CSVWriter;
import services.ComputeP2PCrowdings;
import services.LoadFiles;
import services.UpdateCrowdings;

import static util.HaversineDistance.haverDist;


//import redis.clients.jedis.Jedis;

@Controller
@RequestMapping("/")
public class RESTController {
	//private Properties p;
	private Mongo dao;

	private static List<POI> activities;	

	private static boolean initialized = false;

	private static Map<String, HashMap<String, List<UncertainValue>>> travel_times ;

	private Logger logger = Logger.getLogger(RESTController.class);

	private static List<Cell> grid;

	private static Map<String, List<UncertainValue>> grid_crowdings;

	private static Map<String, Map<String, Map<Integer, Map<String, Double>>>> p2p_cell_paths;

	/*
	public RESTController() {
		Properties p = new Properties();

		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("CM.properties"));
			dao = new Mongo(p.getProperty("mongo.user"), p.getProperty("mongo.db"), p.getProperty("mongo.password"));
		} catch(Exception e) {
			//e.printStackTrace();
			logger.info("Initialization for simulation experiment started");
		}
	}
	*/

	@RequestMapping(value = "init", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean init() {
		boolean result = false;

		if (!initialized) {
			Properties p = new Properties();
			try {
				p.load(this.getClass().getClassLoader().getResourceAsStream("CM.properties"));
				dao = new Mongo(p.getProperty("mongo.user"), p.getProperty("mongo.db"), p.getProperty("mongo.password"));

				logger.info("Crowding Module initialization started");

				grid_crowdings = new LoadFiles().load(dao, this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath());
				logger.info("Grid Crowdings imported ("+grid_crowdings.size()+")");

				activities = dao.retrieveActivities();
				logger.info("Activities retrieved from Mongodb (count "+activities.size()+")");

				//writeCrowdings();
				
				grid = dao.retrieveGrid();

				travel_times = dao.retrieveTravelTimes();
				logger.info("Travel times retrieved from Mongodb (count "+travel_times.size()+")");

				ComputeP2PCrowdings p2p = new ComputeP2PCrowdings();

				if (!dao.checkCellPaths()) {
					p2p_cell_paths = p2p.insertCellPaths(dao, activities);
					logger.info("Cell Paths imported ("+p2p_cell_paths.size()+")");
				} else {
					p2p_cell_paths = dao.retrieveCellPaths();
					logger.info("CellPaths retriedved from Mongodb ("+p2p_cell_paths.size()+")");
				}




				if (!dao.checkCrowdingLevels()) {
					List<POI2POICrowding> crs = p2p.run(dao, p2p_cell_paths, grid_crowdings, travel_times);
					logger.info("POI2POI Crowdings imported ("+crs.size()+")");
				}

				logger.info("\n\n\n\t\t*********************************************\n"
						+ "\t\t***CrowdingModule successfully initialized***\n"
						+ "\t\t*********************************************\n\n\n");

				initialized = true;

				result = true;

			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		} else { 
			result = true;
		}
		return result;
	}

	/*
	private void writeCrowdings() {
		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("CM.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Cell> grid = dao.retrieveGrid();
		Map<String, LngLatAlt> centroids = computeCentroids(grid);
		Map<String, List<UncertainValue>> crowdings = dao.retrieveGridCrowdings();
		Map<POI, Double> crowdings_POI = new HashMap<>();
		CSVWriter csvWriter = null;
		try {
			double max_d = 500d;

			csvWriter = new CSVWriter(new FileWriter(this.getClass().getResource("/../data/"+p.getProperty("data.dir")).getPath()+"crowdings_centroids_WE_max"+max_d+".csv"), ',', '\0');
			csvWriter.writeNext(("poi,lng,lat,crowding").split(","));
			
			
			double min_crowdings_val = Double.MAX_VALUE;
			double max_crowdings_val = Double.MIN_VALUE;
			
			for (POI poi : activities) {
				Map<String, Double> d_toCentroids = new HashMap<>();
				double tot_distance = 0d;
				for (String cell : centroids.keySet()) {
					double distance = 0d;
					if ((distance = haverDist(
							new double [] {poi.getGeometry().getCoordinates().getLatitude(), poi.getGeometry().getCoordinates().getLongitude()},
							new double [] {centroids.get(cell).getLatitude(), centroids.get(cell).getLongitude()}))
							<= max_d) {
						d_toCentroids.put(cell, distance);
						tot_distance += distance;
					}
					
				}
				double crowdings_value = 0d;
				for (String cell : d_toCentroids.keySet()) {
					crowdings_value += crowdings.get(cell).get(40).getMean()*d_toCentroids.get(cell)/tot_distance;
				}
				if (crowdings_value > max_crowdings_val) {
					max_crowdings_val = crowdings_value;
				}
				if (crowdings_value < min_crowdings_val) {
					min_crowdings_val = crowdings_value;
				}
				crowdings_POI.put(poi, crowdings_value);
			}
			
			for (POI poi : crowdings_POI.keySet()) {
				csvWriter.writeNext((
						poi.getPlace_id()+
						","+poi.getGeometry().getCoordinates().getLongitude()+
						","+poi.getGeometry().getCoordinates().getLatitude()+
						","+(crowdings_POI.get(poi)-min_crowdings_val)/(max_crowdings_val-min_crowdings_val)
						).split(","));
			}
			csvWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	*/

	/*
	private Map<String, LngLatAlt> computeCentroids(List<Cell> grid) {

		Map<String, LngLatAlt> centroids = new HashMap<>();

		for (Cell cell : grid) {
			double lng = 0.;
			double lat = 0.;
			int pointCount = 0;
			for (List<LngLatAlt> ext_r : cell.getGeometry().getCoordinates()){
				pointCount = ext_r.size();
				for (LngLatAlt int_r : ext_r) {
					lng += int_r.getLongitude();
					lat += int_r.getLatitude();
				}
			}
			//logger.info(cell.getId()+"_("+lng/(pointCount+0d)+","+lat/(pointCount+0d));
			centroids.put(cell.getId(), new LngLatAlt(lng/(pointCount+0d), lat/(pointCount+0d)));
		}

		return centroids;
	}
	*/

	@Scheduled(fixedRate = 3600000) //hourly
	public void update() throws IOException {
		if (initialized) {
			DateFormat hourFormatter = new SimpleDateFormat("hh");
			DateFormat minuteFormatter = new SimpleDateFormat("mm");
			Date d = new Date();
			hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			String hour = hourFormatter.format(d);
			String minute = minuteFormatter.format(d);

			logger.info("Crowding Module update request at "+hour+":"+minute);
			
			//dao.resetGridCrowdings(hour, minute, grid);
			//logger.info("Grid Crowdings reset");
			
			grid_crowdings = dao.retrieveGridCrowdings();

			List<POI2POICrowding> crs = new ComputeP2PCrowdings().run(dao, p2p_cell_paths, grid_crowdings, travel_times);

			logger.info("POI2POI Crowdings updated and imported ("+crs.size()+")");

			logger.info("\n\n\n\t\t*********************************************\n"
					+ "\t\t*****CrowdingModule successfully updated*****\n"
					+ "\t\t*********************************************\n\n\n");
		}
	}

	@RequestMapping(value = "update", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody void manualUpdate() {

		try {
			update();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		}

	}

	@RequestMapping(value = "crowdings", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI2POICrowding> sendCrowdings() {

		return new ComputeP2PCrowdings().run(dao, p2p_cell_paths, grid_crowdings, travel_times);
	}

	/**
	 * 
	 */
	@RequestMapping(value = "crowding/{inc_dec}/{poi_start}/{poi_end}/{time_slot}", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean updateCrowding(@PathVariable String inc_dec, @PathVariable String poi_start, @PathVariable String poi_end, @PathVariable Long time_slot) {

		logger.info("Update request:<"+inc_dec+","+poi_start+","+poi_end+","+time_slot+">");
		boolean result = new UpdateCrowdings().run(dao, inc_dec, poi_start, poi_end, time_slot, p2p_cell_paths, travel_times, grid);

		try {
			return result;//mapper.writeValueAsString(result);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;//"{\"Error\" : \"GET request failed\"}";
		}
	}


	@RequestMapping(value = "crowding_fdbk/{user}/{departure}/{arrival}/{dep_time}/{choice}", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody UncertainValue setCrowdingFdbk(@PathVariable String user, @PathVariable String departure, @PathVariable String arrival, @PathVariable String dep_time, @PathVariable int choice) {
		logger.info("CM_Crowding Feedback from "+user);
		logger.info("on Path "+departure+"-"+arrival+" ("+dep_time+") ");
		logger.info("Choice "+choice);
		UncertainValue value = dao.retrieveCrowdingLevelP2P(departure, arrival, dep_time);
		logger.info("Value "+ value.getMean());
		boolean updated = dao.updateUserCrowdingStats(
				user, 
				((choice == 0) ? value.getMean() : 0d),
				((choice == 1) ? value.getMean() : 0d),
				((choice == 2) ? value.getMean() : 0d),
				((choice == 3) ? value.getMean() : 0d));
		logger.info("Updated "+updated);
		return value;
	}
}