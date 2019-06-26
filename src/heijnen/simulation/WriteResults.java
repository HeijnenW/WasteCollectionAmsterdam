package heijnen.simulation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import heijnen.objects.Container;
import heijnen.planningObjects.Cluster;
import heijnen.planningObjects.Route;

public class WriteResults {

	////		FIELDS			////
	
	// KPI trackers (a list represents the results of one replication)						( = 100 entries for 100 days )
	public static ArrayList<Double> totalDistancesRepl = new ArrayList<Double>();
	public static ArrayList<Double> totalDurationsRepl = new ArrayList<Double>();
	public static ArrayList<Integer> noEmptiedContainersRepl = new ArrayList<Integer>();
	public static ArrayList<Integer> noOverflowedContainersRepl = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsWPRepl = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsSFRepl = new ArrayList<Integer>();
	public static ArrayList<Double> avgFillContainersRepl = new ArrayList<Double>();
	public static ArrayList<Double> avgFillVehiclesRepl = new ArrayList<Double>();
	public static ArrayList<Integer> noVehiclesUsedRepl = new ArrayList<Integer>();
	public static ArrayList<Double> avgExpFillContainersRepl = new ArrayList<Double>();
	
	public static ArrayList<Integer> noContTooLateRepl = new ArrayList<Integer>();
	public static ArrayList<Integer> noContTooEarlyRepl = new ArrayList<Integer>();
	public static ArrayList<Integer> noContOnTimeRepl = new ArrayList<Integer>();
	

	// KPI trackers (a list represents the results of one experiment)						( = 5 entries for 5 replications )
	public static ArrayList<Double> totalDistancesExp = new ArrayList<Double>();
	public static ArrayList<Double> totalDurationsExp = new ArrayList<Double>();
	public static ArrayList<Integer> noEmptiedContainersExp = new ArrayList<Integer>();
	public static ArrayList<Integer> noOverflowedContainersExp = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsWPExp = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsSFExp = new ArrayList<Integer>();
	public static ArrayList<Double> avgFillContainersExp = new ArrayList<Double>();
	public static ArrayList<Double> avgFillVehiclesExp = new ArrayList<Double>();
	public static ArrayList<Integer> noVehiclesUsedExp = new ArrayList<Integer>();
	public static ArrayList<Double> avgExpFillContainersExp = new ArrayList<Double>();
	
	public static ArrayList<Integer> noContTooLateExp = new ArrayList<Integer>();
	public static ArrayList<Integer> noContTooEarlyExp = new ArrayList<Integer>();
	public static ArrayList<Integer> noContOnTimeExp = new ArrayList<Integer>();
	
	
	// KPI trackers (a list represents the results of an entire simulation run) 			( = ? entries for ? experiments )
	public static ArrayList<Double> totalDistancesSim = new ArrayList<Double>();
	public static ArrayList<Double> totalDurationsSim = new ArrayList<Double>();
	public static ArrayList<Integer> noEmptiedContainersSim = new ArrayList<Integer>();
	public static ArrayList<Integer> noOverflowedContainersSim = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsWPSim = new ArrayList<Integer>();
	public static ArrayList<Integer> noDumpsSFSim = new ArrayList<Integer>();
	public static ArrayList<Double> avgFillContainersSim = new ArrayList<Double>();
	public static ArrayList<Double> avgFillVehiclesSim = new ArrayList<Double>();
	public static ArrayList<Integer> noVehiclesUsedSim = new ArrayList<Integer>();
	public static ArrayList<Double> avgExpFillContainersSim = new ArrayList<Double>();
	public static ArrayList<Long> experimentDurationSim = new ArrayList<Long>();
	
	public static ArrayList<Integer> noContTooLateSim = new ArrayList<Integer>();
	public static ArrayList<Integer> noContTooEarlySim = new ArrayList<Integer>();
	public static ArrayList<Integer> noContOnTimeSim = new ArrayList<Integer>();

	
	//// 		FUNCTIONS		 ////
	
