package model;

public class CrowdingFeedback {

	private String user;
	private POI departure;
	private POI arrival;
	private String departure_time;
	private int choice;
	
	public CrowdingFeedback() {}

	public CrowdingFeedback(String user, POI departure, POI arrival, String departure_time, int value) {
		this.setUser(user);
		this.setDeparture(departure);
		this.setArrival(arrival);
		this.setDeparture_time(departure_time);
		this.setChoice(value);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public POI getDeparture() {
		return departure;
	}

	public void setDeparture(POI departure) {
		this.departure = departure;
	}

	public POI getArrival() {
		return arrival;
	}

	public void setArrival(POI arrival) {
		this.arrival = arrival;
	}

	public String getDeparture_time() {
		return departure_time;
	}

	public void setDeparture_time(String departure_time) {
		this.departure_time = departure_time;
	}

	public int getChoice() {
		return choice;
	}

	public void setChoice(int value) {
		this.choice = value;
	}
	
	
	
}
