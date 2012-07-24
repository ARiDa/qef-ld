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

import java.util.*;
import java.io.*;
//import vtk.vtkDirectory;

/**
 * Classe abstrata que generaliza uma unidade de dados no framework ch.epfl.codimsd.qeef.
 * Abstract classroom that generalizes a unit of data in framework ch.epfl.codimsd.qeef. 
 * 
 * @author Vin�cius Fontes.
 */

public abstract class DataUnit implements Cloneable, Serializable {   

   /* static
    {
	    System.loadLibrary("vtkCommonJava");
	    System.loadLibrary("vtkFilteringJava");
	    System.loadLibrary("vtkIOJava");
	    System.loadLibrary("vtkImagingJava");
	    System.loadLibrary("vtkGraphicsJava");
	    System.loadLibrary("vtkRenderingJava");
            System.loadLibrary("vtkVolumeRenderingJava");
	    System.loadLibrary("vtkParallelJava");
    }*/

    /**
	 * 
	 */
	private static final long serialVersionUID = -3914438221193407987L;
	
	/**
     * Tabela de propriedade utilizadas para guardar informa��es durante o processamento desta tupla.
     * Table of property used to keep the information during the processing of this tuple. 
     */
    protected Properties properties;

    /**
     * Construtor padr�o.
     */
    public DataUnit() {
        properties = new Properties();
    }

    /**
     *  A implementa��o deste m�todo default da linguagem se tornou obrigt�rio
     * com o objetivo de acelerar o processo de libera��o de mem�ria que �
     * realizado no Garbage Colector. Recomenda-se atribuir null a todas as suas refer�ncias.
     * 
     * The implementation of this default method of the language if became obrigt�rio 
     * with the objective to speed up the process of memory release that is carried through 
     * in the Garbage Collector.  One sends regards to attribute null to all its references. 
     */
    public abstract void finalize(); 
    
	/**
     * Cria uma outra instancia deste objeto id�ntica a esta.
     * It creates another instance of this identical object to this. 
     * 
     * @return Nova inst�ncia.
     * 
     * @see Object#clone()
     */
	public abstract Object clone();
	

    /**
     * Grava o conte�do desta unidade de dados de maneira formatada no fluxo out.
     * Out records the content of this unit of data in way formatted in the flow. 
     * 
     * @param out Fluxo para onde unidade vai ser gravada.
     * @throws IOException Se algum problema acontecer durante a grava��o.
     */
    public abstract void display(Writer out) throws IOException;
    
    /**
     * N�mero de unidades de dados representados por esta estrutura.
     * Number of units of data represented for this structure. 
     * 
     * @N�mero de unidade de dados representadas.
     */
    public abstract int size();

    /**
     * Define como uma inst�ncia deve ser serializada. Optou-se por n�o usar a
     * implementa��o de serializa��o default de java por ObjectOutputStream
     * por causa do overhead gerado no tamanho do arquivo.
     * Realiza a grava��o das propriedades da unidade de dados.
     * 
     * It defines as an instance must be serializada.  Default was opted to not 
     * using the implementation of serialisation of java for ObjectOutputStream 
     * because of overhead generated in the size of the archive.  It carries through 
     * the writing of the properties of the unit of data. 
     * 
     * @param Fluxo para onde unidade de dados ser� serializada. 
     * @param Metadados que descreve esta unidade de dados.
     * 
     * @throws  IOException Se algum erro acontecer durante a serializa��o.
     * 
     * @see DataUnit#read(DataInputStream)
     * @see ch.epfl.codimsd.qeef.io.InstanceWriter
     * @see ch.epfl.codimsd.qeef.io.InstanceReader
     */
    /*
     * Formato: nr de propriedades [nome valor]*
     */
    public void write(DataOutputStream out, Metadata instMetadata)
            throws IOException{
        
        //Salva tabela de propriedades
        //N�o utiliza formato default para evitar desperd�cio de espca�o
        

        if( properties.size() > Byte.MAX_VALUE) {
            throw new IOException("Supports only " + Short.MAX_VALUE + " properties." );
        }
        
        Enumeration<Object> itProp;
        String name, value;
        
        itProp = properties.keys();
        out.writeByte(((byte)properties.size()));
        
        while(itProp.hasMoreElements()){
        
            name = (String)itProp.nextElement();
            value = properties.getProperty(name);
            
            out.writeUTF(name);
            out.writeUTF(value);
        }        
    }

    /**
     * Define como uma inst�ncia deve ser deserializada. Realiza a leitura das propriedades desta unidade de dados.
     * 
     * It defines as an instance must be deserializada.  It carries through the 
     * reading of the properties of this unit of data. 
     * 
     * @param Fluxo de onde unidade de dados ser� lida. 
     * @param Metadados que descreve esta unidade de dados.
     * 
     * @throws IOException Se algum erro acontecer durante a deserializa��o.
     * @throws EOFException Qdo n�o houver mais unidades de dados a serem lidas.
     *  
     * @see DataUnit#write(DataOutputStream)
     * @see ch.epfl.codimsd.qeef.io.InstanceWriter
     * @see ch.epfl.codimsd.qeef.io.InstanceReader
     */
    public void read(DataInputStream in, Metadata instMetadata)
            throws IOException, EOFException{
        
        String value, name;
        byte total;
        
        total = in.readByte();
        for(byte i=0; i<total; i++){
            
            name = in.readUTF();
            value = in.readUTF();
            properties.setProperty(name, value);
        }
        
    }

    public void readSmooth(String in, Metadata instMetadata)
            throws IOException, EOFException{

        String value, name;
        byte total;

        total = 0;

        for(byte i=0; i<total; i++){

            name = in;
            value = in;
            properties.setProperty(name, value);
        }
    }

    /*public void readImages(vtkDirectory in, Metadata instMetadata)
            throws IOException, EOFException{

        String value, name;
        byte total;

        total = 0;

        for(byte i=0; i<total; i++){
            name = in.toString();
            value = in.toString();
            properties.setProperty(name, value);
        }
    }*/

    /**
     * Obtem o valor da propriedade que possui este nome.
     * It gets the value of the property that possesss this name. 
     * 
     * @param name Nome da propriedade.
     * @return Valor da propriedade. 
     */
    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    /**
     * Define uma propriedade.
     * @param name Nome da propriedade.
     * @param value Valor da propriedade.
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Remove a propriedade que possui este nome.
     * @param Nome da propriedade.
     * @return True se a propriedade estava definida, false caso contr�rio. 
     */
    public boolean removeProperty(String name) {
        if (properties.containsKey(name)) {
            properties.remove(name);
            return true;
        }
        return false;
    }

    /**
     * Retorna todas as propriedades definidas.
     * @return Propriedades definidas. 
     */
    public Properties getProperty() {
        return properties;
    }

    /**
     * Subistitui as propriedades definidas por este conjunto de propriedades.
     * Substitute the properties defined for this set of properties. 
     * 
     * @param properties Conjunto de propriedades. 
     */
    public void setProperty(Properties properties) {
        this.properties = properties;
    }

}


