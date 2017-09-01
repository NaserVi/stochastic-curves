/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 26.11.2012
 */
package net.finmath.marketdata.products.stochastic;

import net.finmath.marketdata.model.stochastic.AnalyticModelStochastic;
import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.montecarlo.AbstractRandomVariableFactory;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.marketdata.model.curves.stochastic.CurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveFromForwardCurveStochastic;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochastic;
import net.finmath.time.RegularSchedule;
import net.finmath.time.ScheduleInterface;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Implements the valuation of a swap using curves (discount curve, forward curve).
 * The swap valuation supports distinct discounting and forward curve.
 * Support for day counting is limited to the capabilities of
 * <code>TimeDiscretizationInterface</code>.
 * 
 * The swap is just the composition of two <code>SwapLeg</code>s, namely the
 * receiver leg and the payer leg. The value of the swap is the value of the receiver leg minus the value of the payer leg.
 * 
 * @author Christian Fries
 */
public class SwapStochastic extends AbstractAnalyticProductStochastic implements AnalyticProductInterfaceStochastic {

	private final AnalyticProductInterfaceStochastic legReceiver;
	private final AnalyticProductInterfaceStochastic legPayer;

	/**
	 * Create a swap which values as <code>legReceiver - legPayer</code>.
	 * 
	 * @param legReceiver The receiver leg.
	 * @param legPayer The payler leg.
	 */
	public SwapStochastic(AnalyticProductInterfaceStochastic legReceiver, AnalyticProductInterfaceStochastic legPayer) {
		super();
		this.legReceiver = legReceiver;
		this.legPayer = legPayer;
	}

	/**
	 * Creates a swap with notional exchange. The swap has a unit notional of 1.
	 * 
	 * @param scheduleReceiveLeg Schedule of the receiver leg.
	 * @param forwardCurveReceiveName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spreadReceive Fixed spread on the forward or fix rate.
	 * @param discountCurveReceiveName Name of the discount curve for the receiver leg.
	 * @param schedulePayLeg Schedule of the payer leg.
	 * @param forwardCurvePayName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spreadPay Fixed spread on the forward or fix rate.
	 * @param discountCurvePayName Name of the discount curve for the payer leg.
	 * @param isNotionalExchanged If true, both leg will pay notional at the beginning of each swap period and receive notional at the end of the swap period. Note that the cash flow date for the notional is periodStart and periodEnd (not fixingDate and paymentDate).
	 */
	public SwapStochastic(ScheduleInterface scheduleReceiveLeg,
			String forwardCurveReceiveName, double spreadReceive,
			String discountCurveReceiveName,
			ScheduleInterface schedulePayLeg,
			String forwardCurvePayName, double spreadPay,
			String discountCurvePayName,
			boolean isNotionalExchanged
			) {
		super();
		legReceiver		= new SwapLegStochastic(scheduleReceiveLeg, forwardCurveReceiveName, spreadReceive, discountCurveReceiveName, isNotionalExchanged /* Notional Exchange */);
		legPayer		= new SwapLegStochastic(schedulePayLeg, forwardCurvePayName, spreadPay, discountCurvePayName, isNotionalExchanged /* Notional Exchange */);
	}

	/**
	 * Creates a swap with notional exchange. The swap has a unit notional of 1.
	 * 
	 * @param scheduleReceiveLeg Schedule of the receiver leg.
	 * @param forwardCurveReceiveName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spreadReceive Fixed spread on the forward or fix rate.
	 * @param discountCurveReceiveName Name of the discount curve for the receiver leg.
	 * @param schedulePayLeg Schedule of the payer leg.
	 * @param forwardCurvePayName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spreadPay Fixed spread on the forward or fix rate.
	 * @param discountCurvePayName Name of the discount curve for the payer leg.
	 */
	public SwapStochastic(ScheduleInterface scheduleReceiveLeg,
			String forwardCurveReceiveName, double spreadReceive,
			String discountCurveReceiveName,
			ScheduleInterface schedulePayLeg,
			String forwardCurvePayName, double spreadPay,
			String discountCurvePayName) {
		this(scheduleReceiveLeg, forwardCurveReceiveName, spreadReceive, discountCurveReceiveName, schedulePayLeg, forwardCurvePayName, spreadPay, discountCurvePayName, true);
	}

