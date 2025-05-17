package tiles;

import java.awt.image.BufferedImage;

public class AnimatedTile extends Tile {
    private BufferedImage[] frames;
    private int currentFrame;
    private long lastFrameTime;
    private int animationDelay; // in milliseconds
    
    public AnimatedTile(int id, String name, BufferedImage[] frames, boolean collision, 
                        boolean swimmable, boolean encounterable, int animationDelay) {
        super(id, name, frames[0], collision, swimmable, encounterable);
        this.frames = frames;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.animationDelay = animationDelay;
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > animationDelay) {
            currentFrame = (currentFrame + 1) % frames.length;
            lastFrameTime = currentTime;
        }
    }
    
    @Override
    public BufferedImage getImage() {
        return frames[currentFrame];
    }
}
