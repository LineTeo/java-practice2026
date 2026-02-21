package war.tank;

public class Sherman2  extends Sherman {

	public Sherman2(String name, double x, double y) {
		super(name, x, y);
		this.setMaxHp(100);
	}

	@Override
	public void typeName() {
		super.setType("Super M4");
	}

}
