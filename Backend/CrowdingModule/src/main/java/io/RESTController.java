package io;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import model.POI2POICrowding;
import model.UncertainValue;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import services.ComputeP2PCrowdings;
import services.UpdateCrowdings;


//import redis.clients.jedis.Jedis;

@Controller
@RequestMapping("/")
public class RESTController {
	private static boolean initialized = false;
	private Logger logger = Logger.getLogger(RESTController.class);

	Map<String,CityData> cityDataMap;


	public RESTController() {
		cityDataMap = new HashMap<>();
		for(String city: CityProp.getInstance().keySet())
			cityDataMap.put(city,new CityData(city));
	}


	@RequestMapping(value = "init", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean init() {
		boolean result = false;

		if (!initialized) {
			try {

				for(String city: cityDataMap.keySet()) {
					logger.info("\n*********************************** "+city+" ***********************************\n");
					cityDataMap.get(city).init();
				}
				logger.info("\n\n\n\t\t*********************************************\n"
						+ "\t\t***CrowdingModule successfully initialized***\n"
						+ "\t\t*********************************************\n\n\n");

				initialized = true;

				result = true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { 
			result = true;
		}
		return result;
	}


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

			for(String city : cityDataMap.keySet()) {
				cityDataMap.get(city).retrieveGridCrowdings();
				List<POI2POICrowding> crs = new ComputeP2PCrowdings().run(cityDataMap.get(city));
				logger.info(city+ "POI2POI Crowdings updated and imported ("+crs.size()+")");
			}


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

	@RequestMapping(value = "crowdings/{city}", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI2POICrowding> sendCrowdings(@PathVariable String city) {
		return new ComputeP2PCrowdings().run(cityDataMap.get(city));
	}

	/**
	 * 
	 */
	@RequestMapping(value = "crowding/{city}/{inc_dec}/{poi_start}/{poi_end}/{time_slot}", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean updateCrowding(@PathVariable String city, @PathVariable String inc_dec, @PathVariable String poi_start, @PathVariable String poi_end, @PathVariable Long time_slot) {
		logger.info("Update request:<"+city+","+inc_dec+","+poi_start+","+poi_end+","+time_slot+">");
		boolean result = new UpdateCrowdings().run(cityDataMap.get(city), inc_dec, poi_start, poi_end, time_slot);

		try {
			return result;//mapper.writeValueAsString(result);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;//"{\"Error\" : \"GET request failed\"}";
		}
	}


	@RequestMapping(value = "crowding_fdbk/{user}/{city}/{departure}/{arrival}/{dep_time}/{choice}", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody UncertainValue setCrowdingFdbk(@PathVariable String user, @PathVariable String city, @PathVariable String departure, @PathVariable String arrival, @PathVariable String dep_time, @PathVariable int choice) {
		logger.info("CM_Crowding Feedback from "+user);
		logger.info("on Path "+departure+"-"+arrival+" ("+dep_time+") ");
		logger.info("Choice "+choice);
		UncertainValue value = cityDataMap.get(city).dao.retrieveCrowdingLevelP2P(departure, arrival, dep_time);
		logger.info("Value "+ value.getMean());
		boolean updated = cityDataMap.get(city).dao.updateUserCrowdingStats(
				user, 
				((choice == 0) ? value.getMean() : 0d),
				((choice == 1) ? value.getMean() : 0d),
				((choice == 2) ? value.getMean() : 0d),
				((choice == 3) ? value.getMean() : 0d));
		logger.info("Updated "+updated);
		return value;
	}
}