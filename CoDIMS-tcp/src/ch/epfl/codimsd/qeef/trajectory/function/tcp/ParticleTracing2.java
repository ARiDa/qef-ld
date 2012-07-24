package ch.epfl.codimsd.qeef.trajectory.function.tcp;
import ch.epfl.codimsd.qeef.trajectory.function.tcp.structure.*;

import java.io.*;
import java.util.*;

public class ParticleTracing2 {
	
	
	private final int TIMES=150;
	private final int MAXMETAPOINS=2350;
	private final int TOTALpoint2S=14459;

	/* Globals */

	private double dist;				// Global maximum distance between 2 point2s
	private point2 part=new point2();				// Calculated Particle
	private int partNumber;			// Particle Number to que used in the file name
	private int error = 0;			// error code
	private int currentMetacell = 0;// Current Metacell in memory
	private int currentTime = 0;	// Current Time of calculation
	private int startTime;			// Time to start the simulation	
	private double a1, a2, a3, a4;	// Global alphas to enable interpolation
	private String dataDir="C:/CTP/Dados2/";
	private int id;
    //private  double localPosition[];

	/* Global Lists */

//	 Global List that loads all times for one metacell (not indexed)
	private point2[]	vecList = new point2[TIMES * MAXMETAPOINS];	

//	 Global List that loads one time indexed (for search purposes)
	private point2[]	vecListSearch = new point2[TOTALpoint2S];	

	private int localPosition []=new int[9]; 

	/* Prototypes */
	/*

	int checkMetacellIntervals(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax);
	void printfCandidates(point2 p, int * metaVec);
	void setFileName(char *nome, int n, int op);
	void getNumberOfpoint2sAndCells(int metaNumber, int *numpoint2s, int *numCells);
	void setMaximumDistance();
	int checkMinimumDistance(double x, double y,double z);
	int getCloserCells(int* candVec, point2* point2List, cell c);
	void setpoint2s(point2* p0, point2* p1, point2* p2, point2* p3, point2* p4, point2* plist, cell c);
	void computeVectorOperationSub(point2 *v, point2 pa, point2 pb);
	void setVector(point2* v1, point2* v2, point2* v3, point2* v4, point2 p0, point2 p1, point2 p2, point2 p3, point2 p4);
	void computeCrossProd(point2 *v, point2 va, point2 vb);
	void setNValues(point2* n1, point2* n2, point2* n3, point2 v2, point2 v3, point2 v4);
	double computeScalarProd(point2 va, point2 vb);
	void setAlphaValues(double *alpha, point2 n, point2 va, point2 vb);
	int isInsideCell(point2* point2List, cell c);
	void loadVelocityVector(int metaNumber, int numpoint2s);
	void writeTimeInFile(int startTime);
	void computeVelocity(cell c, int metaNumber, int numpoint2s);
	void findCloserCells(int metaNumber);
	void computeTrace(int *metaVec);
	void callMultiPart(int op);
*/


	



