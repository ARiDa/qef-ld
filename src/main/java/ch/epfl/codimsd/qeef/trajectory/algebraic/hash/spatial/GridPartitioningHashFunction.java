package ch.epfl.codimsd.qeef.trajectory.algebraic.hash.spatial;

import java.util.Collection;
import java.util.Vector;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.HashFunction;
import ch.epfl.codimsd.qeef.types.Point;

public class GridPartitioningHashFunction extends HashFunction {
	
	private int nrBuckets; 
	
	private int columnNumber;
	private Point metacels[];	
	
	private BucketExtent extents[];
	
	public GridPartitioningHashFunction(int nrBuckets, int columnNumber, String limits){
		super();

		this.columnNumber = columnNumber;
		this.nrBuckets = nrBuckets;		
		
		float xMin, yMin, zMin, xMax, yMax, zMax;
		String pontos[];
		String metacelLimits[];
		
		metacelLimits = limits.split("#");		
		extents = new BucketExtent[nrBuckets];
		
		for( int i=0; i < nrBuckets; i++ ){
			pontos = metacelLimits[i].split(" ");
			xMin = Float.parseFloat(pontos[0]);
			yMin = Float.parseFloat(pontos[1]);
			zMin = Float.parseFloat(pontos[2]);
			xMax = Float.parseFloat(pontos[3]);
			yMax = Float.parseFloat(pontos[4]);
			zMax = Float.parseFloat(pontos[5]);
			
			extents[i] = new BucketExtent(xMin, yMin, zMin, xMax, yMax, zMax);
		}
	}
	
	
	//Implementa a fun��o de Hash
	//Dado uma tupla indica em qual bucket esta tupla deve estar
	public int[] assign(DataUnit instance) throws Exception{
		
		Object column;
		int aux;
		Vector result = new Vector(nrBuckets);
		Tuple tuple  = (Tuple)instance ;
				
		column = tuple.getData(columnNumber);
		
		//Procura por lugar em que ponto ou conjunto de pontos se encaixam
		//Se de um conjunto de pontos um ficar fora haver� replica��o em varios buckets do conj. de pontos
		for (int i = 0; i < extents.length; i++) {
			
			BucketExtent currExtent = extents[i];
			if( column instanceof Collection)
				aux = currExtent.isInside((Collection)column);
			else
				aux = currExtent.isInside((Point)column);
			
			if( aux == 1 ){
				result.add(new Integer(i));
				i = extents.length;
			} else if ( aux == 0 ){
				result.add(new Integer(i));
			}
		}

//		System.out.println(result);
		//Prepara retorno
		int iResult[] = new int[result.size()];
		
		for (int i=0; i<result.size(); i++) {
			iResult[i] = ((Integer)result.get(i)).intValue();
		}
		
		return iResult;
	}

//	
//	public static void main(String[] args) throws Exception{
//        
//	    String limits = "-0.409683 -1.044562 -0.932400 0.041711 -0.211005 1.407617#-0.398143 -1.551585 01.307436 0.047250 -0.211005 2.927826#-0.378613 -0.292320 -0.932400 0.042523 00.453248 0.255864#-0.390677 -0.300942 00.173429 0.052477 01.128682 2.100821#-0.033205 -1.044562 -0.932400 0.410329 -0.191613 1.351209#-0.038234 -1.551585 01.267152 0.401973 -0.198412 2.927826#-0.029383 -0.281856 -0.932400 0.378720 00.453248 0.251265#-0.038500 -0.285698 00.194394 0.387524 01.128682 2.100821";
//	    String inner = "Tetraedro.vertices";
//	    String outer = "Particula.ponto";
//	    
//	    GridPartitioningHashFunction s = new GridPartitioningHashFunction(0,1, limits);
//	    
//	    s.init(8);
//	    
//	    Tuple t = new Tuple();
//	    t.addData(new Point(00002, (float)-00.0148811, (float)000.0128352, (float)-00.7027797) );
//	    int x[] = s.assign(t, 0);
//	    
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0157395 , (float)000.0116698 , (float)-00.5532234) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0165463 , (float)000.0103636 , (float)-00.4019581) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0173205 , (float)000.0087826 , (float)-00.2495278) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0180677 , (float)000.0067832 , (float)-00.0968539) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0187370 , (float)000.0043454 , (float)000.0550349) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0192649 , (float)000.0015255 , (float)000.2053757) );
//	      x = s.assign(t, 0);
//
//	    t = new Tuple();
//	    t.addData(new Point(00002 , (float)-00.0195945 , (float)-00.0016032 , (float)000.3533714) );
//	      x = s.assign(t, 0);
//
////	    t = new Tuple();
////	    t.addData(new Point );
////	     x = s.assign(t, 0);
////
////	    t = new Tuple();
////	    t.addData(new Point );
////	     x = s.assign(t, 0);
//
//
//    
//    }
}
