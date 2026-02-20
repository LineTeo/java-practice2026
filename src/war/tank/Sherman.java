package war.tank;

public class Sherman  extends Tank {

	public Sherman(String name, double x, double y) {
		super(name, 80, 200, 10, x, y);
		

	}

	@Override
	public void typeName() {
		super.setType("M4");
	}
}
