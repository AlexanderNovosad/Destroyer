public class EnemyEntity extends Entity {
	private double moveSpeed = 40;
	private Game game;
	private int HP;

	public EnemyEntity(Game game, String ref, int x, int y, int HP) {
		super(ref,x,y);
		this.game = game;
		dy = moveSpeed;
		this.HP = HP;
	}

	public void move(long delta) {
		super.move(delta);

		if (y > 570) {
			game.notifyDeath();
		}
	}

	public int hit(int damage){
		HP = HP - damage;
		return HP;
	}
	public int getHP(){
		return HP;
	}

	public void collidedWith(Entity other) {
	}
}