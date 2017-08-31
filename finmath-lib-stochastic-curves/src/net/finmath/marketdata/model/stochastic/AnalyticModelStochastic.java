/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 28.11.2012
 */
package net.finmath.marketdata.model.stochastic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.finmath.marketdata.calibration.ParameterObjectInterface;
import net.finmath.marketdata.model.curves.stochastic.CurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochasticInterface;

/**
 * Implements a collection of market data objects (e.g., discount curves, forward curve)
 * which provide interpolation of market data or other derived quantities
 * ("calibrated curves"). This can be seen as a model to be used in analytic pricing
 * formulas - hence this class is termed <code>AnalyticModel</code>.
 * 
 * @author Christian Fries
 */
public class AnalyticModelStochastic implements AnalyticModelStochasticInterface, Cloneable {

	private final Map<String, CurveStochasticInterface>				curvesMap				= new HashMap<String, CurveStochasticInterface>();
	//private final Map<String, VolatilitySurfaceInterface>	volatilitySurfaceMap	= new HashMap<String, VolatilitySurfaceInterface>();

	/**
	 * Create an empty analytic model.
	 */
	public AnalyticModelStochastic() {
	}

	/**
	 * Create an analytic model with the given curves.
	 * 
	 * @param curves The vector of curves.
	 */
	public AnalyticModelStochastic(CurveStochasticInterface[] curves) {
        for (CurveStochasticInterface curve : curves) curvesMap.put(curve.getName(), curve);
	}
	
	/**
	 * Create an analytic model with the given curves.
	 * 
	 * @param curves A collection of curves.
	 */
	public AnalyticModelStochastic(Collection<CurveStochasticInterface> curves) {
		for(CurveStochasticInterface curve : curves) curvesMap.put(curve.getName(), curve);
	}

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.AnalyticModelInterface#getCurve(java.lang.String)
	 */
	@Override
	public CurveStochasticInterface getCurve(String name)
	{
		return curvesMap.get(name);
	}

	public AnalyticModelStochasticInterface addCurve(String name, CurveStochasticInterface curve) {
		AnalyticModelStochastic newModel = clone();
		newModel.curvesMap.put(name, curve);
		return newModel;
	}

	public AnalyticModelStochasticInterface addCurve(CurveStochasticInterface curve) {
		AnalyticModelStochastic newModel = clone();
		newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	@Override
	public AnalyticModelStochasticInterface addCurves(CurveStochasticInterface... curves) {
		AnalyticModelStochastic newModel = clone();
		for(CurveStochasticInterface curve : curves) newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	@Override
	public AnalyticModelStochasticInterface addCurves(Set<CurveStochasticInterface> curves) {
		AnalyticModelStochastic newModel = clone();
		for(CurveStochasticInterface curve : curves) newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	/**
	 * @deprecated This class will become immutable. Use addCurve instead.
	 */
	@Override
	@Deprecated
    public void setCurve(CurveStochasticInterface curve)
	{
		curvesMap.put(curve.getName(), curve);
	}

	/**
	 * Set some curves.
	 * 
	 * @param curves Array of curves to set.
	 * @deprecated This class will become immutable. Use addCurve instead.
	 */
	@Deprecated
	public void setCurves(CurveStochasticInterface[] curves) {
		for(CurveStochasticInterface curve : curves) setCurve(curve);
	}
	
	@Override
	public DiscountCurveStochasticInterface getDiscountCurve(String discountCurveName) {
		DiscountCurveStochasticInterface discountCurve = null;
		CurveStochasticInterface curve = getCurve(discountCurveName);
		if(DiscountCurveStochasticInterface.class.isInstance(curve))
			discountCurve = (DiscountCurveStochasticInterface)curve;

		return discountCurve;
	}

	@Override
	public ForwardCurveStochasticInterface getForwardCurve(String forwardCurveName) {
		ForwardCurveStochasticInterface forwardCurve = null;
		CurveStochasticInterface curve = getCurve(forwardCurveName);
		if(ForwardCurveStochasticInterface.class.isInstance(curve))
			forwardCurve = (ForwardCurveStochasticInterface)curve;

		return forwardCurve;
	}


	@Override
	public AnalyticModelStochastic clone()
	{
		AnalyticModelStochastic newModel = new AnalyticModelStochastic();
		newModel.curvesMap.putAll(curvesMap);
		return newModel;
	}


	@Override
	public String toString() {
		return "AnalyticModel: curves=" + curvesMap.keySet();
	}
}
