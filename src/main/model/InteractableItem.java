package model;

import java.awt.Point;

import ui.Board;

public class InteractableItem extends InteractableObject {
    private String itemName;
    private int quantity;
    private boolean collected = false;
    private String interactionMessage;
    
    public InteractableItem(Point position, String itemName, int quantity, Direction direction) {
        super(position, generateSpritePath(itemName), direction);
        this.itemName = itemName;
        this.quantity = quantity;
        this.interactionMessage = generateInteractionMessage();
    }
    
    // Overloaded constructor with default ANY direction
    public InteractableItem(Point position, String itemName, int quantity) {
        this(position, itemName, quantity, Direction.ANY);
    }
    
    // Constructor for single item (quantity = 1)
    public InteractableItem(Point position, String itemName) {
        this(position, itemName, 1, Direction.ANY);
    }
    
    private String generateInteractionMessage() {
        if (quantity == 1) {
            return "Found " + itemName + "!";
        } else {
            return "Found " + quantity + " " + itemName + "(s)!";
        }
    }
    
    @Override
    public void performAction(Player player, Board board) {
        if (!collected) {
            // Use ItemFactory to create the actual item
            Item item = ItemFactory.createItem(itemName);
            
            if (item != null) {
                // Add items to player's inventory
                for (int i = 0; i < quantity; i++) {
                    player.addToInventory(itemName);
                }
                
                System.out.println(interactionMessage);
                collected = true;
            } else {
                System.err.println("Failed to create item: " + itemName);
            }
        } else {
            System.out.println("There's nothing here...");
        }
    }
    
    @Override
    public boolean shouldRemoveAfterInteraction() {
        return collected;
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getInteractionMessage() {
        return interactionMessage;
    }
    
    public void setInteractionMessage(String message) {
        this.interactionMessage = message;
    }

    private static String generateSpritePath(String itemName) {
        // Normalize the item name for sprite lookup (same as ItemFactory)
        String normalizedName = itemName.toLowerCase().replace(" ", "-").trim();
        
        // Use the correct path based on your actual folder structure
        return "/sprites/sprites/items/" + normalizedName + ".png";
    }
}
