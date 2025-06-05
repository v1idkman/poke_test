package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import model.Berry;
import model.BerryTree;
import model.Building;
import model.CivilianNpc;
import model.Door;
import model.EncounterManager;
import model.InteractableItem;
import model.InteractableObject;
import model.Player;
import model.Player.Direction;
import model.Player.MovementState;
import model.TrainerNpc;
import model.WorldObject;
import pokes.Pokemon;
import tiles.TileManager;
import model.Npc;

import java.util.List;
import java.util.Random;
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
    private List<InteractableObject> interactableObjects = new ArrayList<>();
    private List<TrainerNpc> trainers = new ArrayList<>();
    private List<CivilianNpc> civilians = new ArrayList<>();
    private List<Npc> allNpcs = new ArrayList<>();
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
    
    // Update the addDoor method
    public void addDoor(Door door) {
        doors.add(door);
        objects.add(door);
        interactableObjects.add(door);
    }

    // Add method for adding any interactable object
    public void addInteractableObject(InteractableObject obj) {
        interactableObjects.add(obj);
        objects.add(obj);
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

        updateBerryTrees();
        
        // Draw objects
        for (WorldObject obj : objects) {
            obj.draw(g2d, this, TILE_SIZE);
        }

        playerView.draw(g2d, this, TILE_SIZE);

        for (TrainerNpc trainer : trainers) {
            trainer.drawIcon(g2d);
        }

        drawDebugBounds(g2d);
        
        g2d.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.updateAnimation();

        if (worldManager != null) {
            worldManager.getCamera().update(player);
        }

        updateNpcs();

        if (approachingTrainer != null && approachingTrainer.isApproachingForBattle()) {
            // Force player to stay still
            player.setMoving(false);
            resetKeyStates();
            repaint();
            return;
        }

        checkNPCEncounters();
        
        // Decrease encounter cooldown if active
        if (encounterCooldown > 0) {
            encounterCooldown--;
        }
        
        // In Board.actionPerformed(), add this debug code:
        if (!player.isInBattle() && !inBattle) {
            boolean isInGrass = tileManager.isPlayerInTallGrass(player);
            boolean isMoving = player.isMoving();
            
            if (encounterCooldown == 0 && encounterManager.checkEncounter(isInGrass, isMoving)) {
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
        
        // Check object collisions
        for (WorldObject obj : objects) {
            if (obj.getClass() == Door.class) {
                continue; // Skip doors for collision detection
            } else if (obj.isWalkable()) {
                continue;
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
    
        // GREEN: door bounds and interaction areas with direction indicators
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
            
            // Draw direction indicator
            g.setColor(Color.YELLOW);
            int centerX = bounds.x + bounds.width / 2;
            int centerY = bounds.y + bounds.height / 2;
            
            switch (door.getDirection()) {
                case FRONT:
                    // Arrow pointing up
                    g.drawLine(centerX, centerY, centerX, centerY - 10);
                    g.drawLine(centerX, centerY - 10, centerX - 3, centerY - 7);
                    g.drawLine(centerX, centerY - 10, centerX + 3, centerY - 7);
                    break;
                case BACK:
                    // Arrow pointing down
                    g.drawLine(centerX, centerY, centerX, centerY + 10);
                    g.drawLine(centerX, centerY + 10, centerX - 3, centerY + 7);
                    g.drawLine(centerX, centerY + 10, centerX + 3, centerY + 7);
                    break;
                case LEFT:
                    // Arrow pointing left
                    g.drawLine(centerX, centerY, centerX - 10, centerY);
                    g.drawLine(centerX - 10, centerY, centerX - 7, centerY - 3);
                    g.drawLine(centerX - 10, centerY, centerX - 7, centerY + 3);
                    break;
                case RIGHT:
                    // Arrow pointing right
                    g.drawLine(centerX, centerY, centerX + 10, centerY);
                    g.drawLine(centerX + 10, centerY, centerX + 7, centerY - 3);
                    g.drawLine(centerX + 10, centerY, centerX + 7, centerY + 3);
                    break;
                case ANY:
                    // Arrow pointing in all directions
                    g.drawLine(centerX, centerY, centerX - 10, centerY);
                    g.drawLine(centerX, centerY, centerX + 10, centerY);
                    g.drawLine(centerX, centerY, centerX, centerY - 10);
                    g.drawLine(centerX, centerY, centerX, centerY + 10);
                    break;
            }
            g.setColor(Color.GREEN); // Reset color
        }
    
        // ORANGE: Trainer NPC bounds
        g.setColor(Color.ORANGE);
        for (TrainerNpc trainer : trainers) {
            Rectangle bounds = trainer.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        
        // PINK: Civilian NPC bounds
        g.setColor(Color.PINK);
        for (CivilianNpc civilian : civilians) {
            Rectangle bounds = civilian.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        
        // CYAN: Battle initiation rectangles for trainers
        g.setColor(Color.CYAN);
        for (TrainerNpc trainer : trainers) {
            if (!trainer.isDefeated()) {
                Rectangle npcBounds = trainer.getBounds(TILE_SIZE);
                Rectangle battleArea = getBattleInitiationArea(trainer, npcBounds, 0);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 255, 255, 50));
                g2d.fillRect(battleArea.x, battleArea.y, battleArea.width, battleArea.height);
                g2d.setColor(Color.CYAN);
                g2d.drawRect(battleArea.x, battleArea.y, battleArea.width, battleArea.height);
                g2d.dispose();
            }
        }

        // PURPLE: All interactable objects
        g.setColor(Color.MAGENTA);
        for (InteractableObject obj : interactableObjects) {
            Rectangle bounds = obj.getBounds(TILE_SIZE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            
            // Draw interaction area
            Rectangle interactionArea = obj.getInteractionArea(TILE_SIZE);
            g.setColor(new Color(255, 0, 255, 50)); // Semi-transparent magenta
            g.fillRect(interactionArea.x, interactionArea.y, 
                    interactionArea.width, interactionArea.height);
            g.setColor(Color.MAGENTA);
            g.drawRect(interactionArea.x, interactionArea.y, 
                    interactionArea.width, interactionArea.height);
            
            // Draw direction indicator
            drawDirectionIndicator(g, obj, bounds);
        }
        
        // YELLOW: Midpoint lines for battle rectangles
        g.setColor(Color.YELLOW);
        for (TrainerNpc trainer : trainers) {
            if (!trainer.isDefeated()) {
                Rectangle battleArea = getBattleInitiationArea(trainer, trainer.getBounds(TILE_SIZE), 0);
                
                switch (trainer.getDirection()) {
                    case FRONT:
                    case BACK:
                        // Vertical rectangle - draw VERTICAL midpoint line
                        int midX = battleArea.x + (battleArea.width / 2);
                        g.drawLine(midX, battleArea.y, midX, battleArea.y + battleArea.height);
                        break;
                        
                    case LEFT:
                    case RIGHT:
                        // Horizontal rectangle - draw HORIZONTAL midpoint line
                        int midY = battleArea.y + (battleArea.height / 2);
                        g.drawLine(battleArea.x, midY, battleArea.x + battleArea.width, midY);
                        break;
                }
            }
        }
        
        // RED DOT: Player center
        int playerCenterX = player.getWorldX() + (TILE_SIZE / 2);
        int playerCenterY = player.getWorldY() + (TILE_SIZE / 2);
        g.setColor(Color.RED);
        g.fillOval(playerCenterX - 3, playerCenterY - 3, 6, 6);
        
        // LIGHT GREEN: Tall grass areas
        g.setColor(new Color(0, 255, 0, 100)); // Semi-transparent green
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (tileManager.isInTallGrass(x, y)) {
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    
        // Player in grass indicator
        if (tileManager.isPlayerInTallGrass(player)) {
            g.setColor(Color.YELLOW);
            g.drawString("IN GRASS", playerBounds.x, playerBounds.y - 10);
        }
    
        // Add legend for debug colors
        // drawDebugLegend(g);

    }
    
    private Rectangle getBattleInitiationArea(TrainerNpc npc, Rectangle npcBounds, int sightRange) {
        // Use player dimensions for consistent rectangle size
        int playerWidth = player.getHeight(); 
        int playerHeight = player.getWidth();
        
        Rectangle battleArea;
        
        switch (npc.getDirection()) {
            case FRONT: // Looking up - vertical rectangle
                int frontVisionHeight = 5 * playerWidth; 
                battleArea = new Rectangle(
                    npcBounds.x + (npcBounds.width / 2) - (playerWidth / 2), 
                    npcBounds.y - frontVisionHeight, 
                    playerWidth, 
                    frontVisionHeight 
                );
                break;
                
            case BACK: // Looking down - vertical rectangle
                int backVisionHeight = 5 * playerWidth; 
                battleArea = new Rectangle(
                    npcBounds.x + (npcBounds.width / 2) - (playerWidth / 2), 
                    npcBounds.y + npcBounds.height, 
                    playerWidth,
                    backVisionHeight 
                );
                break;
                
            case LEFT: // Looking left - horizontal rectangle
                int leftVisionWidth = 5 * playerHeight; 
                battleArea = new Rectangle(
                    npcBounds.x - leftVisionWidth, 
                    npcBounds.y + (npcBounds.height / 2) - (playerHeight / 2), 
                    leftVisionWidth, 
                    playerHeight 
                );
                break;
                
            case RIGHT: // Looking right - horizontal rectangle
                int rightVisionWidth = 5 * playerHeight;
                battleArea = new Rectangle(
                    npcBounds.x + npcBounds.width,
                    npcBounds.y + (npcBounds.height / 2) - (playerHeight / 2),
                    rightVisionWidth, 
                    playerHeight
                );
                break;
                
            default:
                battleArea = new Rectangle(
                    npcBounds.x - TILE_SIZE,
                    npcBounds.y - TILE_SIZE,
                    TILE_SIZE * 2,
                    TILE_SIZE * 2
                );
                break;
        }
        
        return battleArea;
    }
    
    /* private void drawDebugLegend(Graphics g) {
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
        y += 15;
        g.setColor(Color.MAGENTA);
        g.drawString("MAGENTA: Interactable Objects", 15, y);
    } */
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (approachingTrainer != null && player.getMoving() == false) {
            return;
        }

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
            player.setSprintKeyPressed(true);
            player.setMoving(true);
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            upPressed = true;
            player.setDirection(Player.Direction.FRONT);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            rightPressed = true;
            player.setDirection(Player.Direction.RIGHT);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            downPressed = true;
            player.setDirection(Player.Direction.BACK);
            player.setMoving(true);
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            leftPressed = true;
            player.setDirection(Player.Direction.LEFT);
            player.setMoving(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (approachingTrainer != null && player.getMoving() == false) {
            return;
        }
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
            checkInteractableObjectInteraction();
        } else {
            interactionKeyPressed = false;
        }
    }

        // Replace the existing checkDoorInteraction method with a generic one
    private void checkInteractableObjectInteraction() {
        Rectangle playerBounds = player.getBounds(TILE_SIZE);
        
        // Create a list to track objects to remove
        List<InteractableObject> objectsToRemove = new ArrayList<>();
        
        for (InteractableObject obj : interactableObjects) {
            Rectangle interactionArea = obj.getInteractionArea(TILE_SIZE);
            
            if (playerBounds.intersects(interactionArea)) {
                if (interactionKeyPressed) {
                    // Check if player is facing the correct direction for interaction
                    if (obj.canPlayerInteract(player.getDirection())) {
                        resetKeyStates();
                        
                        // Stop player movement before interaction
                        player.setMoving(false);
                        player.stopMoving();
                        
                        // Perform the object's action
                        obj.performAction(player, this);
                        
                        // Check if object should be removed after interaction
                        if (obj.shouldRemoveAfterInteraction()) {
                            objectsToRemove.add(obj);
                        }
                        
                        break; // Only interact with one object at a time
                    } else {
                        System.out.println("You need to face the object to interact with it!");
                    }
                }
            }
        }
        
        // Remove objects that should be removed after interaction
        for (InteractableObject obj : objectsToRemove) {
            interactableObjects.remove(obj);
            objects.remove(obj);
            if (obj instanceof Door) {
                doors.remove(obj);
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
        System.out.println("Moves: " + wildPokemon.getMoves().toString());
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

    public void addTrainer(TrainerNpc trainer) {
        trainers.add(trainer);
        allNpcs.add(trainer);
        objects.add(trainer);
    }
    
    public void addCivilian(CivilianNpc civilian) {
        civilians.add(civilian);
        allNpcs.add(civilian);
        objects.add(civilian);
    }
    
    private void initiateNPCBattle(TrainerNpc npc) {
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
        player.setMovementState(MovementState.IN_BATTLE);
        
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
        player.setMovementState(MovementState.FREE);
        encounterCooldown = ENCOUNTER_COOLDOWN_TIME;
        timer.start();
        
        // Request focus back to the board
        requestFocusInWindow();
    }
    
    // Update the startTrainerBattle method:
    private void startTrainerBattle(TrainerNpc npc) {
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

    private TrainerNpc approachingTrainer = null;
    
    private void checkNPCEncounters() {
        if (npcBattleInProgress || inBattle || approachingTrainer != null) return;
        
        for (TrainerNpc trainer : trainers) {
            if (trainer.canSeePlayer(player, TILE_SIZE) && trainer.canInitiateBattle() 
                && !trainer.isApproachingForBattle()) {
                
                // STOP PLAYER MOVEMENT IMMEDIATELY
                resetKeyStates();
                player.setMoving(false);
                player.stopMoving();
                
                // Start the approach sequence
                approachingTrainer = trainer;
                trainer.startApproachingPlayer(player, TILE_SIZE);
                
                System.out.println("Player movement stopped! " + trainer.getName() + " is approaching!");
                break;
            }
        }
    }

    private void drawDirectionIndicator(Graphics g, InteractableObject obj, Rectangle bounds) {
        g.setColor(Color.YELLOW);
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;
        
        switch (obj.getDirection()) {
            case FRONT:
                // Arrow pointing up
                g.drawLine(centerX, centerY, centerX, centerY - 10);
                g.drawLine(centerX, centerY - 10, centerX - 3, centerY - 7);
                g.drawLine(centerX, centerY - 10, centerX + 3, centerY - 7);
                break;
            case BACK:
                // Arrow pointing down
                g.drawLine(centerX, centerY, centerX, centerY + 10);
                g.drawLine(centerX, centerY + 10, centerX - 3, centerY + 7);
                g.drawLine(centerX, centerY + 10, centerX + 3, centerY + 7);
                break;
            case LEFT:
                // Arrow pointing left
                g.drawLine(centerX, centerY, centerX - 10, centerY);
                g.drawLine(centerX - 10, centerY, centerX - 7, centerY - 3);
                g.drawLine(centerX - 10, centerY, centerX - 7, centerY + 3);
                break;
            case RIGHT:
                // Arrow pointing right
                g.drawLine(centerX, centerY, centerX + 10, centerY);
                g.drawLine(centerX + 10, centerY, centerX + 7, centerY - 3);
                g.drawLine(centerX + 10, centerY, centerX + 7, centerY + 3);
                break;
            case ANY:
                // Circle to indicate any direction
                g.drawOval(centerX - 5, centerY - 5, 10, 10);
                break;
        }
    }
    
    public void onTrainerApproachComplete(TrainerNpc trainer) {
        // Called when trainer finishes approaching the player
        approachingTrainer = null;
        trainer.completeApproach();
        
        // Now initiate the dialogue and battle
        initiateNPCBattle(trainer);
    }
    
    public void updateNpcs() {
        for (TrainerNpc trainer : trainers) {
            if (trainer.canMove()) {
                trainer.updateMovement(TILE_SIZE);
            }
            trainer.updateAnimation();
            trainer.updateIcon();
        }
    }

	public WorldManager getWorldManager() {
		return worldManager;
	}

    // Generic method for adding any interactable item
    public void addInteractableItem(String itemName, int quantity, int x, int y, InteractableObject.Direction direction) {
        InteractableItem item = new InteractableItem(new Point(x, y), itemName, quantity, direction);
        addInteractableObject(item);
    }

    // Convenience method for pokeballs - REMOVE spritePath parameter
    public void addPokeball(String pokeballType, int quantity, int x, int y) {
        InteractableItem pokeball = new InteractableItem(new Point(x, y), pokeballType, quantity);
        pokeball.setInteractionMessage("Found " + quantity + " " + pokeballType + "(s)!");
        addInteractableObject(pokeball);
    }

    // Convenience method for medicine items - REMOVE spritePath parameter
    public void addMedicine(String medicineType, int quantity, int x, int y) {
        InteractableItem medicine = new InteractableItem(new Point(x, y), medicineType, quantity);
        addInteractableObject(medicine);
    }

    // Convenience method for key items - REMOVE spritePath parameter
    public void addKeyItem(String keyItemType, int x, int y) {
        InteractableItem keyItem = new InteractableItem(new Point(x, y), keyItemType, 1);
        keyItem.setInteractionMessage("Found " + keyItemType + "!");
        addInteractableObject(keyItem);
    }

    // Remove the old addCustomItem method with spritePath parameter and keep only this one:
    public void addCustomItem(String itemName, int quantity, int x, int y, String customMessage) {
        InteractableItem item = new InteractableItem(new Point(x, y), itemName, quantity);
        if (customMessage != null) {
            item.setInteractionMessage(customMessage);
        }
        addInteractableObject(item);
    }

    public void addBerryTree(Berry.BerryType berryType, int maxBerries, int x, int y) {
        BerryTree berryTree = new BerryTree(new Point(x, y), berryType, maxBerries);
        addInteractableObject(berryTree);
        System.out.println("Added " + berryType.getName() + " tree at (" + x + ", " + y + ")");
    }

    // Convenience method for adding multiple berry trees
    public void addBerryTreesInArea(Berry.BerryType berryType, int maxBerries, 
                                int startX, int startY, int endX, int endY, 
                                int spacing, int density) {
        Random random = new Random();
        
        for (int y = startY; y <= endY; y += spacing) {
            for (int x = startX; x <= endX; x += spacing) {
                // Add some randomness to tree placement
                if (random.nextInt(100) < density) { // density is percentage chance
                    // Add small random offset to make placement more natural
                    int offsetX = random.nextInt(spacing / 2) - spacing / 4;
                    int offsetY = random.nextInt(spacing / 2) - spacing / 4;
                    
                    int finalX = Math.max(0, Math.min(columns - 1, x + offsetX));
                    int finalY = Math.max(0, Math.min(rows - 1, y + offsetY));
                    
                    addBerryTree(berryType, maxBerries, finalX, finalY);
                }
            }
        }
    }

    public void updateBerryTrees() {
        for (WorldObject obj : objects) {
            if (obj instanceof BerryTree) {
                ((BerryTree) obj).checkBerryRegrowth();
            }
        }
    }
}
