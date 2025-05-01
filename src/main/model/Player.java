package model;

import java.awt.Point;

public class Player extends Trainer {
    protected Point pos;
    protected String facingFront;
    protected String facingLeft;
    protected String facingRight;
    protected String facingBack;

    public Player(String name) {
        super(name);
        initSprites();
        setSprite(facingFront);
        pos = new Point(0, 0);
    }

    public void initSprites() {
        facingFront = "/resources/s_facing_front.png";
        facingLeft = "/resources/s_facing_left.png";
        facingRight = "/resources/s_facing_right.png";
        facingBack = "/resources/s_facing_back.png";
    }

    public String getSprite(String dir) {
        if (dir == "front") return facingFront;
        else if (dir == "left") return facingLeft;
        else if (dir == "right") return facingRight;
        else return facingBack;
    }

    public Point getPosition() {
        return pos;
    }

    public void move(int dx, int dy) {
        pos.translate(dx, dy);
    }

    public void tick(int maxCols, int maxRows) {
        if (pos.x < 0) pos.x = 0;
        else if (pos.x >= maxCols) pos.x = maxCols - 1;
        if (pos.y < 0) pos.y = 0;
        else if (pos.y >= maxRows) pos.y = maxRows - 1;
    }
}
