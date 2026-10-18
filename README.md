# 🎮 2D Tank War (Java Swing)

A retro-style 2D arcade Tank War game built from scratch using Java, Swing, and AWT. The game features interactive multi-level combat, dynamic enemy AI (including scent-tracking and armored tanks), destructible/indestructible terrain, a real-time HUD with game controls, and a built-in interactive Level Editor to create custom battle arenas.

---

## 📑 Table of Contents
- [Game Overview](#-game-overview)
- [Key Features](#-key-features)
- [Game Controls](#-game-controls)
- [Entities & Characters](#-entities--characters)
- [Map Tile Legend](#-map-tile-legend)
- [Project Structure](#-project-structure)
- [How to Build & Run](#-how-to-build--run)
- [Level Editor Guide](#-level-editor-guide)
- [Detailed Code Documentation](#-detailed-code-documentation)

---

## 🕹️ Game Overview

In **Tank War**, you command a player tank navigating a 15x15 grid battlefield. Your mission is to eliminate all enemy bots across 5 challenging levels while dodging incoming enemy fire and maneuvering around brick and steel walls.

You start with **3 lives**. Destroying all hostile tanks in a level automatically advances you to the next stage. Clearing all 5 stages wins the game!

---

## ✨ Key Features

- **Grid-Aligned Brick-Unit Movement**: The tank moves in strict 1-tile increments matching the exact length of a brick (32px). Tapping an arrow key advances the whole tank by exactly 1 brick length without stopping mid-tile (between 0.1 and 1 brick length), completely preventing misalignment in tight 1-tile corridors.
- **Visual Grid Overlay**: Tactical grid overlay lines (32x32) displaying the board matrix, togglable via the <kbd>G</kbd> key or the UI sidebar button.
- **Dynamic Enemy AI**:
  - **Standard Bot**: Wanders randomly and locks onto the player with horizontal/vertical Line-of-Sight detection.
  - **Fast Bot (Predator)**: Sniffs out player scent trails (`ScentNode`) left across tiles to hunt the player through corridors.
  - **Heavy Bot (Juggernaut)**: Armored tank that requires 3 missile hits to eliminate.
- **Destructible Terrain**: Missiles break through brick walls tile by tile while bouncing off indestructible steel fortifications.
- **Interactive Heads-Up Display (HUD)**: Real-time UI panel showing current level, remaining lives, grid overlay toggle, pause/resume toggle, and restart controls.
- **In-Game Level Editor**: Visual 15x15 tile map editor where you can paint tiles, design custom levels, and save/load them to and from text files.
- **A\* Pathfinding Engine**: Integrated `AStar.java` pathfinding algorithm ready for tactical enemy navigation.

---

## ⌨️ Game Controls

| Action | Key / Input |
|---|---|
| **Move Up (1 Brick Step)** | <kbd>↑</kbd> (Up Arrow) |
| **Move Down (1 Brick Step)** | <kbd>↓</kbd> (Down Arrow) |
| **Move Left (1 Brick Step)** | <kbd>←</kbd> (Left Arrow) |
| **Move Right (1 Brick Step)** | <kbd>→</kbd> (Right Arrow) |
| **Shoot Missile** | <kbd>Spacebar</kbd> |
| **Toggle Grid Overlay** | <kbd>G</kbd> or click **GRID** on UI sidebar |
| **Pause / Resume** | Click **Pause Icon** on UI sidebar |
| **Restart Game** | Click **Restart Icon** on UI sidebar |

---

## 🛡️ Entities & Characters

| Entity | Speed | Health | Behavior |
|---|---|---|---|
| **Player Tank** (`Player`) | 4 px/tick | 3 Lives (respawns) | Keyboard controlled; drops decaying scent nodes behind it. |
| **Standard Bot** (`Bot`) | 2 px/tick | 1 Hit | Wanders randomly; enters hunting mode when line-of-sight is unobstructed. |
| **Fast Bot** (`BotFast`) | 3 px/tick | 1 Hit | High speed; hunts by tracking the player's freshest scent trail nodes. |
| **Heavy Bot** (`BotHeavy`) | 1 px/tick | 3 Hits | Slow moving, heavily armored tank; takes 3 hits before being destroyed. |
| **Missile** (`Missile`) | 5 px/tick | N/A | High speed projectile; destroys bricks and eliminates opposing tanks. |

---

## 🧱 Map Tile Legend

Maps are represented as 15x15 character matrices. The tile encoding is as follows:

| Code | Entity / Tile Type | Destructible | Description |
|:---:|---|:---:|---|
| `'0'` | **Empty Floor** | — | Walkable open terrain. |
| `'1'` | **Brick Wall** | Yes | Blocks tanks and projectiles; destroyed when hit by a missile. |
| `'9'` | **Steel Wall** | No | Indestructible wall; absorbs and deflects missiles. |
| `'7'` | **Player Spawn** | — | Starting position for the player tank. |
| `'5'` | **Standard Bot** | Yes | Spawn point for a Standard Bot (Speed: 2). |
| `'6'` | **Fast Bot** | Yes | Spawn point for a Scent-Tracking Fast Bot (Speed: 3). |
| `'8'` | **Heavy Bot** | Yes | Spawn point for an Armored Heavy Bot (Health: 3). |

---

## 📁 Project Structure

```text
tankwar/
├── .vscode/
│   └── settings.json            # VS Code Java classpath configuration
├── bin/                         # Compiled .class bytecode
├── image/                       # Graphic assets and sprites
│   ├── brick.jpg                # Destructible brick texture
│   ├── steel.jpg                # Indestructible steel texture
│   ├── ptank_*.png              # Player sprites (up, down, left, right)
│   ├── btank_*.png              # Standard bot sprites
│   ├── btank_fast_*.png         # Fast bot sprites
│   ├── btank_heavy_*.png        # Heavy bot sprites
│   ├── missile.png              # Missile projectile sprite
│   ├── panal_background.jpg     # UI sidebar background
│   ├── pause.png / resume.png   # Pause button icons
│   └── restat.png               # Restart button icon
├── src/                         # Java source files
│   ├── App.java                 # Main application entry point & CardLayout router
│   ├── HomeScreen.java          # Start menu panel (New Game, Editor, Quit)
│   ├── Tank.java                # Game engine, entities, loops, physics & rendering
│   ├── UIPanel.java             # Sidebar UI displaying stats and control buttons
│   ├── LevelData.java           # Level configurations (Levels 1 to 5)
│   ├── LevelEditor.java         # Visual 15x15 map editor & file I/O
│   └── AStar.java               # A* grid pathfinding algorithm
├── DOCUMENTATION.md             # In-depth architectural & code reference guide
└── README.md                    # Project overview and quickstart guide
```

---

## 🚀 How to Build & Run

### Prerequisites
- **Java Development Kit (JDK 8 or higher)** installed (`javac` and `java` available in PATH).
- Compatible with Windows, macOS, or Linux.

### Method 1: Using VS Code (Recommended)
1. Open the project root folder `tankwar/` in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open [`src/App.java`](file:///d:/csjava/2D%20Game/tankwar/src/App.java) and press `F5` (or click **Run** above the `main` method).

### Method 2: Command Line (PowerShell / Terminal)
From the project root directory (`d:\csjava\2D Game\tankwar`):

```powershell
# 1. Compile all Java source files into the bin directory
javac -d bin src/*.java

# 2. Run the game from bin (ensuring relative image paths resolve correctly)
cd bin
java App
```

> **Note**: Assets in the code are referenced with `../image/filename`, so running with the working directory set to `bin` resolves the relative asset paths correctly.

---

## 🛠️ Level Editor Guide

Want to design your own maps?
1. Launch the game and click **Level Editor** from the main menu.
2. Select a tool from the right-hand palette (**Brick**, **Steel**, **Player**, **Bot**, **Fast Bot**, **Heavy Bot**, or **Eraser**).
3. Click any tile on the 15x15 grid to place your selected tool.
4. Click **Save Level** to export your custom level layout as a `.txt` file.
5. Click **Load Level** at any time to open and modify a previously saved map.

---

## 📖 Detailed Code Documentation

For a comprehensive explanation of how every subsystem works (Player movement, Bot AI state machine, Scent trails, Collision resolution, etc.) and where specific responsibilities live in the source code, see the complete guide:

👉 **[DOCUMENTATION.md](file:///d:/csjava/2D%20Game/tankwar/DOCUMENTATION.md)**

For the full Reinforcement Learning multi-bot training architecture and differentiated behavioral rules roadmap:

👉 **[RL_TRAINING_ROADMAP.md](file:///d:/csjava/2D%20Game/tankwar/RL_TRAINING_ROADMAP.md)**
