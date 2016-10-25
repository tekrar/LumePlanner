package util;


import io.CityData;
import io.Mongo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import services.ComputeDistances;
import model.Distance;
import model.DistanceTo;
import model.POI;

public class TabuSearchTSP {

	//private Logger logger = Logger.getLogger(TabuSearchTSP.class);

	public static double 	[][]	distances;
	public static int 		[][] 	tabuList;
	public static int 				tabuLength;
	public static int				tabuTenure;
	public static int 				numberOfIterations;
	public static int 				numberOfTopSolutions;


//	public static void main(String[] args) throws IOException {
//		TabuSearchTSP tsp = new TabuSearchTSP();
//		//input POIs
//		POI startPlace = tsp.dao.retrieveActivity("20299282");
//		POI endPlace = tsp.dao.retrieveActivity("22495934");
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
//				"22293689",
//				"104952587",
//				"107822765"
//		}; 
//
//		List<String> POIsIDlist = new ArrayList<String>();
//		for (int i=0; i< POIsIDarr.length; i++) {
//			POIsIDlist.add(POIsIDarr[i]);
//		}
//		tsp.run(startPlace, endPlace, POIsIDlist, 15);
//	}

	public Map<String[], Double> run(CityData cityData, POI startPlace, POI endPlace, List<String> POIsIDlist, int numBestSolutions) throws IOException {


		Map<Integer,Map<Double, int[]>> topSolutions = new HashMap<Integer,Map<Double, int[]>>(); //<iteration, <cost, index[]>>
		Map<String[], Double> topSolutionsPlace = new HashMap<String[], Double>(); //<place[], cost>
		double worstTopCost = 0d;
		int worstTopCostIndex = -1;
		

		//retrieve getDistance map
		TreeMap<String, TreeMap<String, Double>> distancesMap = getDistances(cityData, startPlace, endPlace, POIsIDlist);
							//dao.retrieveDistances(startPlace, endPlace, POIsID)
		
		Map<String, Integer> mapPlaceIndex = new HashMap<String, Integer>();
		Map<Integer, String> mapIndexPlace = new HashMap<Integer, String>();

		//initialize getDistance matrix - tabuLength - numIterations - numTopSolutions
		distances = new double [distancesMap.size()][distancesMap.size()];
		//logger.info("distances length:"+distances.length);
		tabuLength = distances.length;
		tabuTenure = 2*distances.length;
		numberOfIterations = 4*(int)Math.pow(distances.length,2);
		numberOfTopSolutions = numBestSolutions;

		//populate getDistance matrix
		int cRow = 0, cCol = 0;
		for (String key : distancesMap.keySet()) {
			//logger.info("1:"+key);
			mapPlaceIndex.put(key, cRow);
			mapIndexPlace.put(cRow, key);
			cCol = 0;
			for (String key2 : distancesMap.get(key).keySet()) {
				if (POIsIDlist.contains(key2)) {
					distances[cRow][cCol++] = distancesMap.get(key).get(key2);
				}
			}
			cRow++;
		}
		//print getDistance matrix
//		String out1 ="";
//		for (int i=0; i<distances.length;i++) {
//			for (int j=0; j<distances[i].length;j++) 
//				out1+=distances[i][j]+"\t";
//			out1+="\n";
//		}
		//logger.info(out1);

		//initialize tabulist & solution array
		tabuList = new int[tabuLength][tabuLength];		

		int [] currentSol;
		if (startPlace.getPlace_id().equals(endPlace.getPlace_id())) {
			currentSol = new int [distances.length+1];
		} else {
			currentSol = new int [distances.length];
		}
		//logger.info("solSize:"+currentSol.length);
		//generate starting solution
		currentSol[0] = mapPlaceIndex.get(startPlace.getPlace_id());
		//currentSol[currentSol.length-1] = currentSol[0];
		currentSol[currentSol.length-1] = mapPlaceIndex.get(endPlace.getPlace_id());
		//logger.info("0:"+currentSol[0]);
		//logger.info((currentSol.length-1)+":"+currentSol[currentSol.length-1]);
		boolean start_inserted = false;
		boolean end_inserted = false;
		for (int i=0;i<currentSol.length-1;) {
			if (start_inserted && end_inserted) {
				currentSol[i] = ++i;
			} else if (!start_inserted && !end_inserted) {
				if (i!=currentSol[0] && i!=currentSol[currentSol.length-1] ) {
					currentSol[i+1] = i++;
				} else if (i==currentSol[0]) {
					i+=2; 
					if (i-1<currentSol.length-1 && i-1!=currentSol[currentSol.length-1]) {
						currentSol[i-1] = i-1;
						if (i<currentSol.length-1 && i!=currentSol[currentSol.length-1]) {
							currentSol[i] = i++;
						} 
					} else if (i-1==currentSol[currentSol.length-1]) {
						i+=0;
						if (i-1<currentSol.length-1) {
							currentSol[i-1] = i;
							if (i<currentSol.length-1) {
								currentSol[i] = ++i;
							}
						}
						end_inserted = true;
					}
					start_inserted = true;
				} else if (i==currentSol[currentSol.length-1]) {
					i+=2; 
					if (i-1<currentSol.length-1 && i-1!=currentSol[0]) {
						currentSol[i-1] = i-1;
						if (i<currentSol.length-1 && i!=currentSol[0]) {
							currentSol[i] = i++;
						} 
					} else if (i-1==currentSol[0]) {
						i+=0;
						if (i-1<currentSol.length-1) {
							currentSol[i-1] = i;
							if (i<currentSol.length-1) {
								currentSol[i] = ++i;
							}
						}
						start_inserted = true;
					}
					end_inserted = true;
				} 
			} else if (!start_inserted) {
				if (i!=currentSol[0]) {
					currentSol[i] = i++;
				} else if (i==currentSol[0]) {
					i+=1; 
					if (i-1<currentSol.length-1) {
						currentSol[i-1] = i;
						if (i<currentSol.length-1) {
							currentSol[i] = ++i;
						}
					}
					start_inserted = true;
				}
			} else {
				if (i!=currentSol[currentSol.length-1]) {
					currentSol[i] = i++;
				} else if (i==currentSol[currentSol.length-1]) {
					i+=1; 
					if (i-1<currentSol.length-1) {
						currentSol[i-1] = i;
						if (i<currentSol.length-1) {
							currentSol[i] = ++i;
						}
					}
					end_inserted = true;
				}
			}

		}

		//print initial solution
		//printSolution(currentSol);
		//logger.info("Initial cost = " + getObjectiveFunctionValue(currentSol));	

		//initial solution is the best So Far
		//int bestSolIndex = -1;
		int[] bestSol = new int[currentSol.length]; 
		System.arraycopy(currentSol, 0, bestSol, 0, bestSol.length);
		double bestCost = getObjectiveFunctionValue(bestSol);

		//store initial solution in the top Map
		topSolutions.put(-1, new HashMap<Double, int[]>());
		topSolutions.get(-1).put(bestCost, bestSol);
		worstTopCost = bestCost;
		worstTopCostIndex = -1;

		for (int i = 0; i < numberOfIterations; i++) { // perform iterations here
			//Swap edges and compute new solution
			currentSol = getBestNeighbour(tabuList, currentSol);
			double currCost = getObjectiveFunctionValue(currentSol);

			//if we have an improvement, then make it the best solution
			if (currCost < bestCost) {
				//printSolution(bestSol);
				//printSolution(currentSol);
				System.arraycopy(currentSol, 0, bestSol, 0, bestSol.length);
				bestCost = currCost;
				//bestSolIndex = i;
			}
			//check if it can be part of the top Map
			if (topSolutions.size()<numberOfTopSolutions) {
				topSolutions.put(i, new HashMap<Double, int[]>());
				topSolutions.get(i).put(currCost, new int[currentSol.length]);
				System.arraycopy(currentSol, 0, topSolutions.get(i).get(currCost), 0, currentSol.length);
				if (currCost > worstTopCost) {
					//update worst cost
					worstTopCost = currCost;
					worstTopCostIndex = i;
				}
			} else {
				if (currCost < worstTopCost) {
					topSolutions.remove(worstTopCostIndex);
					topSolutions.put(i, new HashMap<Double, int[]>());
					topSolutions.get(i).put(currCost, new int[currentSol.length]);
					System.arraycopy(currentSol, 0, topSolutions.get(i).get(currCost), 0, currentSol.length);
					worstTopCost = 0d;
					for (Integer iterKey : topSolutions.keySet()) {
						for (Double costKey : topSolutions.get(iterKey).keySet()) {
							if (costKey > worstTopCost) {
								//update worst cost
								worstTopCostIndex = iterKey;
								worstTopCost = costKey;
							}
						}
					}
				}
			}
		}

		//logger.info("Search done! \nBest cost found at iteration "+bestSolIndex+" = " + bestCost + "\nBest Solution :");
		//printSolution(bestSol);

		for (Integer iterKey : topSolutions.keySet()) {
			//String out = "";
			for (Double key : topSolutions.get(iterKey).keySet()) {
				String [] places = new String [currentSol.length];
				for (int i=0; i<topSolutions.get(iterKey).get(key).length;i++) {
					places[i] = mapIndexPlace.get(topSolutions.get(iterKey).get(key)[i]);
					//out += "("+topSolutions.get(iterKey).get(key)[i]+") ";
					//out += mapIndexPlace.get(topSolutions.get(iterKey).get(key)[i])+" ";
				}
				topSolutionsPlace.put(places, round(key,3));
				//out +="\t("+round(key,3)+") iter "+iterKey;
				//logger.info(out);
				//System.out.println(out);
			}
		}
		return topSolutionsPlace;
	}

