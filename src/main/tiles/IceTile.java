package tiles;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class IceTile extends Tile {
    public IceTile() {
        super(loadGrassImage(), false);
    }
    
    private static BufferedImage loadGrassImage() {
        try {
            return ImageIO.read(IceTile.class.getResourceAsStream("/resources/tiles/ice_tile.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