	/*
	 * 		Save results of current day to relevant arrays
	 */
	public static void saveDayResults() {
		totalDistancesRepl.add(Day.totalDistance);
		totalDurationsRepl.add(Day.totalDuration);
		noEmptiedContainersRepl.add(Day.noEmptyings);
		noOverflowedContainersRepl.add(Day.noOverflows);
		noDumpsWPRepl.add(Day.noDumpsWP);
		noDumpsSFRepl.add(Day.noDumpsSF);
		noVehiclesUsedRepl.add(Day.vehiclesUsed);
		
		noContTooLateRepl.add(Day.noContTooLate);
		noContTooEarlyRepl.add(Day.noContTooEarly);
		noContOnTimeRepl.add(Day.noContOnTime);
		
		if (Double.isNaN(Day.avgFillContainers) == false) {
			avgFillContainersRepl.add(Day.avgFillContainers);
		}
		if (Double.isNaN(Day.avgFillVehicles) == false) {
			avgFillVehiclesRepl.add(Day.avgFillVehicles);
		}
		if (Double.isNaN(Day.avgExpFillContainers) == false) {
			avgExpFillContainersRepl.add(Day.avgExpFillContainers);
		}		
	}
	
	
	public static void saveReplicationResults() {
		
		int noObservations = totalDistancesRepl.size();
		int noObsNaNSensitive = avgFillContainersRepl.size();
		
		double totalDistance = 0;
		double totalDuration = 0;
		int noEmptiedContainers = 0;
		int noOverflowedContainers = 0;
		int noDumpsWP = 0;
		int noDumpsSF = 0;
		double fillContainers = 0;
		double fillVehicles = 0;
		int noVehiclesUsed = 0;
		double expFillContainers = 0;
		
		int noContTooLate = 0;
		int noContTooEarly = 0;
		int noContOnTime = 0;
				
				
		for (int i = 0; i < noObservations; i++) {
			totalDistance += totalDistancesRepl.get(i);
			totalDuration += totalDurationsRepl.get(i);
			noEmptiedContainers += noEmptiedContainersRepl.get(i);
			noOverflowedContainers += noOverflowedContainersRepl.get(i);
			noDumpsWP += noDumpsWPRepl.get(i);
			noDumpsSF += noDumpsSFRepl.get(i);
			noVehiclesUsed += noVehiclesUsedRepl.get(i);
			noContTooLate += noContTooLateRepl.get(i);
			noContTooEarly += noContTooEarlyRepl.get(i);
			noContOnTime += noContOnTimeRepl.get(i);
		}
		for (int i = 0; i < noObsNaNSensitive; i++) {
			fillContainers += avgFillContainersRepl.get(i);
			fillVehicles += avgFillVehiclesRepl.get(i);
			expFillContainers += avgExpFillContainersRepl.get(i);
		}
		
		
		totalDistancesExp.add(totalDistance);
		totalDurationsExp.add(totalDuration);
		noEmptiedContainersExp.add(noEmptiedContainers);
		noOverflowedContainersExp.add(noOverflowedContainers);
		noDumpsWPExp.add(noDumpsWP);
		noDumpsSFExp.add(noDumpsSF);
		avgFillContainersExp.add(fillContainers / noObsNaNSensitive);
		avgFillVehiclesExp.add(fillVehicles / noObsNaNSensitive);
		noVehiclesUsedExp.add(noVehiclesUsed);
		avgExpFillContainersExp.add(expFillContainers / noObsNaNSensitive);
		
		noContTooLateExp.add(noContTooLate);
		noContTooEarlyExp.add(noContTooEarly);
		noContOnTimeExp.add(noContOnTime);
	
	}
	
	
	public static void saveExperimentResults() {
		int noReplications = totalDistancesExp.size();
		
		double totalDistance = 0;
		double totalDuration = 0;
		int noEmptiedContainers = 0;
		int noOverflowedContainers = 0;
		int noDumpsWP = 0;
		int noDumpsSF = 0;
		double fillContainers = 0;
		double fillVehicles = 0;
		int noVehiclesUsed = 0;
		double expFillContainers = 0;
		
		int noContTooLate = 0;
		int noContTooEarly = 0;
		int noContOnTime = 0;
		
		for (int i = 0; i < noReplications; i++) {
			totalDistance += totalDistancesExp.get(i);
			totalDuration += totalDurationsExp.get(i);
			noEmptiedContainers += noEmptiedContainersExp.get(i);
			noOverflowedContainers += noOverflowedContainersExp.get(i);
			noDumpsWP += noDumpsWPExp.get(i);
			noDumpsSF += noDumpsSFExp.get(i);
			fillContainers += avgFillContainersExp.get(i);
			fillVehicles += avgFillVehiclesExp.get(i);
			noVehiclesUsed += noVehiclesUsedExp.get(i);
			expFillContainers += avgExpFillContainersExp.get(i);
			noContTooLate += noContTooLateExp.get(i);
			noContTooEarly += noContTooEarlyExp.get(i);
			noContOnTime += noContOnTimeExp.get(i);
		}
		
		totalDistancesSim.add(totalDistance / noReplications);
		totalDurationsSim.add(totalDuration / noReplications);
		noEmptiedContainersSim.add(noEmptiedContainers / noReplications);
		noOverflowedContainersSim.add(noOverflowedContainers / noReplications);
		noDumpsWPSim.add(noDumpsWP / noReplications);
		noDumpsSFSim.add(noDumpsSF / noReplications);
		avgFillContainersSim.add(fillContainers / noReplications);
		avgFillVehiclesSim.add(fillVehicles / noReplications);
		noVehiclesUsedSim.add(noVehiclesUsed / noReplications);
		avgExpFillContainersSim.add(expFillContainers / noReplications);
		
		noContTooLateSim.add(noContTooLate / noReplications);
		noContTooEarlySim.add(noContTooEarly / noReplications);
		noContOnTimeSim.add(noContOnTime / noReplications);
	}
		
	
	
