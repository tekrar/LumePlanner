package model;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Distance {
	
	private String 				from;
	private List<DistanceTo> 	distances;

	public Distance() {
		// TODO Auto-generated constructor stub
	}

	public Distance(String from, List<DistanceTo> distances) {
		super();
		this.from = from;
		this.distances = distances;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<DistanceTo> getDistances() {
		return distances;
	}

	public void setDistances(List<DistanceTo> distances) {
		this.distances = distances;
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
