package model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

public class Player extends Trainer {
    protected Point pos;
    protected String facingFront;

    public enum Direction { FRONT, BACK, LEFT, RIGHT }

    public enum MovementState {
        FREE,           // Can move normally
        FROZEN,         // Cannot move (spotted by trainer)
        IN_BATTLE       // In battle (existing state)
    }

    private MovementState movementState = MovementState.FREE;

    private Direction direction = Direction.BACK;
    private boolean moving, running = false;
    private int animationFrame = 0;
    
    // Animation constants
    private static final int NUM_FRAMES = 2; 
    private static final int ANIMATION_DELAY = 12; // Increased from 3 to 12
    private static final float RUN_SPEED = 2.5f;
    private static final int TILE_SIZE = 32;
    
    // Animation state
    private int animationCounter = 0;
    private boolean sprintKeyPressed = false;
    
    // Movement state
    private float exactX, exactY;
    private float moveSpeed = 3.0f;
    private int targetX, targetY;
    private boolean hasTarget = false;

    private Set<Item> inventory;
    private int money;
    private int trainerId;

    private boolean inBattle;

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
    }

    public void setAnimationFrame(int frame) {
        animationFrame = frame;
    }

    public Point getPosition() {
        return pos;
    }
    
    public int getWorldX() {
        return Math.round(exactX * TILE_SIZE);
    }
    
    public int getWorldY() {
        return Math.round(exactY * TILE_SIZE);
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

    // use slightly smaller bounds so that the when the player is next to a building it looks better
    public Rectangle getBounds(int tileSize) {
        return new Rectangle(
            getWorldX(),
            getWorldY() + (height / 2),
            width,
            height - (height / 2)
        );
    }

    public Rectangle getFullBounds(int tileSize) {
        return new Rectangle(
            getWorldX(),
            getWorldY(),
            width,
            height
        );
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

    public void setMoney(int money) {
        this.money = money;
    }

    public boolean removeMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        } else {
            return false;
        }
    }

    public void addMoney(int amount) {
        this.money += amount;
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

    public MovementState getMovementState() {
        return movementState;
    }

    public void setMovementState(MovementState state) {
        this.movementState = state;
        if (state == MovementState.FROZEN) {
            setMoving(false);
            setSprintKeyPressed(false);
        } else if (state == MovementState.FREE) {
            setMoving(true);
        } else if (state == MovementState.IN_BATTLE) {
            this.inBattle = true;
            setMoving(false);
            setSprintKeyPressed(false);
        }
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
    
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }

    public void setSprintKeyPressed(boolean pressed) {
        this.sprintKeyPressed = pressed;
        updateRunningState();
    }

    private void updateRunningState() {
        running = moving && sprintKeyPressed;
        if (running) {
            moveSpeed = RUN_SPEED;
        } else {
            moveSpeed = 3.0f;
        }
    }

    public void move(int dx, int dy) {
        // Update exact coordinates (in tile units)
        exactX += dx / (float)TILE_SIZE;
        exactY += dy / (float)TILE_SIZE;
        
        // Update tile position
        pos.x = Math.round(exactX);
        pos.y = Math.round(exactY);
    }
    

    public void move(Direction dir) {
        direction = dir;
        moving = true;
        updateRunningState();
    }

    public boolean getMoving() {
        return moving;
    }

    public void stopMoving() {
        moving = false;
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }
}
