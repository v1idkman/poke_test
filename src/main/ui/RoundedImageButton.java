package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JButton;

public class RoundedImageButton extends JButton {
    private static final int ARC_WIDTH = 20;
    private static final int ARC_HEIGHT = 20;
    private BufferedImage backgroundImage;
    private Shape shape;
    private Shape base;
    
    public RoundedImageButton(String imagePath) {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        
        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getResource(imagePath));
        } catch (Exception e) {
            System.err.println("Failed to load button background: " + e.getMessage());
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        initShape();
        
        // Clip to rounded shape
        g2.setClip(shape);
        
        if (backgroundImage != null) {
            // Draw background image scaled to button size
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback to solid color
            g2.setColor(getBackground());
            g2.fill(shape);
        }
        
        // Add semi-transparent overlay for better text readability
        g2.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black
        g2.fill(shape);
        
        // Reset clip and paint text
        g2.setClip(null);
        super.paintComponent(g2);
        g2.dispose();
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        initShape();
        
        // Draw black outline
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3.0f)); // Thicker outline for better visibility
        g2.draw(shape);
        
        g2.dispose();
    }
    
    private void initShape() {
        if (!getBounds().equals(base)) {
            base = getBounds();
            shape = new RoundRectangle2D.Float(
                1, 1, getWidth() - 2, getHeight() - 2, 
                ARC_WIDTH, ARC_HEIGHT);
        }
    }
    
    @Override
    public boolean contains(int x, int y) {
        initShape();
        return shape != null && shape.contains(x, y);
    }
}
