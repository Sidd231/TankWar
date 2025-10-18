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
