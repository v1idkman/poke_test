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
        try {
            image = ImageIO.read(getClass().getResource(player.getSpriteLocation()));
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
