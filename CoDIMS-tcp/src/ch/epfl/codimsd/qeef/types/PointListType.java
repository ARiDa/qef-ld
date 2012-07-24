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
 * Created on Mar 9, 2005
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.epfl.codimsd.qeef.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import ch.epfl.codimsd.qeef.Data;

/**
 * @author Vinicius Fontes
 *
 * @date Mar 9, 2005
 */
@SuppressWarnings("serial")
public class PointListType extends Vector<Type> implements Type {
    
    public PointListType(){
        super();
    }
    
    public PointListType(int size){
        super(size);
    }
    

    //------------------------------------------------------------
    
    public void setValue(byte value[], int offset){}
    
    public void setValue(String value){}
    
    public void setValue(float value){}
    
    public void setValue(Object value){}
    
    //------------------------------------------------------------


    public int compareTo(Object o) {
        
        return 0;
    }

    /*
     * 
     */
    public void display(Writer out) throws IOException{
        Type curr;
        for(int i=0; i < 4; i++){
            curr = (Type)get(i);
            out.write( curr.toString() );
        }        
    }
    
    /*
     * 
     */    
    public int displayWidth() {
        return 49 + 3;
    }
    
    /*
     * 
     */
    public Type newInstance(){
        return new PointListType();
    }

    
    /*
     * 
     */
    public Type read(DataInputStream in) throws IOException{
        
        PointListType newList = new PointListType();
        Point curr = new Point();
        
        for(int i=0; i < 4; i++){            
            newList.add(curr.read(in));
        }
        
        return newList;
    }

    public Type readSmooth(String in) throws IOException {
        return null;
    }

/*    public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
        return null;
    }
*/
    
    /*
     * 
     */
    public void write(DataOutputStream out) throws IOException{

        Point point;
        for(int i=0; i < 4; i++){
            point = (Point)get(i);
            point.write(out);
        }
    }
    
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
    
    /*
     * 
     */
    public String toString(){
        
        String aux="{";
        
        for(int i=0; i < size(); i++){
            Point p = (Point)get(i);
            aux = aux + p;
        }
        
        return aux + "}";
    }

    public void setMetadata(Data data){}
}

