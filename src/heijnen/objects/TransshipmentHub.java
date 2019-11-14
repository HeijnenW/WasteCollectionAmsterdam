package heijnen.objects;

import heijnen.data.Parameters;

public class TransshipmentHub extends Point {

	// FIELDS
	private int storageCapacity;			// capacity for the number of tanks
	public int storageUsed;					// number of full storage tanks stored
	public int storageFree;					// number of empty storage tanks kept
	
	// CONSTRUCTOR
	public TransshipmentHub(int indexDistanceMatrix, double lat, double lon, int storageCapacity, int storageUsed, int storageFree) {
		super(indexDistanceMatrix, lat, lon);
		this.setProcTime(Parameters.transProcTime);
		this.storageCapacity = storageCapacity;
		this.storageUsed = storageUsed;
		this.storageFree = storageFree;	
	}
	
	// FUNCTIONS
	
	private void setProcTime(double transproctime) {
		this.procTime = transproctime;		
	}

	// GETTERS AND SETTERS
	public int getStorageCapacity() {
		return this.storageCapacity;
	}
}
