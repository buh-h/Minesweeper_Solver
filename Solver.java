import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Solver {

    // Tracks dimensions of screen
    static int ScreenWidth = 1920; 
    static int ScreenHeight = 1080;
    // Tracks the coordinates of the top-left tile
    // Each tile is 16 x 16 pixels, light grey part is 12x12
    static int firstTileX = 0;
    static int firstTileY = 0;
    // Tracks the dimension of the board
    // Currently set for easy (8x8)
    static int boardWidth = 8;
    static int boardHeight = 8;

    // 2D array to track the contents of the board
    // 1-8 represents numbered tiles
    // 0 represented an empty tile
    // -1 represents an unopened tile 
    // -2 represents a mine
    static int[][] board = new int[boardWidth][boardHeight]; 

    static void init() {
        System.out.println("Initializing Solver");
        findFirstTile(); 
        // Initializes board with its default values
        board = new int[boardWidth][boardHeight]; 
        for (int i=0; i<boardWidth; i++) 
            for (int j=0; j<boardWidth; j++) 
                board[i][j] = -1;
    }
    // Takes a screenshot of the current screen
    static BufferedImage screenshot(){
        try {
            Robot robot = new Robot();
            Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            ScreenWidth = captureSize.width;
            ScreenHeight = captureSize.height;
            BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
            return bufferedImage;
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
    
    // Tests whether the rgb value matches the grey border
    static boolean matchesBorder(int color) {
        int[] rgb = new int[] {(color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff};
        return Arrays.equals(rgb, new int[] {128, 128, 128});
    }

    // Tests whether the rgb value matches the grey tile
    static boolean matchesTile(int color) {
        int[] rgb = new int[] {(color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff};
        return Arrays.equals(rgb, new int[] {192, 192, 192});
    }

    // Finds the length of a tile for ease of programming (not used in program)
    static int tileLength()
    {
        BufferedImage image = screenshot();
        boolean offGrey = false;
        for (int i=0; i<ScreenWidth; i++) {
            if (!matchesTile(image.getRGB(firstTileX+i,firstTileY)))
                return i;//offGrey = true;
            if (offGrey && matchesTile(image.getRGB(firstTileX+i,firstTileY)))
                return i;
        }
        return -1;
    }
    
    // Finds the coordinates of the top-leftmost tile 
    static void findFirstTile() {

        BufferedImage image = screenshot();
        boolean borderFound = false;
        //Iterates through screen pixels
        for (int i=0; i<ScreenWidth; i++) {
            for (int j=0; j<ScreenHeight; j++) {
                // Find grey border in top left, ensuring it is the game border by testing squares around it
                if (!borderFound && matchesBorder(image.getRGB(i,j))
                        && matchesBorder(image.getRGB(i+3,j)) && matchesBorder(image.getRGB(i,j+3))
                        && matchesBorder(image.getRGB(i+7,j)) && matchesBorder(image.getRGB(i,j+7))
                        && matchesBorder(image.getRGB(i+2,j+2)) && matchesBorder(image.getRGB(i+2,j+2))) {
                    borderFound = true;
                    // Find the corner of the first individual tile by iterating diagonally from i,j
                    int k = 1;
                    while (!matchesTile(image.getRGB(i+k, j+k))) 
                        k++;
                    // Stores the rough location of the center of the first tile
                    firstTileX = i+k+5;
                    firstTileY = j+k+5;
                    break;
                }
            }
            if (borderFound)
                break;
        }
    }

    // Identifies the current tile

    // Main method
    public static void main(String args[]) {  
        init(); // Initializes solver
        System.out.println(tileLength());







        // For testing colors
        // BufferedImage image = screenshot();
        // Point p;
        // while (true) {
        //     p = MouseInfo.getPointerInfo().getLocation();
        //     int color = image.getRGB(p.x, p.y);
        //     int blue = color & 0xff;
        //     int green = (color & 0xff00) >> 8;
        //     int red = (color & 0xff0000) >> 16;
        //     //System.out.println(p.x + " " + p.y);
        //     System.out.println("Red: " + red + " Green: " + green + " Blue: " + blue);
        // }
    }  
} 