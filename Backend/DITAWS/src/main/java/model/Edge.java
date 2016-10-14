package model;

public class Edge{
    private double cost;
    private Node target;

    public Edge(Node targetNode, double costVal){
            target = targetNode;
            cost = costVal;
    }
    
    public void setCost(double cost) {
    	this.cost = cost;
    }
    
	public double getCost() {
		return cost;
	}

	public void setTarget(Node target) {
		this.target = target;
	}
	public Node getTarget() {
		return target;
	}
}