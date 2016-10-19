package model;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {

	private String 			city;
	private String 			id;
	private String 			email;
	private String 			password;
	private UncertainValue 	low_crowding;
	private UncertainValue 	lowAvg_crowding;
	private UncertainValue 	avgHig_crowding;
	private UncertainValue 	hig_crowding;
	private UncertainValue 	overall_crowding;
	private boolean 		liked_crowding;
	private boolean 		liked_plan;
	
	public User() {
		this("","");
	}
	
	public User(String email, String password) {
		//this.id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
		this.setEmail(email);
		this.setPassword(password);
		this.setLow_crowding(new UncertainValue(0d, "N:0.0"));
		this.setLowAvg_crowding(new UncertainValue(0d, "N:0.0"));
		this.setAvgHig_crowding(new UncertainValue(0d, "N:0.0"));
		this.setHig_crowding(new UncertainValue(0d, "N:0.0"));
		this.setOverall_crowding(new UncertainValue(0d, "N:0.0"));
		this.setLiked_crowding(false);
		this.setLiked_plan(false);
	}


	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getId() {
		return id;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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

	public UncertainValue getLow_crowding() {
		return low_crowding;
	}

	public void setLow_crowding(UncertainValue low_crowding) {
		this.low_crowding = low_crowding;
	}

	public UncertainValue getLowAvg_crowding() {
		return lowAvg_crowding;
	}

	public void setLowAvg_crowding(UncertainValue lowavg_crowding) {
		this.lowAvg_crowding = lowavg_crowding;
	}

	public UncertainValue getAvgHig_crowding() {
		return avgHig_crowding;
	}

	public void setAvgHig_crowding(UncertainValue avghig_crowding) {
		this.avgHig_crowding = avghig_crowding;
	}

	public UncertainValue getHig_crowding() {
		return hig_crowding;
	}

	public void setHig_crowding(UncertainValue hig_crowding) {
		this.hig_crowding = hig_crowding;
	}

	public UncertainValue getOverall_crowding() {
		return overall_crowding;
	}

	public void setOverall_crowding(UncertainValue overall_crowding) {
		this.overall_crowding = overall_crowding;
	}

	public boolean isLiked_crowding() {
		return liked_crowding;
	}

	public void setLiked_crowding(boolean liked_crowding) {
		this.liked_crowding = liked_crowding;
	}

	public boolean isLiked_plan() {
		return liked_plan;
	}

	public void setLiked_plan(boolean liked_plan) {
		this.liked_plan = liked_plan;
	}
	
	

}
