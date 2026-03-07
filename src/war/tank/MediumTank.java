package war.tank;
//MediumTank.java - 中戦車クラス
public class MediumTank extends Tank {
 
 public MediumTank(String name, int jinei, double x, double y) {
     super(name, jinei, 2500, 600, 15, x, y);
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("中戦車");
 }
}