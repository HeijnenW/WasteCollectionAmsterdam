package heijnen.planningObjects;

import java.util.ArrayList;

import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.objects.Container;
import heijnen.objects.Point;
import heijnen.simulation.ExperimentController;

public class Cluster {

	// FIELDS
	
	// construction fields
	public int clusterID;
	public int dayNr;
	public ArrayList<Container> clusteredContainers = new ArrayList<Container>();
	
	// cluster tracking fields
	public double clusterCapacity;
	public double expClusterLoad;
	
	public double currTravelCostApprox;
	public double currIncrCostApprox;
	
	public double clusterCentroidLat;
	public double clusterCentroidLon;
	
	public double areaLeft;
	public double areaRight;
	public double areaTop;
	public double areaBottom;
	

	// CONSTRUCTOR
	public Cluster(int clusterID, int dayNr) {
		this.clusterID = clusterID;
		this.dayNr = dayNr;
		this.clusterCapacity = Parameters.vehicleCapacity * (1-ExperimentController.bufferVehiclesEF);
	}
	
	// FUNCTIONS
	
	public void addContainer(Container container, boolean updateCentroid) {
		
		// add container to clusteredContainer-list
		clusteredContainers.add(container);
		
		// recalculate expected cluster load
		expClusterLoad += container.expectedLoadStartDay(dayNr);
		
		if (updateCentroid == true) {
			
			if (clusteredContainers.size() > 1) {
				clusterCentroidLat = (clusterCentroidLat * (clusteredContainers.size()-1) + container.getLat()) / clusteredContainers.size();
				clusterCentroidLon = (clusterCentroidLon * (clusteredContainers.size()-1) + container.getLon()) / clusteredContainers.size();
			}		
			else if (clusteredContainers.size() == 1) {
				updateCentroid();
			}
		}
		
		// update left, right, top, bottom for areas if using Daganzo
		if (ExperimentController.travelCostsApproxEF.equals("Daganzo")) {
			updateAreaConvex();
		}
		
	}
	
	public void removeContainer(Container container, boolean updateCentroid) {
	
		// remove container from clusteredContainer-list
		clusteredContainers.remove(container);

		// recalculate expected cluster load
		expClusterLoad -= container.expectedLoadStartDay(dayNr);
		
		if (updateCentroid == true) {
			// recalculate all approximation parameters
			clusterCentroidLat = (clusterCentroidLat * (clusteredContainers.size()+1) - container.getLat()) / clusteredContainers.size();
			clusterCentroidLon = (clusterCentroidLon * (clusteredContainers.size()+1) - container.getLon()) / clusteredContainers.size();
		}
		
		// update left, right, top, bottom for areas if using Daganzo
		if (ExperimentController.travelCostsApproxEF.equals("Daganzo")) {
			updateAreaConvex();
		}
	
	}


	
	

