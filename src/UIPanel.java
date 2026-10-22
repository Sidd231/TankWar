import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UIPanel extends JPanel implements GameEventListener {
    // Labels to display information
    private JLabel levelValueLabel;
    private JLabel livesValueLabel;
    private JLabel gridValueLabel;

    // Clickable image-based action labels
    private JLabel pauseLabel;
    private JLabel resetLabel;

    // Reference to the main game panel
    private Tank gamePanel;

    private Image backgroundImage;
    private Image pauseIcon;
    private Image restartIcon;
    private Image resumeIcon;

    public UIPanel(Tank gamePanel) {
        this.gamePanel = gamePanel;

        try {
            backgroundImage = App.loadImage("image/panal_background.jpg");
            Image rawPause = App.loadImage("image/pause.png");
            Image rawRestart = App.loadImage("image/restat.png");
            Image rawResume = App.loadImage("image/resume.png");

            if (rawPause != null) {
                pauseIcon = rawPause.getScaledInstance(110, 42, Image.SCALE_SMOOTH);
            }
            if (rawRestart != null) {
                restartIcon = rawRestart.getScaledInstance(110, 38, Image.SCALE_SMOOTH);
            }
            if (rawResume != null) {
                resumeIcon = rawResume.getScaledInstance(110, 42, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            System.err.println("Error loading UI images.");
            e.printStackTrace();
        }

        // Set up the panel's appearance
        setPreferredSize(new Dimension(150, 480));
        setOpaque(false);
        setLayout(new GridLayout(8, 1, 5, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font valueFont = new Font("Arial", Font.PLAIN, 20);

        // --- Level Display ---
        JLabel levelTextLabel = new JLabel("LEVEL:", SwingConstants.CENTER);
        levelTextLabel.setFont(labelFont);
        levelTextLabel.setForeground(Color.WHITE);
        add(levelTextLabel);

        levelValueLabel = new JLabel("1", SwingConstants.CENTER);
        levelValueLabel.setFont(valueFont);
        levelValueLabel.setForeground(Color.CYAN);
        add(levelValueLabel);

        // --- Lives Display ---
        JLabel livesTextLabel = new JLabel("LIVES:", SwingConstants.CENTER);
        livesTextLabel.setFont(labelFont);
        livesTextLabel.setForeground(Color.WHITE);
        add(livesTextLabel);

        livesValueLabel = new JLabel("3", SwingConstants.CENTER);
        livesValueLabel.setFont(valueFont);
        livesValueLabel.setForeground(Color.GREEN);
        add(livesValueLabel);

        // --- Grid Display / Toggle ---
        JLabel gridTextLabel = new JLabel("GRID:", SwingConstants.CENTER);
        gridTextLabel.setFont(labelFont);
        gridTextLabel.setForeground(Color.WHITE);
        add(gridTextLabel);

        gridValueLabel = new JLabel("ON [G]", SwingConstants.CENTER);
        gridValueLabel.setFont(valueFont);
        gridValueLabel.setForeground(Color.YELLOW);
        gridValueLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gridValueLabel.setToolTipText("Click or press 'G' to toggle grid overlay");
        gridValueLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePanel != null) {
                    gamePanel.toggleGrid();
                    gamePanel.requestFocusInWindow();
                }
            }
        });
        add(gridValueLabel);

        // --- Button Labels ---
        if (pauseIcon != null) {
            pauseLabel = new JLabel(new ImageIcon(pauseIcon), SwingConstants.CENTER);
        } else {
            pauseLabel = new JLabel("PAUSE", SwingConstants.CENTER);
            pauseLabel.setFont(labelFont);
            pauseLabel.setForeground(Color.WHITE);
        }

        if (restartIcon != null) {
            resetLabel = new JLabel(new ImageIcon(restartIcon), SwingConstants.CENTER);
        } else {
            resetLabel = new JLabel("RESTART", SwingConstants.CENTER);
            resetLabel.setFont(labelFont);
            resetLabel.setForeground(Color.RED);
        }

        pauseLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pauseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePanel != null) {
                    gamePanel.togglePause();
                    gamePanel.requestFocusInWindow();
                }
            }
        });

        resetLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePanel != null) {
                    gamePanel.resetGame();
                    gamePanel.requestFocusInWindow();
                }
            }
        });

        add(pauseLabel);
        add(resetLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.setColor(new Color(25, 25, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // --- GameEventListener Implementation ---
    @Override
    public void onLivesChanged(int lives) {
        setLives(lives);
    }

    @Override
    public void onLevelChanged(int level) {
        setLevel(level);
    }

    @Override
    public void onGridToggled(boolean showGrid) {
        setGridStatus(showGrid);
    }

    @Override
    public void onPauseToggled(boolean isPaused) {
        setPauseIcon(isPaused);
    }

    @Override
    public void onGameOver() {}

    @Override
    public void onGameWon() {}

    // --- Public API methods ---
    public void setLevel(int level) {
        if (levelValueLabel != null) {
            levelValueLabel.setText(String.valueOf(level));
        }
    }

    public void setLives(int lives) {
        if (livesValueLabel != null) {
            livesValueLabel.setText(String.valueOf(lives));
        }
    }

    public void setGridStatus(boolean isGridOn) {
        if (gridValueLabel != null) {
            gridValueLabel.setText(isGridOn ? "ON [G]" : "OFF [G]");
            gridValueLabel.setForeground(isGridOn ? Color.YELLOW : Color.GRAY);
        }
    }

    public void setPauseIcon(boolean isPaused) {
        if (pauseLabel == null) return;
        if (isPaused) {
            if (resumeIcon != null) {
                pauseLabel.setIcon(new ImageIcon(resumeIcon));
                pauseLabel.setText("");
            } else {
                pauseLabel.setIcon(null);
                pauseLabel.setText("RESUME");
                pauseLabel.setForeground(Color.ORANGE);
            }
        } else {
            if (pauseIcon != null) {
                pauseLabel.setIcon(new ImageIcon(pauseIcon));
                pauseLabel.setText("");
            } else {
                pauseLabel.setIcon(null);
                pauseLabel.setText("PAUSE");
                pauseLabel.setForeground(Color.WHITE);
            }
        }
    }
}
