package tiles;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import ui.Board;

public class TileManager {
    private Board board;
    private Tile[] tiles;
    private int[][] mapTileNum;
    
    public TileManager(Board board, String boardName) {
        this.board = board;
        tiles = new Tile[10]; // Support up to 10 different tile types
        mapTileNum = new int[board.columns][board.rows];
        loadTiles();
        loadMap(boardName); // Pass just the board name
    }
    
    
    public void loadTiles() {
        // Load different tile types
        tiles[0] = new GrassTile(); // Index 0 is grass
        tiles[1] = new WaterTile(); // Index 1 is water
        // Add more tile types as needed
    }
    
    public void loadMap(String boardName) {
        // Try these different path formats until one works
        String[] pathFormats = {
            "maps/" + boardName + ".txt",              // Option 1
            "/maps/" + boardName + ".txt",             // Option 2
            "resources/maps/" + boardName + ".txt",    // Option 3
            "/resources/maps/" + boardName + ".txt"    // Option 4
        };
        
        InputStream is = null;
        for (String path : pathFormats) {
            // Try with ClassLoader first (usually works best)
            is = getClass().getClassLoader().getResourceAsStream(path);
            if (is != null) {
                System.out.println("Found resource at: " + path);
                break;
            }
            
            // Try with Class.getResourceAsStream as fallback
            is = getClass().getResourceAsStream(path);
            if (is != null) {
                System.out.println("Found resource at: " + path);
                break;
            }
        }
        
        if (is == null) {
            System.err.println("Could not find map file for: " + boardName);
            // Fill with default grass tiles
            fillWithDefaultTiles();
            return;
        }
        
        // Continue with your existing code to read the file
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            int col = 0;
            int row = 0;
            
            while (row < board.rows) {
                String line = br.readLine();
                
                if (line == null) {
                    // If we run out of lines, fill the rest with grass (0)
                    while (row < board.rows) {
                        while (col < board.columns) {
                            mapTileNum[col][row] = 0;
                            col++;
                        }
                        col = 0;
                        row++;
                    }
                    break;
                }
                
                String[] numbers = line.split(" ");
                
                while (col < board.columns && col < numbers.length) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }
                
                // If the line is shorter than the board width, fill with grass
                while (col < board.columns) {
                    mapTileNum[col][row] = 0;
                    col++;
                }
                
                col = 0;
                row++;
            }
        } catch (Exception e) {
            fillWithDefaultTiles();
            e.printStackTrace();
        }
    }    
    
    // Helper method to fill with default tiles
    private void fillWithDefaultTiles() {
        for (int i = 0; i < board.columns; i++) {
            for (int j = 0; j < board.rows; j++) {
                mapTileNum[i][j] = 0; // Default to grass
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        int tileSize = Board.TILE_SIZE;
        
        // Calculate which tiles are visible based on camera position
        int startCol = Math.max(0, board.getCamera().getX() / tileSize);
        int startRow = Math.max(0, board.getCamera().getY() / tileSize);
        
        // Calculate how many tiles to draw (viewport width/height in tiles + buffer)
        int tilesInViewportX = (board.getWidth() / tileSize) + 2;
        int tilesInViewportY = (board.getHeight() / tileSize) + 2;
        
        int endCol = Math.min(board.columns, startCol + tilesInViewportX);
        int endRow = Math.min(board.rows, startRow + tilesInViewportY);
        
        // Draw visible tiles
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int tileNum = mapTileNum[col][row];
                
                if (tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null) {
                    g2d.drawImage(tiles[tileNum].getImage(), 
                                  col * tileSize, 
                                  row * tileSize, 
                                  tileSize, tileSize, null);
                }
            }
        }
    }
    
    public boolean isTileCollision(int col, int row) {
        if (col < 0 || col >= board.columns || row < 0 || row >= board.rows) {
            return true; // Out of bounds is considered collision
        }
        
        int tileNum = mapTileNum[col][row];
        return tiles[tileNum].hasCollision();
    }
}