	public double margCostOfAddingContainer(Container container) {
		double margCosts = 0;
		
		if (ExperimentController.travelCostsApproxEF.equals("CC")) {
			margCosts = this.distCentroidToContainer(container);
			if (Double.isNaN(margCosts)) {
				margCosts = 0;
			}
		}
		
		else if (ExperimentController.travelCostsApproxEF.equals("CP")) {
			
			margCosts = 999;
			double tempMargCosts = 999;
			for (int i = 0; i < this.clusteredContainers.size(); i++) {
				
				if (this.clusteredContainers.get(i) != container) {									// check if you're not comparing with itself, necessary when this function is used when updating cluster-costs
					tempMargCosts = container.distanceToPoint(this.clusteredContainers.get(i));
					if (tempMargCosts < margCosts) {
						margCosts = tempMargCosts;
					}
				}
			}
			
		}
		
		else if (ExperimentController.travelCostsApproxEF.equals("CI")) {
			
			Container containerA = null;
			Container containerB = null;
			double distContainerA = 999;
			double distContainerB = 999;
			double tempCosts = 999;
			
			// find closest container in cluster
			for (int i = 0; i < this.clusteredContainers.size(); i++) {
				if (this.clusteredContainers.get(i) != container) {
					tempCosts = container.distanceToPoint(this.clusteredContainers.get(i));
					if (tempCosts < distContainerA) {
						containerA = this.clusteredContainers.get(i);
						distContainerA = tempCosts;
					}
				}	
			}
			
			if (this.clusteredContainers.size() > 1) {
				// find second closest container in cluster
				for (int i = 0; i < this.clusteredContainers.size(); i++) {
					if ((this.clusteredContainers.get(i) != container) && (this.clusteredContainers.get(i) != containerA)) {
						tempCosts = container.distanceToPoint(this.clusteredContainers.get(i));
						if (tempCosts < distContainerB) {
							containerB = this.clusteredContainers.get(i);
							distContainerB = tempCosts;
						}
					}	
				}
				
				if ((containerA != null) && (containerB != null)) {
					margCosts = (containerA.distanceToPoint(container) + container.distanceToPoint(containerB)) - containerA.distanceToPoint(containerB);
				}
				else {
					margCosts = distContainerA * 2;
				}
			}
			else {
				margCosts = distContainerA * 2;
			}
		}
		
		else if (ExperimentController.travelCostsApproxEF.equals("Daganzo")) {
			
			double prevCostApprox = daganzoApproximation();
			
			// Daganzo variables
			int noCustomers;			// N
			double capacity;			// C
			double area; 				// A
			double shapeConstant;		// k
			
			// supplementary
			double left;
			double right;
			double top;
			double bottom;
			
			// determining values of variables
			noCustomers = this.clusteredContainers.size() + 1;
			capacity = this.clusteredContainers.size() + 1 + ((clusterCapacity - (expClusterLoad + container.expCurrFill))/500);
			
			if (container.getLat() < this.areaBottom) {
				bottom = container.getLat();
			}
			else {
				bottom = this.areaBottom;
			}
			
			if (container.getLat() > this.areaTop) {
				top = container.getLat();
			}
			else {
				top = this.areaTop;
			}
			
			if (container.getLon() < this.areaLeft) {
				left = container.getLon();
			}
			else {
				left = this.areaLeft;
			}
			
			if (container.getLon() > this.areaRight) {
				right = container.getLon();
			}
			else {
				right = this.areaRight;
			}
			
			double horzDist = Data.distance(top, left, top, right);
			double vertDist = Data.distance(top, left, bottom, left); 
			
			if ((horzDist == 0) || (vertDist == 0)) {
				margCosts = horzDist + vertDist;
				return margCosts;
			}
			else {
				area = horzDist * vertDist;
			}
			
			
			if ((Math.min(horzDist, vertDist)/Math.max(horzDist, vertDist)) <= 0.6) {
				shapeConstant = 0.55;
			}
			else {
				shapeConstant = 0.45;
			}
			
			double newDistApprox = (0.9 + shapeConstant * (noCustomers/Math.pow(capacity, 2))) * Math.sqrt(area * noCustomers);
			margCosts = newDistApprox - prevCostApprox;
		} 
		
		else {
			System.out.println("DEBUG: geen juiste travel costs approx gekozen");
		}	
		
		return margCosts;
	}
	
	
	/*
	 * 		calculates the distance of the container to the centroid of the cluster
	 */
	public double distCentroidToContainer(Container container) {
		double distanceToCentroid = Data.distance(clusterCentroidLat, clusterCentroidLon, container.getLat(), container.getLon());
		return distanceToCentroid;
	}
	
	
	public void updateCentroid() {
		double sumLat = 0;
		double sumLon = 0;
		
		for (int i = 0; i < clusteredContainers.size(); i++) {
			sumLat += clusteredContainers.get(i).getLat();
			sumLon += clusteredContainers.get(i).getLon();
		}
		
		clusterCentroidLat = sumLat / clusteredContainers.size();
		clusterCentroidLon = sumLon / clusteredContainers.size();
	}
	
	
	/*
	 * 		Update cost approximations (incremental and travel), used to evaluate local search improvements, only call if necessary
	 */
	public void updateClusterCostApproximations() {
		
		// initialize values
		currTravelCostApprox = 0;
		currIncrCostApprox = 0;
		
		// recalculate values of all containers and sum
		for (int i = 0; i < clusteredContainers.size(); i++) {
			
			Container container = clusteredContainers.get(i);
			
			// update travel costs approximation
			double margCostContainer = margCostOfAddingContainer(container);
			
			if (ExperimentController.travelCostsApproxEF != "Daganzo") {
				currTravelCostApprox += margCostContainer;
			}
			
			// update incremental costs approximation
			currIncrCostApprox += container.calcPenalty(dayNr, margCostContainer);			
		}
		
		if (ExperimentController.travelCostsApproxEF.equals("Daganzo")) {
			currTravelCostApprox = daganzoApproximation();
		}

	}
	
	public double daganzoApproximation() {
		// Daganzo variables
		int noCustomers;			// N
		double capacity;			// C
		double area; 				// A
		double shapeConstant;		// k

		// determining values of variables
		noCustomers = this.clusteredContainers.size();
		capacity = this.clusteredContainers.size() + ((clusterCapacity - expClusterLoad )/500);
		
		double horzDist = Data.distance(areaTop, areaLeft, areaTop, areaRight);
		double vertDist = Data.distance(areaTop, areaLeft, areaBottom, areaLeft); 
		
		area = horzDist * vertDist;
		
		if ((Math.min(horzDist, vertDist)/Math.max(horzDist, vertDist)) <= 0.6) {
			shapeConstant = 0.55;
		}
		else {
			shapeConstant = 0.45;
		}
		
		double newDistApprox = (0.9 + shapeConstant * (noCustomers/Math.pow(capacity, 2))) * Math.sqrt(area * noCustomers);
		return newDistApprox;
	}
	
	
	private void updateAreaConvex() {
		
		double bottom = 999;	// lat
		double top = 0;			// lat
		double left = 999;		// lon
		double right = 0;		// lon
		
		for (int i = 0; i < clusteredContainers.size(); i++) {
			Container container = clusteredContainers.get(i);
			
			if (container.getLat() < bottom) {
				bottom = container.getLat();
			}
			if (container.getLat() > top) {
				top = container.getLat();
			}
			if (container.getLon() < left) {
				left = container.getLon();
			}
			if (container.getLon() > right) {
				right = container.getLon();
			}
		}
		
		this.areaBottom = bottom;
		this.areaTop = top;
		this.areaLeft = left;
		this.areaRight = right;
	}

	
}
