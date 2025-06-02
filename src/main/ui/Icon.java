package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Icon {
    private int x, y;
    private float alpha = 1.0f;
    private boolean visible = false;
    private boolean fading = false;
    private long displayStartTime;
    private long fadeStartTime;
    private BufferedImage iconImage;
    
    private static final int DISPLAY_DURATION = 1000; // 1 second display
    private static final int FADE_DURATION = 500;     // 0.5 second fade
    private static final int ICON_SIZE = 16;
    
    public Icon(String iconType) {
        loadIconImage(iconType);
    }
    
    private void loadIconImage(String iconType) {
        String imagePath;
        switch (iconType) {
            case "exclamation":
                imagePath = "/resources/icons/exclamation.png";
                break;
            case "cave_exclamation":
                imagePath = "/resources/icons/cave_exclamation.png";
                break;
            case "question":
                imagePath = "/resources/icons/question.png";
                break;
            case "heart":
                imagePath = "/resources/icons/heart.png";
                break;
            default:
                throw new IllegalArgumentException("Unknown icon type: " + iconType);
        }
        
        try {
            iconImage = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load icon: " + imagePath + " - " + e.getMessage());
            // Create a fallback icon if image loading fails
            createFallbackIcon(iconType);
        }
    }
    
    private void createFallbackIcon(String iconType) {
        iconImage = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = iconImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw fallback based on icon type
        if (iconType.equals("exclamation")) {
            // Yellow circle with exclamation mark
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(0, 0, ICON_SIZE - 1, ICON_SIZE - 1);
            
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "!";
            int textX = (ICON_SIZE - fm.stringWidth(text)) / 2;
            int textY = (ICON_SIZE + fm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);
        }
        
        g2d.dispose();
    }
    
    public void show(int x, int y) {
        this.x = x;
        this.y = y - 24; // Position above the NPC sprite
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
        if (!visible || iconImage == null) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Center the icon
        int drawX = x - iconImage.getWidth() / 2;
        int drawY = y - iconImage.getHeight() / 2;
        
        g2d.drawImage(iconImage, drawX, drawY, null);
        g2d.dispose();
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isComplete() {
        return !visible && fading;
    }
    
    public void hide() {
        visible = false;
        fading = false;
        alpha = 0;
    }
}