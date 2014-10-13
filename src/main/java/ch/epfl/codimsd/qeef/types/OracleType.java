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
package ch.epfl.codimsd.qeef.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.relational.Column;

/**
 * The OracleType is a CoDIMS type which contains SQL data.
 * 
 * @author Othman Tajmouati.
 */
public class OracleType implements Type {

	/**
	 * For the Serializer.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * SQL object casted to Object.
	 */
	private Object object;
	
	/**
	 * The type of the SQL object. See sql types for more information.
	 */
	private int type;
	
	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(OracleType.class.getName());
	
	/**
	 * Default constructor.
	 */
	public OracleType() {}
	
	/**
	 * Constructor. It sets the sql object.
	 * 
	 * @param object SQL object of type Object.
	 */
	public OracleType(Object object) {
		this.object = object;
	}
	
	/**
	 * @return the sql object.
	 */
	public Object getObject() {
		return object;
	}
	
	/**
	 * Set the sql object.
	 * 
	 * @param object sql object.
	 */
	public  void setValue(Object value) {
		this.object = value;
	}

	/**
	 * @return the cloned object.
	 */
	public Object clone() {
		return this.clone();
	}
	
	/**
	 * @return a new OracleTye instance.
	 */
	public Type newInstance() { return new OracleType(); }
	
	/**
	 * Write the sql object to this DataOutputStream.
	 * 
	 * @param out the DataOutputStream to write to.
	 */
	public void write(DataOutputStream out) throws IOException {
		
		// Write the type of the sql object (see sql types) at the begining of the OutputStream.
		out.writeInt(type);
		
		// Depending on the sql type, perform the right serialiazion method.
		switch (type) {
		
			case java.sql.Types.BIGINT :
				long longValue = (Long) object;
				out.writeLong(longValue);
			break;
		
			case java.sql.Types.CHAR :
				String charValue = (String) object;
				out.writeUTF(charValue);
				break;
		
			case java.sql.Types.INTEGER :
				int intValue = (Integer) object;
				out.writeInt(intValue);
			break;
			
			case java.sql.Types.DOUBLE :
				double doubleValue = (Double) object;
				out.writeDouble(doubleValue);
			break;
		
			case java.sql.Types.FLOAT :
				float floatValue = (Long) object;
				out.writeFloat(floatValue);
			break;
		
			case java.sql.Types.DECIMAL :
			case java.sql.Types.NUMERIC :
				BigDecimal bigDecimalValue = (BigDecimal) object;
				out.writeUTF(bigDecimalValue.toString());
			break;
		
			case java.sql.Types.TIME :
				Time timeValue = (Time) object;
				out.writeUTF(timeValue.toString());
			break;
		
			case java.sql.Types.TIMESTAMP :
				out.writeUTF(object.toString());
			break;
		
			case java.sql.Types.VARCHAR :
				String varcharValue = (String) object;
				out.writeUTF(varcharValue);
			break;
		
			default:
				break;		
		}
	}
	
	/**
	 * Read the sql object from this DataInputStream.
	 * 
	 * @param in DataInputStrem to read from.
	 */
	public Type read(DataInputStream in) throws IOException { 
		
		// Read the sql type in the InputStream.
		type = in.readInt();
		
		// Perform the right deserializatio nmethod depending on the sql type.
		switch (type) {
		
			case java.sql.Types.BIGINT :
				object = in.readLong();
			break;
		
			case java.sql.Types.CHAR :
				object = in.readUTF();
			break;
		
			case java.sql.Types.INTEGER :
				object = in.readInt();
			break;
		
			case java.sql.Types.DOUBLE :
				object = in.readDouble();
			break;
		
			case java.sql.Types.FLOAT :
				object = in.readFloat();
			break;
		
			case java.sql.Types.DECIMAL :
			case java.sql.Types.NUMERIC :
				String big = in.readUTF();
				BigDecimal bigValue = new BigDecimal(big);
				object = bigValue;
			break;
		
			case java.sql.Types.TIMESTAMP :
				String time = in.readUTF();
				Timestamp timeStampValue = Timestamp.valueOf(time);
				object = timeStampValue;
			break;
		
			case java.sql.Types.VARCHAR :
				object = in.readUTF();
			break;
		
			default:
				logger.warn("Cannot perform deserializazion : SQL type not defined.");
				break;		
		}
		
		return new OracleType(object);
	}

        public Type readSmooth(String in) throws IOException {
            return null;
        }

/*        public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
            return null;
        }
*/
	/**
	 * Set the metadata for this object. This method is mainly used
	 * for setting the type of the sql object during the serialization
	 * of the object.
	 */
	public void setMetadata(Data data) {
		 
		 Column column = (Column) data;
		 type = column.getSQLType();
	}
	
	/* (non-Javadoc)
	 * Inherited method.
	 */
	public  void setValue(String value) {}
	
	/* (non-Javadoc)
	 * Inherited method.
	 */
	public void display(Writer out) throws IOException {}

	/* (non-Javadoc)
	 * Inherited method.
	 */
	public int displayWidth() { return 0; }
	
	/* (non-Javadoc)
	 * Inherited method.
	 */
	public String recognitionPattern() { return null; }
	
	/* (non-Javadoc)
	 * Inherited method.
	 */
	public void finalize() throws Throwable {}
	
	/* (non-Javadoc)
	 * Inherited method.
	 */
	public int compareTo(Object o) { return 0;}	
}

