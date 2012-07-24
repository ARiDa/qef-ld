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

import java.util.Collection;
import java.util.Hashtable;

import ch.epfl.codimsd.exceptions.dataSource.UnsupportedDataSourceTypeException;
import ch.epfl.codimsd.qeef.types.FloatType;
//import ch.epfl.codimsd.qeef.types.ImagedataType;
import ch.epfl.codimsd.qeef.types.IntegerType;
import ch.epfl.codimsd.qeef.types.Point;
import ch.epfl.codimsd.qeef.types.PointListType;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.WebServiceExt;
import ch.epfl.codimsd.qeef.types.OracleType;
//import ch.epfl.codimsd.qeef.types.PolydataType;
import ch.epfl.codimsd.qeef.types.WsmoWebService;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * Mant�m as configura��es necess�rias para o funcionamento de uma inst�ncia do framework QEEF.<p>
 * Como: Local de instala��o e Tipo de dados suportados.
 * 
 * They keep the necessary configurations for the functioning of an instance of framework QEEF. 
 * As:  Place of installation and Type of supported data. 
 * 
 * <p>
 * Todas as informa��es dispon�veis nesta classe podem ser obtidas de maneira est�tica para simplificar seu uso.
 * All the available information in this classroom can be gotten in static way to simplify its use. 
 * <p>
 * Trabalho Futuros:
 * <ol>
 * <li>Colocar defini��o das propriedades em arquivo de inicializa��o.
 * To place definition of the properties in inicializa��o archive. 
 * </ol>
 * 
 * @author Vinicius Fontes
 */
public class Config  {

    /**
     * Tipo de dados suportados indexados pelo nome.
     * Type of supported data indexados for the name. 
     */
    private static Hashtable<String, Object> dataTypes;
    
    /**
     * Tabela de propriedade que mantem algumas informa��es sobre a m�quina de execu��o.
     * Table of property that keeps some information on the execution machine. 
     */
    private static Hashtable<String, Object> properties;
    
    static{
        loadConfiguration();
    }
    
    /**
     * Carrega algumas propriedades utilizadas pela m�quina de execu��o.
     * It loads some properties used for the execution machine. 
     */
    private static void loadConfiguration(){
        
        defineDataTypes();
        properties = new Hashtable<String, Object>();
    }
    
    /**
     * 
     * Armazena uma propriedade a ser utilzada pela inst�ncia QEEF.
     * It stores a property to be utilzada by instance QEEF. 
     * 
     * @param name Nome da propriedade.
     * @param value Valor da proipriedade.
     */
    public static void setProperty(String name, Object value){
        properties.put(name, value);
    }
    
    /**
     * Obtem o valor de uma determinada propriedade.
     * 
     * @param name Nome da propriedade que se deseja obter.
     * 
     * @return valor da propriedade desejada.
     */
    public static Object getProperty(String name){
        return properties.get(name);
    }

       /**
     * Define os tipos de dados suportados.
     *
     */
    private static void defineDataTypes(){

        dataTypes = new Hashtable<String, Object>();

        dataTypes.put(Constants.POINT, new Point());
        dataTypes.put(Constants.STRING, new StringType());
        dataTypes.put(Constants.INTEGER, new IntegerType());
       // dataTypes.put(Constants.POLYDATA, new PolydataType());
        //dataTypes.put(Constants.IMAGEDATA, new ImagedataType());
        dataTypes.put(Constants.FLOAT, new FloatType());
        dataTypes.put(Constants.POINT_LIST, new PointListType());
        dataTypes.put(Constants.WEBSERVICE_EXT, new WebServiceExt());
        dataTypes.put(Constants.ORACLETYPE, new OracleType());
        dataTypes.put(Constants.WEBSERVICE_WSMO, new WsmoWebService());
    }

    /**
     * Retorna uma refer�ncia a um determinado tipo da m�quina de execu��o.
     * 
     * @param name Nome do tipo desejado.
     * @return Refer�ncia a um tipo. Null se n�o existir tipo com o nome informado.
     * 
     * @throws UnsupportedDataTypeException Se o tipo n�o existir.
     */
    public static Type getDataType(String name) throws UnsupportedDataSourceTypeException{

    	Type aux;
        name = name.trim();
        aux = (Type)dataTypes.get(name);
        if(aux == null)
            throw new UnsupportedDataSourceTypeException("Tipo n�o suportado: " + name);
        return aux;
    }
    
    /**
     * Tipos de dados suportados nesta inst�ncia do framework QEEF.
     * 
     * @return Tipo de dados suportados.
     */
    public static Collection supportedDataTypes(){
        return dataTypes.values();
    }
   
}

