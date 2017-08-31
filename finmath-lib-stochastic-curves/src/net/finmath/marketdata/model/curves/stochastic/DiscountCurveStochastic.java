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

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.montecarlo.AbstractRandomVariableFactory;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Implementation of a discount factor curve based on {@link net.finmath.marketdata.model.curves.Curve}. The discount curve is based on the {@link net.finmath.marketdata.model.curves.Curve} class.
 * 
 * It thus features all interpolation and extrapolation methods and interpolation entities
 * as {@link net.finmath.marketdata.model.curves.Curve} and implements the {@link net.finmath.marketdata.model.curves.DiscountCurveInterface}.
 * 
 * Note that this version of the DiscountCurve will no longer make the
 * assumption that at t=0 its value is 1.0. Such a norming is not
 * necessary since valuation will always divide by the corresponding
 * discount factor at evaluation time. See the implementation of {@link net.finmath.marketdata.products.SwapLeg}
 * for an example.
 * 
 * @author Christian Fries
 * @see net.finmath.marketdata.products.SwapLeg
 * @see net.finmath.marketdata.model.curves.Curve
 */
public class DiscountCurveStochastic extends CurveStochastic implements Serializable, DiscountCurveInterface {

	private static final long serialVersionUID = -4126228588123963885L;

	/**
	 * Create an empty discount curve using default interpolation and extrapolation methods.
	 * 
	 * @param name The name of this discount curve.
	 */
	private DiscountCurveStochastic(String name, AbstractRandomVariableFactory factory) {
		super(name, null, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT, InterpolationEntity.LOG_OF_VALUE_PER_TIME, factory);
	}

