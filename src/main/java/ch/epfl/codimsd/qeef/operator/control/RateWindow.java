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
package ch.epfl.codimsd.qeef.operator.control;

public class RateWindow implements Cloneable{

	private int rates[];
	
	private int size;
	
	/**
	 * Comeca em zero e vai ate size
	 */
	private int currSize;
	
	private int curr;
	
	private int rate;
	
	public RateWindow(int size){
		
		this.size  = size;
		this.curr  = 0;
		this.rate  = 0;
		this.currSize = 0;
		this.rates = new int[size];				
	}
	
	/**
	 * Insere na posicao atual e depois determina proxima posicao
	 */
	public void insertRate(int newRate){
		
		//Insere taxa
		rates[curr] = newRate;
		curr = (curr+1)%size;
		currSize = currSize<size?currSize+1:currSize;
		
		//Determina a taxa atual
		int sum=0;		
		for(int i=0; i < currSize; i++){
			sum += rates[i];
		}		
		rate = sum / currSize;
	}
	
	public int getRate(){
		
		return rate;
	}
	
	public Object clone(){
		
		RateWindow aux = new RateWindow(size);
		
		aux.curr = this.curr;
		aux.rate = this.rate;
		aux.currSize = this.currSize;
		
		for(int i=0; i < currSize; i++){
			aux.rates[i] = this.rates[i];
		}
		
		return aux;
	}
	
	public static void main(String[] args) {
		
		RateWindow r = new RateWindow(3);
		
		r.insertRate(10);
		System.out.println(r.getRate());
		r.insertRate(20);
		System.out.println(r.getRate());
		r.insertRate(30);
		System.out.println(r.getRate());
		r.insertRate(40);
		System.out.println(r.getRate());
	}
}

