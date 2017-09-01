/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 26.11.2012
 */
package net.finmath.marketdata.products.stochastic;

import net.finmath.marketdata.model.stochastic.AnalyticModelStochastic;
import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.marketdata.model.curves.stochastic.CurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochastic;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveFromForwardCurveStochastic;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochasticInterface;
import net.finmath.time.RegularSchedule;
import net.finmath.time.ScheduleInterface;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Implements the valuation of a swap annuity using curves (discount curve).
 * Support for day counting is limited to the capabilities of
 * <code>TimeDiscretizationInterface</code>.
 * 
 * @author Christian Fries
 */
public class SwapAnnuityStochastic extends AbstractAnalyticProductStochastic implements AnalyticProductInterfaceStochastic {

	private final ScheduleInterface	schedule;
	private final String			discountCurveName;

	/**
	 * Creates a swap annuity for a given schedule and discount curve.
	 * 
	 * @param schedule Tenor of the swap annuity.
	 * @param discountCurveName Name of the discount curve for the swap annuity.
	 */
	public SwapAnnuityStochastic(ScheduleInterface schedule, String discountCurveName) {
		super();
		this.schedule = schedule;
		this.discountCurveName = discountCurveName;
	}

	@Override
	public RandomVariableInterface getValue(double evaluationTime, AnalyticModelStochasticInterface model) {	
		DiscountCurveStochasticInterface discountCurve = (DiscountCurveStochasticInterface) model.getCurve(discountCurveName);

		return getSwapAnnuity(evaluationTime, schedule, discountCurve, model);
	}

	/**
	 * Function to calculate an (idealized) swap annuity for a given schedule and discount curve.
	 * 
	 * @param tenor The schedule discretization, i.e., the period start and end dates. End dates are considered payment dates and start of the next period.
	 * @param discountCurve The discount curve.
	 * @return The swap annuity.
	 */
	static public RandomVariableInterface getSwapAnnuity(TimeDiscretizationInterface tenor, DiscountCurveStochasticInterface discountCurve) {
		return getSwapAnnuity(new RegularSchedule(tenor), discountCurve);
	}

	/**
	 * Function to calculate an (idealized) single curve swap annuity for a given schedule and forward curve.
	 * The discount curve used to calculate the annuity is calculated from the forward curve using classical
	 * single curve interpretations of forwards and a default period length. The may be a crude approximation.
	 * 
	 * @param tenor The schedule discretization, i.e., the period start and end dates. End dates are considered payment dates and start of the next period.
	 * @param forwardCurve The forward curve.
	 * @return The swap annuity.
	 */
	static public RandomVariableInterface getSwapAnnuity(TimeDiscretizationInterface tenor, ForwardCurveStochasticInterface forwardCurve) {
		return getSwapAnnuity(new RegularSchedule(tenor), forwardCurve);
	}

	/**
	 * Function to calculate an (idealized) swap annuity for a given schedule and discount curve.
	 * 
	 * Note: This method will consider evaluationTime being 0, see {@link net.finmath.marketdata.products.SwapAnnuity#getSwapAnnuity(double, ScheduleInterface, DiscountCurveInterface, AnalyticModelStochasticInterface)}.
	 * 
	 * @param schedule The schedule discretization, i.e., the period start and end dates. End dates are considered payment dates and start of the next period.
	 * @param discountCurve The discount curve.
	 * @return The swap annuity.
	 */
	static public RandomVariableInterface getSwapAnnuity(ScheduleInterface schedule, DiscountCurveStochasticInterface discountCurve) {
		double evaluationTime = 0.0;	// Consider only payment time > 0
		return getSwapAnnuity(evaluationTime, schedule, discountCurve, null);
	}

	/**
	 * Function to calculate an (idealized) single curve swap annuity for a given schedule and forward curve.
	 * The discount curve used to calculate the annuity is calculated from the forward curve using classical
	 * single curve interpretations of forwards and a default period length. The may be a crude approximation.
	 * 
	 * Note: This method will consider evaluationTime being 0, see {@link net.finmath.marketdata.products.SwapAnnuity#getSwapAnnuity(double, ScheduleInterface, DiscountCurveInterface, AnalyticModelStochasticInterface)}.
	 * 
	 * @param schedule The schedule discretization, i.e., the period start and end dates. End dates are considered payment dates and start of the next period.
	 * @param forwardCurve The forward curve.
	 * @return The swap annuity.
	 */
	static public RandomVariableInterface getSwapAnnuity(ScheduleInterface schedule, ForwardCurveStochasticInterface forwardCurve) {
		DiscountCurveStochasticInterface discountCurve = new DiscountCurveFromForwardCurveStochastic(forwardCurve.getName(), forwardCurve.getRandomVariableFactory());
		double evaluationTime = 0.0;	// Consider only payment time > 0
		return getSwapAnnuity(evaluationTime, schedule, discountCurve, new AnalyticModelStochastic( new CurveStochasticInterface[] {forwardCurve, discountCurve} ));
	}

	/**
	 * Function to calculate an (idealized) swap annuity for a given schedule and discount curve.
	 * 
	 * Note that, the value returned is divided by the discount factor at evaluation.
	 * This matters, if the discount factor at evaluationTime is not equal to 1.0.
	 * 
	 * @param evaluationTime The evaluation time as double. Cash flows prior and including this time are not considered.
	 * @param schedule The schedule discretization, i.e., the period start and end dates. End dates are considered payment dates and start of the next period.
	 * @param discountCurve The discount curve.
	 * @param model The model, needed only in case the discount curve evaluation depends on an additional curve.
	 * @return The swap annuity.
	 */
	static public RandomVariableInterface getSwapAnnuity(double evaluationTime, ScheduleInterface schedule, DiscountCurveStochasticInterface discountCurve, AnalyticModelStochasticInterface model) {
		RandomVariableInterface value = discountCurve.getRandomVariableFactory().createRandomVariable(0.0);
		for(int periodIndex=0; periodIndex<schedule.getNumberOfPeriods(); periodIndex++) {
			double paymentDate		= schedule.getPayment(periodIndex);
			if(paymentDate <= evaluationTime) continue;

			double periodLength		= schedule.getPeriodLength(periodIndex);
			RandomVariableInterface discountFactor	= discountCurve.getDiscountFactor(model, paymentDate);
			value = value.addProduct(discountFactor, periodLength);
		}
		return value.div(discountCurve.getDiscountFactor(model, evaluationTime));
	}

	@Override
	public String toString() {
		return "SwapAnnuity [schedule=" + schedule + ", discountCurveName="
				+ discountCurveName + "]";
	}
}

