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
        this.regrowthTimeMs = 30000; // time in milliseconds to regrow
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
            // Determine how many berries to give
            int berriesGiven = currentBerries;
            
            // Show initial discovery message
            String discoveryMessage = "You found a " + berryType.getName() + " tree with " + 
                                    currentBerries + " berr" + (currentBerries == 1 ? "y" : "ies") + "!";
            board.showDialogue(discoveryMessage);
            
            // Show options for berry collection
            String[] options = {
                "Take all (" + currentBerries + ")",
                "Take one",
                "Leave them"
            };
            
            board.showDialogueWithOptions("", "What would you like to do?", options, (choice) -> {
                switch (choice) {
                    case 0: // Take all
                        for (int i = 0; i < berriesGiven; i++) {
                            player.addToInventory(berryType.getName());
                        }
                        currentBerries = 0;
                        lastHarvestTime = System.currentTimeMillis();
                        
                        String allMessage = "You picked all " + berriesGiven + " " + 
                                          berryType.getName() + (berriesGiven == 1 ? "" : "s") + "!";
                        board.showDialogue(allMessage);
                        updateSprite();
                        break;
                        
                    case 1: // Take one
                        player.addToInventory(berryType.getName());
                        currentBerries--;
                        if (currentBerries == 0) {
                            lastHarvestTime = System.currentTimeMillis();
                        }
                        
                        board.showDialogue("You picked 1 " + berryType.getName() + "!");
                        updateSprite();
                        break;
                        
                    case 2: // Leave them
                        board.showDialogue("You decided to leave the berries for now.");
                        break;
                }
            });
        } else {
            // No berries available
            long timeLeft = getTimeUntilRegrowth();
            if (timeLeft > 0) {
                int secondsLeft = (int) (timeLeft / 1000);
                int minutesLeft = secondsLeft / 60;
                secondsLeft = secondsLeft % 60;
                
                String timeMessage = "This " + berryType.getName() + " tree has no berries right now.";
                board.showDialogue(timeMessage);
                
                if (minutesLeft > 0) {
                    board.showDialogue("New berries will grow in " + minutesLeft + " minute(s) and " + 
                                     secondsLeft + " second(s).");
                } else {
                    board.showDialogue("New berries will grow in " + secondsLeft + " second(s).");
                }
            } else {
                board.showDialogue("This " + berryType.getName() + " tree has no berries right now. Come back later!");
            }
        }
    }
    
    public void checkBerryRegrowth() {
        if (currentBerries < maxBerries && lastHarvestTime > 0) {
            long timeSinceHarvest = System.currentTimeMillis() - lastHarvestTime;
            if (timeSinceHarvest >= regrowthTimeMs) {
                currentBerries = maxBerries;
                updateSprite();
                // Note: We don't show a dialogue here as this is a background process
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
    
    // Getters remain the same
    public int getCurrentBerries() { return currentBerries; }
    public int getMaxBerries() { return maxBerries; }
    public Berry.BerryType getBerryType() { return berryType; }
    public long getTimeUntilRegrowth() {
        if (currentBerries >= maxBerries || lastHarvestTime == 0) return 0;
        long elapsed = System.currentTimeMillis() - lastHarvestTime;
        return Math.max(0, regrowthTimeMs - elapsed);
    }
}