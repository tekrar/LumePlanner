package citylive;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import util.KMLSquare;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static citylive.Tables.LonLatBbox;
import static citylive.Tables.anyFile;
import static citylive.Tables.base_dir_er;
import static util.GeoJson2KML.printFooterDocument;
import static util.GeoJson2KML.printHeaderDocument;


public class CityArea {

	private String name;
	private double[][] lonLatBbox;


	private double ox;
	private double oy;
	private double xdim;
	private double ydim;
	private int mini, maxi, minj, maxj;
	private int nrows, ncols;
	
	public CityArea(String name, double[][] lonLatBbox, String anyFile) {
		this.name = name;
		this.lonLatBbox = PointConvert.WGS84toROMA40(lonLatBbox);
		
		
		// read header of one file to get information about the city area	
		try{

			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(anyFile))));
			
			int hour = Integer.parseInt(br.readLine().trim().substring("HOUR".length()+1).trim());
			int minutes = Integer.parseInt(br.readLine().trim().substring("MINUTES".length()+1).trim());
			int year = Integer.parseInt(br.readLine().trim().substring("YEAR".length()+1).trim());
			int month = Integer.parseInt(br.readLine().trim().substring("MONTH".length()+1).trim()) - 1; // JANUARY = 0 in java;
			int day = Integer.parseInt(br.readLine().trim().substring("DAY".length()+1).trim());
			String city = br.readLine().trim();
			int tot_rows = Integer.parseInt(br.readLine().trim().substring("NROWS".length()+1).trim());
			int tot_cols = Integer.parseInt(br.readLine().trim().substring("NCOLS".length()+1).trim());
			
			double ulxmap = Double.parseDouble(br.readLine().trim().substring("ULXMAP".length()+1).trim());
			double ulymap = Double.parseDouble(br.readLine().trim().substring("ULYMAP".length()+1).trim());
			xdim = Double.parseDouble(br.readLine().trim().substring("XDIM".length()+1).trim());
			ydim = Double.parseDouble(br.readLine().trim().substring("YDIM".length()+1).trim());
			
			int celle_true = Integer.parseInt(br.readLine().trim().substring("CELLE_TRUE".length()+1).trim());
			int celle_false = Integer.parseInt(br.readLine().trim().substring("CELLE_FALSE".length()+1).trim());



			minj = (int)Math.floor((lonLatBbox[0][0] - ulxmap + xdim/2)/xdim);
			maxi = (int)Math.ceil((ulymap - lonLatBbox[0][1] + ydim/2)/ydim);

			maxj = (int)Math.ceil((lonLatBbox[1][0] - ulxmap + xdim/2)/xdim);
			mini = (int)Math.floor((ulymap - lonLatBbox[1][1] + ydim/2)/ydim);

			nrows = maxi - mini;
			ncols = maxj - minj;

			ox = ulxmap + (minj * xdim);
			oy = ulymap - (mini * ydim);


			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	public double[][] getCellBorder(int i, int j) {
		double[][] ll = new double[5][2];		
		
		// bottom left corner
		double x = ox + (j * xdim) - xdim/2;
		double y = oy - (i * ydim) - ydim/2; 
		
		ll[0] = PointConvert.ROMA40toWGS84(new double[]{x,y});
		ll[1] = PointConvert.ROMA40toWGS84(new double[]{x+xdim,y});
		ll[2] = PointConvert.ROMA40toWGS84(new double[]{x+xdim,y+ydim});
		ll[3] = PointConvert.ROMA40toWGS84(new double[]{x,y+ydim});
		ll[4] = PointConvert.ROMA40toWGS84(new double[]{x,y});
		return ll;
	}
	
	public double[] getCenter(int i, int j) {
		double x = ox + (j * xdim);
		double y = oy - (i * ydim);
		
		double [] bb = new double[]{x,y};
		return PointConvert.ROMA40toWGS84(bb);
	}
	
	
	
	public int[] getij(double lon, double lat) {
		int j = (int)Math.floor((lon - ox + xdim/2)/xdim);
		int i = (int)Math.floor((oy - lat + ydim/2)/ydim);
		return new int[]{i,j};
	}


	public void toKml(String file) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		printHeaderDocument(out,this.name);
		KMLSquare kmlsq = new KMLSquare();
		for(int i=0; i<nrows;i++)
			for(int j=0; j<ncols;j++)
				out.println(kmlsq.draw(this.getCellBorder(i,j),i+","+j,"990000ff","990000aa",i+","+j));
		printFooterDocument(out);
		out.close();
	}



	public void toGeoJson(String file) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		FeatureCollection featureCollection = new FeatureCollection();

		for(int i=0; i<nrows;i++)
			for(int j=0; j<ncols;j++) {

				Feature f = new Feature();
				Map<String, Object> p = new HashMap<>();
				p.put("id", i+"_"+j);
				p.put("area", xdim*ydim);
				f.setProperties(p);

				Polygon poly = new Polygon();
				double[][] lonLatBorder = this.getCellBorder(i, j);
				List<LngLatAlt> border = new ArrayList<>();
				for (int k = 0; k < lonLatBorder.length; k++)
					border.add(new LngLatAlt(lonLatBorder[k][0], lonLatBorder[k][1]));
				poly.setExteriorRing(border);
				f.setGeometry(poly);

				featureCollection.add(f);
			}
		new ObjectMapper().writeValue(new File(file),featureCollection);
		out.close();
	}


	
	// getters
	public String getName() {
		return name;
	}

	public double[][] getLonLatBbox() {
		return lonLatBbox;
	}

	public double getOx() {
		return ox;
	}

	public double getOy() {
		return oy;
	}

	public double getXdim() {
		return xdim;
	}

	public double getYdim() {
		return ydim;
	}

	public int getMini() {
		return mini;
	}

	public int getMaxi() {
		return maxi;
	}

	public int getMinj() {
		return minj;
	}

	public int getMaxj() {
		return maxj;
	}

	public int getNrows() {
		return nrows;
	}

	public int getNcols() {
		return ncols;
	}

	public static void main(String[] args) throws Exception {

		String city = "modena";
		CityArea a = new CityArea(city,LonLatBbox.get(city),anyFile);
		//a.toKml(base_dir+city".kml");
		a.toGeoJson(base_dir_er+city+".geojson");
	}

}
