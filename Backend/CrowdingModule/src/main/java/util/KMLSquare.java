package util;


import java.util.ArrayList;
import java.util.List;

public class KMLSquare {
	
	public List<String> STYLES;
	
	
	public KMLSquare() {
		STYLES = new ArrayList<String>();
	}
	
	
	public String draw(double[][] ll, String name, String color_in, String color_border, String description) {
		
		
		StringBuffer sb = new StringBuffer();
		
		if(!STYLES.contains(color_in+color_border)) {
			STYLES.add(color_in+color_border);
			sb.append("<Style id=\""+color_in+color_border+"\">\n");
			sb.append("<LineStyle>\n");
			sb.append("<width>2</width>");
			sb.append("<color>"+color_border+"</color>\n");
			sb.append("</LineStyle>\n");
			sb.append("<PolyStyle>");
			sb.append("<color>"+color_in+"</color>\n");
			sb.append("</PolyStyle>\n");
			sb.append("</Style>\n");
		}
		
		
		
		sb.append("<Placemark>\n");
		sb.append("<name>"+name+"</name>\n");
		sb.append("<description><![CDATA[\n");
		sb.append(description+"\n");
		sb.append("]]></description>\n");
		sb.append("<styleUrl>#"+color_in+color_border+"</styleUrl>\n");
		
		sb.append("<Polygon>\n");
		sb.append("<tessellate>1</tessellate>\n");
		sb.append("<outerBoundaryIs>\n");
		sb.append("<LinearRing>\n");
		sb.append("<coordinates>\n");
		for(int k=0; k<ll.length;k++)
			sb.append(ll[k][0]+","+ll[k][1]+",0\n");  
		sb.append("</coordinates>\n");
		sb.append("</LinearRing>\n");
		sb.append("</outerBoundaryIs>\n");
		sb.append("</Polygon>\n");
		sb.append("</Placemark>\n");
		
		
		return sb.toString();
	}

}
