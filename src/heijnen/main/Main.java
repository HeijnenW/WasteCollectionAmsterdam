package heijnen.main;

import java.util.ArrayList;

import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.objects.Container;
import heijnen.simulation.SimulationController;


public class Main {

	public static long startTime = System.nanoTime();
	public static long maxVehicles = 0;
	public static ArrayList<Double> routeLoads = new ArrayList<Double>();
	public static int violations = 0;
	public static int nonViolations = 0;


	public static void main(String[] args) {
		
		Data.readInputData();	
		
		System.out.println(Data.containerList.size());
			
		SimulationController.startSim();
		

		// several temporary checks for input
		/*double totWasteGenerated = 0;
		// 3 replications
		for (int i = 0; i < 3; i++) {
			
			// 100 days
			for (int j = 0; j < 100; j++) {
				
				for (int k = 0; k < Data.containerList.size(); k++) {
					
					// update expected current fill
					double shapeDay = Data.containerList.get(k).shapePweek / 7;
					double scaleDay = Data.containerList.get(k).scalePweek;
					
					GammaDistribution gamma = new GammaDistribution(shapeDay, scaleDay);
					double rnd = SimulationController.rgen.nextDouble();
					double addFill = gamma.inverseCumulativeProbability(rnd);
					
					totWasteGenerated += addFill;
					
				}
			}
		}
		
		System.out.println(totWasteGenerated/3);*/
		
		/*
		ArrayList<Double> stDevContainers = new ArrayList<Double>();
		double totStDev = 0;
		
		for (int i = 0; i < Data.containerList.size(); i++) {
			double shapeDay = Data.containerList.get(i).shapePweek / 7;
			double scaleDay = Data.containerList.get(i).scalePweek;
			GammaDistribution gamma = new GammaDistribution(shapeDay, scaleDay);
			
			ArrayList<Double> deposits = new ArrayList<Double>();
			double totFill = 0;
			
			// generate 300 deposits
			for (int j = 0; j < 300; j++) {
				double rnd = SimulationController.rgen.nextDouble();
				double addFill = gamma.inverseCumulativeProbability(rnd);
				deposits.add(addFill);
				totFill += addFill;
			}

			double meanFill = totFill / deposits.size();
			double temp = 0;
			
			// calc st dev of all deposits
			for (int j = 0; j < deposits.size(); j++) {
				double squrDiffToMean = Math.pow(deposits.get(j) - meanFill, 2);
				temp += squrDiffToMean;
			}
			
			double stdevDeposits = Math.sqrt(temp / deposits.size());
			stDevContainers.add(stdevDeposits);
			totStDev += stdevDeposits;
		}
		
		System.out.println("Avg stdev: " + (totStDev / 353));*/
		
		
		long endTime = System.nanoTime();
		
		
		System.out.println("Total time elapsed: " + ((endTime - startTime)/1000000000) + " seconds");
		System.out.println("Max vehicles used: " + maxVehicles);		

		// counting the number of times the capacity of a vehicle is exceeded, there is currently no method to schedule the remaining containers, so this should be kept to a minimum
		/*
		int violations = 0;
		int nonViolations = 0;
		for (int i = 0; i < routeLoads.size(); i++) {
			if (routeLoads.get(i) > Parameters.vehicleCapacity) {
				violations++;
			}
			else {
				nonViolations++;
			}
		}
		
		System.out.println("Violations: " + violations);
		System.out.println("Non violations: " + nonViolations);
		System.out.println("Violations: " + violations + ", non-violations: " + nonViolations);
		*/
	}
}