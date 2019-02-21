public class ShotEntity extends Entity {
	private double moveSpeed = -300;
	private Game game;
	private boolean used = false;
	private int damage = 50;

	public ShotEntity(Game game, String sprite, int x, int y) {
		super(sprite,x,y);
		this.game = game;
		dy = moveSpeed;
	}

	public void move(long delta) {
		super.move(delta);

		if (y < -100) {
			game.removeEntity(this);
		}
	}

	public void collidedWith(Entity other) {
		if (used) {
			return;
		}

		if (other instanceof EnemyEntity) {
			game.removeEntity(this);
			((EnemyEntity) other).hit(damage);
			if (((EnemyEntity) other).getHP() <= 0) {
				game.drawExplosion(other.getX(), other.getY(), other.getWidth(), other.getHeight());
				game.removeEntity(other);

				if (other instanceof BossEntity){
					game.notifyBossKilled();
				}
				else{
					game.notifyEnemyKilled();
				}
			}
			used = true;
		}
	}
}