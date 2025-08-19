import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;




public class Tank extends JPanel{

    class block{
        int x ;
        int y ;
        int width;
        int height;
        Image image ; 

        int startX;
        int startY;

        block(Image image , int x ,int y, int width , int height){
            this.image = image ;
            this.x = x;
            this.y = y ;
            this.width = width;
            this.height = height;

            this.startX = x ;
            this.startY = y ;

        }
    }

    private int rowCount = 15 ;
    private int columnCount = 15 ; 
    private int tileSize = 32 ; 
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize ; 

    private Image Player_tank;
    private Image brick_wall ; 
    private Image Bot_tank;
    private Image Steel_Wall;

    HashSet<Block> brick;
    HashSet<Block> bot;
    HashSet<Block> steel;
    Block Player;



    Tank (){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);

        //loading image 
        brick_wall = new ImageIcon(getClass(),getResource("./brick.jpg")).getImage();
        Steel_Wall = new ImageIcon(getClass(),getResource("./image_1755482051164.jpg")).getImage();
        Bot_tank = new ImageIcon(getClass(),getResource("./btank.png")).getImage();
        Player_tank = new ImageIcon(getClass(),getResource("./ptank.png")).getImage();

    }


    public void loadMap(){
    // Initialize the HashSets to store different types of blocks.
    brick = new HashSet<Block>();
    Bot_tank = new HashSet<Block>();
    steel = new HashSet<Block>(); 
    
    // Loop through each row of the current map data.
    for (int r = 0; r < currentMapData.length; r++) {
        String line = currentMapData[r];
        // Loop through each character (column) in the current row.
        for (int c = 0; c < line.length(); c++) {
            char tileType = line.charAt(c);
            
            // Create a new Block object for the current position.
            // We'll add this block to the correct set based on its type.
            int x = c * tileSize;
            int y = r * tileSize;

            switch (tileType) {
                case '1': // Brick Wall
                    brick.add(new Block(x, y));
                    break;
                case '5': // Bot Tank
                    Bot_tank.add(new Block(x, y));
                    break;
                case '7': // Player Tank
                    // Assuming you have a player object to track separately
                    player = new PlayerTank(x, y); 
                    break;
                // You could add a case for 'steal' walls here later.
            }
        }
    }
}


}
