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
package ch.epfl.codimsd.qeef;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB.Query;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;

/**
 * The DataSourceManager is the component that creates CoDIMS dataSources. When
 * creating a query execution plan, the QEPFactory calls the DataSourceManager
 * to creates existing dataSources in the plan. The dataSources are put in the
 * BlackBoard for further use.
 * 
 * Added by Othman : - The DataSourceManager is a singleton. - The
 * DataSourceManager creates each dataSource using the reflection. - Javadoc
 * translated.
 * 
 * @author Fausto Ayres, Vinicius Fontes, Othman Tajmouati.
 * 
 * @date Jun 18, 2005
 */
public class DataSourceManager {

    /**
     * Contains the dataSources referenced by their names.
     */
    private Hashtable<String, DataSource> dataSources;

    /**
     * DataSourceManager singleton reference
     */
    private static DataSourceManager ref;

    /**
     * Log4j logger.
     */
    protected Logger logger = Logger.getLogger(DataSourceManager.class
            .getName());

    /**
     * Private default constructor.
     */
    private DataSourceManager() {

        dataSources = new Hashtable<String, DataSource>();
    }

    /**
     * @return the singletonf reference of the DataSourceManager.
     */
    public static synchronized DataSourceManager getDataSourceManager() {

        if (ref == null)
            ref = new DataSourceManager();

        return ref;
    }

    /**
     * Create the dataSources according to the informations encapsulated in the
     * OpNode structure.
     * 
     * @param opNode
     *            structure containing node indormations.
     * @throws DataSourceException
     * @throws OperatorInitializationException
     */
    public void createDataSources(OpNode opNode) throws DataSourceException, OperatorInitializationException {

        // Intializations.
        String dataSourceName = opNode.getParams()[0];
        CatalogManager catalogManager = CatalogManager.getCatalogManager();
        BlackBoard bl = BlackBoard.getBlackBoard();

        // Check if we have already computed the number of initial tuples.
        if (bl.containsKey(Constants.QEP_SCAN_NUMBER_TUPLES)) {

            // The RelationalDataSource reads the number of tuples from the
            // corresponding
            // database IRI and using the request from the QEP.
            if (dataSourceName.equalsIgnoreCase("RelationalDataSource")) {

                try {

                    String IRI = opNode.getParams()[1];
                    TransactionMonitor tm = TransactionMonitor.getTransactionMonitor();
                    int id = tm.open(IRI);
                    Query query = new Query();
                    query.setStringRequest(opNode.getParams()[2]);
                    ResultSet rset = tm.executeQuery(id, query);

                    int nrTuples = 0;
                    while (rset.next() == true)
                        nrTuples++;

                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                    tm.close(id);

                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in RelationalDataSource : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Particula")) {

                try {
                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject(
                            "ds_table", "numberoftuples", "name='PARTICULA'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");

                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Tetraedro")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject(
                            "ds_table", "numberoftuples", "name='TETRAEDRO'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Velocidade")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject(
                            "ds_table", "numberoftuples", "name='VELOCIDADE'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Poligono")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject(
                            "ds_table", "numberoftuples", "name='POLIGONO'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");

                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Vertice")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject(
                            "ds_table", "numberoftuples", "name='VERTICE'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Polydata")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject("ds_table", "numberoftuples", "name='POLYDATA'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            } else if (dataSourceName.equalsIgnoreCase("Images")) {

                try {

                    int nrTuples;
                    nrTuples = (Integer) catalogManager.getSingleObject("ds_table", "numberoftuples", "name='IMAGES'");
                    bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, nrTuples + "");
                } catch (Exception ex) {
                    throw new DataSourceException("Cannot access data "
                            + "in Scan : " + ex.getMessage());
                }
            }
        }

        // Get the dataSource className.
        String className = (String) catalogManager.getSingleObject("dataSource", "className", "name='" + dataSourceName + "'");

        className = className.trim();

        Class dataSourceDefinition = null;

        try {

            // Create the dataSource using the Reflection.
            int numberOfParameters = opNode.getParams().length - 1;
            Class[] intArgsClass = new Class[numberOfParameters + 2];
            Object[] intArgs = new Object[numberOfParameters + 2];

            // Set the name of the dataSource.
            intArgsClass[0] = String.class;
            intArgs[0] = dataSourceName;

            // Second parameter of the constructor is a TupleMetadata object set
            // to null.
            intArgsClass[1] = TupleMetadata.class;
            intArgs[1] = null;

            // Set other parameters of the dataSource.
            for (int i = 1; i <= numberOfParameters; i++) {
                intArgsClass[i + 1] = String.class;
                intArgs[i + 1] = opNode.getParams()[i];
            }

            // Call the constructor.
            Constructor intArgsConstructor;
            DataSource ds = null;
            dataSourceDefinition = Class.forName(className);
            intArgsConstructor = dataSourceDefinition.getConstructor(intArgsClass);
            ds = (DataSource) intArgsConstructor.newInstance(intArgs);

            // Put the datasource in the BlackBoard and reference it with its
            // timeStamp.
            String key = opNode.getOpTimeStamp();
            dataSources.put(key, ds);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(), ex);
            throw new OperatorInitializationException("DataSource initialization exception : " + ex.getCause().getMessage());
        }
    }

    /**
     * @return the dataSources hashtable.
     */
    public Hashtable getDataSources() {
        return dataSources;
    }

    /**
     * @param name
     *            name of the dataSource.
     * @return the requested dataSource.
     */
    public DataSource getDataSource(String key) {
        return ((DataSource) dataSources.get(key));
    }
}
