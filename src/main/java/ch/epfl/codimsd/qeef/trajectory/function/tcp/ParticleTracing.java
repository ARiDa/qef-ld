package ch.epfl.codimsd.qeef.trajectory.function.tcp;

import ch.epfl.codimsd.qeef.trajectory.function.tcp.structure.*;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

public class ParticleTracing {

    private final int TIMES = 150;

    private final int MAXMETAPOINS = 2350;

    private final int TOTALPOINTS = 14459;

    /* Globals */

    private float dist; // Global maximum distance between 2 points

    private point part = new point(); // Calculated Particle

    private int partNumber; // Particle Number to que used in the file name

    private int error = 0; // error code

    private int currentMetacell = 0;// Current Metacell in memory

    private int currentTime = 0; // Current Time of calculation

    private int startTime; // Time to start the simulation

    private float a1, a2, a3, a4; // Global alphas to enable interpolation

    private String dataDir = "/srv/dadosTCP/";

    private int metaCelAnterior;

    // Variaveis do
    // findCloserCells***********************************************************
    /*
     * private int numPoints, numCells, i; private PointsAndCells
     * auxPointsAndCells= new PointsAndCells(); int candVec []; // Candidate
     * Cells Vector point pointList[]; cell cellList[]; // Cell List
     */
    //	Variaveis do
    // findCloserCells**********************************************************
    //private float localPosition[];
    /* Global Lists */

    //	 Global List that loads all times for one metacell (not indexed)
    private point[] vecList = new point[TIMES * MAXMETAPOINS];

    //	 Global List that loads one time indexed (for search purposes)
    private point[] vecListSearch = new point[TOTALPOINTS];

    private int localPosition[] = new int[9];

    private int metaVec[] = new int[9];

    /* Prototypes */
    /*
     * 
     * int checkMetacellIntervals(float xmin, float xmax, float ymin, float
     * ymax, float zmin, float zmax); void printfCandidates(point p, int *
     * metaVec); void setFileName(char *nome, int n, int op); void
     * getNumberOfPointsAndCells(int metaNumber, int *numPoints, int *numCells);
     * void setMaximumDistance(); int checkMinimumDistance(float x, float
     * y,float z); int getCloserCells(int* candVec, point* pointList, cell c);
     * void setPoints(point* p0, point* p1, point* p2, point* p3, point* p4,
     * point* plist, cell c); void computeVectorOperationSub(point *v, point pa,
     * point pb); void setVector(point* v1, point* v2, point* v3, point* v4,
     * point p0, point p1, point p2, point p3, point p4); void
     * computeCrossProd(point *v, point va, point vb); void setNValues(point*
     * n1, point* n2, point* n3, point v2, point v3, point v4); float
     * computeScalarProd(point va, point vb); void setAlphaValues(float *alpha,
     * point n, point va, point vb); int isInsideCell(point* pointList, cell c);
     * void loadVelocityVector(int metaNumber, int numPoints); void
     * writeTimeInFile(int startTime); void computeVelocity(cell c, int
     * metaNumber, int numPoints); void findCloserCells(int metaNumber); void
     * computeTrace(int *metaVec); void callMultiPart(int op);
     */

    /** **** Functions ***** */

