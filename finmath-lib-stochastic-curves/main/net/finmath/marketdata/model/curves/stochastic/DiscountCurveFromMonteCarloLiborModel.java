package net.finmath.marketdata.model.curves.stochastic;
/* (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
*
* Created on 05.09.2017
*/

import java.io.Serializable;
import java.util.ArrayList;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.stochastic.AnalyticModelInterface;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.model.AbstractModel;
import net.finmath.stochastic.RandomVariableInterface;

/**
* A discount curve built from forward rates given by a Monte Carlo Libor model.
* 
* The discount factors <i>df(t)</i> are defined at <i>t = k * d</i>
* for integers <i>k</i> via
* <i>df(t+d) = df(t) / (1 + f(t) * d)</i> and
* for <i>t = k * d</i> and <i>0 &lt; r &lt; d</i>
* via <i>df(t+r) = df(t) / (1 + f(t) * r)</i>
* where <i>d</i> is a given the payment offset and <i>f(t)</i> is the forward curve.
* 
* <p>
* <i>Note that a special interpolation is performed for in-between points.
* Hence, creating a {@link ForwardCurveFromMonteCarloLiborModel} and from it
* a DiscountCurveFromForwardCurve will not recover the original curve
* since interpolation points may be lost.
* </i>
* </p>
* 
* @author Christian Fries
*/
public class DiscountCurveFromMonteCarloLiborModel extends AbstractCurve implements Serializable, DiscountCurveInterface {

	private static final long serialVersionUID = -4126228588123963885L;

	private LIBORModelMonteCarloSimulationInterface	  model;
	private double                                    startTime;
	private String                        			  forwardCurveName;
	private ForwardCurveInterface 					  forwardCurve = null;

	private final double		  timeScaling;
	
	/**
	 * Create a discount curve using a given forward curve.
	 * The discount factors df(t) are defined at t = k * d for integers k
	 * via df(t+d) = df(t) / (1 + f(t) * d) and
	 * for t = k * d and 0 &lt; r &lt; d
	 * via df(t+r) = df(t) / (1 + f(t) * r)
	 * where d is a given the payment offset and f(t) is the forward curve.
	 * 
	 * @param forwardCurveName The name of the forward curve used for calculation of the discount factors.
	 * @param periodLengthTimeScaling A scaling factor applied to d, adjusting for the internal double time to the period length daycount fraction (note that this may only be an approximate solution to capture daycount effects).
	 */
	public DiscountCurveFromMonteCarloLiborModel(String forwardCurveName, LIBORModelMonteCarloSimulationInterface model, double startTime, double periodLengthTimeScaling) {
		super("DiscountCurveFromMonteCarloLiborModelForForwardCurve(" + forwardCurveName + ")", null);

		this.forwardCurveName	= forwardCurveName;
		this.model              = model;
		this.startTime          = startTime;
		this.timeScaling		= periodLengthTimeScaling;
	}

	/**
	 * Create a discount curve using a given forward curve.
	 * The discount factors df(t) are defined at t = k * d for integers k
	 * via df(t+d) = df(t) / (1 + f(t) * d) and
	 * for t = k * d and 0 &lt; r &lt; d
	 * via df(t+r) = df(t) / (1 + f(t) * r)
	 * where d is a given the payment offset and f(t) is the forward curve.
	 * 
	 * @param forwardCurveName The name of the forward curve used for calculation of the discount factors.
	 */
	public DiscountCurveFromMonteCarloLiborModel(String forwardCurveName, LIBORModelMonteCarloSimulationInterface model, double startTime) {
		this(forwardCurveName, model, startTime, /*timeScaling*/1.0);
	}
  
	/**
	 * Create a discount curve using a given forward curve.
	 * The discount factors df(t) are defined at t = k * d for integers k
	 * via df(t+d) = df(t) / (1 + f(t) * d) and
	 * for t = k * d and 0 &lt; r &lt; d
	 * via df(t+r) = df(t) / (1 + f(t) * r)
	 * where d is a given the payment offset and f(t) is the forward curve.
	 * 
	 * @param forwardCurveName The name of the forward curve used for calculation of the discount factors.
	 */
	public DiscountCurveFromMonteCarloLiborModel(String forwardCurveName, LIBORModelMonteCarloSimulationInterface model) {
		this(forwardCurveName, model, /*startTime*/ 0.0, /*timeScaling*/1.0);
	}



