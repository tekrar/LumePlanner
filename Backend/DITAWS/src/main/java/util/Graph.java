package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import model.Edge;
import model.Node;

public class Graph {

	private static Logger logger = Logger.getLogger(Graph.class);

	//static Map<String, Double> h_walkTime = new HashMap<String, Double>(); //<POI, distance to end>
	//static Map<String, HashMap<String, ArrayList<UncertainValue>>> walkTime = new HashMap<String, HashMap<String, ArrayList<UncertainValue>>>(); //<POIstart, <POIend, walking time between> [seconds]
	static Map<String, List<String>> nodeSuccessors = new HashMap<String, List<String>>();
	static Map<String, Map<Integer, Node>> graph = new HashMap<String, Map<Integer, Node>>();

	//	public static void main(String[] args) throws IOException {
	//		Mongo dao = new Mongo();
	//		//input POIs
	//		POI startPlace = dao.retrieveActivity("22495934");
	//		POI endPlace = dao.retrieveActivity("22321057");//22321057
	//		String [] POIsIDarr = new String [] {
	//				"22495934", //22495934=hotel
	//				"15614232", 
	//				"99538099", 
	//				"22321057",
	//				"16915931", //restaurant
	//				"22495269", 
	//				"20299282", 
	//				"16617120", 
	//				"99531978", 
	//		"22293689"}; 
	//		List<String> POIsIDlist = new ArrayList<String>();
	//		for (int i=0; i< POIsIDarr.length; i++) {
	//			POIsIDlist.add(POIsIDarr[i]);
	//		}
	//
	//		//runDocit() as an alternative to compute the heuristic cost for the graph
	//		//getWalkTimeDOCIT(POIsIDarr);
	//
	//		Map<String[], Double> TSPSolutions = new TabuSearchTSP().run(startPlace, endPlace, POIsIDlist, 5);
	//
	//
	//		//walkTime = getTravelTimeFromIDs(POIsIDlist);
	//		nodeSuccessors = getSuccessorsList(TSPSolutions);
	//		//graph = buildGraph(startPlace, nodeSuccessors, walkTime);
	//		graph = buildGraph(startPlace.getPlace_id(), endPlace.getPlace_id(), nodeSuccessors);
	//	}


	public static Map<String, Map<Integer, Node>> buildGraph(String source_id, String destination_id, Map<String, List<String>> successors) {

		Map<String, Map<Integer, Node>> result = new HashMap<String, Map<Integer, Node>>(); // < POIID < NodeID, Node > >

		try {
			int node_id = 0;
			int depths = successors.size(); //number of POIs in the visiting sequence
			int depth = 0;
			Vector<Node> sources = new Vector<Node>();
			Node source = new Node(node_id, source_id, 0d, depth, null); //first node, root without predecessor
			//logger.info("["+depth+"]("+node_id+") "+source.getName());//+" ("+source.getPredecessor()+")");
			result.put(source_id, new HashMap<Integer, Node>());
			result.get(source_id).put(node_id++, source);
			sources.add(source); 
			while (depth++<depths) { //loop while you are not at the final depth
				while (!sources.isEmpty() && sources.get(0).getDepth() < depth) {
					source = sources.remove(0);

					List<String> nextSuccessors = (successors.get(source.getName())==null) ? new ArrayList<String>() : successors.get(source.getName());
					//logger.info(source.getName());
					//logger.info("and its successors:"+ nextSuccessors.toString() +" (size:"+nextSuccessors.size()+")");
					for (String sourceSucc : nextSuccessors) { //loop on each successor candidate
						boolean toInsert = true;
						Node predecessor = source.getPredecessor();
						//while (predecessor != null) {
						while (predecessor != null) {
							//if (predecessor.getName().equals(sourceSucc) && !(depth==depths && predecessor.getName().equals(destination_id))) {
							if (predecessor.getName().equals(sourceSucc) && !(depth==depths && predecessor.getName().equals(source_id)) || (depth!=depths && sourceSucc.equals(destination_id) )) {
								toInsert = false;
								break;
							}
							predecessor = predecessor.getPredecessor();
						}
						if (toInsert) {
							//Node newNode = new Node(node_id, sourceSucc, travelTime.get(source_destination).get(sourceSucc), depth, source);
							Node newNode = new Node(node_id, sourceSucc, 0, depth, source);
							//logger.info("["+depth+"]("+node_id+") "+newNode.getName()+" ("+newNode.getPredecessor().getName()+"-"+newNode.getPredecessor().getId()+")");
							if (result.get(sourceSucc)==null)
								result.put(sourceSucc, new HashMap<Integer, Node>());
							//result.get(source.getName()).get(source.getId()).getAdjacencies().add(new Edge(newNode, travelTime.get(source.getName()).get(sourceSucc)));
							result.get(source.getName()).get(source.getId()).getAdjacencies().add(new Edge(newNode, 0));
							result.get(sourceSucc).put(node_id++, newNode);
							sources.add(newNode);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}


	public static Map<String, List<String>> getSuccessorsList(Map<String[], Double> TSPSolutions) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (String[] solution : TSPSolutions.keySet()) {
			for (int i=0; i<solution.length-1;i++) {
				if (solution[i]!=null) {
					if (result.get(solution[i])==null) {
						result.put(solution[i], new ArrayList<String>());
					}
					if (!result.get(solution[i]).contains(solution[i+1]) && solution[i+1]!=null) {
						result.get(solution[i]).add(solution[i+1]);
					}
				}
			}
		}

		for (String node : result.keySet()) {
			logger.info(node+":");
			for (String succ : result.get(node)) {
				logger.info(succ+" ");
			}
			logger.info("");
		}
		return result;
	}











}