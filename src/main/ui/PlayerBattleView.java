package ui;

import model.Player;
import model.Player.Direction;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class PlayerBattleView {
    private final Player player;
    private Image currentImage;
    
    // Cache images to avoid reloading
    private Map<String, Image> imageCache = new HashMap<>();
    
    // Throwing animation state
    private boolean isThrowingPokeball = false;
    private int throwingFrame = 0;
    private static final int THROWING_FRAMES = 4;
    
    // Battle positioning and visibility
    private int battleX;
    private int battleY;
    private boolean isVisible = false; // Start hidden by default

    public PlayerBattleView(Player player, int battleX, int battleY) {
        this.player = player;
        this.battleX = battleX;
        this.battleY = battleY;
        
        preloadBattleImages();
        loadImage();
    }
    
    private void preloadBattleImages() {
        String[] directions = {"front", "back", "left", "right"};
        
        for (String dir : directions) {
            String facingPath = String.format("/resources/player_sprites/battle_sprites/male_facing.png", dir);
            cacheImage(facingPath);
            
            for (int i = 0; i < THROWING_FRAMES; i++) {
                String throwingPath = String.format("/resources/player_sprites/battle_sprites/male_throwing_%d.png", i);
                cacheImage(throwingPath);
            }
        }
    }
    
    private void cacheImage(String path) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            if (img != null) {
                imageCache.put(path, img);
            }
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Battle sprite not found: " + path);
        }
    }

    public void loadImage() {
        String path;
        Direction direction = player.getDirection();
        
        if (isThrowingPokeball) {
            path = String.format("/resources/player_sprites/battle_sprites/male_throwing_%d.png", throwingFrame);
        } else {
            path = String.format("/resources/player_sprites/battle_sprites/male_facing.png", 
                                direction.toString().toLowerCase());
        }
        
        if (imageCache.containsKey(path)) {
            currentImage = imageCache.get(path);
        } else {
            String fallbackPath = "/resources/player_sprites/battle_sprites/male_facing.png";
            if (imageCache.containsKey(fallbackPath)) {
                currentImage = imageCache.get(fallbackPath);
            } else {
                loadFallbackImage();
            }
        }
    }
    
    private void loadFallbackImage() {
        try {
            String path = String.format("/resources/player_sprites/s_facing_%s.png", 
                                      player.getDirection().toString().toLowerCase());
            currentImage = ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            System.out.println("Error loading fallback image: " + e.getMessage());
        }
    }
    
    /**
     * Start the pokeball throwing animation and make player visible
     */
    public void startThrowingAnimation() {
        isThrowingPokeball = true;
        throwingFrame = 0;
        isVisible = true; // Show player when throwing
        loadImage();
    }
    
    /**
     * Advance the throwing animation frame
     */
    public boolean advanceThrowingFrame() {
        if (!isThrowingPokeball) return true;
        
        throwingFrame++;
        if (throwingFrame >= THROWING_FRAMES) {
            isThrowingPokeball = false;
            throwingFrame = 0;
            isVisible = false; // Hide player after throwing
            loadImage();
            return true;
        }
        
        loadImage();
        return false;
    }
    
    /**
     * Stop the throwing animation and hide player
     */
    public void stopThrowingAnimation() {
        isThrowingPokeball = false;
        throwingFrame = 0;
        isVisible = false; // Hide player
        loadImage();
    }
    
    /**
     * Show player temporarily (for throwing animations only)
     */
    public void showForThrow() {
        isVisible = true;
    }
    
    /**
     * Hide player (default battle state)
     */
    public void hidePlayer() {
        isVisible = false;
    }
    
    public boolean isThrowingPokeball() {
        return isThrowingPokeball;
    }
    
    public void updateDirection(Direction direction) {
        player.setDirection(direction);
        if (!isThrowingPokeball) {
            loadImage();
        }
    }
    
    public void setBattlePosition(int x, int y) {
        this.battleX = x;
        this.battleY = y;
    }
    
    public int getBattleX() {
        return battleX;
    }
    
    public int getBattleY() {
        return battleY;
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public Point getThrowingCenter() {
        if (currentImage != null) {
            return new Point(
                battleX + currentImage.getWidth(null) / 2,
                battleY + currentImage.getHeight(null) / 2
            );
        }
        return new Point(battleX + 32, battleY + 32);
    }

    /**
     * Draw the player only when visible
     */
    public void draw(Graphics g, ImageObserver observer) {
        if (currentImage != null && isVisible) {
            g.drawImage(currentImage, battleX, battleY, observer);
        }
        // Don't draw anything if not visible - this is the key change
    }
    
    public Dimension getSpriteDimensions() {
        if (currentImage != null) {
            return new Dimension(currentImage.getWidth(null), currentImage.getHeight(null));
        }
        return new Dimension(64, 64);
    }
    
    /**
     * Reset to hidden battle state
     */
    public void resetBattleState() {
        isThrowingPokeball = false;
        throwingFrame = 0;
        isVisible = false; // Start hidden
        loadImage();
    }
}
