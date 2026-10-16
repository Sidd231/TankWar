import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.util.HashMap;

public class LevelEditor extends JPanel {

    private App mainApp;
    private JPanel gridPanel;
    private JButton[][] gridButtons;
    private char currentTool = '1'; // Start with brick tool selected

    // Map characters to their image icons for the editor
    private HashMap<Character, ImageIcon> toolIcons;

    public LevelEditor(App mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());

        loadToolIcons();
        setupGridPanel();
        setupPalettePanel();
        setupControlPanel();
    }

    private void loadToolIcons() {
        toolIcons = new HashMap<>();
        // Load small, 32x32 icons for the editor grid
        try {
            toolIcons.put('1', App.loadIcon("image/brick.jpg"));
            toolIcons.put('9', App.loadIcon("image/steel.jpg"));
            toolIcons.put('7', App.loadIcon("image/ptank_up.png"));
            toolIcons.put('5', App.loadIcon("image/btank_up.png"));
            toolIcons.put('6', App.loadIcon("image/btank_fast_up.png"));
            toolIcons.put('8', App.loadIcon("image/btank_heavy_up.png"));
            toolIcons.put('0', null); // Represents empty space
        } catch (Exception e) {
            System.err.println("Error loading tool icons for editor.");
            e.printStackTrace();
        }
    }

    private void setupGridPanel() {
        gridPanel = new JPanel(new GridLayout(15, 15));
        gridButtons = new JButton[15][15];

        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                JButton button = new JButton();
                button.setBackground(Color.BLACK);
                button.putClientProperty("gridChar", '0'); // Store the character on the button
                int row = r;
                int col = c;
                button.addActionListener(e -> paintTile(row, col));
                gridButtons[r][c] = button;
                gridPanel.add(button);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    private void setupPalettePanel() {
        JPanel palettePanel = new JPanel();
        palettePanel.setLayout(new BoxLayout(palettePanel, BoxLayout.Y_AXIS));
        palettePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        palettePanel.setBackground(Color.DARK_GRAY);

        palettePanel.add(new JLabel("TOOLS"));
        addToolButton(palettePanel, "Brick", '1');
        addToolButton(palettePanel, "Steel", '9');
        addToolButton(palettePanel, "Player", '7');
        addToolButton(palettePanel, "Bot", '5');
        addToolButton(palettePanel, "Fast Bot", '6');
        addToolButton(palettePanel, "Heavy Bot", '8');
        addToolButton(palettePanel, "Eraser", '0');

        add(palettePanel, BorderLayout.EAST);
    }

    private void addToolButton(JPanel panel, String name, char toolChar) {
        JButton button = new JButton(name);
        button.addActionListener(e -> currentTool = toolChar);
        panel.add(button);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        JButton saveButton = new JButton("Save Level");
        JButton loadButton = new JButton("Load Level");
        JButton backButton = new JButton("Back to Menu");

        saveButton.addActionListener(e -> saveLevel());
        loadButton.addActionListener(e -> loadLevel());
        backButton.addActionListener(e -> mainApp.showHomeScreen());

        controlPanel.add(saveButton);
        controlPanel.add(loadButton);
        controlPanel.add(backButton);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void paintTile(int r, int c) {
        JButton button = gridButtons[r][c];
        button.putClientProperty("gridChar", currentTool);
        button.setIcon(toolIcons.get(currentTool));
    }

    private void saveLevel() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (int r = 0; r < 15; r++) {
                    StringBuilder line = new StringBuilder();
                    for (int c = 0; c < 15; c++) {
                        line.append(gridButtons[r][c].getClientProperty("gridChar"));
                    }
                    writer.println(line.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLevel() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                for (int r = 0; r < 15; r++) {
                    String line = reader.readLine();
                    if (line == null) break;
                    for (int c = 0; c < 15 && c < line.length(); c++) {
                        char tool = line.charAt(c);
                        gridButtons[r][c].putClientProperty("gridChar", tool);
                        gridButtons[r][c].setIcon(toolIcons.get(tool));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
