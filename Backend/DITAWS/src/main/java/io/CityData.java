package io;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import model.*;
import org.apache.log4j.Logger;
import services.ComputeDistances;
import services.GetPOIs;
import util.Occupancies;
import util.TimeUtils;
import util.TravelTime;

import java.io.IOException;
import java.util.*;

/**
 * Created by marco on 18/10/2016.
 */
public class CityData {
    private String city;
    public List<POI> activities;
    private List<POI> restaurants;
    private String last_crowding_levels;
    public Map<String, HashMap<String, List<UncertainValue>>> crowding_levels;
    public Map<String, List<Integer>> occupancies;
    public Map<String, HashMap<String, List<UncertainValue>>> travel_times;
    private Mongo dao;
    public GraphHopper hopper;
    public String data_path;
    public TreeMap<String, TreeMap<String, Double>> distances;
    private static Logger logger = Logger.getLogger(CityData.class);

    public CityData(String city) {
        this.city = city;
        activities = new ArrayList<>();
        restaurants = new ArrayList<>();
        last_crowding_levels = String.valueOf(Long.MIN_VALUE);
        crowding_levels = new HashMap<>();
        occupancies = new HashMap<>();
        travel_times = new HashMap<>();

        try {

            dao = new Mongo(CityProp.getInstance().get(city).getDB());
            hopper = new GraphHopper().forServer();


            data_path = this.getClass().getResource("/../data/"+CityProp.getInstance().get(city).getDataDir()).getPath();
            hopper.setInMemory();
            hopper.setOSMFile(data_path+"bbox.osm");
            hopper.setGraphHopperLocation(data_path+"graph");
            hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
            hopper.importOrLoad();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setPath(String path) {
        data_path = path;
        hopper.setInMemory();
        hopper.setOSMFile(data_path+"bbox.osm");
        hopper.setGraphHopperLocation(data_path+"graph");
        hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
        hopper.importOrLoad();
    }

    public void init() {

        if (!dao.checkActivities()) {
            logger.info("/../data/"+CityProp.getInstance().get(city).getDataDir());
            new GetPOIs().run(dao, this.getClass().getResource("/../data/"+CityProp.getInstance().get(city).getDataDir()).getPath(), CityProp.getInstance().get(city).getBbox());
            logger.info("POIs collected from OSM API");
        }
        activities = dao.retrieveActivities();
        restaurants = dao.retrieveRestaurants();
        logger.info("Activities retrieved from Mongodb (count "+activities.size()+")");
        logger.info("Restaurants retrieved from Mongodb (count "+restaurants.size()+")");

        if (!dao.checkDistances()) {
            new ComputeDistances().run(dao, activities);
            logger.info("Distances among activities computed");
        }
        distances = dao.retrieveDistances();
        logger.info("Look-up table for distances initialized (count "+distances.size()+")");

        if (!dao.checkTravelTimes()) {
            travel_times = new TravelTime(city).initTravelTimeFromPOIs(dao, activities);
        } else {
            travel_times = dao.retrieveTravelTimes();
        }
        logger.info("Look-up table for travel times initialized (count "+travel_times.size()+")");

        occupancies = new Occupancies().init(activities);

        logger.info("Look-up table for POIs occupancies initialized (count "+occupancies.size()+")");



        logger.info("Look-up table for congestion levels was initialized at "+last_crowding_levels);
        last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
        logger.info("Look-up table for congestion levels initialized at "+last_crowding_levels);
    }

    public void updateCongestions() {
        last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
        logger.info("Look-up table for congestion levels initialized at "+ TimeUtils.getStringTime(Long.parseLong(last_crowding_levels)));
    }

    public Integer login(User user) {
        return dao.Login(user);
    }

    public boolean signup(User user) {
        return dao.Signup(user);
    }

    public boolean insertPlan(VisitPlanAlternatives plan) {
        return dao.insertPlan(plan);
    }

    public POI retrieveClosestActivity(POI poi) {
        return dao.retrieveClosestActivity(poi);
    }

    public boolean updateUser(CrowdingFeedback fdbk, UncertainValue value) {
        return dao.updateUser(fdbk,value);
    }

    public VisitPlanAlternatives retrievePlan(String email) {
        return dao.retrievePlan(email);
    }

    public VisitPlanAlternatives updatePlan(Visit v) {
        return dao.updatePlan(v);

    }

    public boolean updateUserOv_Cr(OverallFeedback fdbk) {
        return dao.updateUserOv_Cr(fdbk);
    }


    public boolean updateUserOv_Pl(OverallFeedback fdbk) {
        return dao.updateUserOv_Pl(fdbk);
    }

    public boolean deletePlan(String email) {
        return dao.deletePlan(email);
    }

    public List<GridCrowding> retrieveGridCrowding() {
        return dao.retrieveGridCrowding();
    }

    public POI getActivity(String place_id) {
        for (POI current : activities) {
            if (current.getPlace_id().equals(place_id)) {
                return current;
            }
        }
        return null;
    }

    public double retrieveGridMaxCrowding() {
       return dao.retrieveGridMaxCrowding();
    }


    public int findMaxOccupancy() {
        int result = Integer.MIN_VALUE;

        for (String poi : occupancies.keySet()) {
            for (Integer value : occupancies.get(poi)) {
                if (value > result) {
                    result = value;
                }
            }
        }

        if (result<1000) {
            result = 1000;
        }

        return result;
    }


    public void increaseOccupancies(String to, String arr_t, String dep_t) {
        occupancies = new Occupancies().increase(to, arr_t, dep_t, occupancies);
    }

    public void decreaseOccupancies(String to, String arr_t, String dep_t) {
        occupancies = new Occupancies().decrease(to, arr_t, dep_t, occupancies);

    }

    public POI retrieveActivity(String next_id) {
        return dao.retrieveActivity(next_id);
    }
}
