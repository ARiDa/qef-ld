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

/**
 * Classe abstrata que generaliza os modulos ch.epfl.codimsd.qeef. Um m�dulo encapsula uma caracteristica do plano de execu��o
 * dos demais operadores do plano.Para isso, introduz os operadores de controle necess�rios entre os operadores
 * alg�bricos do plano.
 *
 * Changes made by Othman :
 * - deleted : private Vector componentes; import java.util.*;
 *
 * @author Fausto Ayres, Vinicius Fontes, Othman Tajmouati.
 */
public abstract class Modulo {

    public Modulo() {
        super();
    }
}

