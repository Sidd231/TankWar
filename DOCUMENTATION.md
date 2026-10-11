# 📖 Tank War — Complete Technical Architecture & Code Reference Manual

> **Purpose of this Document**:  
> This documentation provides an exhaustive, code-level explanation of every system, class, method, and variable in the **Tank War** project. When you return to this codebase in the future, use this guide to instantly understand how each feature works, which code is responsible for it, and how to safely modify or expand it.

---

## 📑 Table of Contents
1. [Overall Architecture & Application Lifecycle](#1-overall-architecture--application-lifecycle)
2. [Player System](#2-player-system)
3. [Enemy Bot AI System](#3-enemy-bot-ai-system)
   - [Base Bot AI & State Machine](#31-base-bot-tank-bot)
   - [Fast Bot & Scent Tracking AI](#32-fast-bot-botfast)
   - [Heavy Bot & Armor Durability](#33-heavy-armored-bot-botheavy)
4. [Scent Trail System](#4-scent-trail-system)
5. [Combat, Projectiles & Collision System](#5-combat-projectiles--collision-system)
6. [Terrain & Level System](#6-terrain--level-system)
7. [User Interface & HUD Panel](#7-user-interface--hud-panel)
8. [Level Editor System](#8-level-editor-system)
9. [A* Pathfinding Algorithm](#9-a-pathfinding-algorithm)
10. [Master Code Responsibility Lookup Table](#10-master-code-responsibility-lookup-table)

---

## 1. Overall Architecture & Application Lifecycle

The application is structured into a modular Swing/AWT desktop application.

```
                    ┌─────────────────────────┐
                    │        App.java         │
                    │   (JFrame + CardLayout) │
                    └────────────┬────────────┘
         ┌───────────────────────┼───────────────────────┐
         ▼                       ▼                       ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│ HomeScreen.java  │   │  Game Container  │   │ LevelEditor.java │
│   ("HOME")       │   │    ("GAME")      │   │   ("EDITOR")     │
└──────────────────┘   └─────────┬────────┘   └──────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
       ┌───────────────────┐           ┌───────────────────┐
       │     Tank.java     │◄─────────►│    UIPanel.java   │
       │ (Main Game Panel) │           │    (HUD Sidebar)  │
       └───────────────────┘           └───────────────────┘
```

### 1.1 Entry Point — `App.java`
- **Location**: [`src/App.java`](file:///d:/csjava/2D%20Game/tankwar/src/App.java)
- **Role**: Initializes the main application window, constructs shared panels, and controls navigation between screens using `CardLayout`.
- **Key Methods**:
  - `main(String[] args)`: Launches the UI on the Swing Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater()`.
  - `createAndShowGUI()`: Configures the 630x540 non-resizable `JFrame`. Embeds `homeScreen`, `gameContainer` (containing `tankPanel` and `uiPanel`), and `levelEditor`.
  - `showGameScreen()`: Navigates to `"GAME"`, calls `tankPanel.resetGame()`, and transfers keyboard focus using `tankPanel.requestFocusInWindow()`.
  - `showHomeScreen()`: Navigates back to the main menu (`"HOME"`).
  - `showLevelEditor()`: Opens the visual map editor (`"EDITOR"`).

### 1.2 Main Game Loop — `Tank.java`
- **Location**: [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L292-L294)
- **Clock**: A Swing `Timer` running at an interval of **10 milliseconds** (~100 ticks per second):
  ```java
  gameLoop = new Timer(10, this);
  gameLoop.start();
  ```
- **Tick Cycle**: Every 10ms, `actionPerformed(ActionEvent e)` is triggered:
  ```java
  @Override
  public void actionPerformed(ActionEvent e) {
      gameUpdate(); // 1. Updates game physics and logic
      repaint();    // 2. Triggers paintComponent() to render graphics
  }
  ```
- **Physics Update (`gameUpdate()`)**:
  1. Checks if paused, game over, or won; if so, halts tick execution.
  2. Updates player position via `playerMove()`.
  3. Decays scent trail via `updateScentTrail()`.
  4. Runs bot AI updates for each active bot via `bot.update()`.
  5. Moves all in-flight missiles via `moveMissiles()`.
  6. Resolves collisions between missiles, walls, bots, and the player via `checkCollisions()`.
  7. If all bots are eliminated, increments level and loads the next stage via `loadLevel(currentLevel)`.

---

## 2. Player System

The player tank is an interactive entity controlled by the user via the keyboard, featuring directional sprites, grid-lane alignment, scent dropping, missile firing, and life management.

```
 GameObject (x, y, width, height, image, getBounds())
     └── TankObject (speed, velocityX, velocityY, direction, 4 sprites, snapToGrid(), setDirection())
             └── Player (Specific player constructor and attributes)
```

### 2.1 Code Elements Involved
- **Classes**:
  - `Tank.GameObject` ([`Tank.java:L12-L17`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L12-L17))
  - `Tank.TankObject` ([`Tank.java:L19-L59`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L19-L59))
  - `Tank.Player` ([`Tank.java:L61-L65`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L61-L65))
- **Fields in `Tank`**:
  - `Player player`: Holds the current player instance.
  - `int playerLives = 3`: Player's remaining lives.
  - `Image player_up, player_down, player_left, player_right`: Directional player sprites loaded from `../image/ptank_*.png`.
  - `int tileSize = 32`: Grid tile size in pixels.

### 2.2 Input Handling & Key Listeners
The `Tank` panel implements `KeyListener` ([`Tank.java:L473-L501`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L473-L501)):

```java
@Override
public void keyPressed(KeyEvent e) {
    if (player == null || isPaused) return;
    int key = e.getKeyCode();
    
    if (key == KeyEvent.VK_SPACE) {
        shoot(); // Launches missile
        return;
    }

    // Lane Snapping when turning orthogonal to movement
    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) && player.velocityX != 0) player.snapToGrid();
    if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) && player.velocityY != 0) player.snapToGrid();

    // Set directional velocity (Speed = 4)
    if (key == KeyEvent.VK_UP)         { player.setDirection("UP");    player.velocityY = -player.speed; player.velocityX = 0; } 
    else if (key == KeyEvent.VK_DOWN)  { player.setDirection("DOWN");  player.velocityY = player.speed;  player.velocityX = 0; } 
    else if (key == KeyEvent.VK_LEFT)  { player.setDirection("LEFT");  player.velocityX = -player.speed; player.velocityY = 0; } 
    else if (key == KeyEvent.VK_RIGHT) { player.setDirection("RIGHT"); player.velocityX = player.speed;  player.velocityY = 0; }
}
```

When an arrow key is released:
```java
@Override
public void keyReleased(KeyEvent e) {
    if (player == null) return;
    int key = e.getKeyCode();
    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) && player.velocityY != 0) player.velocityY = 0;
    if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) && player.velocityX != 0) player.velocityX = 0;
}
```

### 2.3 Grid Alignment (`snapToGrid()`)
In grid-based tile games, turning into a perpendicular 32px corridor while slightly off-center causes wall collisions.  
`snapToGrid()` ([`Tank.java:L48-L58`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L48-L58)) fixes this:
```java
public void snapToGrid() {
    if (velocityY != 0) { // Moving vertically, align horizontal coordinate
        int gridX = Math.round((float)x / tileSize);
        this.x = gridX * tileSize;
    }
    if (velocityX != 0) { // Moving horizontally, align vertical coordinate
        int gridY = Math.round((float)y / tileSize);
        this.y = gridY * tileSize;
    }
}
```

### 2.4 Movement & Scent Emission (`playerMove()`)
- Located at [`Tank.java:L365-L374`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L365-L374).
- Calculates predicted next position: `nextX = player.x + player.velocityX`, `nextY = player.y + player.velocityY`.
- Checks `checkWallCollision()` against brick and steel bounding boxes and map boundaries.
- If unobstructed: updates `player.x` and `player.y`, then invokes `dropScent()`.

### 2.5 Player Shooting (`shoot()`)
- Located at [`Tank.java:L423-L426`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L423-L426).
- Instantiates a `Missile` centered at the tank's barrel with flag `fromPlayer = true`.

### 2.6 Player Hit & Respawn Cycle
- In `checkCollisions()` ([`Tank.java:L388-L400`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L388-L400)):
  - When an enemy missile (`!m.fromPlayer`) intersects `player.getBounds()`:
    1. The missile is queued for removal.
    2. `playerLives` is decremented by 1.
    3. UI panel is updated via `uiPanel.setLives(playerLives)`.
    4. If `playerLives <= 0`, `player = null` and `gameOver = true`.
    5. If lives remain, `loadLevel(currentLevel)` is called to reset the current level layout and respawn the player.

---

## 3. Enemy Bot AI System

The game features three distinct enemy classes derived from `TankObject`, each implementing unique combat personalities.

```
                 ┌─────────────────────────────────┐
                 │           TankObject            │
                 └────────────────┬────────────────┘
                                  │
                 ┌────────────────▼────────────────┐
                 │            Tank.Bot             │
                 │ (Standard Wanderer / LOS Hunter)│
                 └────────┬───────────────┬────────┘
                          │               │
        ┌─────────────────┴──────┐ ┌──────┴──────────────────┐
        │       Tank.BotFast     │ │      Tank.BotHeavy      │
        │ (Scent-Tracking Hound) │ │ (3-HP Armored Juggernaut)│
        └────────────────────────┘ └─────────────────────────┘
```

---

### 3.1 Base Bot Tank (`Bot`)
- **Location**: [`Tank.java:L73-L159`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L73-L159)
- **Base Speed**: `2` pixels/tick.
- **Health**: 1 missile hit (`takeHit()` queues `this` into `botsToRemove`).

#### AI State Machine:
```
           hasLineOfSight() == true
      ┌─────────────────────────────────┐
      │                                 ▼
┌──────────────┐                 ┌──────────────┐
│  WANDERING   │                 │   HUNTING    │
└──────────────┘                 └──────────────┘
      ▲                                 │
      └─────────────────────────────────┘
         Arrives at lastKnownPlayerPosition
```

1. **State: `WANDERING`**:
   - Decrements `moveTimer`. When it reaches 0:
     - Calls `changeRandomDirection()`: randomly picks one of 5 options (Up, Down, Left, Right, or Stop) and resets `moveTimer = 50 + random.nextInt(100)`.
   - If a wall collision occurs (`checkWallCollision`), immediately calls `changeRandomDirection()`.

2. **State: `HUNTING`**:
   - Triggered when `hasLineOfSight()` returns `true`.
   - Caches `lastKnownPlayerPosition = new Point(player.x, player.y)`.
   - Executes `moveTowards(lastKnownPlayerPosition)`: compares horizontal (`dx`) and vertical (`dy`) delta distances and navigates along the dominant axis.
   - Once the bot reaches the target point (`getBounds().contains(lastKnownPlayerPosition)`), it reverts to `WANDERING`.

3. **Line-of-Sight Calculation (`hasLineOfSight()`)** ([`Tank.java:L145-L155`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L145-L155)):
   - Checks if the bot is aligned with the player on either the X axis (`Math.abs(player.x - x) < tileSize`) or Y axis (`Math.abs(player.y - y) < tileSize`).
   - If aligned, casts a `Rectangle sightLine` spanning from the bot to the player.
   - Checks if `sightLine` intersects **any** brick or steel wall in the level:
     ```java
     for (GameObject wall : brick) if (sightLine.intersects(wall.getBounds())) return false;
     for (GameObject wall : steel) if (sightLine.intersects(wall.getBounds())) return false;
     return true;
     ```

4. **Bot Shooting (`shoot()`)** ([`Tank.java:L157`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L157)):
   - Decrements `shootCooldown` each tick.
   - When cooldown reaches 0 **and** `hasLineOfSight()` is true:
     - Fires a missile with `fromPlayer = false`.
     - Resets `shootCooldown = 100 + random.nextInt(100)` ticks.

---

### 3.2 Fast Bot (`BotFast`)
- **Location**: [`Tank.java:L161-L200`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L161-L200)
- **Base Speed**: `3` pixels/tick (50% faster than standard bot).
- **Spawn Character**: `'6'`.
- **Special AI: Scent Hunting**:
  Rather than wandering blindly until obtaining line-of-sight, the `BotFast` actively stalks the player by smelling the scent trail left in tiles.

```java
@Override
public void update() {
    ScentNode targetScent = findStrongestAdjacentScent();
    if (targetScent != null) {
        currentState = AIState.HUNTING;
        moveTowards(new Point(targetScent.x * tileSize, targetScent.y * tileSize));
    } else {
        currentState = AIState.WANDERING;
        moveTimer--;
        if (moveTimer <= 0) {
            changeRandomDirection();
            moveTimer = 20 + random.nextInt(50); // Faster direction changes
        }
    }
    // Wall collision checks & shooting logic...
}
```

- **Scent Detection (`findStrongestAdjacentScent()`)**:
  - Calculates current tile coordinate `(x / tileSize, y / tileSize)`.
  - Scans all active `ScentNode` objects in `scentTrail`.
  - Inspects the 8 surrounding neighbor tiles (`Math.abs(scent.x - currentX) <= 1 && Math.abs(scent.y - currentY) <= 1`).
  - Selects the neighbor node with the highest remaining `strength` (the freshest scent trail).

---

### 3.3 Heavy Armored Bot (`BotHeavy`)
- **Location**: [`Tank.java:L201-L214`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L201-L214)
- **Base Speed**: `1` pixel/tick (slow, heavy tank).
- **Spawn Character**: `'8'`.
- **Special Mechanic: Multi-Hit Armor**:
  - Features an internal field `int health = 3`.
  - Overrides `takeHit()`:
    ```java
    @Override
    public void takeHit() {
        this.health--;
        if (this.health <= 0) {
            botsToRemove.add(this); // Only destroyed after 3 hits!
        }
    }
    ```

---

## 4. Scent Trail System

The scent trail system allows bots to track players through maze-like corridors even after the player has moved around a corner.

```
[Player Position (x,y)]
        │
        ▼ (drops or refreshes)
[ScentNode (gridX, gridY, strength = 300)]
        │
        ▼ (each tick: strength--)
[Expired: strength <= 0 -> removed from scentTrail]
```

### 4.1 Data Structure & Mechanics
- **Class**: `Tank.ScentNode` ([`Tank.java:L67-L71`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L67-L71))
  ```java
  class ScentNode {
      int x, y;         // Grid coordinates (tile X, tile Y)
      int strength;     // Remaining scent lifespan
      ScentNode(int x, int y, int strength) { ... }
  }
  ```
- **Dropping Scent (`dropScent()`)** ([`Tank.java:L346-L354`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L346-L354)):
  - Called in `playerMove()` whenever the player moves.
  - Converts player pixel position to tile indices: `player.x / tileSize`, `player.y / tileSize`.
  - If a `ScentNode` already exists at that tile, resets its strength to `300`.
  - Otherwise, instantiates a new `ScentNode(playerGridX, playerGridY, 300)` and adds it to `scentTrail`.
- **Decay & Evaporation (`updateScentTrail()`)** ([`Tank.java:L356-L363`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L356-L363)):
  - Iterates safely through `scentTrail` using an `Iterator`.
  - Decrements `scent.strength--` each tick.
  - When `scent.strength <= 0`, removes the node from memory.

---

## 5. Combat, Projectiles & Collision System

### 5.1 Missile Representation (`Missile`)
- **Location**: [`Tank.java:L216-L231`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L216-L231)
- **Speed**: `5` pixels/tick.
- **Size**: 8x8 pixels.
- **Directional Velocity**: Configured upon spawn based on firing tank direction (`UP`, `DOWN`, `LEFT`, `RIGHT`).
- **Affiliation Flag**: `boolean fromPlayer` (prevents friendly fire).

### 5.2 Collision Resolution Matrix (`checkCollisions()`)
Located at [`Tank.java:L378-L413`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L378-L413).  
To prevent `ConcurrentModificationException` during collection traversal, changes are recorded in temporary lists (`missilesToRemove`, `bricksToRemove`, `botsToRemove`) and batch-removed at the end of the check.

| Collider A | Collider B | Result / Code Action |
|---|---|---|
| **Any Missile** | **Brick Wall** (`brick`) | `missilesToRemove.add(m);` and `bricksToRemove.add(b);` (Wall destroyed, missile destroyed). |
| **Any Missile** | **Steel Wall** (`steel`) | `missilesToRemove.add(m);` (Steel wall survives, missile destroyed). |
| **Enemy Missile** (`!fromPlayer`) | **Player** (`player`) | `missilesToRemove.add(m); playerLives--;` Respawn level if lives > 0; Game Over if lives = 0. |
| **Player Missile** (`fromPlayer`) | **Bot** (`bots`) | `missilesToRemove.add(m); b.takeHit();` (Bot damaged or destroyed). |

---

## 6. Terrain & Level System

### 6.1 Level Maps Data (`LevelData.java`)
- **Location**: [`src/LevelData.java`](file:///d:/csjava/2D%20Game/tankwar/src/LevelData.java)
- **Map Dimensions**: 15 rows by 15 columns.
- **Built-in Levels**:
  - **Level 1**: Introduction (Brick corridors, standard bots).
  - **Level 2**: Introduction of indestructible steel walls and crossfire intersections.
  - **Level 3**: Introduction of scent-tracking Fast Bots (`'6'`).
  - **Level 4**: Introduction of heavily armored Heavy Bots (`'8'`).
  - **Level 5**: Final Challenge combining all bot varieties and complex fortress geometry.

### 6.2 Level Parsing & Spawning (`loadLevel()`)
Located at [`Tank.java:L302-L334`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java#L302-L334):
```java
public void loadLevel(int level) {
    if (level > map.getTotalLevels()) {
        gameWon = true; // Cleared all levels!
        return;
    }
    String[] currentMapData = map.getMap(level);
    // Clears existing entities...
    for (int r = 0; r < currentMapData.length; r++) {
        String line = currentMapData[r];
        for (int c = 0; c < line.length(); c++) {
            char tileType = line.charAt(c);
            int x = c * tileSize, y = r * tileSize;
            switch (tileType) {
                case '1': brick.add(new GameObject(brick_img, x, y, tileSize, tileSize)); break;
                case '9': steel.add(new GameObject(steel_img, x, y, tileSize, tileSize)); break;
                case '7': player = new Player(..., x, y, tileSize, tileSize, 4); break;
                case '5': bots.add(new Bot(..., x, y, tileSize, tileSize, 2)); break;
                case '6': bots.add(new BotFast(..., x, y, tileSize, tileSize, 3)); break;
                case '8': bots.add(new BotHeavy(..., x, y, tileSize, tileSize, 1)); break;
            }
        }
    }
    // Updates HUD
    uiPanel.setLevel(currentLevel);
    uiPanel.setLives(playerLives);
}
```

---

## 7. User Interface & HUD Panel

- **Location**: [`src/UIPanel.java`](file:///d:/csjava/2D%20Game/tankwar/src/UIPanel.java)
- **Dimensions**: 150px width, 480px height.
- **Background**: Rendered via `panal_background.jpg` in `paintComponent()`.

### 7.1 Displays & Controls
1. **Level Display**: `JLabel levelValueLabel` — updated via `uiPanel.setLevel(int level)`.
2. **Lives Display**: `JLabel livesValueLabel` — updated via `uiPanel.setLives(int lives)`.
3. **Pause / Resume Button**:
   - `JLabel pauseLabel` using `pause.png` and `resume.png` icons.
   - Click listener calls `gamePanel.togglePause()` and `gamePanel.requestFocusInWindow()`.
   - `togglePause()` stops/starts the `gameLoop` timer and switches the icon.
4. **Restart Button**:
   - `JLabel resetLabel` using `restat.png` icon.
   - Click listener calls `gamePanel.resetGame()` and `gamePanel.requestFocusInWindow()`.
   - Resets to Level 1, restores 3 lives, unpauses if paused, and reloads level data.

---

## 8. Level Editor System

- **Location**: [`src/LevelEditor.java`](file:///d:/csjava/2D%20Game/tankwar/src/LevelEditor.java)
- **Role**: Allows visual map creation, editing, and file saving/loading.

### 8.1 Key Mechanisms
1. **15x15 Interactive Grid (`gridButtons`)**:
   - Grid of 225 `JButton` elements.
   - Each button stores its current tile character in client properties: `button.putClientProperty("gridChar", currentTool)`.
2. **Palette Selector (`setupPalettePanel`)**:
   - Buttons to pick active drawing tool:
     - `Brick ('1')`, `Steel ('9')`, `Player ('7')`, `Bot ('5')`, `Fast Bot ('6')`, `Heavy Bot ('8')`, `Eraser ('0')`.
3. **Saving (`saveLevel()`)**:
   - Opens a `JFileChooser`.
   - Uses `PrintWriter` to write 15 lines of 15 characters each representing the map layout.
4. **Loading (`loadLevel()`)**:
   - Opens a `JFileChooser`.
   - Uses `BufferedReader` to read 15 lines, populates `gridButtons`, and displays corresponding tool icons.

---

## 9. A* Pathfinding Algorithm

- **Location**: [`src/AStar.java`](file:///d:/csjava/2D%20Game/tankwar/src/AStar.java)
- **Purpose**: Provides optimal shortest-path calculation on a 2D grid matrix. Available for AI navigation extensions.

### 9.1 Mechanics
- **`Node` Structure**:
  - `gCost`: Distance from starting node.
  - `hCost`: Manhattan distance heuristic (`|startX - targetX| + |startY - targetY|`).
  - `fCost = gCost + hCost`.
  - Implements `Comparable<Node>` for automatic sorting inside `PriorityQueue<Node>`.
- **`findPath(boolean[][] grid, int startX, int startY, int endX, int endY)`**:
  - Evaluates cardinal neighbors (Up, Down, Left, Right).
  - Skips diagonals and wall tiles (`grid[y][x] == true`).
  - Uses `retracePath()` to backtrack from target node through parent references to yield a complete `List<Node>`.

---

## 10. Master Code Responsibility Lookup Table

Use this quick reference table to find the exact file and lines responsible for any game feature:

| Game Feature / Mechanism | Responsible File | Class / Method | Line Range (Approx.) |
|---|---|---|---|
| **Application Launch & Window Setup** | [`src/App.java`](file:///d:/csjava/2D%20Game/tankwar/src/App.java) | `App.createAndShowGUI()` | Lines 20–57 |
| **Screen Switching (CardLayout)** | [`src/App.java`](file:///d:/csjava/2D%20Game/tankwar/src/App.java) | `showGameScreen()`, `showLevelEditor()`, `showHomeScreen()` | Lines 60–74 |
| **Start Screen & Menu Buttons** | [`src/HomeScreen.java`](file:///d:/csjava/2D%20Game/tankwar/src/HomeScreen.java) | `HomeScreen` constructor | Lines 14–47 |
| **Game Tick Loop (Timer)** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `gameLoop`, `actionPerformed()`, `gameUpdate()` | Lines 292, 336–344, 470 |
| **Player Base Class & Sprites** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.Player`, `Tank.TankObject` | Lines 19–65 |
| **Player Keyboard Controls** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `keyPressed()`, `keyReleased()` | Lines 474–501 |
| **Player Grid Alignment** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `TankObject.snapToGrid()` | Lines 48–58 |
| **Player Movement & Scent Trigger** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `playerMove()` | Lines 365–374 |
| **Player Missile Firing** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `shoot()` | Lines 423–426 |
| **Player Lives & Respawn Handling** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `checkCollisions()` | Lines 388–400 |
| **Base Bot AI (Wander & Hunt)** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.Bot.update()`, `moveTowards()` | Lines 84–130 |
| **Line-of-Sight Calculation** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.Bot.hasLineOfSight()` | Lines 145–155 |
| **Bot Shooting Mechanic** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.Bot.shoot()` | Line 157 |
| **Fast Bot (Scent Stalker AI)** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.BotFast.update()`, `findStrongestAdjacentScent()` | Lines 161–199 |
| **Heavy Bot (3-HP Multi-Hit)** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.BotHeavy.takeHit()` | Lines 201–214 |
| **Scent Trail Creation & Decay** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `dropScent()`, `updateScentTrail()` | Lines 346–363 |
| **Missile Lifecycle & Movement** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `Tank.Missile`, `moveMissiles()` | Lines 216–231, 376 |
| **Collision Resolution (Bricks/Steel/Hits)** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `checkCollisions()` | Lines 378–413 |
| **Level Maps (1 through 5)** | [`src/LevelData.java`](file:///d:/csjava/2D%20Game/tankwar/src/LevelData.java) | `maps` array, `getMap()` | Lines 3–102 |
| **Level Loading & Map Parsing** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `loadLevel()` | Lines 302–334 |
| **Pause & Game Reset Logic** | [`src/Tank.java`](file:///d:/csjava/2D%20Game/tankwar/src/Tank.java) | `togglePause()`, `resetGame()` | Lines 429–447 |
| **HUD Panel & Button Clicks** | [`src/UIPanel.java`](file:///d:/csjava/2D%20Game/tankwar/src/UIPanel.java) | `UIPanel` constructor, mouse listeners | Lines 24–97 |
| **Level Editor (Grid, Tools, Save/Load)** | [`src/LevelEditor.java`](file:///d:/csjava/2D%20Game/tankwar/src/LevelEditor.java) | `paintTile()`, `saveLevel()`, `loadLevel()` | Lines 104–146 |
| **A* Pathfinding Search** | [`src/AStar.java`](file:///d:/csjava/2D%20Game/tankwar/src/AStar.java) | `AStar.findPath()`, `retracePath()` | Lines 37–104 |
