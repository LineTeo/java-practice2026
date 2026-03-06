package war.ai;
import java.io.Serializable;


public class DecisionFactors  implements Serializable {

	private int ZONE;

	private double distance, distanceSIM;
	
	private double AC1,AC2;
	private double DC1,DC2;
	private double AT1,AT2;
	private double DT1,DT2;

	public DecisionFactors() {}

    public double getZONE() { return ZONE; }
    public void setZONE(int ZONE) { this.ZONE = ZONE; }

	
	public double getAC1() { return AC1; }
    public void setAC1(double AC1) { this.AC1 = AC1; }
    public double getAC2() { return AC2; }
    public void setAC2(double AC2) { this.AC2 = AC2; }
    public double getDC1() { return DC1; }
    public void setDc1(double DC1) { this.DC1 = DC1; }
    public double getDC2() { return DC2; }
    public void setDC2(double DC2) { this.DC2 = DC2; }
    public double getAT1() { return AT1; }
    public void setAT1(double AT1) { this.AT1 = AT1; }
    public double getAT2() { return AT2; }
    public void setAT2(double AT2) { this.AT2 = AT2; }
    public double getDT1() { return DT1; }
    public void setDT1(double DT1) { this.DT1 = DT1; }
    public double getDT2() { return DT2; }
    public void setDT2(double DT2) { this.DT2 = DT2; }
	
    public double getDistance() { return distance; }
    public void setdistance(double distance) { this.distance = distance; }
    public double getdistanceSIM() { return distanceSIM; }
    public void setdistanceSIM(double DTdistanceSIM2) { this.distanceSIM = distanceSIM; }

}
