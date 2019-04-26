package heijnen.algorithms;

import java.util.ArrayList;
import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.objects.Container;
import heijnen.objects.Point;
import heijnen.objects.Wharf;
import heijnen.planningObjects.Cluster;
import heijnen.planningObjects.Route;
import heijnen.simulation.ExperimentController;
import heijnen.simulation.WriteResults;

public class RouteConstruction {

	////		FIELDS			////
	
	
	//// 		FUNCTIONS		 ////

	/*
	 * 		Implementation of nearest insertion heuristic that can be used for both 'using' and 'disregarding' clusters
	 */
	public static ArrayList<Route> nearestInsertionHeuristic(ArrayList<Cluster> clusterList) {
		
		ArrayList<Route> routeList = new ArrayList<Route>();
		
		if (ExperimentController.usingClustersEF == true) {
			
			// for each cluster, make a route from wharf -> wharf
			for (int i = 0; i < clusterList.size(); i++) {
				Route route = usingCluster(clusterList.get(i));
				routeList.add(route);
			}
			
			// from this route list, decide which routes start and end where, i.e. choose dump locations
			routeList = assembleRoutes(routeList);
			
		}
		else if (ExperimentController.usingClustersEF == false) {
			
			// gather all containers in one container list
			ArrayList<Container> containerList = new ArrayList<Container>();
			for (int i = 0; i < clusterList.size(); i++) {
				for (int j = 0; j < clusterList.get(i).clusteredContainers.size(); j++) {
					containerList.add(clusterList.get(i).clusteredContainers.get(j));
				}
			}
			
			// apply routing heuristic on this container list
			routeList.addAll(disregardingClusters(containerList));
			
		}
		else {
			System.out.println("DEBUG: geen keuze gemaakt om clusters te gebruiken of niet.");
		}
		
		
		return routeList;
	}
	
	
	/*
	 * 		Constructs one route (wharf -> wharf) from one cluster
	 */
	public static Route usingCluster(Cluster cluster) {
		
		Route route = new Route();
		
		// group all containers in a list
		ArrayList<Container> unassignedContainers = new ArrayList<Container>();
		for (int i = 0; i < cluster.clusteredContainers.size(); i++) {
			unassignedContainers.add(cluster.clusteredContainers.get(i));
		}
		
		int noUnassignedContainers = unassignedContainers.size();
		
		for (int i = 0; i < noUnassignedContainers; i++) {
			
			Container niContainer = null;
			int niIndex = 999;
			double niCosts = 999;
			
			for (int j = 0; j < unassignedContainers.size(); j++) {
				
				for (int k = 1; k < route.routingSequence.size(); k++) {
					
					double iCosts = route.evaluateAddingPoint(k, unassignedContainers.get(j));			// iCosts = insertion costs
					if (iCosts < niCosts) {
						niContainer = unassignedContainers.get(j);
						niIndex = k;
						niCosts = iCosts;
					}	
				}
			}
			
			// perform nearest insertion
			route.addPointToRoute(niIndex, niContainer);
			unassignedContainers.remove(niContainer);
		}
		
		// choose dump location
		double addWPCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.wasteProcessorList.get(0));
		double addSFCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.transshipmentHubList.get(0)) + Parameters.transShipmentCosts;
		
		if (addSFCosts < addWPCosts) {
			route.addPointToRoute(route.routingSequence.size() - 1, Data.transshipmentHubList.get(0));
			//route.indexDumpLocations.add(route.routingSequence.size() - 1);
		}
		else if (addWPCosts < addSFCosts) {
			route.addPointToRoute(route.routingSequence.size() - 1, Data.wasteProcessorList.get(0));
			//route.indexDumpLocations.add(route.routingSequence.size() - 1);
		}
		
		return route;
	}
	
	
	/*
	 * 		Constructs x routes based on a list of containers
	 */
	public static ArrayList<Route> disregardingClusters(ArrayList<Container> containerList) {
		
		ArrayList<Route> routeList = new ArrayList<Route>();
		ArrayList<Container> unassignedContainers = new ArrayList<Container>(containerList);

		
		while (unassignedContainers.size() > 0) {
			
			Route route = new Route();
			routeList.add(route);
			double tempExpRouteLoad = 0;		// use because expRouteLoad does not account for emptying in the middle
			boolean routeFinished = false;
			
			int minAddIndex = 1;
			
			int noUnassignedContainers = unassignedContainers.size();
			
			for (int i = 0; i < noUnassignedContainers; i++) {
				
				boolean foundContainer = false;
				Container niContainer = null;
				int niIndex = 999;
				double niCosts = 999;
				
				for (int j = 0; j < unassignedContainers.size(); j++) {
					Container container = unassignedContainers.get(j);
					
					for (int k = minAddIndex; k < route.routingSequence.size(); k++) {
						
						double iCosts = route.evaluateAddingPoint(k, container);			// iCosts = insertion costs
						if ((iCosts < niCosts) && (tempExpRouteLoad + container.expCurrFill <= Parameters.vehicleCapacity * (1-ExperimentController.bufferVehiclesEF)) 
								&& (route.routeDuration < Parameters.workingHours - Parameters.timeBufferTotalRoute)) {
							foundContainer = true;
							niContainer = unassignedContainers.get(j);
							niIndex = k;
							niCosts = iCosts;
						}
						
					}
					
				}
				
				if (foundContainer == true) {							// a container is found and can be added to the routing sequence
					// perform nearest insertion
					route.addPointToRoute(niIndex, niContainer);
					tempExpRouteLoad += niContainer.expCurrFill;
					unassignedContainers.remove(niContainer);
				}
				else if (foundContainer == false) {						// no feasible containers could be found, time to add a dump location
					// choose dump location
					double addWPCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.wasteProcessorList.get(0));
					double addSFCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.transshipmentHubList.get(0)) + Parameters.transShipmentCosts;
					
					if (addSFCosts < addWPCosts) {
						route.addPointToRoute(route.routingSequence.size() - 1, Data.transshipmentHubList.get(0));
						route.indexDumpLocations.add(route.routingSequence.size() - 1);
					}
					else if (addWPCosts <= addSFCosts) {
						route.addPointToRoute(route.routingSequence.size() - 1, Data.wasteProcessorList.get(0));
						route.indexDumpLocations.add(route.routingSequence.size() - 1);
					}
					minAddIndex = route.routingSequence.size() - 2 + 1; 
					i--;																// as no container is chosen from the list
					tempExpRouteLoad = 0;
					
					if (Parameters.workingHours - route.routeDuration < 1) {			// TODO: hard-coded algorithm setting
						routeFinished = true;
						break;		// if time left for route is too small, do not bother adding a very small tour, but restart with another vehicle
					}
				}
				
			}
			
			// add last dump location to end of route
			if (routeFinished == false) {
				double addWPCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.wasteProcessorList.get(0));
				double addSFCosts = route.evaluateAddingPoint(route.routingSequence.size() - 2, Data.transshipmentHubList.get(0)) + Parameters.transShipmentCosts;
				
				if (addSFCosts < addWPCosts) {
					route.addPointToRoute(route.routingSequence.size() - 1, Data.transshipmentHubList.get(0));
					route.indexDumpLocations.add(route.routingSequence.size() - 1);
				}
				else if (addWPCosts <= addSFCosts) {
					route.addPointToRoute(route.routingSequence.size() - 1, Data.wasteProcessorList.get(0));
					route.indexDumpLocations.add(route.routingSequence.size() - 1);
				}
			}
		}
		
		return routeList;		
	}
	
	
	/*
	 * 		Combines initial routes (wharf -> wharf) into less routes that can be performed consecutively
	 */
	public static ArrayList<Route> assembleRoutes(ArrayList<Route> initialRouteList) {
		
		ArrayList<Route> unassembledRoutes = new ArrayList<Route>(initialRouteList);
		ArrayList<ArrayList<Route>> routeCombinations = new ArrayList<ArrayList<Route>>();
		ArrayList<Route> assembledRouteList = new ArrayList<Route>();
		
		int noRoutes = unassembledRoutes.size();
		
		for (int i = 0; i < noRoutes; i++) {
			
			Route longestRoute = null;
			double lengthLongestRoute = 0;
			
			boolean newComb = false;
			
			for (int j = 0; j < unassembledRoutes.size(); j++) {
				
				if (unassembledRoutes.get(j).routeDuration > lengthLongestRoute) {
					longestRoute = unassembledRoutes.get(j);
					lengthLongestRoute = unassembledRoutes.get(j).routeDuration;
				}
			}
			
			// fit the longest route into the first possible combination, if not possible, add another combination
			for (int j = 0; j < routeCombinations.size(); j++) {
				
				double totalRouteCombLength = 0;
				for (int k = 0; k < routeCombinations.get(j).size(); k++) {
					totalRouteCombLength += routeCombinations.get(j).get(k).routeDuration;
				}
				
				if (totalRouteCombLength + lengthLongestRoute < Parameters.workingHours) {
					newComb = false;
					routeCombinations.get(j).add(longestRoute);
					unassembledRoutes.remove(longestRoute);
				}
				else {
					newComb = true;
				}
			}
			
			if ((newComb == true) || (routeCombinations.size() == 0)){
				routeCombinations.add(new ArrayList<Route>());
				routeCombinations.get(routeCombinations.size() - 1).add(longestRoute);
				unassembledRoutes.remove(longestRoute);
			}
		}
		
		////	route combinations are now made, within these combinations, sequences still have to be decided and performed	////
		
		for (int i = 0; i < routeCombinations.size(); i++) {
			
			Route routeClosestToWharf = null;
			double closestDistToWharf = 999;
			
			// find route with start closest to wharf
			for (int j = 0; j < routeCombinations.get(i).size(); j++) {
				
				Route tempRoute = routeCombinations.get(i).get(j);
				
				double tempRouteDistToWharf = tempRoute.routingSequence.get(1).distanceToPoint(Data.wharfList.get(0));
				if (tempRouteDistToWharf < closestDistToWharf) {
					routeClosestToWharf = tempRoute;
					closestDistToWharf = tempRouteDistToWharf;
				}
			}
			
			routeCombinations.get(i).remove(routeClosestToWharf);
			routeCombinations.get(i).add(0, routeClosestToWharf);
			
			// route combination array is now in the order in which we want to drive them, only thing to do is to remove unnecessary wharfs inbetween
			Route newRoute = new Route();
			assembledRouteList.add(newRoute);
			
			int indexCounter = 1;
			
			for (int j = 0; j < routeCombinations.get(i).size(); j++) {
				
				for (int k = 1; k < routeCombinations.get(i).get(j).routingSequence.size() - 1; k++) {
									
					newRoute.addPointToRoute(indexCounter, routeCombinations.get(i).get(j).routingSequence.get(k));
					
					if ((routeCombinations.get(i).get(j).routingSequence.get(k).equals(Data.wasteProcessorList.get(0))) ||
						(routeCombinations.get(i).get(j).routingSequence.get(k).equals(Data.transshipmentHubList.get(0)))) {
						newRoute.indexDumpLocations.add(indexCounter);
					}
					
					indexCounter++;
					
				}
				
			}
			
		}
			
		return assembledRouteList;
	}
	
	
	
	
}

