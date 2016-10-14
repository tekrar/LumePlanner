package model;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Timing {
	private String from;
	private List<TimingTo> times;
	
	public Timing() {}

	public Timing(String from, List<TimingTo> times) {
		super();
		this.from = from;
		this.times = times;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<TimingTo> getTimes() {
		return times;
	}

	public void setTimes(List<TimingTo> times) {
		this.times = times;
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
