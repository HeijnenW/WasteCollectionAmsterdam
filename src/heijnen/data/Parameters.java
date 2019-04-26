package heijnen.data;

public class Parameters {

	// system specific parameters
	public static final int daysOfTheWeek = 7;					// number of days in a week
	public static final double workingHours = 7.5;				// number of working hours per day, corrected for 30 minute lunch break
	public static final double hourlyWage = 40;
	public static final double overtimePremium = 1.4;			
	public static final double timeBufferTotalRoute = 0.66;		// 40 minutes buffer at the end of a total (day-spanning) route to make sure, during construction, there is still place for a last container -> dump -> wharf
																// TODO: hard-coded algorithm setting
	
	public static final double contProcTime  = 0.05;			// processing time, container
	public static final double procProcTime  = 0.25;			// processing time, waste processor
	public static final double transProcTime = 0.25;			// processing time, transshipment station
	
	public static final double avgSpeed = 15;				// km/h, average speed trucks are able to drive at
	
	public static final double transShipmentCosts = 2.6; 	// costs per container shipped from transshipment hub to waste processor, based on calculation in report
	
	public static final int householdWeight = 100;		// kg weight per m^3, household waste
	public static final int paperWeight     = 70;		// kg weight per m^3, paper waste
	public static final int glassWeight     = 300;		// kg weight per m^3, glass waste
	public static final int plasticWeight   = 50;		// kg weight per m^3, plastic waste
	
	public static final double vehicleCapacity = 9000;	// kg, weight capacity of each vehicle
	
	// replication/deletion approach parameters
	public static final int experimentWarmupPeriod = 25;		// standard: 25
	public static final int experimentTotalLength = 125;		// standard: 125
	public static final int experimentNoReplications = 3;		// standard: 3

}