	/******  Functions  ******/


//	 Write the starting time on the particle file
	private void writeTimeInFile(int startTime)
	{
		//FILE *fpar;
		//char particleName[50];
		String particleName= new String();
		particleName=setFileName(particleName, partNumber, 2);
		File arqRemovido= new File(particleName);
		arqRemovido.delete();
		//remove(particleName);	
		//fpar = fopen(particleName, "w");
		FileOutputStream out; 
		PrintStream p;           
		try
		{
			// Cria um novo file output stream 
			// conecta ao arquivo chamado "arq.txt"
						
			out = new FileOutputStream (new String(particleName));
		
			// conecta o print stream ao output stream
			
			p   = new  PrintStream ( out );
			
			
			// Writing Time
			p.println("TIME "+startTime);
			//Writing First Position
			p.print(part.x);
			p.print(" ");
			p.print(" "+part.y);
			p.print(" ");
			p.println(" "+part.z);
			
			p.close();
		}
		catch (Exception e )
		{
			e.printStackTrace();
		}


		//fprintf(fpar, "TIME %d\n", startTime); // Writing Time
		//fprintf(fpar, "%f %f %f\n", part.x, part.y, part.z); // Writing First Position

		//fclose(fpar);
	}


//	Check if a metacell is inside or outside an interval
	private int checkMetacellIntervals(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax)
	{
		point2 p = part;
		if((p.x >= xmin && p.x <= xmax) && (p.y >= ymin && p.y <= ymax) && (p.z >= zmin && p.z <= zmax)){
			return 1; // Particle is inside
		}
		else {
			return 0; // Particle is outside
		}
	}


//	 Print the Metacells Candidates
	private void printfCandidates(point2 p, int  metaVec [])
	{
		System.out.println("\nParticula pode estar nas seguintes Metacelulas: ");
		for(int i = 1; i <= 8; i++)
		{
			if((metaVec[i]==1))
				System.out.println(" i "+ i);
		}
	}


//	 Set MetaCell File Name
	private String setFileName(String nome, int n, int op)
	{
		//nome.trim();
		//nome="TESTE";
		String  num=new String(); 
		num=(Integer.toString(n));
		//sprintf(num, "%03d", n);?????????????????Estou com duvida sobre esse comando.
		//strcpy(nome,dataDir);
		nome=nome+dataDir;  
		//nome=dataDir.getDataDir();
		//nome.concat("metacell");"particle"
		//Faltou especificar o caminho para o arquivo,ja que essa fun��o era responsabilidade do comando dataDir.getDataDir;
		//nome=nome+(new dataDir()).dataDir;
		if(op == 1){ 
			nome=nome+("metacell");
		}
		if(op == 2){
			nome=nome+("particle");
		}
		if(op == 3) 
		{
			//strcat(nome, "_index.txt");
			nome=nome+"_index.txt";
			//nome.concat("_index.txt");
			return nome;
		}
		//strcat(nome, num);
		///(new Integegr(n))
		if ((n>=0)&&(n<10)){
			nome=nome+"00"+num;
		}
		if ((n>=9)&&(n<100)){
			nome=nome+"0"+num;
				}
		if ((n>=100)){
			nome=nome+num;
		}
		
		//nome.concat(num);
		//strcat(nome, ".txt");
		nome=nome+".txt";
		//nome.concat(".txt");
		return nome;
	}


//	 Return the number of point2s and cells written in the _index.txt file
	private PointsAndCells getNumberOfpointsAndCells(int metaNumber, int numpoint2s, int numCells)
	{
		//char aux2[]=new char[50];
		String indexName=new String();
		indexName=setFileName(indexName, 0, 3);
		/*
		FILE *f = fopen(indexName, "r");

		char aux[50];
		double faux;

		for(int i = 1; i <= 8; i++)
		{
			//fprintf("\nMetaCellFile	Number_point2s	Number_Cells	xmin		xmax		ymin		ymax		zmin		zmax");
			fscanf(f, "%s %d %d %f %f %f %f %f %f", aux, numpoint2s, numCells, &faux, &faux, &faux, &faux, &faux, &faux);
			if(i == metaNumber) break;
		}

		fclose(f);
		*/
		PointsAndCells auxpointsAndCells=new PointsAndCells();
		char aux[]=new char[50];
		char auxBranco;
		double faux;
		FileInputStream arq; 
		DataInputStream in;
		
		try
		{
		  // Abre um arquivo existente chamado "arq.txt"
			 arq  = new FileInputStream (indexName);
  		  // CONVERTE o arquivo inputstream em um 
		  // datainputstream
			in   = new DataInputStream ( arq );
			// continua lendo linhas enquanto exista alguma
			// para ler
			//while ( in.available() != 0 )
			for(int i = 1; i <= 8; i++){
				// escreve a linha do arquivo na screen
				//System.out.println( in.readLine() );
				StringTokenizer tokens=new StringTokenizer(in.readLine());
				//while(tokens.hasMoreTokens()){
				
				//}
				
				aux=(tokens.nextToken()).toCharArray();
				//aux=(tokens.nextToken()).toCharArray();
				
				numpoint2s=Integer.parseInt(tokens.nextToken());
				
				numCells=Integer.parseInt(tokens.nextToken());
				
				auxpointsAndCells.points=numpoint2s;
				
				auxpointsAndCells.cells=numCells;
				
				
				if(i == metaNumber){
					break;
				}
				
			}
			in.close();
			
			
		}
		catch (IOException e )
		{
			e.printStackTrace();
		}
		return auxpointsAndCells;

	}


//	 Set global variable "dist" for general purposes
	private void setMaximumDistance()
	{
		//char indexName[50];
		//char indexName1 []=new char[50];
		//String indexName=new String(indexName1);
		String indexName=new String();
		indexName=setFileName(indexName, 0, 3);
		//FILE *findex = fopen(indexName, "r");

		int i;
		double faux;
		char aux []=new char[50];
		FileInputStream arq; 
		DataInputStream in;
		String linha=new String();
		try
		{
			// Abre um arquivo existente chamado "arq.txt"
			arq  = new FileInputStream (indexName);
			// CONVERTE o arquivo inputstream em um 
			// datainputstream
			in   = new DataInputStream ( arq );
			// continua lendo linhas enquanto exista alguma
			// para ler
			for(i = 1; i <= 8; i++){
				// escreve a linha do arquivo na screen
   	 			linha= in.readLine();
			}
			linha=in.readLine();
			StringTokenizer tokens=new StringTokenizer(linha);
			aux=(tokens.nextToken()).toCharArray();
			dist = Double.parseDouble( tokens.nextToken() );
			linha=in.readLine();
			StringTokenizer tokens3=new StringTokenizer(linha);
			aux=(tokens3.nextToken()).toCharArray();
			
			
			for(i = 1; i <= 8; i++){
				linha=in.readLine();
				StringTokenizer tokens1=new StringTokenizer(linha);
				localPosition[i]=Integer.parseInt(tokens1.nextToken()); // Load Global Vector
			}			
			in.close();
		}
		catch (IOException e )
		{
			e.printStackTrace();
		}

		//for(i = 1; i <= 8; i++)
		//	fscanf(findex, "%s %s %s %f %f %f %f %f %f", aux, aux, aux, &faux, &faux, &faux, &faux, &faux, &faux);

		//fscanf(findex, "%s %f", aux, &dist);
		//fscanf(findex, "%s", aux); // Reading VelocityVectorPositionLabel

		//for(i = 1; i <= 8; i++)
		//	fscanf(findex, "%d", &localPosition[i]); // Load Global Vector

		//fclose(findex);

//		printfCandidates(p, metaVec);
	}


//	 Compute Euclidean Distance between two point2s and check if
//	 that value is lower than the minimum 'distance'
	private int checkMinimumDistance(double x, double y,double z)
	{
		double d,j;
		//d=((x - part.x)*(x - part.x))+((y - part.y)*(y - part.y))+((z - part.z)*(z - part.z))-(dist*dist);
		d = (double)( Math.pow(x - part.x,2) + Math.pow(y - part.y,2) + Math.pow(z - part.z,2) - Math.pow(dist,2));
		
		//if(((new double(d)).compareTo((new double("0.0")))) <= 0){
		if(d <= 0){
		    return 1;
	    }
		else{
			return 0;
		}
	}


//	 Get Cells by the Euclidean Distance of the Particle coordinate
	private int getCloserCells(int candVec[],point2 point2List[], cell c)
	{
		double x, y, z;
		//int res[4];		// load the result for each vertex
		int res[]=new int[4];
		for(int i = 0; i <=3; i++)
		{
			if((point2List[ c.v[i] ]).pointId==1){
				x = point2List[ c.v[i] ].x;
				y = point2List[ c.v[i] ].y;
				z = point2List[ c.v[i] ].z;
				res[i] = checkMinimumDistance(x,y,z);
			}
			else{
				//printf("\n\nCheck getCloserCells Function!!!\n\n"); 
				//system("PAUSE");
				//exit(1);???????????????
				break;
			}	
		}
		if( (res[0]==1) && (res[1]==1) && (res[2]==1) && (res[3]==1)){
			return 1;
		}
		else{
			return 0;
		}
	}

//	 Set point2 Values
	private void setpoints(point2 p0, point2 p1, point2 p2, point2 p3, point2 p4, point2 plist[], cell c)
	{
		
		p0.pointId  = part.pointId;
		p0.x = part.x;
		p0.y = part.y;
		p0.z = part.z;

		//p1 = plist[ c.v[0] ];
		p1.pointId  = plist[ c.v[0] ].pointId;
		p1.x = plist[ c.v[0] ].x;
		p1.y = plist[ c.v[0] ].y;
		p1.z = plist[ c.v[0] ].z;
				
		//p2 = plist[ c.v[1] ];
		p2.pointId  = plist[ c.v[1] ].pointId;
		p2.x = plist[ c.v[1] ].x;
		p2.y = plist[ c.v[1] ].y;
		p2.z = plist[ c.v[1] ].z;


		//p3 = plist[ c.v[2] ];
		p3.pointId  = plist[ c.v[2] ].pointId;
		p3.x = plist[ c.v[2] ].x;
		p3.y = plist[ c.v[2] ].y;
		p3.z = plist[ c.v[2] ].z;

		//p4 = plist[ c.v[3] ];
		p4.pointId  = plist[ c.v[3] ].pointId;
		p4.x = plist[ c.v[3] ].x;
		p4.y = plist[ c.v[3] ].y;
		p4.z = plist[ c.v[3] ].z;

	}


//	 Compute subtration operation for point2s
	private void computeVectorOperationSub(point2 p, point2 pa, point2 pb)
	{
		p.x = pa.x -pb.x;
		p.y = pa.y - pb.y;
		p.z = pa.z - pb.z;
	}


//	 Set Vector Values
	//Perguntar para alguem se a passagem dos parametros do metodo sao por referencia ou valor?
	private void setVector(point2 v1, point2 v2, point2 v3, point2 v4, point2 p0, point2 p1, point2 p2, point2 p3, point2 p4)
	{
		computeVectorOperationSub(v1,p0, p1);
		computeVectorOperationSub(v2,p2, p1);
		computeVectorOperationSub(v3,p3, p1);
		computeVectorOperationSub(v4,p4, p1);
	}


//	 Calculates Cross Product between two vector v = pa x pb
	private void computeCrossProd(point2 p,point2 va,point2 vb)
	{
		p.x = (va.y * vb.z) - (va.z * vb.y);
		p.y = (va.z * vb.x) - (va.x * vb.z);
		p.z = (va.x * vb.y) - (va.y * vb.x);

	}


//	 Set the 'n' Values to compute inside/outside point2s
	
