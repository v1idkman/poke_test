package model;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JPanel;

public abstract class Item {
    // Common properties for all items
    protected String name;
    protected String description;
    protected int quantity;
    protected Image image;
    protected boolean stackable;
    
    // Constructor for basic item properties
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.quantity = 1;
    }

    public abstract void loadImage();
    
    // Abstract method that all items must implement
    public abstract boolean use(Player player);
    
    // Draw the item in the UI
    public void draw(Graphics g, JPanel panel, int tileSize) {
        if (image != null) {
            g.drawImage(image, 0, 0, tileSize, tileSize, panel);
        }
    }
    
    // Common methods for all items
    public boolean addQuantity(int amount) {
        if (!stackable) return false;
        else {
            quantity += amount;
            return true;
        }
    }
    
    public boolean reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        if (stackable) {
            this.quantity = quantity;
        } else {
            this.quantity = 1;
        }
    }
    
    public boolean isStackable() {
        return stackable;
    }
    
    public Image getImage() {
        return image;
    }
    
    // For collision detection if items are placed in the world
    public Rectangle getBounds(int tileSize) {
        return new Rectangle(0, 0, tileSize, tileSize);
    }
    
    @Override
    public String toString() {
        return name;
    }

    // compare only by name, since name duplicates should never happen
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item obj = (Item) o;
        return name.equals(obj.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode() ;
    }

    public void decreaseQuantity() {
        if (quantity > 0) {
            quantity--;
        }
    }
}
