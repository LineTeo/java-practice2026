package war.tank;

public class Tiger2 extends Tank2{
	//Tiger 前面装甲強化型
	
	
	public Tiger2(String name, int jinei,double x, double y) {
		super(name, jinei, 4000, 1000, 200, 1, x, y, 0);
		// 名前、陣営（敵味方）、耐久力、攻撃力、防御力、速度、位置X、位置Y
		typeName();
	}
	@Override
	public void typeName() {
		super.setType("Tiger2");
	}

	@Override
	public double angleGain(double angle) {
		
		angle = Math.abs(angle) % 360;	
		
		if (angle > 180) {
			angle = 360 - angle;
		}
    	
		switch ((int)(angle/30)){
    	case 0:
    		return 0.5;
		case 1:
			return 0.6;
		case 2:
			return 0.6;
		case 3:
			return 0.7;
		case 4:
			return 0.8;
		case 5:
			return 1.0;
		case 6:
			return 1.0;
    	}
    	return 2.0;
		
    }
}
