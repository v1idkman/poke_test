package model;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import pokes.Pokemon;
import ui.Board;
import ui.NpcView;

public class TrainerNpc extends Npc {
    private List<Pokemon> team;
    private boolean hasBeenDefeated;
    private boolean defeated;
    private int visionRange;
    private String trainerClass;
    private boolean canMove;
    private NpcView npcView;
    
    // Movement animation variables
    private boolean isMovingTowardsPlayer = false;
    private boolean isApproachingForBattle = false;
    private Point targetPosition;
    private Point originalPosition;
    private int movementSpeed = 2;
    private double exactX, exactY;
    private Player targetPlayer; // Store reference to player being approached
    
    public TrainerNpc(Point position, String name, String spritePath, Direction facing, 
                     Board board, String trainerClass, boolean canMove) {
        super(position, name, spritePath, facing, board, canMove);
        this.team = new ArrayList<>();
        this.hasBeenDefeated = false;
        this.defeated = false;
        this.visionRange = 5;
        this.trainerClass = trainerClass;
        this.canMove = canMove;
        this.dialogueText = "Let's battle!";
        this.originalPosition = new Point(position);
        this.exactX = position.x;
        this.exactY = position.y;
        
        if (canMove) {
            String npcType = extractNpcTypeFromPath(spritePath);
            this.npcView = new NpcView(this, npcType);
        }
    }
    
    private String extractNpcTypeFromPath(String spritePath) {
        String[] parts = spritePath.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "default";
    }
    
    public void startApproachingPlayer(Player player, int tileSize) {
        if (!canMove || hasBeenDefeated || defeated || isApproachingForBattle) return;
        
        this.targetPlayer = player;
        this.isApproachingForBattle = true;
        
        // Calculate the tile to stop at (adjacent to player)
        int playerTileX = player.getWorldX() / tileSize;
        int playerTileY = player.getWorldY() / tileSize;
        
        // Find the best adjacent position to approach
        Point approachPosition = findBestApproachPosition(playerTileX, playerTileY);
        
        if (approachPosition != null) {
            moveTowardsPosition(approachPosition);
        }
    }
    
    private Point findBestApproachPosition(int playerTileX, int playerTileY) {
        // Try positions adjacent to the player, prioritizing the direction we're facing
        Point[] adjacentPositions = {
            new Point(playerTileX, playerTileY - 1), // Above player
            new Point(playerTileX, playerTileY + 1), // Below player
            new Point(playerTileX - 1, playerTileY), // Left of player
            new Point(playerTileX + 1, playerTileY)  // Right of player
        };
        
        // Find the closest valid position
        Point bestPosition = null;
        double shortestDistance = Double.MAX_VALUE;
        
        for (Point pos : adjacentPositions) {
            if (isValidPosition(pos)) {
                double distance = Math.sqrt(Math.pow(pos.x - position.x, 2) + Math.pow(pos.y - position.y, 2));
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestPosition = pos;
                }
            }
        }
        
