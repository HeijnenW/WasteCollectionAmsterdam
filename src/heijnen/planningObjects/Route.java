package heijnen.planningObjects;

import java.util.ArrayList;

import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.objects.Container;
import heijnen.objects.Point;
import heijnen.simulation.ExperimentController;

public class Route {

	////		FIELDS		 ////
	
	public ArrayList<Point> routingSequence = new ArrayList<Point>();
	public ArrayList<Container> containersInRoute = new ArrayList<Container>();
	public ArrayList<Integer> indexDumpLocations = new ArrayList<Integer>();
	
	public int	dayNr;
	
	public double routeDistance;
	public double routeDuration;
	public double expectedRouteLoad;
		
	
	////		CONSTRUCTORS		////
	public Route() {
		
		routingSequence.add(Data.wharfList.get(0));
		routingSequence.add(Data.wharfList.get(0));
		
		dayNr = ExperimentController.currDay;
		
		routeDistance = 0;
		routeDuration = 0;
		expectedRouteLoad = 0;
	}
	
	
	////		FUNCTIONS		 ////
	
	public void addPointToRoute(int index, Point point) {
		
		double margDist = evaluateAddingPoint(index, point);
		
		if (margDist < 0) {
			System.out.println("DEBUG: wss niet waar");
		}
		
		routingSequence.add(index, point);
		
		if (point instanceof Container) {
			containersInRoute.add((Container) point);
			expectedRouteLoad += ((Container) point).expCurrFill;			
		}
		
		if (routingSequence.size() > 1) {
			routeDistance += margDist;
			routeDuration += (margDist / Parameters.avgSpeed) + (point.procTime);	
		}
	
	}
	
	
	/*
	 * 		Evaluate the additional distance that is traveled when adding the point between index-1 and index+1
	 */
	public double evaluateAddingPoint(int index, Point point) {
		double margDist = 0;
		
		if (routingSequence.size() == 0) {
			margDist = 0;
		}
		else if (routingSequence.size() == 1) {
			margDist = 2 * routingSequence.get(0).distanceToPoint(point);
		}
		else if (routingSequence.size() > 1) {
			double prevDist = routingSequence.get(index-1).distanceToPoint(routingSequence.get(index));
			double aftDist  = routingSequence.get(index-1).distanceToPoint(point) + point.distanceToPoint(routingSequence.get(index));
			margDist = aftDist - prevDist;
		}

		return margDist;
	}
	
		
	public void updateRouteCosts() {
		double distanceCosts = 0;
		double drivingDuration = 0;
		double procTimes = 0;
		double routeDuration = 0;
		
		for (int i = 0; i < this.routingSequence.size() - 1; i++) {
			distanceCosts += this.routingSequence.get(i).distanceToPoint(this.routingSequence.get(i+1));
		}
		
		drivingDuration = distanceCosts / Parameters.avgSpeed;
		
		for (int j = 0; j < this.routingSequence.size(); j++ ) {
			procTimes += this.routingSequence.get(j).procTime;
		}
		
		routeDuration = drivingDuration + (procTimes / 60);
		
		this.routeDistance = distanceCosts;
		this.routeDuration = routeDuration;
	}

}
