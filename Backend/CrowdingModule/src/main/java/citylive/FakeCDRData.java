package citylive;

import io.CityProp;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by marco on 13/10/2016.
 */
public class FakeCDRData {

    public static void main(String[] args) throws Exception {
        for(String city: CityProp.getInstance().keySet()) {
            System.out.println("Processing "+city);
            run(city);
        }
    }

    public static void run(String city) throws Exception {

        String cm_base_dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\";
        String dita_base_dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\";
        new File(cm_base_dir).mkdirs();
        new File(dita_base_dir).mkdirs();
        CityArea a = new CityArea(city, CityProp.getInstance().get(city).getLonLatBbox());

        a.toGeoJson(cm_base_dir+"grid.geojson");
        a.toGeoJson(dita_base_dir+"grid.geojson");

        a.toKml(cm_base_dir+"grid.kml");

        PrintWriter out_wd = new PrintWriter(new FileWriter(cm_base_dir+"ts_cell_occupancy_wd_avg_n.csv"));
        PrintWriter out_we = new PrintWriter(new FileWriter(cm_base_dir+"ts_cell_occupancy_we_avg_n.csv"));

        PrintWriter out_cell_max = new PrintWriter(new FileWriter(cm_base_dir+"cell_max"));
        PrintWriter out_grid_roads = new PrintWriter(new FileWriter(cm_base_dir+"grid_roads.csv"));

        for(int i=0; i<a.getNrows();i++)
            for(int j=0; j<a.getNcols();j++)
                for(int t=0; t<60*15*4*24;t=t+900) {
                    double r = Math.random();
                    // 56700	3693_3_0_2	235.391	5.00833	5.00833	0.330849	0.330849
                    out_wd.println(t+"\t"+(i+"_"+j)+"\t"+r+"\t"+r+"\t"+r+"\t"+r+"\t"+r);
                    out_we.println(t+"\t"+(i+"_"+j)+"\t"+r+"\t"+r+"\t"+r+"\t"+r+"\t"+r);
                    out_cell_max.println((i+"_"+j)+" 1");
                    out_grid_roads.println((i+"_"+j)+",1");
                }
        out_wd.close();
        out_we.close();
        out_cell_max.close();
        out_grid_roads.close();
    }
}
