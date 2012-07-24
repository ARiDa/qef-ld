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
import java.io.Serializable;
import java.io.Writer;
import java.text.NumberFormat;

import ch.epfl.codimsd.qeef.Data;

@SuppressWarnings("serial")
public class Point implements Serializable, Type {

	public int id;
	public float x, y, z;
	
	public Point(){
	    super();
	}
	
	public Point(int id, float x, float y, float z){
	    super();
	    
	    this.id = id;
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}
	
    public void setValue(byte value[], int offset){}
    
    public void setValue(String value){
        String valores[];
        valores = value.split(" ");
        
        id = Integer.parseInt(valores[0]);
        x = Float.parseFloat(valores[1]);
        y = Float.parseFloat(valores[2]);
        z = Float.parseFloat(valores[3]);   
    }
    
    public void setValue(Object value){
       
        id = ((Point)value).id;
        x = ((Point)value).x;
        y = ((Point)value).y;
        z = ((Point)value).z;
    }

    
    public int compareTo(Object o) {
        
        Point p = (Point)o;
       
        return  p.id - this.id;
        //return (x==p.x && y ==p.y && z==p.z)?0:-1;
    }

	
	public Object get(String name){

	    Object r = null;
	    
	    if( name.equalsIgnoreCase("id"))
	        r = new IntegerType(id);
	    if( name.equalsIgnoreCase("x"))
	        r = new FloatType(x);
	    if( name.equalsIgnoreCase("y"))
	        r = new FloatType(y);
	    if( name.equalsIgnoreCase("z"))
	        r = new FloatType(z);
	    
	    return r;
        }

	public String toString() {

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(2);
		nf.setMaximumFractionDigits(7);

		nf.setMinimumFractionDigits(7);
		nf.setMinimumIntegerDigits(2);

		return "(" + formatar(id, 5) + " " + (x >= 0 ? "0" : "") + nf.format(x) + " "
				+ (y >= 0 ? "0" : "") + nf.format(y) + " "
				+ (z >= 0 ? "0" : "") + nf.format(z) + ")";

	}

	public String formatar(int nr, int tam) {

		String strNum = "" + nr;

		while (strNum.length() < tam)
			strNum = "0" + strNum;

		return strNum;
	}

	public Object clone(){
		
		Point p = new Point();
		p.id = id;
		p.x = x;
		p.y = y;
		p.z = z;
		
		return p;
	}
	
    /*
     * 
     */
    public void display(Writer out) throws IOException{
        out.write( toString() + " ");
    }
    
    /*
     * 
     */    
    public int displayWidth() {
        return 46 + 3; // 3 dos delimitadores
    }
    
    /*
     * 
     */
    public Type newInstance(){
        return new Point();
    }


	public void write(DataOutputStream out) throws IOException{
		
		out.writeInt(id);
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
	}
	public Type read(DataInputStream in) throws IOException {

	    return new Point(in.readInt(),in.readFloat(),in.readFloat(),in.readFloat());

	}

    public Type readSmooth(String in) throws IOException {
        return null;
    }
/*
    public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
        return null;
    }
*/

    /**
     *  A implementa��o deste m�todo default da linguagem se tornou obrigat�ria
     * com o objetivo de agilizar o processo de libera��o de mem�ria que �
     * realizado no Garbage Colector. Recomenda-se atribuir null a todas as suas refer�ncias.
     */
    public void finalize() throws Throwable{
        super.finalize();
    }
    
    /**
     * @see Type#recognitionPattern()
     */
    public String recognitionPattern(){
        //TODO
        return null;
    }
    
    public void setMetadata(Data data){}
}

