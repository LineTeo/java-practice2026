package war.tank;
//LightTank.java - 軽戦車クラス
public class LightTank extends Tank {
 
 public LightTank(String name, int jinei, double x, double y) {
     super(name, jinei, 800, 15, 5, x, y);
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("軽戦車");
 }
}