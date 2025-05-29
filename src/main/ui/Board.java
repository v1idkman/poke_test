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
import model.Npc;

import java.util.List;
import java.util.ArrayList;

public class Board extends JPanel implements ActionListener, KeyListener {
    private final int DELAY = 50;
    public static final int TILE_SIZE = 32;
    public int rows;
    public int columns;
    private String worldName;

    private Timer timer;
    private Player player;
    private PlayerView playerView;

    private boolean upPressed, downPressed, leftPressed, rightPressed, 
            interactionKeyPressed, shiftPressed;

    private static final int RUN_SPEED = 4; 

    private List<Door> doors;
    private List<WorldObject> objects;
    private List<Npc> npcs = new ArrayList<>();
    private boolean npcBattleInProgress = false;
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
        
        // Set rendering hints for pixel-perfect rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        
        Camera camera = worldManager.getCamera();
        
        // Apply camera translation if active - ensure integer coordinates
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

        drawDebugBounds(g2d);
        
        g2d.dispose();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        player.updateAnimation();

        if (worldManager != null) {
            worldManager.getCamera().update(player);
        }

        checkNPCEncounters();
        
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
    
        // GREEN: door bounds and interaction areas
        g.setColor(Color.GREEN);
        for (Door door : doors) {
            Rectangle bounds = door.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            
            // Draw door interaction area
            Rectangle interactionArea = new Rectangle(
                bounds.x - TILE_SIZE, 
                bounds.y - TILE_SIZE,
                bounds.width + TILE_SIZE * 2, 
                bounds.height + TILE_SIZE * 2
            );
            g.drawRect(interactionArea.x, interactionArea.y, 
                    interactionArea.width, interactionArea.height);
        }
    
        // ORANGE: NPC bounds
        g.setColor(Color.ORANGE);
        for (Npc npc : npcs) {
            Rectangle npcBounds = npc.getBounds(TILE_SIZE);
            g.drawRect(npcBounds.x, npcBounds.y, npcBounds.width, npcBounds.height);
        }
    
        // MAGENTA: NPC interaction areas (for dialogue/general interaction)
        g.setColor(Color.MAGENTA);
        for (Npc npc : npcs) {
            Rectangle npcBounds = npc.getBounds(TILE_SIZE);
            Rectangle npcInteractionArea = new Rectangle(
                npcBounds.x - TILE_SIZE, 
                npcBounds.y - TILE_SIZE,
                npcBounds.width + TILE_SIZE * 2, 
                npcBounds.height + TILE_SIZE * 2
            );
            g.drawRect(npcInteractionArea.x, npcInteractionArea.y, 
                    npcInteractionArea.width, npcInteractionArea.height);
        }
    
