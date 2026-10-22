import java.awt.Image;

/**
 * Represents a wall obstacle on the game board (e.g., Brick or Steel).
 * Encapsulates destructibility behavior.
 */
public class Wall extends GameObject implements Damageable {
    private final boolean destructible;
    private boolean destroyed = false;

    public Wall(Image image, int x, int y, int width, int height, boolean destructible) {
        super(image, x, y, width, height);
        this.destructible = destructible;
    }

    public boolean isDestructible() {
        return destructible;
    }

    @Override
    public void takeHit() {
        if (destructible) {
            this.destroyed = true;
        }
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
}
