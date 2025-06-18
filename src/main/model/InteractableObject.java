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
    
    public InteractableObject(Point position, String spriteLocation) {
        this(position, spriteLocation, Direction.ANY);
    }
    
    // Helper method for common interaction patterns
    protected void showTakeOrLeaveOptions(Board board, String itemName, Runnable takeAction, Runnable leaveAction) {
        String[] options = {"Take it", "Leave it"};
        board.showDialogueWithOptions("", "What would you like to do with the " + itemName + "?", 
                                    options, (choice) -> {
            if (choice == 0 && takeAction != null) {
                takeAction.run();
            } else if (choice == 1 && leaveAction != null) {
                leaveAction.run();
            }
        });
    }
    
    // Existing methods remain the same...
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
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
                return true;
            default:
                return false;
        }
    }
    
    public Rectangle getInteractionArea(int tileSize) {
        Rectangle bounds = getBounds(tileSize);
        int interactionPadding = tileSize / 4;
        return new Rectangle(
            bounds.x - interactionPadding, 
            bounds.y - interactionPadding,
            bounds.width + interactionPadding * 2, 
            bounds.height + interactionPadding * 2
        );
    }
    
    public abstract void performAction(Player player, Board board);
    
    public boolean shouldRemoveAfterInteraction() {
        return false;
    }
}