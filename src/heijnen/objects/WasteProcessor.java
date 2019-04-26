package heijnen.objects;

import heijnen.data.Parameters;

public class WasteProcessor extends Point {
	// FIELDS
	private String wasteFraction;
	
	// CONSTRUCTOR
	public WasteProcessor(int indexDistanceMatrix, double lat, double lon, String wasteFraction) {
		super(indexDistanceMatrix, lat, lon);
		this.setProcTime(Parameters.procProcTime);
		this.wasteFraction = wasteFraction;		
	}
	
	// FUNCTIONS
	
	
	private void setProcTime(double procproctime) {
		this.procTime = procproctime;
		
	}

	// GETTERS AND SETTERS
	public String getWasteFraction() {
		return this.wasteFraction;
	}
}
