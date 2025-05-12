package tiles;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GrassTile extends Tile {
    public GrassTile() {
        super(loadGrassImage(), false);
    }
    
    private static BufferedImage loadGrassImage() {
        try {
            return ImageIO.read(GrassTile.class.getResourceAsStream("/resources/tiles/grass.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
