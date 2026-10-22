import java.awt.Rectangle;

/**
 * Interface for game entities capable of collision detection.
 */
public interface Collidable {
    Rectangle getBounds();
    boolean intersects(Collidable other);
    boolean intersects(Rectangle rect);
}
