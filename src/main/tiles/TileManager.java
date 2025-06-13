package tiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import model.Player;
import ui.App;
import ui.Board;
import ui.Camera;

public class TileManager {
    private Board board;
    private int[][] mapTileNum;
    private TileFactory tileFactory;
    private boolean[][] grassTiles; // Track grass tiles for encounter optimization
    
    public TileManager(Board board, String boardName) {
        this.board = board;
        this.tileFactory = TileFactory.getInstance();
        this.mapTileNum = new int[board.columns][board.rows];
        this.grassTiles = new boolean[board.columns][board.rows];
        loadMap(boardName);
        cacheGrassTiles(); // Pre-cache grass tiles for faster lookup
    }
    
    // Cache which tiles are grass for faster lookup during gameplay
    private void cacheGrassTiles() {
        for (int col = 0; col < board.columns; col++) {
            for (int row = 0; row < board.rows; row++) {
                grassTiles[col][row] = tileFactory.isTallGrass(mapTileNum[col][row]);
            }
        }
    }
    
    public void loadMap(String boardName) {
        // Try these different path formats until one works
        String pathName = "/resources/maps/" + boardName + ".txt";
        InputStream is = null;

        
        is = getClass().getClassLoader().getResourceAsStream(pathName);
        if (is != null) {
            System.out.println("Found resource at: " + pathName);
        }
        
        // Try with Class.getResourceAsStream as fallback
        is = getClass().getResourceAsStream(pathName);
        if (is != null) {
            System.out.println("Found resource at: " + pathName);
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
            
            // After loading the map, update the grass tiles cache
            cacheGrassTiles();
        } catch (Exception e) {
            fillWithDefaultTiles();
            e.printStackTrace();
        }
    }    
    
    // Helper method to fill with default tiles
    private void fillWithDefaultTiles() {
        for (int i = 0; i < board.columns; i++) {
            for (int j = 0; j < board.rows; j++) {
                mapTileNum[i][j] = TileFactory.GRASS; // Default to grass
            }
        }
        // Update grass tiles cache
        cacheGrassTiles();
    }
    
    public void draw(Graphics2D g2d) {
        // Set rendering hints for pixel-perfect rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        
        int tileSize = Board.TILE_SIZE; // Use logical tile size
        
        // Calculate which tiles are visible based on camera position
        Camera camera = Camera.getInstance();
        
        int cameraX = camera.getX();
        int cameraY = camera.getY();
        
        // Calculate visible tile range in logical coordinates
        int startCol = Math.max(0, cameraX / tileSize);
        int startRow = Math.max(0, cameraY / tileSize);
        
        // Calculate viewport dimensions in logical coordinates
        int viewportWidth = App.CURRENT_WIDTH / App.getZoomLevel();
        int viewportHeight = App.CURRENT_HEIGHT / App.getZoomLevel();
        
        int tilesInViewportX = (viewportWidth / tileSize) + 2;
        int tilesInViewportY = (viewportHeight / tileSize) + 2;
        
        int endCol = Math.min(board.columns, startCol + tilesInViewportX);
        int endRow = Math.min(board.rows, startRow + tilesInViewportY);
        
        // Draw visible tiles in logical coordinates
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int tileNum = mapTileNum[col][row];
                
                Tile tile = tileFactory.getTile(tileNum);
                
                if (tile != null) {
                    BufferedImage tileImage = tile.getImage();
                    
                    // Calculate exact pixel positions in logical coordinates
                    int pixelX = col * tileSize;
                    int pixelY = row * tileSize;
                    
                    // Draw with logical tile size (zoom scaling handled elsewhere)
                    g2d.drawImage(tileImage, pixelX, pixelY, tileSize, tileSize, null);
                }
            }
        }
    }
    public boolean isTileCollision(int col, int row) {
        if (col < 0 || col >= board.columns || row < 0 || row >= board.rows) {
            return true; // Out of bounds is considered collision
        }
        
        int tileNum = mapTileNum[col][row];
        Tile tile = tileFactory.getTile(tileNum);
        return tile.hasCollision();
    }

    public void update() {
        // Update all animated tiles
        tileFactory.updateAnimations();
    }

    public boolean isInTallGrass(int col, int row) {
        if (col < 0 || col >= board.columns || row < 0 || row >= board.rows) {
            return false;
        }
        
        // Use cached grass tiles for faster lookup
        return grassTiles[col][row];
    }

    public boolean isPlayerInTallGrass(Player player) {
        // Get player's bounds
        Rectangle playerBounds = player.getBounds(Board.TILE_SIZE);
        
        // Convert pixel coordinates to tile coordinates
        int startTileX = playerBounds.x / Board.TILE_SIZE;
        int startTileY = playerBounds.y / Board.TILE_SIZE;
        int endTileX = (playerBounds.x + playerBounds.width - 1) / Board.TILE_SIZE;
        int endTileY = (playerBounds.y + playerBounds.height - 1) / Board.TILE_SIZE;
        
        // Check all tiles that the player's bounds intersect with
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                if (isInTallGrass(tileX, tileY)) {
                    return true; // Found grass
                }
            }
        }
        
        return false; // No grass found
    }

    public boolean isTileGrass(int col, int row) {
        return isInTallGrass(col, row);
    }
}
