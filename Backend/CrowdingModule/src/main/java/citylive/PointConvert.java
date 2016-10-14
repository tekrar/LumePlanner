package citylive;

import tilab.nimble.coordconvert.CoordConvert;
import tilab.nimble.coordconvert.GlobalCoordinates;



public class PointConvert {
	
	public static final double offset = 12.45233;
	
	public static double[][] WGS84toROMA40(double[][] coord){
		
		return new double[][]{WGS84toROMA40(coord[0]), WGS84toROMA40(coord[1])};		
		
	}
	
	public static double[] WGS84toROMA40(double[] coord){
		int ROMA40 = 1;
		
		CoordConvert cordconv = new CoordConvert();
		GlobalCoordinates inCoord = new GlobalCoordinates();
		inCoord.Latitude = coord[1];
		inCoord.Longitude = coord[0];
		
		if(inCoord.Longitude <0) inCoord.est = false;
		if(inCoord.Longitude >0) inCoord.est = true;
		
		GlobalCoordinates outCoord = new GlobalCoordinates();
		cordconv.ConvertFromWGS(ROMA40, inCoord, outCoord);
		
		if(!outCoord.est) outCoord.Longitude = - outCoord.Longitude;
		
		return new double[]{outCoord.Longitude+offset, outCoord.Latitude};
		
	}
	
	
	public static double[][] ROMA40toWGS84(double[][] coord){
		
		return new double[][]{ROMA40toWGS84(coord[0]), ROMA40toWGS84(coord[1])};
		
	}
	
	public static double[] ROMA40toWGS84(double[] coord){
		int WGS84 = 4; //WGS84
		
		CoordConvert cordconv = new CoordConvert();
		GlobalCoordinates inCoord = new GlobalCoordinates();
		inCoord.Latitude = coord[1];
		inCoord.Longitude = coord[0]-offset;
		
		if(inCoord.Longitude <0) inCoord.est = false;
		if(inCoord.Longitude >0) inCoord.est = true;
		
		GlobalCoordinates outCoord = new GlobalCoordinates();
		cordconv.ConvertFromRM40(WGS84, inCoord, outCoord);
		
		if(!outCoord.est) outCoord.Longitude = - outCoord.Longitude;
		
		
		return new double[]{outCoord.Longitude, outCoord.Latitude};
	}
	
	
	
	public static void main(String[] args){
		PointConvert pc = new PointConvert();
		double [][] Torino = new double[][]{{7.5778996,45.0055411}, {7.7735908,45.1402043}};
		
		double [][] transformed = pc.WGS84toROMA40(Torino);
		
		double[] single = pc.WGS84toROMA40(Torino[0]);
		
		System.out.println(transformed[0][1]+","+transformed[0][0]);
		System.out.println(single[1]+","+single[0]);
		System.out.println(transformed[1][1]+","+transformed[1][0]);
		
		System.out.println();
		double[][] ROMA40 = new double[][]{{6.2381633332,46.7360416666},{6.2381633332,46.7360416666}};
		transformed = pc.ROMA40toWGS84(ROMA40);
		single = pc.ROMA40toWGS84(ROMA40[0]);
		
		System.out.println(transformed[0][1]+","+transformed[0][0]);
		System.out.println(single[1]+","+single[0]);
		System.out.println(transformed[1][1]+","+transformed[1][0]);
		
	}

}
