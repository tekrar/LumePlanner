package io;

import model.Cell;
import model.POI;
import model.POI2POICrowding;
import model.UncertainValue;
import org.apache.log4j.Logger;
import services.ComputeP2PCrowdings;
import services.LoadFiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 19/10/2016.
 */
public class CityData {
    private Logger logger = Logger.getLogger(RESTController.class);
    private String city;

    public Mongo dao;
    public List<POI> activities;
    public Map<String, HashMap<String, List<UncertainValue>>> travel_times ;
    public List<Cell> grid;
    public Map<String, List<UncertainValue>> grid_crowdings;
    public Map<String, Map<String, Map<Integer, Map<String, Double>>>> p2p_cell_paths;

    public CityData(String city) {
        this.city = city;
    }

    public void init() {
        dao = new Mongo(CityProp.getInstance().get(city).getDB());

        logger.info("Crowding Module initialization started");

        grid_crowdings = new LoadFiles().load(dao, city);
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
            List<POI2POICrowding> crs = p2p.run(this);
            logger.info("POI2POI Crowdings imported ("+crs.size()+")");
        }
    }


    public void retrieveGridCrowdings() {
        grid_crowdings = dao.retrieveGridCrowdings();
    }
}
