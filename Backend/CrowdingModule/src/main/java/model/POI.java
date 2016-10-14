package model;

import org.geojson.Point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class POI implements Comparable<POI> {

	private String 	place_id;
	private Point 	geometry;
	private String 	display_name;
	private String 	category;
	private String 	type;
	private float 	importance;
	private String 	icon;
	
	//extra fields
	private UncertainValue visiting_time;
	private String opening_hours; //format: [hh:mm-hh:mm] comma separated strings
	private String opening_days; //format: [wd] comma separated int (form 1=sunday to 7=saturday)
	private int rating;

	public POI(String place_id, double lat, double lon, String display_name, String category, String type,
			float importance, String icon, UncertainValue visiting_time, String opening_hours, String opening_days, int rating) {
		super();
		this.place_id 				= place_id;
		this.geometry 				= new Point(lon, lat);
		this.display_name			= display_name;
		this.category 				= category;
		this.type 					= type;
		this.importance 			= importance;
		this.icon 					= icon;
		this.visiting_time			= visiting_time;
		this.opening_hours 			= opening_hours;
		this.opening_days 			= opening_days;
		this.rating 				= rating;

	}
	
	public POI(String place_id, Point geometry, String display_name, String category, String type,
			float importance, String icon, UncertainValue visiting_time, String opening_hours, String opening_days, int rating) {
		super();
		this.place_id 				= place_id;
		this.geometry 				= geometry;
		this.display_name			= display_name;
		this.category 				= category;
		this.type 					= type;
		this.importance 			= importance;
		this.icon 					= icon;
		this.visiting_time			= visiting_time;
		this.opening_hours 			= opening_hours;
		this.opening_days 			= opening_days;
		this.rating					= rating;
	}
	
	public POI(String place_id) {
		this.place_id 		= place_id;
		this.visiting_time 	= new UncertainValue(0d, "N:0");
		this.geometry 		= new Point(0d, 0d);
		this.importance		= 0f;
		this.rating			= 3;
	}
	
	public POI(String place_id, double lat, double lng, String display_name) {
		this.place_id 		= place_id;
		this.visiting_time 	= new UncertainValue(0d, "N:0");
		this.geometry 		= new Point(lng, lat);
		this.display_name	= display_name;
		this.importance		= 0f;
		this.rating			= 3;
	}
	
	public POI() {
		this.visiting_time	= new UncertainValue(0d, "N:0");
		this.geometry 		= new Point(0d, 0d);
		this.importance		= 0f;
		this.rating			= 3;
	}

	public String getPlace_id() {
		return place_id;
	}

	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}
	
	
	public void setLonLat(double lon, double lat) {
		this.geometry = new Point(lon, lat);
	}
	
	public void setLat(double lat) {
		this.geometry.getCoordinates().setLatitude(lat);
	}
	
	public void setLon(double lon) {
		this.geometry.getCoordinates().setLongitude(lon);
	}
	

	public Point getGeometry() {
		return geometry;
	}

	public void setGeometry(Point geometry) {
		this.geometry = geometry;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getImportance() {
		return importance;
	}

	public void setImportance(float importance) {
		this.importance = importance;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
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
	public int compareTo(POI arg0) {
		return place_id.compareTo(arg0.place_id);
	}

	public String getOpening_hours() {
		return opening_hours;
	}

	public void setOpening_hours(String opening_hours) {
		this.opening_hours = opening_hours;
	}

	public String getOpening_days() {
		return opening_days;
	}

	public void setOpening_days(String opening_days) {
		this.opening_days = opening_days;
	}

	public UncertainValue getVisiting_time() {
		return visiting_time;
	}

	public void setVisiting_time(UncertainValue visiting_time) {
		this.visiting_time = visiting_time;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((geometry == null) ? 0 : geometry.hashCode());
		result = prime * result
				+ ((place_id == null) ? 0 : place_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		POI other = (POI) obj;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (place_id == null) {
			if (other.place_id != null)
				return false;
		} else if (!place_id.equals(other.place_id))
			return false;
		return true;
	}

	
}
