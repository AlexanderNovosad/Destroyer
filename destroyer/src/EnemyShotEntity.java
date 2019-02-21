import java.util.Random;

public class EnemyShotEntity extends Entity {
    private final Random random = new Random();
    private double moveSpeed = 300;
    private double horizontalMoveSpeed = 100;
    private Game game;
    private boolean used = false;

    public EnemyShotEntity(Game game, String sprite, int x, int y, String orientation) {
        super(sprite,x,y);
        this.game = game;
        dy = moveSpeed;
        if (orientation.equals("left")){
            dx = -horizontalMoveSpeed+random.nextInt(100);
        } else if (orientation.equals("right")){
            dx = horizontalMoveSpeed-random.nextInt(100);
        }
        if (orientation.equals("null")){
            return;
        }
    }

    public void move(long delta) {
        super.move(delta);

        if (y < 100) {
            game.removeEntity(this);
        }
    }

    public void collidedWith(Entity other) {
        if (used) {
            return;
        }

        if (other instanceof ShipEntity) {
            game.removeEntity(this);
            game.removeEntity(other);

            game.notifyDeath();
            used = true;
        }
    }
}
