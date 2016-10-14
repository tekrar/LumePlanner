package util;

public class TimeUtils {

	public static void main (String args[]) {
		System.out.println(getString15MRoundTime(1, 7));	
	}

//	public static int getHour(int time) { //time in minutes
//		return (time/60>=24) ? 
//				(time%60>30) ? 
//						time/60-24+1 : time/60-24
//						: (time/60==23) ? 
//								(time%60>30) ? 
//										0 : time/60 
//										: (time%60>30) ? 
//												time/60+1 : time/60;
//		//return (time % 60 > 30 ) ? (((int)(time/60)==23) ? 0 : (time/60+1)) : (time/60);
//	}


	public static int getTimeSlot(int time) { //time in minutes
		return (time/15>=96) ? 
				(time%15>7) ? 
						time/15-96+1 : time/15-96
						: (time/15==95) ? 
								(time%15>7) ? 
										0 : time/15 
										: (time%15>7) ? 
												time/15+1 : time/15;
	}



	public static String getStringTime(int time) { //time in minutes
		return ( (time/60>9) ?
				(((time/60)>=24) ? 
						("0"+(time/60-24)):(time/60)) 
						:"0"+(time/60) ) + ":" + ( (time%60>9)?(time%60):"0"+(time%60) );
	}


	public static String getStringTime(long millis) { //time in millis
		//millis to minutes
		int time = (int)millis/1000/60;
		return ( (time/60>9) ?
				(((time/60)>=24) ? 
						("0"+(time/60-24)):(time/60)) 
						:"0"+(time/60) ) + ":" + ( (time%60>9)?(time%60):"0"+(time%60) );
	}


	public static String getString15MRoundTime(int hh, int mm) {

		int time = hh*60+mm; //time in minutes

		if (time%15 > 7) {
			time = time + 15-time%15;
		} else {
			time = time - time%15;
		}

		return getStringTime(time);
	}

	public static Long getMillis(String string_time) {

		String[] values = string_time.split(":");

		long time = 60*1000*(Integer.parseInt(values[0])*60+Integer.parseInt(values[1])); //time in millis

		return time;
	}

	public static int toMinutes(long from_millis) {

		return (int)from_millis/1000/60;
	}
	
	
}