	private void setNValues(point2 n1, point2 n2, point2 n3, point2 v2, point2 v3, point2 v4)
	{
		computeCrossProd(n1, v2, v3);
		computeCrossProd(n2, v2, v4);
		computeCrossProd(n3, v3, v4);
	}
	


//	 Calculates Scalar Product between two vector r = v1. v2
	private double computeScalarProd(point2 va, point2 vb)
	{
		return (double)((va.x * vb.x) + (va.y * vb.y) + (va.z * vb.z)); 
	}


//	 Set the alpha values to compute inside/outside point2s
	private double setAlphaValues(point2 n, point2 va, point2 vb)
	{
		double val1, val2;
		val1 = (double)(computeScalarProd(n, va)); 
		val2 = (double)(computeScalarProd(n, vb));
		
		if(val2!=0.0)
			return( (double)(val1 / val2));
		else{
			//printf("\nCheck 'setAlphaValues' function!!!\n");
			//system("PAUSE");
			//???????????? O que faz o comando SYSTEM
			return (0);
		}

	}

//	 Check if the particle is inside a given cell (tetraedro).
	private int isInsideCell(point2 point2List[], cell c)
	{
		point2 v1=new point2();
		point2 v2=new point2();
		point2 v3=new point2();
		point2 v4=new point2();
		point2 n1=new point2();
		point2 n2=new point2();
		point2 n3=new point2();
		point2 p0=new point2();
		point2 p1=new point2();
		point2 p2=new point2();
		point2 p3=new point2();
		point2 p4=new point2();

		setpoints(p0, p1, p2, p3, p4, point2List, c);
		//p0 =(point2)( part.clone());
//		p1 = (point2)(point2List[ c.v[0] ]).clone();
//		p2 = (point2)(point2List[ c.v[1] ]).clone();
//		p3 = (point2)(point2List[ c.v[2] ]).clone();
//		p4 = (point2)(point2List[ c.v[3] ]).clone();
		setVector(v1, v2, v3, v4, p0, p1, p2, p3, p4);
//		v1=computeVectorOperationSub(v1,p0, p1);
//		v2=computeVectorOperationSub(v2,p2, p1);
//		v3=computeVectorOperationSub(v3,p3, p1);
//		v4=computeVectorOperationSub(v4,p4, p1);
		setNValues(n1, n2, n3, v2, v3, v4);
//		n1=computeCrossProd(n1,v2, v3);
//		n2=computeCrossProd(n2, v2, v4);
//		n3=computeCrossProd(n3, v3, v4);

		a4=setAlphaValues( n1, v1, v4);
		a3=setAlphaValues( n2, v1, v3);
		a2=setAlphaValues( n3, v1, v2);
		
		a1 =( 1 - a2 - a3 - a4);
		
		if((a1>=0  && a1<=1) && (a2>=0  && a2<=1) && (a3>=0  && a3<=1) && (a4>=0  && a4<=1)){
			return 1; // point2 defined by global variable 'part' is inside the cell 'c'
		}
		else{
			return 0; // point2 is outside the cell 'c'
		}
	}


//	 Load Global Velocity Vector
	private void loadVelocityVector(int metaNumber, int numpoint2s)
	{
		int t,i, id, fullId;
		char aux []=new char[50];
		//String aux = new String(aux1);
		//char metaName1[]=new char[50];
        String metaName= new String(); 
		currentMetacell = metaNumber;

		metaName=setFileName(metaName, metaNumber, 1);
		FileReader arq; 
		BufferedReader in;
		String linha=new String();
		try
		{
			// Abre um arquivo existente chamado "arq.txt"
			arq  = new FileReader(metaName);
			// CONVERTE o arquivo inputstream em um 
			// datainputstream
			in   = new BufferedReader( arq );
			int auxId=(localPosition[metaNumber]);
			in.skip(auxId);
//			Load the full List for the 'metaNumber' metacell
			for(t = 0; t < TIMES; t++){
				String linha1=new String();
				linha1=in.readLine();
				linha1=in.readLine();
				StringTokenizer tokens=new StringTokenizer(linha1);
				
				aux=(tokens.nextToken()).toCharArray();
							
				
				for(i = 0; i < numpoint2s; i++)
				{
					StringTokenizer tokens1=new StringTokenizer(in.readLine());
					id = i + t * numpoint2s;
					vecList[id]=new point2();
					
					vecList[id].pointId=Integer.parseInt(tokens1.nextToken());
					vecList[id].x=Double.parseDouble(tokens1.nextToken());
					vecList[id].y=Double.parseDouble(tokens1.nextToken());
					vecList[id].z=Double.parseDouble(tokens1.nextToken());
					//fscanf(fvec, "%d %f %f %f", vecList[id].point2Id, vecList[id].x, vecList[id].y, vecList[id].z); 
				}
			}
//			Load the search list for the time needed
			for(i = 0; i < numpoint2s; i++)
			{	
				 fullId = i + numpoint2s * currentTime; // point2 to First element of each time in the main vector
				 id = vecList[fullId].pointId;
				 vecListSearch[ id ] = vecList[fullId];
			}
			in.close();
		}
		catch (IOException e )
		{
			e.printStackTrace();
		}
			
	}


//	 Procedures that computes the velocity of a particle inside a cell
	private void computeVelocity(cell c, int metaNumber, int numpoints)
	{
		float delta = 0.005f;
		//char particleName1[]=new char[50];
		String particleName=new String();
	
		if(metaNumber != currentMetacell){
			loadVelocityVector(metaNumber, numpoints);
		}
		particleName=setFileName(particleName, partNumber, 2);

		// New
		FileOutputStream out; 
		PrintStream p;           
		try	{
			// Cria um novo file output stream 
			// conecta ao arquivo chamado "arq.txt"
			out = new FileOutputStream ( particleName,true);
			// conecta o print stream ao output stream
			p   = new  PrintStream ( out );
//			Computes the vector
			part.x = part.x + delta * (a1 * vecListSearch[ c.v[0] ].x + a2 * vecListSearch[ c.v[1] ].x + a3 * vecListSearch[ c.v[2] ].x + a4 * vecListSearch[ c.v[3] ].x); 
			part.y = part.y + delta * (a1 * vecListSearch[ c.v[0] ].y + a2 * vecListSearch[ c.v[1] ].y + a3 * vecListSearch[ c.v[2] ].y + a4 * vecListSearch[ c.v[3] ].y); 
			part.z = part.z + delta * (a1 * vecListSearch[ c.v[0] ].z + a2 * vecListSearch[ c.v[1] ].z + a3 * vecListSearch[ c.v[2] ].z + a4 * vecListSearch[ c.v[3] ].z); 

			currentTime++;
			p.print (part.x);
			p.print("  ");
			p.print(part.y);
			p.print("  ");
			p.println(part.z);
			p.close();
		}catch (Exception e ){
			e.printStackTrace();
		}
			
	}


//	 Find Cells by the Euclidean Distance of the Particle coordinate
	private void findCloserCells(int metaNumber)
	{
		//char metaName1[]=new char[50];
		String metaName=new String();
		//char aux[]=new char[50];
		String aux=new String();
		int numpoint2s, numCells, i;
		numpoint2s=0;
		numCells=0;
		PointsAndCells auxpointsAndCells= new PointsAndCells();
		i=0;
		metaName=setFileName(metaName,metaNumber, 1);

		int close = 0, inside = 0;

		auxpointsAndCells=getNumberOfpointsAndCells(metaNumber, numpoint2s, numCells);
		numpoint2s=auxpointsAndCells.points;
		numCells=auxpointsAndCells.cells;

		int		candVec []=new int[numCells];		// Candidate Cells Vector
		point2	point2List[]=new point2[(10 * numpoint2s)];// point2 List// Multiply by 10 to allow direct seach
		cell	cellList[]=new cell[numCells];		// Cell List	

		if(currentTime >= startTime) // Just print if its the right time
			System.out.println("\nReading "+ metaName+" in time ."+ currentTime);

			
		// Set all Values to NULL
		/*
		for(i=0; i < numpoint2s; i++){ 
			point2List[i].point2Id =0 ;
		}*/
		int id;
		FileInputStream arq; 
		DataInputStream in;   
		try	{
			 // Abre um arquivo existente chamado "arq.txt"
			 arq  = new FileInputStream (metaName);
  		     // CONVERTE o arquivo inputstream em um 
			 // datainputstream
			 in   = new DataInputStream ( arq );
			 // continua lendo linhas enquanto exista alguma
			 // para ler
			String linha=in.readLine();
			// linha=in.readLine();
			for(i=0; i < numpoint2s; i++){
				linha=in.readLine();
				StringTokenizer tokens=new StringTokenizer(linha);
				
				//aux=(tokens.nextToken()).toCharArray();
				id=Integer.parseInt(tokens.nextToken());
				(point2List[ id ])=new point2();
				(point2List[ id ]).pointId = 1;
				(point2List[ id ]).x=Double.parseDouble(tokens.nextToken());
				(point2List[ id ]).y=Double.parseDouble(tokens.nextToken());
				(point2List[ id ]).z=Double.parseDouble(tokens.nextToken());
			}
			/*
			for (int k=0;k<50;k++){
				aux[k]=in.readChar();
			}
			for (int k=0;k<50;k++){
				aux[k]=in.readChar();
			}
			*/
			StringTokenizer tokens1 = new StringTokenizer(in.readLine());
			aux=(tokens1.nextToken());//.toCharArray();
			aux=(tokens1.nextToken());//.toCharArray();
			if(currentTime >= startTime) // Just print if its the right time
				System.out.println("\t\t\t[ OK ]");
			/* Reading cells */	
			//printf("\nReading %d from %s...", numCells, metaName);
			String linhaAux=in.readLine();
			linhaAux=in.readLine();
			//linhaAux=in.readLine();
			for(i=0; i < numCells; i++)
			{
				linhaAux=in.readLine();
				StringTokenizer tokens2=new StringTokenizer(linhaAux);
				cellList[i]=new cell();
				cellList[i].cellId = (new Integer(i)).intValue();
				(cellList[i]).v[0]=0;
				//String linha2=tokens2.nextToken();
				(cellList[i]).v[0]=(new Integer(tokens2.nextToken())).intValue();
				(cellList[i]).v[1]=(new Integer(tokens2.nextToken())).intValue();
				(cellList[i]).v[2]=(new Integer(tokens2.nextToken())).intValue();
				(cellList[i]).v[3]=(new Integer(tokens2.nextToken())).intValue();
			
			}
			/*
			for (int k=0;k<50;k++){
				aux[k]=in.readChar();
			}
			for (int k=0;k<50;k++){
				aux[k]=in.readChar();
			}
			*/
			StringTokenizer tokens3=new StringTokenizer(in.readLine());
			aux=(tokens3.nextToken());
			aux=(tokens3.nextToken());
			//printf("\t\t\t\t[ OK ]");
//			Finding an inside point2
			for(i=0; i < numCells; i++)
			{
				if(getCloserCells(candVec, point2List,(cell) (cellList[i]))==1)
				{
					 close++;
					 if(isInsideCell(point2List,(cell) cellList[i])==1)
					 {
						 if(currentTime >= startTime)
						 {
							 inside++;
							 computeVelocity(cellList[i], metaNumber, numpoint2s);
							 break; // If a cell was found, end the search
						 }
						 else{
							 currentTime++;
						 }
					 }
				 }
			 } 
			 if(inside>1)
			 {
				 System.out.println("\nCheck findCloserCells\n!");
			
			 }

				 inside = close = 0;
				 //printf("\n(temp) Close Cells  %d. Inside: %d.", close, inside);
    
			in.close();
		}
		catch (IOException e )
		{
			e.printStackTrace();
		}
		
	}

//	 Start Computing trace by finding the possible Metacells that contains the particle
//	 The MetaVec vector set the candidates (1)
	private void computeTrace(int metaVec [])
	{
		point2 p = part;
		//char indexName1[]=new char[50];
		String indexName=new String();

		System.out.println("\n\n\nComputing particle"+ partNumber+" ["+p.x+","+p.y+","+p.z+"]");

		indexName=setFileName(indexName, 0, 3);
		int i,t;
		double xmin[], xmax[], ymin[], ymax[], zmin[], zmax[];
		xmin=new double[9];
		xmax=new double[9];
		ymin=new double[9];
		ymax=new double[9];
		zmin=new double[9];
		zmax=new double[9];
		char aux[]=new char[50];
		FileInputStream out; 
		DataInputStream in;           
		try{
			// Cria um novo file output stream 
			// conecta ao arquivo chamado "arq.txt"
			
			out =  new FileInputStream (indexName);
			// conecta o print stream ao output stream
			in   = new  DataInputStream(  out );
			String linha=new String();
			for(i = 1; i <= 8; i++){
				
				//fprintf("\nMetaCellFile	Number_point2s	Number_Cells	xmin		xmax		ymin		ymax		zmin		zmax");
				linha=in.readLine();
				StringTokenizer tokens=new StringTokenizer(linha);
				aux=(tokens.nextToken()).toCharArray();
				aux=(tokens.nextToken()).toCharArray();
				aux=(tokens.nextToken()).toCharArray();
				xmin[i] = Double.parseDouble( tokens.nextToken());
				xmax[i] = Double.parseDouble(tokens.nextToken());
				ymin[i] = Double.parseDouble(tokens.nextToken());
				ymax[i] = Double.parseDouble(tokens.nextToken());
				zmin[i] = Double.parseDouble(tokens.nextToken());
				zmax[i] = Double.parseDouble(tokens.nextToken());							
				
				
				//	fscanf(findex, "%s %s %s %f %f %f %f %f %f", aux, aux, aux, &xmin[i], &xmax[i], &ymin[i], &ymax[i], &zmin[i], &zmax[i]);
			}
            for(t = 0; t < TIMES; t++){
				for(i = 1; i <= 8; i++)
				{
					//fprintf("\nMetaCellFile	Number_point2s	Number_Cells	xmin		xmax		ymin		ymax		zmin		zmax");
						metaVec[i] = checkMetacellIntervals(xmin[i], xmax[i], ymin[i], ymax[i], zmin[i], zmax[i]);
						if(metaVec[i]==1){ 
							findCloserCells(i);
						}
				}
            }
			in.close();
		}
		catch (Exception e )
		{
			e.printStackTrace();
		}

		

		
	}


	private void callMultiPart(int op)
	{
		switch(op)
		{
			case  1: part.x = 0.0f; part.y = 0.3f; part.z = -0.85f;	break;
			case  2: part.x = 0.3f; part.y = 0.0f; part.z = -0.85f;	break;
			case  3: part.x = 0.0f; part.y = 0.0f; part.z = -0.85f;	break;
			case  4: part.x = -.3f; part.y = 0.0f; part.z = -0.85f;	break;
			case  5: part.x = 0.0f; part.y = -.3f; part.z = -0.85f;	break;
			case  6: part.x = 0.2f; part.y = 0.2f; part.z = -0.85f;	break;
			case  7: part.x = -.2f; part.y = 0.2f; part.z = -0.85f;	break;
			case  8: part.x = 0.2f; part.y = -.2f; part.z = -0.85f;	break;
			case  9: part.x = -.2f; part.y = -.2f; part.z = -0.85f;	break;
			case 10: part.x = 0.0f; part.y = -.2f; part.z = -0.85f;	break;
		}
	}
	
}