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
        run("modena");
    }

    public static void run(String city) throws Exception {

        String base_dir = "C:\\Users\\marco\\Desktop\\AppVenezia\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\";
        CityArea a = new CityArea(city, CityProp.getInstance().get(city).getLonLatBbox());

        a.toGeoJson(base_dir+"grid.geojson");


        new File(base_dir).mkdirs();
        PrintWriter out_wd = new PrintWriter(new FileWriter(base_dir+"ts_cell_occupancy_wd_avg_n.csv"));
        PrintWriter out_we = new PrintWriter(new FileWriter(base_dir+"ts_cell_occupancy_we_avg_n.csv"));

        PrintWriter out_cell_max = new PrintWriter(new FileWriter(base_dir+"cell_max"));
        PrintWriter out_grid_roads = new PrintWriter(new FileWriter(base_dir+"grid_roads.csv"));

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
    }
}
