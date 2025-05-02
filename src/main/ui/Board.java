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
    private static final int PLAYER_HEIGHT_COLLISION = 30;
    private List<WorldObject> objects;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    
    private int animationCounter = 0;
    private static final int ANIMATION_DELAY = 5;

    public Board(Player player) {
        objects = new ArrayList<>();
        setPreferredSize(new Dimension(TILE_SIZE * COLUMNS, TILE_SIZE * ROWS));
        setBackground(new Color(232, 232, 232));
        this.player = player;
        playerView = new PlayerView(player);
        timer = new Timer(DELAY, this);
        timer.start();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addObject("/resources/buildings/red_house.png", 40, 40);
        addObject("/resources/buildings/red_house.png", 60, 80);
        addObject("/resources/buildings/red_house.png", 20, 20);
        
        // Initialize the menu singleton with this board's information
        Menu menu = Menu.getInstance();
        menu.setPlayer(player);
        menu.setGameTimer(timer);
        menu.initializeMenuButton(this, TILE_SIZE, COLUMNS, ROWS);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);

        for (WorldObject obj : objects) {
            obj.draw(g, this, TILE_SIZE);
        }

        playerView.draw(g, this, TILE_SIZE);
        drawDebugBounds(g);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isMoving = false;
        if (upPressed && canMove(0, -1)) {
            player.move(0, -1);
            isMoving = true;
        }
        if (downPressed && canMove(0, 1)) {
            player.move(0, 1);
            isMoving = true;
        }
        if (leftPressed && canMove(-1, 0)) {
            player.move(-1, 0);
            isMoving = true;
        }
        if (rightPressed && canMove(1, 0)) {
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
    
    public boolean canMove(int dx, int dy) {
        Rectangle nextBounds = new Rectangle(
            (player.getPosition().x + dx) * TILE_SIZE,
            ((player.getPosition().y + dy) * TILE_SIZE) + PLAYER_HEIGHT_COLLISION,
            player.getWidth(),
            player.getHeight() - PLAYER_HEIGHT_COLLISION
        );
        for (WorldObject obj : objects) {
            if (nextBounds.intersects(obj.getBounds(TILE_SIZE))) {
                return false; // Collision detected
            }
        }
        return true; // No collision
    }

    public void addObject(String path, int x, int y) {
        objects.add(new Building(new Point(x, y), path));
    }

    private void drawDebugBounds(Graphics g) {
        g.setColor(Color.RED);
        // Draw player bounds
        g.drawRect((player.getPosition().x) * TILE_SIZE, ((player.getPosition().y) * TILE_SIZE) + PLAYER_HEIGHT_COLLISION, 
        player.getWidth(), player.getHeight() - PLAYER_HEIGHT_COLLISION);
        
        // Draw object bounds
        g.setColor(Color.BLUE);
        for (WorldObject obj : objects) {
            Rectangle bounds = obj.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
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
}
