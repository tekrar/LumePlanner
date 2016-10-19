package util;

import java.io.IOException;
import java.util.Properties;

import io.CityProp;
import org.geojson.LngLatAlt;

public class PointWithinBBox {
	
	private String city;
	
	public PointWithinBBox(String city) {
		this.city = city;
	}
	
	public boolean check(LngLatAlt coords) {
		
		double lat = coords.getLatitude();
		double lon = coords.getLongitude();

		String tl[] = CityProp.getInstance().get(city).getTL().split(", ");
		String tr[] = CityProp.getInstance().get(city).getTR().split(", ");
		String br[] = CityProp.getInstance().get(city).getBR().split(", ");
		String bl[] = CityProp.getInstance().get(city).getBL().split(", ");
		
		double bbox_min_lng = (Double.parseDouble(tl[0]) < Double.parseDouble(bl[0])) ? Double.parseDouble(tl[0]) : Double.parseDouble(bl[0]);
		double bbox_max_lng = (Double.parseDouble(tr[0]) > Double.parseDouble(br[0])) ? Double.parseDouble(tr[0]) : Double.parseDouble(br[0]);
		double bbox_min_lat = (Double.parseDouble(bl[1]) < Double.parseDouble(br[1])) ? Double.parseDouble(bl[1]) : Double.parseDouble(br[1]);
		double bbox_max_lat = (Double.parseDouble(tl[1]) > Double.parseDouble(tr[1])) ? Double.parseDouble(tl[1]) : Double.parseDouble(tr[1]);
		
		return (lat >= bbox_min_lat && lat <= bbox_max_lat && lon >= bbox_min_lng && lon <= bbox_max_lng);
	}

}
