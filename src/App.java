import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

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
        java.io.File f = new java.io.File(cleanPath);
        if (f.exists()) {
            return new javax.swing.ImageIcon(f.getAbsolutePath()).getImage();
        }
        f = new java.io.File("../" + cleanPath);
        if (f.exists()) {
            return new javax.swing.ImageIcon(f.getAbsolutePath()).getImage();
        }
        java.net.URL url = App.class.getResource("/" + cleanPath);
        if (url != null) {
            return new javax.swing.ImageIcon(url).getImage();
        }
        return new javax.swing.ImageIcon(relativePath).getImage();
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
        
        int gameWidth = 15 * 32;
        int uiWidth = 150;
        int height = 15 * 32 + 60; 
        frame.setPreferredSize(new Dimension(gameWidth + uiWidth, height));

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
