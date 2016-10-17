package services;

import io.Mongo;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import model.Cell;
import model.GridCrowding;
import model.UncertainValue;

import org.apache.log4j.Logger;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import util.TimeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.bytecode.opencsv.CSVReader;

public class LoadFiles {

	private Properties p;

	private String weekend_weekdays = "wd"; //we

	//private Double p_B = 0.5d; //we assume there is the same number of tourists and residents every day
	// p_A_intersects_B = P(calling_tourist) 
	// p_A_due_B 		= P(calling) due P(tourist)
	// p_B 	 			= P(tourist)
	// n_t   			= # tourists
	//p_A_due_B = p_A_intersects_B/p_B
	/*
	 Double p_A_intersects_B = calls_tourists_residents.get(currentCell).get(currentTS);
						Double p_A_due_B = p_A_intersects_B/p_B;
						Double n_t = (p_A_due_B.equals(0D)) ? 0D : calls_by_cap.get(1).get(currentCell).get(currentTS)/p_A_due_B;
	 */

	private Logger logger = Logger.getLogger(LoadFiles.class);

	private String baseDir;

	//private static Map<String, Map<Long, Double>> presences; //<cell_id, <time_slot, presences>>

	//private List<Long> veniceCaps;

	//private Map<Integer, Map<String, Map<Long, Double>>> calls_by_cap; //<tourists_residents, <cell_id, <time_slot, calls_by_cap>>>

	//private Map<String, Map<Long, Double>> calls_tourists_residents; //<cell_id, <time_slot, calls_by_tourists/calls_by_residents>>

	private Map<String, Double> roads_lengths; //<cell_id, tot_length>

	private List<String> grid;

	private Map<String, Double> max_occupancies;

	private Map<String, List<Double>> normCallsByCellAndTS;

