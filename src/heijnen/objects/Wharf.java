package heijnen.objects;

public class Wharf extends Point {
	
	// CONSTRUCTOR
	public Wharf(int indexDistanceMatrix, double lat, double lon) {
		super(indexDistanceMatrix, lat, lon);
		this.setProcTime(0);
	}

	private void setProcTime(int i) {
		this.procTime = i;
	}
	
}
