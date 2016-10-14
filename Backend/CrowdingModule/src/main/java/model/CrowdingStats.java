package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrowdingStats {
	
	private double minValue;
	private double maxValue;
	private double avgLowValue;
	private double avgLowAvgValue;
	private double avgAvgHigValue;
	private double avgHigValue;
	
	public CrowdingStats(double minValue, double maxValue) {
		this.setMinValue(minValue);
		this.setMaxValue(maxValue);
		this.setAvgLowValue(0d);
		this.setAvgLowAvgValue(0d);
		this.setAvgAvgHigValue(0d);
		this.setAvgHigValue(0d);
	}
	
	public CrowdingStats() {}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getAvgLowValue() {
		return avgLowValue;
	}

	public void setAvgLowValue(double avgLowValue) {
		this.avgLowValue = avgLowValue;
	}

	public double getAvgLowAvgValue() {
		return avgLowAvgValue;
	}

	public void setAvgLowAvgValue(double avgLowAvgValue) {
		this.avgLowAvgValue = avgLowAvgValue;
	}

	public double getAvgAvgHigValue() {
		return avgAvgHigValue;
	}

	public void setAvgAvgHigValue(double avgAvgHigValue) {
		this.avgAvgHigValue = avgAvgHigValue;
	}

	public double getAvgHigValue() {
		return avgHigValue;
	}

	public void setAvgHigValue(double avgHigValue) {
		this.avgHigValue = avgHigValue;
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
