package simulation;

import java.util.Comparator;

public abstract class Event implements Comparator<Event>, Comparable<Event> {
	public abstract long getTime();
	
	@Override
	public int compare(Event o1, Event o2) {
		return Long.compare(o1.getTime(), o2.getTime());
	}
	
	@Override
	public int compareTo(Event arg0) {
		return Long.compare(this.getTime(), arg0.getTime());
	}
}
