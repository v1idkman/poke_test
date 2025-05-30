package model;

import java.awt.*;
import ui.Board;

public abstract class Npc extends WorldObject {
    protected String name;
    protected String dialogueText;
    protected String spritePath;
    protected Direction facing;
    protected Board board;
    protected boolean isMoving;
    protected boolean canMove;
    protected int animationFrame = 0;
    protected int animationCounter = 0;
    
    public enum Direction {
        FRONT, BACK, LEFT, RIGHT
    }
    
    public enum NpcType {
        TRAINER,      // Battlable NPCs with Pokemon
        CIVILIAN      // Non-battlable NPCs (nurses, shop employees, etc.)
    }
    
    public Npc(Point position, String name, String spritePath, Direction facing, Board board, boolean canMove) {
        super(position, spritePath);
        this.name = name;
        this.spritePath = spritePath;
        this.facing = facing;
        this.board = board;
        this.canMove = canMove;
        this.isMoving = false;
        this.dialogueText = "Hello there!";
    }
    
    // Abstract methods that subclasses must implement
    public abstract NpcType getNpcType();
    public abstract boolean canInitiateBattle();
    public abstract String getInteractionDialogue();
    
    // Common movement methods
    public void updateAnimation() {
        if (isMoving && canMove) {
            animationCounter++;
            if (animationCounter >= 12) { // Animation delay
                animationFrame = (animationFrame + 1) % 2; // 2 frames for walking
                animationCounter = 0;
            }
        } else {
            animationFrame = 0;
            animationCounter = 0;
        }
    }
    
    public void startMoving() {
        if (canMove) {
            isMoving = true;
        }
    }
    
    public void stopMoving() {
        isMoving = false;
        animationFrame = 0;
        animationCounter = 0;
    }
    
    // Common interaction area check
    public boolean isPlayerInInteractionRange(Player player, int tileSize) {
        Rectangle npcBounds = getBounds(tileSize);
        Rectangle playerBounds = player.getBounds(tileSize);
        
        Rectangle interactionArea = new Rectangle(
            npcBounds.x - tileSize,
            npcBounds.y - tileSize,
            npcBounds.width + tileSize * 2,
            npcBounds.height + tileSize * 2
        );
        
        return playerBounds.intersects(interactionArea);
    }
    
    // Getters and setters
    public String getName() { return name; }
    public String getDialogueText() { return dialogueText; }
    public void setDialogueText(String text) { this.dialogueText = text; }
    public Direction getFacing() { return facing; }
    public void setFacing(Direction facing) { this.facing = facing; }
    public Direction getDirection() { return facing; }
    public boolean isMoving() { return isMoving; }
    public boolean canMove() { return canMove; }
    public int getAnimationFrame() { return animationFrame; }
    public void setBoard(Board board) { this.board = board; }
}
