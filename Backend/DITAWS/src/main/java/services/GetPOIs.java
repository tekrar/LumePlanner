package services;

import io.Mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.RESTController;
import model.POI;
import model.UncertainValue;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetPOIs  {

	private Properties p;
	
	public static void main(String[] args) throws Exception  {
		GetPOIs g = new GetPOIs();
		String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\modena\\";




		Mongo dao = new Mongo("lume-modena", "dita","mames1976");


		g.run(dao, dir);
	}

	static String [] POICategories = {"attractions", "monuments", "museums", "eating", "parks", "resting", "historical_sites", "religious_sites"}; //"lifestyle"

	private Logger logger = Logger.getLogger(RESTController.class);
	public void run(Mongo dao, String dataPath) {

		
		Map<String, Integer> ratings = new HashMap<>();//new SocialPulse().loadStars();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		BufferedReader file_br; //category file
		BufferedReader url_br; //url API

		try {
			p = new Properties();
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));

			for (int i=0; i<POICategories.length;i++)  {
				File file = new File(dataPath+"nominatim/"+POICategories[i]);

				logger.info("=======> "+file.getAbsolutePath());

				file_br = new BufferedReader(new FileReader(file));
				String rowCategory = "";
				while ((rowCategory = file_br.readLine()) != null)  {

					logger.info("=======================> "+rowCategory);

					String typeCategory = rowCategory.split(",")[0]; //[0] contains the type of category

					double visiting_time = 0d;


					if (!POICategories[i].equals("resting"))
						visiting_time = Double.parseDouble(rowCategory.split(",")[1]); //[1] contains the visiting time (not for hotels)

					//osm API call to get POIs of a certain category type within the bbox
					URL url = new URL(p.getProperty("osm.nominatim")+"search?q="+typeCategory+"&format=json&viewbox="+p.getProperty("bbox")+"&bounded=1&limit=1000");
					logger.info(url.toString());
					url_br = new BufferedReader(new InputStreamReader(url.openStream()));

					//parsing the POIs in the json returned by the API call
					JSONArray parsed = new JSONArray(url_br.readLine());

					//loop over the POIs retrieved
					for (int j=0;j<parsed.length();j++) {
						JSONObject currentJPOI = (JSONObject) parsed.get(j);
						POI currentPOI = new POI();
						currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
						currentPOI.setCategory(POICategories[i]);
						if (!POICategories[i].equals("resting")) {
							currentPOI.setVisiting_time(new UncertainValue(visiting_time, "N:"+(visiting_time/10)));
							if (ratings.containsKey(currentPOI.getPlace_id())) {
								currentPOI.setRating(ratings.get(currentPOI.getPlace_id()));
							}
							//to add: opening times and days
						}
						if (POICategories[i].equals("eating")) {
							dao.insertRestaurant(currentPOI);
						} else {
							logger.info("**** "+currentPOI.toString());
							dao.insertActivity(currentPOI);
						}
					}
					url_br.close();
				}
				file_br.close();

			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}