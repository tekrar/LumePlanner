package simulation;


public class Replan extends Event {
	private long time;
	private String id;
	private String visit;

	public Replan(String id, long time, String visit) {
		this.time = time;
		this.id = id;
		this.visit = visit;
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		return time;
	}
	
	public String getId() {
		return id;
	}

	public String getVisit() {
		return visit;
	}
	
}
