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
}
