import java.awt.Image;

/**
 * Represents a projectile fired by the player or an enemy tank.
 */
public class Missile extends GameObject {
    private int velocityX = 0;
    private int velocityY = 0;
    private final boolean fromPlayer;

    public Missile(Image image, int x, int y, int width, int height, String direction, boolean fromPlayer) {
        super(image, x, y, width, height);
        this.fromPlayer = fromPlayer;

        int missileSpeed = 5;
        switch (direction) {
            case "UP":
                this.velocityY = -missileSpeed;
                break;
            case "DOWN":
                this.velocityY = missileSpeed;
                break;
            case "LEFT":
                this.velocityX = -missileSpeed;
                break;
            case "RIGHT":
                this.velocityX = missileSpeed;
                break;
            default:
                break;
        }
    }

    public void move() {
        this.x += velocityX;
        this.y += velocityY;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }
}
