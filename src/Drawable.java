import java.awt.Graphics;
import java.awt.image.ImageObserver;

/**
 * Interface for any game entity that can be rendered to the screen.
 */
public interface Drawable {
    void draw(Graphics g, ImageObserver observer);
}
