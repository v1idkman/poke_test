package ui;

import model.Player;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerView {
    private final Player player;
    private Image image;

    public PlayerView(Player player) {
        this.player = player;
        loadImage();
    }

    public void loadImage() {
        String path;
        if (player.isMoving()) {
            // Walking animation frame
            path = String.format(
                "/resources/s_walking_%s_%d.png",
                player.getDirection().toString().toLowerCase(),
                player.getAnimationFrame()
            );
        } else {
            // Standing sprite
            path = String.format(
                "/resources/s_facing_%s.png",
                player.getDirection().toString().toLowerCase()
            );
        }
        try {
            image = ImageIO.read(getClass().getResource(path));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
            image = null;
        }
    }
    

    public void draw(Graphics g, ImageObserver observer, int tileSize) {
        Point pos = player.getPosition();
        if (image != null) {
            g.drawImage(image, pos.x * tileSize, pos.y * tileSize, observer);
        } else {
            g.setColor(Color.RED);
            g.fillRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
        }
    }
}
