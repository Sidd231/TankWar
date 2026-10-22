import java.awt.Image;
import java.awt.Rectangle;

/**
 * Encapsulates the Player tank, including grid-based tile movement,
 * smooth animation interpolation, direction reversing, and firing.
 */
public class Player extends TankObject {
    private int targetX;
    private int targetY;
    private boolean isMoving = false;

    public Player(Image up, Image down, Image left, Image right, int x, int y, int width, int height, int speed) {
        super(up, down, left, right, x, y, width, height, speed);
        this.targetX = x;
        this.targetY = y;
        this.isMoving = false;
    }

    /**
     * Handles an input command to move in a specific direction.
     */
    public void handleDirectionInput(String dir, GameContext context) {
        if (dir == null || context == null) return;
        int tileSize = context.getTileSize();

        // If player is already transitioning between tiles:
        if (isMoving) {
            boolean isReverse = false;
            int revX = targetX;
            int revY = targetY;

            if ("UP".equals(direction) && "DOWN".equals(dir)) {
                revY += tileSize;
                isReverse = true;
            } else if ("DOWN".equals(direction) && "UP".equals(dir)) {
                revY -= tileSize;
                isReverse = true;
            } else if ("LEFT".equals(direction) && "RIGHT".equals(dir)) {
                revX += tileSize;
                isReverse = true;
            } else if ("RIGHT".equals(direction) && "LEFT".equals(dir)) {
                revX -= tileSize;
                isReverse = true;
            }

            if (isReverse) {
                targetX = revX;
                targetY = revY;
                setDirection(dir);
            }
            return;
        }

        // Tank is stopped at a grid cell
        setDirection(dir);
        int stepX = 0;
        int stepY = 0;
        switch (dir) {
            case "UP":
                stepY = -tileSize;
                break;
            case "DOWN":
                stepY = tileSize;
                break;
            case "LEFT":
                stepX = -tileSize;
                break;
            case "RIGHT":
                stepX = tileSize;
                break;
            default:
                break;
        }

        int nextX = x + stepX;
        int nextY = y + stepY;

        if (!context.checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
            targetX = nextX;
            targetY = nextY;
            isMoving = true;
        }
    }

    /**
     * Updates player position along the grid path and records scent markers.
     */
    public void update(GameContext context, String activeInputDirection) {
        if (context == null) return;

        if (isMoving) {
            int dx = targetX - x;
            int dy = targetY - y;

            if (dx != 0) {
                int step = Math.min(Math.abs(dx), speed) * Integer.signum(dx);
                x += step;
            }
            if (dy != 0) {
                int step = Math.min(Math.abs(dy), speed) * Integer.signum(dy);
                y += step;
            }

            int tileSize = context.getTileSize();
            context.registerScent(x / tileSize, y / tileSize);

            if (x == targetX && y == targetY) {
                isMoving = false;
                checkNextMove(context, activeInputDirection);
            }
        } else {
            checkNextMove(context, activeInputDirection);
        }
    }

    private void checkNextMove(GameContext context, String activeInputDirection) {
        if (activeInputDirection != null && !activeInputDirection.isEmpty()) {
            handleDirectionInput(activeInputDirection, context);
        }
    }

    /**
     * Fires a projectile from the player's center.
     */
    public void shoot(GameContext context, Image missileImage) {
        if (context == null || missileImage == null) return;
        int tileSize = context.getTileSize();
        int missileSize = 8;
        int spawnX = x + tileSize / 2 - (missileSize / 2);
        int spawnY = y + tileSize / 2 - (missileSize / 2);
        context.spawnMissile(new Missile(missileImage, spawnX, spawnY, missileSize, missileSize, direction, true));
    }

    @Override
    public void takeHit() {
        setDestroyed(true);
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }
}
