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
    
    // Animation constants
    private static final int NUM_FRAMES = 2; 
    private static final int ANIMATION_DELAY = 5;
    
    // Animation state
    private int animationCounter = 0;
    
    // Movement state
    private float exactX, exactY;
    private float moveSpeed = 2.0f;
    private int targetX, targetY;
    private boolean hasTarget = false;

    private Set<Item> inventory;
    private int money;
    private int trainerId;

    private boolean inBattle;
    
    private int tileSize;

    public Player(String name) {
        super(name);
        facingFront = "/resources/player_sprites/s_facing_front";
        setSprite(facingFront);
        pos = new Point(60, 60);
        exactX = pos.x;
        exactY = pos.y;
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
        return (int)(exactX * tileSize);
    }
    
    public int getWorldY() {
        return (int)(exactY * tileSize);
    }
    
    // Set tile size for coordinate conversion
    public void setTileSize(int size) {
        this.tileSize = size;
    }

    public void updateAnimation() {
        if (isMoving()) {
            animationCounter++;
            if (animationCounter >= ANIMATION_DELAY) {
                nextAnimationFrame();
                animationCounter = 0;
            }
        } else {
            animationFrame = 0;
            animationCounter = 0;
        }
    }
    
    // Update movement logic
    public void move(int dx, int dy) {
        if ((dx != 0 && dy != 0)) return;
        targetX = pos.x + dx;
        targetY = pos.y + dy;
        hasTarget = true;
    }
    
    public void updatePosition() {
        if (!hasTarget) return;
        
        boolean reachedX = false;
        boolean reachedY = false;
        
        // Move towards target X with increased speed
        if (exactX < targetX) {
            exactX = Math.min(exactX + moveSpeed, targetX);
            reachedX = exactX == targetX;
        } else if (exactX > targetX) {
            exactX = Math.max(exactX - moveSpeed, targetX);
            reachedX = exactX == targetX;
        } else {
            reachedX = true;
        }
        
        // Move towards target Y with increased speed
        if (exactY < targetY) {
            exactY = Math.min(exactY + moveSpeed, targetY);
            reachedY = exactY == targetY;
        } else if (exactY > targetY) {
            exactY = Math.max(exactY - moveSpeed, targetY);
            reachedY = exactY == targetY;
        } else {
            reachedY = true;
        }
        
        // Update integer position for collision detection
        pos.x = Math.round(exactX);
        pos.y = Math.round(exactY);
        
        // If reached target, clear target
        if (reachedX && reachedY) {
            hasTarget = false;
            if (!moving) {
                animationCounter = 0; // Reset animation when stopped
            }
        }
    }
    
    public void tick(int maxCols, int maxRows) {
        updatePosition();
        updateAnimation();
        
        // Boundary checks
        if (exactX < 0) exactX = 0;
        else if (exactX >= maxCols) exactX = maxCols - 1;
        if (exactY < 0) exactY = 0;
        else if (exactY >= maxRows) exactY = maxRows - 1;
        
        pos.x = Math.round(exactX);
        pos.y = Math.round(exactY);
    }

        public void setSpriteSize(int width, int height) {
        this.width = width;
        this.height = height;   
    }

    public Rectangle getBounds(int tileSize) {
        int x = pos.x * tileSize;
        int y = pos.y * tileSize;
        return new Rectangle(x, y, width, height);
    }

    public void updateExactCoordinates() {
        exactX = pos.x;
        exactY = pos.y;
    }
    

    public void setDirection(Direction dir) {
        direction = dir;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setMoving(boolean moving) {
        this.moving = moving;
        if (!moving) {
            animationFrame = 0;
            animationCounter = 0;
        }
    }
    
    public boolean isMoving() {
        return moving || hasTarget;
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
        this.exactX = point.x;
        this.exactY = point.y;
    }
    
    public boolean isAtTarget() {
        return !hasTarget;
    }
    
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
}
