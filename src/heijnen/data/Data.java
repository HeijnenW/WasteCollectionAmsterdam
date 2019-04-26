package heijnen.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import heijnen.objects.Container;
import heijnen.objects.Point;
import heijnen.objects.TransshipmentHub;
import heijnen.objects.Vehicle;
import heijnen.objects.WasteProcessor;
import heijnen.objects.Wharf;
import heijnen.simulation.SimulationController;

/*
 * 		Static class (does not need to be instantiated) that is used to read and store all input data
 */

public class Data {

	////		FIELDS			////
	
	public static ArrayList<Container> containerList = new ArrayList<Container>();
	public static ArrayList<Wharf> wharfList = new ArrayList<Wharf>();
	public static ArrayList<WasteProcessor> wasteProcessorList = new ArrayList<WasteProcessor>();
	public static ArrayList<TransshipmentHub> transshipmentHubList = new ArrayList<TransshipmentHub>();
	public static ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	
	public static ArrayList<ArrayList<Double>> distanceMatrix = new ArrayList<ArrayList<Double>>();
	

	
	////		FUNCTIONS		////
	
	/*
	 * Reads input data from local files and creates a distance matrix between all points
	 */
	public static void readInputData() {
		
		// initialize index of distance matrix counter
		int indexDM = 0;
		
		// read containers
		Scanner inputContainer = null;
		try {
			inputContainer = new Scanner(new File("inputContainer.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputContainer.useDelimiter("\t|\r\n");
		
		while (inputContainer.hasNext()) {
			String wasteFraction = inputContainer.next();
			double lat = Double.valueOf(inputContainer.next());
			double lon = Double.valueOf(inputContainer.next());
			
			double capacityVolume = 0;
			switch (wasteFraction) {
				case "glas":     	capacityVolume = Double.valueOf(inputContainer.next()) * Parameters.glassWeight;
				case "kunststof":	capacityVolume = Double.valueOf(inputContainer.next()) * Parameters.plasticWeight;
				case "papier":    	capacityVolume = Double.valueOf(inputContainer.next()) * Parameters.paperWeight;
				case "rest":		capacityVolume = Double.valueOf(inputContainer.next()) * Parameters.householdWeight;
			}
			
			double shapePweek = Double.valueOf(inputContainer.next());
			double scalePweek = Double.valueOf(inputContainer.next());
			
			int indexDistanceMatrix = indexDM;
			Container c = new Container(indexDistanceMatrix, lat, lon, wasteFraction, capacityVolume, shapePweek, scalePweek);
			containerList.add(c);
			indexDM++;
		}
		
		inputContainer.close();
		
		// read transshipment hubs
		Scanner inputTransshipmentHub = null;
		try {
			inputTransshipmentHub = new Scanner(new File("inputTransshipmentHub.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputTransshipmentHub.useDelimiter("\t|\r\n");
		
		while (inputTransshipmentHub.hasNext()) {
			double lat = Double.valueOf(inputTransshipmentHub.next());
			double lon = Double.valueOf(inputTransshipmentHub.next());
			int storageCapacity = Integer.valueOf(inputTransshipmentHub.next());
			int storageUsed = Integer.valueOf(inputTransshipmentHub.next());
			int storageFree = Integer.valueOf(inputTransshipmentHub.next());
			
			int indexDistanceMatrix = indexDM;
			TransshipmentHub t = new TransshipmentHub(indexDistanceMatrix, lat, lon, storageCapacity, storageUsed, storageFree);
			transshipmentHubList.add(t);
			indexDM++;
		}
		
		inputTransshipmentHub.close();

	
		// read waste processors
		Scanner inputWasteProcessor = null;
		try {
			inputWasteProcessor = new Scanner(new File("inputWasteProcessor.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputWasteProcessor.useDelimiter("\t|\r\n");
		
		while (inputWasteProcessor.hasNext()) {
			double lat = Double.valueOf(inputWasteProcessor.next());
			double lon = Double.valueOf(inputWasteProcessor.next());
			String wasteFraction = inputWasteProcessor.next();
			
			int indexDistanceMatrix = indexDM;
			WasteProcessor wp = new WasteProcessor(indexDistanceMatrix, lat, lon, wasteFraction);
			wasteProcessorList.add(wp);
			indexDM++;
		}
		
		inputWasteProcessor.close();

		
		// read wharfs
		Scanner inputWharf = null;
		try {
			inputWharf = new Scanner(new File("inputWharf.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputWharf.useDelimiter("\t|\r\n");
		
		while (inputWharf.hasNext()) {
			double lat = Double.valueOf(inputWharf.next());
			double lon = Double.valueOf(inputWharf.next());
			
			int indexDistanceMatrix = indexDM;
			Wharf wh = new Wharf(indexDistanceMatrix, lat, lon);
			wharfList.add(wh);
			indexDM++;
		}
		
		inputWharf.close();
		
		
		// read vehicles
		Scanner inputVehicle = null;
		try {
			inputVehicle = new Scanner(new File("inputVehicle.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputVehicle.useDelimiter("\t|\r\n");
		
		while (inputVehicle.hasNext()) {
			double capacity = Double.valueOf(inputVehicle.next());
			boolean detachable = Boolean.valueOf(inputVehicle.next());
			
			Vehicle v = new Vehicle(capacity, detachable);
			vehicleList.add(v);
		}
				
		inputVehicle.close();
		
		// create distance matrix
		final ArrayList<Point> pointList = new ArrayList<Point>();
		pointList.addAll(containerList);
		pointList.addAll(transshipmentHubList);
		pointList.addAll(wasteProcessorList);
		pointList.addAll(wharfList);
		
		final ArrayList<Double> tempDist = new ArrayList<Double>();
		
		for (int i = 0; i < pointList.size(); i++) {
			for (int j = 0; j < pointList.size(); j++) {
				tempDist.add(distance(pointList.get(i).getLat(), pointList.get(i).getLon(), pointList.get(j).getLat(), pointList.get(j).getLon()));
			}
			
			distanceMatrix.add(new ArrayList<Double>());
			for (int k = 0; k < tempDist.size(); k++) {
				distanceMatrix.get(i).add(tempDist.get(k));
			}

			tempDist.clear();
		}
		
		pointList.clear();
		
		// calculate the SCRC for all containers (only able to do so after distance matrix is created)
		for (int i = 0; i < containerList.size(); i++) {
			double SCRCWP = wharfList.get(0).distanceToPoint(containerList.get(i)) + containerList.get(i).distanceToPoint(wasteProcessorList.get(0)) +
					wasteProcessorList.get(0).distanceToPoint(wharfList.get(0));          // SCRC to waste processor     (wh -> c -> wp -> wh)
			double SCRCSF = wharfList.get(0).distanceToPoint(containerList.get(i)) + containerList.get(i).distanceToPoint(transshipmentHubList.get(0)) +
					transshipmentHubList.get(0).distanceToPoint(wharfList.get(0));		  // SCRC to satellite facility  (wh -> c -> sf -> wh)
			
			containerList.get(i).SCRC = Math.min(SCRCWP, SCRCSF);
		}

		
		// read experimental settings
		Scanner inputExperiments = null;
		try {
			inputExperiments = new Scanner(new File("inputExperiments.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		inputExperiments.useDelimiter("\t|\r\n");
		
		int noExp = 0;
		
		while (inputExperiments.hasNext()) {
			SimulationController.expSettingsTravelCostsApprox.add(inputExperiments.next());
			SimulationController.expSettingsPenaltyTooLateFactor.add(Double.valueOf(inputExperiments.next()));
			SimulationController.expSettingsPenaltyTooEarlyFactor.add(Double.valueOf(inputExperiments.next()));
			SimulationController.expSettingsAddingNewClusterCosts.add(Double.valueOf(inputExperiments.next()));
			SimulationController.expSettingsUsingClusters.add(Boolean.valueOf(inputExperiments.next()));
			SimulationController.expSettingsAOP.add(Double.valueOf(inputExperiments.next()));
			SimulationController.expSettingsSensors.add(Boolean.valueOf(inputExperiments.next()));
			SimulationController.expSettingsLengthPlanningHorizon.add(Integer.valueOf(inputExperiments.next()));
			SimulationController.expSettingsOnlineReschedulingTechnique.add(inputExperiments.next());
			SimulationController.expSettingsBufferVehicles.add(Double.valueOf(inputExperiments.next()));
			
			noExp++;
		}
				
		SimulationController.nrOfExp = noExp;
		
		inputExperiments.close();
		
		System.out.println("All input files are read");
	
	} 
	
	
	/*
	 * Calculates the distance between two points based on latitude and longitude
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		}
		
		double theta = lon1 - lon2;
		double dist = (Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta)));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = (dist * 60 * 1.1515);
		dist = (dist * 1.609344);
		return (dist);
	}
	    
	private static double deg2rad(double deg) {
	    	return (deg * Math.PI / 180.0);
	}
	
	private static double rad2deg(double rad) {
	    	return (rad * 180.0 / Math.PI);
	}
	
}
