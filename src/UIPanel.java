import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UIPanel extends JPanel {
    // Labels to display information
    private JLabel levelValueLabel;
    private JLabel livesValueLabel;
    private JLabel gridValueLabel;

    // --- UPDATED: Using JLabels for image-based buttons ---
    private JLabel pauseLabel;
    private JLabel resetLabel;

    // A reference to the main game panel to send commands to it
    private Tank gamePanel;
    
    private Image backgroundImage;
    // --- NEW: Images for the icon buttons ---
    private Image pauseIcon;
    private Image restartIcon;
    private Image resumeIcon; // Optional: an icon for the resume state

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
                gamePanel.toggleGrid();
                gamePanel.requestFocusInWindow(); // Return focus to the game!
            }
        });
        add(gridValueLabel);

        // --- UPDATED: Creating clickable image labels ---
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

        // Add listeners to the labels
        pauseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gamePanel.togglePause();
                gamePanel.requestFocusInWindow(); // Return focus to the game!
            }
        });
        
        resetLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gamePanel.resetGame();
                gamePanel.requestFocusInWindow(); // Return focus to the game!
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

    // --- Public methods to allow the game to update the UI ---
    public void setLevel(int level) {
        levelValueLabel.setText(String.valueOf(level));
    }

    public void setLives(int lives) {
        livesValueLabel.setText(String.valueOf(lives));
    }

    public void setGridStatus(boolean isGridOn) {
        if (gridValueLabel != null) {
            gridValueLabel.setText(isGridOn ? "ON [G]" : "OFF [G]");
            gridValueLabel.setForeground(isGridOn ? Color.YELLOW : Color.GRAY);
        }
    }

    // --- UPDATED: This method now changes the icon instead of text ---
    public void setPauseIcon(boolean isPaused) {
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
