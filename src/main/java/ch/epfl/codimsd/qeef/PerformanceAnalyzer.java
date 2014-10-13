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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.epfl.codimsd.config.AppConfig;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.util.FileUtils;


public class PerformanceAnalyzer {

	private BufferedWriter  out ;
	private static PerformanceAnalyzer ref = null;
	private String file = "";
	boolean isFileClosed = true;
	
	private PerformanceAnalyzer() {
		
		createNewFile();
		
		try {
			
			out  = new BufferedWriter(new FileWriter(file));
			isFileClosed = false;
		
		} catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
	
	private void createNewFile() {
		SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getInstance();
		sdf.applyPattern("yyyyMMdd_HHmmss");
		String newFileEntry = "Perf_" + sdf.format(Calendar.getInstance().getTime()) + ".log";
		String dir = AppConfig.OUTPUT_DIR +  File.separator + "logs" + File.separator 
				+ "performances" + File.separator;
		FileUtils.createDirectoryStructure(dir);
		file =  dir + newFileEntry;
	}
	
	public static synchronized PerformanceAnalyzer getPerformanceAnalyzer() {
		
		if (ref == null)
			ref = new PerformanceAnalyzer();
		
		return ref;
	}
	
	public void log(String title, long time) {
		
		long seconds = time / 1000;
		long minutes = seconds / 60;
		
		String fileContent = title + time + "(milli) ; " + seconds + "(sec) ; " + minutes + "(min)"; 
		
		try {
			
			if (isFileClosed == true) {
				
				createNewFile();
				out  = new BufferedWriter(new FileWriter(file));
				isFileClosed = false;
			}
	        
			out.write(fileContent);
	        out.newLine();
	       
		} catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
	
	public void finalizeLogging() {

		try {
			out.flush();
	        out.close();
	        isFileClosed = true;
		} catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
}

