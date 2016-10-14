package io;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import model.POI;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;

public class DOCIT {
	
	private Properties p;

	public DOCIT() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, HashMap<String, ArrayList<Double>>> getTravelTime(List<POI> pOIs) {
		
		Map<String, HashMap<String, ArrayList<Double>>> result = new HashMap<String, HashMap<String,ArrayList<Double>>>();
		
		try {
			URL serverURL = new URL(p.getProperty("docit.url", "http://184.173.177.188:8080/docit/api/dija"));

			JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
			mySession.getOptions().setRequestContentType("application/json");

			String method = "route";
			int requestID = 0;
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orig_time", "00:00:00");
			
			for (POI from : pOIs) {
				for (POI to : pOIs) {
					
					if (to.equals(from)) continue;
					
					params.put("orig_loc_lat", from.getGeometry().getCoordinates().getLatitude());
					params.put("orig_loc_lon", from.getGeometry().getCoordinates().getLongitude());
					params.put("dest_loc_lat", to.getGeometry().getCoordinates().getLatitude());
					params.put("dest_loc_lon", to.getGeometry().getCoordinates().getLongitude());
					
					JSONRPC2Request request = new JSONRPC2Request(method, params, requestID);

					JSONRPC2Response response = null;

					response = mySession.send(request);

					if (response.indicatesSuccess())
						System.out.println("SUCC:"+response.getResult());
					else {
						System.out.println("ERR:"+response.getError().getMessage());
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return result;
	}
}
