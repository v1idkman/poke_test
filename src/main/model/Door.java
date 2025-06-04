package model;

import java.awt.Point;
import java.awt.Rectangle;
import exceptions.NoSuchWorldException;
import ui.Board;

public class Door extends InteractableObject {
    private String targetWorld;
    private Point spawnPoint;
    
    public Door(Point position, String spriteLocation, String targetWorld, Point spawnPoint, Direction direction) {
        super(position, spriteLocation, direction);
        this.targetWorld = targetWorld;
        // Create a defensive copy to prevent external modification
        this.spawnPoint = new Point(spawnPoint.x, spawnPoint.y);
    }
    
    // Overloaded constructor for backward compatibility (defaults to ANY direction)
    public Door(Point position, String spriteLocation, String targetWorld, Point spawnPoint) {
        this(position, spriteLocation, targetWorld, spawnPoint, Direction.ANY);
    }
    
    public String getTargetWorld() {
        return targetWorld;
    }
    
    public Point getSpawnPoint() {
        // Return a copy to prevent external modification
        return new Point(spawnPoint.x, spawnPoint.y);
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public boolean canPlayerEnter(Player.Direction playerDirection) {
        return canPlayerInteract(playerDirection);
    }
    
    @Override
    public void performAction(Player player, Board board) {
        try {
            Point destinationPoint = getSpawnPoint();
            System.out.println("Door teleporting player to: (" + destinationPoint.x + ", " + destinationPoint.y + ")");
            board.getWorldManager().switchWorld(targetWorld, destinationPoint);
        } catch (NoSuchWorldException e) {
            System.err.println("Could not find world: " + targetWorld);
        }
    }
    
    @Override
    public Rectangle getBounds(int tileSize) {
        // Handle null sprite gracefully
        if (sprite == null) {
            return new Rectangle(
                position.x * tileSize, 
                position.y * tileSize,
                tileSize,  // Default width
                tileSize   // Default height
            );
        }
        
        return new Rectangle(
            position.x * tileSize, 
            position.y * tileSize,
            sprite.getWidth(null),
            sprite.getHeight(null)
        );
    }
}