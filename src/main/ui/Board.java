package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import exceptions.NoSuchWorldException;
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
    public int rows;
    public int columns;
    private List<WorldObject> objects;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed, 
            interactionKeyPressed, shiftPressed;

    private Camera camera;

    private List<Door> doors;
    private Point defaultSpawnPoint;
    private String worldName;
    private WorldManager worldManager;

    public Board(Player player, String worldName, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        setPreferredSize(new Dimension(TILE_SIZE * columns, TILE_SIZE * rows));
        setBackground(new Color(232, 232, 232));
        this.player = player;
        this.worldName = worldName;
        defaultSpawnPoint = new Point(columns / 2, rows / 2);
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
        menu.initializeMenuButton(this, TILE_SIZE, columns, rows);

        // Only use camera for large boards
        if (rows >= 120 || columns >= 120) {
            camera = new Camera(
                TILE_SIZE * columns, 
                TILE_SIZE * rows,
                TILE_SIZE * columns * 2, // World is twice the screen size
                TILE_SIZE * rows * 2
            );
        } else {
            // For small boards, create a fixed camera that doesn't move
            camera = new Camera(0, 0, TILE_SIZE * columns, TILE_SIZE * rows);
        }
        
        // Center player initially CHANGE when world is full.
        player.setPosition(new Point(columns/2, rows/2));
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
        
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Only translate for large boards
        if (rows > 50 || columns > 50) {
            g2d.translate(-camera.getX(), -camera.getY());
        }
        
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
        
        // Apply speed multiplier if running
        if (shiftPressed) {
            dx *= player.getMoveSpeed();
            dy *= player.getMoveSpeed();
        }
        
        // Move player
        player.move(dx, dy);
        player.setMoving(true);
        player.setSprintKeyPressed(shiftPressed);
        player.tick(columns * 2, rows * 2);
        
        playerView.loadImage();
        
        resolveCollisions();
        
        camera.update(player.getWorldX(), player.getWorldY());
        
        repaint();
    }

    public boolean canMove(int dx, int dy) {
        // Calculate the next position in world coordinates
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        // Create a rectangle representing where the player would be
        Rectangle nextBounds = new Rectangle(
            playerBounds.x + (dx * TILE_SIZE),
            playerBounds.y + (dy * TILE_SIZE),
            playerBounds.width,
            playerBounds.height
        );
        
        // Check world boundaries - restrict to actual board size
        if (nextBounds.x < 0 || nextBounds.y < 0 || 
        (rows > 50 || columns > 50 ? 
            (nextBounds.x + nextBounds.width > columns * TILE_SIZE * 2 || 
            nextBounds.y + nextBounds.height > rows * TILE_SIZE * 2) :
            (nextBounds.x + nextBounds.width > columns * TILE_SIZE || 
            nextBounds.y + nextBounds.height > rows * TILE_SIZE))) {
        return false;
        }
                
        // Check object collisions
        for (WorldObject obj : objects) {
            if (obj.getClass() == Door.class) {
                continue; // Skip doors for collision detection
            } else if (nextBounds.intersects(obj.getBounds(TILE_SIZE))) {
                return false; // Collision detected
            }
        }
        return true; // No collision
    }
    
    private void drawBackground(Graphics g) {
        // For small boards, draw the entire background without camera offset
        if (rows < 120 && columns < 180) {
            g.setColor(new Color(214, 214, 214));
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    if ((row + col) % 2 == 1) {
                        g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        } else {
            // Original code for large boards with camera
            int startCol = camera.getX() / TILE_SIZE;
            int startRow = camera.getY() / TILE_SIZE;
            int endCol = startCol + columns + 1;
            int endRow = startRow + rows + 1;
            
            // Limit to world bounds (doubled for large worlds)
            startCol = Math.max(0, startCol);
            startRow = Math.max(0, startRow);
            endCol = Math.min(columns * 2, endCol);
            endRow = Math.min(rows * 2, endRow);
            
            g.setColor(new Color(214, 214, 214));
            for (int row = startRow; row < endRow; row++) {
                for (int col = startCol; col < endCol; col++) {
                    if ((row + col) % 2 == 1) {
                        g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
    }    

    public void addObject(String path, int x, int y) {
        objects.add(new Building(new Point(x, y), path));
    }

    private void drawDebugBounds(Graphics g) {
        // RED: player bounds
        g.setColor(Color.RED);
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        g.drawRect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
        
        // BLUE: object bounds
        g.setColor(Color.BLUE);
        for (WorldObject obj : objects) {
            Rectangle bounds = obj.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        // GREEN: door bounds
        g.setColor(Color.GREEN);
        for (Door door : doors) {
            Rectangle bounds = door.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            
            // Draw interaction area
            Rectangle interactionArea = new Rectangle(
                bounds.x - TILE_SIZE, 
                bounds.y - TILE_SIZE,
                bounds.width + TILE_SIZE * 2, 
                bounds.height + TILE_SIZE * 2
            );
            g.drawRect(interactionArea.x, interactionArea.y, 
                    interactionArea.width, interactionArea.height);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
            player.setSprintKeyPressed(true);
            player.setMoving(true);
        }
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
        if (key == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
            player.setSprintKeyPressed(false);
        }
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

        if (key == KeyEvent.VK_E) {
            interactionKeyPressed = true;
            checkDoorInteraction(); 
        } else {
            interactionKeyPressed = false;
        }
    }

    private void checkDoorInteraction() {
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        for (Door door : doors) {
            Rectangle doorBounds = door.getBounds(TILE_SIZE);
            // Create a slightly larger interaction area around the door
            Rectangle interactionArea = new Rectangle(
                doorBounds.x - TILE_SIZE, 
                doorBounds.y - TILE_SIZE,
                doorBounds.width + TILE_SIZE * 2, 
                doorBounds.height + TILE_SIZE * 2
            );
            
            if (playerBounds.intersects(interactionArea)) {
                if (interactionKeyPressed && worldManager != null) {
                    resetKeyStates();
                    try {
                        // Get the door's spawn point for the target world
                        Point spawnPoint = door.getSpawnPoint();
                        worldManager.switchWorld(door.getTargetWorld(), spawnPoint);
                        break;
                    } catch (NoSuchWorldException e) {
                        // Handle exception
                        System.err.println("Could not find world: " + door.getTargetWorld());
                    }
                }
            }
        }
    }

    private void resetKeyStates() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        interactionKeyPressed = false;
        shiftPressed = false;
        
        player.setMoving(false);
        player.setSprintKeyPressed(false);
        playerView.loadImage();
    }

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

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public Camera getCamera() {
        return camera;
    }

    public Point getDefaultSpawnPoint() {
        return defaultSpawnPoint;
    }
    
    public void setDefaultSpawnPoint(Point point) {
        this.defaultSpawnPoint = point;
    }
}
