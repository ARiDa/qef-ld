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
package ch.epfl.codimsd.qeef.scheduler;

import ch.epfl.codimsd.qeef.scheduler.G2NNode;

/**
 * keeps information on each fragment.
 * 
 * @author Vinicius Fontes
 * 
 * 
 * 
 * OBS: the computation of B is not defined using the function getMinBlockSize due to rounding problems.
 */
public class G2NNodeIterative extends G2NNode implements Cloneable {


	/**
	 * Serialization and deserialization time of a tupla
	 */
	public int msgProcessingTime;
	
	/**
	 * Nr Iterations
	 */
	public int iterations;	

	/**
	 * 
	 * @param id
	 * @param rate
	 * @param msgProcessingTime
	 * @param netTransmitionRate
	 * @param nrInstances
	 * @param iterations
	 */
	public G2NNodeIterative(int id, int rate, int msgProcessingTime,
			  int nrInstances, int iterations) {

		super(id, nrInstances, rate);

		this.msgProcessingTime = msgProcessingTime;

		this.iterations = iterations;
	}

	public int getMinimunBlockSize() {
		return (int) Math.ceil((2.0 * msgProcessingTime) / rate);
	}


	/**
	 * Cost in milliseconds
	 */
	public float evaluationCost(int nrNodes) {

		float cost = 0;
		int prodRate = rate;
		float minBlock = (float)(2.0*msgProcessingTime)/prodRate;

		if (nrInstances == 0)
			return 0;
		else if (nrInstances >= 2 * minBlock)
			cost = (2 * msgProcessingTime) + (iterations * nrInstances * prodRate);
                else if (nrInstances < (2.0 * minBlock) && nrInstances > minBlock)

                        cost = 2 * msgProcessingTime + nrInstances * prodRate + ((iterations - 1) * (2*msgProcessingTime + 2 * msgProcessingTime));
                else
			cost = iterations * (2 * msgProcessingTime + (nrInstances * prodRate));

		return cost;
	}

	/**
	 * Qtas tuplas este no pode processar em time - nr Instancias que ele ja tem
	 * 
	 * @param time
	 *            tempo em ms
	 * @return tuplas que este no pode processar neste tempo Se
	 *         supportedInstances < 0, significa que com o nr de instancias que
	 *         ele ja possui se custo � maior que time. Se supportedInstances ==
	 *         0, pode cair em dois casos: 1-Ele ja tem algumas tuplas e se
	 *         receber mais uma seu custo ultrapassa time 2-Ele n�o tem nenhuma
	 *         tupla e n�o consegue processar nada com custo time
	 */
//	public int ableToProcess(int time) {
//
//		int Q;
//
//		float minBlockSize =  (float)(2.0*msgProcessingTime)/rate;
//
//		Q = (time - 2 * msgProcessingTime) / (rate * iterations);
//
//		if (Q < 2.0 * minBlockSize) {//Caiu no 1 caso
//
////			A seguinte simplificacao foi utilizada: B * taxa = (2*MSG / taxa) * taxa = 2*MSG
////			Q = (time - 2 * msgProcessingTime - ((iterations - 1) * (minBlockSize
////					* rate + 2 * msgProcessingTime)))
////					/ rate;
//			Q = (time - 2 * msgProcessingTime - 
//					((iterations - 1) * (2*msgProcessingTime + 2 * msgProcessingTime)) )
//					/ rate;
//
//			if (Q <= minBlockSize) { //Caiu no 2 caso
//
//				//Caiu no 3 caso
//				Q = ((time - 2 * iterations * msgProcessingTime) / (iterations * rate));
//			}
//		}
//
//		return Q - nrInstances;
//	}
	public int ableToProcess(float time) {

		int Q;

		float minBlockSize =  (float)(2.0*msgProcessingTime)/rate;

		Q = (int)Math.floor( (time - 2 * msgProcessingTime) / (rate * iterations) );

		if (Q < 2.0 * minBlockSize) {//Caiu no 1 caso

//			A seguinte simplificacao foi utilizada: B * taxa = (2*MSG / taxa) * taxa = 2*MSG
//			Q = (time - 2 * msgProcessingTime - ((iterations - 1) * (minBlockSize
//					* rate + 2 * msgProcessingTime)))
//					/ rate;
			if(time > 4*msgProcessingTime){
				Q = (int)Math.floor(2 * minBlockSize - 1);
			} else	{
				//Caiu no 3 caso
				Q = (int)Math.floor( ((time - 2 * iterations * msgProcessingTime) / (iterations * rate)) );
			}
		}

		return Q - nrInstances;
	}

	public Object clone() {

		G2NNodeIterative aux = new G2NNodeIterative(id, rate, msgProcessingTime,
				nrInstances, iterations);

		return aux;
	}
}

