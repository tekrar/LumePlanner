package model;

public class UncertainValue {
	
	private Double initial = -1d;
	
	private Double mean;
	
	private String distribution; //format: "D:P" --> D(char) identifies the distribution, P(float) is a parameter for the distribution

	public UncertainValue() {
		this.initial = -1d;
		this.mean = 0d;
		this.distribution = null;
	}

	public UncertainValue(Double mean_value, String value_distribution) {
		super();
		if (this.initial.equals(-1d)) {
			this.initial = mean_value;
		}
		this.mean = mean_value;
		this.distribution = value_distribution;
	}

	public Double getInitial() {
		return initial;
	}

	public void setInitial(Double initial) {
		this.initial = initial;
	}

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean_value) {
		this.mean = mean_value;
	}

	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String value_distribution) {
		this.distribution = value_distribution;
	}

	@Override
	public String toString() {
		return "UncertainValue [initial=" + initial + ", mean=" + mean
				+ ", distribution=" + distribution + "]";
	}

	

}
