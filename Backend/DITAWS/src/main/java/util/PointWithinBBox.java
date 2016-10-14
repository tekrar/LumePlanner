package util;

import java.io.IOException;
import java.util.Properties;

import org.geojson.LngLatAlt;

public class PointWithinBBox {
	
	private Properties p;
	
	public PointWithinBBox() {
		p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("DITA.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean check(LngLatAlt coords) {
		
		double lat = coords.getLatitude();
		double lon = coords.getLongitude();

		String tl[] = p.getProperty("bbox.tl").split(", ");
		String tr[] = p.getProperty("bbox.tr").split(", ");
		String br[] = p.getProperty("bbox.br").split(", ");
		String bl[] = p.getProperty("bbox.bl").split(", ");
		
		double bbox_min_lng = (Double.parseDouble(tl[0]) < Double.parseDouble(bl[0])) ? Double.parseDouble(tl[0]) : Double.parseDouble(bl[0]);
		double bbox_max_lng = (Double.parseDouble(tr[0]) > Double.parseDouble(br[0])) ? Double.parseDouble(tr[0]) : Double.parseDouble(br[0]);
		double bbox_min_lat = (Double.parseDouble(bl[1]) < Double.parseDouble(br[1])) ? Double.parseDouble(bl[1]) : Double.parseDouble(br[1]);
		double bbox_max_lat = (Double.parseDouble(tl[1]) > Double.parseDouble(tr[1])) ? Double.parseDouble(tl[1]) : Double.parseDouble(tr[1]);
		
		return (lat >= bbox_min_lat && lat <= bbox_max_lat && lon >= bbox_min_lng && lon <= bbox_max_lng);
	}

}
