package ui;

public class Camera {
    private int x;
    private int y;
    private Board board;
    
    public Camera(Board board) {
        this.board = board;
        this.x = 0;
        this.y = 0;
    }
    
    public void update(int playerX, int playerY) {
        // Center camera on player (in pixel coordinates)
        x = playerX - board.getWidth() / 2;
        y = playerY - board.getHeight() / 2;
        
        // Keep camera within map bounds
        x = Math.max(0, Math.min(x, board.columns * Board.TILE_SIZE - board.getWidth()));
        y = Math.max(0, Math.min(y, board.rows * Board.TILE_SIZE - board.getHeight()));
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}