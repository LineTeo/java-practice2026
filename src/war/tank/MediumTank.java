package war.tank;
//MediumTank.java - 中戦車クラス
public class MediumTank extends Tank {
 
 public MediumTank(String name, int jinei, double x, double y) {
     super(name, jinei, 2000, 20, 10, x, y);
     typeName();
 }
 
 @Override
 public void typeName() {
     setType("中戦車");
 }
}