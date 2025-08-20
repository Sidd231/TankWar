import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;

public class Tank extends JPanel implements ActionListener, KeyListener {

    // =================================================================================
    // --- NEW: Class Hierarchy for Game Objects ---
    // =================================================================================

    /**
     * A base class for any object that appears in the game.
     * Contains common properties like position and image.
     */
    class GameObject {
        int x, y, width, height;
        Image image;

        GameObject(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    /**
     * A template for any tank (Player or Bot). Inherits from GameObject.
     * Adds shared tank properties like speed, direction, and the ability to shoot.
     */
    class TankObject extends GameObject {
        int speed;
        int velocityX = 0;
        int velocityY = 0;
        String direction = "UP"; // To know which way the missile should fire

        TankObject(Image image, int x, int y, int width, int height, int speed) {
            super(image, x, y, width, height);
            this.speed = speed;
        }
    }

    /**
     * The Player's tank. Inherits from TankObject.
     */
    class Player extends TankObject {
        Player(Image image, int x, int y, int width, int height, int speed) {
            super(image, x, y, width, height, speed);
        }
    }

    /**
     * A Bot tank. Inherits from TankObject. Can have different speeds.
     */
    class Bot extends TankObject {
        Bot(Image image, int x, int y, int width, int height, int speed) {
            super(image, x, y, width, height, speed);
        }
    }

    /**
     * A Missile. Inherits from GameObject.
     * Has its own movement logic.
     */
    class Missile extends GameObject {
        int velocityX = 0;
        int velocityY = 0;

        Missile(Image image, int x, int y, int width, int height, String direction) {
            super(image, x, y, width, height);
            int missileSpeed = 5;
            switch (direction) {
                case "UP": velocityY = -missileSpeed; break;
                case "DOWN": velocityY = missileSpeed; break;
                case "LEFT": velocityX = -missileSpeed; break;
                case "RIGHT": velocityX = missileSpeed; break;
            }
        }

        public void move() {
            x += velocityX;
            y += velocityY;
        }
    }

    // =================================================================================
    // --- Panel Properties ---
    // =================================================================================

    private int tileSize = 32;
    private int boardWidth = 15 * tileSize;
    private int boardHeight = 15 * tileSize;

    private Image player_tank_img, brick_wall_img, bot_tank_img, steel_wall_img, missile_img;

    private LevelData map;
    private int currentLevel = 1;

    // --- NEW: Using specific collections for objects ---
    HashSet<GameObject> brick;
    HashSet<GameObject> steel;
    HashSet<Bot> bots;
    ArrayList<Missile> missiles;
    Player player;

    Timer gameLoop;

    Tank() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        try {
            brick_wall_img = new ImageIcon(getClass().getResource("/brick.jpg")).getImage();
            steel_wall_img = new ImageIcon(getClass().getResource("/image_1755482051164.jpg")).getImage();
            bot_tank_img = new ImageIcon(getClass().getResource("/btank.png")).getImage();
            player_tank_img = new ImageIcon(getClass().getResource("/ptank.png")).getImage();
            // A simple image for the missile, you can create your own
            missile_img = new ImageIcon(getClass().getResource("/missile.png")).getImage(); 
            missile_right_img = new
        } catch (Exception e) {
            e.printStackTrace();
        }

        map = new LevelData();
        loadLevel(currentLevel);

        gameLoop = new Timer(10, this);
        gameLoop.start();
    }

    public void loadLevel(int level) {
        String[] currentMapData = map.getMap(level);
        
        brick = new HashSet<>();
        steel = new HashSet<>();
        bots = new HashSet<>();
        missiles = new ArrayList<>();
        player = null;

        for (int r = 0; r < currentMapData.length; r++) {
            String line = currentMapData[r];
            for (int c = 0; c < line.length(); c++) {
                char tileType = line.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileType) {
                    case '1':
                        brick.add(new GameObject(brick_wall_img, x, y, tileSize, tileSize));
                        break;
                    case '5':
                        // This bot has a speed of 1
                        bots.add(new Bot(bot_tank_img, x, y, tileSize, tileSize, 1));
                        break;
                    case '7':
                        // The player has a speed of 2
                        player = new Player(player_tank_img, x, y, tileSize, tileSize, 2);
                        break;
                }
            }
        }
    }
    
