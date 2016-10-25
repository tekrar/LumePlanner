package io;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 19/10/2016.
 */
public class CityProp extends HashMap<String,CityProperties> {

    private static CityProp instance;

    public static CityProp getInstance() {
        if(instance == null) instance = new CityProp();
        return instance;
    }


    private CityProp() {
        super();
        this.put("Modena",new CityProperties("Modena",new double[]{10.857719,44.620862},new double[]{10.995702, 44.688281}));
        //this.put("Venezia",new CityProperties("Venezia",new double[]{12.297951, 45.414259},new double[]{12.406648, 45.481153}));
        this.put("ReggioEmilia",new CityProperties("ReggioEmilia",new double[]{10.600261,44.668362},new double[]{10.696646,44.727112}));
        this.put("Bologna",new CityProperties("Bologna",new double[]{11.295272,44.477848},new double[]{11.371821, 44.514401}));
        this.put("Ferrara",new CityProperties("Ferrara",new double[]{11.592697,44.827567},new double[]{11.640950, 44.850873}));
        this.put("Parma",new CityProperties("Parma",new double[]{10.269492,44.754067},new double[]{10.378844,      44.835184}));
    }
}

