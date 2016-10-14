package io;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import model.Cell;
import model.CrowdingStats;
import model.GridCrowding;
import model.P2PCellPaths;
import model.POI;
import model.POI2POICrowding;
import model.Timing;
import model.TimingTo;
import model.UncertainValue;
import model.User;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.geojson.Point;

import util.PointCodec;
import util.RandomValue;
import util.TimeUtils;
import util.UncertainValueCodec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class Mongo {

	private Properties p;

	private Logger logger = Logger.getLogger(Mongo.class);

	private ObjectMapper mapper;

	private MongoClient mongoClient;

	private MongoDatabase db;

	private java.util.logging.Logger mongoLogger;

	public Mongo () {

		CodecRegistry codecRegistry = 
				CodecRegistries.fromRegistries(
						CodecRegistries.fromCodecs(new PointCodec(), new UncertainValueCodec()),
						MongoClient.getDefaultCodecRegistry());  

		p = new Properties();

		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		mongoLogger = java.util.logging.Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE); 

		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("CM.properties"));
			mongoClient = new MongoClient(new ServerAddress(p.getProperty("mongo.url")),
					Arrays.asList(
							MongoCredential.createCredential(
									p.getProperty("mongo.user"),
									p.getProperty("mongo.db"),
									p.getProperty("mongo.password").toCharArray())),
									MongoClientOptions.builder().codecRegistry(codecRegistry).build());
			db = mongoClient.getDatabase(p.getProperty("mongo.db"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//	public void closeMongoConnection(){
	//		this.mongoClient.close();
	//	}


	/*
	 * *************************************** INSERT ***************************************
	 */


	public void testInsertion() throws ParseException{
		db.getCollection("testCollection").insertOne(
				new Document("id",new Document("id1", "key1").append("id2", "key2")));
		List<Document> docList = new ArrayList<Document>();
		docList.add(new Document("key1", "value1"));
		docList.add(new Document("key2", "value2"));
		docList.add(new Document("key3", "value3"));
		db.getCollection("testCollection").insertMany(docList);
	}


	public void insertGrid(List<Cell> grid) {
		if (db.getCollection("grid").count() == 0l ) {
			List<Document> gridDocuments = new ArrayList<Document>();
			for (int i =0; i< grid.size(); i++) {
				gridDocuments.add(Document.parse(grid.get(i).toJSONString()));
			}
			db.getCollection("grid").insertMany(gridDocuments);
		}
	}


	public void insertGridCrowdings(List<GridCrowding> crowdings) {
		if (db.getCollection("grid_crowdings").count() == 0l ) {
			List<Document> crowdDocuments = new ArrayList<Document>();
			for (int i =0; i< crowdings.size(); i++) {
				crowdDocuments.add(Document.parse(crowdings.get(i).toJSONString()));
			}
			db.getCollection("grid_crowdings").insertMany(crowdDocuments);
		}

	}

	public void insertCrowdings(List<POI2POICrowding> crowdings) {
		if (db.getCollection("crowdings").count() > 0l ) {
			removeCrowdings();
		}
		List<Document> crowdDocuments = new ArrayList<Document>();
		for (int i =0; i< crowdings.size(); i++) {
			crowdDocuments.add(Document.parse(crowdings.get(i).toJSONString()));
		}
		db.getCollection("crowdings").insertMany(crowdDocuments);

	}

	public void insertCrowding(POI2POICrowding crowding) {
		Document crowdDocument = Document.parse(crowding.toJSONString());
		db.getCollection("crowdings").insertOne(crowdDocument);
	}
	
	public void updateCrowding(POI2POICrowding crowding) {
		Document crowdDocument = Document.parse(crowding.toJSONString());
		Document filter = new Document("from_id", crowding.getFrom_id()).append("to_id", crowding.getTo_id());
		if (db.getCollection("crowdings").find(filter).iterator().hasNext()) {
			db.getCollection("crowdings").replaceOne(filter, crowdDocument);
		} else {
			insertCrowding(crowding);
		}
	}

	public void insertP2PPaths(List<P2PCellPaths> paths) {
		if (db.getCollection("cellpaths").count() == 0l ) {

			List<Document> pathsDocuments = new ArrayList<Document>();
			for (int i =0; i< paths.size(); i++) {
				pathsDocuments.add(Document.parse(paths.get(i).toJSONString()));
			}
			db.getCollection("cellpaths").insertMany(pathsDocuments);
		}
	}

	public void insertP2PPaths(P2PCellPaths path) {
		Document pathsDocument = Document.parse(path.toJSONString());
		db.getCollection("cellpaths").insertOne(pathsDocument);
	}


	public void insertCrowdingStats(double minValue, double maxValue) {
		CrowdingStats cs;
		try {
			if (db.getCollection("crowdingStats").count() == 0l ) {
				cs = new CrowdingStats(minValue, maxValue);
				db.getCollection("crowdingStats").insertOne(Document.parse(cs.toJSONString()));					
			} else {
				cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
				cs.setMinValue(minValue);
				cs.setMaxValue(maxValue);
				db.getCollection("crowdingStats").replaceOne(new Document(), Document.parse(cs.toJSONString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateGridCrowdingStats(double oldMaxValue, double newMaxValue) {
		db.getCollection("crowdingStats").replaceOne(new Document(), new Document("minValue", 0d).append("maxValue", newMaxValue));
		FindIterable<Document> crowdings = db.getCollection("grid_crowdings").find();
		logger.info("Max crowding level updated from "+oldMaxValue+" to "+newMaxValue);
		try {
			for (Document crowd : crowdings) {
				GridCrowding grid_crowd = mapper.readValue(crowd.toJson(), GridCrowding.class);
				List<UncertainValue> this_crowd = grid_crowd.getCrowdings();
				for (UncertainValue uncertainValue : this_crowd) {
					uncertainValue.setMean(round(uncertainValue.getMean() * oldMaxValue / newMaxValue, 5));
					uncertainValue.setDistribution("N:"+round(uncertainValue.getMean()/10d, 5));

				}
				grid_crowd.setCrowdings(this_crowd);
				db.getCollection("grid_crowdings").findOneAndReplace(
						new Document("cell", grid_crowd.getCell()), 
						new Document("cell", grid_crowd.getCell()).append("crowdings", grid_crowd.getCrowdings()));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private FindIterable<Document> getOtherUsers(String user_mail) {
		return db.getCollection("users").find(new Document("id", new Document("$ne", UUID.nameUUIDFromBytes(user_mail.getBytes()).toString())));
	}

	public boolean updateUserCrowdingStats(String user_mail, double newLowValue, double newLowAvgValue, double newAvgHigValue, double newHigValue) {
		CrowdingStats cs = null;
		double sum_values = 0d;
		double num_sum = 0d;
		boolean returnValue = false;
		try {
			User thisUser = mapper.readValue(db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(user_mail.getBytes()).toString())).first().toJson(), User.class);
			if (newLowValue != 0d && thisUser.getLow_crowding().getMean() < newLowValue) {
				sum_values += newLowValue;
				num_sum += 1d;
				for (Document user : getOtherUsers(user_mail)) {
					User current_user = mapper.readValue(user.toJson(), User.class);
					if (current_user.getLow_crowding().getMean() < newLowValue) {
						sum_values += current_user.getLow_crowding().getMean();
						num_sum += 1d;
					}
					cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
					cs.setAvgLowValue(sum_values/num_sum);
				}

				returnValue = true;

			} else if (newLowAvgValue != 0d && thisUser.getLowAvg_crowding().getMean() < newLowAvgValue) {
				sum_values += newLowAvgValue;
				num_sum += 1d;
				for (Document user : getOtherUsers(user_mail)) {
					User current_user = mapper.readValue(user.toJson(), User.class);
					if (current_user.getLowAvg_crowding().getMean() < newLowAvgValue) {
						sum_values += current_user.getLowAvg_crowding().getMean();
						num_sum += 1d;
					}
					cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
					cs.setAvgLowAvgValue(sum_values/num_sum);
				}

				returnValue = true;

			} else if (newAvgHigValue != 0d && thisUser.getAvgHig_crowding().getMean() < newAvgHigValue) {
				sum_values += newAvgHigValue;
				num_sum += 1d;
				for (Document user : getOtherUsers(user_mail)) {
					User current_user = mapper.readValue(user.toJson(), User.class);
					if (current_user.getAvgHig_crowding().getMean() < newAvgHigValue) {
						sum_values += current_user.getAvgHig_crowding().getMean();
						num_sum += 1d;
					}
					cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
					cs.setAvgAvgHigValue(sum_values/num_sum);
				}

				returnValue = true;

			} else if (newHigValue != 0d && thisUser.getHig_crowding().getMean() > newHigValue) {
				sum_values += newHigValue;
				num_sum += 1d;
				for (Document user : getOtherUsers(user_mail)) {
					User current_user = mapper.readValue(user.toJson(), User.class);
					if (current_user.getHig_crowding().getMean() > newHigValue) {
						sum_values += current_user.getHig_crowding().getMean();
						num_sum += 1d;
					}
					cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
					cs.setAvgHigValue(sum_values/num_sum);
				}

				returnValue = true;
			}

			if (returnValue) {
				db.getCollection("crowdingStats").replaceOne(new Document(), Document.parse(cs.toJSONString()));
			}

			return returnValue; //no need to update


		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}



	public Double retrieveGridCrowding(Cell current, Long arr_time_inCell, Long dep_time_fromCell) {
		Long start_time = TimeUtils.getMillis15MRoundTime(arr_time_inCell);
		Long end_time = TimeUtils.getMillis15MRoundTime(dep_time_fromCell);
		if (end_time<start_time) end_time += 86400000L;
		int number_time_slots = 1 + ((int)(end_time - start_time))/900000;

		double max_mean = Double.MIN_VALUE;
		try {
			GridCrowding current_crowding = mapper.readValue(db.getCollection("grid_crowdings").find(new Document("cell", current.getId())).first().toJson(), GridCrowding.class);
			for (int i=0; i<number_time_slots; i++) {
				double mean = current_crowding.getCrowdings().get(TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)).getMean();
				max_mean = (mean > max_mean) ? mean : max_mean; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return max_mean;
	}

	public boolean checkCrowdingLevels(){
		if (db.getCollection("crowdings").count() == 0l) {
			return false;
		}
		return true;
	}

	public boolean checkCellPaths(){
		if (db.getCollection("cellpaths").count() == 0l) {
			return false;
		}
		return true;
	}

	public Map<String, HashMap<String, List<UncertainValue>>> retrieveCrowdingLevels() {
		Map<String, HashMap<String, List<UncertainValue>>> result = new HashMap<>();
		try {
			for (Iterator<Document> iter = db.getCollection("crowdings").find().iterator(); iter.hasNext();) {

				POI2POICrowding crowding = mapper.readValue(iter.next().toJson(), POI2POICrowding.class);

				if (!result.containsKey(crowding.getFrom_id())) {
					result.put(crowding.getFrom_id(), new HashMap<String, List<UncertainValue>>());
				}
				result.get(crowding.getFrom_id()).put(crowding.getTo_id(), crowding.getCrowdings());

			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}



	public void updateGridCrowdings(Cell current, Long arr_time_inCell, Long dep_time_fromCell, String inc_dec) {
		
		double POfMakingACallTIM = 0.008035714;
		
		double max_in_cell = findMaxInitialCrowding(current.getId());

		int i_d_val = (inc_dec.equals("i")) ? 1 : -1;

		Long start_time = TimeUtils.getMillis15MRoundTime(arr_time_inCell);
		Long end_time = TimeUtils.getMillis15MRoundTime(dep_time_fromCell);
		int number_time_slots = 1 + ((int)(end_time - start_time))/900000;
		Document stats = db.getCollection("crowdingStats").find().first();

		double old_max = stats.getDouble("maxValue");
		try {
			GridCrowding grid_crowd = mapper.readValue(db.getCollection("grid_crowdings").find(new Document("cell", current.getId())).first().toJson(), GridCrowding.class);
			List<UncertainValue> crowdings = grid_crowd.getCrowdings();
			for (int i=0; i<number_time_slots; i++) {
				double current_mean = crowdings.get(TimeUtils.getTimeSlot(start_time+900000*i)).getMean();
				////////////////
				double new_mean = round(current_mean + ((i_d_val / max_in_cell) * POfMakingACallTIM), 5);
				///////////////
				// The new_mean is normalized to the current maxValue in the stats.
				// The normalizaion to the eventual new maxValue is done afterwards in updateGridCrowdingStats
				crowdings.get(TimeUtils.getTimeSlot(start_time+900000*i)).setMean(new_mean);
				crowdings.get(TimeUtils.getTimeSlot(start_time+900000*i)).setDistribution("N:"+round(new_mean/10d, 5));

				new_mean = (new_mean < 0d) ? 0d : new_mean;

				if (new_mean > old_max) {
					logger.info("Max Value changed ("+old_max+"-->"+new_mean+"). Updating all the congestions");
					updateGridCrowdingStats(old_max, new_mean);
					old_max = new_mean;
				}

			}
			db.getCollection("grid_crowdings").findOneAndReplace(
					new Document("cell", current.getId()), 
					new Document("cell", current.getId()).append("crowdings", crowdings));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	private double findMaxInitialCrowding(String id) {
		Cell result = null;
		Document doc_cell = db.getCollection("grid").find(new Document("id", id)).first();
		try {
			result = mapper.readValue(doc_cell.toJson(), Cell.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.getMax_occupancies();
	}

	/*
	 * *************************************** RETRIEVE ***************************************
	 */


	public void testQuery() {
		MongoCollection<Document> collection = db.getCollection("testCollection");
		FindIterable<Document> cursor = collection.find();
		for (Iterator<Document> iter = cursor.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
		}
	}


	public List<POI> retrieveActivities() {
		List<POI> result = new ArrayList<POI>();
		try {
			for (Iterator<Document> iter = db.getCollection("activities").find().iterator(); iter.hasNext();) {
				result.add(mapper.readValue(iter.next().toJson(), POI.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	public POI retrievePOI(String place_id) {
		POI result = null;
		try {
			result = mapper.readValue(db.getCollection("activities").find(new Document("place_id", place_id)).first().toJson(), POI.class);
		} catch(Exception e) {
			e.printStackTrace();
			logger.info("error:"+place_id);
		}
		return result;
	}

	public Cell retrieveCell(String cell_id) {
		Cell result = null;
		try {
			result = mapper.readValue(db.getCollection("grid").find(new Document("id", cell_id)).first().toJson(), Cell.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<Cell> retrieveGrid() {
		List<Cell> result = new ArrayList<>();
		try {
			FindIterable<Document> query =db.getCollection("grid").find();
			for (Document doc : query) {
				result.add(mapper.readValue(doc.toJson(), Cell.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Cell retrieveCellofPOI(String place_id) {
		Cell result = null;
		POI poi;
		try {
			logger.info("retrieveCellofPOI ----> place_id = "+place_id);
			System.err.println("retrieveCellofPOI ----> place_id = "+place_id);
			poi = mapper.readValue(db.getCollection("activities").find(new Document("place_id", place_id)).first().toJson(), POI.class);
			logger.info("retrieveCellofPOI ----> POI = "+poi);
			System.err.println("retrieveCellofPOI ----> POI = "+poi);
			System.err.println("retrieveCellofPOI ----> POI GEOMETRY = "+poi.getGeometry());

			Document query = new Document("geometry", new Document("$near", new Document("$geometry", poi.getGeometry())));
			System.err.println(query);


			Document first = db.getCollection("grid").find(query).first();
			result = mapper.readValue(first.toJson(), Cell.class);

		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	public List<Cell> retrieveAdjacentCells(Point point) {
		List<Cell> result = new ArrayList<>();
		try {
			for (Iterator<Document> iterDocument = 
					db.getCollection("grid").find(new Document("geometry", new Document("$geoIntersects", 
							new Document("$geometry", point)))).iterator(); iterDocument.hasNext(); ) {
				result.add(mapper.readValue(iterDocument.next().toJson(), Cell.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}


	public Map<String, List<UncertainValue>> retrieveGridCrowdings() {
		Map<String, List<UncertainValue>> result = new HashMap<>();
		try {
			FindIterable<Document> iterable = db.getCollection("grid_crowdings").find();
			for (Iterator<Document> iter = iterable.iterator(); iter.hasNext();) {
				GridCrowding crowding = mapper.readValue(iter.next().toJson(), GridCrowding.class);
				result.put(crowding.getCell(), crowding.getCrowdings());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}



	public Long retrieveTravelTime(String poi_start, String poi_end, Long dep_time_millis) {
		UncertainValue travel_time = null;
		try {
			Document filter = new Document("from", poi_start); 
			Document result = db.getCollection("timings").find(filter).first();
			Timing timing = mapper.readValue(result.toJson(), Timing.class);
			logger.info("timing size:"+timing.getTimes().size());
			for (TimingTo timing_to : timing.getTimes()) {
				if (timing_to.getTo().equals(poi_end)) {
					travel_time = timing_to.getTime().get(TimeUtils.getTimeSlot(TimeUtils.getMillis15MRoundTime(dep_time_millis)));
					break;
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		return ((long)RandomValue.get(travel_time))*60*1000l;
	}



	public Map<String, HashMap<String, List<UncertainValue>>> retrieveTravelTimes() {
		Map<String, HashMap<String, List<UncertainValue>>> result = new HashMap<>();
		try {
			FindIterable<Document> iterable = db.getCollection("timings").find();
			for (Iterator<Document> iter = iterable.iterator(); iter.hasNext();) {
				Timing timing = mapper.readValue(iter.next().toJson(), Timing.class);
				for(TimingTo to : timing.getTimes()) {
					if (!result.containsKey(timing.getFrom())) {
						result.put(timing.getFrom(), new HashMap<String, List<UncertainValue>>());
					}
					result.get(timing.getFrom()).put(to.getTo(), to.getTime());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	public UncertainValue retrieveCrowdingLevelP2P(String from, String to, String time) {
		try {
			Document crowd_doc = db.getCollection("crowdings").find(new Document("from_id", from).append("to_id", to)).first();
			if (null == crowd_doc) {
				crowd_doc = db.getCollection("crowdings").find(new Document("from_id", to).append("to_id", from)).first();
			}
			POI2POICrowding crowding = mapper.readValue(crowd_doc.toJson(), POI2POICrowding.class);
			return crowding.getCrowdings().get(TimeUtils.getTimeSlot(TimeUtils.getMillis(time)));

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public Map<String, Map<String, Map<Integer, Map<String, Double>>>> retrieveCellPaths() {
		Map<String, Map<String, Map<Integer, Map<String, Double>>>> result = new HashMap<>();
		try {
			FindIterable<Document> iterable = db.getCollection("cellpaths").find();
			for (Iterator<Document> iter = iterable.iterator(); iter.hasNext();) {
				P2PCellPaths path = mapper.readValue(iter.next().toJson(), P2PCellPaths.class);
				if (!result.containsKey(path.getFrom())) {
					result.put(path.getFrom(), new HashMap<String, Map<Integer, Map<String, Double>>>());
				}
				result.get(path.getFrom()).put(path.getTo(), path.getCell_distance());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	/*
	 * *************************************** REMOVE ***************************************
	 */


	public void testRemoval() {
		MongoCollection<Document> collection = db.getCollection("testCollection");
		collection.deleteOne(new Document("key1", "value1"));
		collection.deleteMany(new Document());
	}


	public void removeCrowdings() {
		MongoCollection<Document> collection = db.getCollection("crowdings");
		collection.deleteMany(new Document());

	}









	private double round(double x, int position)
	{
		double a = x;
		double temp = Math.pow(10.0, position);
		a *= temp;
		a = Math.round(a);
		return (a / (double)temp);
	}

	public double findMaxGridCrowding() {
		double max = Double.MIN_VALUE;
		try {
			FindIterable<Document> documents = db.getCollection("grid_crowdings").find();
			for (Document doc : documents) {
				GridCrowding cell_crowd;
				cell_crowd = mapper.readValue(doc.toJson(), GridCrowding.class);
				for (UncertainValue value : cell_crowd.getCrowdings()) {
					if (value.getMean().compareTo(max) > 0) {
						max = value.getMean();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return max;
	}

	public void resetGridCrowdings(String hour, String minute, List<Cell> grid) {
		Document stats = db.getCollection("crowdingStats").find().first();
		double old_max = stats.getDouble("maxValue");
		boolean max_reset = false;
		int minutes = TimeUtils.toMinutes(TimeUtils.getMillis(TimeUtils.getString15MRoundTime(Integer.parseInt(hour), Integer.parseInt(minute))));
		try {
		for (Cell cell : grid) {
			Document filter = new Document("cell", cell.getId());
			GridCrowding crowding = mapper.readValue(db.getCollection("grid_crowdings").find(filter).first().toJson(), GridCrowding.class);

			for (int cont=0; cont<4; cont++) { //restore to the initial value the grid crowdings in the previous hour 
				int time_slot = TimeUtils.getTimeSlot(minutes-15*cont);
				UncertainValue current_value = crowding.getCrowdings().get(time_slot);
				if (current_value.getMean().equals(old_max)) {
					max_reset = true;
				}
				crowding.getCrowdings().set(time_slot, new UncertainValue(current_value.getInitial(), "N:"+round((current_value.getInitial()/10d),5)));
			}

			if (max_reset) {
				updateGridCrowdingStats(old_max, findMaxGridCrowding());
			}

		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}








}
