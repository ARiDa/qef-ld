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
package ch.epfl.codimsd.qeef.operator.modules;


import java.util.*;

import ch.epfl.codimsd.qeef.Modulo;
import ch.epfl.codimsd.qeef.Operator;

/**
 * Representa o tipo de liga��o mais simples entre operadores.
 * Neste tipo de m�dulo nenhum operador de controle � adicionado 
 * entre um consumidor e produtor.
 * 
 * It represents the type of simpler linking between operators.  
 * In this type of module no operator of control is added between a consumer and producer. 
 *
 * @author Fausto Ayres, Vinicius Fontes
 */
public class ModFix extends Modulo {
    
    @SuppressWarnings("unused")
	private Operator root;
    private Vector<Operator> operadores;
         
    /**
     * Construtor padr�o.
     * 
     * @param root Primeiro operador do fragmento de plano encapsulado por este m�dulo.
     * @param producer Um produtor do primeiro operador.
     */
	public ModFix (Operator root, Operator producer) {
	    super();
	    
   		this.root = root;
   		operadores = new Vector<Operator>();
   		adicionar (root, producer);
   	}	
   	
   	
	/**
	 * Realiza a liga��o entre os dois operadores.
	 * @para cons Operador consumidor.
	 * @param prod Operador produtor.
	 */
	public void adicionar (Operator cons, Operator prod)
	{
//			int a= operadores.indexOf(cons);
//			int b= operadores.indexOf(prod);
//			if (a==-1) operadores.add(cons);
//			if (b==-1) operadores.add(prod);
//			cons.addProducer( prod );
//			prod.addConsumer(cons);
//			
			int a= operadores.indexOf(cons);
			int b= operadores.indexOf(prod);
			if (a==-1) operadores.add(cons);
			if (b==-1) operadores.add(prod);
			cons.addProducer( prod );
			if (prod!= null)prod.addConsumer(cons);
     }

   

}