    //	 Write the starting time on the particle file
    private void writeTimeInFile(int startTime) {
        //FILE *fpar;
        //char particleName[50];
        String particleName = new String();
        particleName = setFileName(particleName, partNumber, 2);
        File arqRemovido = new File(particleName);
        arqRemovido.delete();
        //remove(particleName);
        //fpar = fopen(particleName, "w");
        FileOutputStream out;
        PrintStream p;
        try {
            // Cria um novo file output stream
            // conecta ao arquivo chamado "arq.txt"

            out = new FileOutputStream(new String(particleName));

            // conecta o print stream ao output stream

            p = new PrintStream(out);

            // Writing Time
            p.println("TIME " + startTime);
            //Writing First Position
            p.print(part.x);
            p.print(" ");
            p.print(" " + part.y);
            p.print(" ");
            p.println(" " + part.z);

            p.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //fprintf(fpar, "TIME %d\n", startTime); // Writing Time
        //fprintf(fpar, "%f %f %f\n", part.x, part.y, part.z); // Writing First
        // Position

        //fclose(fpar);
    }

    //	Check if a metacell is inside or outside an interval
    private int checkMetacellIntervals(float xmin, float xmax, float ymin,
            float ymax, float zmin, float zmax) {
        point p = part;
        if ((p.x >= xmin && p.x <= xmax) && (p.y >= ymin && p.y <= ymax)
                && (p.z >= zmin && p.z <= zmax)) {
            return 1; // Particle is inside
        } else {
            return 0; // Particle is outside
        }
    }

    //	 Print the Metacells Candidates
    private void printfCandidates(point p, int metaVec[]) {
        for (int i = 1; i <= 8; i++) {
            if ((metaVec[i] == 1)) {
            }
            //System.out.println(" i "+ i);
        }
    }

    //	 Set MetaCell File Name
    public String setFileName(String nome, int n, int op) {
        //nome.trim();
        //nome="TESTE";
        String num = new String();
        num = (Integer.toString(n));
        //sprintf(num, "%03d", n);?????????????????Estou com duvida sobre esse
        // comando.
        //strcpy(nome,dataDir);
        nome = nome + dataDir;
        //nome=dataDir.getDataDir();
        //nome.concat("metacell");"particle"
        //Faltou especificar o caminho para o arquivo,ja que essa fun��o
        // era responsabilidade do comando dataDir.getDataDir;
        //nome=nome+(new dataDir()).dataDir;
        if (op == 1) {
            nome = nome + ("metacell");
        }
        if (op == 2) {
            nome = nome + ("particle");
        }
        if (op == 3) {
            //strcat(nome, "_index.txt");
            nome = nome + "_index.txt";
            //nome.concat("_index.txt");
            return nome;
        }
        //strcat(nome, num);
        ///(new Integegr(n))
        if ((n >= 0) && (n < 10)) {
            nome = nome + "00" + num;
        }
        if ((n >= 9) && (n < 100)) {
            nome = nome + "0" + num;
        }
        if ((n >= 100)) {
            nome = nome + num;
        }

        //nome.concat(num);
        //strcat(nome, ".txt");
        nome = nome + ".txt";
        //nome.concat(".txt");
        return nome;
    }

    //	 Return the number of points and cells written in the _index.txt file
    private PointsAndCells getNumberOfPointsAndCells(int metaNumber,
            int numPoints, int numCells) {
        //char aux2[]=new char[50];
        String indexName = new String();
        indexName = setFileName(indexName, 0, 3);
        /*
         * FILE *f = fopen(indexName, "r");
         * 
         * char aux[50]; float faux;
         * 
         * for(int i = 1; i <= 8; i++) { //fprintf("\nMetaCellFile Number_Points
         * Number_Cells xmin xmax ymin ymax zmin zmax"); fscanf(f, "%s %d %d %f
         * %f %f %f %f %f", aux, numPoints, numCells, &faux, &faux, &faux,
         * &faux, &faux, &faux); if(i == metaNumber) break; }
         * 
         * fclose(f);
         */
        PointsAndCells auxPointsAndCells = new PointsAndCells();
        char aux[] = new char[50];
        char auxBranco;
        float faux;
        FileInputStream arq;
        DataInputStream in;

        try {
            // Abre um arquivo existente chamado "arq.txt"
            arq = new FileInputStream(indexName);
            // CONVERTE o arquivo inputstream em um
            // datainputstream
            in = new DataInputStream(arq);
            // continua lendo linhas enquanto exista alguma
            // para ler
            //while ( in.available() != 0 )
            for (int i = 1; i <= 8; i++) {
                // escreve a linha do arquivo na screen
                //System.out.println( in.readLine() );
                StringTokenizer tokens = new StringTokenizer(in.readLine());
                //while(tokens.hasMoreTokens()){

                //}

                aux = (tokens.nextToken()).toCharArray();
                //aux=(tokens.nextToken()).toCharArray();

                numPoints = Integer.parseInt(tokens.nextToken());

                numCells = Integer.parseInt(tokens.nextToken());

                auxPointsAndCells.points = numPoints;

                auxPointsAndCells.cells = numCells;

                if (i == metaNumber) {
                    break;
                }

            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return auxPointsAndCells;

    }

    //	 Set global variable "dist" for general purposes
    private void setMaximumDistance() {
        //char indexName[50];
        //char indexName1 []=new char[50];
        //String indexName=new String(indexName1);
        String indexName = new String();
        indexName = setFileName(indexName, 0, 3);
        //FILE *findex = fopen(indexName, "r");

        int i;
        float faux;
        char aux[] = new char[50];
        FileInputStream arq;
        DataInputStream in;
        String linha = new String();
        try {
            // Abre um arquivo existente chamado "arq.txt"
            arq = new FileInputStream(indexName);
            // CONVERTE o arquivo inputstream em um
            // datainputstream
            in = new DataInputStream(arq);
            // continua lendo linhas enquanto exista alguma
            // para ler
            for (i = 1; i <= 8; i++) {
                // escreve a linha do arquivo na screen
                linha = in.readLine();
            }
            linha = in.readLine();
            StringTokenizer tokens = new StringTokenizer(linha);
            aux = (tokens.nextToken()).toCharArray();
            dist = Float.parseFloat(tokens.nextToken());
            linha = in.readLine();
            StringTokenizer tokens3 = new StringTokenizer(linha);
            aux = (tokens3.nextToken()).toCharArray();

            for (i = 1; i <= 8; i++) {
                linha = in.readLine();
                StringTokenizer tokens1 = new StringTokenizer(linha);
                localPosition[i] = Integer.parseInt(tokens1.nextToken()); // Load
                                                                          // Global
                                                                          // Vector
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //for(i = 1; i <= 8; i++)
        //	fscanf(findex, "%s %s %s %f %f %f %f %f %f", aux, aux, aux, &faux,
        // &faux, &faux, &faux, &faux, &faux);

        //fscanf(findex, "%s %f", aux, &dist);
        //fscanf(findex, "%s", aux); // Reading VelocityVectorPositionLabel

        //for(i = 1; i <= 8; i++)
        //	fscanf(findex, "%d", &localPosition[i]); // Load Global Vector

        //fclose(findex);

        //		printfCandidates(p, metaVec);
    }

    //	 Compute Euclidean Distance between two points and check if
    //	 that value is lower than the minimum 'distance'
    private int checkMinimumDistance(float x, float y, float z) {
        float d, j;
        //d=((x - part.x)*(x - part.x))+((y - part.y)*(y - part.y))+((z -
        // part.z)*(z - part.z))-(dist*dist);
        d = (float) (Math.pow(x - part.x, 2) + Math.pow(y - part.y, 2)
                + Math.pow(z - part.z, 2) - Math.pow(dist, 2));

        //if(((new Float(d)).compareTo((new Float("0.0")))) <= 0){
        if (d <= 0) {
            return 1;
        } else {
            return 0;
        }
    }

    //	 Get Cells by the Euclidean Distance of the Particle coordinate
    private int getCloserCells(int candVec[], point pointList[], cell c) {
        float x, y, z;
        //int res[4]; // load the result for each vertex
        int res[] = new int[4];
        for (int i = 0; i <= 3; i++) {
            if ((pointList[c.v[i]]).pointId == 1) {
                x = pointList[c.v[i]].x;
                y = pointList[c.v[i]].y;
                z = pointList[c.v[i]].z;
                res[i] = checkMinimumDistance(x, y, z);
            } else {
                //printf("\n\nCheck getCloserCells Function!!!\n\n");
                //system("PAUSE");
                //exit(1);???????????????
                break;
            }
        }
        if ((res[0] == 1) && (res[1] == 1) && (res[2] == 1) && (res[3] == 1)) {
            return 1;
        } else {
            return 0;
        }
    }

    //	 Set Point Values
    private void setPoints(point p0, point p1, point p2, point p3, point p4,
            point plist[], cell c) {

        p0.pointId = part.pointId;
        p0.x = part.x;
        p0.y = part.y;
        p0.z = part.z;

        //p1 = plist[ c.v[0] ];
        p1.pointId = plist[c.v[0]].pointId;
        p1.x = plist[c.v[0]].x;
        p1.y = plist[c.v[0]].y;
        p1.z = plist[c.v[0]].z;

        //p2 = plist[ c.v[1] ];
        p2.pointId = plist[c.v[1]].pointId;
        p2.x = plist[c.v[1]].x;
        p2.y = plist[c.v[1]].y;
        p2.z = plist[c.v[1]].z;

        //p3 = plist[ c.v[2] ];
        p3.pointId = plist[c.v[2]].pointId;
        p3.x = plist[c.v[2]].x;
        p3.y = plist[c.v[2]].y;
        p3.z = plist[c.v[2]].z;

        //p4 = plist[ c.v[3] ];
        p4.pointId = plist[c.v[3]].pointId;
        p4.x = plist[c.v[3]].x;
        p4.y = plist[c.v[3]].y;
        p4.z = plist[c.v[3]].z;
    }

    //	 Compute subtration operation for points
    private void computeVectorOperationSub(point p, point pa, point pb) {
        p.x = pa.x - pb.x;
        p.y = pa.y - pb.y;
        p.z = pa.z - pb.z;
    }

    //	 Set Vector Values
    //Perguntar para alguem se a passagem dos parametros do metodo sao por
    // referencia ou valor?
    private void setVector(point v1, point v2, point v3, point v4, point p0,
            point p1, point p2, point p3, point p4) {
        computeVectorOperationSub(v1, p0, p1);
        computeVectorOperationSub(v2, p2, p1);
        computeVectorOperationSub(v3, p3, p1);
        computeVectorOperationSub(v4, p4, p1);
    }

    //	 Calculates Cross Product between two vector v = pa x pb
    private void computeCrossProd(point p, point va, point vb) {
        p.x = (va.y * vb.z) - (va.z * vb.y);
        p.y = (va.z * vb.x) - (va.x * vb.z);
        p.z = (va.x * vb.y) - (va.y * vb.x);

    }

    //	 Set the 'n' Values to compute inside/outside points

    private void setNValues(point n1, point n2, point n3, point v2, point v3,
            point v4) {
        computeCrossProd(n1, v2, v3);
        computeCrossProd(n2, v2, v4);
        computeCrossProd(n3, v3, v4);
    }

    //	 Calculates Scalar Product between two vector r = v1. v2
    private float computeScalarProd(point va, point vb) {
        return (float) ((va.x * vb.x) + (va.y * vb.y) + (va.z * vb.z));
    }

    //	 Set the alpha values to compute inside/outside points
    private float setAlphaValues(point n, point va, point vb) {
        float val1, val2;
        val1 = (float) (computeScalarProd(n, va));
        val2 = (float) (computeScalarProd(n, vb));

        if (val2 != 0.0)
            return ((float) (val1 / val2));
        else {
            //printf("\nCheck 'setAlphaValues' function!!!\n");
            //system("PAUSE");
            //???????????? O que faz o comando SYSTEM
            return (0);
        }
    }

    //	 Check if the particle is inside a given cell (tetraedro).
    private int isInsideCell(point pointList[], cell c) {
        point v1 = new point();
        point v2 = new point();
        point v3 = new point();
        point v4 = new point();
        point n1 = new point();
        point n2 = new point();
        point n3 = new point();
        point p0 = new point();
        point p1 = new point();
        point p2 = new point();
        point p3 = new point();
        point p4 = new point();

        setPoints(p0, p1, p2, p3, p4, pointList, c);
        //p0 =(point)( part.clone());
        //		p1 = (point)(pointList[ c.v[0] ]).clone();
        //		p2 = (point)(pointList[ c.v[1] ]).clone();
        //		p3 = (point)(pointList[ c.v[2] ]).clone();
        //		p4 = (point)(pointList[ c.v[3] ]).clone();
        setVector(v1, v2, v3, v4, p0, p1, p2, p3, p4);
        //		v1=computeVectorOperationSub(v1,p0, p1);
        //		v2=computeVectorOperationSub(v2,p2, p1);
        //		v3=computeVectorOperationSub(v3,p3, p1);
        //		v4=computeVectorOperationSub(v4,p4, p1);
        setNValues(n1, n2, n3, v2, v3, v4);
        //		n1=computeCrossProd(n1,v2, v3);
        //		n2=computeCrossProd(n2, v2, v4);
        //		n3=computeCrossProd(n3, v3, v4);

        a4 = setAlphaValues(n1, v1, v4);
        a3 = setAlphaValues(n2, v1, v3);
        a2 = setAlphaValues(n3, v1, v2);

        a1 = (1 - a2 - a3 - a4);

        if ((a1 >= 0 && a1 <= 1) && (a2 >= 0 && a2 <= 1)
                && (a3 >= 0 && a3 <= 1) && (a4 >= 0 && a4 <= 1)) {
            return 1; // Point defined by global variable 'part' is inside the
                      // cell 'c'
        } else {
            return 0; // Point is outside the cell 'c'
        }
    }

    //	 Load Global Velocity Vector
    private void loadVelocityVector(int metaNumber, int numPoints) {
        int t, i, id, fullId;
        char aux[] = new char[50];
        //String aux = new String(aux1);
        //char metaName1[]=new char[50];
        String metaName = new String();
        currentMetacell = metaNumber;

        metaName = setFileName(metaName, metaNumber, 1);
        FileReader arq;
        BufferedReader in;
        String linha = new String();
        try {
            // Abre um arquivo existente chamado "arq.txt"
            arq = new FileReader(metaName);
            // CONVERTE o arquivo inputstream em um
            // datainputstream
            in = new BufferedReader(arq);
            int auxId = (localPosition[metaNumber]);
            in.skip(auxId);
            //			Load the full List for the 'metaNumber' metacell
            for (t = 0; t < TIMES; t++) {
                String linha1 = new String();
                linha1 = in.readLine();
                linha1 = in.readLine();
                StringTokenizer tokens = new StringTokenizer(linha1);

                aux = (tokens.nextToken()).toCharArray();

                for (i = 0; i < numPoints; i++) {
                    StringTokenizer tokens1 = new StringTokenizer(in.readLine());
                    id = i + t * numPoints;
                    vecList[id] = new point();

                    vecList[id].pointId = Integer.parseInt(tokens1.nextToken());
                    vecList[id].x = Float.parseFloat(tokens1.nextToken());
                    vecList[id].y = Float.parseFloat(tokens1.nextToken());
                    vecList[id].z = Float.parseFloat(tokens1.nextToken());
                    //fscanf(fvec, "%d %f %f %f", vecList[id].pointId,
                    // vecList[id].x, vecList[id].y, vecList[id].z);
                }
            }
            //			Load the search list for the time needed
            for (i = 0; i < numPoints; i++) {
                fullId = i + numPoints * currentTime; // Point to First element
                                                      // of each time in the
                                                      // main vector
                id = vecList[fullId].pointId;
                vecListSearch[id] = vecList[fullId];
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //	 Procedures that computes the velocity of a particle inside a cell
    private Resultado computeVelocity(cell c, int metaNumber, int numPoints) {
        float delta = 0.005f;

        if (metaNumber != currentMetacell) {
            loadVelocityVector(metaNumber, numPoints);
        }

        Resultado res = new Resultado();

        res.v1 = vecListSearch[c.v[0]];
        res.v2 = vecListSearch[c.v[1]];
        res.v3 = vecListSearch[c.v[2]];
        res.v4 = vecListSearch[c.v[3]];

        res.a1 = a1;
        res.a2 = a2;
        res.a3 = a3;
        res.a4 = a4;

        return res;

        //			Computes the vector
        //			part.x = part.x + delta * (a1 * vecListSearch[ c.v[0] ].x + a2 *
        // vecListSearch[ c.v[1] ].x + a3 * vecListSearch[ c.v[2] ].x + a4 *
        // vecListSearch[ c.v[3] ].x);
        //			part.y = part.y + delta * (a1 * vecListSearch[ c.v[0] ].y + a2 *
        // vecListSearch[ c.v[1] ].y + a3 * vecListSearch[ c.v[2] ].y + a4 *
        // vecListSearch[ c.v[3] ].y);
        //			part.z = part.z + delta * (a1 * vecListSearch[ c.v[0] ].z + a2 *
        // vecListSearch[ c.v[1] ].z + a3 * vecListSearch[ c.v[2] ].z + a4 *
        // vecListSearch[ c.v[3] ].z);
        //			currentTime++;
        //			return part;
    }

    //	 Find Cells by the Euclidean Distance of the Particle coordinate
    private Resultado findCloserCells(int metaNumber) {
        //char metaName1[]=new char[50];
        String metaName = new String();
        //char aux[]=new char[50];
        String aux = new String();

        int numPoints, numCells, i;
        numPoints = 0;
        numCells = 0;
        PointsAndCells auxPointsAndCells = new PointsAndCells();
        i = 0;
        metaName = setFileName(metaName, metaNumber, 1);

        int close = 0, inside = 0;

        auxPointsAndCells = getNumberOfPointsAndCells(metaNumber, numPoints,
                numCells);
        numPoints = auxPointsAndCells.points;
        numCells = auxPointsAndCells.cells;

        int candVec[] = new int[numCells]; // Candidate Cells Vector
        point pointList[] = new point[(10 * numPoints)];// Point List// Multiply
                                                        // by 10 to allow direct
                                                        // seach
        cell cellList[] = new cell[numCells]; // Cell List

        //if(currentTime >= startTime) // Just print if its the right time
        //	System.out.println("\nReading "+ metaName+" in time ."+ currentTime);

        // Set all Values to NULL
        /*
         * for(i=0; i < numPoints; i++){ pointList[i].pointId =0 ; }
         */
        if (metaCelAnterior == 1) {
        }
        int id;
        FileInputStream arq;
        DataInputStream in;
	        try {
	            // Abre um arquivo existente chamado "arq.txt"
	            arq = new FileInputStream(metaName);
            // CONVERTE o arquivo inputstream em um
            // datainputstream
            in = new DataInputStream(arq);
            // continua lendo linhas enquanto exista alguma
            // para ler
            String linha = in.readLine();
            // linha=in.readLine();
            for (i = 0; i < numPoints; i++) {
                linha = in.readLine();
                StringTokenizer tokens = new StringTokenizer(linha);

                //aux=(tokens.nextToken()).toCharArray();
                id = Integer.parseInt(tokens.nextToken());
                (pointList[id]) = new point();
                (pointList[id]).pointId = 1;
                (pointList[id]).x = Float.parseFloat(tokens.nextToken());
                (pointList[id]).y = Float.parseFloat(tokens.nextToken());
                (pointList[id]).z = Float.parseFloat(tokens.nextToken());
            }
            /*
             * for (int k=0;k <50;k++){ aux[k]=in.readChar(); } for (int k=0;k
             * <50;k++){ aux[k]=in.readChar(); }
             */
            StringTokenizer tokens1 = new StringTokenizer(in.readLine());
            aux = (tokens1.nextToken());//.toCharArray();
            aux = (tokens1.nextToken());//.toCharArray();
            //if(currentTime >= startTime) // Just print if its the right time
            //System.out.println("\t\t\t[ OK ]");
            /* Reading cells */
            //printf("\nReading %d from %s...", numCells, metaName);
            String linhaAux = in.readLine();
            linhaAux = in.readLine();
            //linhaAux=in.readLine();
            for (i = 0; i < numCells; i++) {
                linhaAux = in.readLine();
                StringTokenizer tokens2 = new StringTokenizer(linhaAux);
                cellList[i] = new cell();
                cellList[i].cellId = (new Integer(i)).intValue();
                (cellList[i]).v[0] = 0;
                //String linha2=tokens2.nextToken();
                (cellList[i]).v[0] = (new Integer(tokens2.nextToken()))
                        .intValue();
                (cellList[i]).v[1] = (new Integer(tokens2.nextToken()))
                        .intValue();
                (cellList[i]).v[2] = (new Integer(tokens2.nextToken()))
                        .intValue();
                (cellList[i]).v[3] = (new Integer(tokens2.nextToken()))
                        .intValue();

            }
            /*
             * for (int k=0;k <50;k++){ aux[k]=in.readChar(); } for (int k=0;k
             * <50;k++){ aux[k]=in.readChar(); }
             */
            StringTokenizer tokens3 = new StringTokenizer(in.readLine());
            aux = (tokens3.nextToken());
            aux = (tokens3.nextToken());
            //printf("\t\t\t\t[ OK ]");
            //Finding an inside point
            for (i = 0; i < numCells; i++) {
                if (getCloserCells(candVec, pointList, (cell) (cellList[i])) == 1) {
                    close++;
                    if (isInsideCell(pointList, (cell) cellList[i]) == 1) {
                        if (currentTime >= startTime) {
                            inside++;
                            return (computeVelocity(cellList[i], metaNumber,
                                    numPoints));
                            //break; // If a cell was found, end the search
                        } else {
                            currentTime++;
                        }
                    }
                }
            }
            if (inside > 1) {
                //System.out.println("\nCheck findCloserCells\n!");

            }

            inside = close = 0;
            //printf("\n(temp) Close Cells %d. Inside: %d.", close, inside);

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (null);
    }

    //	 Start Computing trace by finding the possible Metacells that contains the
    // particle
    //	 The MetaVec vector set the candidates (1)
    private Resultado computeTrace(int metaVec[]) {

        point p = part;
        //char indexName1[]=new char[50];
        String indexName = new String();

        //System.out.println("\n\n\nComputing particle"+ partNumber+"
        // ["+p.x+","+p.y+","+p.z+"]");

        indexName = setFileName(indexName, 0, 3);
        int i, t;
        float xmin[], xmax[], ymin[], ymax[], zmin[], zmax[];
        xmin = new float[9];
        xmax = new float[9];
        ymin = new float[9];
        ymax = new float[9];
        zmin = new float[9];
        zmax = new float[9];
        char aux[] = new char[50];
        FileInputStream out;
        DataInputStream in;
        try {
            // Cria um novo file output stream
            // conecta ao arquivo chamado "arq.txt"

            out = new FileInputStream(indexName);
            // conecta o print stream ao output stream
            in = new DataInputStream(out);
            String linha = new String();
            for (i = 1; i <= 8; i++) {

                //fprintf("\nMetaCellFile Number_Points Number_Cells xmin xmax
                // ymin ymax zmin zmax");
                linha = in.readLine();
                StringTokenizer tokens = new StringTokenizer(linha);
                aux = (tokens.nextToken()).toCharArray();
                aux = (tokens.nextToken()).toCharArray();
                aux = (tokens.nextToken()).toCharArray();
                xmin[i] = Float.parseFloat(tokens.nextToken());
                xmax[i] = Float.parseFloat(tokens.nextToken());
                ymin[i] = Float.parseFloat(tokens.nextToken());
                ymax[i] = Float.parseFloat(tokens.nextToken());
                zmin[i] = Float.parseFloat(tokens.nextToken());
                zmax[i] = Float.parseFloat(tokens.nextToken());

                //	fscanf(findex, "%s %s %s %f %f %f %f %f %f", aux, aux, aux,
                // &xmin[i], &xmax[i], &ymin[i], &ymax[i], &zmin[i], &zmax[i]);
            }
            Resultado res;
            for (t = 0; t < TIMES; t++) {
                for (i = 1; i <= 8; i++) {
                    //fprintf("\nMetaCellFile Number_Points Number_Cells xmin
                    // xmax ymin ymax zmin zmax");
                    //checa se a particula esta dentro desta metacelula
                    metaVec[i] = checkMetacellIntervals(xmin[i], xmax[i],
                            ymin[i], ymax[i], zmin[i], zmax[i]);
                    if (metaVec[i] == 1) {
                        metaCelAnterior = i;
                        res = findCloserCells(i);
                        if (res != null) {
                            return res;
                        }
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (null);

    }

    private void callMultiPart(int op) {
        switch (op) {
        case 1:
            part.x = 0.0f;
            part.y = 0.3f;
            part.z = -0.85f;
            break;
        case 2:
            part.x = 0.3f;
            part.y = 0.0f;
            part.z = -0.85f;
            break;
        case 3:
            part.x = 0.0f;
            part.y = 0.0f;
            part.z = -0.85f;
            break;
        case 4:
            part.x = -.3f;
            part.y = 0.0f;
            part.z = -0.85f;
            break;
        case 5:
            part.x = 0.0f;
            part.y = -.3f;
            part.z = -0.85f;
            break;
        case 6:
            part.x = 0.2f;
            part.y = 0.2f;
            part.z = -0.85f;
            break;
        case 7:
            part.x = -.2f;
            part.y = 0.2f;
            part.z = -0.85f;
            break;
        case 8:
            part.x = 0.2f;
            part.y = -.2f;
            part.z = -0.85f;
            break;
        case 9:
            part.x = -.2f;
            part.y = -.2f;
            part.z = -0.85f;
            break;
        case 10:
            part.x = 0.0f;
            part.y = -.2f;
            part.z = -0.85f;
            break;
        }
    }

    /* main */
    public static void main(String[] args) {

        ParticleTracing pc = new ParticleTracing();
        //int metaVec[]=new int[9];
        int op;

        System.out.println("\nPARTICLE TRACING");
        System.out.println("\n================");
        pc.setMaximumDistance();
        do {
            System.out.println("\n1 - Set one specific particle");
            System.out
                    .println("\n2 - Set Multiple Particles on a specific start time");
            System.out.println("\n3 - Exit");
            System.out.println("\nOption: ");
            op = (new Integer((JOptionPane.showInputDialog(null, "\nOption: "))))
                    .intValue();
            //op=1;
            //scanf("%d",&op);
            if (op == 3) {
                break;
            } //exit(1);
            if (op == 1) {
                // Processing one particle
                System.out
                        .println("\n\nEnter x, y and z of the particle (separated by spaces): ");
                //scanf("%f %f %f", &part.x, &part.y, &part.z);

                pc.part.x = 0;
                pc.part.y = (float) 0.3;
                pc.part.z = (float) -0.85;
                //System.out.println("\n\nEnter file Number (p.ex. '5' for
                // 'particle005.txt'): ");
                //scanf("%d", &partNumber);
                //pc.partNumber=(new
                // Integer((JOptionPane.showInputDialog(null," Enter file Number
                // (p.ex. '5' for 'particle005.txt'): ")))).intValue();
                pc.partNumber = 1;
                //System.out.println("\n\nEnter starting TIME: ");
                //					scanf("%d", &startTime);
                //pc.startTime=(new Integer((JOptionPane.showInputDialog(null,"
                // Enter starting TIME: ")))).intValue();
                pc.startTime = 1;
                pc.writeTimeInFile(pc.startTime);
                System.out.println("\n Inicio do metodo computeTrace\n!");
                pc.computeTrace(pc.metaVec);
                System.out.println("\n Inicio do metodo computeTrace\n!");
            } else if (op == 2) {

                // Processing multiple Particles
                System.out
                        .println("\n\nBatch Processing (Multiple Particles).");
                System.out.println("\n\nWhich time do you want to start? ");
                //scanf("%d",&startTime);
                pc.startTime = (new Integer((JOptionPane.showInputDialog(null,
                        " Which time do you want to start? ")))).intValue();
                System.out.println("\n\nStart processing from time "
                        + (pc.startTime + 1) + " to " + (pc.startTime + 10));
                for (int i = 1; i <= 10; i++) {
                    pc.callMultiPart(i);
                    pc.currentMetacell = 0;
                    pc.currentTime = 0;
                    pc.partNumber = i + pc.startTime;
                    pc.writeTimeInFile(pc.startTime);
                    pc.computeTrace(pc.metaVec);
                }

            }
        } while (op < 1 || op > 3);
        System.out.println("\n Acabou o Processamento do algoritmo n!");
        //printf("\n");
    }

    public Resultado proximaPosicao(int id, float x, float y, float z, int time) {
        part.pointId = id;
        part.x = x;
        part.y = y;
        part.z = z;
        startTime = time;
        return (computeTrace(metaVec));
    }

    public ParticleTracing() {
        setMaximumDistance();
    }

}