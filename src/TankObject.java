import java.awt.Image;

/**
 * Abstract base class for all tanks (Player and Bots).
 * Encapsulates directional sprites, orientation, speed, and movement.
 */
public abstract class TankObject extends GameObject implements Damageable {
    protected int speed;
    protected int velocityX = 0;
    protected int velocityY = 0;
    protected String direction = "UP";
    protected Image imgUp;
    protected Image imgDown;
    protected Image imgLeft;
    protected Image imgRight;
    protected boolean destroyed = false;

    public TankObject(Image up, Image down, Image left, Image right, int x, int y, int width, int height, int speed) {
        super(up, x, y, width, height);
        this.imgUp = up;
        this.imgDown = down;
        this.imgLeft = left;
        this.imgRight = right;
        this.speed = speed;
    }

    public void setDirection(String newDir) {
        if (newDir == null) return;
        this.direction = newDir;
        switch (direction) {
            case "UP":
                this.image = imgUp;
                break;
            case "DOWN":
                this.image = imgDown;
                break;
            case "LEFT":
                this.image = imgLeft;
                break;
            case "RIGHT":
                this.image = imgRight;
                break;
            default:
                break;
        }
    }

    public void snapToGrid(int tileSize) {
        if (tileSize <= 0) return;
        if (velocityY != 0) {
            int gridX = Math.round((float) x / tileSize);
            this.x = gridX * tileSize;
        }
        if (velocityX != 0) {
            int gridY = Math.round((float) y / tileSize);
            this.y = gridY * tileSize;
        }
    }

    public void snapToGrid() {
        snapToGrid(32);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public String getDirection() {
        return direction;
    }
}
