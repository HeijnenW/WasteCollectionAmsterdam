package heijnen.algorithms;

import java.util.ArrayList;

import heijnen.data.Data;
import heijnen.objects.Container;
import heijnen.simulation.Day;

/*
 * 		Implementation of phase I, the selection of the relevant containers
 */

public class ContainerSelection {
	
	////		FUNCTIONS			////
	
	public static ArrayList<Container> selectionBasedOnDED(int lengthPlanningHor) {
		
		ArrayList<Container> selectedContainers = new ArrayList<Container>();
		
		for (int i = 0; i < Data.containerList.size(); i++) {
			Container container = Data.containerList.get(i);
			
			if ((container.DED <= Day.dayNr + lengthPlanningHor - 1) && (container.overflowed == false)) {
				selectedContainers.add(container);
			}
		}
		
		return selectedContainers;
	}
	
	
}
