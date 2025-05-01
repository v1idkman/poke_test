package model;

import java.awt.Graphics;
import java.awt.image.ImageObserver;

public interface Drawable {
    void draw(Graphics g, ImageObserver obs, int tileSize);
}
