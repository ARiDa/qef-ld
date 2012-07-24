package ch.epfl.codimsd.qeef.trajectory.function.tcp.structure;

public class point {
	public int pointId;
	public float x, y, z;
    public point (int p, float x,float y, float z) {
      this.pointId=p;
      this.x=x;
      this.y=y;
      this.z=z;
    }
    
	public point () {
	  this.pointId=0;
	  this.x=0;
	  this.y=0;
	  this.z=0;
	}
	public void setPointId(int p){
		pointId=p;
	}
	public int getPointId(int p){
		return p;
	}
	public void setX(float x){
		this.x=x;
	}
	public float getX(float x){
		return x;
	}
	public void setY(float y){
		this.y=y;
	}
	public float getY(float y){
		return y;
	}
	public void setZ(float z){
		this.z=z;
	}
	public float getZ(float z){
		return z;
	}
	public Object clone(){
		try{
			return super.clone();
		}catch (CloneNotSupportedException e){
			return null;
		}
	}	
}
