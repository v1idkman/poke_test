package tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class TileFactory {
    private static TileFactory instance;
    private Map<Integer, Tile> tileCache = new HashMap<>();
    
    // Tile IDs as constants for easy reference
    public static final int GRASS = 0;
    public static final int WATER = 1;
    public static final int DIRT = 2;
    public static final int ICE = 3;
    public static final int HOUSE_GOLD = 4;
    public static final int WATER_TO_GRASS_LEFT = 5;
    // Add more tile constants as needed
    
    private TileFactory() {
        // Private constructor for singleton
    }
    
    public static TileFactory getInstance() {
        if (instance == null) {
            instance = new TileFactory();
        }
        return instance;
    }
    
    /**
     * Get a tile by ID, creating it if it doesn't exist in the cache
     */
    public Tile getTile(int id) {
        if (tileCache.containsKey(id)) {
            return tileCache.get(id);
        }
        
        Tile newTile = createTile(id);
        if (newTile != null) {
            tileCache.put(id, newTile);
        }
        return newTile;
    }
    
    /**
     * Create a new tile based on ID
     */
    private Tile createTile(int id) {
        switch (id) {
            case GRASS:
                return new Tile(GRASS, "Grass", loadImage("/resources/tiles/grass_tile.png"), false, 
                            false, false);
            case WATER:
                return new Tile(WATER, "Water", loadImage("/resources/tiles/water_tile.png"), true, 
                            true, true);
            case DIRT:
                return new Tile(DIRT, "Dirt", loadImage("/resources/tiles/dirt_tile.png"), false, 
                            false, false);
            case ICE:
                return new Tile(ICE, "Ice", loadImage("/resources/tiles/ice_tile.png"), false, 
                            false, false);
            case HOUSE_GOLD:
                return new Tile(HOUSE_GOLD, "House Gold", loadImage("/resources/tiles/house_tile_golden.png"), false, 
                            false, false);
            case WATER_TO_GRASS_LEFT:
                return new Tile(WATER_TO_GRASS_LEFT, "Water to Grass Side Left", 
                    loadImage("/resources/tiles/water_to_grass_left.png"), true, true, false);
            // Add more cases for additional tile types
            default:
                System.err.println("Unknown tile ID: " + id);
                return new Tile(GRASS, "Default Grass", loadImage("/resources/tiles/grass_tile.png"), false, false, false);
        }
    }
    
    /**
     * Load an image from the specified path
     */
    private BufferedImage loadImage(String path) {
        System.out.println("Attempting to load: " + path);
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Resource not found: " + path);
            // Return fallback image
        } else {
            try {
                return ImageIO.read(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return ImageIO.read(getClass().getResourceAsStream(path));
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load tile image: " + path);
            e.printStackTrace();
            
            // Create a simple colored image as fallback
            BufferedImage fallback = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    fallback.setRGB(x, y, 0xFF00FF); // Magenta for missing textures
                }
            }
            return fallback;
        }
    }
    
    /**
     * Get all available tiles
     */
    public Tile[] getAllTiles() {
        Tile[] tiles = new Tile[30]; // Support up to 30 different tile types
        
        // Pre-load all known tiles
        tiles[GRASS] = getTile(GRASS);
        tiles[WATER] = getTile(WATER);
        tiles[DIRT] = getTile(DIRT);
        tiles[ICE] = getTile(ICE);
        tiles[HOUSE_GOLD] = getTile(HOUSE_GOLD);
        tiles[WATER_TO_GRASS_LEFT] = getTile(WATER_TO_GRASS_LEFT);
        // Add more tiles as needed
        
        return tiles;
    }
}
