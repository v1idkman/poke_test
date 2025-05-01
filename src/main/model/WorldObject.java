package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class WorldObject implements Drawable {
    protected Point position;
    protected String location;
    protected Image sprite;
    protected int width;
    protected int height;

    public WorldObject(Point position, String location) {
        try {
            sprite = ImageIO.read(getClass().getResource(location));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
            sprite = null;
        }
        this.position = position;
        width = sprite.getWidth(null);
        height = sprite.getHeight(null);
    }

    // The draw method to draw the object on the board
    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        if (sprite != null) {
            g.drawImage(
                sprite,
                position.x * tileSize,
                position.y * tileSize,
                observer
            );
        } else {
            // Fallback: draw a placeholder if image is missing
            g.setColor(Color.GRAY);
            g.fillRect(position.x * tileSize, position.y * tileSize, tileSize, tileSize);
        }
    }

    public Point getPosition() {
        return position;
    }

    public Image getSprite() {
        return sprite;
    }

    public Rectangle getBounds(int tileSize) {
        return new Rectangle(position.x * tileSize, position.y * tileSize, width, height);
    }
}