        return bestPosition;
    }
    
    private boolean isValidPosition(Point pos) {
        // Check if position is within board bounds and not blocked
        if (pos.x < 0 || pos.y < 0 || pos.x >= board.getColumns() || pos.y >= board.getRows()) {
            return false;
        }
        
        // Check for object collisions
        Rectangle posRect = new Rectangle(pos.x * Board.TILE_SIZE, pos.y * Board.TILE_SIZE, 
                                        Board.TILE_SIZE, Board.TILE_SIZE);
        
        for (WorldObject obj : board.getObjects()) {
            if (obj != this && !(obj instanceof Door)) {
                if (posRect.intersects(obj.getBounds(Board.TILE_SIZE))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void moveTowardsPosition(Point target) {
        this.targetPosition = target;
        this.isMovingTowardsPlayer = true;
        
        // Calculate direction to face
        int deltaX = target.x - position.x;
        int deltaY = target.y - position.y;
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            setFacing(deltaX > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            setFacing(deltaY > 0 ? Direction.FRONT : Direction.BACK);
        }
        
        startMoving();
    }
    
    public void updateMovement(int tileSize) {
        if (!isMovingTowardsPlayer || targetPosition == null) return;
        
        double targetPixelX = targetPosition.x * tileSize;
        double targetPixelY = targetPosition.y * tileSize;
        double currentPixelX = exactX * tileSize;
        double currentPixelY = exactY * tileSize;
        
        double distanceX = targetPixelX - currentPixelX;
        double distanceY = targetPixelY - currentPixelY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        if (distance <= movementSpeed) {
            // Reached target position
            position.setLocation(targetPosition);
            exactX = targetPosition.x;
            exactY = targetPosition.y;
            isMovingTowardsPlayer = false;
            stopMoving();
            
            // If this was an approach for battle, face the player and trigger dialogue
            if (isApproachingForBattle && targetPlayer != null) {
                facePlayer(targetPlayer, tileSize);
                // Notify the board that approach is complete
                board.onTrainerApproachComplete(this);
            }
        } else {
            // Continue moving
            double moveX = (distanceX / distance) * movementSpeed;
            double moveY = (distanceY / distance) * movementSpeed;
            
            exactX += moveX / tileSize;
            exactY += moveY / tileSize;
            
            updateAnimation();
        }
        
        if (npcView != null) {
            npcView.update();
        }
    }
    
    private void facePlayer(Player player, int tileSize) {
        int playerTileX = player.getWorldX() / tileSize;
        int playerTileY = player.getWorldY() / tileSize;
        
        int deltaX = playerTileX - position.x;
        int deltaY = playerTileY - position.y;
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            setFacing(deltaX > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            setFacing(deltaY > 0 ? Direction.FRONT : Direction.BACK);
        }
    }
    
    @Override
    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        if (npcView != null && canMove) {
            // Use exact position for smooth movement during approach
            if (isMovingTowardsPlayer) {
                if (npcView.getCurrentImage() != null) {
                    int x = (int)(exactX * tileSize);
                    int y = (int)(exactY * tileSize);
                    g.drawImage(npcView.getCurrentImage(), x, y, observer);
                }
            } else {
                npcView.draw(g, observer, tileSize);
            }
        } else {
            super.draw(g, observer, tileSize);
        }
    }
    
    public boolean isApproachingForBattle() {
        return isApproachingForBattle;
    }
    
    public void completeApproach() {
        isApproachingForBattle = false;
        targetPlayer = null;
    }
    
    public void resetToOriginalPosition() {
        position.setLocation(originalPosition);
        exactX = originalPosition.x;
        exactY = originalPosition.y;
        isMovingTowardsPlayer = false;
        stopMoving();
    }
    
    @Override
    public NpcType getNpcType() {
        return NpcType.TRAINER;
    }
    
    @Override
    public boolean canInitiateBattle() {
        return !hasBeenDefeated && !defeated && !team.isEmpty();
    }
    
    @Override
    public String getInteractionDialogue() {
        if (hasBeenDefeated || defeated) {
            return "Good battle! You're getting stronger!";
        }
        return dialogueText;
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
        if (distance > visionRange) {
            return false;
        }
        
        // Check if player is in the direction NPC is facing
        boolean inFacingDirection = false;
        switch (facing) {
            case BACK: // NPC looking down (positive Y direction)
                inFacingDirection = deltaY > 0 && Math.abs(deltaX) <= 1;
                break;
            case FRONT: // NPC looking up (negative Y direction)
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
    
    public boolean hasLineOfSight(int startX, int startY, int endX, int endY, int tileSize) {
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
    
    // Trainer-specific getters and setters
    public List<Pokemon> getTeam() { return team; }
    public boolean hasBeenDefeated() { return hasBeenDefeated; }
    public void setDefeated(boolean defeated) { 
        this.hasBeenDefeated = defeated;
        this.defeated = defeated;
    }
    public boolean isDefeated() { return defeated; }
    public int getSightRange() { return visionRange; }
    public void setVisionRange(int range) { this.visionRange = range; }
    public String getTrainerClass() { return trainerClass; }

    // TODO: the npc moving mecahnics are not working, I'm very tired
}
