package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import exceptions.NoSuchWorldException;
import model.Building;
import model.Door;
import model.EncounterManager;
import model.Player;
import model.Player.Direction;
import model.WorldObject;
import pokes.Pokemon;
import tiles.TileManager;

import java.util.List;
import java.util.ArrayList;

public class Board extends JPanel implements ActionListener, KeyListener {
    private final int DELAY = 50;
    public static final int TILE_SIZE = 32;
    public int rows;
    public int columns;
    private List<WorldObject> objects;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed, 
            interactionKeyPressed, shiftPressed;

    private static final int RUN_SPEED = 4; 

    private List<Door> doors;
    private String worldName;
    private WorldManager worldManager;
    private TileManager tileManager;

    private EncounterManager encounterManager;
    private boolean inBattle = false;
    private int encounterCooldown = 0;
    private static final int ENCOUNTER_COOLDOWN_TIME = 3;

    public Board(Player player, String worldName, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        setPreferredSize(new Dimension(TILE_SIZE * columns, TILE_SIZE * rows));
        setBackground(new Color(232, 232, 232));
        this.player = player;
        this.worldName = worldName;
        tileManager = new TileManager(this, worldName);
        this.doors = new ArrayList<>();
        objects = new ArrayList<>();
        playerView = new PlayerView(player);
        timer = new Timer(DELAY, this);
        timer.start();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        
        Menu menu = Menu.getInstance();
        menu.setPlayer(player);
        menu.setGameTimer(timer);
        menu.initializeMenuButton(this, TILE_SIZE, columns, rows);

        player.setPosition(new Point(columns, rows));
        Camera camera = Camera.getInstance();
        if (isLarge()) {
            camera.update(player);
        }

        this.encounterManager = new EncounterManager();
        // Add this to the Board constructor
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Board has focus now
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                resetKeyStates();
            }
        });

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
        
        Camera camera = worldManager.getCamera();
        
        // Apply camera translation if active
        if (camera != null && camera.isActive()) {
            g2d.translate(-camera.getX(), -camera.getY());
        }
        
        // Draw tiles first
        tileManager.draw(g2d);
        
        // Then draw objects
        for (WorldObject obj : objects) {
            obj.draw(g2d, this, TILE_SIZE);
        }
        
        // Draw player
        playerView.draw(g2d, this, TILE_SIZE);
        
        // Debug bounds if needed
        drawDebugBounds(g2d);
        
        g2d.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.updateAnimation();

        if (worldManager != null) {
            worldManager.getCamera().update(player);
        }
        
        // Decrease encounter cooldown if active
        if (encounterCooldown > 0) {
            encounterCooldown--;
        }
        
        // Check for wild Pokémon encounters if not in battle and cooldown is over
        if (!player.isInBattle() && !inBattle && encounterCooldown == 0) {
            boolean isInGrass = tileManager.isPlayerInTallGrass(player);
            boolean isMoving = player.isMoving();
            
            if (encounterManager.checkEncounter(isInGrass, isMoving)) {
                startWildEncounter();
            }
        }
        
        // Clear previous movement
        boolean[] directions = {upPressed, downPressed, leftPressed, rightPressed};
        int activeDirections = 0;
        for(boolean dir : directions) if(dir) activeDirections++;
        
        // Allow only single direction
        if(activeDirections == 1) {
            int moveSpeed = 4;
            if (shiftPressed) {
                moveSpeed = moveSpeed * Math.round(player.getMoveSpeed());
            }
            
            if (upPressed && canMove(0, -moveSpeed)) {
                handleMovement(0, -1, Direction.FRONT);
            } else if (downPressed && canMove(0, moveSpeed)) {
                handleMovement(0, 1, Direction.BACK);
            } else if (leftPressed && canMove(-moveSpeed, 0)) {
                handleMovement(-1, 0, Direction.LEFT);
            } else if (rightPressed && canMove(moveSpeed, 0)) {
                handleMovement(1, 0, Direction.RIGHT);
            } else {
                // If we can't move in the desired direction, stop the player
                player.setMoving(false);
                playerView.loadImage();
            }
        }

        if (worldManager != null) {
            Camera camera = worldManager.getCamera();
            camera.update(player);
        }

        tileManager.update();
        
        repaint();
    }

    private void handleMovement(int dx, int dy, Direction dir) {
        player.setDirection(dir);
        
        int moveSpeed = RUN_SPEED;
        if (shiftPressed) {
            moveSpeed = moveSpeed * Math.round(player.getMoveSpeed());
        }
    
        player.setDirection(dir);
    
        // Check for tile collisions
        if (checkTileCollision(dx, dy)) {
            // If there's a collision, don't move the player
            player.setMoving(false);
        } else {
            // Move player if no collision
            player.move(dx * moveSpeed, dy * moveSpeed);
            player.setMoving(true);
            player.setSprintKeyPressed(shiftPressed);
        }
        
        playerView.loadImage();
        
        // Update camera with pixel coordinates
        Camera camera = worldManager.getCamera();
        camera.update(player);
        
        repaint();
    }

    private boolean checkTileCollision(int dx, int dy) {
        // Get player's current bounds
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        // Calculate the position after movement
        int moveAmount = Math.round(player.getMoveSpeed());
        Rectangle nextBounds = new Rectangle(
            playerBounds.x + dx * moveAmount,
            playerBounds.y + dy * moveAmount,
            playerBounds.width,
            playerBounds.height
        );
        
        return checkTileBoundsCollision(nextBounds);
    }

    public boolean canMove(int dx, int dy) {
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        Rectangle nextBounds = new Rectangle(
            playerBounds.x + dx,
            playerBounds.y + dy,
            playerBounds.width,
            playerBounds.height
        );

        Rectangle fullPixelRectangle = player.getFullBounds(TILE_SIZE);
        Rectangle nextFullPixelRectangle = new Rectangle(
            fullPixelRectangle.x + dx,
            fullPixelRectangle.y + dy,
            fullPixelRectangle.width,
            fullPixelRectangle.height
        );
        
        // Check world boundaries
        if (nextFullPixelRectangle.x < 0 || nextFullPixelRectangle.y < 0 || 
            nextFullPixelRectangle.x + nextFullPixelRectangle.width > columns * TILE_SIZE || 
            nextFullPixelRectangle.y + nextFullPixelRectangle.height > rows * TILE_SIZE) {
            return false;
        }
        
        // Check object collisions (keep this part)
        for (WorldObject obj : objects) {
            if (obj.getClass() == Door.class) {
                continue; // Skip doors for collision detection
            } else if (nextBounds.intersects(obj.getBounds(TILE_SIZE))) {
                return false; // Collision detected
            }
        }
        
        // Check tile collisions using the bounds
        return !checkTileBoundsCollision(nextBounds);
    } 
    
    private boolean checkTileBoundsCollision(Rectangle bounds) {
        // Convert pixel coordinates to tile coordinates
        int startTileX = bounds.x / TILE_SIZE;
        int startTileY = bounds.y / TILE_SIZE;
        int endTileX = (bounds.x + bounds.width - 1) / TILE_SIZE;
        int endTileY = (bounds.y + bounds.height - 1) / TILE_SIZE;
        
        // Check all tiles that the bounds intersect with
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                if (tileManager.isTileCollision(tileX, tileY)) {
                    return true; // Collision found
                }
            }
        }
        return false; // No collision
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

        g.setColor(new Color(0, 255, 0, 100)); // Semi-transparent green
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (tileManager.isInTallGrass(x, y)) {
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        if (tileManager.isPlayerInTallGrass(player)) {
            g.setColor(Color.YELLOW);
            g.drawString("IN GRASS", playerBounds.x, playerBounds.y - 10);
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
    
    public void resetKeyStates() {
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

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isLarge() {
        return rows >= 20 || columns >= 30;
    }

    private void startWildEncounter() {
        // Set flags to prevent movement during encounter
        inBattle = true;
        
        // Reset all movement states
        resetKeyStates();
        
        // Stop the player's movement
        player.setMoving(false);
        player.stopMoving();
        
        // Pause the game timer to prevent any movement updates
        timer.stop();
        
        // Generate a wild Pokémon based on current location
        Pokemon wildPokemon = encounterManager.generateWildPokemon(worldName);
        
        // Play encounter animation
        playEncounterAnimation(wildPokemon);
        
        // Start battle
        player.setInBattle(true);
        
        // Create and show battle screen
        SwingUtilities.invokeLater(() -> {
            BattleScreen battleScreen = new BattleScreen(player, wildPokemon, true, "route");
            battleScreen.setVisible(true);
            
            // When battle ends
            battleScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    endWildEncounter();
                }
            });
        });
    }
    
    // Add method to end wild encounter
    private void endWildEncounter() {
        inBattle = false;
        player.setInBattle(false);
        encounterCooldown = ENCOUNTER_COOLDOWN_TIME;
        timer.start();
        
        // Request focus back to the board
        requestFocusInWindow();
    }
    
    // Add method for encounter animation
    private void playEncounterAnimation(Pokemon wildPokemon) {
        // This would be where you'd implement a screen flash or transition animation
        // For now, just print to console
        System.out.println("A wild " + wildPokemon.getName() + " appeared!");
        
        // You could add a visual effect here like screen flashing
        // For example:
        /*
        JPanel flashPanel = new JPanel();
        flashPanel.setBackground(Color.BLACK);
        flashPanel.setBounds(0, 0, getWidth(), getHeight());
        add(flashPanel, JLayeredPane.POPUP_LAYER);
        
        Timer flashTimer = new Timer(100, new ActionListener() {
            int flashes = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                flashes++;
                flashPanel.setVisible(!flashPanel.isVisible());
                
                if (flashes >= 6) {
                    remove(flashPanel);
                    revalidate();
                    repaint();
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        flashTimer.start();
        */
    }
    
    // Add this method to Player class if it doesn't exist
    public void stopMoving() {
        // Reset any movement-related state
        player.setMoving(false);
        player.setSprintKeyPressed(false);
    }
    
    // Add this method to Player class if it doesn't exist
    public boolean isInBattle() {
        return inBattle; // You'll need to add this field to Player
    }
    
    // Add this method to Player class if it doesn't exist
    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }
}
