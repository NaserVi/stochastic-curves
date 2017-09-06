/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2005
 */
package net.finmath.marketdata.model.curves.stochastic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurve.InterpolationEntityForward;
import net.finmath.marketdata.model.stochastic.AnalyticModelInterface;
import net.finmath.montecarlo.AbstractRandomVariableFactory;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.model.AbstractModel;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;

/**
 * A container for a forward (rate) curve. The forward curve is based on the {@link net.finmath.marketdata.model.curves.Curve} class.
 * It thus features all interpolation and extrapolation methods and interpolation entities as {@link net.finmath.marketdata.model.curves.Curve}.
 * 
 * The forward F(t) of an index is such that  * F(t) * D(t+p) equals the market price of the corresponding 
 * index fixed in t and paid in t+d, where t is the fixing time of the index and t+p is the payment time of the index. 
 * F(t) is the corresponding forward and D is the associated discount curve.
 * 
 * @author Christian Fries
 */
public class ForwardCurveFromMonteCarloLiborModel extends AbstractForwardCurve implements Serializable {

	private static final long serialVersionUID = -4126228588123963885L;
	private LIBORModelMonteCarloSimulationInterface	  model;
	private double                                    startTime;
	private ForwardCurve                              forwardCurve =null;
	/**
	 * Additional choice of interpolation entities for forward curves.
	 */
	public enum InterpolationEntityForward {
		/** Interpolation is performed on the forward **/
		FORWARD,
		/** Interpolation is performed on the value = forward * discount factor **/
		FORWARD_TIMES_DISCOUNTFACTOR,
		/** Interpolation is performed on the zero rate **/
		ZERO,
		/** Interpolation is performed on the (synthetic) discount factor **/
		DISCOUNTFACTOR
	}

	private InterpolationEntityForward	interpolationEntityForward = InterpolationEntityForward.FORWARD;

	/**
	 * Generate a forward curve using a given discount curve and payment offset. 
	 * 
	 * @param name The name of this curve.
	 * @param referenceDate The reference date for this code, i.e., the date which defines t=0.
	 * @param paymentOffsetCode The maturity of the index modeled by this curve.
	 * @param paymentBusinessdayCalendar The business day calendar used for adjusting the payment date.
	 * @param paymentDateRollConvention The date roll convention used for adjusting the payment date.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 */
	public ForwardCurveFromMonteCarloLiborModel(String name, 
			            LocalDate referenceDate, 
			            String paymentOffsetCode, 
			            BusinessdayCalendarInterface paymentBusinessdayCalendar, 
			            BusinessdayCalendarInterface.DateRollConvention paymentDateRollConvention, 
			            InterpolationMethod interpolationMethod, 
			            ExtrapolationMethod extrapolationMethod, 
			            InterpolationEntity interpolationEntity, 
			            InterpolationEntityForward interpolationEntityForward, 
			            LIBORModelMonteCarloSimulationInterface model,
			            double startTime) {
		super(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar, paymentDateRollConvention, interpolationMethod, 
				extrapolationMethod, interpolationEntity, null);
		this.interpolationEntityForward	= interpolationEntityForward;
		this.model                      = model;
		this.startTime                  = startTime;
		if(interpolationEntityForward == InterpolationEntityForward.DISCOUNTFACTOR) {
			super.addPoint(0.0, model.getRandomVariableForConstant(1.0), false);
		}
	}
	
	

	/**
	 * Generate a forward curve using a given discount curve and payment offset.
	 * 
	 * @param name The name of this curve.
	 * @param referenceDate The reference date for this code, i.e., the date which defines t=0.
	 * @param paymentOffsetCode The maturity of the index modeled by this curve.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 */
	public ForwardCurveFromMonteCarloLiborModel(String name, LocalDate referenceDate, String paymentOffsetCode, InterpolationEntityForward interpolationEntityForward, LIBORModelMonteCarloSimulationInterface model, double startTime) {
		this(name, referenceDate, paymentOffsetCode, new BusinessdayCalendarExcludingWeekends(), BusinessdayCalendarInterface.DateRollConvention.FOLLOWING, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT, InterpolationEntity.VALUE, interpolationEntityForward, model, startTime);
	}

	

	/**
	 * Generate a forward curve using a given discount curve and payment offset.
	 * 
	 * @param name The name of this curve.
	 * @param paymentOffset The maturity of the underlying index modeled by this curve.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 */
	public ForwardCurveFromMonteCarloLiborModel(String name, double paymentOffset, LIBORModelMonteCarloSimulationInterface model, double startTime) {
		super(name, null, paymentOffset, null);
		this.interpolationEntityForward	= InterpolationEntityForward.FORWARD;
		this.model                      = model;
		this.startTime                  = startTime;
	}



	@Override
	public RandomVariableInterface getForward(AnalyticModelInterface model, double fixingTime){
		return getForward(fixingTime) ;
	}
	
	
	public RandomVariableInterface getForward(double fixingTime){
		if(forwardCurve == null){
			try {
				doCreateForwardCurve();
			} catch (CalculationException e) {
				e.printStackTrace();
			}
		}
		
		double paymentOffset = this.getPaymentOffset(fixingTime);

		RandomVariableInterface interpolationEntityForwardValue = forwardCurve.getValue(fixingTime);
		switch(interpolationEntityForward) {
		case FORWARD:
		default:
			return interpolationEntityForwardValue;
		case ZERO:
		{
			RandomVariableInterface interpolationEntityForwardValue2 = forwardCurve.getValue(fixingTime+paymentOffset);
			return interpolationEntityForwardValue2.mult(fixingTime+paymentOffset).sub(interpolationEntityForwardValue.mult(fixingTime)).exp().sub(1.0).div(paymentOffset);
		}
		case DISCOUNTFACTOR:
		{
			RandomVariableInterface interpolationEntityForwardValue2 = forwardCurve.getValue(fixingTime+paymentOffset);
			return interpolationEntityForwardValue.div(interpolationEntityForwardValue2).sub(1.0).div(paymentOffset);
		}
		}
	}

	/**
	 * Returns the forward for the corresponding fixing time.
	 * 
	 * <b>Note:</b> This implementation currently ignores the provided <code>paymentOffset</code>.
	 * Instead it uses the payment offset calculate from the curve specification.
	 * 
	 * @param model An analytic model providing a context. Some curves do not need this (can be null).
	 * @param fixingTime The fixing time of the index associated with this forward curve.
     * @param paymentOffset The payment offset (as internal day count fraction) specifying the payment of this index. Used only as a fallback and/or consistency check.
	 * 
	 * @return The forward.
	 */
	@Override
	public RandomVariableInterface getForward(AnalyticModelInterface model, double fixingTime, double paymentOffset)
	{
		return this.getForward(fixingTime);
	}


	@Override
	protected void addPoint(double time, RandomVariableInterface value, boolean isParameter) {
		if(interpolationEntityForward == InterpolationEntityForward.DISCOUNTFACTOR) time += getPaymentOffset(time);
		if(forwardCurve == null){
			try {
				doCreateForwardCurve();
			} catch (CalculationException e) {
				e.printStackTrace();
			}
		}
		forwardCurve.addPoint(time, value, isParameter);
	}
	
	/**
	 * Returns the special interpolation method used for this forward curve.
	 * 
	 * @return The interpolation method used for the forward.
	 */
	public InterpolationEntityForward getInterpolationEntityForward() {
		return interpolationEntityForward;
	}

	@Override
	public String toString() {
		return "ForwardCurve [" + super.toString() + ", interpolationEntityForward=" + interpolationEntityForward + "]";
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
}