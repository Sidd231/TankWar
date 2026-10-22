import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

/**
 * Base class for all physical entities in the Tank War game.
 * Encapsulates position, dimensions, sprite, and collision boundaries.
 */
public class GameObject implements Drawable, Collidable {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image image;

    public GameObject(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean intersects(Collidable other) {
        if (other == null) return false;
        return getBounds().intersects(other.getBounds());
    }

    @Override
    public boolean intersects(Rectangle rect) {
        if (rect == null) return false;
        return getBounds().intersects(rect);
    }

    @Override
    public void draw(Graphics g, ImageObserver observer) {
        if (image != null && g != null) {
            g.drawImage(image, x, y, width, height, observer);
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
