package citylive;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 13/10/2016.
 */
public class Tables {

    static final String base_dir_er = "C:\\Users\\marco\\Desktop\\AppVenezia\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\emilia-romagna\\";
    static final String anyFile = base_dir_er + "Emilia_CityLive_TrafficErl_201101310900_201101311000.txt.gz";

    static final Map<String,double[][]> LonLatBbox = new HashMap<>();
    static {
        LonLatBbox.put("modena",new double[][]{{10.857719,44.620862},{10.995702,44.688281}});
    }



    public static void main(String[] args) {
        double[][] bbox =  LonLatBbox.get("modena");
        System.out.println("bbox.bl="+bbox[0][0]+", "+bbox[0][1]);
        System.out.println("bbox.tr="+bbox[1][0]+", "+bbox[1][1]);
        System.out.println("bbox.tl="+bbox[0][0]+", "+bbox[1][1]);
        System.out.println("bbox.br="+bbox[1][0]+", "+bbox[0][1]);
    }


}
