import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * Main game panel coordinating game loop ticks, input handling, rendering,
 * and board state. Implements GameContext to provide safe environment access to entities.
 */
public class Tank extends JPanel implements ActionListener, KeyListener, GameContext {

    // --- Panel Dimensions & Configuration ---
    private final int tileSize = 32;
    private final int boardWidth = 15 * tileSize;
    private final int boardHeight = 15 * tileSize;

    // --- Sprites ---
    private Image player_up, player_down, player_left, player_right;
    private Image bot_up, bot_down, bot_left, bot_right;
    private Image bot_fast_up, bot_fast_down, bot_fast_left, bot_fast_right;
    private Image bot_heavy_up, bot_heavy_down, bot_heavy_left, bot_heavy_right;
    private Image brick_img, steel_img, missile_img;

    // --- Level and Game State ---
    private LevelData map;
    private int currentLevel = 1;
    private int playerLives = 3;
    private boolean isPaused = false;
    private boolean showGrid = true;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // --- Entities Collections ---
    private HashSet<Wall> brick;
    private HashSet<Wall> steel;
    private HashSet<Bot> bots;
    private ArrayList<Missile> missiles;
    private ArrayList<ScentNode> scentTrail;
    private Player player;

    // --- Game Loop Timer ---
    private Timer gameLoop;

    // --- Event Listeners (Observer Pattern) ---
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    // --- Movement Input State ---
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private String latestDirection = "";

    public Tank() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        loadSprites();

        brick = new HashSet<>();
        steel = new HashSet<>();
        bots = new HashSet<>();
        missiles = new ArrayList<>();
        scentTrail = new ArrayList<>();

