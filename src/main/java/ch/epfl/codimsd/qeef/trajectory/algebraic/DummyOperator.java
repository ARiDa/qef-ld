package ch.epfl.codimsd.qeef.trajectory.algebraic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;

/**
 * 
 */
//Variacao das taxas
public class DummyOperator extends Operator{

	private int interval;
	private int currInterval;
	private int proc;
	private int rates[];
	
	private int loop;
	
    public DummyOperator(int id, int loop) throws Exception
    {

        super(id);
        
        this.loop = loop;
            
        //Configura questoes relacionadas a variacao das taxas
        int nrIntervals; 
        File home = (File)Config.getProperty("QEEF_HOME");
        BufferedReader in = new BufferedReader( new FileReader( home + File.separator + "randtaxa.txt" ) );

        nrIntervals= Integer.parseInt(in.readLine().trim());
        interval = Integer.parseInt(in.readLine().trim());        
        proc = 0;     
        currInterval = 0;
        rates = new int[nrIntervals];               
        
        for(int i=0; i < nrIntervals; i++){
        	
        	rates[i] = Integer.parseInt(in.readLine().trim());
        }
        
    }


    public DataUnit getNext(int idConsumer) throws Exception{

    	int wait, aux;
    	
        instance = super.getNext(idConsumer);
        
        proc++;
        if( proc > interval*(currInterval+1) )
        	currInterval++;
       
        if(currInterval >= rates.length)
        	currInterval = rates.length-1;
        
        for(int i=0; i < rates[currInterval]; i++){        	
        }
        
        return instance;
    }
    
    public void setMetadata(Metadata prdMetadata[]){
       
        //Obtem metadado
        this.metadata[0] = (Metadata)prdMetadata[0].clone();        
    }
    
    public static void main(String[] args) {
		long time=System.currentTimeMillis();
    	for (int i=0; i<1000000000; i++){}
    	time=System.currentTimeMillis()-time;
    	System.out.println(time);
	}
	
    
}


//public class DummyOperator extends Operator{
//
//	private Random rand;
//	
//	private int loop;
//	
//
//	
//    public DummyOperator(int id, BlackBoard blackBoard, int loop) {
//        super(id, blackBoard);
//        
//        this.loop = loop;
//        this.rand = new Random(System.currentTimeMillis());
//    }
//
//
//    public DataUnit getNext(int idConsumer) throws Exception{
//
//    	int wait, aux;
//    	
//        instance = super.getNext(idConsumer);
//
//        aux = rand.nextInt(2);
//        wait = (int)(( rand.nextFloat() + aux) * loop);
//        System.out.println("Coeficiente " + wait);
//        //wait = loop;
//        for(int i=0; i < wait; i++){        	
//        }
//        
//        return instance;
//    }
//    
//    public void setMetadata(Metadata prdMetadata[]){
//       
//        //Obtem metadado
//        this.metadata[0] = (Metadata)prdMetadata[0].clone();        
//    }
//    
//}