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
    static boolean gameOver = false; // Tracks whether the game is over

    static Random rand = new Random();
    static Robot robot; 

    // 2D array to track the contents of the board
    // 1-8 represents numbered tiles
    // 0 represented an empty tile
    // -1 represents an unopened tile 
    // -2 represents a mine
    static int[][] board = new int[boardWidth][boardHeight]; 

    static void init() throws Throwable{
        System.out.println("Initializing Solver");
        robot = new Robot();
        findFirstTile(); 
        // Initializes board with its default values
        board = new int[boardWidth][boardHeight]; 
        for (int i=0; i<boardWidth; i++) 
            for (int j=0; j<boardWidth; j++) 
                board[i][j] = -1;
        click(0, 0);
        updateBoard();
    }
    // Takes a screenshot of the current screen
    static BufferedImage screenshot(){
        try {
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
    // Prints out board for debugging
    static void printBoard() {
        for (int i=0; i<boardWidth; i++) {
            for (int j=0; j<boardHeight; j++) 
                System.out.print(board[i][j] + "\t");
            System.out.println("");
        }
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

    // Clicks specified tile
    static void click(int i, int j) throws Throwable {
        // Converts i and j to a pizel point
        int x = firstTileX + i * 16; 
        int y = firstTileY + j * 16;
        robot.mouseMove(x, y);
    
        robot.mousePress(1024); // Left click
        Thread.sleep(5); // wait
        robot.mouseRelease(1024);
        Thread.sleep(10);
    }

    // Chords specified tile
    static void chord(int i, int j) throws Throwable {
        // Converts i and j to a pizel point
        int x = firstTileX + i * 16; 
        int y = firstTileY + j * 16;
        robot.mouseMove(x, y);
    
        robot.mousePress(1024); // Left click
        robot.mousePress(4096); // Right click
        Thread.sleep(5); // wait
        robot.mouseRelease(1024);
        robot.mouseRelease(4096);
        Thread.sleep(10);
    }

    // Flags specified tile
    static void flag(int i, int j) throws Throwable {
        // Converts i and j to a pizel point
        int x = firstTileX + i * 16; 
        int y = firstTileY + j * 16;
        robot.mouseMove(x, y);
    
        robot.mousePress(4096); // Right click
        Thread.sleep(5); // wait
        robot.mouseRelease(4096);
        Thread.sleep(10);

        board[i][j] = -2; // Updates the stored board with the flagged mine
        // Calls chord on all 8 surrounding tiles 
        // MAKE SURE THIS DOESN'T CAUSE ANY ISSUES LIKE UNFLAGGING A MINE
        chord(i+1, j);
        chord(i+1, j+1);
        chord(i+1, j-1);
        chord(i, j+1);
        chord(i, j-1);
        chord(i-1, j);
        chord(i-1, j+1);
        chord(i+1, j-1);
    }

    // Counts number of unopened tiles around and mines them if they match number
    // Maybe find a way to make this prettier later
    static boolean countSurrounding(int i, int j) throws Throwable {
        int count = 0;
        // See if we're on the edge of the board
        boolean l = false, r = false, u = false, d = false;
        if(i == 0) l = true;
        if(j == 0) u = true;
        if(i == boardHeight-1) r = true;
        if(j == boardWidth-1) d = true;

        if (!r && (board[i+1][j] == -1 || board[i+1][j] == -2)) {count++;}
        if (!r && !d && (board[i+1][j+1] == -1 || board[i+1][j+1] == -2)) {count++;}
        if (!r && !u && (board[i+1][j-1] == -1 || board[i+1][j-1] == -2)) {count++;}
        if (!d && (board[i][j+1] == -1 || board[i][j+1] == -2)) {count++;}
        if (!u && (board[i][j-1] == -1 || board[i][j-1] == -2)) {count++;}
        if (!l && (board[i-1][j] == -1 || board[i-1][j] == -2)) {count++;}
        if (!l && !d && (board[i-1][j+1] == - 1 || board[i-1][j+1] == -2)) {count++;}
        if (!l && !u && (board[i-1][j-1] == -1 || board[i-1][j-1] == -2)) {count++;}

        // Fix this for edge cases later
        if (count == board[i][j]) { // If the number of unopened tiles matches the number, flag all mines
            if (!r && board[i+1][j] == -1) {flag(i+1, j);}
            if (!r && !d && board[i+1][j+1] == -1) {flag(i+1, j+1);}
            if (!r && !u && board[i+1][j-1] == -1) {flag(i+1, j-1);}
            if (!d && board[i][j+1] == -1) {flag(i, j+1);}
            if (!u && board[i][j-1] == -1) {flag(i, j-1);}
            if (!l && board[i-1][j] == -1) {flag(i-1, j);}
            if (!l && !d && board[i-1][j+1] == -1) {flag(i-1, j+1);}
            if (!l && !u && board[i-1][j-1] == -1) {flag(i-1, j-1);}
            return true;
        }
        return false;
    }


    // Tests whether a tile has been cleared or not
    static boolean isCleared(int i, int j, BufferedImage image) {
        // Converts i and j to a pixel point
        int x = firstTileX + i * 16; 
        int y = firstTileY + j * 16;
        // int k = 0;
        // while (matchesTile(image.getRGB(x-k, y))) // Iterates backwards to find border of tile
        //     k++;
        
        if ((image.getRGB(x-6, y) & 0xff) > 192) // if the left border is white, tile is uncleared
            return false;
        return true; // If left border is not white, tile is clearned
    }
    // Identifies the current tile
    // 1-8 represents numbered tiles
    // 0 represented an empty tile
    // -1 represents an unopened tile 
    // -2 represents a mine
    static int identify(int i, int j, BufferedImage image) {
        if (board[i][j] == -2) {return -2;} // If tile is flagged, ignore it
        // Converts i and j to a pixel point
        int x = firstTileX + i * 16; 
        int y = firstTileY + j * 16;

        // takes a 4x4 area of pixels from the center of the tile
        int pixels[] = new int[16];
        int count = 0;
        for(int n=x; n<x+4; n++)
            for(int m=y; m<y+4; m++) {
                pixels[count] = image.getRGB(n,m);
                count++;
            }   

        for(int color: pixels) {

            int red = (color & 0xff0000) >> 16;
            int green = (color & 0xff00) >> 8;
            int blue = color & 0xff;

            if (!matchesTile(color)) { // Ignores the background tile color

                if (blue >= 200) {return 1;}
                if (red >= 200) {return 3;}
                if (red >= 100 && green >= 100 && blue >= 100) {return 8;}
                if (green >= 100 && blue >=100) {return 6;}
                if (green >=100) {return 2;}
                if (blue >= 100) {return 4;}
                if (red >= 100) {return 5;};
                if (red <= 100 && green <= 100 && blue <= 100) {return 7;}
            }
        }
        // Need to differentiate between opened and unopened tile
        if (!isCleared(i, j, image)) {return -1;} 
        return 0;
    }

    // Updates the content of board to match the game
    static void updateBoard() {
        BufferedImage image = screenshot();
        for (int i=0; i<boardWidth; i++) 
            for (int j=0; j<boardWidth; j++) 
                board[j][i] = identify(i, j, image);
    }

    // Randomly clicks on an unopned tile, also tests whether the game is over 
    // Might want to make more efficient later and make prettier
    static void guess() throws Throwable {
        int openCount = 0;
        for (int i=0; i<boardWidth; i++)  // Counts unopened tiles
            for (int j=0; j<boardWidth; j++) 
                if (board[i][j] == -1)
                    openCount++;

        if (openCount == 0)
            gameOver = true;
        
        int randomTile = rand.nextInt(openCount); // Picks an unopened tile at random 
        for (int i=0; i<boardWidth; i++) 
            for (int j=0; j<boardWidth; j++) {
                if (board[i][j] == -1) {
                    if (randomTile == 0) {
                        click(i, j);
                        // Check to see if the guess lost the game
                        BufferedImage image = screenshot();
                        // Converts i and j to a pixel point
                        int x = firstTileX + i * 16; 
                        int y = firstTileY + j * 16;
                        // Game is lost when the tile turns red
                        if (((image.getRGB(x+6,y+6) & 0xff0000) >> 16) > 200) 
                            gameOver = true;
                        return;
                    }
                    randomTile--;
                }
            }


    }

    // Basic solving algorithm
    static void simpleSolve() throws Throwable {
        init(); // Initializes solver
        boolean changeMade = false;
        while (!gameOver) { // Runs as long as the game is not over
            for (int i=0; i<boardWidth; i++) 
                for (int j=0; j<boardWidth; j++) {
                    
                    changeMade = countSurrounding(i, j);
                }
            if (!changeMade) {
                System.out.println("Guessing");
                guess();
                Thread.sleep(1000);
            }
                 
            updateBoard();
            changeMade = false;
            //Thread.sleep(10000);
        }
        System.out.println("Game Over");
    }
    // Main method
    public static void main(String args[]) throws Throwable {  
        simpleSolve();




        // //For testing colors
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
