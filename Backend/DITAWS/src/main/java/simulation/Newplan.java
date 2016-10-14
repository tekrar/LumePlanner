package simulation;


public class Newplan extends Event {
	private long time;
	private String id;

	public Newplan(String id, long time) {
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
