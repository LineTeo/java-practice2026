package war.tank;

public class DummyTank extends Tank {

	
	 public DummyTank(Tank original) {
		 super(original.getName()+"_SIM",
		      original.getJinei(),
		      original.getMaxHp(),
		      original.getAttack(),
		      original.getDefense(),
		      original.getX(),
		      original.getY());

		 this.setHp(original.getHp());
		 this.setAngle(original.getAngle());
		 this.setSpeed(original.getSpeed());    
	     typeName();
	 }
	 

	 public void setXY(double X, double Y) {
		 super.setX(X);
		 super.setY(Y); 
	 }
	 public void typeName() {
		// TODO 自動生成されたメソッド・スタブ
	}

    @Override
    public String toString() {
    	return this.name;
    }    
    
    
}
