package ch.epfl.codimsd.catalog;

import java.sql.ResultSet;
import org.apache.log4j.Logger;
import ch.epfl.codimsd.connection.CatalogManager;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class TestCatalog {
    private static Logger logger = Logger.getLogger(TestCatalog.class.getName());

    public static void main(String[] args) {
        try {
            ResultSet rs = CatalogManager.getCatalogManager().executeQueryString("select * from template");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
   }
    
}