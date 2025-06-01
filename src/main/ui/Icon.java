package ui;

import java.awt.*;

public class Icon {
    private int x, y;
    private float alpha = 1.0f;
    private boolean visible = false;
    private boolean fading = false;
    private long displayStartTime;
    private long fadeStartTime;
    
    private static final int DISPLAY_DURATION = 1000; // 1 second display
    private static final int FADE_DURATION = 500;     // 0.5 second fade
    private static final int ICON_SIZE = 16;
    
    // TODO: the icon does not appear and messes up the battle initiation
    public Icon(String name) {
        if (name.equals("exclamation")) {
            // load exclamation icon
        } else if (name.equals("cave_exclamation")) {
            // load question icon
        } else {
            throw new IllegalArgumentException("Unknown icon type: " + name);
        }
    }
    
    public void show(int x, int y) {
        this.x = x;
        this.y = y - 40; // Position above the NPC
        this.visible = true;
        this.fading = false;
        this.alpha = 1.0f;
        this.displayStartTime = System.currentTimeMillis();
    }
    
    public void update() {
        if (!visible) return;
        
        long currentTime = System.currentTimeMillis();
        
        if (!fading && (currentTime - displayStartTime) >= DISPLAY_DURATION) {
            // Start fading
            fading = true;
            fadeStartTime = currentTime;
        }
        
        if (fading) {
            long fadeElapsed = currentTime - fadeStartTime;
            alpha = 1.0f - ((float) fadeElapsed / FADE_DURATION);
            
            if (alpha <= 0) {
                visible = false;
                alpha = 0;
            }
        }
    }
    
    public void draw(Graphics g) {
        if (!visible) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Draw exclamation mark background (yellow circle)
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x - ICON_SIZE/2, y - ICON_SIZE/2, ICON_SIZE, ICON_SIZE);
        
        // Draw exclamation mark border
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x - ICON_SIZE/2, y - ICON_SIZE/2, ICON_SIZE, ICON_SIZE);
        
        // Draw exclamation mark
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String exclamation = "!";
        int textX = x - fm.stringWidth(exclamation) / 2;
        int textY = y + fm.getAscent() / 2 - 2;
        g2d.drawString(exclamation, textX, textY);
        
        g2d.dispose();
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isComplete() {
        return !visible && fading;
    }
}
