package model;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private int 		id;
	private String 		name;
	//private double g_scores = 0; //cost from parent
	private int 		arrivalTime = 0;
	private int 		departureTime = 0; //cost from parent
	private double 		h_scores = 0; //heuristic cost to target
	private double 		f_scores = 0; //f=g+h total cost of traversal
	private List<Edge> 	adjacencies;
	private double		congestion_score = 0;
	private Node 		predecessor;
	private int 		depth;
	private Node 		parent;

	public Node(int id, String val, double hVal, int depth, Node predecessor){
		this.id =  id;
		this.name = val;
		this.h_scores = hVal;
		this.depth = depth;
		this.predecessor = predecessor;
		this.adjacencies = new ArrayList<Edge>();
	}
	
	public Node() {
		this.id = -1;
		this.name = "";
		this.h_scores = 0d;
		this.depth = -1;
		this.predecessor = null;
		this.adjacencies = new ArrayList<Edge>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

//	public double getG_scores() {
//		return g_scores;
//	}
//
//	public void setG_scores(double g_scores) {
//		this.g_scores = g_scores;
//	}

	public double getH_scores() {
		return h_scores;
	}

	public void setH_scores(double h_scores) {
		this.h_scores = h_scores;
	}

	public double getF_scores() {
		return f_scores;
	}

	public void setF_scores(double f_scores) {
		this.f_scores = f_scores;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(int departureTime) {
		this.departureTime = departureTime;
	}

	public List<Edge> getAdjacencies() {
		return adjacencies;
	}

	public void setAdjacencies(List<Edge> adjacencies) {
		this.adjacencies = adjacencies;
	}

	public double getCongestion_score() {
		return congestion_score;
	}

	public void setCongestion_score(double congestion_score) {
		this.congestion_score = congestion_score;
	}

	public Node getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(Node predecessor) {
		this.predecessor = predecessor;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	
	

}







