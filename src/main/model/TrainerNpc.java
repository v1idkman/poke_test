package model;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

import model.Player.MovementState;
import pokes.Pokemon;
import ui.Board;
import ui.Icon;
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
    private Point originalPosition;
    private int movementSpeed = 2;
    private double exactX, exactY;
    private Player targetPlayer;

    private Icon exclamationIcon;
    private boolean iconDisplayed = false;
    private boolean waitingForIconComplete = false;
    
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
        
        // Initialize exact coordinates (like Player class)
        this.exactX = position.x;
        this.exactY = position.y;
        this.movementSpeed = 2; // Set movement speed in pixels per frame

        this.exclamationIcon = new Icon("exclamation");
        
        if (canMove) {
            this.npcView = new NpcView(this, extractNpcTypeFromPath(spritePath));
        }
    }

    private String extractNpcTypeFromPath(String spritePath) {
        String[] parts = spritePath.split("/");
        // The path structure is: /resources/npc_sprites/bug_catcher/facing_back.png
        // parts[0] = "" (empty because of leading slash)
        // parts[1] = "resources"
        // parts[2] = "npc_sprites" 
        // parts[3] = "bug_catcher" <- This is what we want
        // parts[4] = "facing_back.png"
        
        if (parts.length >= 4) {
            return parts[3]; // Return "bug_catcher" instead of "npc_sprites"
        }
        return "default";
    }
    
    
    public void startApproachingPlayer(Player player, int tileSize) {
        if (!canMove || hasBeenDefeated || defeated || isApproachingForBattle) {
            return;
        }
        
        // Only start approaching if icon has been displayed and completed
        if (!iconDisplayed || !exclamationIcon.isComplete()) {
            this.targetPlayer = player; // Store for later
            return;
        }
        
        this.targetPlayer = player;
        this.isApproachingForBattle = true;
        this.isMovingTowardsPlayer = true;
        
        System.out.println(getName() + " is now approaching the player!");
        
        // Calculate initial direction to face player
        double playerPixelX = player.getWorldX();
        double playerPixelY = player.getWorldY();
        double npcPixelX = exactX * tileSize;
        double npcPixelY = exactY * tileSize;
        
        double distanceX = playerPixelX - npcPixelX;
        double distanceY = playerPixelY - npcPixelY;
        
        // Face the direction with the largest distance first
        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            setFacing(distanceX > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            setFacing(distanceY > 0 ? Direction.BACK : Direction.FRONT);
        }
        
        // Start moving immediately
        startMoving();
    }
    
    public void updateMovement(int tileSize) {
        if (!isMovingTowardsPlayer || targetPlayer == null) return;
        
        // Get current positions in pixels
        double currentPixelX = exactX * tileSize;
        double currentPixelY = exactY * tileSize;
        double playerPixelX = targetPlayer.getWorldX();
        double playerPixelY = targetPlayer.getWorldY();
        
        // Calculate direction to player
        double distanceX = playerPixelX - currentPixelX;
        double distanceY = playerPixelY - currentPixelY;
        double totalDistance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        // Debug output
        System.out.println("NPC at: " + currentPixelX + ", " + currentPixelY + 
                          " | Player at: " + playerPixelX + ", " + playerPixelY + 
                          " | Distance: " + totalDistance + " | Facing: " + facing);
        
        // Check for collision with player
        Rectangle npcBounds = new Rectangle(
            (int)currentPixelX, 
            (int)currentPixelY, 
            tileSize, 
            tileSize
        );
        Rectangle playerBounds = targetPlayer.getBounds(tileSize);
        
        if (npcBounds.intersects(playerBounds) || totalDistance <= tileSize) {
            // Collision detected - stop moving and initiate battle
            System.out.println("COLLISION DETECTED! Starting battle...");
            isMovingTowardsPlayer = false;
            stopMoving();
            facePlayer(targetPlayer, tileSize);
            
            // Force the NPC view to update to standing sprite immediately
            if (npcView != null) {
                npcView.update();
            }
            
            if (isApproachingForBattle) {
                board.onTrainerApproachComplete(this);
            }
            
            isApproachingForBattle = false;
            return;
        }        
        
        double moveX = 0;
        double moveY = 0;
        
        switch (facing) {
            case LEFT:
            case RIGHT:
                // Only move in X axis when facing left or right
                if (Math.abs(distanceX) > 2) { // Only move if distance is significant
                    moveX = (distanceX > 0) ? movementSpeed : -movementSpeed;
                    moveY = 0; // No Y movement
                }
                break;
                
            case FRONT:
            case BACK:
                // Only move in Y axis when facing front or back
                if (Math.abs(distanceY) > 2) { // Only move if distance is significant
                    moveX = 0; // No X movement
                    moveY = (distanceY > 0) ? movementSpeed : -movementSpeed;
                }
                break;
        }
        
        // Apply movement if there's any
        if (moveX != 0 || moveY != 0) {
            // Update exact position in tile units
            exactX += moveX / tileSize;
            exactY += moveY / tileSize;
            
            // Update tile position for collision detection
            position.setLocation((int)Math.round(exactX), (int)Math.round(exactY));
            
            updateAnimation();
        } else {
            // If we can't move in our facing direction, turn to face the player
            turnTowardsPlayer(distanceX, distanceY);
        }
        
        if (npcView != null) {
            npcView.update();
        }
    }

    private void turnTowardsPlayer(double distanceX, double distanceY) {
        // Determine which axis has the larger distance and turn to face that direction
        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            // Player is more to the left or right
            Direction newDirection = (distanceX > 0) ? Direction.RIGHT : Direction.LEFT;
            if (facing != newDirection) {
                setFacing(newDirection);
                System.out.println("Turning to face: " + newDirection);
            }
        } else {
            // Player is more up or down
            Direction newDirection = (distanceY > 0) ? Direction.BACK : Direction.FRONT;
            if (facing != newDirection) {
                setFacing(newDirection);
                System.out.println("Turning to face: " + newDirection);
            }
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
            // CORRECTED: FRONT = down (positive Y), BACK = up (negative Y)
            setFacing(deltaY > 0 ? Direction.BACK : Direction.FRONT);
        }
    }
    
        @Override
    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        if (npcView != null && canMove) {
            // Use exact pixel position for smooth movement
            if (npcView.getCurrentImage() != null) {
                int x = getWorldX();
                int y = getWorldY();
                g.drawImage(npcView.getCurrentImage(), x, y, observer);
            } else {
                // Fallback if image is missing
                g.setColor(Color.BLUE);
                g.fillRect(getWorldX(), getWorldY(), tileSize, tileSize);
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
        
        // Calculate player center
        int playerCenterX = player.getWorldX() + (player.width / 2);
        int playerCenterY = player.getWorldY() + (player.height / 2);
        
        // Calculate battle initiation rectangle
        Rectangle battleArea = getBattleInitiationRectangle(tileSize);
        
        // Check if player center has crossed the midpoint of the battle rectangle
        boolean crossedMidpoint = hasPlayerCrossedMidpoint(playerCenterX, playerCenterY, battleArea);
        
        if (!crossedMidpoint) {
            return false;
        }
        
        // Perform line-of-sight check if midpoint crossing detected
        int playerTileX = playerCenterX / tileSize;
        int playerTileY = playerCenterY / tileSize;
        int npcTileX = position.x;
        int npcTileY = position.y;
        
        boolean hasLineOfSight = hasLineOfSight(npcTileX, npcTileY, playerTileX, playerTileY, tileSize);
        
        // If we can see the player and haven't shown the icon yet
        if (hasLineOfSight && !iconDisplayed && !isApproachingForBattle) {
            this.targetPlayer = player;
            
            // IMMEDIATELY STOP PLAYER MOVEMENT when spotted
            player.setMoving(false);
            player.stopMoving();
            
            // Show the exclamation icon
            player.setMovementState(MovementState.FROZEN);
            showExclamationIcon();
            
            System.out.println("Player spotted by " + getName() + "! Movement disabled.");
        }
        
        return hasLineOfSight;
    }
    private boolean hasPlayerCrossedMidpoint(int playerCenterX, int playerCenterY, Rectangle battleArea) {
        switch (facing) {
            case FRONT: // Looking up - vertical rectangle, divide by Y (midpoint line is vertical)
                int frontMidpointX = battleArea.x + (battleArea.width / 2);
                return battleArea.contains(playerCenterX, playerCenterY) && 
                       playerCenterX >= frontMidpointX;
                
            case BACK: // Looking down - vertical rectangle, divide by Y (midpoint line is vertical)
                int backMidpointX = battleArea.x + (battleArea.width / 2);
                return battleArea.contains(playerCenterX, playerCenterY) && 
                       playerCenterX >= backMidpointX;
                
            case LEFT: // Looking left - horizontal rectangle, divide by X (midpoint line is horizontal)
                int leftMidpointY = battleArea.y + (battleArea.height / 2);
                return battleArea.contains(playerCenterX, playerCenterY) && 
                       playerCenterY >= leftMidpointY;
                
            case RIGHT: // Looking right - horizontal rectangle, divide by X (midpoint line is horizontal)
                int rightMidpointY = battleArea.y + (battleArea.height / 2);
                return battleArea.contains(playerCenterX, playerCenterY) && 
                       playerCenterY >= rightMidpointY;
                
            default:
                // Fallback - just check if player is in rectangle
                return battleArea.contains(playerCenterX, playerCenterY);
        }
    }
    
    private Rectangle getBattleInitiationRectangle(int tileSize) {
        int npcPixelX = position.x * tileSize;
        int npcPixelY = position.y * tileSize;
        
        // Use player dimensions for consistent rectangle size
        int playerWidth = tileSize;
        int playerHeight = tileSize;
        
        Rectangle battleArea;
        
        switch (facing) {
            case FRONT: // Looking up - vertical rectangle
                int frontVisionHeight = 5 * playerWidth;
                battleArea = new Rectangle(
                    npcPixelX - (playerWidth / 2), // Center horizontally on NPC
                    npcPixelY - frontVisionHeight, // Extend upward
                    playerWidth,
                    frontVisionHeight
                );
                break;
                
            case BACK: // Looking down - vertical rectangle
                int backVisionHeight = 5 * playerWidth;
                battleArea = new Rectangle(
                    npcPixelX - (playerWidth / 2),
                    npcPixelY + tileSize, // Start after NPC sprite
                    playerWidth,
                    backVisionHeight
                );
                break;
                
            case LEFT: // Looking left - horizontal rectangle
                int leftVisionWidth = 5 * playerHeight;
                battleArea = new Rectangle(
                    npcPixelX - leftVisionWidth, // Extend leftward
                    npcPixelY - (playerHeight / 2), // Center vertically on NPC
                    leftVisionWidth,
                    playerHeight
                );
                break;
                
            case RIGHT: // Looking right - horizontal rectangle
                int rightVisionWidth = 5 * playerHeight;
                battleArea = new Rectangle(
                    npcPixelX + tileSize, // Start after NPC sprite
                    npcPixelY - (playerHeight / 2),
                    rightVisionWidth,
                    playerHeight
                );
                break;
                
            default:
                battleArea = new Rectangle(
                    npcPixelX - tileSize,
                    npcPixelY - tileSize,
                    tileSize * 2,
                    tileSize * 2
                );
                break;
        }
        
        return battleArea;
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

    public int getWorldX() {
        return (int) Math.round(exactX * Board.TILE_SIZE);
    }
    
    public int getWorldY() {
        return (int) Math.round(exactY * Board.TILE_SIZE);
    }
    
    
    public Rectangle getBounds(int tileSize) {
        return new Rectangle(
            getWorldX(),
            getWorldY(),
            tileSize,
            tileSize
        );
    }

    public boolean isMovingTowardsPlayer() {
        return isMovingTowardsPlayer;
    }

    public void showExclamationIcon() {
        if (!iconDisplayed && !hasBeenDefeated && !defeated) {
            int iconX = getWorldX() + (Board.TILE_SIZE / 2);
            int iconY = getWorldY();
            exclamationIcon.show(iconX, iconY);
            iconDisplayed = true;
            waitingForIconComplete = false;
        }
    }
    
    public void updateIcon() {
        if (exclamationIcon != null) {
            exclamationIcon.update();
            
            // Check if icon animation is complete and we should start approaching
            if (iconDisplayed && !waitingForIconComplete && exclamationIcon.isComplete()) {
                waitingForIconComplete = true;
                // Icon has finished displaying, now start approaching
                if (targetPlayer != null) {
                    startApproachingPlayer(targetPlayer, Board.TILE_SIZE);
                }
            }
        }
    }

    public void drawIcon(Graphics g) {
        if (exclamationIcon != null) {
            exclamationIcon.draw(g);
        }
    }
    
    public boolean isIconComplete() {
        return exclamationIcon != null && exclamationIcon.isComplete();
    }

    public void drawIconWithZoom(Graphics2D g2d, int zoomLevel) {
        if (sprite != null && !isDefeated()) {
            int effectiveTileSize = Board.TILE_SIZE * zoomLevel;
            
            // Calculate scaled position
            int scaledX = position.x * effectiveTileSize;
            int scaledY = position.y * effectiveTileSize;
            
            // Calculate scaled size
            int scaledWidth = width * zoomLevel;
            int scaledHeight = height * zoomLevel;
            
            g2d.drawImage(sprite, scaledX, scaledY, scaledWidth, scaledHeight, null);
            
            // Draw any additional UI elements (like exclamation marks) if needed
            if (canInitiateBattle() && !isDefeated()) {
                // Scale UI elements too
                g2d.setColor(Color.RED);
                g2d.fillOval(scaledX + scaledWidth - 8 * zoomLevel, scaledY - 8 * zoomLevel, 
                            8 * zoomLevel, 8 * zoomLevel);
            }
        }
    }

    // Also add the regular drawIcon method if it doesn't exist
    public void drawIcon(Graphics2D g2d) {
        drawIconWithZoom(g2d, 1); // Default zoom level of 1
    }

}
