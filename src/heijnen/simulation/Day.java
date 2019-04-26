package heijnen.simulation;

import java.util.ArrayList;

import heijnen.algorithms.ContainerSelection;
import heijnen.algorithms.DayAssignment;
import heijnen.algorithms.KMeansClustering;
import heijnen.algorithms.LocalSearch;
import heijnen.algorithms.RouteConstruction;
import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.main.Main;
import heijnen.objects.Container;
import heijnen.objects.TransshipmentHub;
import heijnen.objects.WasteProcessor;
import heijnen.objects.Wharf;
import heijnen.planningObjects.Cluster;
import heijnen.planningObjects.Route;

/*
 * 		Static class (does not need to be instantiated), that is used to simulate a day
 */

public class Day {
	
	////	 	FIELDS			////
	
	// day settings
	public static int dayNr = ExperimentController.currDay;
	
	// storage lists
	public static ArrayList<Container> selectedContainers = new ArrayList<Container>();
	public static ArrayList<Container> overflowedContainersYesterday = new ArrayList<Container>();
	public static ArrayList<ArrayList<Cluster>> dayAssignment = new ArrayList<ArrayList<Cluster>>();
	public static ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
	public static ArrayList<Route> plannedRouteList = new ArrayList<Route>();
	public static ArrayList<Route> actualRouteList = new ArrayList<Route>();
	
	// daily performance indicators
	public static double totalDistance;
	public static double totalDuration;
	public static int noEmptyings;
	public static int noOverflows;
	public static int noDumpsWP;
	public static int noDumpsSF;
	public static double avgFillContainers;
	public static double avgFillVehicles;
	public static int vehiclesUsed;
	public static double avgExpFillContainers;
	
	public static int noContTooLate;
	public static int noContTooEarly;
	public static int noContOnTime;
	

	
	//// 		FUNCTIONS		 ////
		
	/*
	 * 		Framework in which all steps that are taken in a day are formulated. 
	 */
	public static void simDay() {
		
		// initialize day
		initDay();
		
		
		////		   PHASE I			  ////
		////	 Container selection	  ////
				
		// select containers to be selected
		selectedContainers = ContainerSelection.selectionBasedOnDED(ExperimentController.lengthPlanningHorizonEF);
		
		
		////		  PHASE II	     	////
		////	   Day assignment		////
		
		// assign containers to days
		DayAssignment.clustersForOverflowedContainers(overflowedContainersYesterday);
		dayAssignment = DayAssignment.cheapestInsertionClustering(selectedContainers, ExperimentController.lengthPlanningHorizonEF);
		
		// apply k-means algorithm to first day of day assignment
		ArrayList<Cluster> preprocessedClusters = new ArrayList<Cluster>(KMeansClustering.basedOnClusters(dayAssignment.get(0)));
		dayAssignment.get(0).clear();
		dayAssignment.add(0, preprocessedClusters);
		
		// apply local improvement on total day assignment
		dayAssignment = DayAssignment.localSearchImprDayAssignment(dayAssignment);
			
			
			
		////		PHASE III		  ////
		////	Route construction	  ////
			
		// construct routes for first day
		plannedRouteList = RouteConstruction.nearestInsertionHeuristic(dayAssignment.get(0));		
			
		// route improvement with local search (using 2-opt)
		for (int i = 0; i < plannedRouteList.size(); i++) {
			Route newRoute = LocalSearch.twoOptImplementation(plannedRouteList.get(i));
			plannedRouteList.set(i, newRoute);
		}
			
			
			
		////	 	 PHASE IV		     ////
		////	 Online rescheduling     ////
			
		// execute and evaluate planning
		actualRouteList = executeRoutes(plannedRouteList);		// only first day is actually executed
		evaluateActualRoutes(actualRouteList);
			
		// empty emptied containers
		int noEmptiedOverflowedCont = 0;
		for (int i = 0; i < actualRouteList.size(); i++) {
			for (int j = 0; j < actualRouteList.get(i).containersInRoute.size(); j++) {
				Container container = actualRouteList.get(i).containersInRoute.get(j);
				
				if (container.overflowed == true) {
					noEmptiedOverflowedCont++;
				}
				else if (dayNr > container.DED) {		// too late
					noContTooLate++;
				}
				else if (dayNr < container.DED) {		// too early
					noContTooEarly++;
				}
				else {									// on-time
					noContOnTime++;
				}
				
				actualRouteList.get(i).containersInRoute.get(j).emptyContainer();
			}
		}
			

		// check if actualRouteList contains containers multiple times
		for (int i = 0; i < actualRouteList.size(); i++) {
			for (int j = 0; j < actualRouteList.get(i).containersInRoute.size(); j++) {
				Container container = actualRouteList.get(i).containersInRoute.get(j);
				int containerCount = 0;
				
				for (int k = 0; k < actualRouteList.get(i).containersInRoute.size(); k++) {
					if (actualRouteList.get(i).containersInRoute.get(k).equals(container)) {
						containerCount++;
					}
				}
				if (containerCount != 1) {
					System.out.println("DEBUG: containerCount > 1 betekend dat dezelfde container vaker in de actualRoute zit");
				}
			}
		}
		
		// no emptied containers should equal all overflowed containers yesterday
		if (noEmptiedOverflowedCont != overflowedContainersYesterday.size()) {
			System.out.println("DEBUG: niet alle overstroomde containers van gisteren zijn geleegd");
		}
		
		
		// reset overflowed containers
		overflowedContainersYesterday.clear();
			
		// fill containers with randomly generated deposits
		for (int i = 0; i < Data.containerList.size(); i++) {
			Container container = Data.containerList.get(i);
			container.dailyDepositUpdate();
			if (ExperimentController.sensorsEF == true) {
				container.dailyUpdateSensorReadings();
			}
		}
				
		// summarize and export results of the day
		if (dayNr % 25 == 0) {
			long currTime = System.nanoTime();
			long runTime = ((currTime - Main.startTime)/1000000000);
			System.out.println("Progress: Experiment " + (ExperimentController.currExpNr+1) + "/" + SimulationController.nrOfExp + ", replication: " + (ExperimentController.currRepl+1) +"/" + 
					Parameters.experimentNoReplications + ", day: " + dayNr + "/" + Parameters.experimentTotalLength + ", total time: " + runTime + " seconds");
		}
	
		
		// save gathered results in array if replication is out of the warm-up period
		if (dayNr >= Parameters.experimentWarmupPeriod) {
			WriteResults.saveDayResults();
		}
	}
	
	
	public static void initDay() {
		// initialize day
		dayNr = ExperimentController.currDay;
		overflowedContainersYesterday.addAll(ExperimentController.overflowedContainers);		// ExpContr.overflowedContainers is reset per container when they are emptied
		
		DayAssignment.clusterList.clear();
		clusterList.clear();
		selectedContainers.clear();
		dayAssignment.clear();
		plannedRouteList.clear();
		actualRouteList.clear();
		
		totalDistance     = 0;
		totalDuration     = 0;
		noEmptyings       = 0;
		noOverflows       = 0;
		noDumpsWP         = 0;
		noDumpsSF         = 0;
		avgFillContainers = 0;
		avgFillVehicles   = 0;
		vehiclesUsed      = 0;
		avgExpFillContainers = 0;
		
		noContTooLate = 0;
		noContTooEarly = 0;
		noContOnTime = 0;
	}
	
