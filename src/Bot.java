import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

/**
 * Represents the base AI enemy tank.
 * Implements a state machine (Wandering vs Hunting) and line-of-sight targeting.
 */
public class Bot extends TankObject {
    public enum AIState { WANDERING, HUNTING }

    protected AIState currentState = AIState.WANDERING;
    protected int moveTimer = 0;
    protected int shootCooldown = 100;
    protected Random random = new Random();
    private Point lastKnownPlayerPosition;

    public Bot(Image up, Image down, Image left, Image right, int x, int y, int width, int height, int speed) {
        super(up, down, left, right, x, y, width, height, speed);
    }

    public void update(GameContext context, Image missileImage) {
        if (context == null) return;
        int tileSize = context.getTileSize();

        if (hasLineOfSight(context)) {
            currentState = AIState.HUNTING;
            Player player = context.getPlayer();
            if (player != null) {
                lastKnownPlayerPosition = new Point(player.getX(), player.getY());
            }
        }

        if (currentState == AIState.HUNTING) {
            moveTowards(lastKnownPlayerPosition, tileSize);
            if (lastKnownPlayerPosition != null && getBounds().contains(lastKnownPlayerPosition)) {
                currentState = AIState.WANDERING;
            }
        } else {
            moveTimer--;
            if (moveTimer <= 0) {
                changeRandomDirection(tileSize);
                moveTimer = 50 + random.nextInt(100);
            }
        }

        int nextX = x + velocityX;
        int nextY = y + velocityY;
        if (!context.checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
            x = nextX;
            y = nextY;
        } else {
            changeRandomDirection(tileSize);
        }

        shootCooldown--;
        if (shootCooldown <= 0 && hasLineOfSight(context)) {
            shoot(context, missileImage);
        }
    }

    protected void moveTowards(Point target, int tileSize) {
        if (target == null) return;
        snapToGrid(tileSize);
        int dx = target.x - x;
        int dy = target.y - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                setDirection("RIGHT");
                velocityX = speed;
                velocityY = 0;
            } else {
                setDirection("LEFT");
                velocityX = -speed;
                velocityY = 0;
            }
        } else {
            if (dy > 0) {
                setDirection("DOWN");
                velocityY = speed;
                velocityX = 0;
            } else {
                setDirection("UP");
                velocityY = -speed;
                velocityX = 0;
            }
        }
    }

    protected void changeRandomDirection(int tileSize) {
        snapToGrid(tileSize);
        int dir = random.nextInt(5);
        String newDir = direction;
        switch (dir) {
            case 0:
                velocityY = -speed;
                velocityX = 0;
                newDir = "UP";
                break;
            case 1:
                velocityY = speed;
                velocityX = 0;
                newDir = "DOWN";
                break;
            case 2:
                velocityX = -speed;
                velocityY = 0;
                newDir = "LEFT";
                break;
            case 3:
                velocityX = speed;
                velocityY = 0;
                newDir = "RIGHT";
                break;
            case 4:
                velocityX = 0;
                velocityY = 0;
                break;
            default:
                break;
        }
        setDirection(newDir);
    }

    protected boolean hasLineOfSight(GameContext context) {
        if (context == null) return false;
        Player player = context.getPlayer();
        if (player == null) return false;

        int tileSize = context.getTileSize();
        Rectangle sightLine = new Rectangle();
        boolean aligned = false;

        if (Math.abs(player.getX() - x) < tileSize) {
            aligned = true;
            if (player.getY() < y) {
                sightLine.setBounds(x, player.getY(), width, y - player.getY());
            } else {
                sightLine.setBounds(x, y, width, player.getY() - y);
            }
        } else if (Math.abs(player.getY() - y) < tileSize) {
            aligned = true;
            if (player.getX() < x) {
                sightLine.setBounds(player.getX(), y, x - player.getX(), height);
            } else {
                sightLine.setBounds(x, y, player.getX() - x, height);
            }
        }

        if (!aligned) return false;
        return context.hasLineOfSight(sightLine);
    }

    public void shoot(GameContext context, Image missileImage) {
        if (context == null || missileImage == null) return;
        int tileSize = context.getTileSize();
        int missileSize = 8;
        int spawnX = x + tileSize / 2 - (missileSize / 2);
        int spawnY = y + tileSize / 2 - (missileSize / 2);
        context.spawnMissile(new Missile(missileImage, spawnX, spawnY, missileSize, missileSize, direction, false));
        shootCooldown = 100 + random.nextInt(100);
    }

    @Override
    public void takeHit() {
        setDestroyed(true);
    }

    public AIState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AIState currentState) {
        this.currentState = currentState;
    }
}
