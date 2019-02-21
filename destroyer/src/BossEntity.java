public class BossEntity extends EnemyEntity {
    private double moveSpeed = 250;
    private Game game;
    public BossEntity(Game game, String ref, int x, int y, int HP){
        super(game,ref, x, y, HP);
        this.game = game;
        dx = -moveSpeed;
        dy = 5;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
		}
		if ((dx > 0) && (x > 750)) {
			game.updateLogic();
		}
    }

    public void doLogic() {
		dx = -dx;
		if (y > 570) {
			game.notifyDeath();
		}
    }
}
