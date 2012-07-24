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

import java.io.Serializable;

import ch.epfl.codimsd.qeef.types.Type;

/**
 * 
 * digite aqui descricao do tipo
 * it types descricao of the type here 
 * 
 * it types describes the type here 
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 29, 2005
 */
public abstract class Data implements Serializable, Cloneable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -1351377975973357698L;
	
    /**
     * Nome da coluna.
     */
	public String name; 

	/**
	 * Tipo desta coluna.
	 */
	public Type type;
	
	/**
	 * Construtor padr�o. 
	 */
	public Data(){
	    name = null;
	    type = null;
	}

	/**
	 * Cria uma inst�ncia de dados com estes parametros.
	 * @param name Nome desta coluna.
	 * @param type Tipo desta coluna.
	 */
	public Data(String name, Type type){
		this.name = name;
		this.type = type;
	}	
	
    /**
     *  A implementa��o deste m�todo default da linguagem se tornou obrigt�ria
     * com o objetivo de acelerar o processo de libera��o de mem�ria que �
     * realizado no Garbage Colector. Recomenda-se atribuir null a todas as suas refer�ncias.
     */
    public abstract void finalize(); 
    
	/**
     * Cria uma outra inst�ncia deste dado id�ntica a esta.
     * 
     * @return Novo dado..
     */
	public abstract Object clone();
	
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Data)) {
			return false;
		}
		Data data = (Data) obj;
		if (this.name.equals(data.name) && ( (this.type == null && data.type == null) || (this.type.equals(data.type)) ) ) {
			return true;
		}
		return false;
	}
	
	
	/*=====================================================================================
	 * Modificadores de acesso. 
	 *====================================================================================*/
	
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

}
///**
//* 
//* Define como uma inst�ncia deve ser serializada. Optou-se por n�o usar a
//* implementa��o default por quest�es de performance e ocupa��o de mem�ria.
//* A implementa��o default mant�m em mem�ria os objetos serializados o que 
//* gera erros se o volume de dados manipulado for muito grande.
//* 
//* @see java.io.ObjectOutputStream#writeObject(java.lang.Object)
//*/
//protected abstract void write(DataOutputStream out)
//     throws IOException;
//
///**
//* 
//* Define como uma inst�ncia deve ser serializada. Optou-se por n�o usar a
//* implementa��o default por quest�es de performance e ocupa��o de mem�ria.
//* A implementa��o default mant�m em mem�ria os objetos serializados o que 
//* gera erros se o volume de dados manipulado for muito grande.
//* 
//* @see ObjectInput#readObject()
//*/
//protected abstract void read(DataInputStream in)
//     throws IOException;

