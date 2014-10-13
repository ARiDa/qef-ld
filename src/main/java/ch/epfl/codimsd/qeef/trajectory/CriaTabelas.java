package ch.epfl.codimsd.qeef.trajectory;
/*
 * Created on Dec 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.StringTokenizer;


import ch.epfl.codimsd.qeef.types.Point;

/**
 * @author Vinicius Fontes
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CriaTabelas {
	
	public static void main(String[] args) throws Exception{
		
		BufferedReader arq = new BufferedReader( new FileReader(new File("/home/douglas/dados/HemoOut_cl.vtk")) );
		
		Point pontos[];
		String aux, aux2;
		StringTokenizer stk;
		
		//Le cabecalho pontos
		int nrPontos;
		aux = arq.readLine();
		aux = arq.readLine();
		aux = aux.substring(15, 21).trim();
		nrPontos = Integer.parseInt( aux );

		pontos = new Point[nrPontos];
		
		//Le pontos
		BufferedWriter arqPontos = new BufferedWriter( new FileWriter(new File("/home/douglas/dados/Vertice.txt")) );

		for(int i=0; i<nrPontos; i++){
			
			pontos[i] = new Point();
			
			float x,y,z;
			
			//le ponto
			aux = arq.readLine();
			x = Float.parseFloat( aux.substring(0,18).trim() );
			y = Float.parseFloat( aux.substring(18, 34).trim() );
			z = Float.parseFloat( aux.substring(34).trim() );
			
			pontos[i].id = i;
			pontos[i].x = x;
			pontos[i].y = y;
			pontos[i].z = z;

			//Grava ponto
			arqPontos.write(formatar(pontos[i].id,5) + " " + pontos[i].toString() + "\n");
		}

		arqPontos.close();

		//Le Tetraedros
		BufferedWriter arqTetraedros = new BufferedWriter( new FileWriter(new File("/home/douglas/dados/Tetraedro.txt")) );
		int nrTetraedros;
		
		aux = arq.readLine();
		aux = arq.readLine();
		nrTetraedros = Integer.parseInt( aux.substring(12,23).trim() );
		
		for(int i=0; i<nrTetraedros; i++){
			
			int nrPonto;
			aux = arq.readLine();
			
			//Grava id tetraedro
			arqTetraedros.write(formatar(i, 5));
			
			//Le  e grava tetraedro 1
			aux2 = aux.substring(8,14);
			nrPonto = Integer.parseInt( aux2.trim() );
			arqTetraedros.write(" " + formatar(pontos[nrPonto].id,5) + " " + pontos[nrPonto].toString());
			
			//Le  e grava tetraedro 2			
			aux2 = aux.substring(15,21);
			nrPonto = Integer.parseInt( aux2.trim() );
			arqTetraedros.write(" " + formatar(pontos[nrPonto].id,5) + " " + pontos[nrPonto].toString());
			
			//Le  e grava tetraedro 3
			aux2 = aux.substring(22, 28);
			nrPonto = Integer.parseInt( aux2.trim() );
			arqTetraedros.write(" " + formatar(pontos[nrPonto].id,5) + " " + pontos[nrPonto].toString());
			
			//Le  e grava tetraedro 4
			aux2 = aux.substring(29);
			nrPonto = Integer.parseInt( aux2.trim() );
			arqTetraedros.write(" " + formatar(pontos[nrPonto].id,5) + " " + pontos[nrPonto].toString());
			
			//Termina linha
			arqTetraedros.write("\n");	
			
		}
		arqTetraedros.close();
		
		//Le Vetor Velocidade
		BufferedWriter arqVv = new BufferedWriter( new FileWriter(new File("/home/douglas/dados/Velocidade.txt")) );
		for(int i=0; i<161; i++){
			
			//i = id tempo do vetor velocidade
			aux = arq.readLine();
			//System.out.println(aux);
			aux = arq.readLine();
			//System.out.println(aux);
			
			for(int j=0; j<nrPontos; j++){
				
				//j id ponto
				float x,y,z;
				Point vv = new Point();
				
				//le ponto
				aux = arq.readLine();
				
//				if(i==160)
//					System.out.println(aux);
				
				x = Float.parseFloat( aux.substring(0,16).trim() );
				y = Float.parseFloat( aux.substring(18, 32).trim() );
				z = Float.parseFloat( aux.substring(34).trim() );
				
				vv.x = x;
				vv.y = y;
				vv.z = z;
				
				arqVv.write(formatar(i, 3) + " " + formatar(j, 5) + " " + vv.toString());
				arqVv.write("\n");
				

			}
			
			//System.out.println("Acabou");
		}
		arqVv.close();
		
	}
	
	
	public static String formatar(int nr, int tam){
	
		String strNum= "" + nr;
		
		while(strNum.length()<tam)
			strNum = "0" + strNum;
		
		return strNum;
	}

}
