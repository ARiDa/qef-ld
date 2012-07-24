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
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.epfl.codimsd.qeef.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.Data;
//import vtk.vtkDirectory;

/**
 * @author Vinicius Fontes
 *
 * @date Mar 9, 2005
 */
@SuppressWarnings("serial")
public class IntegerType implements Type {
    
    //Campos auxiliares utilizados na definicao e INTEGER_PATTERN   
    public static char digSep, decSep;
    static {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        digSep = dfs.getGroupingSeparator();
        decSep = dfs.getDecimalSeparator();
    }
    
    /**
     * Define um n�mero inteiro, com ou sem formata��o. Aceita
     * qualquer n�mero com ou sem separador digito(milhar)
     */
    public static final String INTEGER_PATTERN = "(([0-9]+([" + digSep + "][0-9(?!.)]+)*)(?![.0-9]))";
    
    private int value;

    //------------------------------------------------------------
    public IntegerType(){
        super();
    }
    
    public IntegerType(String value){
        this.setValue(value);
    }
    
    public IntegerType(byte value[], int offset){
        this.setValue(value, offset);
    }
    
    public IntegerType(int value){
        this.value = value;
    }
    //------------------------------------------------------------
    
    public void setValue(byte value[], int offset){}
    
    public void setValue(String value){
      //  value = value.replaceAll(digSep+"", "" );
        this.value = Integer.parseInt(value);
    }
    
    public void setValue(int value){
        this.value = value;
    }
    
    public void setValue(Object value){
        this.value = ((Integer)value).intValue();
    }
    
    //------------------------------------------------------------

    public int compareTo(Object o) {
        
        return value - ((IntegerType)o).intValue();
    }

    
    public int intValue(){
        return value;
    }
    
	public Object clone(){
	    IntegerType v = new IntegerType();
	    v.value = this.value;
	    return v;
	}
    
    
    public String toString(){
        return new String("" + value);
    }

    /*
     * 
     */
    public void display(Writer out) throws IOException{
        out.write(value + " ");
    }
    
    /*
     * 
     */    
    public int displayWidth() {
        return 10;
    }
    
    /*
     * 
     */
    public Type newInstance(){
        return new IntegerType();
    }

    
    /*
     * 
     */
    public Type read(DataInputStream in) throws IOException{
        return new IntegerType(in.readInt());  
    }

    public Type readSmooth(String in) throws IOException {
        return null;
    }

/*    public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
        return null;
    }*/

    /*
     * 
     */
    public void write(DataOutputStream out) throws IOException{
        out.writeInt(value);
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
     * @see Type.recognitionPattern()
     */
    public String recognitionPattern(){
        
        return INTEGER_PATTERN;
    }
    
    public  void dispose(){
        //nothing to do
    }

    public static void main(String[] args) {
        
        Matcher m = Pattern.compile("([0-9]+([" + digSep + "][0-9]+)*)(?!.)").matcher("333.129");
        
        m.find();
        System.out.println(m.group());
    }

    public void setMetadata(Data data){}
}

