package io;

import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import model.Activity;
import model.Cell;
import model.CrowdingFeedback;
import model.CrowdingStats;
import model.Distance;
import model.DistanceTo;
import model.GridCrowding;
import model.OverallFeedback;
import model.POI;
import model.POI2POICrowding;
import model.Timing;
import model.TimingTo;
import model.UncertainValue;
import model.User;
import model.Visit;
import model.VisitPlan;
import model.VisitPlanAlternatives;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.geojson.Point;

import services.ComputeDistances;
import util.PointCodec;
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
import com.mongodb.client.result.DeleteResult;


public class Mongo {

    private static final String MONGO_URL = "127.0.0.1:27017";
	private static final String MONGO_USER = "dita";
    private static final String MONGO_PASSWORD = "mames1976";

	private Logger logger = Logger.getLogger(Mongo.class);

	private ObjectMapper mapper;

	private MongoClient mongoClient;

	private MongoDatabase db;

	private java.util.logging.Logger mongoLogger;

	private String nameDB;

	public Mongo (String nameDB) {

		this.nameDB = nameDB;

		CodecRegistry codecRegistry =
				CodecRegistries.fromRegistries(
						CodecRegistries.fromCodecs(new PointCodec(), new UncertainValueCodec()),
						MongoClient.getDefaultCodecRegistry());


		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		mongoLogger = java.util.logging.Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
        mongoClient = new MongoClient(new ServerAddress(MONGO_URL), Arrays.asList(
							MongoCredential.createCredential(
                                    MONGO_USER,
									nameDB,
                                    MONGO_PASSWORD.toCharArray())),
									MongoClientOptions.builder().codecRegistry(codecRegistry).build());
        db = mongoClient.getDatabase(nameDB);
        logger.info("loading new db "+nameDB+" .....");
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


	public void insertActivity(POI poi) {
		try {
			if (db.getCollection("activities").find(new Document("place_id", poi.getPlace_id())).first() == null)
				db.getCollection("activities").insertOne(Document.parse(poi.toJSONString()));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

	public void insertRestaurant(POI poi) {
		try {
			if (db.getCollection("restaurants").find(new Document("place_id", poi.getPlace_id())).first() == null)
				db.getCollection("restaurants").insertOne(Document.parse(poi.toJSONString()));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}


	public void insertDistances(List<Distance> distances) {
		if (db.getCollection("distances").count() == 0l ) {
			List<Document> distanceDocuments = new ArrayList<Document>();
			for (int i =0; i< distances.size(); i++) {
				distanceDocuments.add(Document.parse(distances.get(i).toJSONString()));
			}
			logger.info("---------> "+distanceDocuments.size());
			db.getCollection("distances").insertMany(distanceDocuments);
		}
	}

	public void insertDistancesAsPOIUpdate(List<Distance> distances) {
		try {
			for (Distance d : distances) {
				db.getCollection("POIs").updateOne(
						db.getCollection("POIs").find(new Document("place_id", d.getFrom())).first(), 
						new Document("$set", new Document("distances", mapper.writeValueAsString(d.getDistances()))));
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}



	public void insertTimings(List<Timing> timings) {
		if (db.getCollection("timings").count() == 0l ) {
			try{
				for (int i =0; i< timings.size(); i++) {
					db.getCollection("timings").insertOne(Document.parse(timings.get(i).toJSONString()));
					//the insertMany takes too much memory: java heap memory error
				}
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}

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


	public List<POI> retrieveRestaurants() {
		List<POI> result = new ArrayList<POI>();
		try {
			for (Iterator<Document> iter = db.getCollection("restaurants").find().iterator(); iter.hasNext();) {
				result.add(mapper.readValue(iter.next().toJson(), POI.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<POI> retrieveActivities() {

		logger.info("retrieveActivities from "+nameDB+" "+db.getName());

		List<POI> result = new ArrayList<POI>();
		try {
			for (Iterator<Document> iter = db.getCollection("activities").find().iterator(); iter.hasNext();) {
				POI p = mapper.readValue(iter.next().toJson(), POI.class);
				p.setDisplay_name(Normalizer.normalize(p.getDisplay_name(), Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").replaceAll("''", "'"));
				result.add(p);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean checkActivities(){
        return db.getCollection("activities").count() != 0l;
    }

	public POI retrieveActivity(String place_id) {
		try {
			//logger.info("Retrieve activity:"+place_id);
			return mapper.readValue(db.getCollection("activities").find(new Document("place_id", place_id)).iterator().next().toJson(), POI.class);
		} catch(Exception e) {
			logger.info(e.getMessage());
		}
		return null;
	}

	public POI retrieveClosestActivity(POI custom) {
		POI result = new POI();
		try {
			result = mapper.readValue(db.getCollection("activities").find(new Document("geometry", new Document("$near", 
					new Document("$geometry", custom.getGeometry())))).first().toJson(), POI.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<POI> retrieveClosestRestaurants(Point location) {
		List<POI> result = new ArrayList<>();
		try {
			FindIterable<Document> top15 = db.getCollection("activities").find(new Document("geometry", new Document("$near", 
					new Document("$geometry", location)))).limit(15);
			for (Document document : top15) {
				result.add(mapper.readValue(document.toJson(), POI.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean checkDistances(){
        return db.getCollection("distances").count() != 0l;
    }

	public TreeMap<String, TreeMap<String, Double>> retrieveDistances() {
		TreeMap<String, TreeMap<String, Double>> result = new TreeMap<>();
		try {
			for (Iterator<Document> iter = db.getCollection("distances").find().sort(new Document("from", 1)).iterator(); iter.hasNext();) {
				Distance distance = mapper.readValue(iter.next().toJson(), Distance.class);

				TreeMap<String, Double> tos = new TreeMap<String, Double>();
				for (DistanceTo current : distance.getDistances()) {
					tos.put(current.getTo(), current.getDistance());
				}
				result.put(distance.getFrom(), tos);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

    /*
	public TreeMap<String, TreeMap<String, Double>> retrieveDistances(POI startPlace, POI endPlace, List<String> POIsID) throws IOException {
		POI closest_to_end = null;
		if (endPlace.getPlace_id().equals("00")) {
			closest_to_end = retrieveClosestActivity(endPlace);
			POIsID.add(closest_to_end.getPlace_id());
		}
		Document inClause = new Document("from", new Document("$in", mapper.readValue(mapper.writeValueAsString(POIsID), List.class)));
		logger.info("distance filter:"+mapper.readValue(mapper.writeValueAsString(POIsID), List.class));
		//Document inClause = new Document("from", new Document("$in", mapper.writeValueAsString(POIsID)));
		TreeMap<String, TreeMap<String, Double>> result = new TreeMap<String, TreeMap<String, Double>>(); //<From, <To, Distance>>

		try {

			for (Iterator<Document> iter = db.getCollection("distances").find(inClause).sort(new Document("from", 1)).iterator(); iter.hasNext();) {
				Distance distance = mapper.readValue(iter.next().toJson(), Distance.class);
				TreeMap<String, Double> tos = new TreeMap<String, Double>();
				for (DistanceTo current : distance.getDistances()) {
					if (null != closest_to_end && closest_to_end.getPlace_id().equals(current.getTo())) {
						tos.put("00", current.getDistance());
					} else {
						tos.put(current.getTo(), current.getDistance());
					}
				}
				if (null != closest_to_end && closest_to_end.getPlace_id().equals(distance.getFrom())) {
					result.put("00", tos);
				} else {
					result.put(distance.getFrom(), tos);
				}
			}

			if (startPlace.getPlace_id().equals("0")) {
				Distance d = new ComputeDistances().runOnetoMany(this, startPlace, endPlace, POIsID);
				TreeMap<String, Double> tos = new TreeMap<String, Double>();
				for (Iterator<DistanceTo> iterTos = d.getDistances().iterator();iterTos.hasNext();) {
					DistanceTo current = iterTos.next();
					tos.put(current.getTo(), current.getDistance());
				}
				result.put(d.getFrom(), tos);
			}

			logger.info("distances size:"+result.size());
		} catch(Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
    */

	public String retrieveCrowdingLevels(Map<String, HashMap<String, List<UncertainValue>>> crowding_levels, String last_crowding_levels) {
		int update_size=0;
		String result = last_crowding_levels;
		try {
			FindIterable<Document> crowdings = db.getCollection("crowdings").find(new Document("timestamp", new Document("$gt", last_crowding_levels)));
			for (Iterator<Document> iter = crowdings.iterator(); iter.hasNext();) {
				update_size++;
				POI2POICrowding crowding = mapper.readValue(iter.next().toJson(), POI2POICrowding.class);
				if (!crowding_levels.containsKey(crowding.getFrom_id())) {
					crowding_levels.put(crowding.getFrom_id(), new HashMap<String, List<UncertainValue>>());
				}
				crowding_levels.get(crowding.getFrom_id()).put(crowding.getTo_id(), crowding.getCrowdings());
				if (Long.parseLong(crowding.getTimestamp()) > Long.parseLong(result)) {
					result = crowding.getTimestamp();
				}
			}
			logger.info(update_size+" crowdings updated");
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return ""+Long.MIN_VALUE;
		}
		
	}

	public boolean checkTravelTimes(){
        return db.getCollection("timings").count() != 0l;
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



	/*
	 * *************************************** REMOVE ***************************************
	 */


	public void testRemoval() {
		MongoCollection<Document> collection = db.getCollection("testCollection");
		collection.deleteOne(new Document("key1", "value1"));
		collection.deleteMany(new Document());
	}




	/*
	 * *************************************** USER-SERVICES ***************************************
	 */


	public boolean Signup(User user) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(user.getEmail().getBytes()).toString())).first();
		if (null == userRecord) {
			logger.info("Creating new user account for "+user.getEmail());
			db.getCollection("users").insertOne(Document.parse(user.toJSONString()));
			return true;
		} else {
			logger.info("User "+user.getEmail()+" already exists");
			return false;
		}
	}

	public Integer Login(User user) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(user.getEmail().getBytes()).toString())).first();
		if (null != userRecord) {
			if (userRecord.get("password").equals(user.getPassword())) {
				logger.info("User "+user.getEmail()+" successfully logged in");
				if (null == db.getCollection("plans").find(new Document("crowd_related.user", user.getEmail())).first()) {
					return 1;
				} else {
					return 2;
				}

			} else {
				logger.info("Wrong password for "+user.getEmail());
				return 0; //wrong password
			}
		}
		logger.info("User "+user.getEmail()+" not found");
		return -1; //user not found
	}


	public boolean insertPlan(VisitPlanAlternatives plans) {
		VisitPlan plan_accepted = plans.getCrowd_related();
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd_related.user", plan_accepted.getUser())).first();
			if (null == userPlanRecord) {
				logger.info("Creating new visit plan for user "+plan_accepted.getUser());
				db.getCollection("plans").insertOne(Document.parse(plans.toJSONString()));
				return true;
			} else {
				logger.info("Updating visit plan for user "+plan_accepted.getUser());
				db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(plans.toJSONString()));
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public VisitPlanAlternatives retrievePlan(String user_email) {
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd_related.user", user_email)).first();
			if (null == userPlanRecord) {
				return null;
			} else {
				logger.info("Getting visit plan for user "+user_email);
				return mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	public boolean updateUser(CrowdingFeedback fdbk, UncertainValue value) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(fdbk.getUser().getBytes()).toString())).first();
		try {
			User user = mapper.readValue(userRecord.toJson(), User.class);
			switch(fdbk.getChoice()) {
			case 0: {
				if (user.getLow_crowding().getMean() == 0d || user.getLow_crowding().getMean() < value.getMean()) {
					user.setLow_crowding(value); //store the higher
				}
				logger.info("User "+user.getEmail()+" reported "+value.getMean()+" as a low crowding");
				break;
			}
			case 1: {
				if (user.getLowAvg_crowding().getMean() == 0d || user.getLowAvg_crowding().getMean() < value.getMean()) {
					user.setLowAvg_crowding(value); //store the higher
				}
				logger.info("User "+user.getEmail()+" reported "+value.getMean()+" as an average crowding");
				break;
			}
			case 2: {
				if (user.getAvgHig_crowding().getMean() == 0d || user.getAvgHig_crowding().getMean() < value.getMean()) {
					user.setAvgHig_crowding(value); //store the higher
				}
				logger.info("User "+user.getEmail()+" reported "+value.getMean()+" as an average crowding");
				break;
			}
			case 3: {
				if (user.getHig_crowding().getMean() == 0d || user.getHig_crowding().getMean() > value.getMean()) {
					user.setHig_crowding(value); //store the lower
				}
				logger.info("User "+user.getEmail()+" reported "+value.getMean()+" as an high crowding");
				break;
			}
			default: break;
			}
			db.getCollection("users").findOneAndReplace(userRecord, Document.parse(user.toJSONString()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean updateUserOv_Cr(OverallFeedback fdbk) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(fdbk.getUser().getBytes()).toString())).first();
		try {
			User user = mapper.readValue(userRecord.toJson(), User.class);

			user.setOverall_crowding(new UncertainValue(fdbk.getCrowding(), "N:"+(fdbk.getCrowding()/10d)));

			if (fdbk.getChoice() == 0) {
				user.setLiked_crowding(true);
			} else {
				user.setLiked_crowding(false);
			}

			db.getCollection("users").findOneAndReplace(userRecord, Document.parse(user.toJSONString()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean updateUserOv_Pl(OverallFeedback fdbk) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(fdbk.getUser().getBytes()).toString())).first();
		try {
			User user = mapper.readValue(userRecord.toJson(), User.class);
			if (fdbk.getChoice() == 0) {
				user.setLiked_plan(true);
			} else {
				user.setLiked_plan(false);
			}

			db.getCollection("users").findOneAndReplace(userRecord, Document.parse(user.toJSONString()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public VisitPlanAlternatives updatePlan(Visit new_visited) {
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd_related.user", new_visited.getUser())).first();
			if (null != userPlanRecord) {
				VisitPlanAlternatives current = mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);

				/***** UPDATE SHORTEST PATH *****/
				VisitPlan plan = current.getShortest();
				Activity to_swap = null;
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Short check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on shortest: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}
				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);

				//logger.info("short swap:"+to_swap);
				/***** UPDATE GREEDY *****/
				to_swap = null;
				plan = current.getGreedy();
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Greedy check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on greedy: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}

				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);
				//logger.info("greedy swap:"+to_swap);
				/***** UPDATE LESS CROWDED *****/

				plan = current.getCrowd_related();
				to_swap = null;
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Crowd check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on crow_related: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}


				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);
				db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(current.toJSONString()));

				//logger.info("crowd swap:"+to_swap);
				return current;
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		logger.info("user plan not found or exception");

		return null;
	}


	public boolean deletePlan(String user_mail) {
		try{
			DeleteResult userPlanRecord = db.getCollection("plans").deleteOne(new Document("crowd_related.user", user_mail));
			if (0 == userPlanRecord.getDeletedCount()) {
				logger.info("Visiting Plan not found for removal "+user_mail);
				return false;
			} else {
				logger.info("Deleting Visiting Plan for user "+user_mail);
				return true;
			}
		} catch (Exception e) {
			logger.info("Deleting Visiting Plan for user "+user_mail+" thrown an exception: "+e.getMessage());
			return false;
		}
	}


	public List<GridCrowding> retrieveGridCrowding() {
		List<GridCrowding> result = new ArrayList<>();
		try {
			FindIterable<Document> documents = db.getCollection("grid_crowdings").find();
			for (Document doc : documents) {
				result.add(mapper.readValue(doc.toJson(), GridCrowding.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public double retrieveGridMaxCrowding() {
		double result = 1d;
		CrowdingStats cs = null;
		try {
			if (db.getCollection("crowdingStats").count() > 0l ) {
				cs = mapper.readValue(db.getCollection("crowdingStats").find().first().toJson(), CrowdingStats.class);
				result = cs.getMaxValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}



}