	/*
	 * 		Execute initially constructed routes. Possibly, new routes have to be constructed to prevent
	 * 		overflowing containers.
	 */
	public static ArrayList<Route> executeRoutes(ArrayList<Route> routeList) {
		
		// TODO: dynamic alteration of routes!
		
		if (ExperimentController.onlineReschedulingTechniqueEF.equals("passive")) {
			return routeList;
		}
		
		else if (ExperimentController.onlineReschedulingTechniqueEF.equals("intra-route")) {			
			return routeList;
		}
		
		else if (ExperimentController.onlineReschedulingTechniqueEF.equals("inter-route")) {
			return routeList;
		}
		
		else if (ExperimentController.onlineReschedulingTechniqueEF.equals("expResults")) {
			return routeList;
		}
					
		else {
			System.out.println("WARNING, geen valide online planningslevel gekozen");	
			return routeList;
		}
		
	}

	
	/*
	 * 		Evaluate the performance of the routes that are actually performed at the end of the day
	 */
	public static void evaluateActualRoutes(ArrayList<Route> routeList) {
		
		double totalFillLevelContainers = 0;
		double totalCapacityContainers = 0;
		double totalExpFillLevelContainers = 0;
		
		// loop over all routes that are executed this day
		for (int i = 0; i < routeList.size(); i++) {
			Route route = routeList.get(i);
			
			totalDistance += route.routeDistance;
			totalDuration += route.routeDuration;
			noEmptyings += route.containersInRoute.size();
			
			// loop over the entire sequence to find all WPs and SFs
			for (int j = 0; j < route.routingSequence.size(); j++) {
				if (route.routingSequence.get(j) instanceof TransshipmentHub) {
					noDumpsSF++;
				}
				else if (route.routingSequence.get(j) instanceof WasteProcessor) {
					noDumpsWP++;
				}
			}
			
			// loop over all containers to find out avg fill levels
			for (int j = 0; j < route.containersInRoute.size(); j++) {
				totalFillLevelContainers += route.containersInRoute.get(j).currFill;
				totalExpFillLevelContainers += route.containersInRoute.get(j).expCurrFill;
				totalCapacityContainers  += route.containersInRoute.get(j).capacity;
			}
		}
		
		vehiclesUsed = routeList.size();
		
		// calculate average fill levels
		avgFillContainers = totalFillLevelContainers / totalCapacityContainers;
		avgFillVehicles = (totalFillLevelContainers / (noDumpsWP + noDumpsSF)) / Parameters.vehicleCapacity;
		avgExpFillContainers = totalExpFillLevelContainers / totalCapacityContainers;
	}
}
