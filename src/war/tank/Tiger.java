package war.tank;

public class Tiger extends Tank {

	public Tiger(String name, int jinei,double x, double y) {
		super(name, jinei, 4000, 15, 20, x, y);
		// TODO 自動生成されたコンストラクター・スタブ
		typeName();
	}
	@Override
	public void typeName() {
		super.setType("Tiger1");
	}

}
