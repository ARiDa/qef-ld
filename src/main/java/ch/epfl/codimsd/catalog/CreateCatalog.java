package ch.epfl.codimsd.catalog;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class CreateCatalog {
    private static Logger logger = Logger.getLogger(CreateCatalog.class.getName());

    public static void main(String[] args) {
        try {
            CatalogManager.getCatalogManager().setup();
            logger.info("Catalog created successfully.");
        } catch (CatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
