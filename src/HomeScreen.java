import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HomeScreen extends JPanel {

    private JButton newGameButton;
    private JButton levelEditorButton;
    private JButton quitButton;

    private App mainApp;

    public HomeScreen(App mainApp) {
        this.mainApp = mainApp;

        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("TANK WAR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 70));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, gbc);

        Font buttonFont = new Font("Arial", Font.BOLD, 24);

        newGameButton = new JButton("New Game");
        newGameButton.setFont(buttonFont);
        newGameButton.addActionListener(e -> mainApp.showGameScreen());
        add(newGameButton, gbc);

        
        levelEditorButton = new JButton("Level Editor");
        levelEditorButton.setFont(buttonFont);
        levelEditorButton.setEnabled(true); 
        levelEditorButton.addActionListener(e -> mainApp.showLevelEditor()); 
        add(levelEditorButton, gbc);
        
        quitButton = new JButton("Quit");
        quitButton.setFont(buttonFont);
        quitButton.addActionListener(e -> System.exit(0));
        add(quitButton, gbc);
    }
}
