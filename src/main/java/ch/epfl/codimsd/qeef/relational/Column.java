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
package ch.epfl.codimsd.qeef.relational;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.discovery.datasource.RelationalDataSource;
import ch.epfl.codimsd.qeef.types.Type;

/**
 * Descreve as informa��es necess�rias a representa��o de uma coluna no modelo relacional. 
 *
 * @author Fausto Ayres,Vinicius Fontes.
 */

@SuppressWarnings("serial")
public class Column extends Data {
    
	/**
	 * Log4j Logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RelationalDataSource.class.getName());
	
	
	/**
	 * Tamanho em bytes desta coluna.
	 */
	public int size;

	/**
	 * Deslocamento em rela��o ao in�cio da tupla.
	 */
	public int position;

	/**
	 * Define se esta coluna � uma das colunas que pertencem a chave prim�ria.
	 */
	public boolean key;
	
	public int sqlType = 0;

	/**
	 * Construtor padr�o.
	 */
	public Column(){
	    super();
	}

	/**
	 * Cria uma nova coluna com estes parametros.
	 * @param name Nome desta coluna.
	 * @param type Tipo desta coluna.
	 * @param size Tamanho em bytes desta coluna.
	 * @param position Deslocamento em nr de bytes com rela��o ao in�cio da tupla.
	 * @param key Define se esta coluna � uma das colunas que pertencem a chave prim�ria.
	 */
	public Column(String name, Type type, int size, int position, boolean key) {
        super(name, type);
		this.size = size;
		this.position = position;
		this.key = key;
	}
	
//	public Column(String name, Type type, int sqlType) {
//	    
//		super(name, type);
//		this.sqlType = sqlType;
//	}
	
	public Column(String name, Type type, int size, int position, boolean key, int sqlType) {
	    
		super(name, type);
		this.sqlType = sqlType;
		this.size = size;
		this.position = position;
		this.key = key;
	}
    
	/**
	 * Remove as refer�ncias realizadas por este objeto afim de se melhorar o desempenho do garbage collector.
	 */
    public void finalize(){
        this.type = null;
        this.name = null;
    }
    
	/**
     * Cria uma outra instância desta coluna idêntica a esta.
     * 
     * @return Nova coluna.
     */
	public Object clone() {
		Type type = (this.type == null ? null : this.type.newInstance());
		if (sqlType == 0) {
			return new Column(name, type, size, position, key);
		} else
			return new Column(name, type, size, position, key, sqlType);
		
	}

	/**
	 * Define uma representa��o textual para este objeto.
	 * @return Representa��o textual.
	 */
	public String toString(){
		
		String s = "";
		s += "Attr: ";
		s += " Name: " + this.name;
		s += " Type: " + this.type;
		s += " Size: " + this.size;
		s += " Position : " + this.position;
		s += " Key: " + this.key;
		s += " SQL Type: " + this.sqlType;

		return s;
	}

	
	/*=====================================================================================
	 * Modificadores de acesso. 
	 *====================================================================================*/
	 
    public boolean isKey() {
        return key;
    }
    public void setKey(boolean key) {
        this.key = key;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public int getSQLType() {
    	return this.sqlType;
    }
    
}

//protected void read(java.io.ObjectInputStream in)
//throws IOException, ClassNotFoundException {        
//
//  
//  name = in.readUTF();
//  type = TypeRegister.getType( in.readInt() );
//  size = in.readInt();
//  position = in.readInt();
//  key = in.readBoolean();
//  
//}

//protected void write(java.io.ObjectOutputStream out)
//throws IOException {
//  
//  out.writeUTF(name);
//  out.writeInt(TypeRegister.getCode(type));
//  out.writeInt(size);
//  out.writeInt(position);
//  out.writeBoolean(key);
//}