	private TreeMap<String, TreeMap<String, Double>> getDistances(CityData cityData, POI startPlace, POI endPlace, List<String> POIsIDlist) {
		TreeMap<String, TreeMap<String, Double>> distancesMap = new TreeMap<>();
		POI closest_to_end = null;
		if (endPlace.getPlace_id().equals("00")) {
			closest_to_end = cityData.retrieveClosestActivity(endPlace);
			POIsIDlist.add(closest_to_end.getPlace_id());
		}
		
		for (String from : cityData.distances.keySet()) {
			if (POIsIDlist.contains(from)) {
				TreeMap<String, Double> tos= new TreeMap<String, Double>();
				for (String to : cityData.distances.get(from).keySet()) {
					if (null != closest_to_end && closest_to_end.getPlace_id().equals(to)) {
						tos.put("00", cityData.getDistance(from,to));
					} else {
						tos.put(to,  cityData.getDistance(from,to));
					}
				}
				if (null != closest_to_end && closest_to_end.getPlace_id().equals(from)) {
					distancesMap.put("00", tos);
				} else {
					distancesMap.put(from, tos);
				}
			}
		}
		
		if (startPlace.getPlace_id().equals("0")) {
			Distance d = new ComputeDistances().runOnetoMany(cityData, startPlace, endPlace, POIsIDlist);
			TreeMap<String, Double> tos = new TreeMap<String, Double>();
			for (DistanceTo current : d.getDistances()) {
				tos.put(current.getTo(), current.getDistance());
			}
			distancesMap.put(d.getFrom(), tos);
		}
		return distancesMap;
	}


