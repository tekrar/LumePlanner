package util;

import model.UncertainValue;

import org.apache.commons.math3.random.RandomDataGenerator;

public class RandomValue {
	
	private static RandomDataGenerator rnd;
	
	static {
		rnd = new RandomDataGenerator();
		rnd.reSeed(1234567890l);
	}

	/**
	 * 
	 * @param value --> 		mean
	 * @param distribution --> 	format: "D:P" 
	 * 							where D is a char identifying the distribution (e.g. 'N'=normal)
	 * 							and P is a float (optional) containing the parameter for the distribution (e.g. 1.0=std-dev)
	 */
	public static double get(double value, String distribution) {
		return value;
//		switch (distribution.split(":")[0]) {
//		//normal
//		case "N": return rnd.nextGaussian((double)value, Double.parseDouble(distribution.split(":")[1]));
//		//exponential
//		case "E": return rnd.nextExponential((double)value);
//		//poisson
//		case "P": return rnd.nextPoisson((double)value);
//		//not found
//		default: return Double.NEGATIVE_INFINITY;
//		}
	}

	public static double get(UncertainValue value) {
		
		if (null == value || value.getMean() == 0d || Double.parseDouble(value.getDistribution().split(":")[1]) == 0d) return 0d;
		return value.getMean();
//		switch (value.getDistribution().split(":")[0]) {
//		//normal
//		case "N": return rnd.nextGaussian(value.getMean(), Double.parseDouble(value.getDistribution().split(":")[1]));
//		//exponential
//		case "E": return rnd.nextExponential(value.getMean());
//		//poisson
//		case "P": return rnd.nextPoisson(value.getMean());
//		//not found
//		default: return Double.NEGATIVE_INFINITY;
//		}
	}

}