	/* (non-Javadoc)
	 * @see net.finmath.marketdata.DiscountCurveInterface#getDiscountFactor(double)
	 */
	@Override
	public RandomVariableInterface getDiscountFactor(double maturity) {
		if(this.forwardCurve==null){
			try {
				doCreateForwardCurve();
			} catch (CalculationException e) {
				e.printStackTrace();
			}
		}
		
		double time = 0;
		double paymentOffset = 0;
		RandomVariableInterface discountFactor = new RandomVariable(1.0);
		while(time < maturity) {
			paymentOffset	= forwardCurve.getPaymentOffset(time);
			if(paymentOffset <= 0) throw new RuntimeException("Trying to calculate a discount curve from a forward curve with non-positive payment offset.");
			discountFactor = forwardCurve.getForward(null,time).mult(Math.min(paymentOffset, maturity-time) * timeScaling).add(1.0).pow(-1.0).mult(discountFactor);
			time += paymentOffset;
		}
		return discountFactor;
		
	}
	
	private void doCreateForwardCurve() throws CalculationException{
		
		int timeIndex	= ((AbstractModel) model).getTimeIndex(startTime);
		// Get all Libors at timeIndex which are not yet fixed (others null) and times for the timeDiscretization of the curves
		ArrayList<RandomVariableInterface> liborsAtTimeIndex = new ArrayList<RandomVariableInterface>();
		int firstLiborIndex = model.getLiborPeriodDiscretization().getTimeIndexNearestGreaterOrEqual(startTime);
		double firstLiborTime = model.getLiborPeriodDiscretization().getTime(firstLiborIndex);
		if(firstLiborTime>startTime) liborsAtTimeIndex.add(model.getLIBOR(startTime, startTime, firstLiborTime));
		// Vector of times for the forward curve
		double[] times = new double[firstLiborTime==startTime ? (model.getNumberOfLibors()-firstLiborIndex) : (model.getNumberOfLibors()-firstLiborIndex+1)];
		times[0]=0;
		int indexOffset = firstLiborTime==startTime ? 0 : 1;
		for(int i=firstLiborIndex;i<model.getNumberOfLibors();i++) {
			    liborsAtTimeIndex.add(model.getLIBOR(timeIndex,i));
			    times[i-firstLiborIndex+indexOffset]=model.getLiborPeriodDiscretization().getTime(i)-startTime;
		}

		RandomVariableInterface[] libors = liborsAtTimeIndex.toArray(new RandomVariableInterface[liborsAtTimeIndex.size()]);
	    this.forwardCurve = ForwardCurve.createForwardCurveFromForwards("", times, libors, model.getLiborPeriodDiscretization().getTimeStep(firstLiborIndex));
				
	}

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.DiscountCurveInterface#getDiscountFactor(double)
	 */
	@Override
	public RandomVariableInterface getDiscountFactor(AnalyticModelInterface model, double maturity) {
		return getDiscountFactor(maturity);
		
	}

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.curves.CurveInterface#getValue(double)
	 */
	@Override
	public RandomVariableInterface getValue(AnalyticModelInterface analyticModel, double time) {
		return getDiscountFactor(analyticModel, time);
	}



	@Override
	public CurveBuilderInterface getCloneBuilder() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((forwardCurve == null) ? 0 : forwardCurve.hashCode());
		result = prime * result + ((forwardCurveName == null) ? 0 : forwardCurveName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(timeScaling);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscountCurveFromMonteCarloLiborModel other = (DiscountCurveFromMonteCarloLiborModel) obj;
		if (forwardCurve == null) {
			if (other.forwardCurve != null)
				return false;
		} else if (!forwardCurve.equals(other.forwardCurve))
			return false;
		if (forwardCurveName == null) {
			if (other.forwardCurveName != null)
				return false;
		} else if (!forwardCurveName.equals(other.forwardCurveName))
			return false;
		if (Double.doubleToLongBits(timeScaling) != Double
				.doubleToLongBits(other.timeScaling))
			return false;
		return true;
	}
	
}
