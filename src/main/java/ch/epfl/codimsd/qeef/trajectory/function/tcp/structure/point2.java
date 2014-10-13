package ch.epfl.codimsd.qeef.trajectory.function.tcp.structure;

public class point2 {
	public int pointId;
	public double x, y, z;
    public point2 (int p, double x,double y, double z) {
      this.pointId=p;
      this.x=x;
      this.y=y;
      this.z=z;
    }
    
	public point2 () {
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
	public void setX(double x){
		this.x=x;
	}
	public double getX(double x){
		return x;
	}
	public void setY(double y){
		this.y=y;
	}
	public double getY(double y){
		return y;
	}
	public void setZ(double z){
		this.z=z;
	}
	public double getZ(double z){
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
