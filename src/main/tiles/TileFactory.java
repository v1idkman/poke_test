package tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class TileFactory {
    private static TileFactory instance;
    private Map<Integer, Tile> tileCache;
    private Map<Integer, String> tiles;
    
    // Tile IDs as constants for easy reference
    public static final int GRASS = 00;
    public static final int GRASS_PATH_TOP = 01;
    public static final int GRASS_PATH_BOTTOM = 02;
    public static final int GRASS_PATH_RIGHT = 03;
    public static final int GRASS_PATH_LEFT = 04;
    public static final int GRASS_PATH_TOP_RIGHT = 05;
    public static final int GRASS_PATH_TOP_LEFT = 06;
    public static final int GRASS_PATH_BOTTOM_RIGHT = 07;
    public static final int GRASS_PATH_BOTTOM_LEFT = 8;
    public static final int GRASS_PATH_TOP_RIGHT_INSIDE = 9;
    public static final int GRASS_PATH_TOP_LEFT_INSIDE = 10;
    public static final int GRASS_PATH_BOTTOM_RIGHT_INSIDE = 11;
    public static final int GRASS_PATH_BOTTOM_LEFT_INSIDE = 12;

    public static final int GRASS_PATCH = 19;

    public static final int WATER = 20;
    public static final int WAVY_WATER = 21;
    public static final int WATER_TO_GRASS_LEFT = 22;

    public static final int DIRT = 40;
    public static final int ICE = 60;
    public static final int HOUSE_GOLD = 80;
    
    // Add more tile constants as needed
    
    private TileFactory() {
        tileCache = new HashMap<>();
        tiles = new HashMap<>();
        addTiles();
    }
    
    public static TileFactory getInstance() {
        if (instance == null) {
            instance = new TileFactory();
        }
        return instance;
    }

    public void addTiles() {
        tiles.put(GRASS, "grass_tile");
        tiles.put(GRASS_PATH_TOP, "grass_path_top");
        tiles.put(GRASS_PATH_BOTTOM, "grass_path_bottom");
        tiles.put(GRASS_PATH_RIGHT, "grass_path_right");
        tiles.put(GRASS_PATH_LEFT, "grass_path_left");
        tiles.put(GRASS_PATH_TOP_RIGHT, "grass_path_top_right");
        tiles.put(GRASS_PATH_TOP_LEFT, "grass_path_top_left");
        tiles.put(GRASS_PATH_BOTTOM_RIGHT, "grass_path_bottom_right");
        tiles.put(GRASS_PATH_BOTTOM_LEFT, "grass_path_bottom_left");
        tiles.put(GRASS_PATH_TOP_RIGHT_INSIDE, "grass_path_top_right_inside");
        tiles.put(GRASS_PATH_TOP_LEFT_INSIDE, "grass_path_top_left_inside");
        tiles.put(GRASS_PATH_BOTTOM_RIGHT_INSIDE, "grass_path_bottom_right_inside");
        tiles.put(GRASS_PATH_BOTTOM_LEFT_INSIDE, "grass_path_bottom_left_inside");
        tiles.put(GRASS_PATCH, "grass_patch");
        tiles.put(WATER, "water_tile_0");
        tiles.put(WAVY_WATER, "wavy_water");
        tiles.put(DIRT, "dirt_tile");
        tiles.put(ICE, "ice_tile");
        tiles.put(HOUSE_GOLD, "house_tile_golden");
        tiles.put(WATER_TO_GRASS_LEFT, "water_to_grass_left");
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
        if (id == WAVY_WATER) { // animations
            BufferedImage[] waterFrames = loadAnimationFrames(
                "/resources/tiles/water_tile_{frame}.png", 4);
            return new AnimatedTile(WAVY_WATER, "Water", waterFrames, 
                true, true, true, 300);
        } else if (tiles.containsKey(id)) { // all static tiles
            String name = tiles.get(id);
            return new Tile(id, name, loadImage("/resources/tiles/" + name + ".png"), false, 
                false, false);
        } else { // unknown tiles
            System.err.println("Unknown tile ID: " + id);
            return new Tile(GRASS, "Default Grass", loadImage("/resources/tiles/grass_tile.png"), 
                false, false, false);
        }
    }
    
    /**
     * Load an image from the specified path
     */
    private BufferedImage loadImage(String path) {
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

    private BufferedImage[] loadAnimationFrames(String basePath, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = basePath.replace("{frame}", String.valueOf(i));
            frames[i] = loadImage(path);
        }
        return frames;
    }

    public void updateAnimations() {
        for (Tile tile : tileCache.values()) {
            if (tile instanceof AnimatedTile) {
                ((AnimatedTile) tile).update();
            }
        }
    }

    public boolean isTallGrass(int tileId) {
        return tileId == GRASS_PATCH;
    }
    
    /**
     * Get all available tiles
     */
    public Tile[] getAllTiles() {
        Tile[] tiles = new Tile[100];
        
        // Pre-load all known tiles
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == null) {
                continue;
            }
            tiles[i] = null;  
        }
        return tiles;
    }
}
