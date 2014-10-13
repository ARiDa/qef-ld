package ch.epfl.codimsd.qeef.trajectory.algebraic.hash.spatial;

import java.util.Collection;
import java.util.Iterator;

import ch.epfl.codimsd.qeef.types.Point;

public class BucketExtent {
	
	public ch.epfl.codimsd.qeef.types.Point min, max;
	
	public BucketExtent(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax){
		
		min = new Point();
		max = new Point();
		
		min.x = xMin;
		min.y = yMin;
		min.z = zMin;
		
		max.x = xMax;
		max.y = yMax;
		max.z = zMax;		
	}
	
	
	//Retorna 1 se todos os pontos estao dentro da parti�ao
	//        0 se algum ponto esta dentro da particao
	//       -1 se nenhum ponto esta dentro da paticao 
	public int isInside(Collection points){
		
		int nr=0;
		Iterator itPoints = points.iterator();
		
		while(itPoints.hasNext()){
			if( isInside((Point)itPoints.next()) == 1 )
				nr++;
		}
		
		if (nr == 0)
			return -1;
		if (nr == points.size())
			return 1;
		else
			return 0;
	}
	
	public int isInside(Point point){
		
		if( (point.x >= min.x && point.x <= max.x) &&
			(point.y >= min.y && point.y <= max.y) &&
			(point.z >= min.z && point.z <= max.z)){
			return 1;
		} else
			return -1;

	}
}
