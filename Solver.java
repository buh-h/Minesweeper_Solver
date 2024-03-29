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
    // Easy 8x8, Intermediate 16x16, Expert 16x30
    static int boardWidth = 16;
    static int boardHeight = 16;
    static boolean gameOver = false; // Tracks whether the game is over

    static Random rand = new Random();
    static Robot robot; 

    // 2D array to track the contents of the board
    // 1-8 represents numbered tiles
    // 0 represented a cleared tile
    // -1 represents an unopened tile 
    // -2 represents a mine
    static int[][] board = new int[boardWidth][boardHeight]; 

    static void init() throws Throwable {
        System.out.println("Initializing Solver");
        robot = new Robot();
        findFirstTile(); 
        // Initializes board with its default values
        board = new int[boardHeight][boardWidth]; 
        for (int i=0; i<boardHeight; i++) 
            for (int j=0; j<boardWidth; j++) 
                board[i][j] = -1;
        guess(); //click(0, 0);
        updateBoard();
    }
    // Takes a screenshot of the current screen
    static BufferedImage screenshot() {
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
        for (int i=0; i<boardHeight; i++) {
            for (int j=0; j<boardWidth; j++) 
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
                    firstTileY = j+k+5;
                    firstTileX = i+k+5;
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
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;
        robot.mouseMove(x, y);
    
        robot.mousePress(1024); // Left click
        Thread.sleep(5); // wait
        robot.mouseRelease(1024);
        Thread.sleep(10);
    }

    // Chords specified tile
    static void chord(int i, int j) throws Throwable {
        // Converts i and j to a pixel point
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;
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
        // Converts i and j to a pixel point
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;
        robot.mouseMove(x, y);
    
        robot.mousePress(4096); // Right click
        Thread.sleep(5); // wait
        robot.mouseRelease(4096);
        Thread.sleep(10);

        board[i][j] = -2; // Updates the stored board with the flagged mine
    }

    // Checks whether a coordinate is on the board
    static boolean isOnBoard(int i, int j) {
        return (i>=0 && j>=0 && i<boardHeight && j<boardWidth);
    }
    // Counts number of unopened tiles around a tile
    // Maybe find a way to make this prettier later
    static int countUnopened(int i, int j) {
        int count = 0;

        if (isOnBoard(i+1, j) && board[i+1][j] == -1) count++;
        if (isOnBoard(i+1, j+1) && board[i+1][j+1] == -1) count++;
        if (isOnBoard(i+1, j-1) && board[i+1][j-1] == -1) count++;
        if (isOnBoard(i, j+1) &&  board[i][j+1] == -1) count++;
        if (isOnBoard(i, j-1) && board[i][j-1] == -1) count++;
        if (isOnBoard(i-1, j) && board[i-1][j] == -1) count++;
        if (isOnBoard(i-1, j+1) && board[i-1][j+1] == - 1 ) count++;
        if (isOnBoard(i-1, j-1) && board[i-1][j-1] == -1) count++;

        return count;
    }
    // Counts number of unopened tiles around a tile for a passed board
    static int countUnopenedCopy(int i, int j, int[][] copy) {
        int count = 0;

        if (isOnBoard(i+1, j) && copy[i+1][j] == -1) count++;
        if (isOnBoard(i+1, j+1) && copy[i+1][j+1] == -1) count++;
        if (isOnBoard(i+1, j-1) && copy[i+1][j-1] == -1) count++;
        if (isOnBoard(i, j+1) &&  copy[i][j+1] == -1) count++;
        if (isOnBoard(i, j-1) && copy[i][j-1] == -1) count++;
        if (isOnBoard(i-1, j) && copy[i-1][j] == -1) count++;
        if (isOnBoard(i-1, j+1) && copy[i-1][j+1] == - 1 ) count++;
        if (isOnBoard(i-1, j-1) && copy[i-1][j-1] == -1) count++;

        return count;
    }

    // Counts number of mines around a tile
    // Maybe find a way to make this prettier later
    static int countMines(int i, int j) {
        int count = 0;

        if (isOnBoard(i+1, j) && board[i+1][j] == -2) count++;
        if (isOnBoard(i+1, j+1) && board[i+1][j+1] == -2) count++;
        if (isOnBoard(i+1, j-1) && board[i+1][j-1] == -2) count++;
        if (isOnBoard(i, j+1) &&  board[i][j+1] == -2) count++;
        if (isOnBoard(i, j-1) && board[i][j-1] == -2) count++;
        if (isOnBoard(i-1, j) && board[i-1][j] == -2) count++;
        if (isOnBoard(i-1, j+1) && board[i-1][j+1] == -2) count++;
        if (isOnBoard(i-1, j-1) && board[i-1][j-1] == -2) count++;

        return count;
    }

    // Counter number of mines around a tile for a passed board
    static int countMinesCopy(int i, int j, int[][] copy) {
        int count = 0;

        if (isOnBoard(i+1, j) && copy[i+1][j] == -2) count++;
        if (isOnBoard(i+1, j+1) && copy[i+1][j+1] == -2) count++;
        if (isOnBoard(i+1, j-1) && copy[i+1][j-1] == -2) count++;
        if (isOnBoard(i, j+1) &&  copy[i][j+1] == -2) count++;
        if (isOnBoard(i, j-1) && copy[i][j-1] == -2) count++;
        if (isOnBoard(i-1, j) && copy[i-1][j] == -2) count++;
        if (isOnBoard(i-1, j+1) && copy[i-1][j+1] == -2) count++;
        if (isOnBoard(i-1, j-1) && copy[i-1][j-1] == -2) count++;

        return count;
    }

    // Counts the number of cleared tiles that surround the 


    // Tests whether a tile has been cleared or not
    static boolean isCleared(int i, int j, BufferedImage image) {
        // Converts i and j to a pixel point
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;
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
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;

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

                // Checking color of tile to see what number it matches
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

    // Flags and opens all obvious tiles, returns true if a change was made, false otherwise
    static boolean clearEasyTiles() throws Throwable{
        boolean changeMade = false;
        for (int i=0; i<boardHeight; i++)
            for (int j=0; j<boardWidth; j++) {
                // If the number on a tile is the same as the number of unopened tiles, accounting for mines 
                if (board[i][j] > 0 && board[i][j] == countUnopened(i,j) + countMines(i, j) && countUnopened(i,j) != 0) { 
                    // Flags all surrounding unopened tiles
                    if (isOnBoard(i+1, j) && board[i+1][j] == -1) flag(i+1, j);
                    if (isOnBoard(i+1, j+1) && board[i+1][j+1] == -1) flag(i+1, j+1);
                    if (isOnBoard(i+1, j-1) && board[i+1][j-1] == -1) flag(i+1, j-1);
                    if (isOnBoard(i, j+1) &&  board[i][j+1] == -1) flag(i, j+1);
                    if (isOnBoard(i, j-1) && board[i][j-1] == -1) flag(i, j-1);
                    if (isOnBoard(i-1, j) && board[i-1][j] == -1) flag(i-1, j);
                    if (isOnBoard(i-1, j+1) && board[i-1][j+1] == - 1 ) flag(i-1, j+1);
                    if (isOnBoard(i-1, j-1) && board[i-1][j-1] == -1) flag(i-1, j-1);

                    changeMade = true;
                }
                // If the number of mines matches the number on a tile and there are unopened tiles
                if (board[i][j] > 0 && board[i][j] == countMines(i, j) && countUnopened(i,j) != 0) {
                    chord(i, j); // Chords the tile
                    changeMade = true;
                }
            }

        return changeMade;
    }
    // Updates the content of board to match the game
    static void updateBoard() throws Throwable{
        robot.mouseMove(0, 0); // Moves mouse out of way
        Thread.sleep(10);
        BufferedImage image = screenshot();
        for (int i=0; i<boardHeight; i++) 
            for (int j=0; j<boardWidth; j++) {
                if (board[i][j] != -2)
                    board[i][j] = identify(i, j, image);
            }
    }

    // Randomly clicks on an unopned tile, also tests whether the game is over 
    // Might want to make more efficient later and make prettier
    static void guess() throws Throwable {
        // counts clearable tiles
        boolean clearableTile = false;
        for (int i=0; i<boardHeight; i++) 
            for (int j=0; j<boardWidth; j++) 
                if (board[i][j] == -1) 
                    clearableTile = true;
        if (!clearableTile) { // If there are no clearable tilees game is over
            gameOver = true; 
            return;
        }

        Random rand = new Random();
        int i = rand.nextInt(boardWidth);
        int j = rand.nextInt(boardHeight);
        while (board[i][j] != -1) { // Generates random numbers until an open tile is found
            i = rand.nextInt(boardWidth);
            j = rand.nextInt(boardHeight);
        }
        click(i, j);
        
        // Test whether the guess lost the game
        BufferedImage image = screenshot();
        // Converts i and j to a pixel point
        int x = firstTileX + j * 16; 
        int y = firstTileY + i * 16;
        // Gets the color of the edge of the tile
        int color = image.getRGB(x+6,y+6);
        int red = (color & 0xff0000) >> 16;
        if (red >= 200) {
            gameOver = true; // If the edge is red the game was lost
            //printBoard();
        }

    }

    static void test() throws Throwable {
        init();
        while (clearEasyTiles()) {updateBoard(); printBoard();}
    }
    // Basic solving algorithm
    static void simpleSolve() throws Throwable {
        init(); // Initializes solver
        boolean changeMade = false;
        while (!gameOver) { // Runs as long as the game is not over
            changeMade = clearEasyTiles(); // Clears all "obvious tiles"  
            if (!changeMade) { // Makes a gues when all else fails
                guess();
                System.out.println("Guessing");
                //printBoard();
                //Thread.sleep(1000); // for testing
            }
                 
            updateBoard();
            changeMade = false;
            //Thread.sleep(1000); // for testing
        }
        System.out.println("Game Over");
    }

    // Returns a list of all surrounding unopened tiles
    public static ArrayList<Point> listUnopened(int i, int j, int[][] b) {
        ArrayList<Point> list = new ArrayList<Point>();
        // Adds surrounding unopened tiles to the list 
        if (isOnBoard(i+1, j) && b[i+1][j] == -1) list.add(new Point(i+1, j));
        if (isOnBoard(i+1, j+1) && b[i+1][j+1] == -1) list.add(new Point(i+1, j+1));
        if (isOnBoard(i+1, j-1) && b[i+1][j-1] == -1) list.add(new Point(i+1, j-1));
        if (isOnBoard(i, j+1) &&  b[i][j+1] == -1) list.add(new Point(i, j+1));
        if (isOnBoard(i, j-1) && b[i][j-1] == -1) list.add(new Point(i, j-1));
        if (isOnBoard(i-1, j) && b[i-1][j] == -1) list.add(new Point(i-1, j));
        if (isOnBoard(i-1, j+1) && b[i-1][j+1] == - 1 ) list.add(new Point(i-1, j+1));
        if (isOnBoard(i-1, j-1) && b[i-1][j-1] == -1) list.add(new Point(i-1, j-1));
        return list;
    }
    // Returns a copy of the current board
    public static int[][] copyBoard() {
        int[][] copy = new int[boardHeight][boardWidth];
        for (int i=0; i<boardHeight; i++)
            for (int j=0; j<boardWidth; j++) {
                copy[i][j] = board[i][j];
            }
        return copy;
    }

    // Modified version of clearEasyTiles to use for testing cases
    // Modify this to just fully clear a test case rather than just iterating rhgouh once 
    // Modify this to add mines or clear tiles to an arraylist, and ensuring each element only gets added once
    // And add all elements of the array list to the maps at end
    public static boolean testCase(HashMap<Point, Integer> clear, HashMap<Point, Integer> mines, int[][] copy) {
        boolean changeMade = true;
        ArrayList<Point> caseClear = new ArrayList<Point>();
        ArrayList<Point> caseMines = new ArrayList<Point>();
        // System.out.println("");
        // for (int i=0; i<boardHeight; i++) {
        //     for (int j=0; j<boardWidth; j++) 
        //         System.out.print(copy[i][j] + "\t");
        //     System.out.println("");
        // }
        while (changeMade) {
            changeMade = false;
            for (int i=0; i<boardHeight; i++)
                for (int j=0; j<boardWidth; j++) {
                    boolean invalid = copy[i][j] > countUnopenedCopy(i,j, copy)+countMinesCopy(i, j, copy) 
                            || copy[i][j] < countMinesCopy(i, j, copy);
                    if (copy[i][j] > 0 && invalid) {
                        for (int x=0;x<8;x++) {
                            for (int y=0;y<8;y++)
                                System.out.print(copy[x][y] + "\t");
                                System.out.println("");
                        }
                        System.out.println("" + i + " " + j);
                        System.out.println(countUnopenedCopy(i,j, copy)+countMinesCopy(i, j, copy));
                        return false;
                    }
                    // If the number on a tile is the same as the number of unopened tiles, accounting for mines 
                    else if (copy[i][j] > 0 && copy[i][j] == countUnopenedCopy(i,j, copy) + countMinesCopy(i, j, copy) 
                            && countUnopenedCopy(i, j, copy) != 0) {
                        
                        changeMade = true;
                        ArrayList<Point> unopened = listUnopened(i, j, copy); // Gets a list of unopened tiles
                        for (Point p: unopened) { // Iterates through the list of unopened tiles
                            copy[p.x][p.y] = -2; // Updates the copy's boardstate
                            if (!caseMines.contains(p)) caseMines.add(p);
                        }
                    }
                    // If the number of mines matches the number on a tile and there are unopened tiles
                    else if (copy[i][j] > 0 && copy[i][j] == countMinesCopy(i, j, copy) 
                            && countUnopenedCopy(i,j, copy) != 0) {
                        changeMade = true;
                        ArrayList<Point> unopened = listUnopened(i, j, copy); // Gets a list of unopened tiles
                        for (Point p: unopened) { // Iterates through the list of unopened tiles
                            copy[p.x][p.y] = 0; // Updates the copy's boardstate
                            if (!caseClear.contains(p)) caseClear.add(p); 
                        }
                    }
                }
        }
        for (Point p: caseClear) 
            if (clear.containsKey(p)) clear.put(p, clear.get(p) + 1);
            // If the key is not in mines, add it to clear with value 1
            else clear.put(p, 1);
        for (Point p: caseMines) 
            if (mines.containsKey(p)) mines.put(p, mines.get(p) + 1);
            // If the key is not in mines, add it to clear with value 1
            else mines.put(p, 1);
        return true;
    }

    // More robust solving algorithm, works by testing cases 
    static void caseSolve() throws Throwable {
        init(); // Initializes solver
        boolean changeMade = false;
        while (!gameOver) { // Runs as long as the game is not over
            changeMade = false;
            while (clearEasyTiles()) updateBoard();

            // Tests cases and compares them against each other
            for (int i=0; i<boardHeight; i++) { // Iterates through board
                for (int j=0; j<boardWidth; j++) {
                    // Creates hashmaps for potentially clear and unclear tiles
                    HashMap<Point, Integer> clear = new HashMap<Point, Integer>();
                    HashMap<Point, Integer> mines = new HashMap<Point, Integer>();
                    int numCases = 0; // Number to keep track of the number of cases being tested
                    int numInvalid = 0; // Number to keep track of the number of invalid cases found
                    // If there is 1 extra unopened tile, ensures there isn't too much uncertainty
                    if (board[i][j]>0 && board[i][j]-countMines(i, j)==countUnopened(i, j)-1) {
                        numCases = countUnopened(i, j);
                        //System.out.println("Enter Case 1 " + i + " " + j + " " + numCases);
                        // Creates cases
                        for (int k=0; k<numCases; k++) { // Runs once for each case
                            ArrayList<Point> caseClear = new ArrayList<Point>();
                            ArrayList<Point> caseMines = new ArrayList<Point>();
                            int[][] boardCopy = copyBoard(); // Makes a copy of the board
                            // clear tile at k, flag the rest of the tiles
                            int index = 0;
                            for (Point p: listUnopened(i, j, board)) {
                                if (k == index) {
                                    boardCopy[p.x][p.y] = 0;
                                    if (!caseClear.contains(p)) caseClear.add(p); 
                                }
                                else {
                                    boardCopy[p.x][p.y] = -2;
                                    if (!caseMines.contains(p)) caseMines.add(p); 
                                }
                                index++;
                            }
                            if (testCase(clear, mines, boardCopy)) {// Solves the case
                                for (Point p: caseClear) 
                                    if (clear.containsKey(p)) clear.put(p, clear.get(p) + 1);
                                    // If the key is not in mines, add it to clear with value 1
                                    else clear.put(p, 1);
                                    for (Point p: caseMines) 
                                    if (mines.containsKey(p)) mines.put(p, mines.get(p) + 1);
                                    // If the key is not in mines, add it to clear with value 1
                                    else mines.put(p, 1);                            
                            } else numInvalid++;
                        }

                    }
                    // If there is one mine and that mine isn't flagged, ensures there isn't too much uncertainty
                    else if (board[i][j]>0 && board[i][j]-countMines(i, j)==1) {
                        numCases = countUnopened(i, j);
                        //System.out.println("Enter Case 2 " + i + " " + j + " " + numCases);
                        // Creates cases
                        for (int k=0; k<numCases; k++) { // Runs once for each case
                            ArrayList<Point> caseClear = new ArrayList<Point>();
                            ArrayList<Point> caseMines = new ArrayList<Point>();
                            int[][] boardCopy = copyBoard(); // Makes a copy of the board
                            // flag tile at k, clear the rest of the tiles
                            int index = 0;
                            for (Point p: listUnopened(i, j, board)) {
                                if (k == index) {
                                    boardCopy[p.x][p.y] = -2;
                                    if (!caseMines.contains(p)) caseMines.add(p); 
                                }
                                else {
                                    boardCopy[p.x][p.y] = 0;
                                    if (!caseClear.contains(p)) caseClear.add(p);
                                }
                                index++;
                            }
                            if (testCase(clear, mines, boardCopy)) {// Solves the case
                                for (Point p: caseClear) 
                                    if (clear.containsKey(p)) clear.put(p, clear.get(p) + 1);
                                    // If the key is not in mines, add it to clear with value 1
                                    else clear.put(p, 1);
                                for (Point p: caseMines) 
                                    if (mines.containsKey(p)) mines.put(p, mines.get(p) + 1);
                                    // If the key is not in mines, add it to clear with value 1
                                    else mines.put(p, 1);
                            } else numInvalid++;
                        }
                    }
                    // Checks whether a tile has been cleared or mines in all cases
                    for (Point p: clear.keySet()) 
                        if (clear.get(p) == numCases-numInvalid) {
                            click(p.x, p.y);
                            changeMade = true;
                            System.out.print(p +" "+ clear.get(p));
                        }

                    for (Point p: mines.keySet()) 
                    if (mines.get(p) == numCases-numInvalid) {
                        flag(p.x, p.y);
                        changeMade = true;
                        System.out.print(p + " " + clear.get(p));
                    }

                    if (changeMade) break; // Breaks out of loop if a change was made
                }
                if (changeMade) break; // Breaks out of loop if a change was made
            }

            // Makes a guess when all else fails
            if (!changeMade) {
                guess();
                System.out.println("Guessing");
            }   
            updateBoard();
        }
        System.out.println("Game Over");
    }

    // Main method
    public static void main(String args[]) throws Throwable{  

        //simpleSolve();
        caseSolve();

        // HashMap<Point, Integer> clear = new HashMap<Point, Integer>();
        // HashMap<Point, Integer> mines = new HashMap<Point, Integer>();
        // init();
        // board[6][3] = -2;
        // board[7][3] = -2;
        // //printBoard();
        // System.out.println("");
        // int[][] copy = copyBoard();
        // copy[2][1] = -2;
        
        // System.out.println(testCase(clear, mines, copy));
        // for (int i=0; i<boardHeight; i++) {
        //     for (int j=0; j<boardWidth; j++) 
        //         System.out.print(copy[i][j] + "\t");
        //     System.out.println("");
        // }
        // System.out.println(clear);

        // int[][] copy2 = copyBoard();
        // copy2[2][0] = -2;
        
        // System.out.println(testCase(clear, mines, copy2));
        // for (int i=0; i<boardHeight; i++) {
        //     for (int j=0; j<boardWidth; j++) 
        //         System.out.print(copy2[i][j] + "\t");
        //     System.out.println("");
        // }
        // System.out.println(clear);
    }  
} 
