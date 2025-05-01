package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import model.Building;
import model.Player;
import model.WorldObject;

import java.util.List;
import java.util.ArrayList;

public class Board extends JPanel implements ActionListener, KeyListener {
    private final int DELAY = 25;
    public static final int TILE_SIZE = 5;
    public static final int ROWS = 120;
    public static final int COLUMNS = 180;
    private List<WorldObject> objects;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    
    private int animationCounter = 0;
    private static final int ANIMATION_DELAY = 5;

    public Board() {
        objects = new ArrayList<>();
        setPreferredSize(new Dimension(TILE_SIZE * COLUMNS, TILE_SIZE * ROWS));
        setBackground(new Color(232, 232, 232));
        player = new Player("sarp");
        playerView = new PlayerView(player);
        timer = new Timer(DELAY, this);
        timer.start();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        placeObjects();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);

        for (WorldObject obj : objects) {
            obj.draw(g, this, TILE_SIZE);
        }

        playerView.draw(g, this, TILE_SIZE);
        drawScore(g);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isMoving = false;
        if (upPressed) {
            player.move(0, -1);
            isMoving = true;
        }
        if (downPressed) {
            player.move(0, 1);
            isMoving = true;
        }
        if (leftPressed) {
            player.move(-1, 0);
            isMoving = true;
        }
        if (rightPressed) {
            player.move(1, 0);
            isMoving = true;
        }
        player.setMoving(isMoving);
    
        if (isMoving) {
            animationCounter++;
            if (animationCounter >= ANIMATION_DELAY) {
                player.nextAnimationFrame();
                playerView.loadImage();
                animationCounter = 0;
            }
        } else {
            // Optionally reset animation frame to standing
            player.setAnimationFrame(0);
            playerView.loadImage();
        }
    
        player.tick(COLUMNS, ROWS);
        repaint();
    }
    

    public void placeObjects() {
        objects.add(new Building(new Point(5, 5), "/resources/red_house.png"));
    }

    @Override
    public void keyTyped(KeyEvent e) {}

        @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) {
            upPressed = true;
            player.setDirection(Player.Direction.FRONT);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
            player.setDirection(Player.Direction.RIGHT);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_DOWN) {
            downPressed = true;
            player.setDirection(Player.Direction.BACK);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
            player.setDirection(Player.Direction.LEFT);
            player.setMoving(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_DOWN) downPressed = false;
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;

        // If no movement keys are pressed, stop animation
        if (!upPressed && !downPressed && !leftPressed && !rightPressed) {
            player.setMoving(false);
            player.setAnimationFrame(0); // Reset to standing frame
            playerView.loadImage();
        }
    }


    private void drawBackground(Graphics g) {
        g.setColor(new Color(214, 214, 214));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if ((row + col) % 2 == 1) {
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        String text = "TEST";
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setColor(new Color(30, 201, 139));
        g2d.setFont(new Font("Lato", Font.BOLD, 25));
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        Rectangle rect = new Rectangle(0, TILE_SIZE * (ROWS - 1), TILE_SIZE * COLUMNS, TILE_SIZE);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2d.drawString(text, x, y);
    }
}