        // CYAN: NPC battle initiation areas (sight range)
        g.setColor(Color.CYAN);
        for (Npc npc : npcs) {
            if (!npc.isDefeated()) { // Only show for undefeated NPCs
                Rectangle npcBounds = npc.getBounds(TILE_SIZE);
                
                int sightRange = npc.getSightRange() * TILE_SIZE;
                
                // Draw battle initiation area based on NPC's facing direction
                Rectangle battleArea = getBattleInitiationArea(npc, npcBounds, sightRange);
                
                // Draw with semi-transparent fill to show the area clearly
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 255, 255, 50)); // Semi-transparent cyan
                g2d.fillRect(battleArea.x, battleArea.y, battleArea.width, battleArea.height);
                g2d.setColor(Color.CYAN);
                g2d.drawRect(battleArea.x, battleArea.y, battleArea.width, battleArea.height);
                g2d.dispose();
            }
        }
    
        // YELLOW: Tall grass areas (existing code)
        g.setColor(new Color(0, 255, 0, 100)); // Semi-transparent green
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (tileManager.isInTallGrass(x, y)) {
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    
        // Player in grass indicator (existing code)
        if (tileManager.isPlayerInTallGrass(player)) {
            g.setColor(Color.YELLOW);
            g.drawString("IN GRASS", playerBounds.x, playerBounds.y - 10);
        }
    
        // Add legend for debug colors
        drawDebugLegend(g);
    }
    
    private Rectangle getBattleInitiationArea(Npc npc, Rectangle npcBounds, int sightRange) {
        Npc.Direction npcDirection = npc.getDirection();
        
        // Use the actual sight range from the NPC
        int actualSightRange = npc.getSightRange() * TILE_SIZE;
        
        Rectangle battleArea;
        
        switch (npcDirection) {
            case FRONT: // Looking down
                battleArea = new Rectangle(
                    npcBounds.x - TILE_SIZE, // Allow ±1 tile width (Math.abs(deltaX) <= 1)
                    npcBounds.y + npcBounds.height,
                    npcBounds.width + (2 * TILE_SIZE), // 3 tiles wide total
                    actualSightRange
                );
                break;
            case BACK: // Looking up
                battleArea = new Rectangle(
                    npcBounds.x - TILE_SIZE,
                    npcBounds.y - actualSightRange,
                    npcBounds.width + (2 * TILE_SIZE),
                    actualSightRange
                );
                break;
            case LEFT: // Looking left
                battleArea = new Rectangle(
                    npcBounds.x - actualSightRange,
                    npcBounds.y - TILE_SIZE, // Allow ±1 tile height
                    actualSightRange,
                    npcBounds.height + (2 * TILE_SIZE) // 3 tiles tall total
                );
                break;
            case RIGHT: // Looking right
                battleArea = new Rectangle(
                    npcBounds.x + npcBounds.width,
                    npcBounds.y - TILE_SIZE,
                    actualSightRange,
                    npcBounds.height + (2 * TILE_SIZE)
                );
                break;
            default:
                battleArea = new Rectangle(
                    npcBounds.x - actualSightRange,
                    npcBounds.y - actualSightRange,
                    npcBounds.width + actualSightRange * 2,
                    npcBounds.height + actualSightRange * 2
                );
                break;
        }
        
        return battleArea;
    }
    
    private void drawDebugLegend(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(10, 10, 200, 160);
        g.setColor(Color.WHITE);
        g.drawRect(10, 10, 200, 160);
        
        int y = 25;
        g.setColor(Color.RED);
        g.drawString("RED: Player Bounds", 15, y);
        y += 15;
        g.setColor(Color.BLUE);
        g.drawString("BLUE: Object Bounds", 15, y);
        y += 15;
        g.setColor(Color.GREEN);
        g.drawString("GREEN: Door Areas", 15, y);
        y += 15;
        g.setColor(Color.ORANGE);
        g.drawString("ORANGE: NPC Bounds", 15, y);
        y += 15;
        g.setColor(Color.MAGENTA);
        g.drawString("MAGENTA: NPC Interaction", 15, y);
        y += 15;
        g.setColor(Color.CYAN);
        g.drawString("CYAN: NPC Battle Range", 15, y);
        y += 15;
        g.setColor(Color.GREEN);
        g.drawString("GREEN: Tall Grass", 15, y);
        y += 15;
        g.setColor(Color.YELLOW);
        g.drawString("YELLOW: In Grass Indicator", 15, y);
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
    
    // Add method for encounter animation
    private void playEncounterAnimation(Pokemon wildPokemon) {
        // This would be where you'd implement a screen flash or transition animation
        // For now, just print to console
        System.out.println("A wild " + wildPokemon.getName() + " appeared!");
    }
    
    public void stopMoving() {
        // Reset any movement-related state
        player.setMoving(false);
        player.setSprintKeyPressed(false);
    }
    
    public boolean isInBattle() {
        return inBattle; // You'll need to add this field to Player
    }
    
    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public void placeManyObjects(String path, int startTileX, int startTileY, int endTileX, int endTileY, int xSpacing, int ySpacing) {
        // Ensure start coordinates are less than end coordinates
        if (startTileX > endTileX) {
            int temp = startTileX;
            startTileX = endTileX;
            endTileX = temp;
        }
        
        if (startTileY > endTileY) {
            int temp = startTileY;
            startTileY = endTileY;
            endTileY = temp;
        }
        
        // Place objects in a rectangular grid
        for (int y = startTileY; y <= endTileY; y = y + ySpacing) {
            for (int x = startTileX; x <= endTileX; x = x + xSpacing) {
                // Check bounds to prevent placing objects outside the world
                if (x >= 0 && x < columns && y >= 0 && y < rows) {
                    addObject(path, x, y);
                }
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    // Add these methods to your Board class
    public void addNPC(Npc npc) {
        npcs.add(npc);
        objects.add(npc); // Add to general objects for rendering
    }

    private void checkNPCEncounters() {
        if (npcBattleInProgress || inBattle) return;
        
        for (Npc npc : npcs) {
            if (npc.canSeePlayer(player, TILE_SIZE)) {
                initiateNPCBattle(npc);
                break;
            }
        }
    }

    private void initiateNPCBattle(Npc npc) {
        npcBattleInProgress = true;
        
        // Stop player movement
        resetKeyStates();
        player.setMoving(false);
        player.stopMoving();
        timer.stop();
        
        // Show dialogue first
        showNPCDialogue(npc, () -> {
            // Start battle after dialogue
            startTrainerBattle(npc);
        });
    }

    private void showNPCDialogue(Npc npc, Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            NpcDialogueScreen dialogue = new NpcDialogueScreen(npc, this);
            dialogue.setVisible(true);
            
            dialogue.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    onComplete.run();
                }
            });
        });
    }

    private void startWildEncounter() {
        inBattle = true;
        resetKeyStates();
        player.setMoving(false);
        player.stopMoving();
        timer.stop();
        
        Pokemon wildPokemon = encounterManager.generateWildPokemon(worldName);
        playEncounterAnimation(wildPokemon);
        player.setInBattle(true);
        
        SwingUtilities.invokeLater(() -> {
            WildPokemonBattle battleScreen = new WildPokemonBattle(player, wildPokemon, "route");
            battleScreen.setVisible(true);
            
            battleScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    endWildEncounter();
                }
            });
        });
    }

    private void endWildEncounter() {
        inBattle = false;
        player.setInBattle(false);
        encounterCooldown = ENCOUNTER_COOLDOWN_TIME;
        timer.start();
        
        // Request focus back to the board
        requestFocusInWindow();
    }
    
    // Update the startTrainerBattle method:
    private void startTrainerBattle(Npc npc) {
        SwingUtilities.invokeLater(() -> {
            TrainerBattle battleScreen = new TrainerBattle(player, npc, "route");
            battleScreen.setVisible(true);
            
            battleScreen.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    endNPCBattle();
                }
            });
        });
    }

    private void endNPCBattle() {
        npcBattleInProgress = false;
        timer.start();
        requestFocusInWindow();
    }
}
