import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class Tank extends JPanel implements ActionListener, KeyListener {

    // --- Class Hierarchy ---
    class GameObject {
        int x, y, width, height;
        Image image;
        GameObject(Image i, int x, int y, int w, int h) { image = i; this.x = x; this.y = y; width = w; height = h; }
        public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }

    class TankObject extends GameObject {
        int speed;
        int velocityX = 0, velocityY = 0;
        String direction = "UP";
        Image imgUp, imgDown, imgLeft, imgRight;

        TankObject(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, x, y, w, h);
            imgUp = u; imgDown = d; imgLeft = l; imgRight = r;
            speed = s;
        }

        public void setDirection(String newDir) {
            direction = newDir;
            switch (direction) {
                case "UP": image = imgUp; break;
                case "DOWN": image = imgDown; break;
                case "LEFT": image = imgLeft; break;
                case "RIGHT": image = imgRight; break;
            }
        }
    }

    class Player extends TankObject {
        Player(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
        }
    }

    class Bot extends TankObject {
        private int moveTimer = 0, shootCooldown = 100;
        private Random random = new Random();

        Bot(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
            changeDirection();
        }

        public void update() {
            moveTimer--;
            if (moveTimer <= 0) changeDirection();
            shootCooldown--;
            if (shootCooldown <= 0 && hasLineOfSight()) shoot();
            
            int nextX = x + velocityX, nextY = y + velocityY;
            if (!checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
                x = nextX; y = nextY;
            } else {
                changeDirection();
            }
        }

        private boolean hasLineOfSight() {
            if (player == null) return false;
            Rectangle sightLine = new Rectangle();
            boolean aligned = false;
            if (Math.abs(player.x - x) < tileSize) {
                aligned = true;
                if (player.y < y) { if (!direction.equals("UP")) return false; sightLine.setBounds(x, player.y, width, y - player.y); } 
                else { if (!direction.equals("DOWN")) return false; sightLine.setBounds(x, y, width, player.y - y); }
            } else if (Math.abs(player.y - y) < tileSize) {
                aligned = true;
                if (player.x < x) { if (!direction.equals("LEFT")) return false; sightLine.setBounds(player.x, y, x - player.x, height); } 
                else { if (!direction.equals("RIGHT")) return false; sightLine.setBounds(x, y, player.x - x, height); }
            }
            if (!aligned) return false;
            for (GameObject wall : brick) if (sightLine.intersects(wall.getBounds())) return false;
            for (GameObject wall : steel) if (sightLine.intersects(wall.getBounds())) return false;
            return true;
        }

        public void shoot() {
            missiles.add(new Missile(missile_img, x + tileSize / 2 - 4, y + tileSize / 2 - 4, 8, 8, direction, false));
            shootCooldown = 100 + random.nextInt(100);
        }

        private void changeDirection() {
            int dir = random.nextInt(5);
            String newDir = direction;
            switch (dir) {
                case 0: velocityY = -speed; velocityX = 0; newDir = "UP"; break;
                case 1: velocityY = speed; velocityX = 0; newDir = "DOWN"; break;
                case 2: velocityX = -speed; velocityY = 0; newDir = "LEFT"; break;
                case 3: velocityX = speed; velocityY = 0; newDir = "RIGHT"; break;
                case 4: velocityX = 0; velocityY = 0; break;
            }
            setDirection(newDir);
            moveTimer = 50 + random.nextInt(100);
        }
        
        public void takeHit() {
            botsToRemove.add(this);
        }
    }
    
    class BotFast extends Bot {
        BotFast(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
        }
    }

    class BotHeavy extends Bot {
        int health = 3;
        BotHeavy(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
        }

        @Override
        public void takeHit() {
            this.health--;
            if (this.health <= 0) {
                botsToRemove.add(this);
            }
        }
    }

    class Missile extends GameObject {
        int velocityX = 0, velocityY = 0;
        boolean fromPlayer;
        Missile(Image i, int x, int y, int w, int h, String dir, boolean p) {
            super(i, x, y, w, h);
            fromPlayer = p;
            int missileSpeed = 5;
            switch (dir) {
                case "UP": velocityY = -missileSpeed; break;
                case "DOWN": velocityY = missileSpeed; break;
                case "LEFT": velocityX = -missileSpeed; break;
                case "RIGHT": velocityX = missileSpeed; break;
            }
        }
        public void move() { x += velocityX; y += velocityY; }
    }

    // --- Panel Properties ---
    private int tileSize = 32;
    private int boardWidth = 15 * tileSize, boardHeight = 15 * tileSize;
    private Image player_up, player_down, player_left, player_right;
    private Image bot_up, bot_down, bot_left, bot_right;
    private Image bot_fast_up, bot_fast_down, bot_fast_left, bot_fast_right;
    private Image bot_heavy_up, bot_heavy_down, bot_heavy_left, bot_heavy_right;
    private Image brick_img, steel_img, missile_img;

    private LevelData map;
    private int currentLevel = 1;

    HashSet<GameObject> brick, steel;
    HashSet<Bot> bots;
    ArrayList<Missile> missiles;
    Player player;
    
    ArrayList<Missile> missilesToRemove;
    ArrayList<GameObject> bricksToRemove;
    ArrayList<Bot> botsToRemove;

    Timer gameLoop;
    boolean gameOver = false, gameWon = false;
    
    // --- NEW: Game State Variables ---
    private UIPanel uiPanel;
    private int playerLives = 3;
    private boolean isPaused = false;

    Tank() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        try {
            player_up = new ImageIcon("../image/ptank_up.png").getImage();
            player_down = new ImageIcon("../image/ptank_down.png").getImage();
            player_left = new ImageIcon("../image/ptank_left.png").getImage();
            player_right = new ImageIcon("../image/ptank_right.png").getImage();
            bot_up = new ImageIcon("../image/btank_up.png").getImage();
            bot_down = new ImageIcon("../image/btank_down.png").getImage();
            bot_left = new ImageIcon("../image/btank_left.png").getImage();
            bot_right = new ImageIcon("../image/btank_right.png").getImage();
            bot_fast_up = new ImageIcon("../image/btank_fast_up.png").getImage();
            bot_fast_down = new ImageIcon("../image/btank_fast_down.png").getImage();
            bot_fast_left = new ImageIcon("../image/btank_fast_left.png").getImage();
            bot_fast_right = new ImageIcon("../image/btank_fast_right.png").getImage();
            bot_heavy_up = new ImageIcon("../image/btank_heavy_up.png").getImage();
