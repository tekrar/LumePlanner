package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Cell;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by marco on 12/10/2016.
 */
public class GeoJson2KML {

    public static void main(String[] args) throws Exception {
        String baseDir = "C:\\Users\\marco\\Desktop\\AppVenezia\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\emilia-romagna\\";
        FeatureCollection featureCollection = new ObjectMapper().readValue(new FileReader(baseDir + "modena.geojson"), FeatureCollection.class);

        PrintWriter out = new PrintWriter(new FileWriter(baseDir+"modena2.kml"));


        printHeaderDocument(out,"grid");
        KMLSquare kmlsq = new KMLSquare();
        for (Feature f : featureCollection.getFeatures()) {

            List<LngLatAlt> lbbox = ((Polygon) f.getGeometry()).getExteriorRing();
            double[][] bbox = new double[lbbox.size()][2];
            for(int i = 0; i < bbox.length; i++) {
                bbox[i] = new double[]{lbbox.get(i).getLongitude(),lbbox.get(i).getLatitude()};
            }

           out.println(kmlsq.draw(bbox,f.getId(),"990000ff","990000aa",f.getId()));




        }
        printFooterDocument(out);
        out.close();
    }


    public static void printHeaderDocument(PrintWriter out, String name) {
        try{
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<kml xmlns=\"http://earth.google.com/kml/2.2\">");
            out.println("<Document>");
            out.println("<name>"+name+"</name>");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void printFooterDocument(PrintWriter out) {
        try{
            out.println("</Document>");
            out.println("</kml>");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
