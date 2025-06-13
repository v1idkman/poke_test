package ui;

import model.Player;

public class Camera {
    private static Camera instance;
    private int x, y;
    private int worldWidth, worldHeight;
    private boolean isActive;
    private int zoomLevel = 1;
    
    private Camera() {
    }
    
    public static Camera getInstance() {
        if (instance == null) {
            instance = new Camera();
        }
        return instance;
    }
    
    public void setWorldDimensions(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = Math.max(1, Math.min(4, zoomLevel)); // Clamp between 1-4
    }
    
    /**
     * Get the current zoom level
     * @return current zoom level
     */
    public int getZoomLevel() {
        return zoomLevel;
    }
    
    public void update(Player player) {
        if (!isActive) return;
        
        // Get logical viewport dimensions (screen size divided by zoom)
        int logicalViewportWidth = App.CURRENT_WIDTH / zoomLevel;
        int logicalViewportHeight = App.CURRENT_HEIGHT / zoomLevel;
        
        // Center camera on player (in logical coordinates)
        int targetX = player.getWorldX() + (Board.TILE_SIZE / 2) - (logicalViewportWidth / 2);
        int targetY = player.getWorldY() + (Board.TILE_SIZE / 2) - (logicalViewportHeight / 2);
        
        // Smooth camera following
        float followSpeed = 0.1f;
        x = (int)(x + (targetX - x) * followSpeed);
        y = (int)(y + (targetY - y) * followSpeed);
        
        // Clamp camera to world bounds
        x = Math.max(0, Math.min(x, worldWidth - logicalViewportWidth));
        y = Math.max(0, Math.min(y, worldHeight - logicalViewportHeight));
        
        // Handle cases where world is smaller than viewport
        if (worldWidth < logicalViewportWidth) {
            x = -(logicalViewportWidth - worldWidth) / 2;
        }
        if (worldHeight < logicalViewportHeight) {
            y = -(logicalViewportHeight - worldHeight) / 2;
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
