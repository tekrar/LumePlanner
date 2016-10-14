package model;


import java.util.List;

import org.geojson.LineString;
import org.geojson.LngLatAlt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Path {
	
	private LineString points;
	
	private double length;
	
	public Path() {
		points = new LineString();
		length = 0d;
	}

	public LineString getPoints() {
		return points;
	}
	
	@Override
	public String toString() {
		return "Path [points=" + points.toString() + ", length=" + length + "]";
	}
	
	public void addPoints(LineString points) {
		for (LngLatAlt point : points.getCoordinates()) {
			this.points.add(point);
		}
	}
	
	public void incrementLength(double length) {
		this.length += length;
	}

	public void setPoints(LineString points) {
		this.points = points;
	}


	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
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
