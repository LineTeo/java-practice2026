package war.tank;

public class Tank2 extends Tank {

	int t;
	
    public Tank2(String name, int jinei, int hp, int attack, int defense,double speed, double x, double y, int t) {
		super(name, jinei, hp, attack, defense, speed, x, y);
		// 名前、陣営（敵味方）、耐久力、攻撃力、防御力、速度、位置X、位置Y
		this.t = t;
		super.setAngle(30 * t);
		
		typeName();
    }

	@Override
	public void typeName() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void forward() { //前進
		double targetX = this.getX() - Math.sin(this.t * Math.PI / 6) * 100;
		double targetY = this.getY() - Math.cos(this.t * Math.PI / 6) * 100;			
		this.move(targetX, targetY);
	}

	public void back() {    //後退
		double targetX = this.getX() + Math.sin(this.t * Math.PI / 6) * 100;
		double targetY = this.getY() + Math.cos(this.t * Math.PI / 6) * 100;			
		this.move(targetX, targetY);
	}
}
