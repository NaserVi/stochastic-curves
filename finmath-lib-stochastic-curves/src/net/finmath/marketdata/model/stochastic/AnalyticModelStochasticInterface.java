/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 28.11.2012
 */
package net.finmath.marketdata.model.stochastic;

import java.util.Map;
import java.util.Set;

import net.finmath.marketdata.model.curves.stochastic.CurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.DiscountCurveStochasticInterface;
import net.finmath.marketdata.model.curves.stochastic.ForwardCurveStochasticInterface;
//import net.finmath.marketdata.model.volatilities.AbstractVolatilitySurface;
//import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface;
import net.finmath.modelling.ModelInterface;

/**
 * A collection of objects representing analytic valuations, i.e., curves and volatility surfaces.
 * 
 * @author Christian Fries
 */
public interface AnalyticModelStochasticInterface extends ModelInterface, Cloneable {

	/**
	 * Get a curve by a given curve name.
	 * 
	 * @param name The name of the curve.
	 * @return The curve with the corresponding name, given that it is part of this model, otherwise null is return.
	 */
	CurveStochasticInterface getCurve(String name);

	/**
	 * Add a reference to a given curve under a given name to this model. It is not necessary that the name given agrees with
	 * <code>curve.getName()</code>. This method comes in handy, if you like to create curve mappings.
	 * 
	 * @param name Name under which the curve is known in the model.
	 * @param curve The curve.
	 * @return A clone of this model, containing the curves of this model which are not known under the given name and the new curve under the given name.
	 */
	AnalyticModelStochasticInterface addCurve(String name, CurveStochasticInterface curve);

	/**
	 * Create a new analytic model consisting of a clone of this one together with the given curves added.
	 * 
	 * @param curves The set of curves to add.
	 * @return A new analytic model.
	 */
	AnalyticModelStochasticInterface addCurves(CurveStochasticInterface... curves);

	/**
	 * Create a new analytic model consisting of a clone of this one together with the given curves added.
	 * 
	 * @param curves The list of curves to add.
	 * @return A new analytic model.
	 */
	AnalyticModelStochasticInterface addCurves(Set<CurveStochasticInterface> curves);

	@Deprecated
	void setCurve(CurveStochasticInterface curve);

	/**
	 * Returns a discount curve for a given name.
	 * 
	 * @param discountCurveName The name of the requested curve.
	 * @return discount curve corresponding to discountCurveName or null if no discountCurve with this name exists in the model
	 */
	DiscountCurveStochasticInterface getDiscountCurve(String discountCurveName);

	/**
	 * Returns a forward curve for a given name.
	 * 
	 * @param forwardCurveName The name of the requested curve.
	 * @return forward curve corresponding to forwardCurveName or null if no forwardCurve with this name exists in the model
	 */
	ForwardCurveStochasticInterface getForwardCurve(String forwardCurveName);

	//VolatilitySurfaceInterface getVolatilitySurface(String name);

	//AnalyticModelInterface addVolatilitySurfaces(VolatilitySurfaceInterface... volatilitySurfaces);

	/**
	 * Create a new analytic model consisting of a clone of this one together with the given volatility surfaces added.
	 * 
	 * @param volatilitySurfaces The list of volatility surfaces to add.
	 * @return A new analytic model.
	 */
	//AnalyticModelInterface addVolatilitySurfaces(Set<AbstractVolatilitySurface> volatilitySurfaces);

	
	AnalyticModelStochasticInterface clone();

	//AnalyticModelInterface getCloneForParameter(Map<ParameterObjectInterface, double[]> curvesParameterPairs) throws CloneNotSupportedException;
}