	public int[] getBestNeighbour(int[][] tabuList, int[] initSolution) {

		int[] bestSol = new int[initSolution.length]; 
		System.arraycopy(initSolution, 0, bestSol, 0, bestSol.length);
		double bestCost = getObjectiveFunctionValue(initSolution);
		int poi1 = 0;
		int poi2 = 0;
		boolean firstNeighbor = true;

		for (int i = 1; i < bestSol.length - 1; i++) {
			for (int j = 2; j < bestSol.length - 1; j++) {
				if (i == j) continue;

				int[] newBestSol = new int[bestSol.length]; //this is the best Solution So Far
				System.arraycopy(bestSol, 0, newBestSol, 0, newBestSol.length);

				newBestSol = swapOperator(i, j, initSolution); //Try swapping i and j
				// , maybe we get a bettersolution
				double newBestCost = getObjectiveFunctionValue(newBestSol);

				if ((newBestCost < bestCost || firstNeighbor) && tabuList[i][j] == 0) { //if better move found, store it
					firstNeighbor = false;
					poi1 = i;
					poi2 = j;
					System.arraycopy(newBestSol, 0, bestSol, 0, newBestSol.length);
					bestCost = newBestCost;
				}

			}
		}

		if (poi1 != 0) {
			decrementTabu();
			tabuMove(poi1, poi2);
		}

		return bestSol;
	}

	public int[] swapOperator(int poi1, int poi2, int[] solution) {
		int temp = solution[poi1];
		solution[poi1] = solution[poi2];
		solution[poi2] = temp;
		return solution;
	}

	public void tabuMove(int poi1, int poi2){ //tabus the swap operation
		tabuList[poi1][poi2]+= tabuTenure;
		tabuList[poi2][poi1]+= tabuTenure;
	}

	public void decrementTabu(){
		for(int i = 0; i<tabuList.length; i++){
			for(int j = 0; j<tabuList.length; j++){
				tabuList[i][j] -= tabuList[i][j] <= 0 ? 0 : 1;
			} 
		}
	}

	public double getObjectiveFunctionValue(int solution[]){ //returns the path cost
		double cost = 0;
		for(int i = 0 ; i < solution.length-1; i++){
			cost+= distances[solution[i]][solution[i+1]];
		}
		return cost;
	}

	public void printSolution(int[] solution) {
		String out = "";
		for (int i = 0; i < solution.length; i++) {
			out += solution[i] + " ";
		}
		//logger.info(out);
		System.out.println(out);
	}

	private double round(double x, int position)
	{
		double a = x;
		double temp = Math.pow(10.0, position);
		a *= temp;
		a = Math.round(a);
		return (a / temp);
	}
}
