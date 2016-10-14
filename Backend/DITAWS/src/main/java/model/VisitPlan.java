package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VisitPlan {
	
	private String 			date;
	private String 			user;
	private POI 			departure;
	private POI 			arrival;
	private String 			departure_time;
	private String 			arrival_time;
	private List<Activity> 	to_visit;
	private List<Activity>	visited;
	private double			crowding;
	private Path			path;
	private int				hash;
	
	public VisitPlan(){
		this.date 		= ""+Calendar.getInstance().getTime().getTime();
		this.to_visit 	= new ArrayList<>();
		this.visited 	= new ArrayList<>();
		this.crowding 	= 0d;
	}
	
	public VisitPlan(POI departure){
		this.departure 	= departure;
		this.date 		= ""+Calendar.getInstance().getTime().getTime();
		this.to_visit 	= new ArrayList<>();
		this.visited 	= new ArrayList<>();
		this.crowding 	= 0d;
	}

	public VisitPlan(String user, POI departure, POI arrival, String departure_time, String arrival_time, List<Activity> to_visit,
			List<Activity> visited, Path path, double crowding) {

		this.setDate(""+Calendar.getInstance().getTime().getTime());
		this.setUser(user);
		this.setDeparture(departure);
		this.setArrival(arrival);
		this.setDeparture_time(departure_time);
		this.setArrival_time(arrival_time);
		this.setTo_visit(to_visit);
		this.setVisited(visited);
		this.setPath(path);
		this.setCrowding(crowding);
		this.computeHash();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getArrival_time() {
		return arrival_time;
	}

	public void setArrival_time(String arrival_time) {
		this.arrival_time = arrival_time;
	}

	public List<Activity> getTo_visit() {
		return to_visit;
	}

	public void setTo_visit(List<Activity> to_visit) {
		this.to_visit = to_visit;
	}

	public List<Activity> getVisited() {
		return visited;
	}

	public void setVisited(List<Activity> visited) {
		this.visited = visited;
	}
	
	public double getCrowding() {
		return crowding;
	}

	public void setCrowding(double crowding) {
		this.crowding = crowding;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public int getHash() {
		return hash;
	}

	public void computeHash() {
		int hash = 7;
		final int prime = 31;
		String concatenated = "";
		for (Activity a : visited) {
			if (a == null) throw new Error();
			concatenated += a.getVisit().getPlace_id();
		}
		for (Activity a : to_visit) {
			concatenated += a.getVisit().getPlace_id();
		}
		for (int i=0; i<concatenated.length(); i++) {
			hash ^= prime+concatenated.charAt(i);
		}
		this.hash = hash;
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

	@Override
	public String toString() {
		return "[departure=" + departure+ ";departure_time=" + departure_time + ";arrival=" + arrival + ";arrival_time=" + arrival_time + ";to_visit=" + Arrays.toString(to_visit.toArray())
				+ ";path=" + path.toString() + ";crowding=" + crowding + "]";
	}
	
	

}
