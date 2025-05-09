package ui;

import model.Drawable;
import model.Player;
import model.Player.Direction;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class PlayerView implements Drawable {
    private final Player player;
    private Image currentImage;
    
    // Cache images to avoid reloading
    private Map<String, Image> imageCache = new HashMap<>();

    public PlayerView(Player player) {
        this.player = player;
        // Preload all animation images
        preloadImages();
        loadImage();

        player.setSpriteSize(currentImage.getWidth(null), currentImage.getHeight(null));
    }
    
    private void cacheImage(String path) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            if (img != null) {
                imageCache.put(path, img);
            }
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error preloading image: " + path + " - " + exc.getMessage());
        }
    }
    
    private void preloadImages() {
        String[] directions = {"front", "back", "left", "right"};
        for (String dir : directions) {
            String facingPath = String.format("/resources/player_sprites/s_facing_%s.png", dir);
            cacheImage(facingPath);
            
            for (int i = 0; i < 2; i++) {
                String walkingPath = String.format("/resources/player_sprites/s_walking_%s_%d.png", dir, i);
                cacheImage(walkingPath);
            }
        }
    }

    public void loadImage() {
        String path;
        boolean isMoving = player.isMoving();
        Direction direction = player.getDirection();
        
        if (isMoving) {
            path = String.format(
                "/resources/player_sprites/s_walking_%s_%d.png",
                direction.toString().toLowerCase(),
                player.getAnimationFrame()
            );
        } else {
            path = String.format(
                "/resources/player_sprites/s_facing_%s.png",
                direction.toString().toLowerCase()
            );
        }
        
        if (imageCache.containsKey(path)) {
            currentImage = imageCache.get(path);
        } else {
            System.out.println("Not in cache, loading from file");
            try {
                currentImage = ImageIO.read(getClass().getResource(path));
                if (currentImage != null) {
                    imageCache.put(path, currentImage);
                }
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }
    }

    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        if (currentImage != null) {
            // Draw at exact pixel position for smooth movement
            int x = (int)(player.getWorldX());
            int y = (int)(player.getWorldY());
            g.drawImage(currentImage, x, y, observer);
        } else {
            // Fallback if image is missing
            g.setColor(Color.RED);
            g.fillRect(player.getWorldX(), player.getWorldY(), tileSize, tileSize);
        }
    }
}
