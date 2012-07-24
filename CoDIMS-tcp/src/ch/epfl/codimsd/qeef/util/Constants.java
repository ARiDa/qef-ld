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
package ch.epfl.codimsd.qeef.util;

import java.io.File;

/**
 * The Constants class store CoDIMS constants.
 * 
 * @author Othman Tajmouati.
 */
public interface Constants {

	// Initial Configuration parameters
	static final String IRI = "IRI";
	static final String IRICatalog = "IRI_CATALOG_DERBY";
	static final String HOME = "CODIMS_HOME";
	static final String ENVIRONMENT_ID = "ENVIRONMENT_ID";
	static final String NODE_ID = "NODE_ID";
	static final String ENVIRONMENT_READY = "ENVIRONMENT_READY";
	static final String IS_SERVICES_STARTED = "IS_SERVICES_STARTED";
	static final String LOG4J_HOME_PROP = "codims.home";
	static final String LOG4J_FILENAME_PROP = "log4j.filename";
	static final String LOG4J_FILENAME = "codimsLogs";
	static final String SystemConfigFile = "SystemConfigFile.txt";
	static final String initialConfigDB = "initialConfig";
	static final String DerbyCatalog = "DerbyCatalog";
	static final String  DISTRIBUTED_OPERATOR_ID = "DISTRIBUTED_OPERATOR_ID";
	static final String derbyServerPortNumber = "DERBY_SERVER_PORT_NUMBER";
	static final int DEFAULT_OPERATOR_EXECUTION_TIME = 500;
	static final int CONTAINER_INIT_WAIT_TIME = 4000;
	static final String THIS_NODE = "THIS_NODE";
	static final String EXEC_ASYNC = "EXEC_ASYNC_";
	static final String STOP_WEBSERVICES_FROM_CODE = "STOP_WEBSERVICES_FROM_CODE";
	static final String START_WEBSERVICES_FROM_CODE = "START_WEBSERVICES_FROM_CODE";
	static final String IS_DERBY_STARTED = "IS_DERBY_STARTED";
	static final String LOCAL_WEB_SERVICE = "LOCAL_WEB_SERVICE";
	
	// Parameters written to the Catalog
	static final String QEPInitial = "QEPInitial";
	static final String QEPDistributedRemote = "QEPDistributedRemote";
	static final String QEPDistributedLocal = "QEPDistributedLocal";
	static final String OPERATOR_ALGEBRA = "/OperatorAlgebra.txt";
	static final String WSMLTEXT_FILENAME = "WSMLTEXT_FILENAME";
	static final String IRI_OPERATORS_LBD7 = "IRI_OPERATORS_LBD7";
	static final String IRI_DATASOURCE_LBD7 = "IRI_DATASOURCE_LBD7";
	static final String MaxBusyConnectionTimeKey = "MaxBusyConnectionTimeKey";
	static final String ThreadSleepTimeKey = "ThreadSleepTimeKey";
	static final String IRI_DATABASECATALOG = "ds_database";
	static final String IRI_TABLECATALOG = "ds_table";

	// Objects taken from the BlackBoard
	static final String TRANSACTION_MONITOR = "TRANSACTION_MONITOR";
	static final String DATASOURCE_MANAGER = "DATASOURCE_MANAGER";
	
	// Operator names and parameters 
	static final String ScanDiscoveryOperator = "SCANDISCOVERY";
	static final String SplitOperator = "SPLIT";
	static final String MergeOperator = "MERGE";
	static final String Instance2BlockOperator = "Instance2Block";
        static final String Block2InstanceOperator = "Block2Instance";
	static final int idB2IOperator = 2;
	static final int idReceiverInRemotePlan = 1;
	static final int idSenderInRemotePlan = 4;
	static final String tempParamForReceveiver = "0";
	static final String QEP_DATASOURCE_NAME_PARAM = "DataSourceName";
	static final String QEP_SCAN_NUMBER_TUPLES = "numberTuples";
	
	// These parameters are used in order to access the QEP
	// If the QEP is a local one then it's an xml file otherwise it's a text file
	static final String qepAccessTypeLocal = "LOCAL";
	static final String qepAccessTypeRemote = "REMOTE";
	
	//Parameters of Globus & Local Web Service
	static final String portLocalWebService = "8082";
	static final String addressLocalWebService = 
		"http://localhost:" + portLocalWebService 
			+ "/wsrf/services/examples/core/first/DQEEService";
	static final String  globusContainer = 
		System.getenv("GLOBUS_LOCATION") + File.separator + "bin" 
			+ File.separator + "globus-start-container.bat";
	
	// DB parameters
	static final String COMMIT = "COMMIT";
	static final String ROLLBACK = "ROLLBACK";
	static final String SELECT = "SELECT";
	
