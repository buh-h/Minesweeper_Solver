# Minesweeper_Solver
A bot to solve games of minesweeper, this program works on the app MinesweeperX, and the game window must be 
visible the entire time in order for it to work.

# How it works: 

This program uses a Robot object to take screenshots of the screen and reads the color of specific pixes in order to 
find and identify the board. 

In order to solve the board the program first clears easy tiles with easy solutions (i.e. when the number of 
unopened tiles and mines surrounding a tile matches the number on the tile, or if the number of mines surrounding a
tile is already fulfilled). When there are no longer any easy tiles to clear, the program then iterates throguh the board, 
and generates all possible arrangements of mines based on a number tile, and compares the results of each. If a tile is 
safe or mined in all combinations, the program clicks or flags the respectively. When all else fails, the program randomly 
selects an unopened tile to click.

# Future additions: 
- Create a better method for generating all possible combinations, currently only generates possible combinations for specific
common patterns
- Have the program make a more educated guess rather than randomly choosing a tile
