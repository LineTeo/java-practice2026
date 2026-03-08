package war.tank;
//HeavyTank.java - 重戦車クラス
public class HeavyTank extends Tank {
 
 public HeavyTank(String name, int jinei,  double x, double y) {
     super(name, jinei, 3500, 800, 120, 1,x, y);
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("重戦車");
 }
}