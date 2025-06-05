package model;

import java.awt.Point;
import java.util.Random;

import ui.Board;

public class BerryTree extends InteractableObject {
    private Berry.BerryType berryType;
    private int currentBerries;
    private int maxBerries;
    private long lastHarvestTime;
    private long regrowthTimeMs; // Time in milliseconds for berries to regrow
    private Random random = new Random();
    
    public BerryTree(Point position, Berry.BerryType berryType, int maxBerries) {
        super(position, generateTreeSpritePath(berryType), Direction.ANY);
        this.berryType = berryType;
        this.maxBerries = maxBerries;
        this.currentBerries = maxBerries; // Start with full berries
        this.regrowthTimeMs = 300000; // 5 minutes for testing (adjust as needed)
        this.lastHarvestTime = 0;
    }
    
    @Override
    public void performAction(Player player, Board board) {
        checkBerryRegrowth();
        
        if (currentBerries > 0) {
            // Determine how many berries to give (1-3 random)
            int berriesGiven = Math.min(random.nextInt(3) + 1, currentBerries);
            
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
    
    private void checkBerryRegrowth() {
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
        // Update sprite based on berry availability
        String spritePath = currentBerries > 0 ? 
            generateTreeSpritePath(berryType) : 
            generateEmptyTreeSpritePath(berryType);
        
        loadSprite(spritePath);
    }
    
    private static String generateTreeSpritePath(Berry.BerryType berryType) {
        // Match your actual file naming convention: oran_berry_tree_...
        String normalizedName = berryType.getName().replace(" ", "_").toLowerCase();
        return "/resources/trees/" + normalizedName + "_tree_full.png";
    }
    
    private static String generateEmptyTreeSpritePath(Berry.BerryType berryType) {
        // Match your actual file naming convention: oran_berry_tree_...
        String normalizedName = berryType.getName().replace(" ", "_").toLowerCase();
        return "/resources/trees/" + normalizedName + "_tree_empty.png";
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