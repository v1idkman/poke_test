package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;

public abstract class WorldObject implements Drawable {
    protected Point position;
    protected String location;
    protected Image sprite;
    protected int width;
    protected int height;

    protected boolean walkable = false;

    public WorldObject(Point position, String location) {
        this.position = position;
        this.location = location;
        
        try {
            if (location != null) {
                // Try loading as resource first
                sprite = ImageIO.read(getClass().getResource(location));
                
                if (sprite == null) {
                    // Fallback: try loading from file system
                    java.io.File file = new java.io.File("." + location);
                    if (file.exists()) {
                        sprite = ImageIO.read(file);
                        System.out.println("Loaded sprite from file system: " + location);
                    } else {
                        // Try alternative resource paths
                        String[] alternatePaths = {
                            location.substring(1), // Remove leading slash
                            "sprites/sprites/items/" + location.substring(location.lastIndexOf("/") + 1),
                            "/resources" + location
                        };
                        
                        for (String altPath : alternatePaths) {
                            try {
                                sprite = ImageIO.read(getClass().getResource(altPath));
                                if (sprite != null) {
                                    System.out.println("Found sprite at: " + altPath);
                                    break;
                                }
                            } catch (Exception e) {
                                // Continue trying
                            }
                        }
                    }
                }
            }
            
            if (sprite != null) {
                width = sprite.getWidth(null);
                height = sprite.getHeight(null);
            } else {
                System.err.println("Could not load sprite: " + location);
                width = 32;
                height = 32;
            }
            
        } catch (Exception exc) {
            // System.err.println("Error loading sprite: " + location + " - " + exc.getMessage());
            sprite = null;
            width = 32;
            height = 32;
        }
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

    public boolean isWalkable() {
        return walkable;
    }
    
    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    protected void loadSprite(String spritePath) {
        try {
            java.io.File file = new java.io.File(spritePath);
            if (file.exists()) {
                sprite = new javax.swing.ImageIcon(file.getAbsolutePath()).getImage();
                System.out.println("Successfully loaded sprite: " + spritePath);
            } else {
                System.err.println("Sprite file not found: " + spritePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading sprite: " + e.getMessage());
        }
    }

    public void drawWithZoom(Graphics g, ImageObserver observer, int tileSize, int zoomLevel) {
        int effectiveTileSize = tileSize * zoomLevel;
        
        if (sprite != null) {
            g.drawImage(
                sprite,
                position.x * effectiveTileSize,
                position.y * effectiveTileSize,
                width * zoomLevel,
                height * zoomLevel,
                observer
            );
        } else {
            // Fallback: draw a placeholder if image is missing
            g.setColor(Color.GRAY);
            g.fillRect(
                position.x * effectiveTileSize, 
                position.y * effectiveTileSize, 
                effectiveTileSize, 
                effectiveTileSize
            );
        }
    }

}
