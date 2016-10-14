package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VisitPlanAlternatives {
	private VisitPlan 	greedy;
	private VisitPlan 	shortest;
	private VisitPlan 	crowd_related;
	private double		crowd_preference; //-1=fullyCrowded; -0.5=mainlyCrowded; +0.5=mainlyUncrowded; +1=fullyUncrowded
	
	public VisitPlanAlternatives() {
	    this.shortest = new VisitPlan();
	    this.crowd_related = new VisitPlan();
	    this.greedy = new VisitPlan();
	    this.crowd_preference = 1d;
	}
	
	public VisitPlanAlternatives(VisitPlan greedy, VisitPlan shortest, VisitPlan crowd_related, double crowd_preference) {
		this.setGreedy(greedy);
		this.setShortest(shortest);
		this.setCrowd_related(crowd_related);
		this.setCrowd_preference(crowd_preference);
	}

	public VisitPlan getGreedy() {
		return greedy;
	}

	public void setGreedy(VisitPlan greedy) {
		this.greedy = greedy;
	}

	public VisitPlan getShortest() {
		return shortest;
	}

	public void setShortest(VisitPlan shortest) {
		this.shortest = shortest;
	}

	public VisitPlan getCrowd_related() {
		return crowd_related;
	}
	
	public void setCrowd_related(VisitPlan crowd_related) {
		this.crowd_related = crowd_related;
	}

	public double getCrowd_preference() {
		return crowd_preference;
	}

	public void setCrowd_preference(double crowd_preference) {
		this.crowd_preference = crowd_preference;
	}

	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String toString() {
		return "VisitPlanAlternatives [greedy=" + greedy + ", shortest="
				+ shortest + ", crowd_related=" + crowd_related + "]";
	}

	
	

}
