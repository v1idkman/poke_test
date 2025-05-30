package ui;

import model.Drawable;
import model.Npc;
import model.Npc.Direction;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class NpcView implements Drawable {
    private final Npc npc;
    private Image currentImage;
    private String npcType; // e.g., "bug_catcher"
    
    // Cache images to avoid reloading
    private Map<String, Image> imageCache = new HashMap<>();

    public NpcView(Npc npc, String npcType) {
        this.npc = npc;
        this.npcType = npcType;
        
        if (npc.canMove()) {
            preloadMovingImages();
        } else {
            preloadStaticImages();
        }
        
        loadImage();
    }
    
    private void cacheImage(String path) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            if (img != null) {
                imageCache.put(path, img);
            }
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error preloading NPC image: " + path + " - " + exc.getMessage());
        }
    }
    
    private void preloadStaticImages() {
        String[] directions = {"front", "back", "left", "right"};
        for (String dir : directions) {
            String facingPath = String.format("/resources/npc_sprites/%s/facing_%s.png", npcType, dir);
            cacheImage(facingPath);
        }
    }
    
    private void preloadMovingImages() {
        String[] directions = {"front", "back", "left", "right"};
        for (String dir : directions) {
            // Static facing sprites
            String facingPath = String.format("/resources/npc_sprites/%s/facing_%s.png", npcType, dir);
            cacheImage(facingPath);
            
            // Walking animation sprites
            for (int i = 0; i < 2; i++) {
                String walkingPath = String.format("/resources/npc_sprites/%s/walking_%s_%d.png", npcType, dir, i);
                cacheImage(walkingPath);
            }
        }
    }

    public void loadImage() {
        String path;
        boolean isMoving = npc.isMoving();
        Direction direction = npc.getDirection();
        
        if (isMoving && npc.canMove()) {
            path = String.format(
                "/resources/npc_sprites/%s/walking_%s_%d.png",
                npcType,
                direction.toString().toLowerCase(),
                npc.getAnimationFrame()
            );
        } else {
            path = String.format(
                "/resources/npc_sprites/%s/facing_%s.png",
                npcType,
                direction.toString().toLowerCase()
            );
        }
        
        if (imageCache.containsKey(path)) {
            currentImage = imageCache.get(path);
        } else {
            System.out.println("NPC sprite not in cache, loading from file: " + path);
            try {
                currentImage = ImageIO.read(getClass().getResource(path));
                if (currentImage != null) {
                    imageCache.put(path, currentImage);
                }
            } catch (Exception e) {
                System.out.println("Error loading NPC image: " + e.getMessage());
                // Fallback to static sprite
                String fallbackPath = String.format("/resources/npc_sprites/%s/facing_%s.png", 
                                                  npcType, direction.toString().toLowerCase());
                if (imageCache.containsKey(fallbackPath)) {
                    currentImage = imageCache.get(fallbackPath);
                }
            }
        }
    }

    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        if (currentImage != null) {
            int x = npc.getPosition().x * tileSize;
            int y = npc.getPosition().y * tileSize;
            g.drawImage(currentImage, x, y, observer);
        } else {
            // Fallback if image is missing
            g.setColor(Color.BLUE);
            g.fillRect(npc.getPosition().x * tileSize, npc.getPosition().y * tileSize, tileSize, tileSize);
        }
    }
    
    public void update() {
        loadImage();
    }
    
    // ADD THIS METHOD - This is what was missing!
    public Image getCurrentImage() {
        return currentImage;
    }
}
