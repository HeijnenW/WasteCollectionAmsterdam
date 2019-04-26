package heijnen.algorithms;

import java.util.ArrayList;

import heijnen.data.Parameters;
import heijnen.objects.Container;
import heijnen.planningObjects.Cluster;
import heijnen.simulation.ExperimentController;
import heijnen.simulation.WriteResults;

public class KMeansClustering {

	
	public static ArrayList<Cluster> basedOnClusters(ArrayList<Cluster> inputClusters) {
		
		// TODO: temp
		//WriteResults.writeResults(WriteResults.clusterQGISVisualization(inputClusters), "XinputClusters.txt");
		
		// determine k
		int k = 0;
		double totalLoad = 0;
		for (int i = 0; i < inputClusters.size(); i++) {
			totalLoad += inputClusters.get(i).expClusterLoad;
		}
		k = (int) Math.ceil((totalLoad*1.05) / (Parameters.vehicleCapacity * (1-ExperimentController.bufferVehiclesEF)));
		
		// occurs when AOP is so low, you even want to empty on the first day, with expFill = 0 of containers
		if ((k == 0) && (inputClusters.isEmpty() == false)) {
			k = 1;
		}
		
		double totalLoadTwo = 0;
		
		// save containers of clusters in list
		ArrayList<Container> containerList = new ArrayList<Container>();
		for (int i = 0; i < inputClusters.size(); i++) {
			for (int j = 0; j < inputClusters.get(i).clusteredContainers.size(); j++) {
				containerList.add(inputClusters.get(i).clusteredContainers.get(j));
				totalLoadTwo += inputClusters.get(i).clusteredContainers.get(j).expCurrFill;
			}
		}
		
		// debug check, expClusterLoad should align with sum of all expLoads of containers
		if (Math.round(totalLoad) != Math.round(totalLoadTwo)) {
			System.out.println("Debug: expClusterLoad != SUM(expLoad containers)");
		}
		
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
		
		if (k > 1) {

			// find and create k starting points for initial clusters
			for (int i = 0; i < k; i++) {
				Cluster largestCluster = null;
				double loadLargestCluster = 0;
				
				for (int j = 0; j < inputClusters.size(); j++) {
					if (inputClusters.get(j).expClusterLoad > loadLargestCluster) {
						largestCluster = inputClusters.get(j);
						loadLargestCluster = largestCluster.expClusterLoad;
					}
				}
				
				if (largestCluster == null) {
					
					// if (very incidental) k > noInputClusters, the last new cluster has the average coordinates of the other clusters
					double totalLat = 0;
					double totalLon = 0;
					for (int j = 0; j < clusterList.size(); j++) {
						totalLat += clusterList.get(j).clusterCentroidLat;
						totalLon += clusterList.get(j).clusterCentroidLon;
					}
					
					Cluster newCluster = new Cluster(i, ExperimentController.currDay);
					newCluster.clusterCentroidLat = totalLat / clusterList.size();
					newCluster.clusterCentroidLon = totalLon / clusterList.size();
					clusterList.add(newCluster);
					
				}
				else {
					Cluster newCluster = new Cluster(i, ExperimentController.currDay);
					newCluster.clusterCentroidLat = largestCluster.clusterCentroidLat;
					newCluster.clusterCentroidLon = largestCluster.clusterCentroidLon;
					clusterList.add(newCluster);
					inputClusters.remove(largestCluster);
				}
			}
						
			clusterList = assignContainersToClosestClusterXIterations(clusterList, containerList);
				
		}
		else if (k == 1) {
			
			// add all containers to one single cluster
			Cluster newCluster = new Cluster(0, ExperimentController.currDay);
			clusterList.add(newCluster);
			for (int i = 0; i < containerList.size(); i++) {
				newCluster.addContainer(containerList.get(i), true);
			}
			
		}
		else {
			if ((k == 0) && containerList.isEmpty()) {
				
			}
			else {
				System.out.println("DEBUG CHECK: Should never be smaller than 1, would mean no clusters at all");
			}
		}
			
		
		// TODO: temp
		//WriteResults.writeResults(WriteResults.clusterQGISVisualization(clusterList), "XfinalClusters.txt");
		
		return clusterList;
	}
	
	
	/*
	 * 		Assign containers to the closest (possible) clusters, based on priority (distance/demand), until convergence
	 */
	public static ArrayList<Cluster> assignContainersToClosestClusterXIterations(ArrayList<Cluster> clusterList, ArrayList<Container> containerList) {
		
		for (int loop = 0; loop < 10; loop++) {
		
			// (re)calculate priorities for all containers
			for (int i = 0; i < containerList.size(); i++) {
				
				Cluster closestCluster = null;
				double distClosestCluster = 999;
				
				for (int j = 0; j < clusterList.size(); j++) {
					double distCluster = clusterList.get(j).distCentroidToContainer(containerList.get(i));
					if (distCluster < distClosestCluster) {
						closestCluster = clusterList.get(j);
						distClosestCluster = distCluster;
					}
				}
				
				//containerList.get(i).clusterPriority = distClosestCluster / containerList.get(i).expCurrFill;		// TODO: which priority measure to use?
				containerList.get(i).clusterPriority = containerList.get(i).expCurrFill / distClosestCluster;
				containerList.get(i).closestCluster = closestCluster;
			}
			
			// if not the first loop, first remove all containers from clusters, while remembering their centroid coordinates
			if (loop > 0) {
				for (int i = 0; i < clusterList.size(); i++) {
					clusterList.get(i).clusteredContainers.clear();
					clusterList.get(i).expClusterLoad = 0;
				}
			}
			
			// find container with highest priority and assign to favorite cluster, if not possible due to capacity restraints recalculate priority for another cluster
			ArrayList<Container> unassignedContainers = new ArrayList<Container>(containerList);
			while (unassignedContainers.size() > 0) {
					
				Container highestPriorityCont = null;
				double highestPriority = 0;
					
				// find highest priority container
				for (int i = 0; i < unassignedContainers.size(); i++) {
					if (unassignedContainers.get(i).clusterPriority > highestPriority) {
						highestPriorityCont = unassignedContainers.get(i);
						highestPriority = unassignedContainers.get(i).clusterPriority;
					}
				}
				
				if (highestPriorityCont.closestCluster == null) {
					
					//System.out.println("WOW, k clusters is te weinig, nu proppen geblazen");
					
					// this will only occur if the clusters are made so that the last container does not fit, then add disregarding capacity limits
					Cluster closestCluster = null;
					double distClosestCluster = 999;
					for (int j = 0; j < clusterList.size(); j++) {
						if (clusterList.get(j).margCostOfAddingContainer(highestPriorityCont) < distClosestCluster) {
							closestCluster = clusterList.get(j);
							distClosestCluster = clusterList.get(j).distCentroidToContainer(highestPriorityCont);
						}
					}
						
					// add container
					closestCluster.addContainer(highestPriorityCont, false);
					unassignedContainers.remove(highestPriorityCont);
				}
				
				else {
					// add highest priority container to favorite cluster if possible, otherwise recalculate priority for feasible closest cluster
					if (highestPriorityCont.closestCluster.clusterCapacity >= (highestPriorityCont.closestCluster.expClusterLoad + highestPriorityCont.expCurrFill)) {
							
						// add without affecting the centroid
						highestPriorityCont.closestCluster.addContainer(highestPriorityCont, false);
						unassignedContainers.remove(highestPriorityCont);
						
					}
					else {			
						// find closest feasible other cluster
						Cluster newAssignCluster = null;
						double distNewAssignCluster = 999;
						for (int i = 0; i < clusterList.size(); i++) {
							if ((clusterList.get(i).distCentroidToContainer(highestPriorityCont) < distNewAssignCluster) && (clusterList.get(i).clusterCapacity >= 
								clusterList.get(i).expClusterLoad + highestPriorityCont.expCurrFill)) {
								newAssignCluster = clusterList.get(i);
								distNewAssignCluster = clusterList.get(i).distCentroidToContainer(highestPriorityCont);
							}	
						}
							
						// recalculate priority
						highestPriorityCont.clusterPriority = distNewAssignCluster / highestPriorityCont.expCurrFill;
						highestPriorityCont.closestCluster  = newAssignCluster;
					}
				}
			}
			
			// update centroids of all clusters as this was skipped when adding/removing the containers
			recomputeClusterCentroids(clusterList);
			
			// TODO: temp
			//String iterationString = "XAfterIteration" + Integer.toString(loop) + "Clusters.txt";
			//WriteResults.writeResults(WriteResults.clusterQGISVisualization(clusterList), iterationString);
			
		}
		
		return clusterList;
	}
	
	
	// MAYBE NOT NECESSARY, ADDING AND REMOVING CONTAINERS FROM CLUSTER WITH THE HANDMADE FUNCTION ALREADY UPDATES CENTROIDS
	public static void recomputeClusterCentroids(ArrayList<Cluster> clusterList) {
		for (int i = 0; i < clusterList.size(); i++) {
			clusterList.get(i).updateCentroid();
		}
	}
	
}
