package model;

public class Visit {
	
	private String user;
	private POI visited;
	private long time;
	
	public Visit() {}

	public Visit(String user, POI visited, long time) {
		super();
		this.setUser(user);
		this.setVisited(visited);
		this.setTime(time);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public POI getVisited() {
		return visited;
	}

	public void setVisited(POI visited) {
		this.visited = visited;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	

}
