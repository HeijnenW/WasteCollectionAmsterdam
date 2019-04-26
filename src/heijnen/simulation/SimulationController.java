package heijnen.simulation;

import java.util.ArrayList;
import java.util.Random;

/*
 * 		Controls simulation, by coordinating experiments and holding experiment data/parameters
 */

public class SimulationController {
	
	////		FIELDS			////
	
	// storage for settings of all experiments
	public static int nrOfExp;																		// number of experiments that will be performed
	
	// experimental factors
	public static final ArrayList<String> expSettingsTravelCostsApprox = new ArrayList<String>();
	public static final ArrayList<Double> expSettingsPenaltyTooLateFactor = new ArrayList<Double>();
	public static final ArrayList<Double> expSettingsPenaltyTooEarlyFactor = new ArrayList<Double>(); 
	public static final ArrayList<Double> expSettingsAddingNewClusterCosts = new ArrayList<Double>();
	public static final ArrayList<Boolean> expSettingsUsingClusters = new ArrayList<Boolean>();
	public static final ArrayList<Double> expSettingsAOP = new ArrayList<Double>();
	public static final ArrayList<Boolean> expSettingsSensors = new ArrayList<Boolean>();
	public static final ArrayList<Integer> expSettingsLengthPlanningHorizon = new ArrayList<Integer>();
	public static final ArrayList<String> expSettingsOnlineReschedulingTechnique = new ArrayList<String>();
	public static final ArrayList<Double> expSettingsBufferVehicles = new ArrayList<Double>();

	public static Random rgen = new Random();
	
	
	////		FUNCTIONS			////
	
	public static void startSim() {
		
		for (int i = 0; i < nrOfExp; i++) {		
			updateExpSettings(i);
			ExperimentController.runExp();
		}
		
		WriteResults.printSimulationResults();
		
		endSim();
		
	}
	
	
	public static void updateExpSettings(int currExp) {
		ExperimentController.currExpNr = currExp;
		
		ExperimentController.travelCostsApproxEF 			= expSettingsTravelCostsApprox.get(currExp);
		ExperimentController.penaltyTooLateFactorEF 		= expSettingsPenaltyTooLateFactor.get(currExp);		
		ExperimentController.penaltyTooEarlyFactorEF 		= expSettingsPenaltyTooEarlyFactor.get(currExp);
		ExperimentController.addingNewClusterCostsEF 		= expSettingsAddingNewClusterCosts.get(currExp);
		ExperimentController.usingClustersEF 				= expSettingsUsingClusters.get(currExp);
		ExperimentController.AOPEF 							= expSettingsAOP.get(currExp);
		ExperimentController.sensorsEF 						= expSettingsSensors.get(currExp);
		ExperimentController.lengthPlanningHorizonEF 		= expSettingsLengthPlanningHorizon.get(currExp);
		ExperimentController.onlineReschedulingTechniqueEF	= expSettingsOnlineReschedulingTechnique.get(currExp);
		ExperimentController.bufferVehiclesEF 				= expSettingsBufferVehicles.get(currExp);
	}
	
	public static void endSim() {
		// TODO: summarize all experiment-results in one file for analysis
	}
	
	
	
}
