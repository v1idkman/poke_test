package tiles;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class DirtTile extends Tile {
    public DirtTile() {
        super(loadGrassImage(), false);
    }
    
    private static BufferedImage loadGrassImage() {
        try {
            return ImageIO.read(GrassTile.class.getResourceAsStream("/resources/tiles/dirt_tile.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
