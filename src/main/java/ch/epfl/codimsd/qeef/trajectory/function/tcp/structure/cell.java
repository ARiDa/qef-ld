
package ch.epfl.codimsd.qeef.trajectory.function.tcp.structure;

public class cell {
	public int cellId;
	public int [] v= new int[4];		// Vertex index
	public	point2 center;
	public Object clone(){
		try{
			return super.clone();
		}catch (CloneNotSupportedException e){
			return null;
		}
	}
}