	/*	
	 * 		Write results (in String-type) to the given text-file
	 */
	public static void writeResults(String results, String outputFileName) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFileName);
			writer.write(results);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Results are written to an output-file");
	}
	
	
	
	public static void printReplicationResults() {
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("Replication" + "\t");
		stringList.add("totalDistance" + "\t");
		stringList.add("totalDuration" + "\t");
		stringList.add("noEmptiedContainers" + "\t");
		stringList.add("noOverflowedContainers" + "\t");
		stringList.add("noDumpsWP" + "\t");
		stringList.add("noDumpsSF" + "\t");
		stringList.add("avgFillContainers" + "\t");
		stringList.add("avgFillVehicles" + "\t");
		stringList.add("noVehiclesUsed" + "\t");
		stringList.add("avgExpFillContainers" + "\t");
		stringList.add(newLine);
		
		// loop over entries in arrays
		for (int i = 0; i < totalDistancesRepl.size(); i++) {
			stringList.add(Integer.toString(i) + "\t");
			stringList.add(Double.toString(totalDistancesRepl.get(i)) + "\t");
			stringList.add(Double.toString(totalDurationsRepl.get(i)) + "\t");
			stringList.add(Integer.toString(noEmptiedContainersRepl.get(i)) + "\t");
			stringList.add(Integer.toString(noOverflowedContainersRepl.get(i)) + "\t");
			stringList.add(Integer.toString(noDumpsWPRepl.get(i)) + "\t");
			stringList.add(Integer.toString(noDumpsSFRepl.get(i)) + "\t");
			stringList.add(Double.toString(avgFillContainersRepl.get(i)) + "\t");
			stringList.add(Double.toString(avgFillVehiclesRepl.get(i)) + "\t");
			stringList.add(Integer.toString(noVehiclesUsedRepl.get(i)) + "\t");
			stringList.add(Double.toString(avgExpFillContainersRepl.get(i)) + "\t");
			stringList.add(newLine);
		}
		
		// add all input to results-string
		for (String s : stringList) {
			results += s;
		}
		
		String nameFileString = "outputRepl" + Integer.toString(ExperimentController.currRepl) + ".txt";
		
		writeResults(results, nameFileString);
		openOutputFile(nameFileString);
	}
	
	
	
	public static void printSimulationResults() {
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("Experiment" + "\t");
		stringList.add("totalDistance" + "\t");
		stringList.add("totalDuration" + "\t");
		stringList.add("noEmptiedContainers" + "\t");
		stringList.add("noOverflowedContainers" + "\t");
		stringList.add("noDumpsWP" + "\t");
		stringList.add("noDumpsSF" + "\t");
		stringList.add("avgFillContainers" + "\t");
		stringList.add("avgFillVehicles" + "\t");
		stringList.add("noVehiclesUsed" + "\t");
		stringList.add("avgExpFillContainers" + "\t");
		stringList.add("noContTooLate" + "\t");
		stringList.add("noContTooEarly" + "\t");
		stringList.add("noContOnTime" + "\t");
		stringList.add("experimentDuration" + "\t");
		stringList.add(newLine);
		
		// loop over entries in arrays
		for (int i = 0; i < totalDistancesSim.size(); i++) {
			stringList.add(Integer.toString(i) + "\t");
			stringList.add(Double.toString(totalDistancesSim.get(i)) + "\t");
			stringList.add(Double.toString(totalDurationsSim.get(i)) + "\t");
			stringList.add(Integer.toString(noEmptiedContainersSim.get(i)) + "\t");
			stringList.add(Integer.toString(noOverflowedContainersSim.get(i)) + "\t");
			stringList.add(Integer.toString(noDumpsWPSim.get(i)) + "\t");
			stringList.add(Integer.toString(noDumpsSFSim.get(i)) + "\t");
			stringList.add(Double.toString(avgFillContainersSim.get(i)) + "\t");
			stringList.add(Double.toString(avgFillVehiclesSim.get(i)) + "\t");
			stringList.add(Integer.toString(noVehiclesUsedSim.get(i)) + "\t");
			stringList.add(Double.toString(avgExpFillContainersSim.get(i)) + "\t");
			stringList.add(Integer.toString(noContTooLateSim.get(i)) + "\t");
			stringList.add(Integer.toString(noContTooEarlySim.get(i)) + "\t");
			stringList.add(Integer.toString(noContOnTimeSim.get(i)) + "\t");
			stringList.add(Long.toString(experimentDurationSim.get(i)) + "\t");
			stringList.add(newLine);
		}
		
		// add all input to results-string
		for (String s : stringList) {
			results += s;
		}
		
		String nameFileString = "outputSim.txt";
		
		writeResults(results, nameFileString);
		openOutputFile(nameFileString);
	}
	
	
	
	/*
	 * 		Given the results of an heuristic that assigns containers to days, this function evaluates the results and outputs an evaluation-form
	 */
	/*public static String evaluateDayAssignment(ArrayList<ArrayList<Cluster>> dayAssignment) {		
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("Day" + "\t");
		stringList.add("Cluster" + "\t");
		stringList.add("clusterCosts" + "\t");
		stringList.add("tooLate" + "\t");
		stringList.add("tooEarly"  + "\t");
		stringList.add("onTime"  + "\t");
		stringList.add("clusterLoad" + "\t");
		stringList.add(newLine);
		
		// loop over days
		for (int i = 0; i < dayAssignment.size(); i++) {
			// loop over clusters within day
			for (int j = 0; j < dayAssignment.get(i).size(); j++) {
				
				Cluster cluster = dayAssignment.get(i).get(j);
				
				// calculations
				double distanceToContainers = cluster.avgDistanceContainersToCentroid() * cluster.clusteredContainers.size() * 2;
				double distanceToWharf = cluster.distanceToCentroid(Data.wharfList.get(0)) * 2;
				
				
				double clusterCosts = distanceToContainers + distanceToWharf;
				int containersTooLate = cluster.emptyingTimingContainers().get(0);
				int containersTooEarly = cluster.emptyingTimingContainers().get(1);
				int containersOnTime = cluster.emptyingTimingContainers().get(2);
				double clusterLoad = cluster.clusterLoad;
				
				// add to stringList
				stringList.add(Integer.toString(i) + "\t");
				stringList.add(Integer.toString(cluster.clusterID) + "\t");
				stringList.add(Double.toString(clusterCosts) + "\t");
				stringList.add(Integer.toString(containersTooLate) + "\t");
				stringList.add(Integer.toString(containersTooEarly) + "\t");
				stringList.add(Integer.toString(containersOnTime) + "\t");	
				stringList.add(Double.toString(clusterLoad) + "\t");
				stringList.add(newLine);
				
			}
			
		}
		
		for (String s : stringList) {
			results += s;
		}
		
		return results;
		
	}
	*/
	
	public static String pointQGISVisualization(ArrayList<Container> containerList) {
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("id" + ";");
		stringList.add("WKT");
		stringList.add(newLine);
		
		// fill in data
		for (int i = 0; i < containerList.size(); i++) {
			Container tempContainer = containerList.get(i);
			
			stringList.add(Integer.toString(tempContainer.getIndexDistanceMatrix()) + ";");
			stringList.add("POINT (" + Double.toString(tempContainer.getLon()) + " "
					+ Double.toString(tempContainer.getLat()) + ")");
			stringList.add(newLine);
		}
		
		for (String s : stringList) {
			results += s;
		}
		
		return results;
	}
	
	
	/*
	 * 		Given the results of an heuristic that assigns containers to days, this function writes an evaluation-form in a QGIS appropriate format
	 */
	public static String clusterQGISVisualization(ArrayList<Cluster> clusterList) {
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("WKT" + ";");
		stringList.add("id" + ";");
		stringList.add("cluster_id");
		stringList.add(newLine);
		
		// loop over clusters within day
		for (int j = 0; j < clusterList.size(); j++) {
				
			// loop over containers within cluster
			for (int k = 0; k < clusterList.get(j).clusteredContainers.size(); k++) {
				Container tempContainer = clusterList.get(j).clusteredContainers.get(k);
				
				stringList.add("POINT (" + Double.toString(tempContainer.getLon()) + " "
						+ Double.toString(tempContainer.getLat()) + ")" + ";");
				stringList.add(Integer.toString(tempContainer.getIndexDistanceMatrix()) + ";");
				stringList.add(Integer.toString(j) + ";");
				stringList.add(newLine);
			}
		}			
		
		for (String s : stringList) {
			results += s;
		}
		
		return results;
	}
	
	public static String routeQGISVisualization(ArrayList<Route> routeList) {
		List<String> stringList = new ArrayList<String>();
		String newLine = System.lineSeparator();
		String results = "";
		
		// make headers for output
		stringList.add("ID;");
		stringList.add("WKT");
		stringList.add(newLine);
		
		for (int i = 0; i < routeList.size(); i++) {
			Route route = routeList.get(i);
			stringList.add(Integer.toString(i) + ";");
			stringList.add("POLYGON((");
			
			for (int j = 0; j < route.routingSequence.size(); j++) {
				stringList.add(Double.toString(route.routingSequence.get(j).getLon()) + " ");
				stringList.add(Double.toString(route.routingSequence.get(j).getLat()) + ",");
			}
			
			stringList.add("))");
			stringList.add(newLine);
		}
		
		for (String s : stringList) {
			results += s;
		}
		
		return results;
	}
	
	
	/*
	 * 		Opens the desired file
	 */
	public static void openOutputFile(String outputFileName) {
		File file = new File("C:/Users/WHeijnen/git/repository/WasteCollectionAmsterdam/" + outputFileName);
		if (!Desktop.isDesktopSupported()) {
			System.out.println("Desktop is not supported");
			return;
		}
		
		Desktop desktop = Desktop.getDesktop();
		if (file.exists())
			try {
				desktop.open(file);
			} catch (IOException e) {
				System.out.println("IOException occured at 'openOutputFile' in Results-class");
				e.printStackTrace();
			}
	}
	
}
