package ch.epfl.codimsd.qep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;

public class QEPInfo {
	private int id;
	private String description;
	private String path;
	
	public QEPInfo() {
		super();
	}

	public QEPInfo(int id, String description, String path) {
		super();
		this.id = id;
		this.description = description;
		this.path = path;
	}
	
	/**
	 * Gets information from all QEPs from catalog.
	 * @return
	 * @throws Exception
	 */
	public static List<QEPInfo> getQEPInfo() throws Exception {
		List<QEPInfo> results = null;

		// Get the CatalogManager and construct the query
		CatalogManager catalogManager = CatalogManager.getCatalogManager();
		String query = "select RequestType.IdRequest, Description, Path " + 
			"from Template, RequestTypeTemplate, RequestType " + 
			"where " + 
			"RequestType.idRequest = RequestTypeTemplate.idRequest and " + 
			"RequestTypeTemplate.idTemplate = Template.idTemplate and " + 
			"TemplateType = 0 " + 
			"order by RequestType.idRequest";

		try {
			// get the qep path.
			ResultSet rset = catalogManager.executeQueryString(query);
			
			while (rset.next()) {
				if (results == null)
					results = new ArrayList<QEPInfo>();
				
				int idRequest = rset.getInt("IdRequest");
				String description = rset.getString("Description");
				String path = rset.getString("Path");

				results.add(new QEPInfo(idRequest, description, path));
			}
			
		} catch (SQLException ex) {
			throw new CatalogException("SQLException while reading template from catalog : " + ex.getMessage());
		} catch (CatalogException ex) {
			throw new CatalogException("CatalogException while reading template from catalog : " + ex.getMessage());
		}
		return results;
	}

	/**
	 * Finds QEPInfo about a specified requestId.
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static QEPInfo getQEPInfo(int id) throws Exception {
		QEPInfo qepInfo = null;

		// Get the CatalogManager and construct the query
		CatalogManager catalogManager = CatalogManager.getCatalogManager();
		String query = "select RequestType.IdRequest, Description, Path " + 
			"from Template, RequestTypeTemplate, RequestType " + 
			"where " + 
			"RequestType.idRequest = RequestTypeTemplate.idRequest and " + 
			"RequestTypeTemplate.idTemplate = Template.idTemplate and " + 
			"TemplateType = 0 and " + 
			"RequestType.IdRequest = " + id;

		try {
			// get the qep path.
			ResultSet rset = catalogManager.executeQueryString(query);
			
			if (rset.next()) {
				int idRequest = rset.getInt("IdRequest");
				String description = rset.getString("Description");
				String path = rset.getString("Path");

				qepInfo = new QEPInfo(idRequest, description, path);
			}
			
		} catch (SQLException ex) {
			throw new CatalogException("SQLException while reading template from catalog : " + ex.getMessage());
		} catch (CatalogException ex) {
			throw new CatalogException("CatalogException while reading template from catalog : " + ex.getMessage());
		}
		
		return qepInfo;
	}


	@Override
	public String toString() {
		return this.id + " - " + this.description + " - " + this.path;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
}
