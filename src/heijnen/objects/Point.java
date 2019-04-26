package heijnen.objects;

import java.util.ArrayList;
import heijnen.data.Data;

/*
 * 	SUPER CLASS for containers, wharfs, processors, and transshipment hubs
 */

public class Point {

	// FIELDS
	public double procTime;
	private int indexDistanceMatrix;
	private double lat;
	private double lon;
	
	// CONSTRUCTOR
	public Point(int indexDistanceMatrix, double lat, double lon) {
		this.indexDistanceMatrix = indexDistanceMatrix;
		this.lat = lat;
		this.lon = lon;
	}
	
	
	// FUNCTIONS
	public Double distanceToPoint(Point point) {
		int indexDM1 = this.indexDistanceMatrix;
		int indexDM2 = point.indexDistanceMatrix;
				
		return Data.distanceMatrix.get(indexDM1).get(indexDM2);
	}

	public Container findNearestContainer(ArrayList<Container> toBeAssignedContainers) {
		// initialize variables
		Container container = null;
		Double shortestDistance = (double) 999;
		
		for (int i = 0; i < toBeAssignedContainers.size(); i++) {
			Double temp = distanceToPoint(toBeAssignedContainers.get(i));
			
			if (temp < shortestDistance) {
				container = toBeAssignedContainers.get(i);
				shortestDistance = temp;
			}
		}

		return container;
	}
	
	public Point findNearestDumpLocation() {
		Point dumpLocation = null;
		Double shortestDistance = (double) 999;
		
		for (int i = 0; i < Data.wasteProcessorList.size(); i++) {
			Double temp = distanceToPoint(Data.wasteProcessorList.get(i));
			
			if (temp < shortestDistance) {
				dumpLocation = Data.wasteProcessorList.get(i);
				shortestDistance = temp;
			}
		}
		
		for (int j = 0; j < Data.transshipmentHubList.size(); j++) {
			Double temp = distanceToPoint(Data.transshipmentHubList.get(j));
			
			if (temp < shortestDistance) {
				dumpLocation = Data.transshipmentHubList.get(j);
				shortestDistance = temp;
			}
		}
		
		return dumpLocation;
	}


	// GETTERS AND SETTERS
	
	public int getIndexDistanceMatrix() {
		return indexDistanceMatrix;
	}
	
	public double getLat() {
		return lat;
	}
	
	public double getLon() {
		return lon;
	}
	
}
