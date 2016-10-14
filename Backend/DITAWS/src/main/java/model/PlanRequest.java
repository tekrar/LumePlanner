package model;

import java.util.List;

public class PlanRequest {
	
	private String			user;
	private double			crowd_preference;
	private POI 			start_place;
	private POI 			end_place;
	private String 			start_time;
	private List<String> 	visits;
	
	public PlanRequest(){}

	public PlanRequest(String user, double crowd_preference, POI start_place,
			POI end_place, String start_time, List<String> visits) {
		this.user = user;
		this.crowd_preference = crowd_preference;
		this.start_place = start_place;
		this.end_place = end_place;
		this.start_time = start_time;
		this.visits = visits;
	}



	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public double getCrowd_preference() {
		return crowd_preference;
	}

	public void setCrowd_preference(double crowd_preference) {
		this.crowd_preference = crowd_preference;
	}

	public POI getStart_place() {
		return start_place;
	}

	public void setStart_place(POI start_place) {
		this.start_place = start_place;
	}

	public POI getEnd_place() {
		return end_place;
	}

	public void setEnd_place(POI end_place) {
		this.end_place = end_place;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public List<String> getVisits() {
		return visits;
	}

	public void setVisits(List<String> visits) {
		this.visits = visits;
	}
	
	

}
