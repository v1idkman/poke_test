package ui;

public class Camera {
    private int x, y;
    private int width, height;
    private int worldWidth, worldHeight;
    
    public Camera(int width, int height, int worldWidth, int worldHeight) {
        this.width = width;
        this.height = height;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    public void update(int playerX, int playerY) {
        // Get player's dimensions (assuming the player sprite is 16x16 pixels)
        int playerWidth = 16 * 5;
        int playerHeight = 16 * 5;
        
        // Calculate player's center coordinates
        int playerCenterX = playerX + (playerWidth / 2);
        int playerCenterY = playerY + (playerHeight / 2);
        
        // Center camera on player's center point
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        
        // Set camera position to center on player
        x = playerCenterX - halfWidth;
        y = playerCenterY - halfHeight;
        
        // Clamp camera position within world boundaries
        x = Math.max(0, Math.min(x, worldWidth - width));
        y = Math.max(0, Math.min(y, worldHeight - height));
    }    
    
    public int getX() { return x; }
    public int getY() { return y; }
}
