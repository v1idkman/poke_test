package tiles;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class HouseTileGold extends Tile {
    public HouseTileGold() {
        super(loadGrassImage(), false);
    }
    
    private static BufferedImage loadGrassImage() {
        try {
            return ImageIO.read(HouseTileGold.class.getResourceAsStream("/resources/tiles/house_tile_golden.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