        map = new LevelData();
        gameLoop = new Timer(10, this);
        gameLoop.start();
    }

    private void loadSprites() {
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
    }

    // --- Observer Pattern Methods ---
    public void addGameEventListener(GameEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public void removeGameEventListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }

    private void notifyLivesChanged(int lives) {
        for (GameEventListener l : eventListeners) l.onLivesChanged(lives);
    }

    private void notifyLevelChanged(int level) {
        for (GameEventListener l : eventListeners) l.onLevelChanged(level);
    }

    private void notifyGridToggled(boolean grid) {
        for (GameEventListener l : eventListeners) l.onGridToggled(grid);
    }

    private void notifyPauseToggled(boolean paused) {
        for (GameEventListener l : eventListeners) l.onPauseToggled(paused);
    }

    private void notifyGameOver() {
        for (GameEventListener l : eventListeners) l.onGameOver();
    }

    private void notifyGameWon() {
        for (GameEventListener l : eventListeners) l.onGameWon();
    }

    /**
     * Backward-compatible wiring for the sidebar UI panel.
     */
    public void setUiPanel(UIPanel ui) {
        addGameEventListener(ui);
        notifyGridToggled(showGrid);
        loadLevel(currentLevel);
    }

    public void loadLevel(int level) {
        if (level > map.getTotalLevels()) {
            gameWon = true;
            notifyGameWon();
            return;
        }

        String[] currentMapData = map.getMap(level);

        resetInputState();
        brick = new HashSet<>();
        steel = new HashSet<>();
        bots = new HashSet<>();
        missiles = new ArrayList<>();
        scentTrail = new ArrayList<>();
        player = null;
        gameOver = false;
        gameWon = false;

        for (int r = 0; r < currentMapData.length; r++) {
            String line = currentMapData[r];
            for (int c = 0; c < line.length(); c++) {
                char tileType = line.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;
                switch (tileType) {
                    case '1':
                        brick.add(new Wall(brick_img, x, y, tileSize, tileSize, true));
                        break;
                    case '9':
                        steel.add(new Wall(steel_img, x, y, tileSize, tileSize, false));
                        break;
                    case '7':
                        player = new Player(player_up, player_down, player_left, player_right, x, y, tileSize, tileSize, 4);
                        break;
                    case '5':
                        bots.add(new Bot(bot_up, bot_down, bot_left, bot_right, x, y, tileSize, tileSize, 2));
                        break;
                    case '6':
                        bots.add(new BotFast(bot_fast_up, bot_fast_down, bot_fast_left, bot_fast_right, x, y, tileSize, tileSize, 3));
                        break;
                    case '8':
                        bots.add(new BotHeavy(bot_heavy_up, bot_heavy_down, bot_heavy_left, bot_heavy_right, x, y, tileSize, tileSize, 1));
                        break;
                    default:
                        break;
                }
            }
        }

        notifyLevelChanged(currentLevel);
        notifyLivesChanged(playerLives);
    }

    // --- Game Loop Update ---
    public void gameUpdate() {
        if (gameOver || gameWon || isPaused || scentTrail == null || brick == null) return;

        if (player != null) {
            player.update(this, getActiveInputDirection());
        }

        updateScentTrail();

        for (Bot bot : bots) {
            bot.update(this, missile_img);
        }

        moveMissiles();
        checkCollisions();

        if (bots.isEmpty() && player != null) {
            currentLevel++;
            loadLevel(currentLevel);
        }
    }

    private void updateScentTrail() {
        Iterator<ScentNode> iter = scentTrail.iterator();
        while (iter.hasNext()) {
            ScentNode scent = iter.next();
            scent.decay();
            if (scent.isExpired()) {
                iter.remove();
            }
        }
    }

    public void moveMissiles() {
        for (Missile m : missiles) {
            m.move();
        }
    }

    public void checkCollisions() {
        List<Missile> missilesToRemove = new ArrayList<>();
        List<Wall> bricksToRemove = new ArrayList<>();
        List<Bot> botsToRemove = new ArrayList<>();

        for (Missile m : missiles) {
            Rectangle missileBounds = m.getBounds();

            for (Wall b : brick) {
                if (missileBounds.intersects(b.getBounds())) {
                    missilesToRemove.add(m);
                    b.takeHit();
                    if (b.isDestroyed()) {
                        bricksToRemove.add(b);
                    }
                }
            }

            for (Wall s : steel) {
                if (missileBounds.intersects(s.getBounds())) {
                    missilesToRemove.add(m);
                }
            }

            // Player hit logic
            if (!m.isFromPlayer() && player != null && missileBounds.intersects(player.getBounds())) {
                missilesToRemove.add(m);
                playerLives--;
                notifyLivesChanged(playerLives);
                if (playerLives <= 0) {
                    player = null;
                    gameOver = true;
                    notifyGameOver();
                } else {
                    loadLevel(currentLevel);
                }
            }

            // Bot hit logic
            if (m.isFromPlayer()) {
                for (Bot b : bots) {
                    if (missileBounds.intersects(b.getBounds())) {
                        missilesToRemove.add(m);
                        b.takeHit();
                        if (b.isDestroyed()) {
                            botsToRemove.add(b);
                        }
                    }
                }
            }
        }

        missiles.removeAll(missilesToRemove);
        brick.removeAll(bricksToRemove);
        bots.removeAll(botsToRemove);
    }

    // --- GameContext Implementation ---
    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public int getBoardWidth() {
        return boardWidth;
    }

    @Override
    public int getBoardHeight() {
        return boardHeight;
    }

    @Override
    public boolean checkWallCollision(Rectangle nextPos) {
        if (nextPos == null) return false;
        if (brick != null) {
            for (Wall b : brick) {
                if (nextPos.intersects(b.getBounds())) return true;
            }
        }
        if (steel != null) {
            for (Wall s : steel) {
                if (nextPos.intersects(s.getBounds())) return true;
            }
        }
        if (nextPos.x < 0 || nextPos.x + nextPos.width > boardWidth ||
            nextPos.y < 0 || nextPos.y + nextPos.height > boardHeight) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasLineOfSight(Rectangle sightLine) {
        if (sightLine == null) return false;
        if (brick != null) {
            for (Wall b : brick) {
                if (sightLine.intersects(b.getBounds())) return false;
            }
        }
        if (steel != null) {
            for (Wall s : steel) {
                if (sightLine.intersects(s.getBounds())) return false;
            }
        }
        return true;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public List<ScentNode> getScentTrail() {
        return scentTrail;
    }

    @Override
    public void spawnMissile(Missile missile) {
        if (missile != null) {
            missiles.add(missile);
        }
    }

    @Override
    public void registerScent(int gridX, int gridY) {
        if (scentTrail == null) return;
        for (ScentNode scent : scentTrail) {
            if (scent.getX() == gridX && scent.getY() == gridY) {
                scent.setStrength(300);
                return;
            }
        }
        scentTrail.add(new ScentNode(gridX, gridY, 300));
    }

    public void shoot() {
        if (player != null) {
            player.shoot(this, missile_img);
        }
    }

    // --- Control Methods ---
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            gameLoop.stop();
        } else {
            gameLoop.start();
        }
        notifyPauseToggled(isPaused);
    }

    public void toggleGrid() {
        showGrid = !showGrid;
        notifyGridToggled(showGrid);
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
            if ("UP".equals(latestDirection) && upPressed) return "UP";
            if ("DOWN".equals(latestDirection) && downPressed) return "DOWN";
            if ("LEFT".equals(latestDirection) && leftPressed) return "LEFT";
            if ("RIGHT".equals(latestDirection) && rightPressed) return "RIGHT";
        }
        if (upPressed) return "UP";
        if (downPressed) return "DOWN";
        if (leftPressed) return "LEFT";
        if (rightPressed) return "RIGHT";
        return null;
    }

    public void resetGame() {
        currentLevel = 1;
        playerLives = 3;
        resetInputState();
        loadLevel(currentLevel);
        if (isPaused) {
            togglePause();
        }
    }

    // --- Polymorphic Rendering ---
    @Override
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

        // Polymorphic drawing via Drawable interface
        if (player != null) player.draw(g, this);
        if (brick != null) for (Wall b : brick) b.draw(g, this);
        if (steel != null) for (Wall s : steel) s.draw(g, this);
        if (bots != null) for (Bot b : bots) b.draw(g, this);
        if (missiles != null) for (Missile m : missiles) m.draw(g, this);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", boardWidth / 2 - 150, boardHeight / 2);
        } else if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("YOU WIN THE GAME!", boardWidth / 2 - 200, boardHeight / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameUpdate();
        repaint();
    }

    // --- KeyListener Implementation ---
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
            player.handleDirectionInput("UP", this);
        } else if (key == KeyEvent.VK_DOWN) {
            downPressed = true;
            latestDirection = "DOWN";
            player.handleDirectionInput("DOWN", this);
        } else if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
            latestDirection = "LEFT";
            player.handleDirectionInput("LEFT", this);
        } else if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
            latestDirection = "RIGHT";
            player.handleDirectionInput("RIGHT", this);
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

    @Override
    public void keyTyped(KeyEvent e) {}
}