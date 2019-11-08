package heijnen.algorithms;

import java.util.ArrayList;
import heijnen.data.Data;
import heijnen.objects.Container;
import heijnen.simulation.Day;

/*
 * 		Implementation of phase I, the selection of the relevant containers
 */

public class ContainerSelection {
	
	// a list of 'relevant' containers is returned
	// containers are considered relevant if their Desired Empyting Day (DED) falls within the specified planning horizon
	public static ArrayList<Container> selectionBasedOnDED(int lengthPlanningHor) {
		
		ArrayList<Container> selectedContainers = new ArrayList<Container>();
		
		// check all containers for relevance
		for (int i = 0; i < Data.containerList.size(); i++) {
			Container container = Data.containerList.get(i);
			
			// note: overflowed containers are not considered as they are mandatory and are considered separately in DayAssignment.java, clustersForOverflowedContainers
			if ((container.DED <= Day.dayNr + lengthPlanningHor - 1) && (container.overflowed == false)) {
				selectedContainers.add(container);
			}
		}
		
		return selectedContainers;
	}
	
	
}
