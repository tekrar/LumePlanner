package model;

public class OverallFeedback {

	private String city;
	private String user;
	private int choice;
	private double crowding;
	
	public OverallFeedback() {}

	public OverallFeedback(String city, String user, int value, double crowding) {
		this.city = city;
		this.setUser(user);
		this.setChoice(value);
		this.setCrowding(crowding);
	}


	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getChoice() {
		return choice;
	}

	public void setChoice(int value) {
		this.choice = value;
	}

	public double getCrowding() {
		return crowding;
	}

	public void setCrowding(double crowding) {
		this.crowding = crowding;
	}
	
	
}
