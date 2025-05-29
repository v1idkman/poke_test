package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import pokes.Pokemon;
import ui.Board;

public class Npc extends WorldObject {
    private String name;
    private String dialogueText;
    private List<Pokemon> team;
    private boolean hasBeenDefeated;
    private String spritePath;
    private Direction facing;
    private int visionRange;
    private Board board;
    private boolean defeated;
    
    public enum Direction {
        FRONT, BACK, LEFT, RIGHT
    }
    
    public Npc(Point position, String name, String spritePath, Direction facing, Board board) {
        super(position, spritePath);
        this.name = name;
        this.spritePath = spritePath;
        this.facing = facing;
        this.team = new ArrayList<>();
        this.hasBeenDefeated = false;
        this.visionRange = 5; // tiles
        this.dialogueText = "Let's battle!";
        defeated = false;
    }
    
    public void addPokemon(Pokemon pokemon) {
        if (team.size() < 6) {
            team.add(pokemon);
        }
    }
    
    public boolean canSeePlayer(Player player, int tileSize) {
        if (hasBeenDefeated || defeated) return false;
        
        int npcTileX = position.x;
        int npcTileY = position.y;
        int playerTileX = player.getWorldX() / tileSize;
        int playerTileY = player.getWorldY() / tileSize;
        
        // Calculate direction vector from NPC to player
        int deltaX = playerTileX - npcTileX;
        int deltaY = playerTileY - npcTileY;
        
        // Check if player is within maximum vision range (5 tiles)
        int distance = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        if (distance > 5) {
            return false;
        }
        
        // Check if player is in the direction NPC is facing
        boolean inFacingDirection = false;
        switch (facing) {
            case FRONT: // NPC looking down (positive Y direction)
                inFacingDirection = deltaY > 0 && Math.abs(deltaX) <= 1;
                break;
            case BACK: // NPC looking up (negative Y direction)
                inFacingDirection = deltaY < 0 && Math.abs(deltaX) <= 1;
                break;
            case LEFT: // NPC looking left (negative X direction)
                inFacingDirection = deltaX < 0 && Math.abs(deltaY) <= 1;
                break;
            case RIGHT: // NPC looking right (positive X direction)
                inFacingDirection = deltaX > 0 && Math.abs(deltaY) <= 1;
                break;
        }
        
        if (!inFacingDirection) {
            return false;
        }
        
        // Perform line-of-sight check with object blocking
        return hasLineOfSight(npcTileX, npcTileY, playerTileX, playerTileY, tileSize);
    }
    
    private boolean hasLineOfSight(int startX, int startY, int endX, int endY, int tileSize) {
        // Use Bresenham's line algorithm to check each tile along the line of sight
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int x = startX;
        int y = startY;
        int n = 1 + dx + dy;
        int x_inc = (endX > startX) ? 1 : -1;
        int y_inc = (endY > startY) ? 1 : -1;
        int error = dx - dy;
        
        dx *= 2;
        dy *= 2;
        
        for (; n > 0; --n) {
            // Skip the starting position (NPC's position)
            if (!(x == startX && y == startY)) {
                // Check if there's an object blocking the line of sight at this position
                if (isPositionBlocked(x, y, tileSize)) {
                    return false; // Line of sight is blocked
                }
            }
            
            if (error > 0) {
                x += x_inc;
                error -= dy;
            } else {
                y += y_inc;
                error += dx;
            }
        }
        
        return true; // Line of sight is clear
    }
    
    private boolean isPositionBlocked(int tileX, int tileY, int tileSize) {
        // Convert tile coordinates to pixel coordinates for object checking
        int pixelX = tileX * tileSize;
        int pixelY = tileY * tileSize;
        Rectangle tileRect = new Rectangle(pixelX, pixelY, tileSize, tileSize);
        
        // Check against all objects in the world (you'll need access to the Board's objects list)
        // This requires adding a reference to the Board or objects list in the NPC
        if (board != null) {
            for (WorldObject obj : board.getObjects()) {
                // Skip doors and the NPC itself
                if (obj instanceof Door || obj == this) {
                    continue;
                }
                
                Rectangle objBounds = obj.getBounds(tileSize);
                if (tileRect.intersects(objBounds)) {
                    return true; // Object blocks line of sight
                }
            }
        }
        
        return false; // No blocking object found
    }
    
    // Add these methods to your Npc class
    public int getSightRange() { return visionRange; }

    public boolean isDefeated() { return defeated; }

    
    // Getters and setters
    public String getName() { return name; }
    public List<Pokemon> getTeam() { return team; }
    public boolean hasBeenDefeated() { return hasBeenDefeated; }
    public void setDefeated(boolean defeated) { this.hasBeenDefeated = defeated; }
    public String getDialogueText() { return dialogueText; }
    public void setDialogueText(String text) { this.dialogueText = text; }
    public Direction getFacing() { return facing; }
    public void setFacing(Direction facing) { this.facing = facing; }
    public int getVisionRange() { return visionRange; }
    public void setVisionRange(int range) { this.visionRange = range; }
    public Direction getDirection() { return facing; }
}
