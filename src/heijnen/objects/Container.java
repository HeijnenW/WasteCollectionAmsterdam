package heijnen.objects;

import org.apache.commons.math3.distribution.GammaDistribution;
import heijnen.data.Parameters;
import heijnen.planningObjects.Cluster;
import heijnen.planningObjects.Route;
import heijnen.simulation.Day;
import heijnen.simulation.ExperimentController;
import heijnen.simulation.SimulationController;


public class Container extends Point {
	
	////		FIELDS			////
	
	private String wasteFraction;
	public double capacity;
	public Route assignedRoute;			// implement everywhere
	
	public double shapePweek;
	public double scalePweek;	
				
	public double currFill;
	public double expCurrFill;
	public int lastDayEmptied;
	
	public int DED;						// desired emptying day
	public int EIL;						// expected interval length
	public double SCRC;					// single container routing costs
	
	public boolean overflowed;			// boolean that indicates if container was overflowed (so is mandatory now) yesterday
	
	public double clusterPriority;		// priority (distance/demand) used for capacitated k-means clustering
	public Cluster closestCluster;

	
	
	////		CONSTRUCTOR			////
	
	public Container(int indexDistanceMatrix, double lat, double lon, String wasteFraction, double capacity, double shapePweek, double scalePweek) {
		
		super(indexDistanceMatrix, lat, lon);
		this.setProcTime(Parameters.contProcTime);
		
		this.wasteFraction = wasteFraction;
		this.capacity = capacity;
		this.shapePweek = shapePweek;
		this.scalePweek = scalePweek;
		
		this.currFill = 0;
		this.expCurrFill = 0;
		this.lastDayEmptied = 0;
		
		this.overflowed = false;
	}


	
	////		FUNCTIONS			////
	
	private void setProcTime(double contproctime) {
		this.procTime = contproctime;		
	}

	public int noOfDaysUntilDED() {
		int noOfDays = this.DED - ExperimentController.currDay;
		return noOfDays;
	}
	
	public double expectedLoadStartDay(int dayNr) {
		
		double expLoad = 0;
		
		if (ExperimentController.sensorsEF == false) {
			if (overflowed == false) {
				int periodLength = dayNr - this.lastDayEmptied;
				double shapePeriod = (this.shapePweek/7) * periodLength;
				double scalePeriod = this.scalePweek;
				expLoad = shapePeriod * scalePeriod;
			}
			else if (overflowed == true) {
				double shapeDay = shapePweek / 7;
				double scaleDay = scalePweek;
				expLoad = capacity + 0.5 * (shapeDay * scaleDay);		// you know it's more than 500, for certainty add some demand, TODO: still a bit random how much should be added
			}
		}
		
		else if (ExperimentController.sensorsEF == true) {
			int periodLength = dayNr - Day.dayNr;
			double shapePeriod = (this.shapePweek/7) * periodLength;
			double scalePeriod = this.scalePweek;
			expLoad = this.expCurrFill + (shapePeriod * scalePeriod);
		}
		
		else {
			System.out.println("DEBUG: geen juiste parameter ingevuld");
		}

		return expLoad;
	}
	
	
	public double probFullStartDay(int dayNr) {
		int periodLength = 0;
		double shapePeriod;
		double scalePeriod;
		double prob = 0;
		double knownCurrFill = 0;
		
		if (ExperimentController.sensorsEF == false) {
			periodLength = dayNr - this.lastDayEmptied;
		}
		else if (ExperimentController.sensorsEF == true) {
			periodLength = dayNr - Day.dayNr;
			knownCurrFill = this.currFill;			// only non-zero in case of sensors
		}
		else {
			System.out.println("DEBUG: geen juiste parameter ingevoerd");
		}
		
		shapePeriod = (this.shapePweek/7) * periodLength;
		scalePeriod = this.scalePweek;
		
		if (periodLength >= 1) {
			prob = 1 - new GammaDistribution(shapePeriod, scalePeriod).cumulativeProbability(this.capacity - knownCurrFill);
		}
		
		return prob;
	}
	
	
	public double calcPenalty(int emptyingDay, double marginalCosts) {
		
		double penalty = 0;
		
		if (emptyingDay > this.DED) {					// too late
			penalty = penaltyEmptyingTooLate(emptyingDay) * ExperimentController.penaltyTooLateFactorEF;
		} else if (emptyingDay < this.DED) {			// too early
			penalty = (penaltyFactorEmptyingTooEarly(emptyingDay) * marginalCosts) * ExperimentController.penaltyTooEarlyFactorEF;
		} else if (emptyingDay == this.DED) {			// on time
			penalty = 0;
		}
		
		return penalty;
	}
	
	public double penaltyEmptyingTooLate(int emptyingDay) {
		// calculate overflow probability on emptyingDay
		double overflowProb = probFullStartDay(emptyingDay);
		
		// calculate penalty
		double penalty = (overflowProb - ExperimentController.AOPEF) * this.SCRC;
		return penalty;		
	}
	
	public double penaltyFactorEmptyingTooEarly(int emptyingDay) {
		double penaltyFactor = ((double)this.DED - (double)emptyingDay) / (double)this.EIL;
		return penaltyFactor;
	}
	
	
	public void dailyDepositUpdate() {
		// update expected current fill
		double shapeDay = shapePweek / 7;
		double scaleDay = scalePweek;
		
		GammaDistribution gamma = new GammaDistribution(shapeDay, scaleDay);
		double rnd = SimulationController.rgen.nextDouble();
		double addFill = gamma.inverseCumulativeProbability(rnd);				// TODO: test accuracy of method
		
		this.expCurrFill += shapeDay * scaleDay;
		this.currFill    += addFill;

		// alter if container overflows
		if (this.currFill > this.capacity) {
			this.overflowed = true;
			this.expCurrFill = capacity + 0.5 * (shapeDay * scaleDay);		// you know it's more than 500, for certainty add some demand, TODO: still a bit random how much should be added
			Day.noOverflows++;
			ExperimentController.overflowedContainers.add(this);
		}
	}
	
	public void emptyContainer() {
		// reset all fill levels
		this.currFill = 0;
		this.expCurrFill = 0;
		
		// reset DED
		this.DED = ExperimentController.currDay + Math.max(this.EIL, 1);
		
		// update lastDayEmptied
		this.lastDayEmptied = ExperimentController.currDay;
		
		// reset overflowed-status
		this.overflowed = false;
		ExperimentController.overflowedContainers.remove(this);
	}

	
	public void dailyUpdateSensorReadings() {
		// update currFill
		this.expCurrFill = this.currFill;
		
		// update DED
		double overflowProb = 0;
		int afterDays = 0;
		
		while (overflowProb < ExperimentController.AOPEF) {
			overflowProb = this.probFullStartDay(Day.dayNr + afterDays);
			afterDays++;
		}
		
		this.DED = (afterDays - 1) + Day.dayNr;
	}
	
}
