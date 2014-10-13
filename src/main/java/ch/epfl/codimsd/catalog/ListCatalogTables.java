package ch.epfl.codimsd.catalog;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import ch.epfl.codimsd.connection.CatalogManager;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class ListCatalogTables {
    private static Logger logger = Logger.getLogger(ListCatalogTables.class.getName());
    
    public static void main(String[] args) {
        Connection con = null;
        try {
            DatabaseMetaData md = CatalogManager.getCatalogManager().getCatalogMetadata();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                logger.info(rs.getString(3) + " (" + rs.getString(4) + ")");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
   }
}