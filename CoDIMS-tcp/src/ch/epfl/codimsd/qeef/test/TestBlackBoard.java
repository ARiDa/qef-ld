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

import java.util.HashMap;

import ch.epfl.codimsd.qeef.BlackBoard;

public class TestBlackBoard {

	
	public static void main(String[] args) {
		
		try {
			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put("TEST", "SUCCESS");
			HashMap hash = bl.getHashtable();
			if (hash.containsKey("TEST"))
				System.out.println("Contient test");
			else
				System.out.println("ne contient pas test");
		
			String myObj = (String) hash.get("TEST");
			System.out.println("Objet pass� � copyBlackBoard " + myObj);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}

