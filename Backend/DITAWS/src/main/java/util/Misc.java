package util;

/**
 * Created by marco on 12/10/2016.
 */
public class Misc {

    public static double round(double x, int position) {
        double a = x;
        double temp = Math.pow(10.0, position);
        a *= temp;
        a = Math.round(a);
        return (a / (double) temp);
    }

    public static double haverDist(double[]p1, double[]p2) {
        double earthRadius = 6371000d; //m
        double dLat = Math.toRadians(p2[0]-p1[0]);
        double dLng = Math.toRadians(p2[1]-p1[1]);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(p1[0])) * Math.cos(Math.toRadians(p2[0]));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return round(dist,3);
    }

}
