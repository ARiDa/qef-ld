/*
 * CoDIMS version 1.0 
 * Copyright (C) 2006 Othman Tajmouati
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package ch.epfl.codimsd.qeef.operator;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Predicate;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateParser;
import ch.epfl.codimsd.qep.OpNode;

/**
 * Version 1
 * 
 * The OperatorFactoryManager creates operators using Java Reflection. It loads
 * the definition of the operator from the Catalog and calls the right
 * implementation class.
 * 
 * @author Othman Tajmouati
 * 
 * @date May 26, 2006
 */
public class OperatorFactoryManager {
	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(OperatorFactoryManager.class.getName());

	/**
	 * Default constructor
	 */
	public OperatorFactoryManager() {
	}

	/**
	 * Create an operator using the opNode structure. It firsts get the
	 * definition of the operator from the Catalog and then use the reflection
	 * to instantiate it.
	 * 
	 * @param opNode
	 *            opNode structure containing useful informations on the
	 *            operator.
	 * @return the created operator.
	 * @throws OperatorInitializationException
	 */
	public Operator createOperator(OpNode opNode)
			throws OperatorInitializationException, PredicateEvaluatorException {

		// Initializations.
		Operator op = null;
		String className = null;
		String strExpression = null;
		Class operatorDefinition;
		Constructor intArgsConstructor;
		Class[] intArgsClass;
		Object[] intArgs;
		Predicate predicate = null;
		PredicateParser parser = null;

		if (opNode.getOpName().equalsIgnoreCase("Scan")) {

			DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
			DataSource fonte = dsManager.getDataSource(opNode.getOpTimeStamp());

			if (opNode.getParams()[0].equalsIgnoreCase("Velocidade")) {
				strExpression = "Velocidade.idTempo < 10";

				parser = new PredicateParser();
				predicate = parser.parse(strExpression, fonte.getMetadata(),
						fonte.getMetadata());
			} else if (opNode.getParams()[0].equalsIgnoreCase("Particula")) {
				strExpression = "PARTICULA.PONTO.id<=1000";
				parser = new PredicateParser();
				predicate = parser.parse(strExpression, fonte.getMetadata(),
						fonte.getMetadata());
			} else if (opNode.getParams()[0].equalsIgnoreCase("Tetraedro")) {
				strExpression = "_";
				parser = new PredicateParser();
				predicate = parser.parse(strExpression, fonte.getMetadata(),
						fonte.getMetadata());
			}

			intArgsClass = new Class[] { int.class, OpNode.class,
					Predicate.class };
			intArgs = new Object[] { opNode.getOpID(), opNode, predicate };
		} else {

			intArgsClass = new Class[] { int.class, OpNode.class };
			intArgs = new Object[] { opNode.getOpID(), opNode };
		}

		try {
			// Load operator algebra.
			CatalogManager catalogManager = CatalogManager.getCatalogManager();
			className = (String) catalogManager.getSingleObject("operatortype",
					"classname", "name='" + opNode.getOpName() + "'");
			className = className.trim();
			logger.debug("Classname = " + className);
			// Instantiate the operator.
			operatorDefinition = Class.forName(className);
			intArgsConstructor = operatorDefinition.getConstructor(intArgsClass);
			op = (Operator) intArgsConstructor.newInstance(intArgs);

		} catch (CatalogException ex1) {
			throw new OperatorInitializationException("Error loading operator algebra from the catalog (operator "
							+ opNode.getOpName() + ") : " + ex1.getMessage());
		} catch (NoSuchMethodException ex) {
			String intArgsClassStr = Arrays.toString(intArgsClass);
			logger.error("Could not find a suitable constructor to Operator " + className + " with parameters types: "  + intArgsClassStr, ex);
			throw new OperatorInitializationException("Operator initialization error at " 
					+ opNode.getOpName() + " : " + ex.getMessage());
		} catch (Exception ex) {
			throw new OperatorInitializationException("Operator initialization error at " 
					+ opNode.getOpName() + " : " + ex.getMessage());
		}

		return op;
	}
}
