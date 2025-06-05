package model;

import java.awt.Point;

import ui.Board;

public class BerryTree extends InteractableObject {
    private Berry.BerryType berryType;
    private int currentBerries;
    private int maxBerries;
    private long lastHarvestTime;
    private long regrowthTimeMs;
    
    public BerryTree(Point position, Berry.BerryType berryType, int maxBerries) {
        super(position, "/" + generateTreeSpritePath(berryType), Direction.ANY);
        this.berryType = berryType;
        this.maxBerries = maxBerries;
        this.currentBerries = maxBerries;
        this.regrowthTimeMs = 30000; // time in miliseconds to regrow
        this.lastHarvestTime = 0;
        
        // Add validation similar to InteractableItem
        if (this.sprite == null) {
            System.err.println("Failed to load berry tree sprite: " + generateTreeSpritePath(berryType));
        } else {
            System.out.println("Successfully loaded berry tree sprite for: " + berryType.getName());
        }
    }
    
    @Override
    public void performAction(Player player, Board board) {
        checkBerryRegrowth();
        
        if (currentBerries > 0) {
            // Determine how many berries to give (1-3 random)
            int berriesGiven = currentBerries;
            
            // Add berries to player inventory
            for (int i = 0; i < berriesGiven; i++) {
                player.addToInventory(berryType.getName());
            }
            
            currentBerries -= berriesGiven;
            lastHarvestTime = System.currentTimeMillis();
            
            String message = berriesGiven == 1 ? 
                "You picked " + berriesGiven + " " + berryType.getName() + "!" :
                "You picked " + berriesGiven + " " + berryType.getName() + "s!";
            System.out.println(message);
            
            // Update sprite to reflect current berry state
            updateSprite();
        } else {
            System.out.println("This tree has no berries right now. Come back later!");
        }
    }
    
    public void checkBerryRegrowth() {
        if (currentBerries < maxBerries && lastHarvestTime > 0) {
            long timeSinceHarvest = System.currentTimeMillis() - lastHarvestTime;
            if (timeSinceHarvest >= regrowthTimeMs) {
                currentBerries = maxBerries;
                updateSprite();
                System.out.println("The " + berryType.getName() + " tree has grown new berries!");
            }
        }
    }
    
    private void updateSprite() {
        String spritePath = currentBerries > 0 ? 
            generateTreeSpritePath(berryType) : 
            generateEmptyTreeSpritePath(berryType);
        try {
            java.io.InputStream imageStream = getClass().getClassLoader().getResourceAsStream(spritePath);
            if (imageStream != null) {
                this.sprite = javax.imageio.ImageIO.read(imageStream);
                imageStream.close();
                System.out.println("Successfully updated berry tree sprite: " + spritePath);
            } else {
                System.err.println("Berry tree sprite resource not found: " + spritePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading berry tree sprite: " + e.getMessage());
        }
        width = sprite.getWidth(null);
        height = sprite.getHeight(null);
    }

    private static String generateTreeSpritePath(Berry.BerryType berryType) {
        String normalizedName = berryType.getName().replace(" ", "_").toLowerCase();
        return "resources/trees/" + normalizedName + "_tree_full.png";
    }
    
    private static String generateEmptyTreeSpritePath(Berry.BerryType berryType) {
        String normalizedName = berryType.getName().replace(" ", "_").toLowerCase();
        return "resources/trees/" + normalizedName + "_tree_empty.png";
    }    
    
    // Getters
    public int getCurrentBerries() { return currentBerries; }
    public int getMaxBerries() { return maxBerries; }
    public Berry.BerryType getBerryType() { return berryType; }
    public long getTimeUntilRegrowth() {
        if (currentBerries >= maxBerries || lastHarvestTime == 0) return 0;
        long elapsed = System.currentTimeMillis() - lastHarvestTime;
        return Math.max(0, regrowthTimeMs - elapsed);
    }
}