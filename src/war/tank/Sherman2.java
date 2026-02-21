package war.tank;

public class Sherman2  extends Sherman {

	public Sherman2(String name,int jinei, double x, double y) {
		super(name, jinei, x, y);
		this.setMaxHp(100);
	}

	@Override
	public void typeName() {
		super.setType("Super M4");
	}

}
