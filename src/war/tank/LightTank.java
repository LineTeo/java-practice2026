package war.tank;
// LightTank.java - 軽戦車クラス
// 速度特化型
public class LightTank extends Tank {
 
 public LightTank(String name, int jinei, double x, double y) {
     super(name, jinei, 1500, 600, 180, 2, x, y);
		// 名前、陣営（敵味方）、耐久力、攻撃力、防御力、速度、位置X、位置Y
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("軽戦車");
 }
}