/**
 * Represents a scent marker left by the player on the grid.
 */
public class ScentNode {
    private int x;
    private int y;
    private int strength;

    public ScentNode(int x, int y, int strength) {
        this.x = x;
        this.y = y;
        this.strength = strength;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void decay() {
        this.strength--;
    }

    public boolean isExpired() {
        return this.strength <= 0;
    }
}
