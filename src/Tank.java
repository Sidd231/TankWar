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
            imgUp = u; 
            imgDown = d; 
            imgLeft = l; 
            imgRight = r;
            speed = s;
        }

        public void setDirection(String newDir) {
            direction = newDir;
            switch (direction) {
                case "UP": image = imgUp; 
                break;
                case "DOWN": image = imgDown; 
                break;
                case "LEFT": image = imgLeft; 
                break;
                case "RIGHT": image = imgRight; 
                break;
            }
        }
        
        public void snapToGrid() {
            if (velocityY != 0) {
                int gridX = Math.round((float)x / tileSize);
                this.x = gridX * tileSize;
            }
            
            if (velocityX != 0) {
                int gridY = Math.round((float)y / tileSize);
                this.y = gridY * tileSize;
            }
        }
    }

    class Player extends TankObject {
        int targetX, targetY;
        boolean isMoving = false;

        Player(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
            targetX = x;
            targetY = y;
            isMoving = false;
        }
    }

    class ScentNode {
        int x, y;
        int strength;
        ScentNode(int x, int y, int strength) { this.x = x; this.y = y; this.strength = strength; }
    }

    class Bot extends TankObject {
        enum AIState { WANDERING, HUNTING }
        protected AIState currentState = AIState.WANDERING;
        protected int moveTimer = 0, shootCooldown = 100;
        protected Random random = new Random();
        private Point lastKnownPlayerPosition;

        Bot(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) {
            super(u, d, l, r, x, y, w, h, s);
        }

        public void update() {
            if (hasLineOfSight()) {
                currentState = AIState.HUNTING;
                if (player != null) lastKnownPlayerPosition = new Point(player.x, player.y);
            }

            if (currentState == AIState.HUNTING) {
                moveTowards(lastKnownPlayerPosition);
                if (getBounds().contains(lastKnownPlayerPosition)) currentState = AIState.WANDERING;
            } else { 
                moveTimer--;
                if (moveTimer <= 0) {
                    changeRandomDirection();
                    moveTimer = 50 + random.nextInt(100);
                }
            }
            
            
            int nextX = x + velocityX;
            int nextY = y + velocityY;
            if (!checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
                x = nextX;
                y = nextY;
            } else {
                
                changeRandomDirection();
            }

            shootCooldown--;
            if (shootCooldown <= 0 && hasLineOfSight()) shoot();
        }

        protected void moveTowards(Point target) {
            if (target == null) return;
            snapToGrid(); 
            int dx = target.x - x;
            int dy = target.y - y;

            if (Math.abs(dx) > Math.abs(dy)) { // Move horizontally
                if (dx > 0) { setDirection("RIGHT"); velocityX = speed; velocityY = 0; } 
                else { setDirection("LEFT"); velocityX = -speed; velocityY = 0; }
            } else { // Move vertically
                if (dy > 0) { setDirection("DOWN"); velocityY = speed; velocityX = 0; } 
                else { setDirection("UP"); velocityY = -speed; velocityX = 0; }
            }
        }

        protected void changeRandomDirection() {
            snapToGrid(); // Snap to lane before changing direction
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
        }

        protected boolean hasLineOfSight() {
            if (player == null) return false;
            Rectangle sightLine = new Rectangle();
            boolean aligned = false;
            if (Math.abs(player.x - x) < tileSize) { aligned = true; if (player.y < y) { sightLine.setBounds(x, player.y, width, y - player.y); } else { sightLine.setBounds(x, y, width, player.y - y); } } 
            else if (Math.abs(player.y - y) < tileSize) { aligned = true; if (player.x < x) { sightLine.setBounds(player.x, y, x - player.x, height); } else { sightLine.setBounds(x, y, player.x - x, height); } }
            if (!aligned) return false;
            for (GameObject wall : brick) if (sightLine.intersects(wall.getBounds())) return false;
            for (GameObject wall : steel) if (sightLine.intersects(wall.getBounds())) return false;
            return true;
        }

        public void shoot() { missiles.add(new Missile(missile_img, x + tileSize / 2 - 4, y + tileSize / 2 - 4, 8, 8, direction, false)); shootCooldown = 100 + random.nextInt(100); }
        public void takeHit() { botsToRemove.add(this); }
    }
    
    class BotFast extends Bot {
        BotFast(Image u, Image d, Image l, Image r, int x, int y, int w, int h, int s) { super(u, d, l, r, x, y, w, h, s); }
        @Override public void update() {
            ScentNode targetScent = findStrongestAdjacentScent();
            if (targetScent != null) {
                currentState = AIState.HUNTING;
                moveTowards(new Point(targetScent.x * tileSize, targetScent.y * tileSize));
            } else {
                currentState = AIState.WANDERING;
                moveTimer--;
                if (moveTimer <= 0) {
                    changeRandomDirection();
                    moveTimer = 20 + random.nextInt(50);
                }
            }
            
            int nextX = x + velocityX;
            int nextY = y + velocityY;
            if (!checkWallCollision(new Rectangle(nextX, nextY, width, height))) {
                x = nextX;
                y = nextY;
            } else {
                changeRandomDirection();
            }

            shootCooldown--;
            if (shootCooldown <= 0 && hasLineOfSight()) shoot();
        }
        private ScentNode findStrongestAdjacentScent() {
            int currentX = x / tileSize; int currentY = y / tileSize;
            ScentNode bestScent = null;
            for (ScentNode scent : scentTrail) {
                if (Math.abs(scent.x - currentX) <= 1 && Math.abs(scent.y - currentY) <= 1) {
                    if (bestScent == null || scent.strength > bestScent.strength) bestScent = scent;
                }
            }
            return bestScent;
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
    ArrayList<ScentNode> scentTrail;
    ArrayList<Missile> missilesToRemove;
    ArrayList<GameObject> bricksToRemove;
    ArrayList<Bot> botsToRemove;
    Timer gameLoop;
    boolean gameOver = false, gameWon = false;
    
    // --- NEW: Game State Variables ---
    private UIPanel uiPanel;
    private int playerLives = 3;
    private boolean isPaused = false;
    private boolean showGrid = true;

    // --- Grid Movement Input State ---
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private String latestDirection = "";

    Tank() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        try {
            player_up = App.loadImage("image/ptank_up.png");
            player_down = App.loadImage("image/ptank_down.png");
            player_left = App.loadImage("image/ptank_left.png");
            player_right = App.loadImage("image/ptank_right.png");
            bot_up = App.loadImage("image/btank_up.png");
            bot_down = App.loadImage("image/btank_down.png");
            bot_left = App.loadImage("image/btank_left.png");
            bot_right = App.loadImage("image/btank_right.png");
            bot_fast_up = App.loadImage("image/btank_fast_up.png");
            bot_fast_down = App.loadImage("image/btank_fast_down.png");
            bot_fast_left = App.loadImage("image/btank_fast_left.png");
            bot_fast_right = App.loadImage("image/btank_fast_right.png");
            bot_heavy_up = App.loadImage("image/btank_heavy_up.png");
            bot_heavy_down = App.loadImage("image/btank_heavy_down.png");
            bot_heavy_left = App.loadImage("image/btank_heavy_left.png");
            bot_heavy_right = App.loadImage("image/btank_heavy_right.png");
            brick_img = App.loadImage("image/brick.jpg");
            steel_img = App.loadImage("image/steel.jpg");
            missile_img = App.loadImage("image/missile.png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        brick = new HashSet<>();
        steel = new HashSet<>();
        bots = new HashSet<>();
        missiles = new ArrayList<>();
        scentTrail = new ArrayList<>();

        map = new LevelData();
        gameLoop = new Timer(10, this);
        gameLoop.start();
    }
    
    // --- NEW: Method to link to the UI Panel ---
    public void setUiPanel(UIPanel ui) {
        this.uiPanel = ui;
        ui.setGridStatus(showGrid);
        loadLevel(currentLevel); // Load the first level after the UI is set
    }

    public void loadLevel(int level) {
        if (level > map.getTotalLevels()) {
            gameWon = true;
            return;
        }
        String[] currentMapData = map.getMap(level);
        
        resetInputState();
        brick = new HashSet<>(); steel = new HashSet<>();
        bots = new HashSet<>(); missiles = new ArrayList<>();
        scentTrail = new ArrayList<>();
        player = null; gameOver = false; gameWon = false;

        for (int r = 0; r < currentMapData.length; r++) {
            String line = currentMapData[r];
            for (int c = 0; c < line.length(); c++) {
                char tileType = line.charAt(c);
                int x = c * tileSize, y = r * tileSize;
                switch (tileType) {
                    case '1': brick.add(new GameObject(brick_img, x, y, tileSize, tileSize)); break;
                    case '9': steel.add(new GameObject(steel_img, x, y, tileSize, tileSize)); break;
                    case '7': player = new Player(player_up, player_down, player_left, player_right, x, y, tileSize, tileSize, 4); break; // Increased player speed
                    case '5': bots.add(new Bot(bot_up, bot_down, bot_left, bot_right, x, y, tileSize, tileSize, 2)); break; // Increased bot speed
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
        if (gameOver || gameWon || isPaused || scentTrail == null || brick == null) return;
        if (player != null) playerMove();
        updateScentTrail();
        for (Bot bot : bots) bot.update();
        moveMissiles();
        checkCollisions();
        if (bots.isEmpty() && player != null) { currentLevel++; loadLevel(currentLevel); }
    }
    
    private void dropScent() {
        int playerGridX = player.x / tileSize; int playerGridY = player.y / tileSize;
        for (ScentNode scent : scentTrail) {
            if (scent.x == playerGridX && scent.y == playerGridY) {
                scent.strength = 300; return;
            }
        }
        scentTrail.add(new ScentNode(playerGridX, playerGridY, 300));
    }

    private void updateScentTrail() {
        Iterator<ScentNode> iter = scentTrail.iterator();
        while (iter.hasNext()) {
            ScentNode scent = iter.next();
            scent.strength--;
            if (scent.strength <= 0) iter.remove();
        }
    }

    public void playerMove() {
        if (player == null) return;

        if (player.isMoving) {
            int dx = player.targetX - player.x;
            int dy = player.targetY - player.y;

            if (dx != 0) {
                int step = Math.min(Math.abs(dx), player.speed) * Integer.signum(dx);
                player.x += step;
            }
            if (dy != 0) {
                int step = Math.min(Math.abs(dy), player.speed) * Integer.signum(dy);
                player.y += step;
            }

            dropScent();

            if (player.x == player.targetX && player.y == player.targetY) {
                player.isMoving = false;
                checkNextPlayerMove();
            }
        } else {
            checkNextPlayerMove();
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
    
    public void shoot() {
        if (player == null) return;
        missiles.add(new Missile(missile_img, player.x + tileSize / 2 - 4, player.y + tileSize / 2 - 4, 8, 8, player.direction, true));
    }

    // --- NEW: Game Control Methods ---
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            gameLoop.stop();
            uiPanel.setPauseIcon(true);
        } else {
            gameLoop.start();
            uiPanel.setPauseIcon(false);
        }
    }

    public void toggleGrid() {
        showGrid = !showGrid;
        if (uiPanel != null) {
            uiPanel.setGridStatus(showGrid);
        }
        repaint();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    private void resetInputState() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        latestDirection = "";
    }

    private String getActiveInputDirection() {
        if (latestDirection != null && !latestDirection.isEmpty()) {
            if (latestDirection.equals("UP") && upPressed) return "UP";
            if (latestDirection.equals("DOWN") && downPressed) return "DOWN";
            if (latestDirection.equals("LEFT") && leftPressed) return "LEFT";
            if (latestDirection.equals("RIGHT") && rightPressed) return "RIGHT";
        }
        if (upPressed) return "UP";
        if (downPressed) return "DOWN";
        if (leftPressed) return "LEFT";
        if (rightPressed) return "RIGHT";
        return null;
    }

    private void handlePlayerDirectionInput(String dir) {
        if (player == null) return;

        // If player is already moving between tiles:
        if (player.isMoving) {
            // Check if player wants to reverse back to the previous tile
            boolean isReverse = false;
            int revX = player.targetX;
            int revY = player.targetY;

            if (player.direction.equals("UP") && dir.equals("DOWN")) {
                revY += tileSize;
                isReverse = true;
            } else if (player.direction.equals("DOWN") && dir.equals("UP")) {
                revY -= tileSize;
                isReverse = true;
            } else if (player.direction.equals("LEFT") && dir.equals("RIGHT")) {
                revX += tileSize;
                isReverse = true;
            } else if (player.direction.equals("RIGHT") && dir.equals("LEFT")) {
                revX -= tileSize;
                isReverse = true;
            }

            if (isReverse) {
                player.targetX = revX;
                player.targetY = revY;
                player.setDirection(dir);
            }
            return;
        }

        // Tank is stopped at a grid cell
        player.setDirection(dir);
        int stepX = 0, stepY = 0;
        switch (dir) {
            case "UP": stepY = -tileSize; break;
            case "DOWN": stepY = tileSize; break;
            case "LEFT": stepX = -tileSize; break;
            case "RIGHT": stepX = tileSize; break;
        }

        int nextX = player.x + stepX;
        int nextY = player.y + stepY;

        if (!checkWallCollision(new Rectangle(nextX, nextY, player.width, player.height))) {
            player.targetX = nextX;
            player.targetY = nextY;
            player.isMoving = true;
        }
    }

    private void checkNextPlayerMove() {
        if (player == null || player.isMoving) return;

        String dir = getActiveInputDirection();
        if (dir == null) return;

        player.setDirection(dir);
        int stepX = 0, stepY = 0;
        switch (dir) {
            case "UP": stepY = -tileSize; break;
            case "DOWN": stepY = tileSize; break;
            case "LEFT": stepX = -tileSize; break;
            case "RIGHT": stepX = tileSize; break;
        }

        int nextX = player.x + stepX;
        int nextY = player.y + stepY;

        if (!checkWallCollision(new Rectangle(nextX, nextY, player.width, player.height))) {
            player.targetX = nextX;
            player.targetY = nextY;
            player.isMoving = true;
        }
    }

    public void resetGame() {
        currentLevel = 1;
        playerLives = 3;
        resetInputState();
        loadLevel(currentLevel);
        if (isPaused) {
            togglePause(); // Unpause the game on reset
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (showGrid) {
            g.setColor(new Color(255, 255, 255, 30));
            for (int x = 0; x <= boardWidth; x += tileSize) {
                g.drawLine(x, 0, x, boardHeight);
            }
            for (int y = 0; y <= boardHeight; y += tileSize) {
                g.drawLine(0, y, boardWidth, y);
            }
        }

        if (player != null && player.image != null) g.drawImage(player.image, player.x, player.y, player.width, player.height, this);
        if (brick != null) for (GameObject b : brick) if (b != null && b.image != null) g.drawImage(b.image, b.x, b.y, b.width, b.height, this);
        if (bots != null) for (Bot b : bots) if (b != null && b.image != null) g.drawImage(b.image, b.x, b.y, b.width, b.height, this);
        if (steel != null) for (GameObject s : steel) if (s != null && s.image != null) g.drawImage(s.image, s.x, s.y, s.width, s.height, this);
        if (missiles != null) for (Missile m : missiles) if (m != null && m.image != null) g.drawImage(m.image, m.x, m.y, m.width, m.height, this);

        if (gameOver) {
            g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", boardWidth / 2 - 150, boardHeight / 2);
        } else if (gameWon) {
            g.setColor(Color.GREEN); g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("YOU WIN THE GAME!", boardWidth / 2 - 200, boardHeight / 2);
        }
    }

    @Override public void actionPerformed(ActionEvent e) { gameUpdate(); repaint(); }

    @Override
    public void keyPressed(KeyEvent e) {
        if (player == null || isPaused) return;
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_SPACE) {
            shoot();
            return;
        }

        if (key == KeyEvent.VK_G) {
            toggleGrid();
            return;
        }

        if (key == KeyEvent.VK_UP) {
            upPressed = true;
            latestDirection = "UP";
            handlePlayerDirectionInput("UP");
        } else if (key == KeyEvent.VK_DOWN) {
            downPressed = true;
            latestDirection = "DOWN";
            handlePlayerDirectionInput("DOWN");
        } else if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
            latestDirection = "LEFT";
            handlePlayerDirectionInput("LEFT");
        } else if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
            latestDirection = "RIGHT";
            handlePlayerDirectionInput("RIGHT");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) upPressed = false;
        else if (key == KeyEvent.VK_DOWN) downPressed = false;
        else if (key == KeyEvent.VK_LEFT) leftPressed = false;
        else if (key == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
}