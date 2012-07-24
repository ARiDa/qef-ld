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
import java.io.ObjectInput;
import java.io.Serializable;
import java.io.Writer;

import ch.epfl.codimsd.qeef.Data;
//import vtk.vtkDirectory;

/**
 * Classe abstrata que define a interface de um tipo de dados suportados pelo
 * m�quina de execu��o.
 * 
 * Abstract classroom that defines the interface of a type of data supported for the machine of execution. 
 * 
 * @author Vinicius Fontes
 * 
 * @date Mar 8, 2005
 */
public interface Type extends Comparable<Object>, Cloneable, Serializable {

	// Added by Othman
	public void setMetadata(Data data);
	
    /**
     * Atribui um valor a uma inst�ncia do tipo de dado a partir de sua representa��o textual.
     * 
     * @param value Valor a ser atribuido na forma de texto.
     */
    public  void setValue(String value);

    /**
     *  Atribui um valor a uma inst�ncia do tipo de dado a partir de um tipo da linguagem java.
     */
    public  void setValue(Object value);

    /**
     * Cria uma uma nova inst�ncia id�ntica a este objeto.
     * 
     * @return Nova inst�ncia.
     * 
     * @see Object#clone()
     */
    public Object clone();

    /**
     * Grava o valor desta inst�ncia de maneira formatada em um fluxo de texto.
     * Utilizado na visualiza��o do resultado de uma consulta.
     * 
     * @param out Fluxo no qual o valor ser� gravado.  
     */
    public void display(Writer out) throws IOException;

    /**
     * Obtem a largura ocupada (em n�mero de caracteres) na exibi��o do conte�do de um tipo.  
     */
    public int displayWidth();

    /**
     * Cria uma nova inst�ncia deste tipo sem nenhum valor atribuido. 
     */
    public Type newInstance();

    /**
     * 
     * Fornece express�o regular capaz de reconhecer este tipo em textos. Define sua formata��o.
     * Tipicamente usado no parser de predicados. Ver PredicateParser.
     * 
     * @return Express�o Regular para este tipo.
     */
    public String recognitionPattern();

    /**
     * O framework QEEF define um mecanismo pr�prio de serializa��o dos dados que � implementado
     * pela interface InstanceReader e InstanceWrite. Optou-se por n�o utilizar o mecanismo proposto
     * pela linguagem java via interface Serializable e ObjectOutputStream/ObjectInputStream uma vez que um 
     * overhead muito grande � inserido. A implementa��o deste m�todo definir� como
     * uma inst�ncia de Type dever� ser serializada.
     * 
     * @see java.io.ObjectOutputStream#writeObject(java.lang.Object)
     */
    public void write(DataOutputStream out) throws IOException;

    /**
     * O framework QEEF define um mecanismo pr�prio de serializa��o dos dados que � implementado
     * pela interface InstanceReader e InstanceWrite. Optou-se por n�o utilizar o mecanismo proposto
     * pela linguagem java via interface Serializable e ObjectOutputStream/ObjectInputStream uma vez que um 
     * overhead muito grande � inserido. A implementa��o deste m�todo definir� como
     * uma inst�ncia de Type dever� ser deserializada.
     *      * 
     * @see ObjectInput#readObject()
     */
    public Type read(DataInputStream in) throws IOException;

    public Type readSmooth(String in) throws IOException;

//    public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException;


    /**
     * A implementa��o deste m�todo default da linguagem se tornou obrigat�ria
     * com o objetivo de agilizar o processo de libera��o de mem�ria que �
     * realizado no Garbage Colector. Recomenda-se atribuir null a todas as suas
     * refer�ncias.
     */
    public void finalize() throws Throwable;

}

