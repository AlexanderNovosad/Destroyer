import midi.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import javax.swing.*;
import java.util.Random;

public class Game extends Canvas {
	private final Random random = new Random();
	private BufferStrategy strategy;
	private boolean gameRunning = true;
	private ArrayList entities = new ArrayList();
	private Entity ship;
	private Entity enemy;
	private double moveSpeed = 300;
	private long lastFire = 0;
	private long firingInterval = 500;
	private long enemyFiringInterval = 1000;
	private long lastEnemyFire = 0;
	private int enemyCount;
	private int statistic = 0;
	private int record = 0;
	private int levelCount;
	private long levelTime = 0;
	private long respawn = 0;
	private long respawnInterval = 1500;
	private long pauseTime = 0;
	private Sprite background;
	private Sprite explosion;
	private String message = "";

	private boolean waitingForKeyPress = true;
	private boolean respawnSuccess = false;
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean downPressed = false;
	private boolean upPressed = false;
	private boolean firePressed = false;
	private boolean logicRequiredThisLoop = false;
	private boolean pause = false;
	private boolean goToNewLevel = true;

	public Game() {
		JFrame container = new JFrame("Destroyer");
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);
		setBounds(0,0,800,600);
		panel.add(this);

		setIgnoreRepaint(true);

		container.pack();
		container.setResizable(false);
		container.setVisible(true);

		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		addKeyListener(new KeyInputHandler());

		requestFocus();

		createBufferStrategy(2);
		strategy = getBufferStrategy();

		initEntities();

