import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 15 ;
        int columnCount = 15 ; 
        int tileSize = 32 ; 
        int boardWidth = columnCount * tileSize;
        int boardHight = rowCount * tileSize ; 

        javax.swing.JFrame frame = new JFrame (" TANK ");
        //frame.setVisible(true);
        frame.setSize(boardWidth , boardHight );
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        Tank tan = new Tank();
        frame.add (tan);
        frame.pack();
        frame.setVisible(true);



    }
}
