package heijnen.algorithms;

import java.util.ArrayList;

import heijnen.objects.Container;
import heijnen.objects.Point;
import heijnen.planningObjects.Cluster;
import heijnen.planningObjects.Route;
import heijnen.simulation.Day;
import heijnen.simulation.WriteResults;

public class LocalSearch {

	
	/*
	 * 		Swaps two containers from different clusters
	 */
	public static void clusterTwoInterchange(Cluster clusterA, Cluster clusterB, Container container1, Container container2) {
		
		// first check feasibility of move
		boolean feasible = true;
		
		// check overflowed container feasibility
		if ((container1.overflowed == true) && (clusterB.dayNr != Day.dayNr)) {					// if container1 is overflowed, it can only be switched to a cluster on the current day
			feasible = false;
		}	
		else if	((container2.overflowed == true) && (clusterA.dayNr != Day.dayNr)) {			// if container2 is overflowed, it can only be switched to a cluster on the current day
			feasible = false;
		}
		else {
			feasible = true;
		}
		
		// check capacity feasibility
		if (feasible == true) {
			if (((clusterA.expClusterLoad - container1.expectedLoadStartDay(clusterA.dayNr) + container2.expectedLoadStartDay(clusterA.dayNr)) <= clusterA.clusterCapacity) &&
						((clusterB.expClusterLoad - container2.expectedLoadStartDay(clusterB.dayNr) + container1.expectedLoadStartDay(clusterB.dayNr)) <= clusterB.clusterCapacity)) {
				feasible = true;
			}
			else {
				feasible = false;
			}
		}
			
		if (feasible == true) {
			clusterA.removeContainer(container1, true);
			clusterB.removeContainer(container2, true);
			
			clusterA.addContainer(container2, true);
			clusterB.addContainer(container1, true);
			
			clusterA.updateClusterCostApproximations();
			clusterB.updateClusterCostApproximations();
		}
	}
	
	/*
	 * 		Swaps a container from one cluster to another
	 */
	public static void clusterOneMove(Cluster donorCluster, Cluster receivingCluster, Container container) {
		
		// TODO: feasibility checks
		boolean feasible = true;
		
		// check number of containers in both clusters
		if (donorCluster.clusteredContainers.size() < 1) {
			feasible = false;
		}
		
		// check cluster capacity
		if (receivingCluster.clusterCapacity < (receivingCluster.expClusterLoad + container.expectedLoadStartDay(receivingCluster.dayNr))) {
			feasible = false;
		}
		
		// check if container is overflowed
		if ((container.overflowed == true) && (receivingCluster.dayNr != Day.dayNr)) {
			feasible = false;
		}
		
		// if move is still feasible, evaluate and (maybe) perform move
		if (feasible == true) {
			
			donorCluster.removeContainer(container, true);
			receivingCluster.addContainer(container, true);
			
			donorCluster.updateClusterCostApproximations();
			receivingCluster.updateClusterCostApproximations();
			
		}


	}
	
	
	/*
	 * 		2-opt implementation
	 */
	public static Route twoOptImplementation(Route route) {
	
		// TODO: temporary
		//ArrayList<Route> routeList = new ArrayList<Route>();
		//routeList.add(route);
		//WriteResults.writeResults(WriteResults.routeQGISVisualization(routeList), "routeQGISVis.txt");
		
		// loop over all tours (between dump locations)
		for (int tour = 0; tour < route.indexDumpLocations.size(); tour++) {

			int beginTour;
			int endTour;
			
			if (tour == 0) {												// first tour of the route
				beginTour = 0;
				endTour = route.indexDumpLocations.get(0);
			}
			else if (tour == (route.indexDumpLocations.size() - 1)) {		// last tour of the route
				beginTour = route.indexDumpLocations.get(route.indexDumpLocations.size() - 2);
				endTour = route.routingSequence.size() - 2;
			}
			else {															// middle tour of the route
				beginTour = route.indexDumpLocations.get(tour - 1);
				endTour = route.indexDumpLocations.get(tour);
			}

			ArrayList<Point> tourSequence = new ArrayList<Point>();
			for (int i = beginTour; i <= endTour; i++) {
				tourSequence.add(route.routingSequence.get(i));
			}
			double costTourSequence = evaluateTour(tourSequence);
			
			boolean continueLoop = true;
			
			while (continueLoop == true) {
				
				continueLoop = false;
				
				for (int i = beginTour + 1; i <= endTour - 2; i++) {
					for (int j = i + 1; j <= endTour - 1; j++) {
						
						// 2 opt swap between i and j
						ArrayList<Point> newTourSequence = new ArrayList<Point>();
						
						// add route[0] to route[i-1]
						for (int k = 0; k < i - beginTour; k++) {
							newTourSequence.add(tourSequence.get(k));
						}
						
						// add route[i] to route[j] in reverse order
						for (int k = j - beginTour; k >= i - beginTour; k--) {
							newTourSequence.add(tourSequence.get(k));
						}
						
						// add route[j+1] to end
						for (int k = j + 1 - beginTour; k < tourSequence.size(); k++) {
							newTourSequence.add(tourSequence.get(k));
						}
						
						double costNewTourSequence = evaluateTour(newTourSequence);
						
						// if improvement, else nothing
						if (costNewTourSequence < costTourSequence) {
							tourSequence = newTourSequence;
							costTourSequence = costNewTourSequence;
							continueLoop = true;
						}
					}
				}
			}
			
			// replace old sequence with new k-optimal sequence
			for (int i = beginTour; i <= endTour; i++) {
				route.routingSequence.set(i, tourSequence.get(i - beginTour));
			}
			route.updateRouteCosts();
		}
		
		// TODO: temporary
		//WriteResults.writeResults(WriteResults.routeQGISVisualization(routeList), "routeQGISVis.txt");
		
		return route;
	}

	
	public static double evaluateTour(ArrayList<Point> tourSequence) {
		double distanceCosts = 0;
		for (int i = 0; i < tourSequence.size() - 1; i++) {
			distanceCosts += tourSequence.get(i).distanceToPoint(tourSequence.get(i+1));
		}
		return distanceCosts;
	}
	
}
