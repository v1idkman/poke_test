package tiles;

import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage image;
    private boolean collision;
    private boolean swimmable;
    private String name;
    private int id;
    
    public Tile(int id, String name, BufferedImage image, boolean collision, boolean swimmable, boolean encounterable) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.collision = collision;
        this.swimmable = swimmable;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public boolean hasCollision() {
        return collision;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }

    public boolean isSwimmable() {
        return swimmable;
    }
}
