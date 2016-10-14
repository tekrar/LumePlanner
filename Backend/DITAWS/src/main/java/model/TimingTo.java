package model;

import java.util.List;

public class TimingTo {
	
	private String to;
	private List<UncertainValue> time;
	
	public TimingTo(){}

	public TimingTo(String to, List<UncertainValue> time) {
		super();
		this.to = to;
		this.time = time;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public List<UncertainValue> getTime() {
		return time;
	}

	public void setTime(List<UncertainValue> time) {
		this.time = time;
	}
	
	

}