	/**
	 * Creates a swap with notional exchange. The swap has a unit notional of 1.
	 * 
	 * @param scheduleReceiveLeg Schedule of the receiver leg.
	 * @param spreadReceive Fixed spread on the forward or fix rate.
	 * @param discountCurveReceiveName Name of the discount curve for the receiver leg.
	 * @param schedulePayLeg Schedule of the payer leg.
	 * @param forwardCurvePayName Name of the forward curve, leave empty if this is a fix leg.
	 * @param discountCurvePayName Name of the discount curve for the payer leg.
	 */
	public SwapStochastic(ScheduleInterface scheduleReceiveLeg,
			double spreadReceive,
			String discountCurveReceiveName,
			ScheduleInterface schedulePayLeg,
			String forwardCurvePayName,
			String discountCurvePayName) {
		this(scheduleReceiveLeg, null, spreadReceive, discountCurveReceiveName, schedulePayLeg, forwardCurvePayName, 0.0, discountCurvePayName, true);
	}


	@Override
	public RandomVariableInterface getValue(double evaluationTime, AnalyticModelStochasticInterface model) {	

		RandomVariableInterface valueReceiverLeg	= legReceiver.getValue(evaluationTime, model);
		RandomVariableInterface valuePayerLeg	= legPayer.getValue(evaluationTime, model);

		return valueReceiverLeg.sub(valuePayerLeg);
	}

	static public RandomVariableInterface getForwardSwapRate(TimeDiscretizationInterface fixTenor, TimeDiscretizationInterface floatTenor, ForwardCurveStochasticInterface forwardCurve) {
		return getForwardSwapRate(new RegularSchedule(fixTenor), new RegularSchedule(floatTenor), forwardCurve);
	}

	static public RandomVariableInterface getForwardSwapRate(TimeDiscretizationInterface fixTenor, TimeDiscretizationInterface floatTenor, ForwardCurveStochasticInterface forwardCurve, DiscountCurveStochasticInterface discountCurve) {
		AnalyticModelStochastic model = null;
		if(discountCurve != null) {
			model			= new AnalyticModelStochastic(new CurveStochasticInterface[] { forwardCurve, discountCurve });
		}
		return getForwardSwapRate(new RegularSchedule(fixTenor), new RegularSchedule(floatTenor), forwardCurve, model);
	}

	static public RandomVariableInterface getForwardSwapRate(ScheduleInterface fixSchedule, ScheduleInterface floatSchedule, ForwardCurveStochasticInterface forwardCurve) {
		return getForwardSwapRate(fixSchedule, floatSchedule, forwardCurve, null);
	}

	static public RandomVariableInterface getForwardSwapRate(ScheduleInterface fixSchedule, ScheduleInterface floatSchedule, ForwardCurveStochasticInterface forwardCurve, AnalyticModelStochasticInterface model) {
		DiscountCurveStochasticInterface discountCurve = model == null ? null : model.getDiscountCurve(forwardCurve.getDiscountCurveName());
		if(discountCurve == null) {
			discountCurve	= new DiscountCurveFromForwardCurveStochastic(forwardCurve.getName(), forwardCurve.getRandomVariableFactory());
			model			= new AnalyticModelStochastic(new CurveStochasticInterface[] { forwardCurve, discountCurve });
		}

		double evaluationTime = fixSchedule.getFixing(0);	// Consider all values
		RandomVariableInterface swapAnnuity	= SwapAnnuityStochastic.getSwapAnnuity(evaluationTime, fixSchedule, discountCurve, model);

		RandomVariableInterface floatLeg = ((ForwardCurveStochastic)forwardCurve).getRandomVariableFactory().createRandomVariable(0.0);
		for(int periodIndex=0; periodIndex<floatSchedule.getNumberOfPeriods(); periodIndex++) {
			double fixing			= floatSchedule.getFixing(periodIndex);
			double payment			= floatSchedule.getPayment(periodIndex);
			double periodLength		= floatSchedule.getPeriodLength(periodIndex);

			RandomVariableInterface forward			= forwardCurve.getForward(model, fixing);
			RandomVariableInterface discountFactor	= discountCurve.getDiscountFactor(model, payment);

			floatLeg = floatLeg.add(forward.mult(discountFactor).mult(periodLength));
		}

		RandomVariableInterface valueFloatLeg = floatLeg.div(discountCurve.getDiscountFactor(model, evaluationTime));

		return valueFloatLeg.div(swapAnnuity);
	}

	/**
	 * Return the receiver leg of the swap, i.e. the leg who's value is added to the swap value.
	 * 
	 * @return The receiver leg of the swap.
	 */
	public AnalyticProductInterfaceStochastic getLegReceiver() {
		return legReceiver;
	}

	/**
	 * Return the payer leg of the swap, i.e. the leg who's value is subtracted from the swap value.
	 * 
	 * @return The payer leg of the swap.
	 */
	public AnalyticProductInterfaceStochastic getLegPayer() {
		return legPayer;
	}

	@Override
	public String toString() {
		return "Swap [legReceiver=" + legReceiver + ", legPayer=" + legPayer
				+ "]";
	}
}
