package ui;

import model.Player;

public class Camera {
    private static Camera instance;
    private int x;
    private int y;
    private boolean active;
    private int viewportWidth;
    private int viewportHeight;
    private int worldWidth;
    private int worldHeight;
    
    private Camera() {
        this.x = 0;
        this.y = 0;
        this.active = false;
        this.viewportWidth = App.FIXED_WIDTH;
        this.viewportHeight = App.FIXED_HEIGHT;
        this.worldWidth = 0;
        this.worldHeight = 0;
    }

    public static Camera getInstance() {
        if (instance == null) {
            instance = new Camera();
        }
        return instance;
    }
    
    public void setWorldDimensions(int width, int height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }
    
    public void setViewportDimensions(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }
    
    public void update(Player player) {
        // If camera isn't active for small worlds, don't update position
        if (!active) {
            x = 0;
            y = 0;
            return;
        }
        
        // Get player position and calculate center
        int playerX = player.getWorldX();
        int playerY = player.getWorldY();
        
        // Calculate player's center
        int playerWidth = Board.TILE_SIZE;
        int playerHeight = Board.TILE_SIZE;
        
        int playerCenterX = playerX + (playerWidth / 2);
        int playerCenterY = playerY + (playerHeight / 2);
        
        // Calculate the ideal camera position (player centered)
        int idealX = playerCenterX - (viewportWidth / 2);
        int idealY = playerCenterY - (viewportHeight / 2);
        
        // Clamp camera position to prevent showing outside the world
        x = Math.max(0, Math.min(idealX, worldWidth - viewportWidth));
        y = Math.max(0, Math.min(idealY, worldHeight - viewportHeight));
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
