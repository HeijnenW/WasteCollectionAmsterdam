package heijnen.algorithms;

import java.util.ArrayList;
import heijnen.objects.Container;
import heijnen.planningObjects.Cluster;
import heijnen.simulation.Day;
import heijnen.simulation.ExperimentController;
import heijnen.simulation.SimulationController;
import heijnen.simulation.WriteResults;

public class DayAssignment {

	////		FIELDS			////
	public static ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
	
	
	//// 		FUNCTIONS		  ////
	
	
	/*
	 * 		Function that ensures that the containers that overflowed yesterday are selected for tomorrow
	 * 		to ensure solution quality, their locations should be taken into account when making clusters in the day assignment
	 * 		therefore we create initial clusters based on these (mandatory) containers. Containers that are close to each other 
	 * 		are put in the same cluster, others are given their own cluster. These clusters will then be expanded with other
	 * 		(optional) containers in the following step either based on (1) cluster centroids, (2) cluster population proximity,
	 * 		or (3) cheapest insertion.
	 */
	public static void clustersForOverflowedContainers(ArrayList<Container> overflowedContainers) {
		
		// initialize variables
		Cluster closestCluster = null;
		double distToClosestCluster = 999;
		double distToCluster = 999;
		
		// loop over all containers that were overflowed yesterday
		for (int i = 0; i < overflowedContainers.size(); i++) {
			Container container = overflowedContainers.get(i);
			
			// loop over all already existing clusters to see if container can be added somewhere
			for (int j = 0; j < clusterList.size(); j++) {
				Cluster cluster = clusterList.get(j);
				
				if ((cluster.clusterCapacity * 0.9) > cluster.expClusterLoad + container.expCurrFill) {
					
					cluster.margCostOfAddingContainer(container);
									
					if (distToCluster < distToClosestCluster) {
						distToClosestCluster = distToCluster;
						closestCluster = cluster;
					}
				}	
			}
			
			if (distToClosestCluster <= ExperimentController.addingNewClusterCostsEF) {				
				// add to cluster
				closestCluster.addContainer(container, true);
			} 
			else {
				// create new cluster
				int newClusterID = clusterList.size();
				int newClusterDay = Day.dayNr;
				Cluster newCluster = new Cluster(newClusterID, newClusterDay);
				clusterList.add(newCluster);
				newCluster.addContainer(container, true);
			}
			
			// reset values
			closestCluster = null;
			distToClosestCluster = 999;
			distToCluster = 999;
		}
		
		// TODO: temporary visual check
		// WriteResults.writeResults(WriteResults.clusterQGISVisualization(clusterList), "outputVisualization.txt");
	}
	
	
	public static ArrayList<ArrayList<Cluster>> cheapestInsertionClustering(ArrayList<Container> selectedContainers, int lengthPlanningHor) {
		
		// initialize relevant arrays
		ArrayList<ArrayList<Cluster>> dayAssignment = new ArrayList<ArrayList<Cluster>>();
		for (int i = 0; i < lengthPlanningHor; i++) {
			dayAssignment.add(new ArrayList<Cluster>());
		}
		
		// add clusters from overflowed containers to dayAssignment arrays
		for (int i = 0; i < clusterList.size(); i++) {
			dayAssignment.get(0).add(clusterList.get(i));
		}
		
		// make first x clusters of all days free and initialize them 
		for (int i = 0; i < dayAssignment.size(); i++) {
			for (int j = 0 ; j < selectedContainers.size(); j++) {
				if (dayAssignment.get(i).size() < 3) {
					Container container = selectedContainers.get(j);
					int arrayDay = Math.max(0, container.DED - Day.dayNr);
					if (arrayDay == i) {
						
						// check proximity to other free clusters
						double distClosestOtherFree = 999;
						for (int k = 0; k < dayAssignment.get(i).size(); k++) {
							double tempDist = dayAssignment.get(i).get(k).distCentroidToContainer(container);
							if (tempDist < distClosestOtherFree) {
								distClosestOtherFree = tempDist;
							}
						}
						
						if (distClosestOtherFree > ExperimentController.addingNewClusterCostsEF) {
							// add new cluster for free
							Cluster newCluster = new Cluster(clusterList.size(), i + Day.dayNr);
							clusterList.add(newCluster);
							dayAssignment.get(i).add(newCluster);
							
							newCluster.addContainer(container, true);
							selectedContainers.remove(container);
						}					
					}
				}
				else {
					break;
				}
			}
		}
		
		// initialize variables
		Container ciContainer = null;
		Cluster ciCluster = null;
		double ciCosts = 999;
		Container tempContainer = null;
		Cluster tempCluster = null;
		double tempCosts;
		boolean addNewCluster = false;
		
		int noUnassignedContainers = selectedContainers.size();
		
		// repeat all steps for each possible container
		for (int i = 0; i < noUnassignedContainers; i++) {
			
			// evaluate cheapest insertion for each remaining container
			for (int j = 0; j < selectedContainers.size(); j++) {
				tempContainer = selectedContainers.get(j);
				
				// evaluate costs for adding to each cluster
				for (int k = 0; k < clusterList.size(); k++) {
					tempCluster = clusterList.get(k);
					
					// only consider cluster if cluster is not yet full
					if ((tempCluster.clusterCapacity * 0.9) >= tempCluster.expClusterLoad + tempContainer.expectedLoadStartDay(tempCluster.dayNr)) {
						
						// calculate marginal distance costs
						double margDistanceCosts = tempCluster.margCostOfAddingContainer(tempContainer);
						
						// calculate penalty costs
						double penaltyCosts = tempContainer.calcPenalty(tempCluster.dayNr, margDistanceCosts);

						// calculate costs of insertion of this container into this cluster
						tempCosts = margDistanceCosts + penaltyCosts;
						
						// compare with currently cheapest insertion
						if (tempCosts < ciCosts) {
							ciContainer = tempContainer;
							ciCluster = tempCluster;
							ciCosts = tempCosts;
							addNewCluster = false;
						}
					}	
				}

				tempCosts = ExperimentController.addingNewClusterCostsEF;
				
				if (tempCosts < ciCosts) {
					ciContainer = tempContainer;
					ciCosts = tempCosts;
					addNewCluster = true;
				}
				
			}
			
			// perform cheapest insertion
			if (addNewCluster == false) {
				
			/*	// TODO: temporary tracking of marginal vs. incremental costs
				SimulationController.noObs++;
				double margCosts = ciCluster.margCostOfAddingContainer(ciContainer);
				double incrCosts = ciContainer.calcPenalty(ciCluster.dayNr, ciCluster.margCostOfAddingContainer(ciContainer));
				
				
				SimulationController.totMargCosts += ciCluster.margCostOfAddingContainer(ciContainer);
				
				if (ciCluster.dayNr > ciContainer.DED) {
					SimulationController.totLateCosts += ciContainer.calcPenalty(ciCluster.dayNr, margCosts);
				}
				else if (ciCluster.dayNr < ciContainer.DED) {
					SimulationController.totEarlyCosts += ciContainer.calcPenalty(ciCluster.dayNr, margCosts);
				}
			*/
					
				ciCluster.addContainer(ciContainer, true);
				
			}
			else if (addNewCluster == true) {
				int newClusterID = clusterList.size();
				int newClusterDay = Math.max(ciContainer.DED, Day.dayNr);
				
				Cluster newCluster = new Cluster(newClusterID, newClusterDay);
				clusterList.add(newCluster);
				int arrayDay = newCluster.dayNr - Day.dayNr;
				dayAssignment.get(arrayDay).add(newCluster);
				
				newCluster.addContainer(ciContainer, true);
			}
			
			// after insertion, re-initialize variables
			selectedContainers.remove(ciContainer);
			ciContainer = null;
			ciCluster = null;
			ciCosts = 999;
			addNewCluster = false;
		}
		
		
		// fill in dayAssignment array based on clusterList
		for (int i = 0; i < clusterList.size(); i++) {
			clusterList.get(i).updateClusterCostApproximations();
		}
				
		return dayAssignment;
	}
	
	
	/*
	 * 		function that applies local search techniques to improve the dayAssignment
	 */
	public static ArrayList<ArrayList<Cluster>> localSearchImprDayAssignment(ArrayList<ArrayList<Cluster>> dayAssignment) {
		
		// temporary test
		/*
		int deniedChanges = 0;
		long startTime = System.nanoTime();
		double totalIncrBef = 0;
		double totalTravBef = 0;
		*/
		
		// fill cluster list
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
		for (int i = 0; i < dayAssignment.size(); i++) {
			for (int j = 0; j < dayAssignment.get(i).size(); j++) {
				clusterList.add(dayAssignment.get(i).get(j));
			}
		}
		
		// update all cluster costs
		for (int i = 0; i < clusterList.size(); i++) {
			clusterList.get(i).updateClusterCostApproximations();
			//totalIncrBef += clusterList.get(i).currIncrCostApprox;		// used to evaluate local search
			//totalTravBef += clusterList.get(i).currTravelCostApprox;		// used to evaluate local search
		}
		
		// check if there are even two clusters to swap between, otherwise you should do nothing
		if (clusterList.size() > 1) {
			
			// try x iterations
			for (int i = 0; i < 1000; i++) {
				int rnd1 = 999;
				int rnd2 = 999;
				int rnd3 = 999;
				int rnd4 = 999;
				
				int rndOperator = SimulationController.rgen.nextInt(2);
				
				// pick two random clusters
				rnd1 = SimulationController.rgen.nextInt(clusterList.size());
				rnd2 = SimulationController.rgen.nextInt(clusterList.size());
				while (rnd1 == rnd2) {
					rnd2 = SimulationController.rgen.nextInt(clusterList.size());
				}
				Cluster clusterA = clusterList.get(rnd1);
				Cluster clusterB = clusterList.get(rnd2);
				
				if ((clusterA.clusteredContainers.size() != 0) && (clusterB.clusteredContainers.size() != 0)) {
					// pick two random containers from those clusters
					rnd3 = SimulationController.rgen.nextInt(clusterA.clusteredContainers.size());
					rnd4 = SimulationController.rgen.nextInt(clusterB.clusteredContainers.size());
					Container container1 = clusterA.clusteredContainers.get(rnd3);
					Container container2 = clusterB.clusteredContainers.get(rnd4);
					
					// record current cluster costs
					double costsBefore = clusterA.currIncrCostApprox + clusterA.currTravelCostApprox +
							clusterB.currIncrCostApprox + clusterB.currTravelCostApprox;
					
					// call local search swap operation
					if (rndOperator == 0) {
						LocalSearch.clusterTwoInterchange(clusterA, clusterB, container1, container2);
					}
					else if (rndOperator == 1) {
						LocalSearch.clusterOneMove(clusterA, clusterB, container1);
					}
					else {
						System.out.println("DEBUG: niet de bedoeling");
					}
					
					
					// check if beneficial
					double costsAfter = clusterA.currIncrCostApprox + clusterA.currTravelCostApprox +
							clusterB.currIncrCostApprox + clusterB.currTravelCostApprox;
					
					if (costsBefore < costsAfter) {			// correct change if no improvement was found
						if (rndOperator == 0) {
							LocalSearch.clusterTwoInterchange(clusterA, clusterB, container2, container1);
						}
						else if (rndOperator == 1) {
							LocalSearch.clusterOneMove(clusterB, clusterA, container1);
						}
						//deniedChanges++;		// used to evaluate local search
					}
				}
				else {
					// do nothing
				}
			}
		}
		
		// re-transform the cluster-list into a day assignment
		ArrayList<ArrayList<Cluster>> improvedDayAssignment = new ArrayList<ArrayList<Cluster>>();
		for (int i = 0; i < ExperimentController.lengthPlanningHorizonEF; i++) {
			improvedDayAssignment.add(new ArrayList<Cluster>());
		}
		
		for (int i = 0; i < clusterList.size(); i++) {
			Cluster cluster = clusterList.get(i);
			if (cluster.clusteredContainers.size() > 0) {
				int arrayDay = cluster.dayNr - Day.dayNr;
				improvedDayAssignment.get(arrayDay).add(cluster);
				cluster.updateClusterCostApproximations();
			}
		} 
		
		
		//EVALUATING LOCAL SEARCH PERFORMANCE
		/*long endTime = System.nanoTime();
		
		double totalIncrAft = 0;
		double totalTravAft = 0;
		
		for (int i = 0; i < clusterList.size(); i++) {
			clusterList.get(i).updateClusterCostApproximations();
			
			totalIncrAft += clusterList.get(i).currIncrCostApprox;
			totalTravAft += clusterList.get(i).currTravelCostApprox;
			
			if (Double.isNaN(totalTravAft)) {
				System.out.print("");
			}
			
			clusterList.get(i).updateClusterCostApproximations();
		}
		
		double imprIncr = totalIncrBef - totalIncrAft;
		double imprTrav = totalTravBef - totalTravAft;
		
		double deniedPerc = (double) deniedChanges / 1000;
		double imprPerc = ((imprIncr + imprTrav) / (totalIncrBef + totalTravBef)) * 100;
		
		SimulationController.noImprovs++;
		SimulationController.totDeniedPerc += deniedPerc;
		SimulationController.totImprovementPerc += imprPerc;*/
		
		return improvedDayAssignment;
	}
}

