package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import model.Cell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AggregateOccupancies {

	private Properties p;

	public static List<Cell> grid = new ArrayList<Cell>();
	
	public static Map<Long, HashMap<String, Double>> presences = new HashMap<Long, HashMap<String,Double>>();

	public static void main(String[] args) throws JsonParseException, JsonMappingException, JSONException, IOException {
		AggregateOccupancies a = new AggregateOccupancies();
		a.loadGrid();
	}


	public AggregateOccupancies() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadGrid() throws JsonParseException, JsonMappingException, JSONException, IOException {
		File gridfile = new File(this.getClass().getResource("/").getPath()+"../DITA/WEB-INF/data/"+p.getProperty("data.dir")+"grid.geojson");

		FileReader fr = new FileReader(gridfile);
		StringBuilder contents = new StringBuilder();
        char[] buffer = new char[4096];
        int read = 0;
        do {
            contents.append(buffer, 0, read);
            read = fr.read(buffer);
        } while (read >= 0);
		fr.close();
		
		JSONObject gridJson = new JSONObject(contents.toString());
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		JSONArray cellsJson = gridJson.getJSONArray("features");
		
		for (int i=0; i<cellsJson.length(); i++) {
			grid.add(mapper.readValue(cellsJson.getJSONObject(i).toString(), Cell.class));
		}
	}
}
