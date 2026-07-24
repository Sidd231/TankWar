import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UIPanel extends JPanel {
    // Labels to display information
    private JLabel levelValueLabel;
    private JLabel livesValueLabel;

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
            backgroundImage = new ImageIcon("../image/panal_background.jpg").getImage();
            // --- NEW: Load the button icons ---
            pauseIcon = new ImageIcon("../image/pause.png").getImage();
            restartIcon = new ImageIcon("../image/restat.png").getImage();
            // You can create a "resume.png" if you want the icon to change
            resumeIcon = new ImageIcon("../image/resume.png").getImage(); 
        } catch (Exception e) {
            System.err.println("Error loading UI images.");
            e.printStackTrace();
        }

        // Set up the panel's appearance
        setPreferredSize(new Dimension(150, 480));
        setOpaque(false); 
        setLayout(new GridLayout(8, 1, 10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font valueFont = new Font("Arial", Font.PLAIN, 20);

        // --- Level Display ---
        JLabel levelTextLabel = new JLabel("LEVEL:");
        levelTextLabel.setFont(labelFont);
        levelTextLabel.setForeground(Color.WHITE);
        add(levelTextLabel);

        levelValueLabel = new JLabel("1");
        levelValueLabel.setFont(valueFont);
        levelValueLabel.setForeground(Color.CYAN);
        add(levelValueLabel);

        // --- Lives Display ---
        JLabel livesTextLabel = new JLabel("LIVES:");
        livesTextLabel.setFont(labelFont);
        livesTextLabel.setForeground(Color.WHITE);
        add(livesTextLabel);

        livesValueLabel = new JLabel("3");
        livesValueLabel.setFont(valueFont);
        livesValueLabel.setForeground(Color.GREEN);
        add(livesValueLabel);

        // --- UPDATED: Creating clickable image labels ---
        pauseLabel = new JLabel(new ImageIcon(pauseIcon));
        resetLabel = new JLabel(new ImageIcon(restartIcon));

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
        
        add(new JLabel()); // Spacer
        add(pauseLabel);
        add(resetLabel);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    // --- Public methods to allow the game to update the UI ---
    public void setLevel(int level) {
        levelValueLabel.setText(String.valueOf(level));
    }

    public void setLives(int lives) {
        livesValueLabel.setText(String.valueOf(lives));
    }

    // --- UPDATED: This method now changes the icon instead of text ---
    public void setPauseIcon(boolean isPaused) {
        if (isPaused) {
            pauseLabel.setIcon(new ImageIcon(resumeIcon));
        } else {
            pauseLabel.setIcon(new ImageIcon(pauseIcon));
        }
    }
}
