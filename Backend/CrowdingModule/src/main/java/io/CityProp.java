package io;

import java.util.HashMap;

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
        this.put("Venezia",new CityProperties("Venezia",new double[]{12.297951, 45.414259},new double[]{12.406648, 45.481153}));
    }
}

