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
            bot_heavy_down = new ImageIcon("../image/btank_heavy_down.png").getImage();
            bot_heavy_left = new ImageIcon("../image/btank_heavy_left.png").getImage();
            bot_heavy_right = new ImageIcon("../image/btank_heavy_right.png").getImage();
            brick_img = new ImageIcon("../image/brick.jpg").getImage();
            steel_img = new ImageIcon("../image/steel.jpg").getImage();
            missile_img = new ImageIcon("../image/missile.png").getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        map = new LevelData();
        gameLoop = new Timer(10, this);
        gameLoop.start();
    }
    
    // --- NEW: Method to link to the UI Panel ---
    public void setUiPanel(UIPanel ui) {
        this.uiPanel = ui;
        loadLevel(currentLevel); // Load the first level after the UI is set
    }

    public void loadLevel(int level) {
        if (level > map.getTotalLevels()) {
            gameWon = true;
            return;
        }
        String[] currentMapData = map.getMap(level);
        
        brick = new HashSet<>(); steel = new HashSet<>();
        bots = new HashSet<>(); missiles = new ArrayList<>();
        player = null;
        gameOver = false; gameWon = false;

        for (int r = 0; r < currentMapData.length; r++) {
            String line = currentMapData[r];
            for (int c = 0; c < line.length(); c++) {
                char tileType = line.charAt(c);
                int x = c * tileSize, y = r * tileSize;
                switch (tileType) {
                    case '1': brick.add(new GameObject(brick_img, x, y, tileSize, tileSize)); break;
                    case '9': steel.add(new GameObject(steel_img, x, y, tileSize, tileSize)); break;
                    case '7': player = new Player(player_up, player_down, player_left, player_right, x, y, tileSize, tileSize, 2); break;
                    case '5': bots.add(new Bot(bot_up, bot_down, bot_left, bot_right, x, y, tileSize, tileSize, 1)); break;
                    case '6': bots.add(new BotFast(bot_fast_up, bot_fast_down, bot_fast_left, bot_fast_right, x, y, tileSize, tileSize, 3)); break;
                    case '8': bots.add(new BotHeavy(bot_heavy_up, bot_heavy_down, bot_heavy_left, bot_heavy_right, x, y, tileSize, tileSize, 1)); break;
                }
            }
        }
        // Update UI after loading
        if (uiPanel != null) {
            uiPanel.setLevel(currentLevel);
            uiPanel.setLives(playerLives);
        }
    }
    
    public void gameUpdate() {
        if (gameOver || gameWon) return;
        if (player != null) playerMove();
        for (Bot bot : bots) bot.update();
        moveMissiles();
        checkCollisions();

        if (bots.isEmpty() && player != null) {
            currentLevel++;
            loadLevel(currentLevel);
        }
    }

    public void playerMove() {
        int nextX = player.x + player.velocityX, nextY = player.y + player.velocityY;
        if (!checkWallCollision(new Rectangle(nextX, nextY, player.width, player.height))) {
            player.x = nextX; player.y = nextY;
        }
    }

    public void moveMissiles() { for (Missile m : missiles) m.move(); }

    public void checkCollisions() {
        missilesToRemove = new ArrayList<>();
        bricksToRemove = new ArrayList<>();
        botsToRemove = new ArrayList<>();

        for (Missile m : missiles) {
            Rectangle missileBounds = m.getBounds();
            for (GameObject b : brick) if (missileBounds.intersects(b.getBounds())) { missilesToRemove.add(m); bricksToRemove.add(b); }
            for (GameObject s : steel) if (missileBounds.intersects(s.getBounds())) missilesToRemove.add(m);
            
            // --- UPDATED: Player hit logic ---
            if (!m.fromPlayer && player != null && missileBounds.intersects(player.getBounds())) {
                missilesToRemove.add(m);
                playerLives--;
                uiPanel.setLives(playerLives);
                if (playerLives <= 0) {
                    player = null;
                    gameOver = true;
                } else {
                    // Respawn on the same level
                    loadLevel(currentLevel);
                }
            }
            if (m.fromPlayer) {
                for (Bot b : bots) {
                    if (missileBounds.intersects(b.getBounds())) {
                        missilesToRemove.add(m);
                        b.takeHit();
                    }
                }
            }
        }
        missiles.removeAll(missilesToRemove);
        brick.removeAll(bricksToRemove);
        bots.removeAll(botsToRemove);
    }

    public boolean checkWallCollision(Rectangle nextPos) {
        for (GameObject b : brick) if (nextPos.intersects(b.getBounds())) return true;
        for (GameObject s : steel) if (nextPos.intersects(s.getBounds())) return true;
        if (nextPos.x < 0 || nextPos.x + nextPos.width > boardWidth ||
            nextPos.y < 0 || nextPos.y + nextPos.height > boardHeight) return true;
        return false;
    }
