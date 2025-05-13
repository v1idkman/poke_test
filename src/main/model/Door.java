package model;

import java.awt.Point;
import java.awt.Rectangle;

public class Door extends WorldObject {
    private String targetWorld;
    private Point spawnPoint;
    
    public Door(Point position, String spriteLocation, String targetWorld, Point spawnPoint) {
        super(position, spriteLocation);
        this.targetWorld = targetWorld;
        this.spawnPoint = spawnPoint;
    }
    
    public String getTargetWorld() {
        return targetWorld;
    }
    
    public Point getSpawnPoint() {
        return spawnPoint;
    }
    
    @Override
    public Rectangle getBounds(int tileSize) {
        // Create a smaller hitbox just for the door area
        return new Rectangle(
            position.x * tileSize, 
            position.y * tileSize,
            sprite.getWidth(null),
            sprite.getHeight(null)
        );
    }

}