	// Execution modes and multi-env execution informations
	static final int SINGLE_USER = 0;
	static final int MULTI_USER = 1;
	static final String REQUEST_ID = "REQUEST_ID";
	static final int REQUEST_YES = 1;
	static final int REQUEST_NO = 0;
	static final String LOG_EXECUTION_PROFILE = "LOG_EXECUTION_PROFILE";
	static final String NO_DISTRIBUTION = "NO_DISTRIBUTION";
	
	// Thread names
	static final String timeOutThread = "TIMEOUT_CHECKER";
	static final String QueryManagerThread = "QUERY_MANAGER_THREAD";
	
	// Codims Types
	static final String ORACLETYPE = "ORACLE_TYPE";
	static final String STRING = "STRING";
	static final String INTEGER = "INTEGER";
	static final String FLOAT = "FLOAT";
	static final String POINT = "POINT";
	static final String POINT_LIST = "POINT_LIST";
	static final String WEBSERVICE_EXT = "WEBSERVICE_EXT";
	static final String RESULTSET_EXT = "RESULTSET_EXT";
	static final String WEBSERVICE_WSMO = "WEBSERVICE_WSMO";
        static final String POLYDATA = "POLYDATA";
        static final String IMAGEDATA = "IMAGEDATA";

	// BlackBoard params for the Discovery request
	static final String GOAL = "GOAL";
	
	// CacheManager and IRIParser parameters
	static final String CacheSize = "CacheSize";
	static final int IS_SQL_QUERY = 1;
	static final int IS_XMLDATASOURCE_QUERY = 2;
	static final String queryOpenTag = "?[";
	static final String queryCloseTag = "]";
	static final String  sqlQueryTag = "Query";
	static final String  conditionTag = "Condition";
	static final String  projectionTag = "Projection";
	static final String  languageTag = "Language";
	static final String  parametersSeparator = ";";
	static final String parameterContentDefChar = "=";
	static final String parameterContentDelimiterChar = "\"";
	 
	// G2N parameters
	static final String  NR_NODES = "NR_NODES";
	static final String NR_TUPLES = "NR_TUPLES";
	static final String PROD_RATES = "PROD_RATES";
	static final int msgProcessingTime = 2500;
	static final long netTransmissionTime = 5200;
	static final String SENDER_NODE_RELATIONS = "SENDER_NODE_RELATIONS";
	
	// For the DistributedManager
	static final String infoNodes = "INFO_NODES";
	static final String qepRemoteList = "QEP_REMOTE_LSIT";
	static final String GT4_WEBSERVICES = "GT4_WEBSERVICES";
	
	// Request types
	static final int REQUEST_TYPE_SERVICE_DISCOVERY=0;
	static final int REQUEST_TYPE_SERVICE_DISCOVERY_EXTREP=1;
	static final int REQUEST_TYPE_SERVICE_QOS_INDEXING=2;
	static final int REQUEST_TYPE_DISHONEST_DETECTION=3;
	static final int REQUEST_TYPE_REPORT_CLUSTERING=4;
	static final int REQUEST_TYPE_PREDICT_SERVICE_QOS=5;
	static final int REQUEST_TYPE_GET_REPUTATION_INFO=6;
    static final int REQUEST_TYPE_TEST_JOIN=7;
    static final int REQUEST_TYPE_TCP=8;
    static final int REQUEST_TYPE_SMOOTH=9;
    static final int REQUEST_TYPE_VOLUME_RENDERING=10;
    static final int REQUEST_TYPE_QEF_SPARQL_01A=11;
    static final int REQUEST_TYPE_QEF_SPARQL_02A=12;
    static final int REQUEST_TYPE_QEF_SPARQL_03A=13;
    static final int REQUEST_TYPE_QEF_SPARQL_03B=14;
    static final int REQUEST_TYPE_QEF_SPARQL_03C=15;
    static final int REQUEST_TYPE_QEF_SPARQL_04=16;
    static final int REQUEST_TYPE_QEF_SPARQL_05A=17;
    static final int REQUEST_TYPE_QEF_SPARQL_05B=18;
    static final int REQUEST_TYPE_QEF_SPARQL_01B=19;
    static final int REQUEST_TYPE_QEF_SPARQL_05C=20;
    static final int REQUEST_TYPE_DISEASES_DRUGS_Q1=21;
    static final int REQUEST_TYPE_DISEASES_DRUGS_Q2=22;
    static final int REQUEST_TYPE_DISEASES_DRUGS_Q3=23;
    static final int REQUEST_TYPE_RESEARCHERS_DBLP_BIND_LEFT_JOIN=24;
    
    static final String LINE_SEPARATOR = System.getProperty("line.separator");
}

