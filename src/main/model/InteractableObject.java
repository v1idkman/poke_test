package model;

import java.awt.Point;
import java.awt.Rectangle;

import ui.Board;

public abstract class InteractableObject extends WorldObject {
    protected Direction direction;
    
    public enum Direction {
        FRONT, BACK, LEFT, RIGHT, ANY
    }
    
    public InteractableObject(Point position, String spriteLocation, Direction direction) {
        super(position, spriteLocation);
        this.direction = direction;
    }
    
    // Overloaded constructor for backward compatibility (defaults to ANY)
    public InteractableObject(Point position, String spriteLocation) {
        this(position, spriteLocation, Direction.ANY);
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    /**
     * Checks if the player can interact with this object based on direction
     */
    public boolean canPlayerInteract(Player.Direction playerDirection) {
        switch (this.direction) {
            case FRONT:
                return playerDirection == Player.Direction.FRONT;
            case BACK:
                return playerDirection == Player.Direction.BACK;
            case LEFT:
                return playerDirection == Player.Direction.LEFT;
            case RIGHT:
                return playerDirection == Player.Direction.RIGHT;
            case ANY:
                return true; // Any direction can interact
            default:
                return false;
        }
    }
    
    /**
     * Gets the interaction area around this object
     */
    public Rectangle getInteractionArea(int tileSize) {
        Rectangle bounds = getBounds(tileSize);
        return new Rectangle(
            bounds.x - tileSize, 
            bounds.y - tileSize,
            bounds.width + tileSize * 2, 
            bounds.height + tileSize * 2
        );
    }
    
    /**
     * Abstract method that defines what happens when the player interacts with this object
     */
    public abstract void performAction(Player player, Board board);
    
    /**
     * Optional method to check if this object should be removed after interaction
     */
    public boolean shouldRemoveAfterInteraction() {
        return false;
    }
}