		background = SpriteStore.get().getSprite("sprites/sky.gif");
		explosion = SpriteStore.get().getSprite("sprites/exp.png");
	}

	private void startGame() {
		entities.clear();
		initEntities();
		statistic = 0;
		respawnSuccess = true;
		goToNewLevel = true;
		levelCount = 1;
		levelTime = System.currentTimeMillis();
		respawn = System.currentTimeMillis();

		leftPressed = false;
		rightPressed = false;
		downPressed = false;
		upPressed = false;
		firePressed = false;
	}

	public void initEntities() {
		ship = new ShipEntity(this,"sprites/app-launch-spaceship-icon.png",370,520);
		entities.add(ship);

		enemyCount = 0;
		for (int row=0;row<1;row++) {
			for (int x=0;x<6;x++) {
				enemy = new EnemyEntity(this,"sprites/alien.gif",100+(x*100),(50)+row*30,50);
				entities.add(enemy);
				enemyCount++;
			}
		}
	}

	public void updateLogic() {
		logicRequiredThisLoop = true;
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	public void notifyDeath() {
		message = "They got you, try again?";
		waitingForKeyPress = true;
		respawnSuccess = false;
	}

	public void notifyWin() {
		message = "Well done! You Win!";
		waitingForKeyPress = true;
		respawnSuccess = false;
	}

	public void notifyEnemyKilled() {
		enemyCount--;
		statistic += 10;
		if (record < statistic){
			record = statistic;
		}
	}

	public void drawExplosion(int x, int y, int width, int height){
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		explosion.draw(g,x,y,width,height);
	}

	public void notifyBossKilled() {
		enemyCount--;
		statistic += 500;
		if (record < statistic){
			record = statistic;
		}

		levelCount++;
		levelTime = System.currentTimeMillis();
		respawn = System.currentTimeMillis();
		respawnSuccess = true;
		goToNewLevel = true;
		if (enemyCount == 0 && levelCount > 3) {
			notifyWin();
		}
	}

	public void tryToFire() {
		if (System.currentTimeMillis() - lastFire < firingInterval) {
			return;
		}
		lastFire = System.currentTimeMillis();
		ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",ship.getX()+10,ship.getY()-30);
		entities.add(shot);
		Tools.playNote( Note.p4_0_Mi, Note.i127_Gunshot,100, 200);
	}

	public void enemyFire(){
		if (System.currentTimeMillis() - lastEnemyFire < enemyFiringInterval){
			return;
		}
		Entity entity = (Entity) entities.get(random.nextInt(entities.size()));
		if (entity instanceof BossEntity) {
			lastEnemyFire = System.currentTimeMillis();
			EnemyShotEntity enemyShot = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 30, entity.getY() + 85,"right");
			EnemyShotEntity enemyShot2 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 50, entity.getY() + 85,"left");
			entities.add(enemyShot);
			entities.add(enemyShot2);
			if (levelCount == 2){
				EnemyShotEntity enemyShot3 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 30, entity.getY() + 85,"null");
				entities.add(enemyShot3);
			}
			if (levelCount == 3){
				EnemyShotEntity enemyShot3 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 30, entity.getY() + 85,"right");
				EnemyShotEntity enemyShot4 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 50, entity.getY() + 85,"left");
				entities.add(enemyShot3);
				entities.add(enemyShot4);
			}
			Tools.playNote( Note.p24_2_Do, Note.i127_Gunshot,50, 200);
			return;
		}
		if (entity instanceof EnemyEntity) {
			lastEnemyFire = System.currentTimeMillis();
			if (levelCount == 1){
				EnemyShotEntity enemyShot = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 10, entity.getY() + 40,"null");
				entities.add(enemyShot);
			}
			if (levelCount == 2 || levelCount == 3){
				EnemyShotEntity enemyShot = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 30, entity.getY() + 50,"right");
				EnemyShotEntity enemyShot2 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 20, entity.getY() + 50,"left");
				entities.add(enemyShot);
				entities.add(enemyShot2);
			}
			if (levelCount == 3){
				EnemyShotEntity enemyShot3 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 30, entity.getY() + 50,"right");
				EnemyShotEntity enemyShot4 = new EnemyShotEntity(this, "sprites/shot2.gif", entity.getX() + 20, entity.getY() + 50,"left");
				entities.add(enemyShot3);
				entities.add(enemyShot4);
			}
			Tools.playNote( Note.p24_2_Do, Note.i127_Gunshot,100, 200);
		}
	}

	public void initNewEnemies(){
		if (System.currentTimeMillis() - respawn < respawnInterval || pause || (!respawnSuccess)){
			return;
		}
		respawn = System.currentTimeMillis();
		if(levelCount == 1){
			enemy = new EnemyEntity(this,"sprites/alien.gif",random.nextInt(600)+100,0,50);
		}
		if(levelCount == 2){
			enemy = new EnemyEntity(this,"sprites/republic_assault_ship.png",random.nextInt(600)+100,0,100);
		}
		if(levelCount == 3){
			enemy = new EnemyEntity(this,"sprites/alienblaster.png",random.nextInt(600)+100,0,150);
		}
		entities.add(enemy);
		enemyCount++;
	}

	public void initTheBoss(){
		if (levelCount == 1){
			enemy = new BossEntity(this, "sprites/enemy2.png",400,0,1200);
		}
		if (levelCount == 2){
			enemy = new BossEntity(this, "sprites/nightraider.png",400,0,1400);
		}
		if (levelCount == 3){
			enemy = new BossEntity(this, "sprites/tumblr_inline.png",400,0,1600);
		}
		entities.add(enemy);
		enemyCount++;
	}

	public void checkTheCollisions(){
		for (int i=0;i<entities.size();i++) {
			for (int j=i+1;j<entities.size();j++) {
				Entity me = (Entity) entities.get(i);
				Entity him = (Entity) entities.get(j);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}
	}

	public void doShipActions(){
		if ((leftPressed) && (!rightPressed)) {
			ship.setHorizontalMovement(-moveSpeed);
		} else if ((rightPressed) && (!leftPressed)) {
			ship.setHorizontalMovement(moveSpeed);
		}
		if ((upPressed) && (!downPressed)) {
			ship.setVerticalMovement(-moveSpeed);
		} else if ((downPressed) && (!upPressed)) {
			ship.setVerticalMovement(moveSpeed);
		}
		if (firePressed && !pause) {
			tryToFire();
		}
	}

	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();

		while (gameRunning) {
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();

			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.fillRect(0,0,800,600);
			background.draw(g,0,0,800,600);

			if (!waitingForKeyPress  && !pause) {
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					entity.move(delta);
				}

				g.setColor(Color.white);
				g.setFont(new Font("SANS_SERIF",Font.ITALIC,16));
				g.drawString("Statistic: "+statistic,(1130-g.getFontMetrics().stringWidth("Statistic: "+statistic))/2,30);
				g.drawString("Record: "+record,(1400-g.getFontMetrics().stringWidth("Record: "+record))/2,30);
				g.setFont(new Font("SANS_SERIF",Font.BOLD,24));
				g.drawString("Level "+levelCount,10,570);
				enemyFire();
			}

			for (int i=0;i<entities.size();i++) {
				Entity entity = (Entity) entities.get(i);
				entity.draw(g);
			}

			checkTheCollisions();

			if (logicRequiredThisLoop) {
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					entity.doLogic();
				}
				logicRequiredThisLoop = false;
			}

			if (waitingForKeyPress) {
				g.setColor(Color.white);
				g.setFont(new Font("SANS_SERIF",Font.PLAIN,24));
				g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
				g.drawString("Press any key",(800-g.getFontMetrics().stringWidth("Press any key"))/2,300);
			}

			if (pause){
				g.setColor(Color.white);
				g.setFont(new Font("SANS_SERIF",Font.PLAIN,36));
				g.drawString("Pause!",(800-g.getFontMetrics().stringWidth("Pause!"))/2,300);
			}

			g.dispose();
			strategy.show();

			ship.setHorizontalMovement(0);
			ship.setVerticalMovement(0);

			doShipActions();

			initNewEnemies();

			if (System.currentTimeMillis() > levelTime+10000 && !pause && goToNewLevel && enemyCount < 2){
				goToNewLevel = false;
				respawnSuccess = false;
				initTheBoss();
			}
			try { Thread.sleep(10); } catch (Exception e) {}

		}
	}

	private class KeyInputHandler extends KeyAdapter {
		private int pressCount = 1;

		public void keyPressed(KeyEvent e) {
			if (waitingForKeyPress) {
				return;
			}

			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				firePressed = true;
			}
		} 

		public void keyReleased(KeyEvent e) {
			if (waitingForKeyPress) {
				return;
			}
			
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				firePressed = false;
			}
		}

		public void keyTyped(KeyEvent e) {
			if (waitingForKeyPress) {
				if (pressCount == 1) {
					waitingForKeyPress = false;
					startGame();
					pressCount = 0;
				} else {
					pressCount++;
				}
			}

			if (e.getKeyChar() == 80) {
				if (!pause) {
					pause = true;
					pauseTime = System.currentTimeMillis();
				}
				else{
					levelTime += System.currentTimeMillis() - pauseTime;
					respawn += System.currentTimeMillis() - pauseTime;
					pauseTime = 0;
					pause = false;
				}
			}

			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}
}
