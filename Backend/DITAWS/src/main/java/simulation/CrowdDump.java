package simulation;


public class CrowdDump extends Event {
	private long time;
	private String id;

	public CrowdDump(String id, long time) {
		this.time = time;
		this.id = id;
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		return time;
	}
	
	public String getId() {
		return id;
	}

}