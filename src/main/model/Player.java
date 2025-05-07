package model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

public class Player extends Trainer {
    protected Point pos;
    protected String facingFront;

    public enum Direction {FRONT, BACK, LEFT, RIGHT}

    private Direction direction = Direction.BACK;
    private boolean moving = false;
    private int animationFrame = 0;
    private static final int NUM_FRAMES = 2;

    private Set<Item> inventory;
    private int money;
    private int trainerId;

    private boolean inBattle;
    
    // World position in pixels (for camera tracking)
    @SuppressWarnings("unused")
    private int worldX;
    @SuppressWarnings("unused")
    private int worldY;
    private int tileSize;

    public Player(String name) {
        super(name);
        facingFront = "/resources/player_sprites/s_facing_front";
        setSprite(facingFront);
        pos = new Point(60, 60);
        width = 50;
        height = 50;
        inventory = new HashSet<>();
        money = 0;
        trainerId = (int)(Math.random() * 100000);
        inBattle = false;
        tileSize = 5; // Default tile size
    }

    public void setAnimationFrame(int frame) {
        animationFrame = frame;
    }

    public Point getPosition() {
        return pos;
    }
    
    // Get world position in pixels for camera
    public int getWorldX() {
        return pos.x * tileSize;
    }
    
    public int getWorldY() {
        return pos.y * tileSize;
    }
    
    // Set tile size for coordinate conversion
    public void setTileSize(int size) {
        this.tileSize = size;
    }

    public void move(int dx, int dy) {
        pos.translate(dx, dy);
        // Update world coordinates
        worldX = pos.x * tileSize;
        worldY = pos.y * tileSize;
    }

    public void setSpriteSize(int width, int height) {
        this.width = width;
        this.height = height;   
    }

    public Rectangle getBounds(int tileSize) {
        // The position where the sprite is drawn
        int x = pos.x * tileSize;
        int y = pos.y * tileSize;
        return new Rectangle(x, y, width, height);
    }   

    public void tick(int maxCols, int maxRows) {
        if (pos.x < 0) pos.x = 0;
        else if (pos.x >= maxCols) pos.x = maxCols - 1;
        if (pos.y < 0) pos.y = 0;
        else if (pos.y >= maxRows) pos.y = maxRows - 1;
        
        // Update world coordinates
        worldX = pos.x * tileSize;
        worldY = pos.y * tileSize;
    }

    public void setDirection(Direction dir) {
        direction = dir;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setMoving(boolean moving) {
        this.moving = moving;
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    public void nextAnimationFrame() {
        animationFrame = (animationFrame + 1) % NUM_FRAMES;
    }
    
    public int getAnimationFrame() {
        return animationFrame;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Set<Item> getInventory() {
        return inventory;
    }

    public int getMoney() {
        return money;
    }

    public int getId() {
        return trainerId;
    }

    public boolean isInBattle() {
        return inBattle;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public void addToInventory(String itemName) {
        Item newItem = ItemFactory.createItem(itemName);
        if (newItem == null) return;
        
        if (newItem.isStackable()) {
            for (Item existingItem : inventory) {
                if (existingItem.getName().equals(newItem.getName())) {
                    existingItem.setQuantity(existingItem.getQuantity() + 1);
                    return;
                }
            }
        }
        inventory.add(newItem);
    }
    
    public void addToInventory(Item item) {
        // For stackable items, check if we already have this item
        if (item.isStackable()) {
            for (Item existingItem : inventory) {
                if (existingItem.getName().equals(item.getName())) {
                    existingItem.addQuantity(item.getQuantity());
                    return;
                }
            }
        }
        // If not stackable or not found, add as new item
        inventory.add(item);
    }

    public void removeItem(Item item) {
        if (inventory.contains(item)) {
            inventory.remove(item);
        }
    }

    public void setPosition(Point point) {
        this.pos = point;
        // Update world coordinates
        worldX = pos.x * tileSize;
        worldY = pos.y * tileSize;
    }
}
