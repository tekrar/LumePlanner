package io;

/**
 * Created by marco on 19/10/2016.
 */
public class CityProperties {
    private String name;
    private double[] lonLatBL;
    private double[] lonLatTR;

    CityProperties(String name, double[] lonLatBL, double[] lonLatTR) {
        this.name = name;
        this.lonLatBL = lonLatBL;
        this.lonLatTR = lonLatTR;
    }

    public String getDataDir() {
        return name+"/";
    }

    public String getDB() {
        return "lume-"+name;
    }

    public String getBbox() {
        return getBL()+","+getTR();
    }

    public String getBL() {
        return lonLatBL[0]+","+lonLatBL[1];
    }
    public String getTR() {
        return lonLatTR[0]+","+lonLatTR[1];
    }
    public String getBR() {
        return lonLatTR[0]+","+lonLatBL[1];
    }
    public String getTL() {
        return lonLatBL[0]+","+lonLatTR[1];
    }

    public double[][] getLonLatBbox() {
        return new double[][]{lonLatBL,lonLatTR};
    }
}
