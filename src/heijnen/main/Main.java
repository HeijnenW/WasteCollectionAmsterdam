package heijnen.main;

import heijnen.data.Data;
import heijnen.simulation.SimulationController;

public class Main {
	
	public static long startTime = System.nanoTime();

	public static void main(String[] args) {
		
		Data.readInputData();		
		
		SimulationController.startSim();
		
		long endTime = System.nanoTime();
		System.out.println("Total time elapsed: " + ((endTime - startTime)/1000000000) + " seconds");
		
	}
	
}