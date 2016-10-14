package simulation;

import java.util.List;

public class UserRequest implements Comparable<UserRequest> {
	private String 			id;
	private int 			type; //0=crowd-rel, 1=shortest, 2=greedy
	private double 			crowd_preference; //1=FullyEmpty, 0.5=MainlyEmpty, -0.5=MainlyCrowded, -1=FullyCrowded
	private String 			departure;
	private int 			departure_time;
	private List<String>	pois;
	
	public UserRequest(String id, int type, double crowd_preference, String departure, int departure_time, List<String> pois) {
		this.setId(id);
		this.setType(type);
		this.setCrowd_preference(crowd_preference);
		this.setDeparture(departure);
		this.setDeparture_time(departure_time);
		this.setPois(pois);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getCrowd_preference() {
		return crowd_preference;
	}

	public void setCrowd_preference(double crowd_preference) {
		this.crowd_preference = crowd_preference;
	}

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public int getDeparture_time() {
		return departure_time;
	}

	public void setDeparture_time(int departure_time) {
		this.departure_time = departure_time;
	}

	public List<String> getPois() {
		return pois;
	}

	public void setPois(List<String> pois) {
		this.pois = pois;
	}

	@Override
	public int compareTo(UserRequest o) {
		return this.id.compareTo(o.getId());
	}
}
