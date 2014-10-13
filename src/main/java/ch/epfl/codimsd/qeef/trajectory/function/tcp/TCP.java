package ch.epfl.codimsd.qeef.trajectory.function.tcp;
import ch.epfl.codimsd.qeef.types.Point;

public class TCP {
	
	public Point calcularTrajetoria(Point particle,
	        Point p1, Point p2, Point p3, Point p4, 
	        Point v1, Point v2, Point v3, Point v4){
	            
                Alphas alp = computeAlphas(particle, p1, p2, p3, p4);
		
		float delta = 0.005f;
		
		//Computes the vector
		particle.x = particle.x + delta * (alp.a1 * v1.x + alp.a2 * v2.x + alp.a3 * v3.x + alp.a4 * v4.x); 
		particle.y = particle.y + delta * (alp.a1 * v1.y + alp.a2 * v2.y + alp.a3 * v3.y + alp.a4 * v4.y); 
		particle.z = particle.z + delta * (alp.a1 * v1.z + alp.a2 * v2.z + alp.a3 * v3.z + alp.a4 * v4.z); 

		return particle;

	}
	
	//Computa alphas identicos ao do operador isInside
	public static Alphas computeAlphas(Point particle, Point p1, Point p2, Point p3, Point p4)
	{
		Point v1, v2, v3, v4, n1, n2, n3;
		float a1, a2, a3, a4;	//alphas to enable interpolation

		
		v1 = new Point();
		v2 = new Point();
		v3 = new Point();
 		v4 = new Point();
		
		n1 = new Point();
		n2 = new Point();
		n3 = new Point();
		
		setVector(v1, v2, v3, v4, particle, p1, p2, p3, p4);
		setNValues(n1, n2, n3, v2, v3, v4);

		a4=setAlphaValues( n1, v1, v4);
		a3=setAlphaValues( n2, v1, v3);
		a2=setAlphaValues( n3, v1, v2);
		
		a1 =( 1 - a2 - a3 - a4);
		
		if( !((a1>=0  && a1<=1) && (a2>=0  && a2<=1) && (a3>=0  && a3<=1) && (a4>=0  && a4<=1)) ){
		    System.err.println("TCP: Exception Alphas podem ter sido calculados errados em TCP " + particle);
		}
		
		return new Alphas(a1, a2, a3, a4);	
	}

//	 Compute subtration operation for points
	private static  void computeVectorOperationSub(Point p, Point pa, Point pb)
	{
		p.x = pa.x -pb.x;
		p.y = pa.y - pb.y;
		p.z = pa.z - pb.z;
	}


//	 Set Vector Values
	//Perguntar para alguem se a passagem dos parametros do metodo sao por referencia ou valor?
	private static void setVector(Point v1, Point v2, Point v3, Point v4, Point p0, Point p1, Point p2, Point p3, Point p4)
	{
		computeVectorOperationSub(v1,p0, p1);
		computeVectorOperationSub(v2,p2, p1);
		computeVectorOperationSub(v3,p3, p1);
		computeVectorOperationSub(v4,p4, p1);
	}


//	 Calculates Cross Product between two vector v = pa x pb
	private static void computeCrossProd(Point p,Point va,Point vb)
	{
		p.x = (va.y * vb.z) - (va.z * vb.y);
		p.y = (va.z * vb.x) - (va.x * vb.z);
		p.z = (va.x * vb.y) - (va.y * vb.x);

	}


//	 Set the 'n' Values to compute inside/outside Points
	
	private static void setNValues(Point n1, Point n2, Point n3, Point v2, Point v3, Point v4)
	{
		computeCrossProd(n1, v2, v3);
		computeCrossProd(n2, v2, v4);
		computeCrossProd(n3, v3, v4);
	}
	


//	 Calculates Scalar Product between two vector r = v1. v2
	private static float computeScalarProd(Point va, Point vb)
	{
		return (float)((va.x * vb.x) + (va.y * vb.y) + (va.z * vb.z)); 
	}


//	 Set the alpha values to compute inside/outside Points
	private static float setAlphaValues(Point n, Point va, Point vb)
	{
		float val1, val2;
		val1 = (float)(computeScalarProd(n, va)); 
		val2 = (float)(computeScalarProd(n, vb));
		
		if(val2!=0.0)
			return( (float)(val1 / val2));
		else{
			//printf("\nCheck 'setAlphaValues' function!!!\n");
			//system("PAUSE");
			//???????????? O que faz o comando SYSTEM
			return (0);
		}
	}	

	//determina se um ponto esta dentro de um tetraedro
	public static boolean isInsideCell(Point particle, Point p1, Point p2, Point p3, Point p4)
	{
		Point v1, v2, v3, v4, n1, n2, n3;
		float a1, a2, a3, a4;	//alphas to enable interpolation

		
		v1 = new Point();
		v2 = new Point();
		v3 = new Point();
 		v4 = new Point();
		
		n1 = new Point();
		n2 = new Point();
		n3 = new Point();
		
		setVector(v1, v2, v3, v4, particle, p1, p2, p3, p4);
		setNValues(n1, n2, n3, v2, v3, v4);

		a4=setAlphaValues( n1, v1, v4);
		a3=setAlphaValues( n2, v1, v3);
		a2=setAlphaValues( n3, v1, v2);
		
		a1 =( 1 - a2 - a3 - a4);
		
		if((a1>=0  && a1<=1) && (a2>=0  && a2<=1) && (a3>=0  && a3<=1) && (a4>=0  && a4<=1)){
			return true; // Point defined by global variable 'part' is inside the cell 'c'
		}
		else{
			return false; // Point is outside the cell 'c'
		}
	}
}

