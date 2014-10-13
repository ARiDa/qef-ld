package ch.epfl.codimsd.config;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.util.Constants;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class AppConfig {
    private static String ROOT_PATH;

    public static String CATALOG_DB_CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    public static String CATALOG_SERVER_HOST = "localhost";
    public static int CATALOG_SERVER_PORT = 1527;
    
    private static String CATALOG_PATH;
    public static String CATALOG_URL;
    public static String CATALOG_USER = "CODIMS";
    public static String CATALOG_PASSWORD = "CODIMS";
    
    public static String CODIMS_HOME;
    public static String CODIMS_ENV_FILE;
    public static String CODIMS_PROPS_FILE;
    public static String CODIMS_TEMPLATE_PROPS_FILE;
    public static String OUTPUT_DIR;
    
    public static String CATALOG_SCRIPT_PATH;
    public static boolean SHOW_RESULTS = true;
    public static String RESULTS_FILE;
    
    public static int EXECUTE_QEP = Constants.REQUEST_TYPE_QEF_SPARQL_04;

    
    /**
     * Log4j logger.
     */
    protected static Logger logger = Logger.getLogger(AppConfig.class.getName());
    
    static {
    	setRootPath(AppConfig.class.getResource("/").getPath());
    }
    
    public static void setRootPath(String rootPath) {
    	ROOT_PATH = rootPath;
    	CATALOG_PATH = ROOT_PATH + "codims-home/DerbyCatalog";
    	CATALOG_URL = "jdbc:derby://" + CATALOG_SERVER_HOST + ":" + CATALOG_SERVER_PORT + "/" + CATALOG_PATH + ";create=true";
    	CODIMS_HOME = ROOT_PATH + "codims-home/";
    	CODIMS_ENV_FILE = CODIMS_HOME + "codims.env";
    	CODIMS_PROPS_FILE = CODIMS_HOME + "codims.properties";
    	CODIMS_TEMPLATE_PROPS_FILE = CODIMS_HOME + "Scripts/codimsTemplate.properties";
    	CATALOG_SCRIPT_PATH = CODIMS_HOME + "SQLRequests/CatalogSPARQL.txt";
    	OUTPUT_DIR = System.getProperty("user.home") + "/qef-output/";
    	RESULTS_FILE = OUTPUT_DIR + "results.txt";
        logger.info("ROOT_PATH: " + ROOT_PATH);
        logger.info("CATALOG_PATH: " + CATALOG_PATH);
        logger.info("CATALOG_URL: " + CATALOG_URL);
        logger.info("OUTPUT_DIR: " + OUTPUT_DIR);
    }
    
    public static String getRootPath() {
    	return ROOT_PATH;
    }
}
