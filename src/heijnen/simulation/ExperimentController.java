package heijnen.simulation;

import java.util.ArrayList;

import heijnen.algorithms.AmsterdamPlanning;
import heijnen.data.Data;
import heijnen.data.Parameters;
import heijnen.main.Main;
import heijnen.objects.Container;
import heijnen.objects.TransshipmentHub;

public class ExperimentController {

	////		FIELDS			////
	
	// settings of current experiment
	public static int currExpNr;
	
	// experimental factors
	public static String travelCostsApproxEF;
	public static double penaltyTooLateFactorEF;		
	public static double penaltyTooEarlyFactorEF;
	public static double addingNewClusterCostsEF;
	public static boolean usingClustersEF;
	public static double AOPEF;
	public static boolean sensorsEF;
	public static int lengthPlanningHorizonEF;
	public static String onlineReschedulingTechniqueEF;
	public static double bufferVehiclesEF;

	
	// experiment trackers
	public static int currRepl;
	public static int currDay;
	public static ArrayList<Container> overflowedContainers = new ArrayList<Container>();
	
	
	////		EXP FUNCTIONS	      ////

	public static void runExp() {
		
		long startExpTime = System.nanoTime();
		
		initExp();
		
		for (int i = 0; i < Parameters.experimentNoReplications; i++) {
			
			currRepl = i;
			SimulationController.rgen.setSeed(i);		// the i-th replication of each experiment should have the same random numbers
			
			for (int j = 0; j < Parameters.experimentTotalLength; j++) {
				currDay = j;
				Day.simDay();
			}
			
			WriteResults.saveReplicationResults();
			//WriteResults.printReplicationResults();
			resetRepl();
			
		}
			
		WriteResults.saveExperimentResults();
		resetExp();
		
		long expDuration = (System.nanoTime() - startExpTime) / 1000000000;		
		WriteResults.experimentDurationSim.add(expDuration);
	
	}
	
	
	public static void initExp() {
		// calculate DEL, DED, and EIL based on experiments AOP
		recalcContainerVariables(AOPEF);
	}
	
	
	/*
	 * 		Resets all relevant variables to be able to start a new replication (container fill levels, SF capacity used, performance indicators)
	 */
	public static void resetRepl() {
		
		
		// reset relevant container variables
		for (int i = 0; i < Data.containerList.size(); i++) {
			Container container = Data.containerList.get(i);
			
			container.currFill = 0;
			container.expCurrFill = 0;
			container.lastDayEmptied = 0;
			container.DED = container.EIL;
			container.overflowed = false;
			container.clusterPriority = 0;
			container.closestCluster = null;
		}
		
		
		overflowedContainers.clear();
		
		// TODO: NEW
		Day.overflowedContainersYesterday.clear();
		
		// reset KPIs tracking replication performance
		WriteResults.totalDistancesRepl.clear();
		WriteResults.totalDurationsRepl.clear();
		WriteResults.noEmptiedContainersRepl.clear();
		WriteResults.noOverflowedContainersRepl.clear();
		WriteResults.noDumpsWPRepl.clear();
		WriteResults.noDumpsSFRepl.clear();
		WriteResults.avgFillContainersRepl.clear();
		WriteResults.avgFillVehiclesRepl.clear();
		WriteResults.noVehiclesUsedRepl.clear();
		WriteResults.avgExpFillContainersRepl.clear();
		
		WriteResults.noContTooLateRepl.clear();
		WriteResults.noContTooEarlyRepl.clear();
		WriteResults.noContOnTimeRepl.clear();
	}
	
	
	public static void resetExp() {
		// reset KPIs tracking replication performance
		WriteResults.totalDistancesExp.clear();
		WriteResults.totalDurationsExp.clear();
		WriteResults.noEmptiedContainersExp.clear();
		WriteResults.noOverflowedContainersExp.clear();
		WriteResults.noDumpsWPExp.clear();
		WriteResults.noDumpsSFExp.clear();
		WriteResults.avgFillContainersExp.clear();
		WriteResults.avgFillVehiclesExp.clear();
		WriteResults.noVehiclesUsedExp.clear();
		WriteResults.avgExpFillContainersExp.clear();
		
		WriteResults.noContTooLateExp.clear();
		WriteResults.noContTooEarlyExp.clear();
		WriteResults.noContOnTimeExp.clear();
	}
	
	
	
	////		MISC FUNCTIONS		  ////
	
	
	/*
	 * 		Calculate the DEL, DED, and EIL for all containers with the current experimental settings
	 */
	public static void recalcContainerVariables(double AOPEF) {
		
		for (int i = 0; i < Data.containerList.size(); i++) {
			Container container = Data.containerList.get(i);
			
			// calculate DED and EIL
			double overflowProb = 0;		// initialize
			int day = 0;				// initialize
			
			while (overflowProb < AOPEF) {
				overflowProb = container.probFullStartDay(day);		
				day++;
			}
	
			container.DED = day - 2;			//  -1 to correct for dayCount++, and -1 to correct for exceeding the AOP
			container.EIL = day - 2;			//  at the start, DED and EIL are the same, EIL (interval) stays the same, DED is updated after each emptying				

		}
		
	}
	
}
