package model;

import java.awt.Point;
import java.awt.Rectangle;

public class Building extends WorldObject {

    public Building(Point position, String location) {
        super(position, location);
    }

    @Override
    public Rectangle getBounds(int tileSize) {
        // Create a collision box that excludes the doorway
        return new Rectangle(
            position.x * tileSize,
            position.y * tileSize,
            width,
            height
        );
    }
    
    // Add method to create a doorway opening in the collision
    public Rectangle getDoorwayBounds(int tileSize) {
        // Define where the door is on this building
        int doorX = position.x * tileSize + (width / 2) - (tileSize / 2);
        int doorY = position.y * tileSize + height - tileSize;
        return new Rectangle(doorX, doorY, tileSize, tileSize);
    }

}
