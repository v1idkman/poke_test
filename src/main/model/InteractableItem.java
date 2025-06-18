package model;

import java.awt.Point;
import ui.Board;

public class InteractableItem extends InteractableObject {
    private String itemName;
    private int quantity;
    private boolean collected = false;
    private String interactionMessage;
    private Item actualItem;
    
    public InteractableItem(Point position, String itemName, int quantity, Direction direction) {
        super(position, generateSpritePath(itemName), direction);
        this.itemName = itemName;
        this.quantity = quantity;
        this.actualItem = ItemFactory.createItem(itemName);
        this.interactionMessage = generateInteractionMessage();
        this.walkable = true;
        
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
    
    // Overloaded constructors remain the same
    public InteractableItem(Point position, String itemName, int quantity) {
        this(position, itemName, quantity, Direction.ANY);
    }
    
    public InteractableItem(Point position, String itemName) {
        this(position, itemName, 1, Direction.ANY);
    }
    
    private String generateInteractionMessage() {
        if (quantity == 1) {
            return "Found " + itemName + "!";
        } else {
            return "Found " + quantity + " " + itemName + "s!";
        }
    }
    
    @Override
    public void performAction(Player player, Board board) {
        if (!collected) {
            // Show discovery message first
            board.showDialogue(interactionMessage);
            
            // Automatically pick up all items
            boolean allItemsAdded = true;
            for (int i = 0; i < quantity; i++) {
                Item itemToAdd = ItemFactory.createItem(itemName);
                if (itemToAdd != null) {
                    player.addToInventory(itemName);
                } else {
                    allItemsAdded = false;
                    System.err.println("Failed to create item instance: " + itemName);
                }
            }
            
            if (allItemsAdded) {
                // Show success message with item description if available
                String successMessage;
                if (quantity == 1) {
                    successMessage = "You picked up the " + itemName + "!";
                } else {
                    successMessage = "You picked up all " + quantity + " " + itemName + "s!";
                }
                board.showDialogue(successMessage);
                
                collected = true;
            } else {
                // Show error message
                board.showDialogue("Something went wrong... couldn't pick up all the items.");
            }
        } else {
            // Already collected
            board.showDialogue("There's nothing here...");
        }
    }

    @Override
    public boolean shouldRemoveAfterInteraction() {
        return collected;
    }
    
    // Getters remain the same
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
        String normalizedName = itemName.toLowerCase().replace(" ", "-").trim();
        return "sprites/sprites/items/" + normalizedName + ".png";
    }
}