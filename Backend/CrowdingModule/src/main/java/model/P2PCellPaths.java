package model;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P2PCellPaths {
	
	private String from;
	private String to;
	private Map<Integer, Map<String, Double>> cell_distance;
	
	public P2PCellPaths() {}

	public P2PCellPaths(String from, String to, Map<Integer, Map<String, Double>> cell_distance) {
		this.from = from;
		this.to = to;
		this.cell_distance = cell_distance;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	
	
	public Map<Integer, Map<String, Double>> getCell_distance() {
		return cell_distance;
	}

	public void setCell_distance(Map<Integer, Map<String, Double>> cell_distance) {
		this.cell_distance = cell_distance;
	}

	public String toJSONString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
