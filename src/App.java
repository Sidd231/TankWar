import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class App {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private Tank tankPanel;
    private HomeScreen homeScreen;
    private LevelEditor levelEditor; 

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new App().createAndShowGUI());
    }

    public static java.awt.Image loadImage(String relativePath) {
        String cleanPath = relativePath.replace("\\", "/");
        if (cleanPath.startsWith("../")) {
            cleanPath = cleanPath.substring(3);
        }

        // Try to load as BufferedImage so we can trim transparent borders (PNG only)
        File[] candidates = {
            new File(cleanPath),
            new File("../" + cleanPath)
        };
        for (File f : candidates) {
            if (f.exists()) {
                // Only attempt trimming for PNG files
                if (cleanPath.toLowerCase().endsWith(".png")) {
                    try {
                        BufferedImage raw = ImageIO.read(f);
                        if (raw != null) {
                            BufferedImage trimmed = trimTransparentBorders(raw);
                            return trimmed != null ? trimmed : raw;
                        }
                    } catch (IOException ignored) {}
                }
                return new javax.swing.ImageIcon(f.getAbsolutePath()).getImage();
            }
        }

        java.net.URL url = App.class.getResource("/" + cleanPath);
        if (url != null) {
            if (cleanPath.toLowerCase().endsWith(".png")) {
                try {
                    BufferedImage raw = ImageIO.read(url);
                    if (raw != null) {
                        BufferedImage trimmed = trimTransparentBorders(raw);
                        return trimmed != null ? trimmed : raw;
                    }
                } catch (IOException ignored) {}
            }
            return new javax.swing.ImageIcon(url).getImage();
        }

        return new javax.swing.ImageIcon(relativePath).getImage();
    }

    /**
     * Crops away fully-transparent (alpha == 0) rows and columns on all four
     * sides of the image, so the returned image is tight around its content.
     * Returns null if the image is entirely transparent.
     */
    private static BufferedImage trimTransparentBorders(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        // Check if the image has an alpha channel at all
        if (!img.getColorModel().hasAlpha()) {
            return img; // nothing to trim
        }

        int top = -1, bottom = -1, left = -1, right = -1;

        // Find top
        outer:
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    top = y;
                    break outer;
                }
            }
        }
        if (top == -1) return null; // fully transparent

        // Find bottom
        outer:
        for (int y = h - 1; y >= top; y--) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    bottom = y;
                    break outer;
                }
            }
        }

        // Find left
        outer:
        for (int x = 0; x < w; x++) {
            for (int y = top; y <= bottom; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    left = x;
                    break outer;
                }
            }
        }

        // Find right
        outer:
        for (int x = w - 1; x >= left; x--) {
            for (int y = top; y <= bottom; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    right = x;
                    break outer;
                }
            }
        }

        if (left == -1 || right == -1) return null;

        // If nothing was trimmed, return the original
        if (top == 0 && left == 0 && bottom == h - 1 && right == w - 1) {
            return img;
        }

        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    public static javax.swing.ImageIcon loadIcon(String relativePath) {
        java.awt.Image img = loadImage(relativePath);
        return img != null ? new javax.swing.ImageIcon(img) : new javax.swing.ImageIcon(relativePath);
    }

    private void createAndShowGUI() {
        frame = new JFrame("TANK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

       
        homeScreen = new HomeScreen(this);
        levelEditor = new LevelEditor(this);

        JPanel gameContainer = new JPanel(new BorderLayout());
        tankPanel = new Tank();
        UIPanel uiPanel = new UIPanel(tankPanel);
        tankPanel.setUiPanel(uiPanel);
        
        gameContainer.add(tankPanel, BorderLayout.CENTER);
        gameContainer.add(uiPanel, BorderLayout.EAST);

        
        mainPanel.add(homeScreen, "HOME");
        mainPanel.add(gameContainer, "GAME");
        mainPanel.add(levelEditor, "EDITOR"); 

        frame.add(mainPanel);
        
        // Let pack() compute the frame size from component preferred sizes.
        // This ensures the CONTENT area is exactly 480+150 wide and 480 tall,
        // with window title bar / borders added on top (not subtracting from content).
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        cardLayout.show(mainPanel, "HOME");
    }

    
    public void showGameScreen() {
        cardLayout.show(mainPanel, "GAME");
        tankPanel.requestFocusInWindow();
        tankPanel.resetGame();
    }

    public void showHomeScreen() {
        cardLayout.show(mainPanel, "HOME");
    }
    
   
    public void showLevelEditor() {
        cardLayout.show(mainPanel, "EDITOR");
    }
}
