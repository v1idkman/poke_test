package model;

import java.awt.Point;

import ui.Board;

public class InteractableItem extends InteractableObject {
    private String itemName;
    private int quantity;
    private boolean collected = false;
    private String interactionMessage;
    private Item actualItem; // Store the created item for validation
    
    public InteractableItem(Point position, String itemName, int quantity, Direction direction) {
        super(position, generateSpritePath(itemName), direction);
        this.itemName = itemName;
        this.quantity = quantity;
        this.actualItem = ItemFactory.createItem(itemName); // Create item for validation
        this.interactionMessage = generateInteractionMessage();
        this.walkable = true;
        
        // Use the item's own loadImage() method and copy its image to this object
        if (this.actualItem != null) {
            this.actualItem.loadImage();
            this.sprite = this.actualItem.getImage();
            if (this.sprite != null) {
                // System.out.println("Successfully loaded item image using item's loadImage() method for: " + itemName);
            } else {
                System.err.println("Item's loadImage() method failed to load image for: " + itemName);
            }
        } else {
            System.err.println("Warning: Could not create item '" + itemName + "' during InteractableItem construction");
        }
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
            // Create fresh items for each interaction
            boolean allItemsAdded = true;
            
            for (int i = 0; i < quantity; i++) {
                Item itemToAdd = ItemFactory.createItem(itemName);
                if (itemToAdd != null) {
                    // Add to player's inventory using the string-based method
                    // since the Item class doesn't have inventory integration built-in
                    player.addToInventory(itemName);
                } else {
                    allItemsAdded = false;
                    System.err.println("Failed to create item instance: " + itemName);
                }
            }
            
            if (allItemsAdded) {
                System.out.println(interactionMessage);
                collected = true;
            } else {
                System.err.println("Some items could not be added to inventory");
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

    public Item getActualItem() {
        return actualItem;
    }

    private static String generateSpritePath(String itemName) {
        // This method is kept for compatibility with the parent class constructor
        // but the actual image loading is now delegated to the item's loadImage() method
        String normalizedName = itemName.toLowerCase().replace(" ", "-").trim();
        return "sprites/sprites/items/" + normalizedName + ".png";
    }
}