	public LoadFiles() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("CM.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public static void main(String args[]) {
//		Mongo dao = new Mongo();
//
//		LoadFiles l = new LoadFiles();
//
//		l.baseDir = "/home/andrea/workspace/CrowdingModule/target/CrowdingModule/WEB-INF/data/";
//
//		l.roads_lengths = l.loadGridRoads();
//
//		l.grid = l.importGrid(dao);
//
//		l.veniceCaps = l.loadVeniceCaps();
//
//		//l.calls_by_cap = l.loadCallsByCap();
//
//		//l.callsDistribution();
//	}

	public Map<String, List<UncertainValue>> load(Mongo dao, String baseDir) {

		this.baseDir = baseDir;

		roads_lengths = loadGridRoads();

		grid = importGrid(dao);

		//veniceCaps = loadVeniceCaps();

		//calls_by_cap = loadCallsByCap();
		
		normCallsByCellAndTS = loadNormalizedCallsByCellandTS();

		//presences = loadPresences();

		return importGridCrowdings(dao);

	}

//	private Map<String, Map<Long, Double>> computeCrowdings() {
//
//		Map<String, Map<Long, Double>> result = new HashMap<>();
//		double maxValue = Double.MIN_VALUE;
//		try {
//			for (Iterator<String> iterCell = calls_by_cap.get(1).keySet().iterator(); iterCell.hasNext();) {
//				//(Iterator<String> iterCell = presences.keySet().iterator(); iterCell.hasNext();)
//				String currentCell = iterCell.next();
//				result.put(currentCell, new HashMap<Long, Double>());
//				for (Iterator<Long> iterTS = calls_by_cap.get(1).get(currentCell).keySet().iterator(); iterTS.hasNext();) {
//					Long currentTS = iterTS.next();
//					if (grid.contains(currentCell) && 
//							roads_lengths.containsKey(currentCell) &&
//							calls_tourists_residents.containsKey(currentCell) && 
//							calls_tourists_residents.get(currentCell).containsKey(currentTS) ) {
//						//Double presence_t = presences.get(currentCell).get(currentTS);
//						Double p_A_intersects_B = calls_tourists_residents.get(currentCell).get(currentTS);
//
//						Double p_A_due_B = p_A_intersects_B/p_B;
//						Double n_t = (p_A_due_B.equals(0D)) ? 0D : calls_by_cap.get(1).get(currentCell).get(currentTS)/p_A_due_B;
//						Double roads_area = roads_lengths.get(currentCell)*5d;
//						//result.get(currentCell).put(currentTS, presence_t*SCALE_FACTOR*calls_t_r*CALLS_FACTOR/roads_area); //people/m^2
//						result.get(currentCell).put(currentTS, n_t/roads_area); //people/m^2
//						if ((n_t/roads_area) > maxValue) {
//							maxValue = (n_t/roads_area);
//							//							logger.info("calls_t:"+calls_by_cap.get(1).get(currentCell).get(currentTS));
//							//							logger.info("calls_tot:"+(calls_by_cap.get(0).get(currentCell).get(currentTS)+calls_by_cap.get(1).get(currentCell).get(currentTS)));
//							//							logger.info("p_A_intersects_B:"+p_A_intersects_B);
//							//							logger.info("p_A_due_B:"+p_A_due_B);
//							//							logger.info("n_t:"+n_t);
//							//							logger.info("roads_area:"+roads_area);
//							//							logger.info("maxV:"+maxValue);
//						}
//					}
//				}
//			}
//
//			logger.info("Crowdings computed ("+result.size()+")");
//		} catch (Exception e) {
//			logger.info("Error on computing Crowdings");
//			e.printStackTrace();
//		}
//
//		return result;
//	}

	public Map<String, List<UncertainValue>> importGridCrowdings(Mongo dao) {


		//List<GridCrowding> crowdings = aggregateCrowdings(dao, computeCrowdings());
		List<GridCrowding> crowdings = serializeCrowdings(dao, normCallsByCellAndTS);

		try {

			dao.insertGridCrowdings(crowdings);

		} catch (Exception e) {
			logger.info("Error on importing Crowdings");
			e.printStackTrace();
		}

		return deserializeCrowdings(crowdings);


	}

	private Map<String, List<UncertainValue>> deserializeCrowdings(List<GridCrowding> crowdings) {
		Map<String, List<UncertainValue>> result = new HashMap<>();

		for (GridCrowding crowding : crowdings) {
			result.put(crowding.getCell(), crowding.getCrowdings());
		}

		return result;
	}

	private void importCrowdingStats(Mongo dao, double minValue, double maxValue) {
		dao.insertCrowdingStats(minValue, maxValue);
	}


//	private List<GridCrowding> aggregateCrowdings(Mongo dao, Map<String, Map<Long, Double>> computed_crowdings) {
//
//		Map<String, Map<Long, Double>> partial_result = new HashMap<>();
//
//		Map<String, Map<Long, Integer>> aggregated_occurrencies = new HashMap<>();
//
//		List<GridCrowding> result = new ArrayList<>();
//
//		double minValue = Double.MAX_VALUE;
//		double maxValue = Double.MIN_VALUE;
//
//		try {
//			for (Iterator<String> iterCell = computed_crowdings.keySet().iterator(); iterCell.hasNext();) {
//				String currentCell = iterCell.next();
//				partial_result.put(currentCell, new HashMap<Long, Double>());
//				aggregated_occurrencies.put(currentCell, new HashMap<Long, Integer>());
//				for (Iterator<Long> iterTS = computed_crowdings.get(currentCell).keySet().iterator(); iterTS.hasNext();) {
//					Long currentTS = iterTS.next();
//					Long dailybaseTS = dailybaseTS(currentTS);
//					if (partial_result.get(currentCell).containsKey(dailybaseTS)) {
//						Double tempVal = partial_result.get(currentCell).get(dailybaseTS);
//						Double currVal = computed_crowdings.get(currentCell).get(currentTS);
//						Integer tempOcc = aggregated_occurrencies.get(currentCell).get(dailybaseTS);
//						Integer newOcc = tempOcc+1;
//						Double newVal = ((tempOcc*tempVal+currVal)/newOcc);
//
//						if (newVal < minValue) {
//							minValue = newVal;
//						}
//						if (newVal > maxValue) {
//							maxValue = newVal;
//						}
//
//						partial_result.get(currentCell).put(dailybaseTS, newVal);
//						aggregated_occurrencies.get(currentCell).put(dailybaseTS, newOcc);
//
//					} else {
//						partial_result.get(currentCell).put(dailybaseTS, computed_crowdings.get(currentCell).get(currentTS));
//						aggregated_occurrencies.get(currentCell).put(dailybaseTS, 1);
//						if (computed_crowdings.get(currentCell).get(currentTS) < minValue) {
//							minValue = computed_crowdings.get(currentCell).get(currentTS);
//						}
//						if (computed_crowdings.get(currentCell).get(currentTS) > maxValue) {
//							maxValue = computed_crowdings.get(currentCell).get(currentTS);
//						}
//					}
//				}
//			}
//
//			for (Iterator<String> iterCell = partial_result.keySet().iterator(); iterCell.hasNext();) {
//				String currentCell = iterCell.next();
//				List<UncertainValue> values = new ArrayList<UncertainValue>();
//				for (Long time_slot = 0L; time_slot<=85500000L; time_slot+=900000L) {
//					Double value = 0d;
//					if(partial_result.get(currentCell).containsKey(time_slot)) {
//						value = round((partial_result.get(currentCell).get(time_slot)-minValue)/(maxValue-minValue),5);
//					}
//					values.add(new UncertainValue(value, "N:"+(value/10d)));
//				}
//				result.add(new GridCrowding(currentCell, values));
//			}
//
//			importCrowdingStats(dao, minValue, maxValue);
//
//			logger.info("Crowdings aggregated ("+result.size()+")");
//		} catch (Exception e) {
//			logger.info("Error on aggregating Crowdings");
//			logger.info(e.getMessage());
//		}
//
//		return result;
//	}
	
	
	private List<GridCrowding> serializeCrowdings(Mongo dao, Map<String, List<Double>> normalizedCalls) {

		List<GridCrowding> result = new ArrayList<>();

		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		try {
			for (String cell : normalizedCalls.keySet()) {
				List<UncertainValue> crowdings_list = new ArrayList<>();
				for (Double calls : normalizedCalls.get(cell)) {
					crowdings_list.add(new UncertainValue(calls, "N:"+round(calls/10d, 5)));
					if (calls < minValue) {
						minValue = calls;
					}
					if (calls > maxValue) {
						maxValue = calls;
					}
				}
				result.add(new GridCrowding(cell, crowdings_list));
			}
			
			importCrowdingStats(dao, minValue, maxValue);

		} catch (Exception e) {
			logger.info("Error on serializing Crowdings");
			logger.info(e.getMessage());
		}

		return result;
	}

//	private Long dailybaseTS(Long absoluteTS) {
//
//		Calendar c = new GregorianCalendar();
//		c.setTime(new Date(absoluteTS*1000l));
//
//		long hour = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
//		long minute = c.get(Calendar.MINUTE) * 60 * 1000;
//
//		return (hour+minute);
//	}

	public boolean isValid(String str) {  
		try  {  
			Long.parseLong(str);  
		} catch(NumberFormatException nfe) {  
			return false;  
		}  
		return true;  
	}


//	public void callsDistribution() {
//		Map<Integer, Map<String, Double>> calls_byPeopleCat_by15Mins = new HashMap<>();
//		calls_byPeopleCat_by15Mins.put(0, new HashMap<String, Double>());		//0 = calls made by residents
//		calls_byPeopleCat_by15Mins.put(1, new HashMap<String, Double>());		//1 = calls made by italian tourists
//		calls_byPeopleCat_by15Mins.put(2, new HashMap<String, Double>());		//2 = calls made by others
//
//
//		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"calls_cap.csv"), '\t', '@', 0)) {
//			String [] line;
//			while (null != (line = csvReader.readNext())) {
//				Calendar c = new GregorianCalendar();
//				c.setTime(new Date(Long.parseLong(line[0]+"000")));
//				String stringTime = TimeUtils.getString15MRoundTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
//
//				if (isValid(line[2]) && 
//						(veniceCaps.contains(Long.parseLong(line[2])) || 
//								(line[2].length() > 2 && 
//										veniceCaps.contains(Long.parseLong(line[2].substring(0, line[2].length()-2)+"00"))))) { //calls made by residents (id=0)
//					if (!calls_byPeopleCat_by15Mins.get(0).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(0).put(stringTime, Double.parseDouble(line[3]));
//					} else {
//						Double tempVal = calls_byPeopleCat_by15Mins.get(0).get(stringTime);
//						calls_byPeopleCat_by15Mins.get(0).put(stringTime, tempVal+Double.parseDouble(line[3]));
//					}
//					if (!calls_byPeopleCat_by15Mins.get(1).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(1).put(stringTime, 0d);
//					}
//					if (!calls_byPeopleCat_by15Mins.get(2).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(2).put(stringTime, 0d);
//					}
//				} else if (isValid(line[2]) && line[2].length() > 2) {
//					if (!calls_byPeopleCat_by15Mins.get(1).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(1).put(stringTime, Double.parseDouble(line[3]));
//					} else {
//						Double tempVal = calls_byPeopleCat_by15Mins.get(1).get(stringTime);
//						calls_byPeopleCat_by15Mins.get(1).put(stringTime, tempVal+Double.parseDouble(line[3]));
//					}
//					if (!calls_byPeopleCat_by15Mins.get(0).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(0).put(stringTime, 0d);
//					}
//					if (!calls_byPeopleCat_by15Mins.get(2).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(2).put(stringTime, 0d);
//					}
//				} else {
//					if (!calls_byPeopleCat_by15Mins.get(2).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(2).put(stringTime, Double.parseDouble(line[3]));
//					} else {
//						Double tempVal = calls_byPeopleCat_by15Mins.get(2).get(stringTime);
//						calls_byPeopleCat_by15Mins.get(2).put(stringTime, tempVal+Double.parseDouble(line[3]));
//					}
//					if (!calls_byPeopleCat_by15Mins.get(0).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(0).put(stringTime, 0d);
//					}
//					if (!calls_byPeopleCat_by15Mins.get(1).containsKey(stringTime)) {
//						calls_byPeopleCat_by15Mins.get(1).put(stringTime, 0d);
//					}
//				}
//			}
//
//			CSVWriter csvWriter = new CSVWriter(new FileWriter(baseDir+"distribs.csv"), ',', '\0');
//			csvWriter.writeNext(("time,residents,tourists_ita,tourists_for").split(","));
//
//			for (String stringTime : calls_byPeopleCat_by15Mins.get(0).keySet()) {
//				csvWriter.writeNext((stringTime+","
//						+calls_byPeopleCat_by15Mins.get(0).get(stringTime)+","
//						+calls_byPeopleCat_by15Mins.get(1).get(stringTime)+","
//						+calls_byPeopleCat_by15Mins.get(2).get(stringTime)).split(","));
//			}
//			csvWriter.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Load amount of users that generated at least a call-out in the cell and in the timeslot specified
	 * Split them in residents and tourists according to the CAP where they buy their SIM card
	 * Aggregate the quantities generated in the same cell and timeslot by people with the same category of CAP
	 * CAP categories are 2: the first identifies the residents (CAP contained in VeniceCaps collection)
	 * and the second represents the tourists (CAP not contained in VeniceCaps) 
	 * @return The total amount of tourists and residents making a call-out by cell and timeslot
	 */
//	public Map<Integer, Map<String, Map<Long, Double>>> loadCallsByCap() {
//		Map<Integer, Map<String, Map<Long, Double>>> result = new HashMap<>();
//
//		result.put(0, new HashMap<String, Map<Long, Double>>()); //0 = calls made by residents
//		result.put(1, new HashMap<String, Map<Long, Double>>()); //1 = calls made by tourists
//		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"calls_cap.csv"), '\t', '@', 0)) {
//
//			String [] line;
//			while (null != (line = csvReader.readNext())) {
//				if (isValid(line[2]) && grid.contains(line[1])) {
//					if ( (line[2].length() > 1 ) && (veniceCaps.contains(Long.parseLong(line[2])) || 
//							(veniceCaps.contains(Long.parseLong(line[2].substring(0, line[2].length()-2)+"00"))))) { //calls made by residents (id=0)
//						if (!result.get(0).containsKey(line[1])) {
//							result.get(0).put(line[1], new HashMap<Long, Double>());
//							result.get(0).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])));
//						} else {
//							if (result.get(0).get(line[1]).containsKey(Long.parseLong(line[0]))) {
//								Double tempVal = result.get(0).get(line[1]).get(Long.parseLong(line[0]));
//								result.get(0).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])+tempVal));
//							} else {
//								result.get(0).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])));
//							}
//						}
//						if (!result.get(1).containsKey(line[1])) { //initialization tourists in current cell and timeslot to 0
//							result.get(1).put(line[1], new HashMap<Long, Double>());
//							result.get(1).get(line[1]).put(Long.parseLong(line[0]), 0d);
//						} else if (!result.get(1).get(line[1]).containsKey(Long.parseLong(line[0]))) {
//							result.get(1).get(line[1]).put(Long.parseLong(line[0]), 0d);
//						}
//					} else { //calls made by tourists (id=1)
//						if (!result.get(1).containsKey(line[1])) {
//							result.get(1).put(line[1], new HashMap<Long, Double>());
//							result.get(1).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])));
//						} else {
//							if (result.get(1).get(line[1]).containsKey(Long.parseLong(line[0]))) {
//								Double tempVal = result.get(1).get(line[1]).get(Long.parseLong(line[0]));
//								result.get(1).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])+tempVal));
//							} else {
//								result.get(1).get(line[1]).put(Long.parseLong(line[0]), (Double.parseDouble(line[3])));
//							}
//						}
//						if (!result.get(0).containsKey(line[1])) { //initialization residents in current cell and timeslot to 0
//							result.get(0).put(line[1], new HashMap<Long, Double>());
//							result.get(0).get(line[1]).put(Long.parseLong(line[0]), 0d);
//						} else if (!result.get(0).get(line[1]).containsKey(Long.parseLong(line[0]))) {
//							result.get(0).get(line[1]).put(Long.parseLong(line[0]), 0d);
//						}
//					}
//				}
//			}
//
//			//CSVWriter csvWriter = new CSVWriter(new FileWriter(baseDir+"residents.csv"), ',', '\0');
//			//csvWriter.writeNext(("grid_id,timeslot,callout_by_residents").split(","));
//
//			calls_tourists_residents = new HashMap<>();
//			// csvWriter2 = new CSVWriter(new FileWriter(baseDir+"tourists_residents.csv"), ',', '\0');
//			//csvWriter2.writeNext(("grid_id,timeslot,tourists,residents,tourists/(tourists+residents)").split(","));
//
//			for (Iterator<String> iterCell = result.get(1).keySet().iterator(); iterCell.hasNext();) {
//				String currentCell = iterCell.next();
//				for (Iterator<Long> iterTS = result.get(1).get(currentCell).keySet().iterator(); iterTS.hasNext();) {
//					Long currentTS = iterTS.next();
//					double tourists = -1d;
//					double residents = -1d;
//					//if (result.get(0).containsKey(currentCell) && result.get(0).get(currentCell).containsKey(currentTS)) {
//					tourists = result.get(1).get(currentCell).get(currentTS);
//					residents = result.get(0).get(currentCell).get(currentTS);
//					//csvWriter.writeNext((currentCell+","+currentTS+","+residents).split(","));
//					//}
//					if (!calls_tourists_residents.containsKey(currentCell)) {
//						calls_tourists_residents.put(currentCell, new HashMap<Long, Double>());
//					}
//					Double rate = 1d;
//					//if (-1d != tourists) {
//					rate = tourists/(tourists+residents);
//					//}
//					calls_tourists_residents.get(currentCell).put(currentTS, rate);
//					//csvWriter2.writeNext((currentCell+","+currentTS+","+tourists+","+residents+","+rate).split(","));
//				}
//			}
//			//csvWriter.close();
//			//csvWriter2.close();
//
//
//		} catch (Exception e) {
//			logger.info("Error on loading Calls by Cap");
//			e.printStackTrace();
//		}
//		logger.info("Calls by Cap loaded ("+result.get(0).size()+")");
//		return result;
//	}
	
	
	public Map<String, List<Double>> loadNormalizedCallsByCellandTS() {
		Map<String, List<Double>> result = new HashMap<>();
		
		for (String cell : grid) {
			result.put(cell, new ArrayList<Double>(96));
			for (int i=0; i<96; i++) {
				result.get(cell).add(i, 0d);
			}
		}

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"ts_cell_occupancy_"+weekend_weekdays+"_avg_n.csv"), '\t', '"', 0)) {
			//line[0] = TS
			//line[1] = cell
			//line[2] = calls sum over the period
			//line[3] = calls average over the period (count days only when count calls > 0)
			//line[4] = calls average over the period (count days only when count calls >= 0)
			//line[5] = normalized calls (wrt the max_occupancy of the cell) on days where count > 0
			//line[6] = normalized calls (wrt the max_occupancy of the cell) on days where count >= 0
			
			String [] line;
			
			while (null != (line = csvReader.readNext())) {
				List<Double> v = result.get(line[1]);
				if(v == null)
					logger.warn(line[1]+" is not on the grid");
				else
					v.add(TimeUtils.getTimeSlot(Long.parseLong(line[0])*1000l), Double.parseDouble(line[6]));
			}
			
 
		} catch (Exception e) {
			logger.info("Error on loading Normalized Calls by Cell and TS");
			e.printStackTrace();
		}
		logger.info("Normalized calls loaded ("+result.size()+")");
		return result;
	}
	/*
	public List<Long> loadVeniceCaps() {
		List<Long> result = new ArrayList<Long>();

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"venice_cap.csv"), ',', '@', 1)) {

			String [] line;
			while (null != (line = csvReader.readNext())) {
				result.add(Long.parseLong(line[0]));
			}

		} catch (Exception e) {
			logger.info("Error on loading Venice Caps");
			e.printStackTrace();
		}
		logger.info("Venice Caps loaded ("+result.size()+")");
		return result;
	}
	*/
	//	private Map<String, Map<Long, Double>> loadPresences() {
	//		Map<String, Map<Long, Double>> result = new HashMap<>();
	//
	//		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"presences.csv"), ',', '@', 1)) {
	//
	//			String [] line;
	//			while (null != (line = csvReader.readNext())) {
	//				if (!result.containsKey(line[0])) {
	//					result.put(line[0], new HashMap<Long, Double>());
	//				}
	//				Double calls = 0d;
	//				if (calls_by_cap.get(0).containsKey(line[0]) && calls_by_cap.get(0).get(line[0]).containsKey((Long.parseLong(line[2])/1000l))) {
	//					calls = calls_by_cap.get(0).get(line[0]).get((Long.parseLong(line[2])/1000l));
	//				}
	//				result.get(line[0]).put((Long.parseLong(line[2])/1000l), (Double.parseDouble(line[1])/10d-calls));
	//			}
	//
	//		} catch (Exception e) {
	//			logger.info("Error on loading Presences");
	//			e.printStackTrace();
	//		}
	//		logger.info("Presences loaded ("+result.size()+")");
	//		return result;
	//	}

	public List<String> importGrid(Mongo dao) {

		List<Cell> grid = new ArrayList<>();
		List<String> result = new ArrayList<>();

		double bbox_min_lng = Double.MAX_VALUE;
		double bbox_max_lng = Double.MIN_VALUE;
		double bbox_min_lat = Double.MAX_VALUE;
		double bbox_max_lat = Double.MIN_VALUE;

		String tl[] = p.getProperty("bbox.tl").split(", ");
		String tr[] = p.getProperty("bbox.tr").split(", ");
		String br[] = p.getProperty("bbox.br").split(", ");
		String bl[] = p.getProperty("bbox.bl").split(", ");

		Polygon bbox = new Polygon(
				new LngLatAlt(Double.parseDouble(tl[0]), Double.parseDouble(tl[1])),
				new LngLatAlt(Double.parseDouble(tr[0]), Double.parseDouble(tr[1])),
				new LngLatAlt(Double.parseDouble(br[0]), Double.parseDouble(br[1])),
				new LngLatAlt(Double.parseDouble(bl[0]), Double.parseDouble(bl[1])),
				new LngLatAlt(Double.parseDouble(tl[0]), Double.parseDouble(tl[1])));

		for (LngLatAlt coord : bbox.getExteriorRing()) {
			if (coord.getLongitude() < bbox_min_lng) {
				bbox_min_lng = coord.getLongitude();
			}
			if (coord.getLongitude() > bbox_max_lng) {
				bbox_max_lng = coord.getLongitude();
			}
			if (coord.getLatitude() < bbox_min_lat) {
				bbox_min_lat = coord.getLatitude();
			}
			if (coord.getLatitude() > bbox_max_lat) {
				bbox_max_lat = coord.getLatitude();
			}
		}

		max_occupancies = loadMaxOccupancies();

		try{

			FeatureCollection featureCollection = new ObjectMapper().readValue(new FileReader(baseDir+"grid.geojson"), FeatureCollection.class);

			for (Feature f : featureCollection.getFeatures()) {

				boolean toImport = true;
				for (LngLatAlt coord : ((Polygon)f.getGeometry()).getExteriorRing()) {
					if (coord.getLongitude() > bbox_max_lng || coord.getLongitude() < bbox_min_lng 
							|| coord.getLatitude() > bbox_max_lat || coord.getLatitude() < bbox_min_lat ) {
						toImport = false;
						break;
					}
				}
				if (toImport) {
					double max_occupancy = max_occupancies.get((String)f.getProperties().get("id"));
					grid.add(new Cell(
							(String)f.getProperties().get("id"), 
							(double)f.getProperties().get("area"), 
							(roads_lengths.containsKey((String)f.getProperties().get("id"))) ? roads_lengths.get((String)f.getProperties().get("id")) : 0d,
									max_occupancy,
									(Polygon)f.getGeometry()));
					result.add((String)f.getProperties().get("id"));
				}
			}

			dao.insertGrid(grid);
			logger.info("Grid imported ("+grid.size()+")");
		} catch (Exception e) {
			logger.info("Error on importing Grid");
			e.printStackTrace();
		}
		return result;
	}

	private Map<String, Double> loadMaxOccupancies() {
		Map<String, Double> result = new HashMap<>();
		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"cell_max"), ' ')) {
			String [] line;
			while (null != (line = csvReader.readNext())) {
				result.put(line[0], Double.parseDouble(line[1]));
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return result;
	}

	public Map<String, Double> loadGridRoads() {
		Map<String, Double> result = new HashMap<>();

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"grid_roads.csv"), ',', '@', 1)) {

			String [] line;
			while (null != (line = csvReader.readNext())) {
				result.put(line[0], Double.parseDouble(line[1]));
			}

		} catch(Exception e) {
			logger.info("Error on loading Grid Roads lengths");
			e.printStackTrace();
		}
		logger.info("Grid Roads lengths loaded ("+result.size()+")");
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
