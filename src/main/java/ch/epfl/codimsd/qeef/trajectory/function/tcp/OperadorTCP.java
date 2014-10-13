package ch.epfl.codimsd.qeef.trajectory.function.tcp;

import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.IntegerType;
import ch.epfl.codimsd.qeef.types.PointListType;
import ch.epfl.codimsd.qeef.types.Point;
import java.util.Collection;
import javax.naming.spi.DirStateFactory.Result;

public class OperadorTCP extends Operator {

	private TCP tcp;
	private int point, vertexes, vector, iteration;
	
	
	public OperadorTCP(int opId, OpNode op) {
		super(opId);

		tcp = new TCP();
	}

	/*
	 *  (non-Javadoc)
	 * @see QEEF.Op#open()
	 */
	public void open() throws Exception{	   	    
	    super.open();

	    //define formato de metadado
	    point = metadata[0].getDataOrder("PARTICULA.PONTO");
	    vertexes = metadata[0].getDataOrder("TETRAEDRO.VERTICES");
	    vector = metadata[0].getDataOrder("VELOCIDADE.VETOR");
	    iteration = metadata[0].getDataOrder("PARTICULA.ITERACAO");
	}

        public void setMetadata(Metadata metadata[]){
            this.metadata[0] = (Metadata)metadata[0].clone();
        }

	public DataUnit getNext(int consumerId) throws Exception{	   

		//Se comunica com produtor para obter tupla
                long ExecTime = System.currentTimeMillis();
               // System.out.println("TCPNext = " + id + "consumerId"+ consumerId);
		Instance t = (Instance)super.getNext(id);

		if(t != null) {
                        Thread.sleep(100);
/*
			//Obtem valores da tupla
			Point particle, p1, p2, p3, p4, v1, v2, v3, v4;
                        PointListType cells, velVector;

			particle  = (Point) t.getData(this.point);
			cells     = (PointListType) t.getData(this.vertexes);
                        velVector = (PointListType) t.getData(this.vector);

			p1 = (Point)cells.get(0);
			p2 = (Point)cells.get(1);
			p3 = (Point)cells.get(2);
			p4 = (Point)cells.get(3);

                      	v1 = (Point)velVector.get(0);
			v2 = (Point)velVector.get(1);
			v3 = (Point)velVector.get(2);
			v4 = (Point)velVector.get(3);

                        //executa calculo
			tcp.calcularTrajetoria(particle, p1, p2, p3, p4, v1, v2, v3, v4);

			//Incrementa campo de iteracao
			IntegerType it;
			it = (IntegerType) t.getData(this.iteration);
			it.setValue( it.intValue()+1 );			
			
			//System.out.println("TCP(" + id + "): Saida: " + t.getProperty("ITERATION_NUMBER") + " Part " + particle + " p1 " +p1+ " p2 " +p2+ " p3 " +p3+ " p4 " + p4 + " v1 " +v1+ " v2 " +v2+ " v3 " +v3+ " v4 " + v4);
 * 
 */
		}

                System.out.println("resultFinal = " + t + " TCP EXECUTIONTIME = " + (System.currentTimeMillis() - ExecTime));
                return t;
	}	

	
	
	public static void main(String[] args) {


	    Point part = new Point(531, (float) -0.0538065, (float) -0.7983090, (float) 02.2594442);

	    Point p1 = new Point(7429, (float) -0.0268176, (float) -0.7891504, (float) 02.2037511);      Point v1 = new Point(7429, (float) 1.0673780, (float) -52.0090103, (float) 81.0338974);
	    Point p2 = new Point(283, (float) -0.0514934, (float) -0.7652319, (float) 02.2574351);       Point v2 = new Point(283, (float) 0.0000000, (float) 0.0000000, (float) 0.0000000);
	    Point p3 = new Point(11451, (float) -0.0785385, (float) -0.8364025, (float) 2.2645750);     Point v3 = new Point(11451, (float) -2.4649780, (float) -39.6308098, (float) 75.7305832);
	    Point p4 = new Point(11825, (float) -0.0195131, (float) -0.8368953, (float) 2.2711020);     Point v4 = new Point(11825, (float) -0.6291394, (float) -51.7440300, (float) 89.9481506);


	    TCP t = new TCP();
	    
	    System.out.println(t.calcularTrajetoria(part, p1, p2, p3, p4, v1, v2, v3, v4));
    }
}
