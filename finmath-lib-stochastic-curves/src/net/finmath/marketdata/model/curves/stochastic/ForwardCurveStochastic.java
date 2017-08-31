/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2005
 */
package net.finmath.marketdata.model.curves.stochastic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.montecarlo.AbstractRandomVariableFactory;
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
public class ForwardCurveStochastic extends AbstractForwardCurve implements Serializable {

	private static final long serialVersionUID = -4126228588123963885L;

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
	public ForwardCurveStochastic(String name, 
			            LocalDate referenceDate, 
			            String paymentOffsetCode, 
			            BusinessdayCalendarInterface paymentBusinessdayCalendar, 
			            BusinessdayCalendarInterface.DateRollConvention paymentDateRollConvention, 
			            InterpolationMethod interpolationMethod, 
			            ExtrapolationMethod extrapolationMethod, 
			            InterpolationEntity interpolationEntity, 
			            InterpolationEntityForward interpolationEntityForward, 
			            String discountCurveName,
			            AbstractRandomVariableFactory factory) {
		super(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar, paymentDateRollConvention, interpolationMethod, 
				extrapolationMethod, interpolationEntity, discountCurveName, factory);
		this.interpolationEntityForward	= interpolationEntityForward;

		if(interpolationEntityForward == InterpolationEntityForward.DISCOUNTFACTOR) {
			super.addPoint(0.0, super.getRandomVariableFactory().createRandomVariable(1.0), false);
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
	public ForwardCurveStochastic(String name, LocalDate referenceDate, String paymentOffsetCode, InterpolationEntityForward interpolationEntityForward, String discountCurveName, AbstractRandomVariableFactory factory) {
		this(name, referenceDate, paymentOffsetCode, new BusinessdayCalendarExcludingWeekends(), BusinessdayCalendarInterface.DateRollConvention.FOLLOWING, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT, InterpolationEntity.VALUE, interpolationEntityForward, discountCurveName, factory);
	}

	/**
	 * Generate a forward curve using a given discount curve and payment offset.
	 * 
	 * @param name The name of this curve.
	 * @param referenceDate The reference date for this code, i.e., the date which defines t=0.
	 * @param paymentOffsetCode The maturity of the index modeled by this curve.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 */
	public ForwardCurveStochastic(String name, LocalDate referenceDate, String paymentOffsetCode, String discountCurveName, AbstractRandomVariableFactory factory) {
		this(name, referenceDate, paymentOffsetCode, InterpolationEntityForward.FORWARD, discountCurveName, factory);
	}

	/**
	 * Generate a forward curve using a given discount curve and payment offset.
	 * 
	 * @param name The name of this curve.
	 * @param paymentOffset The maturity of the underlying index modeled by this curve.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 */
	public ForwardCurveStochastic(String name, double paymentOffset, InterpolationEntityForward interpolationEntityForward, String discountCurveName, AbstractRandomVariableFactory factory) {
		// What is the use case of this constructor? Can it be deleted?
		super(name, null, paymentOffset, discountCurveName, factory);
		this.interpolationEntityForward	= interpolationEntityForward;
	}

	/**
	 * Create a forward curve from given times and given forwards.
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
	 * @param model The model to be used to fetch the discount curve, if needed.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, LocalDate referenceDate, String paymentOffsetCode, 
			BusinessdayCalendarInterface paymentBusinessdayCalendar, BusinessdayCalendarInterface.DateRollConvention paymentDateRollConvention, 
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity,
			InterpolationEntityForward interpolationEntityForward, String discountCurveName, AnalyticModelStochasticInterface model, double[] times, RandomVariableInterface[] givenForwards, AbstractRandomVariableFactory factory) {

		ForwardCurveStochastic forwardCurve = new ForwardCurveStochastic(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar, paymentDateRollConvention,
				interpolationMethod, extrapolationMethod, interpolationEntity, interpolationEntityForward, discountCurveName, factory);
	
		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			forwardCurve.addForward(model, times[timeIndex], givenForwards[timeIndex], false);
		}
	
		return forwardCurve;
	}

	/**
	 * Create a forward curve from given times and given forwards.
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
	 * @param model The model to be used to fetch the discount curve, if needed.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, Date referenceDate, String paymentOffsetCode, 
			BusinessdayCalendarInterface paymentBusinessdayCalendar, BusinessdayCalendarInterface.DateRollConvention paymentDateRollConvention, 
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity,
			InterpolationEntityForward interpolationEntityForward, String discountCurveName, AnalyticModelStochasticInterface model, double[] times, RandomVariableInterface[] givenForwards, AbstractRandomVariableFactory factory) {

		return createForwardCurveFromForwards(name, referenceDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), paymentOffsetCode, paymentBusinessdayCalendar, paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity, interpolationEntityForward, discountCurveName, model, times, givenForwards, factory);
	}

	/**
	 * Create a forward curve from given times and given forwards.
	 * 
	 * @param name The name of this curve.
	 * @param referenceDate The reference date for this code, i.e., the date which defines t=0.
	 * @param paymentOffsetCode The maturity of the index modeled by this curve.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 * @param model The model to be used to fetch the discount curve, if needed.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, LocalDate referenceDate, String paymentOffsetCode, String interpolationEntityForward, String discountCurveName, AnalyticModelStochasticInterface model, double[] times, RandomVariableInterface[] givenForwards, AbstractRandomVariableFactory factory) {
		return createForwardCurveFromForwards(name, referenceDate, paymentOffsetCode, InterpolationEntityForward.valueOf(interpolationEntityForward), discountCurveName, model, times, givenForwards, factory);
	}

	/**
	 * Create a forward curve from given times and given forwards.
	 * 
	 * @param name The name of this curve.
	 * @param referenceDate The reference date for this code, i.e., the date which defines t=0.
	 * @param paymentOffsetCode The maturity of the index modeled by this curve.
	 * @param interpolationEntityForward Interpolation entity used for forward rate interpolation.
	 * @param discountCurveName The name of a discount curve associated with this index (associated with it's funding or collateralization), if any.
	 * @param model The model to be used to fetch the discount curve, if needed.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, LocalDate referenceDate, String paymentOffsetCode, InterpolationEntityForward interpolationEntityForward, String discountCurveName, AnalyticModelStochasticInterface model, double[] times, RandomVariableInterface[] givenForwards, AbstractRandomVariableFactory factory) {
		ForwardCurveStochastic forwardCurve = new ForwardCurveStochastic(name, referenceDate, paymentOffsetCode, interpolationEntityForward, discountCurveName, factory);

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			forwardCurve.addForward(model, times[timeIndex], givenForwards[timeIndex], false);
		}

		return forwardCurve;
	}

	/**
	 * Create a forward curve from given times and given forwards.
	 * 
	 * @param name The name of this curve.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @param paymentOffset The maturity of the underlying index modeled by this curve.
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, double[] times, RandomVariableInterface[] givenForwards, double paymentOffset, AbstractRandomVariableFactory factory) {
		ForwardCurveStochastic forwardCurve = new ForwardCurveStochastic(name, paymentOffset, InterpolationEntityForward.FORWARD, null, factory);

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			double	fixingTime	= times[timeIndex];
			boolean	isParameter	= (fixingTime > 0);
			forwardCurve.addForward(null, fixingTime, givenForwards[timeIndex], isParameter);
		}

		return forwardCurve;
	}

	/**
	 * Create a forward curve from given times and discount factors.
	 * 
	 * The forward curve will have times.length-1 fixing times from times[0] to times[times.length-2]
	 * where the forwards are calculated via 
	 * <code>
	 * 			forward[timeIndex] = (givenDiscountFactors[timeIndex]/givenDiscountFactors[timeIndex+1]-1.0) / (times[timeIndex+1] - times[timeIndex]);
	 * </code>
	 * Note: If time[0] &gt; 0, then the discount factor 1.0 will inserted at time 0.0
	 * 
	 * @param name The name of this curve.
	 * @param times A vector of given time points.
	 * @param givenDiscountFactors A vector of given discount factors (corresponding to the given time points).
	 * @param paymentOffset The maturity of the underlying index modeled by this curve.
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromDiscountFactors(String name, double[] times, RandomVariableInterface[] givenDiscountFactors, double paymentOffset, AbstractRandomVariableFactory factory) {
		ForwardCurveStochastic forwardCurve = new ForwardCurveStochastic(name, paymentOffset, InterpolationEntityForward.FORWARD, null, factory);

		if(times.length == 0) throw new IllegalArgumentException("Vector of times must not be empty.");

		if(times[0] > 0) {
			// Add first forward
			RandomVariableInterface one = factory.createRandomVariable(1.0);
			RandomVariableInterface forward = one.div(givenDiscountFactors[0].sub(1.0)).div(times[0]);
			forwardCurve.addForward(null, 0.0, forward, true);
		}

		for(int timeIndex=0; timeIndex<times.length-1;timeIndex++) {
			RandomVariableInterface 	forward		= givenDiscountFactors[timeIndex].div(givenDiscountFactors[timeIndex+1].sub(1.0)).div(times[timeIndex+1] - times[timeIndex]);
			double	fixingTime	= times[timeIndex];
			boolean	isParameter	= (fixingTime > 0);
			forwardCurve.addForward(null, fixingTime, forward, isParameter);
		}

		return forwardCurve;
	}

	/**
	 * Create a forward curve from given times and given forwards with respect to an associated discount curve and payment offset.
	 * 
	 * @param name The name of this curve.
	 * @param times A vector of given time points.
	 * @param givenForwards A vector of given forwards (corresponding to the given time points).
	 * @param model An analytic model providing a context. The discount curve (if needed) is obtained from this model.
	 * @param discountCurveName Name of the discount curve associated with this index (associated with it's funding or collateralization).
	 * @param paymentOffset Time between fixing and payment.
	 * @return A new ForwardCurve object.
	 */
	public static ForwardCurveStochastic createForwardCurveFromForwards(String name, double[] times, RandomVariableInterface[] givenForwards, AnalyticModelStochasticInterface model, String discountCurveName, double paymentOffset, AbstractRandomVariableFactory factory) {
		ForwardCurveStochastic forwardCurve = new ForwardCurveStochastic(name, paymentOffset, InterpolationEntityForward.FORWARD, discountCurveName, factory);

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			double	fixingTime	= times[timeIndex];
			boolean	isParameter	= (fixingTime > 0);
			forwardCurve.addForward(model, fixingTime, givenForwards[timeIndex], isParameter);
		}
		return forwardCurve;
	}

	@Override
	public RandomVariableInterface getForward(AnalyticModelStochasticInterface model, double fixingTime)
	{
		double paymentOffset = this.getPaymentOffset(fixingTime);

		RandomVariableInterface interpolationEntityForwardValue = this.getValue(model, fixingTime);
		switch(interpolationEntityForward) {
		case FORWARD:
		default:
			return interpolationEntityForwardValue;
		case FORWARD_TIMES_DISCOUNTFACTOR:
			if(model==null)
				throw new IllegalArgumentException("model==null. Not allowed for interpolationEntityForward " + interpolationEntityForward);
			return interpolationEntityForwardValue.div(model.getDiscountCurve(discountCurveName).getValue(model, fixingTime+paymentOffset));
		case ZERO:
		{
			RandomVariableInterface interpolationEntityForwardValue2 = this.getValue(model, fixingTime+paymentOffset);
			return interpolationEntityForwardValue2.mult(fixingTime+paymentOffset).sub(interpolationEntityForwardValue.mult(fixingTime)).exp().sub(1.0).div(paymentOffset);
		}
		case DISCOUNTFACTOR:
		{
			RandomVariableInterface interpolationEntityForwardValue2 = this.getValue(model, fixingTime+paymentOffset);
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
	public RandomVariableInterface getForward(AnalyticModelStochasticInterface model, double fixingTime, double paymentOffset)
	{
		// @TODO: A warning should be issued that this implementation does not use 
//		if(paymentOffset != this.getPaymentOffset(fixingTime)) {
//			Logger.getLogger("net.finmath").warning("Requesting forward with paymentOffsets not agreeing with original calibration. Requested: " + paymentOffsets +". Calibrated: " + getPaymentOffset(fixingTime) + ".");
//		}
		return this.getForward(model, fixingTime);
	}

	/**
	 * Add a forward to this curve.
	 * 
	 * @param model An analytic model providing a context. The discount curve (if needed) is obtained from this model.
	 * @param fixingTime The given fixing time.
	 * @param forward The given forward.
	 * @param isParameter If true, then this point is server via {@link #getParameter()} and changed via {@link #setParameter(double[])} and {@link #getCloneForParameter(double[])}, i.e., it can be calibrated.
	 */
	private void addForward(AnalyticModelStochasticInterface model, double fixingTime, RandomVariableInterface forward, boolean isParameter) {
		double interpolationEntitiyTime;
		RandomVariableInterface interpolationEntityForwardValue;
		switch(interpolationEntityForward) {
		case FORWARD:
		default:
			interpolationEntitiyTime = fixingTime;
			interpolationEntityForwardValue = forward;
			break;
		case FORWARD_TIMES_DISCOUNTFACTOR:
			interpolationEntitiyTime = fixingTime;
			interpolationEntityForwardValue = forward.mult(model.getDiscountCurve(discountCurveName).getValue(model, fixingTime+getPaymentOffset(fixingTime)));
			break;
		case ZERO:
		{
			double paymentOffset = getPaymentOffset(fixingTime);
			interpolationEntitiyTime = fixingTime+paymentOffset;
			interpolationEntityForwardValue = forward.mult(paymentOffset).add(1.0).log().div(paymentOffset);
		}
		break;
		case DISCOUNTFACTOR:
		{
			double paymentOffset = getPaymentOffset(fixingTime);
			interpolationEntitiyTime		= fixingTime+paymentOffset;
			interpolationEntityForwardValue = getValue(fixingTime).div(forward.mult(paymentOffset).add(1.0));
		}
		break;
		}
		super.addPoint(interpolationEntitiyTime, interpolationEntityForwardValue, isParameter);
	}

	@Override
	protected void addPoint(double time, RandomVariableInterface value, boolean isParameter) {
		if(interpolationEntityForward == InterpolationEntityForward.DISCOUNTFACTOR) time += getPaymentOffset(time);
		super.addPoint(time, value, isParameter);
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
	
	public AbstractRandomVariableFactory getRandomVariableFactory(){
		return super.getRandomVariableFactory();
	}
}
