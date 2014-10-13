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
/*
 * Created on Jun 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.epfl.codimsd.qeef.util;


import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.types.FloatType;
import ch.epfl.codimsd.qeef.types.IntegerType;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qep.QEP;
/**
 * @author Vinicius Fontes
 *
 * @date Mar 10, 2005
 */
public class Util {

    public static Type ConvertToDataType(Object value) {

        Type newValue = null;

        if (value instanceof String) {
            newValue = new StringType();
        } else if (value instanceof Integer) {
            newValue = new IntegerType();
        } else if (value instanceof Float) {
            newValue = new FloatType();
        }

        newValue.setValue(value);

        return newValue;
    }
    
    public static void dump(ResultSet rset) {
		
		if (rset != null) {
			try {
				while (rset.next() == true) {
					
					int nb = rset.getMetaData().getColumnCount();
					for (int i = 1; i <= nb ; i++) {
				
						System.out.print(rset.getObject(i).toString() + " ; ");
					}
					System.out.println("");
				}
			} catch (SQLException ex) { System.out.println(ex.toString()); }
		}
	}
    
    public static void dumpQEP(QEP qepToDump) {
		
		System.out.println("");
		System.out.println("********************************************");
		System.out.println(qepToDump.toString());
		for (int i = 1; i <= qepToDump.getOperatorList().size(); i++) {
			OpNode testNode = (OpNode) qepToDump.getOperatorList().get(i+"");
			if (testNode.getProducerIDs() == null) {
				int ids[] = new int[1]; ids[0] = 0;
				testNode.setProducerIDs(ids);
			}
			System.out.print(i);
			System.out.print(" ; Name: " + testNode.getOpName());
			System.out.print(" ; " + "timeStamp: " + testNode.getOpTimeStamp());
			for (int h1 = 0; h1 < testNode.getProducerIDs().length; h1 ++) {
				System.out.print(" ; Prod: " + testNode.getProducerIDs()[h1]);
			}
			if (testNode.getParams() != null) {
				for (int h1 = 0; h1 < testNode.getParams().length; h1 ++) {
					System.out.print(" ; Param: " + testNode.getParams()[h1]);
				}
			}
			System.out.println("");
		}
		System.out.println("");
		System.out.println("********************************************");
	}
}

