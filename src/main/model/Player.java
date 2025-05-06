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

    public Player(String name) {
        super(name);
        facingFront = "/resources/player_sprites/s_facing_front";
        setSprite(facingFront);
        pos = new Point(0, 0);
        width = 50;
        height = 50;
        inventory = new HashSet<>();
        money = 0;
        trainerId = (int)(Math.random() * 100000);
    }

    public void setAnimationFrame(int frame) {
        animationFrame = frame;
    }

    public Point getPosition() {
        return pos;
    }

    public void move(int dx, int dy) {
        pos.translate(dx, dy);
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

    public void removeItem(Item item) {
        if (inventory.contains(item)) {
            inventory.remove(item);
        }
    }
}
