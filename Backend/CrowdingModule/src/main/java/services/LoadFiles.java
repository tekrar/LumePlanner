package services;

import io.CityProp;
import io.Mongo;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

import static util.Misc.round;

public class LoadFiles {

	private String weekend_weekdays = "wd"; //we

	private Logger logger = Logger.getLogger(LoadFiles.class);
	private String baseDir;
	private Map<String, Double> roads_lengths; //<cell_id, tot_length>
	private List<String> grid;
	private Map<String, Double> max_occupancies;
	private Map<String, List<Double>> normCallsByCellAndTS;


	public Map<String, List<UncertainValue>> load(Mongo dao, String city) {

		baseDir = this.getClass().getResource("/../data/"+ CityProp.getInstance().get(city).getDataDir()).getPath();
		roads_lengths = loadGridRoads();
		grid = importGrid(dao,city);
		normCallsByCellAndTS = loadNormalizedCallsByCellandTS();
		return importGridCrowdings(dao);

	}

	public Map<String, List<UncertainValue>> importGridCrowdings(Mongo dao) {
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


	public boolean isValid(String str) {  
		try  {  
			Long.parseLong(str);  
		} catch(NumberFormatException nfe) {  
			return false;  
		}  
		return true;  
	}
	
	
	public Map<String, List<Double>> loadNormalizedCallsByCellandTS() {
		Map<String, List<Double>> result = new HashMap<>();
		
		for (String cell : grid) {
			result.put(cell, new ArrayList<Double>(96));
			for (int i=0; i<96; i++) {
				result.get(cell).add(i, 0d);
			}
		}

		logger.info(">>>> "+grid);

		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"ts_cell_occupancy_"+weekend_weekdays+"_avg_n.csv"), '\t', '"', 0)) {
			//line[0] = TS
			//line[1] = cell
			//line[2] = calls sum over the period
			//line[3] = calls average over the period (count days only when count calls > 0)
			//line[4] = calls average over the period (count days only when count calls >= 0)
			//line[5] = normalized calls (wrt the max_occupancy of the cell) on days where count > 0
			//line[6] = normalized calls (wrt the max_occupancy of the cell) on days where count >= 0
			String [] line;
			Set<String> offGridCells = new HashSet<>();
			while (null != (line = csvReader.readNext())) {
				List<Double> v = result.get(line[1]);
				if(v == null) offGridCells.add(line[1]);
				else v.add(TimeUtils.getTimeSlot(Long.parseLong(line[0])*1000l), Double.parseDouble(line[6]));
			}
			logger.warn("grid size = "+grid.size()+" off_grid_count = "+offGridCells.size());

		} catch (Exception e) {
			logger.info("Error on loading Normalized Calls by Cell and TS");
			e.printStackTrace();
		}
		logger.info("Normalized calls loaded ("+result.size()+")");
		return result;
	}


	public List<String> importGrid(Mongo dao, String city) {

		List<Cell> grid = new ArrayList<>();
		List<String> result = new ArrayList<>();

		double bbox_min_lng = Double.MAX_VALUE;
		double bbox_max_lng = Double.MIN_VALUE;
		double bbox_min_lat = Double.MAX_VALUE;
		double bbox_max_lat = Double.MIN_VALUE;



		String tl[] = CityProp.getInstance().get(city).getTL().split(",");
		String tr[] = CityProp.getInstance().get(city).getTR().split(",");
		String br[] = CityProp.getInstance().get(city).getBR().split(",");
		String bl[] = CityProp.getInstance().get(city).getBL().split(",");

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
					double max_occupancy = max_occupancies.get(f.getProperties().get("id"));
					grid.add(new Cell(
							(String)f.getProperties().get("id"), 
							(double)f.getProperties().get("area"), 
							(roads_lengths.containsKey(f.getProperties().get("id"))) ? roads_lengths.get(f.getProperties().get("id")) : 0d,
									max_occupancy,
									(Polygon)f.getGeometry()));
					result.add((String)f.getProperties().get("id"));
				}
			}

			if(dao!=null) {
				dao.insertGrid(grid);
				logger.info("Grid imported ("+grid.size()+")");
			}
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
		String [] line = null;
		try (CSVReader csvReader = new CSVReader(new FileReader(baseDir+"grid_roads.csv"), ',', '@', 1)) {

			int skipped = 0 ;
			while (null != (line = csvReader.readNext())) {
				if(line.length < 2) {
					skipped++;
					continue;
				}
				result.put(line[0], Double.parseDouble(line[1]));
			}
			logger.info("skipped "+skipped+" lines");

		} catch(Exception e) {
			logger.info(line.length);
			logger.info("Error on loading Grid Roads lengths");
			e.printStackTrace();
		}
		logger.info("Grid Roads lengths loaded ("+result.size()+")");
		return result;
	}

	public static void main(String[] args) throws Exception {
		LoadFiles lf = new LoadFiles();
		lf.baseDir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\ReggioEmilia\\";
		lf.roads_lengths = lf.loadGridRoads();
		lf.grid = lf.importGrid(null, "Bologna");
		Map<String, List<Double>> grdi_data =  lf.loadNormalizedCallsByCellandTS();
		System.out.println("Done");
	}

}
