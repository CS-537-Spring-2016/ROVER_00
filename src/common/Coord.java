package common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


import enums.Science;
import enums.Terrain;

public class Coord {



	// thanks to this posting http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
	
    public final int xpos;
    public final int ypos;
    public Terrain terrain;
    public boolean hasRover;
    public Science science;
    
    
    private Coord parent;
  
    
    private double g;
    private double h;
    private double f;
	

	public Coord getParent() {
		return parent;
	}


	public void setParent(Coord parent) {
		this.parent = parent;
	}


	public double getG() {
		return g;
	}


	public void setG(Coord current) {
		this.g = current.getG() + 1;
	}

	public double calculategCosts(Coord previous) {

		return (previous.getG() + 1);

	}

	public double getH() {
		return h;
	}


	public void setH(Coord endNode) {
		
		this.h = Math.sqrt((Math.pow(absolute(this.xpos - endNode.xpos), 2)
				+ Math.pow(absolute(this.ypos - endNode.ypos), 2))) * 1;
		
	}


	public double getF() {
		return g + h;
	}


	public void setF(double f) {
		this.f = f;
	}
	private int absolute(int a) {
		return a > 0 ? a : -a;
	}

	@Override
	public String toString() {
		return terrain + " " + science + " " + xpos + " " + ypos;
	}

	
	public Coord(int x, int y){
		this.xpos = x;
		this.ypos = y;
		h=0;
		f=0;
		g=0;
	}
	
    public Coord(Terrain terrain, Science science, int x, int y) {
        this(x, y);
        this.science = science;
        this.terrain = terrain;
        h=0;
		f=0;
		g=0;
    }
	
    /** @return String that can be used to send to other ROVERS. This string
     *         follows the communication protocol: TERRAIN SCIENCE XPOS YPOS
     * @author Shay */
    public String toProtocol() {
        return terrain + " " + science + " " + xpos + " " + ypos;
    }
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(xpos).
            append(ypos).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Coord))
            return false;
        if (obj == this)
            return true;

        Coord theOther = (Coord) obj;

        return ((this.xpos == theOther.xpos) && (this.ypos == theOther.ypos));
    }
	

}