    // --- Game Logic ---
    public void gameUpdate() {
        if (player != null) playerMove();
        moveMissiles();
        checkCollisions();
    }

    public void playerMove() {
        int nextX = player.x + player.velocityX;
        int nextY = player.y + player.velocityY;
        Rectangle nextPos = new Rectangle(nextX, nextY, player.width, player.height);
        if (!checkWallCollision(nextPos)) {
            player.x = nextX;
            player.y = nextY;
        }
    }

    public void moveMissiles() {
        for (Missile m : missiles) {
            m.move();
        }
    }

    public void checkCollisions() {
        // Use an Iterator to safely remove items from a list while looping
        Iterator<Missile> missileIter = missiles.iterator();
        while (missileIter.hasNext()) {
            Missile m = missileIter.next();
            Rectangle missileBounds = m.getBounds();
            boolean missileHit = false;

            // Check collision with brick walls
            Iterator<GameObject> brickIter = brick.iterator();
            while (brickIter.hasNext()) {
                GameObject b = brickIter.next();
                if (missileBounds.intersects(b.getBounds())) {
                    brickIter.remove(); // Destroy the brick
                    missileHit = true;
                    break; 
                }
            }

            if (missileHit) {
                missileIter.remove(); // Destroy the missile
                continue; // Move to the next missile
            }
            
            // Check collision with steel walls (missile destroyed, wall is not)
            for (GameObject s : steel) {
                if (missileBounds.intersects(s.getBounds())) {
                    missileHit = true;
                    break;
                }
            }

            if (missileHit) {
                missileIter.remove();
            }
        }
    }

    public boolean checkWallCollision(Rectangle nextPos) {
        for (GameObject b : brick) if (nextPos.intersects(b.getBounds())) return true;
        for (GameObject s : steel) if (nextPos.intersects(s.getBounds())) return true;
        if (nextPos.x < 0 || nextPos.x + nextPos.width > boardWidth ||
            nextPos.y < 0 || nextPos.y + nextPos.height > boardHeight) return true;
        return false;
    }
    
    public void shoot() {
        if (player == null) return;
        // Create a new missile at the player's center, moving in the player's current direction
        int missileX = player.x + tileSize / 2 - 4; // Center the missile
        int missileY = player.y + tileSize / 2 - 4;
        missiles.add(new Missile(missile_img, missileX, missileY, 8, 8, player.direction));
    }

    // --- Drawing ---
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (player != null) g.drawImage(player.image, player.x, player.y, player.width, player.height, this);
        for (GameObject b : brick) g.drawImage(b.image, b.x, b.y, b.width, b.height, this);
        for (Bot b : bots) g.drawImage(b.image, b.x, b.y, b.width, b.height, this);
        for (GameObject s : steel) g.drawImage(s.image, s.x, s.y, s.width, s.height, this);
        for (Missile m : missiles) g.drawImage(m.image, m.x, m.y, m.width, m.height, this);
    }

    // --- Event Listeners ---
    @Override
    public void actionPerformed(ActionEvent e) {
        gameUpdate();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (player == null) return;
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            player.velocityY = -player.speed;
            player.direction = "UP";
        } else if (key == KeyEvent.VK_DOWN) {
            player.velocityY = player.speed;
            player.direction = "DOWN";
        } else if (key == KeyEvent.VK_LEFT) {
            player.velocityX = -player.speed;
            player.direction = "LEFT";
        } else if (key == KeyEvent.VK_RIGHT) {
            player.velocityX = player.speed;
            player.direction = "RIGHT";
        } else if (key == KeyEvent.VK_SPACE) {
            shoot();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (player == null) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) player.velocityY = 0;
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) player.velocityX = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
