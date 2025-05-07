package ui;

public class Camera {
    private int x, y;
    private int width, height;
    private int worldWidth, worldHeight;
    private float horizontalThreshold = 0.4f; // 40% of screen width
    private float verticalThreshold = 0.4f;   // 40% of screen height
    
    public Camera(int width, int height, int worldWidth, int worldHeight) {
        this.width = width;
        this.height = height;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    public void update(int playerX, int playerY) {
        // Calculate thresholds in pixels
        int horizontalBoundary = (int)(width * horizontalThreshold);
        int verticalBoundary = (int)(height * verticalThreshold);
        
        // Calculate player position relative to camera
        int relativeX = playerX - x;
        int relativeY = playerY - y;
        
        // Update camera position if player crosses threshold
        if (relativeX > width - horizontalBoundary) {
            x = playerX - (width - horizontalBoundary);
        } else if (relativeX < horizontalBoundary) {
            x = playerX - horizontalBoundary;
        }
        
        if (relativeY > height - verticalBoundary) {
            y = playerY - (height - verticalBoundary);
        } else if (relativeY < verticalBoundary) {
            y = playerY - verticalBoundary;
        }
        
        // Ensure camera doesn't go out of world bounds
        x = Math.max(0, Math.min(x, worldWidth - width));
        y = Math.max(0, Math.min(y, worldHeight - height));
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}
