/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 26.11.2012
 */
package net.finmath.marketdata.products.stochastic;

import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochastic;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochasticInterface;
import net.finmath.time.ScheduleInterface;

/**
 * Implements the valuation of a swap leg using curves (discount curve, forward curve).
 * 
 * The swap leg valuation supports distinct discounting and forward curves.
 * 
 * Support for day counting is provided via the class implementing
 * <code>ScheduleInterface</code>.
 * 
 * @author Christian Fries
 */
public class SwapLegStochastic extends AbstractAnalyticProductStochastic implements AnalyticProductInterfaceStochastic {

	private final ScheduleInterface				legSchedule;
	private final String						forwardCurveName;
	private final double						spread;
	private final String						discountCurveName;
	private boolean								isNotionalExchanged = false;

	/**
	 * Creates a swap leg. The swap leg has a unit notional of 1.
	 * 
	 * @param legSchedule Schedule of the leg.
	 * @param forwardCurveName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spread Fixed spread on the forward or fix rate.
	 * @param discountCurveName Name of the discount curve for the leg.
	 * @param isNotionalExchanged If true, the leg will pay notional at the beginning of each swap period and receive notional at the end of the swap period. Note that the cash flow date for the notional is periodStart and periodEnd (not fixingDate and paymentDate).
	 */
	public SwapLegStochastic(ScheduleInterface legSchedule, String forwardCurveName, double spread, String discountCurveName, boolean isNotionalExchanged) {
		super();
		this.legSchedule = legSchedule;
		this.forwardCurveName = forwardCurveName;
		this.spread = spread;
		this.discountCurveName = discountCurveName;
		this.isNotionalExchanged = isNotionalExchanged;
	}

	/**
	 * Creates a swap leg (without notional exchange). The swap leg has a unit notional of 1.
	 * 
	 * @param legSchedule Schedule of the leg.
	 * @param forwardCurveName Name of the forward curve, leave empty if this is a fix leg.
	 * @param spread Fixed spread on the forward or fix rate.
	 * @param discountCurveName Name of the discount curve for the leg.
	 */
	public SwapLegStochastic(ScheduleInterface legSchedule, String forwardCurveName, double spread, String discountCurveName) {
		this(legSchedule, forwardCurveName, spread, discountCurveName, false);
	}


	@Override
	public RandomVariableInterface getValue(double evaluationTime, AnalyticModelStochasticInterface model) {	
		if(model==null) {
			throw new IllegalArgumentException("model==null");
		}
		
		DiscountCurveStochasticInterface discountCurve = model.getDiscountCurve(discountCurveName);
		if(discountCurve == null) {
			throw new IllegalArgumentException("No discount curve with name '" + discountCurveName + "' was found in the model:\n" + model.toString());
		}
		
		ForwardCurveStochasticInterface forwardCurve = model.getForwardCurve(forwardCurveName);
		if(forwardCurve == null && forwardCurveName != null && forwardCurveName.length() > 0) {
			throw new IllegalArgumentException("No forward curve with name '" + forwardCurveName + "' was found in the model:\n" + model.toString());
		}
		
		RandomVariableInterface value = ((DiscountCurveStochastic)discountCurve).getRandomVariableFactory().createRandomVariable(0.0);
		for(int periodIndex=0; periodIndex<legSchedule.getNumberOfPeriods(); periodIndex++) {
			double fixingDate	= legSchedule.getFixing(periodIndex);
			double paymentDate	= legSchedule.getPayment(periodIndex);
			double periodLength	= legSchedule.getPeriodLength(periodIndex);

			RandomVariableInterface forward = ((DiscountCurveStochastic)discountCurve).getRandomVariableFactory().createRandomVariable(spread);
			if(forwardCurve != null) {
				forward = forward.add(forwardCurve.getForward(model, fixingDate, paymentDate-fixingDate));
			}

			RandomVariableInterface discountFactor	= paymentDate > evaluationTime ? discountCurve.getDiscountFactor(model, paymentDate) : ((DiscountCurveStochastic)discountCurve).getRandomVariableFactory().createRandomVariable(0.0);;
			value = value.add(forward.mult(discountFactor).mult(periodLength));

			// Consider notional payments if required
			if(isNotionalExchanged) {
				double periodEnd	= legSchedule.getPeriodEnd(periodIndex);
				value = periodEnd > evaluationTime ? value.add(discountCurve.getDiscountFactor(model, periodEnd)) : value.add(0.0);

				double periodStart	= legSchedule.getPeriodStart(periodIndex);
				value = periodStart > evaluationTime ? value.sub(discountCurve.getDiscountFactor(model, periodStart)) : value.sub(0.0);
			}
		}

		return value.div(discountCurve.getDiscountFactor(model, evaluationTime));
	}

	public ScheduleInterface getSchedule() {
		return legSchedule;
	}

	public String getForwardCurveName() {
		return forwardCurveName;
	}

	public double getSpread() {
		return spread;
	}

	public String getDiscountCurveName() {
		return discountCurveName;
	}

	public boolean isNotionalExchanged() {
		return isNotionalExchanged;
	}

	@Override
	public String toString() {
		return "SwapLeg [legSchedule=" + legSchedule + ", forwardCurveName="
				+ forwardCurveName + ", spread=" + spread
				+ ", discountCurveName=" + discountCurveName
				+ ", isNotionalExchanged=" + isNotionalExchanged + "]";
	}
}
