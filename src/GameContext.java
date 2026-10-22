import java.awt.Rectangle;
import java.util.List;

/**
 * Interface defining the game environment services accessible to entities
 * (collision detection, line of sight, entity references, and spawning).
 */
public interface GameContext {
    int getTileSize();
    int getBoardWidth();
    int getBoardHeight();

    boolean checkWallCollision(Rectangle bounds);
    boolean hasLineOfSight(Rectangle sightLine);

    Player getPlayer();
    List<ScentNode> getScentTrail();

    void spawnMissile(Missile missile);
    void registerScent(int gridX, int gridY);
}
