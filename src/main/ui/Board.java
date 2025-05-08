package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import model.Building;
import model.Door;
import model.Player;
import model.Player.Direction;
import model.WorldObject;

import java.util.List;
import java.util.ArrayList;

public class Board extends JPanel implements ActionListener, KeyListener {
    private final int DELAY = 50;
    public static final int TILE_SIZE = 5;
    public static final int ROWS = 120;
    public static final int COLUMNS = 180;
    private List<WorldObject> objects;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed, interactionKeyPressed;

    private Camera camera;

    private List<Door> doors;
    private String worldName;
    private WorldManager worldManager;

    public Board(Player player, String worldName) {
        setPreferredSize(new Dimension(TILE_SIZE * COLUMNS, TILE_SIZE * ROWS));
        setBackground(new Color(232, 232, 232));
        this.player = player;
        this.worldName = worldName;
        this.doors = new ArrayList<>();
        objects = new ArrayList<>();
        playerView = new PlayerView(player);
        timer = new Timer(DELAY, this);
        timer.start();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        
        // Initialize the menu singleton with this board's information
        Menu menu = Menu.getInstance();
        menu.setPlayer(player);
        menu.setGameTimer(timer);
        menu.initializeMenuButton(this, TILE_SIZE, COLUMNS, ROWS);

        camera = new Camera(
            TILE_SIZE * COLUMNS, 
            TILE_SIZE * ROWS,
            TILE_SIZE * COLUMNS * 2, // World is twice the screen size
            TILE_SIZE * ROWS * 2
        );
        
        // Center player initially
        player.setPosition(new Point(COLUMNS/2, ROWS/2));

        if (worldName.equals("outside")) {
            addObject("/resources/buildings/red_house.png", 40, 40);
            addObject("/resources/buildings/red_house.png", 140, 40);
            // Add more outside objects...
        } else if (worldName.equals("house_interior")) {
            // Add house interior objects
            // Like furniture, etc.
        }
    }

    public void setWorldManager(WorldManager manager) {
        this.worldManager = manager;
    }
    
    public void addDoor(Door door) {
        doors.add(door);
        objects.add(door); // Add to general objects for rendering
    }
    
    public Player getPlayer() {
        return player;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Create a translated graphics context
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(-camera.getX(), -camera.getY());
        
        drawBackground(g2d);
        
        for (WorldObject obj : objects) {
            obj.draw(g2d, this, TILE_SIZE);
        }
        
        playerView.draw(g2d, this, TILE_SIZE);
        
        drawDebugBounds(g2d);
        
        g2d.dispose();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Clear previous movement
        boolean[] directions = {upPressed, downPressed, leftPressed, rightPressed};
        int activeDirections = 0;
        for(boolean dir : directions) if(dir) activeDirections++;
        
        // Allow only single direction
        if(activeDirections == 1) {
            if (upPressed && canMove(0, -1)) {
                handleMovement(0, -1, Direction.FRONT);
            } else if (downPressed && canMove(0, 1)) {
                handleMovement(0, 1, Direction.BACK);
            } else if (leftPressed && canMove(-1, 0)) {
                handleMovement(-1, 0, Direction.LEFT);
            } else if (rightPressed && canMove(1, 0)) {
                handleMovement(1, 0, Direction.RIGHT);
            }
        }
    }

    private void handleMovement(int dx, int dy, Direction dir) {
        player.setDirection(dir);
        player.move(dx, dy);
        player.setMoving(true);
        player.tick(COLUMNS * 2, ROWS * 2);
        
        playerView.loadImage();
        
        checkDoorInteraction();
        resolveCollisions();
        
        camera.update(player.getWorldX(), player.getWorldY());
        
        repaint();
    }

    public boolean canMove(int dx, int dy) {
        // Calculate the next position in world coordinates
        int nextX = player.getWorldX() + (dx * TILE_SIZE);
        int nextY = player.getWorldY() + (dy * TILE_SIZE);
        
        // Create a rectangle representing where the player would be
        Rectangle nextBounds = new Rectangle(
            nextX,
            nextY,
            player.getWidth(),
            player.getHeight()
        );
        
        for (WorldObject obj : objects) {
            if (nextBounds.intersects(obj.getBounds(TILE_SIZE))) {
                return false; // Collision detected
            }
        }
        return true; // No collision
    }
    
    private void drawBackground(Graphics g) {
        // Calculate visible area
        int startCol = camera.getX() / TILE_SIZE;
        int startRow = camera.getY() / TILE_SIZE;
        int endCol = startCol + COLUMNS + 1;
        int endRow = startRow + ROWS + 1;
        
        // Limit to world bounds
        startCol = Math.max(0, startCol);
        startRow = Math.max(0, startRow);
        endCol = Math.min(COLUMNS * 2, endCol);
        endRow = Math.min(ROWS * 2, endRow);
        
        g.setColor(new Color(214, 214, 214));
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                if ((row + col) % 2 == 1) {
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public void addObject(String path, int x, int y) {
        objects.add(new Building(new Point(x, y), path));
    }

    private void drawDebugBounds(Graphics g) {
        g.setColor(Color.RED);
        // Draw player bounds using actual position and dimensions
        int x = player.getWorldX();
        int y = player.getWorldY();
        g.drawRect(x, y, player.getWidth(), player.getHeight());
        
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
            playerView.loadImage();
        }

        repaint();

        // Handle interaction key (e.g., "E")
        if (key == KeyEvent.VK_E) {
            interactionKeyPressed = true;
            // Handle interaction logic here
        } else {
            interactionKeyPressed = false;
        }
    }

    private void checkDoorInteraction() {
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        for (Door door : doors) {
            if (playerBounds.intersects(door.getBounds(TILE_SIZE))) {
                // Check if player is pressing the interaction key (e.g., UP arrow)
                if (interactionKeyPressed) {
                    if (worldManager != null) {
                        worldManager.switchWorld(door.getTargetWorld(), door.getSpawnPoint());
                    }
                    break;
                }
            }
        }
    }

    // Add this method to the Board class
    private void resolveCollisions() {
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        for (WorldObject obj : objects) {
            Rectangle objBounds = obj.getBounds(TILE_SIZE);
            
            if (playerBounds.intersects(objBounds)) {
                // Calculate overlap
                int overlapLeft = objBounds.x + objBounds.width - playerBounds.x;
                int overlapRight = playerBounds.x + playerBounds.width - objBounds.x;
                int overlapTop = objBounds.y + objBounds.height - playerBounds.y;
                int overlapBottom = playerBounds.y + playerBounds.height - objBounds.y;
                
                // Find minimum overlap
                int minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                        Math.min(overlapTop, overlapBottom));
                
                // Push player in direction of minimum overlap
                if (minOverlap == overlapLeft) {
                    player.setPosition(new Point(player.getPosition().x + (overlapLeft / TILE_SIZE) + 1, 
                                                player.getPosition().y));
                } else if (minOverlap == overlapRight) {
                    player.setPosition(new Point(player.getPosition().x - (overlapRight / TILE_SIZE) - 1, 
                                                player.getPosition().y));
                } else if (minOverlap == overlapTop) {
                    player.setPosition(new Point(player.getPosition().x, 
                                                player.getPosition().y + (overlapTop / TILE_SIZE) + 1));
                } else if (minOverlap == overlapBottom) {
                    player.setPosition(new Point(player.getPosition().x, 
                                                player.getPosition().y - (overlapBottom / TILE_SIZE) - 1));
                }
                
                // Update player's exact coordinates
                player.updateExactCoordinates();
            }
        }
    }
}
