import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * Fast reconnaissance bot that tracks player pheromone scent trails.
 */
public class BotFast extends Bot {

    public BotFast(Image up, Image down, Image left, Image right, int x, int y, int width, int height, int speed) {
        super(up, down, left, right, x, y, width, height, speed);
    }

    @Override
    public void update(GameContext context, Image missileImage) {
        if (context == null) return;
        int tileSize = context.getTileSize();

        ScentNode targetScent = findStrongestAdjacentScent(context);
        if (targetScent != null) {
            currentState = AIState.HUNTING;
            moveTowards(new Point(targetScent.getX() * tileSize, targetScent.getY() * tileSize), tileSize);
        } else {
            currentState = AIState.WANDERING;
            moveTimer--;
            if (moveTimer <= 0) {
                changeRandomDirection(tileSize);
                moveTimer = 20 + random.nextInt(50);
            }
        }

        int nextX = x + velocityX;
        int nextY = y + velocityY;
        if (!context.checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
            x = nextX;
            y = nextY;
        } else {
            changeRandomDirection(tileSize);
        }

        shootCooldown--;
        if (shootCooldown <= 0 && hasLineOfSight(context)) {
            shoot(context, missileImage);
        }
    }

    private ScentNode findStrongestAdjacentScent(GameContext context) {
        List<ScentNode> scentTrail = context.getScentTrail();
        if (scentTrail == null) return null;

        int tileSize = context.getTileSize();
        int currentX = x / tileSize;
        int currentY = y / tileSize;
        ScentNode bestScent = null;

        for (ScentNode scent : scentTrail) {
            if (Math.abs(scent.getX() - currentX) <= 1 && Math.abs(scent.getY() - currentY) <= 1) {
                if (bestScent == null || scent.getStrength() > bestScent.getStrength()) {
                    bestScent = scent;
                }
            }
        }
        return bestScent;
    }
}
