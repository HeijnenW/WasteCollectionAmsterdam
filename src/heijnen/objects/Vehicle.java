package heijnen.objects;

import heijnen.data.Data;

public class Vehicle {

	
	////		FIELDS		 ////
	public double capacity;				// vehicle capacity (kg)
	public double currFill;				// current vehicle content (kg)
	public boolean detachable;			// does the container have a detachable container?
	
	public Point currPosition;			// used during online rescheduling of routes

	
	
	////		CONSTRUCTOR			////
	
	public Vehicle(double capacity, boolean detachable) {
		this.capacity = capacity;
		this.detachable = detachable;
		
		this.currFill = 0;
		this.currPosition = Data.wharfList.get(0);
	}
	
}
