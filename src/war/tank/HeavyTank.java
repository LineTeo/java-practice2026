package war.tank;
// HeavyTank.java - 重戦車クラス
// 速度はやや遅い前面装甲特化型

public class HeavyTank extends Tank {
 
 public HeavyTank(String name, int jinei,  double x, double y) {
     super(name, jinei, 3500, 800, 120, 0.8,x, y);
	// 名前、陣営（敵味方）、耐久力、攻撃力、防御力、速度、位置X、位置Y
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("重戦車");
 }

	@Override
	public double angleGain(double angle) {
		
		angle = Math.abs(angle) % 360;	
		
		if (angle > 180) {
			angle = 360 - angle;
		}
 	
		switch ((int)(angle/30)){
		case 0:
			return 0.4;
		case 1:
			return 0.6;
		case 2:
			return 0.7;
		case 3:
			return 0.8;
		case 4:
			return 1.0;
		case 5:
			return 1.0;
		case 6:
			return 1.0;
 	}
 	return 2.0;
		
 }
}