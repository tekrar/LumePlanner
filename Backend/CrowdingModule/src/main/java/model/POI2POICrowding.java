package model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class POI2POICrowding {
	
	private String					timestamp;
	private String 					from_id;
	private String 					to_id;
	private List<UncertainValue> 	crowdings;

	public POI2POICrowding() {
		// TODO Auto-generated constructor stub
	}
	
	

	public POI2POICrowding(String from_id, String to_id, List<UncertainValue> crowdings) {
		super();
		this.setTimestamp(new Date().getTime());
		this.from_id = from_id;
		this.to_id = to_id;
		this.crowdings = crowdings;
	}


	public String getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(long timestamp) {
		this.timestamp = ""+timestamp;
	}



	public String getFrom_id() {
		return from_id;
	}

	public void setFrom_id(String from_id) {
		this.from_id = from_id;
	}

	public String getTo_id() {
		return to_id;
	}

	public void setTo_id(String to_id) {
		this.to_id = to_id;
	}

	public List<UncertainValue> getCrowdings() {
		return crowdings;
	}

	public void setCrowdings(List<UncertainValue> crowdings) {
		this.crowdings = crowdings;
	}
	
	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}
	

}
