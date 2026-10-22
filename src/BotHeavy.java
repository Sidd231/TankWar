import java.awt.Image;

/**
 * Heavy armored bot with multi-hit durability.
 */
public class BotHeavy extends Bot {
    private int health = 3;

    public BotHeavy(Image up, Image down, Image left, Image right, int x, int y, int width, int height, int speed) {
        super(up, down, left, right, x, y, width, height, speed);
    }

    @Override
    public void takeHit() {
        this.health--;
        if (this.health <= 0) {
            setDestroyed(true);
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
