package util;

import java.util.Comparator;

import model.Node;

public class NodeComparator implements Comparator<Node> {

	public NodeComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Node o1, Node o2) {
		if(o1.getF_scores() > o2.getF_scores()){
			return 1;
		}

		else if (o1.getF_scores() < o2.getF_scores()){
			return -1;
		}

		else{
			return 0;
		}
	}

}
