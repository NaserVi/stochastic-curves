/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christianfries.com.
 *
 * Created on 12.10.2013
 */

package net.finmath.marketdata.products.stochastic;

import net.finmath.marketdata.model.stochastic.AnalyticModelStochasticInterface;
import net.finmath.modelling.ModelInterface;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * @author Christian Fries
 *
 */
public abstract class AbstractAnalyticProductStochastic implements AnalyticProductInterfaceStochastic {

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.products.ProductInterface#getValue(double, net.finmath.marketdata.products.ModelInterface)
	 */
	@Override
	public Object getValue(double evaluationTime, ModelInterface model) {
		throw new IllegalArgumentException("The product " + this.getClass()
				+ " cannot be valued against a model " + model.getClass() + "."
				+ "It requires a model of type " + AnalyticModelStochasticInterface.class + ".");
	}

	public RandomVariableInterface getValue(AnalyticModelStochasticInterface model) {
 		return getValue(0.0, model);
	}
}
