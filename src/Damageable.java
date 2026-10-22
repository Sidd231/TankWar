/**
 * Interface for entities that can take damage and be destroyed.
 */
public interface Damageable {
    void takeHit();
    boolean isDestroyed();
}
