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
import net.finmath.marketdata.model.curves.stochastic.CurveInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveInterface;

/**
 * Implements a collection of market data objects (e.g., discount curves, forward curve)
 * which provide interpolation of market data or other derived quantities
 * ("calibrated curves"). This can be seen as a model to be used in analytic pricing
 * formulas - hence this class is termed <code>AnalyticModel</code>.
 * 
 * @author Christian Fries
 */
public class AnalyticModelStochastic implements AnalyticModelStochasticInterface, Cloneable {

	private final Map<String, CurveInterface>				curvesMap				= new HashMap<String, CurveInterface>();
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
	public AnalyticModelStochastic(CurveInterface[] curves) {
        for (CurveInterface curve : curves) curvesMap.put(curve.getName(), curve);
	}
	
	/**
	 * Create an analytic model with the given curves.
	 * 
	 * @param curves A collection of curves.
	 */
	public AnalyticModelStochastic(Collection<CurveInterface> curves) {
		for(CurveInterface curve : curves) curvesMap.put(curve.getName(), curve);
	}

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.AnalyticModelInterface#getCurve(java.lang.String)
	 */
	@Override
	public CurveInterface getCurve(String name)
	{
		return curvesMap.get(name);
	}

	public AnalyticModelStochasticInterface addCurve(String name, CurveInterface curve) {
		AnalyticModelStochastic newModel = clone();
		newModel.curvesMap.put(name, curve);
		return newModel;
	}

	public AnalyticModelStochasticInterface addCurve(CurveInterface curve) {
		AnalyticModelStochastic newModel = clone();
		newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	@Override
	public AnalyticModelStochasticInterface addCurves(CurveInterface... curves) {
		AnalyticModelStochastic newModel = clone();
		for(CurveInterface curve : curves) newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	@Override
	public AnalyticModelStochasticInterface addCurves(Set<CurveInterface> curves) {
		AnalyticModelStochastic newModel = clone();
		for(CurveInterface curve : curves) newModel.curvesMap.put(curve.getName(), curve);
		return newModel;
	}

	/**
	 * @deprecated This class will become immutable. Use addCurve instead.
	 */
	@Override
	@Deprecated
    public void setCurve(CurveInterface curve)
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
	public void setCurves(CurveInterface[] curves) {
		for(CurveInterface curve : curves) setCurve(curve);
	}
	
	@Override
	public DiscountCurveInterface getDiscountCurve(String discountCurveName) {
		DiscountCurveInterface discountCurve = null;
		CurveInterface curve = getCurve(discountCurveName);
		if(DiscountCurveInterface.class.isInstance(curve))
			discountCurve = (DiscountCurveInterface)curve;

		return discountCurve;
	}

	@Override
	public ForwardCurveInterface getForwardCurve(String forwardCurveName) {
		ForwardCurveInterface forwardCurve = null;
		CurveInterface curve = getCurve(forwardCurveName);
		if(ForwardCurveInterface.class.isInstance(curve))
			forwardCurve = (ForwardCurveInterface)curve;

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
