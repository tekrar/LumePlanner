package citylive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by marco on 13/10/2016.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String base_dir = "C:\\Users\\marco\\Desktop\\AppVenezia\\Backend\\CrowdingModule\\src\\main\\webapp\\WEB-INF\\data\\venice\\";
        String file = "ts_cell_occupancy_wd_avg_n.csv";

        BufferedReader br = new BufferedReader(new FileReader(base_dir+file));
        SortedSet<Integer> times = new TreeSet<>();
        String line;

        while((line=br.readLine())!=null){
            String[] e = line.split("\t");
            times.add(Integer.parseInt(e[0]));
        }

        for(int t: times)
            System.out.println(t);

        br.close();

    }

}
