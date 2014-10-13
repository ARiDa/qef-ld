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
package ch.epfl.codimsd.qeef.test;

import org.apache.log4j.*;

public class LoggerTest {

	 static Category cat = Category.getInstance(LoggerTest.class.getName());
	 
	 public static void main(String args[]) {
	        
		 	// Try a few logging methods
	        cat.debug("Start of main()");
	        cat.info("Just testing a log message with priority set to INFO");
	        cat.warn("Just testing a log message with priority set to WARN");
	        cat.error("Just testing a log message with priority set to ERROR");
	        cat.fatal("Just testing a log message with priority set to FATAL");

	        // Alternate but INCONVENIENT form
	        cat.log(Priority.DEBUG, "Calling init()");

	        new LoggerTest().init();
	    }

	 public void init() {
		 
	     java.util.Properties prop = System.getProperties();
	     java.util.Enumeration enum2 = prop.propertyNames();

	     cat.info("***System Environment As Seen By Java***");
	     cat.debug("***Format: PROPERTY = VALUE***");

	     while (enum2.hasMoreElements()) {
	        String key = (String) enum2.nextElement();
	        cat.info(key + " = " + System.getProperty(key));
	     }
	 }
}