	/**
	 * Create an empty discount curve using given interpolation and extrapolation methods.
	 *
	 * @param name The name of this discount curve.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 */
	private DiscountCurveStochastic(String name, InterpolationMethod interpolationMethod,
			ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory){

		super(name, null, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}


	/**
	 * Create an empty discount curve using given interpolation and extrapolation methods.
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 */
	private DiscountCurveStochastic(String name, LocalDate referenceDate, InterpolationMethod interpolationMethod,
			ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory){

		super(name, referenceDate, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given times and given discount factors using given interpolation and extrapolation methods.
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenDiscountFactors Array of corresponding discount factors.
	 * @param isParameter Array of booleans specifying whether this point is served "as as parameter", e.g., whether it is calibrates (e.g. using CalibratedCurves).
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromDiscountFactors(
			String name, LocalDate referenceDate,
			double[] times, RandomVariableInterface[] givenDiscountFactors, boolean[] isParameter,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {

		DiscountCurveStochastic discountFactors = new DiscountCurveStochastic(name, referenceDate, interpolationMethod, extrapolationMethod, interpolationEntity, factory);

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			discountFactors.addDiscountFactor(times[timeIndex], givenDiscountFactors[timeIndex], isParameter != null && isParameter[timeIndex]);
		}

		return discountFactors;
	}

	/**
	 * Create a discount curve from given times and given discount factors using given interpolation and extrapolation methods.
	 *
	 * @param name The name of this discount curve.
	 * @param times Array of times as doubles.
	 * @param givenDiscountFactors Array of corresponding discount factors.
	 * @param isParameter Array of booleans specifying whether this point is served "as as parameter", e.g., whether it is calibrates (e.g. using CalibratedCurves).
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromDiscountFactors(
			String name,
			double[] times,
			RandomVariableInterface[] givenDiscountFactors,
			boolean[] isParameter,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		return createDiscountCurveFromDiscountFactors(name, null, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given times and given discount factors using given interpolation and extrapolation methods.
	 *
	 * @param name The name of this discount curve.
	 * @param times Array of times as doubles.
	 * @param givenDiscountFactors Array of corresponding discount factors.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromDiscountFactors(
			String name,
			double[] times,
			RandomVariableInterface[] givenDiscountFactors,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		boolean[] isParameter = new boolean[times.length];
		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			isParameter[timeIndex] = times[timeIndex] > 0;
		}
		
		return createDiscountCurveFromDiscountFactors(name, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given times and given discount factors using default interpolation and extrapolation methods.
	 * 
	 * @param name The name of this discount curve.
	 * @param times Array of times as doubles.
	 * @param givenDiscountFactors Array of corresponding discount factors.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromDiscountFactors(String name, double[] times, RandomVariableInterface[] givenDiscountFactors, AbstractRandomVariableFactory factory) {
		DiscountCurveStochastic discountFactors = new DiscountCurveStochastic(name, factory);

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			discountFactors.addDiscountFactor(times[timeIndex], givenDiscountFactors[timeIndex], times[timeIndex] > 0);
		}

		return discountFactors;
	}

	/**
	 * Create a discount curve from given times and given zero rates using given interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.exp(- givenZeroRates[timeIndex] * times[timeIndex]);
	 * </code>
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenZeroRates Array of corresponding zero rates.
	 * @param isParameter Array of booleans specifying whether this point is served "as as parameter", e.g., whether it is calibrates (e.g. using CalibratedCurves).
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromZeroRates(
			String name, LocalDate referenceDate,
			double[] times, RandomVariableInterface[] givenZeroRates, boolean[] isParameter,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		
		RandomVariableInterface[] givenDiscountFactors = new RandomVariableInterface[givenZeroRates.length];

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			givenDiscountFactors[timeIndex] = givenZeroRates[timeIndex].mult(-times[timeIndex]).exp();
		}

		return createDiscountCurveFromDiscountFactors(name, referenceDate, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}
	
	/**
	 * Create a discount curve from given times and given zero rates using given interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.exp(- givenZeroRates[timeIndex] * times[timeIndex]);
	 * </code>
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenZeroRates Array of corresponding zero rates.
	 * @param isParameter Array of booleans specifying whether this point is served "as as parameter", e.g., whether it is calibrates (e.g. using CalibratedCurves).
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromZeroRates(
			String name, Date referenceDate,
			double[] times, RandomVariableInterface[] givenZeroRates, boolean[] isParameter,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		
		return createDiscountCurveFromZeroRates(name, referenceDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), times, givenZeroRates, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}



	/**
	 * Create a discount curve from given times and given zero rates using given interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.exp(- givenZeroRates[timeIndex] * times[timeIndex]);
	 * </code>
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenZeroRates Array of corresponding zero rates.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromZeroRates(
			String name, LocalDate referenceDate,
			double[] times, RandomVariableInterface[] givenZeroRates,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		
		RandomVariableInterface[] givenDiscountFactors = new RandomVariableInterface[givenZeroRates.length];
		boolean[] isParameter = new boolean[givenZeroRates.length];

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			givenDiscountFactors[timeIndex] = givenZeroRates[timeIndex].mult(-times[timeIndex]).exp();
			isParameter[timeIndex] = false;
		}

		return createDiscountCurveFromDiscountFactors(name, referenceDate, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given times and given zero rates using default interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.exp(- givenZeroRates[timeIndex] * times[timeIndex]);
	 * </code>
	 * 
	 * @param name The name of this discount curve.
	 * @param times Array of times as doubles.
	 * @param givenZeroRates Array of corresponding zero rates.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromZeroRates(String name, double[] times, RandomVariableInterface[] givenZeroRates, AbstractRandomVariableFactory factory) {
		RandomVariableInterface[] givenDiscountFactors = new RandomVariableInterface[givenZeroRates.length];

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			givenDiscountFactors[timeIndex] = givenZeroRates[timeIndex].mult(-times[timeIndex]).exp();
		}

		return createDiscountCurveFromDiscountFactors(name, times, givenDiscountFactors, factory);
	}

	/**
	 * Create a discount curve from given times and given annualized zero rates using given interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.pow(1.0 + givenAnnualizedZeroRates[timeIndex], -times[timeIndex]);
	 * </code>
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenAnnualizedZeroRates Array of corresponding zero rates.
	 * @param isParameter Array of booleans specifying whether this point is served "as as parameter", e.g., whether it is calibrates (e.g. using CalibratedCurves).
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromAnnualizedZeroRates(
			String name, LocalDate referenceDate,
			double[] times, RandomVariableInterface[] givenAnnualizedZeroRates, boolean[] isParameter,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		
		RandomVariableInterface[] givenDiscountFactors = new RandomVariableInterface[givenAnnualizedZeroRates.length];

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			givenDiscountFactors[timeIndex] = givenAnnualizedZeroRates[timeIndex].add(1.0).pow(-times[timeIndex]);
		}

		return createDiscountCurveFromDiscountFactors(name, referenceDate, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given times and given annualized zero rates using given interpolation and extrapolation methods.
	 * The discount factor is determined by 
	 * <code>
	 * 		givenDiscountFactors[timeIndex] = Math.pow(1.0 + givenAnnualizedZeroRates[timeIndex], -times[timeIndex]);
	 * </code>
	 *
	 * @param name The name of this discount curve.
	 * @param referenceDate The reference date for this curve, i.e., the date which defined t=0.
	 * @param times Array of times as doubles.
	 * @param givenAnnualizedZeroRates Array of corresponding zero rates.
	 * @param interpolationMethod The interpolation method used for the curve.
	 * @param extrapolationMethod The extrapolation method used for the curve.
	 * @param interpolationEntity The entity interpolated/extrapolated.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveStochastic createDiscountCurveFromAnnualizedZeroRates(
			String name, LocalDate referenceDate,
			double[] times, RandomVariableInterface[] givenAnnualizedZeroRates,
			InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity, AbstractRandomVariableFactory factory) {
		
		RandomVariableInterface[] givenDiscountFactors = new RandomVariableInterface[givenAnnualizedZeroRates.length];
		boolean[] isParameter = new boolean[givenAnnualizedZeroRates.length];

		for(int timeIndex=0; timeIndex<times.length;timeIndex++) {
			givenDiscountFactors[timeIndex] = givenAnnualizedZeroRates[timeIndex].add(1.0).pow(-times[timeIndex]);
			isParameter[timeIndex] = false;
		}

		return createDiscountCurveFromDiscountFactors(name, referenceDate, times, givenDiscountFactors, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity, factory);
	}

	/**
	 * Create a discount curve from given time discretization and forward rates.
	 * This function is provided for "single interest rate curve" frameworks.
	 * 
	 * @param name The name of this discount curve.
	 * @param tenor Time discretization for the forward rates
	 * @param forwardRates Array of forward rates.
	 * @return A new discount factor object.
	 */
	public static DiscountCurveInterface createDiscountFactorsFromForwardRates(String name, TimeDiscretizationInterface tenor, RandomVariableInterface [] forwardRates, AbstractRandomVariableFactory factory) {
		DiscountCurveStochastic discountFactors = new DiscountCurveStochastic(name, factory);

		RandomVariableInterface  df = factory.createRandomVariable(1.0);
		for(int timeIndex=0; timeIndex<tenor.getNumberOfTimeSteps();timeIndex++) {
			df = df.div(forwardRates[timeIndex].mult(tenor.getTimeStep(timeIndex)).add(1.0));
			discountFactors.addDiscountFactor(tenor.getTime(timeIndex+1), df, tenor.getTime(timeIndex+1) > 0);
		}

		return discountFactors;
	}
	
	// INSERTED
	public static RandomVariableInterface[] createZeroRates(double time, double[] maturities, LIBORModelMonteCarloSimulationInterface model) throws CalculationException{
	
		// get time index of first libor fixing time after time
		int firstLiborIndex = model.getLiborPeriodDiscretization().getTimeIndexNearestGreaterOrEqual(time);
		int remainingLibors = model.getNumberOfLibors()-firstLiborIndex;
		RandomVariableInterface[] forwardRates;
		double[] liborTimes;
		int indexOffset;
		double periodStart;
		double periodEnd;
		if(model.getLiborPeriodDiscretization().getTime(firstLiborIndex)>time){
		   periodStart = time;
		   periodEnd   = model.getLiborPeriodDiscretization().getTime(firstLiborIndex);
		   forwardRates = new RandomVariableInterface[remainingLibors+1];
		   forwardRates[0] = model.getLIBOR(time, periodStart, periodEnd);
		   indexOffset = 1;
		   liborTimes = new double[forwardRates.length+1];
		   liborTimes[0] = 0;
		} else {
		   forwardRates = new RandomVariableInterface[remainingLibors];
		   indexOffset = 0;
		   liborTimes = new double[forwardRates.length+1];
		}
		for(int liborIndex=firstLiborIndex;liborIndex<model.getNumberOfLibors();liborIndex++){
			periodStart = model.getLiborPeriodDiscretization().getTime(liborIndex);
			periodEnd   = model.getLiborPeriodDiscretization().getTime(liborIndex+1);
			forwardRates[liborIndex-firstLiborIndex+indexOffset]=model.getLIBOR(time, periodStart, periodEnd);
		}
		
		for(int i=indexOffset;i<liborTimes.length;i++) liborTimes[i]=model.getLiborPeriod(firstLiborIndex+i-indexOffset)-time;
		DiscountCurveStochastic df = (DiscountCurveStochastic) createDiscountFactorsFromForwardRates("",new TimeDiscretization(liborTimes), forwardRates, ((LIBORMarketModel)model).getRandomVariableFactory()); 
	    return df.getZeroRates(maturities);
	}
	

	/**
	 * Returns the zero rate for a given maturity, i.e., -ln(df(T)) / T where T is the given maturity and df(T) is
	 * the discount factor at time $T$.
	 * 
	 * @param maturity The given maturity.
	 * @return The zero rate.
	 */
	public RandomVariableInterface  getZeroRate(double maturity)
	{
		if(maturity == 0) return this.getZeroRate(1.0E-14);

		return getDiscountFactor(maturity).log().div(-maturity);
	}

	/**
	 * Returns the zero rates for a given vector maturities.
	 * 
	 * @param maturities The given maturities.
	 * @return The zero rates.
	 */
	public RandomVariableInterface[] getZeroRates(double[] maturities)
	{
		RandomVariableInterface[] values = new RandomVariableInterface [maturities.length];

		for(int i=0; i<maturities.length; i++) values[i] = getZeroRate(maturities[i]);

		return values;
	}

	protected void addDiscountFactor(double maturity, RandomVariableInterface discountFactor, boolean isParameter) {
		this.addPoint(maturity, discountFactor, isParameter);
	}

	@Override
	public String toString() {
		return "DiscountCurve [" + super.toString() + "]";
	}

	@Override
	public RandomVariableInterface getDiscountFactor(double maturity) {
		return getValue(null, maturity);
	}

	@Override
	public RandomVariableInterface getDiscountFactor(AnalyticModelStochasticInterface model, double maturity) {
		return getValue(model, maturity);
	}
}
