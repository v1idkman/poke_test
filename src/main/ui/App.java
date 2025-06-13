package ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import model.Move;
import model.Player;
import moves.LearnsetLoader;
import moves.MoveFactory;
import moves.MoveLoader;
import pokes.Pokemon;
import pokes.PokemonFactory;
import pokes.PokemonStatsLoader;
import model.EncounterTable;
import model.ItemFactory;

public class App {
    private static Player player = new Player("sarp");
    private static JFrame window = new JFrame("Poke test");
    private static WorldManager worldManager = new WorldManager(window);
    
    // Fullscreen support - remove windowed mode options
    private static GraphicsDevice graphicsDevice;
    
    // Zoom functionality
    public static int ZOOM_LEVEL = 2; // Start with 2x zoom
    public static final int MIN_ZOOM = 1;
    public static final int MAX_ZOOM = 4;
    
    // Dynamic dimensions based on screen size and zoom
    public static int CURRENT_WIDTH;
    public static int CURRENT_HEIGHT;
    public static int EFFECTIVE_TILE_SIZE;

    static {
        // Initialize graphics device for fullscreen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsDevice = ge.getDefaultScreenDevice();
        
        // Force fullscreen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CURRENT_WIDTH = screenSize.width;
        CURRENT_HEIGHT = screenSize.height;
        
        // Calculate effective tile size based on zoom
        EFFECTIVE_TILE_SIZE = Board.TILE_SIZE * ZOOM_LEVEL;
    }

    public static void initPokemonData() {
        PokemonStatsLoader loader = PokemonStatsLoader.getInstance();
        loader.loadFromCSV("/resources/pokemon_information.csv");
    }

    public static void initWorlds() {
        worldManager.initBoards(player);
        worldManager.setCurrentWorld("outside");
    }

    private static void initWindow() {
        // Force fullscreen - no windowed mode option
        if (!graphicsDevice.isFullScreenSupported()) {
            System.err.println("Fullscreen not supported! Exiting...");
            System.exit(1);
        }

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.setResizable(false);

        for (Board board : worldManager.getWorlds().values()) {
            board.setWorldManager(worldManager);
        }
        
        Board currentBoard = worldManager.getCurrentWorld();
        Camera camera = Camera.getInstance();
        
        // Set camera with zoom considerations
        camera.setWorldDimensions(
            currentBoard.columns * EFFECTIVE_TILE_SIZE,
            currentBoard.rows * EFFECTIVE_TILE_SIZE
        );
        camera.setActive(true); // Always active for zoomed view
        camera.setZoomLevel(ZOOM_LEVEL);
        camera.update(player);
        
        setupFullscreenWindow(currentBoard);
        
        // Add zoom controls
        addZoomKeyListener();
        
        // Set fullscreen
        graphicsDevice.setFullScreenWindow(window);
        
        window.setVisible(true);
        currentBoard.requestFocusInWindow();
    }
    
    private static void setupFullscreenWindow(Board currentBoard) {
        // Create layered pane for fullscreen
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(CURRENT_WIDTH, CURRENT_HEIGHT));
        layeredPane.setBounds(0, 0, CURRENT_WIDTH, CURRENT_HEIGHT);
        
        // Scale board to fullscreen with zoom
        currentBoard.setBounds(0, 0, CURRENT_WIDTH, CURRENT_HEIGHT);
        currentBoard.setZoomLevel(ZOOM_LEVEL);
        layeredPane.add(currentBoard, JLayeredPane.DEFAULT_LAYER);
        
        // Ensure camera is properly set up
        Camera camera = Camera.getInstance();
        camera.setWorldDimensions(
            currentBoard.columns * Board.TILE_SIZE,
            currentBoard.rows * Board.TILE_SIZE
        );
        camera.setActive(currentBoard.isLarge());
        
        // Force initial camera update
        camera.update(player);
        
        // Position menu button for fullscreen
        Menu menu = Menu.getInstance();
        menu.positionMenuButtonForWindow(layeredPane, CURRENT_WIDTH - 40, CURRENT_HEIGHT - 40);
        
        window.getContentPane().removeAll();
        window.add(layeredPane);
        window.addKeyListener(currentBoard);
        
        // Set focus traversal policy
        window.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
            @Override
            public Component getDefaultComponent(Container container) {
                Board currentBoard = worldManager.getCurrentWorld();
                return currentBoard != null ? currentBoard : super.getDefaultComponent(container);
            }
        });
    }
    
    /**
     * Change zoom level and update display
     */
    public static void changeZoom(int delta) {
        int newZoom = ZOOM_LEVEL + delta;
        if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
            ZOOM_LEVEL = newZoom;
            EFFECTIVE_TILE_SIZE = Board.TILE_SIZE * ZOOM_LEVEL;
            
            // Update camera and world
            Board currentBoard = worldManager.getCurrentWorld();
            Camera camera = Camera.getInstance();
            
            // Calculate player's relative position before zoom change
            double playerRelativeX = (double) player.getWorldX() / (currentBoard.columns * (Board.TILE_SIZE * (ZOOM_LEVEL - delta)));
            double playerRelativeY = (double) player.getWorldY() / (currentBoard.rows * (Board.TILE_SIZE * (ZOOM_LEVEL - delta)));
            
            // Update world dimensions
            camera.setWorldDimensions(
                currentBoard.columns * EFFECTIVE_TILE_SIZE,
                currentBoard.rows * EFFECTIVE_TILE_SIZE
            );
            camera.setZoomLevel(ZOOM_LEVEL);
            
            // Adjust player position to maintain relative position
            int newWorldX = (int) (playerRelativeX * currentBoard.columns * EFFECTIVE_TILE_SIZE);
            int newWorldY = (int) (playerRelativeY * currentBoard.rows * EFFECTIVE_TILE_SIZE);
            player.setPosition(new Point(newWorldX, newWorldY));
            
            // Update board zoom
            currentBoard.setZoomLevel(ZOOM_LEVEL);
            
            // Update camera
            camera.update(player);
            
            // Refresh display
            window.repaint();
            
            System.out.println("Zoom level: " + ZOOM_LEVEL + "x");
        }
    }
    
    /**
     * Add zoom controls (+ and - keys)
     */
    private static void addZoomKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    // Plus key zooms in
                    if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
                        changeZoom(1);
                        return true;
                    }
                    
                    // Minus key zooms out
                    if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                        changeZoom(-1);
                        return true;
                    }
                    
                    // ESC key exits application (since no windowed mode)
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                        return true;
                    }
                }
                
                // Handle other key events for the game board
                Board currentBoard = worldManager.getCurrentWorld();
                if (currentBoard != null && !currentBoard.hasFocus()) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        currentBoard.keyPressed(e);
                        return true;
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        currentBoard.keyReleased(e);
                        return true;
                    }
                }
                return false;
            }
        });
    }
    
    public static int getEffectiveTileSize() {
        return Board.TILE_SIZE * ZOOM_LEVEL;
    }
    
    public static int getZoomLevel() {
        return ZOOM_LEVEL;
    }
    
    public static void initMoves() {
        MoveLoader moveLoader = MoveLoader.getInstance();
        moveLoader.loadFromCSV("src/main/resources/pokemon_moves.csv");
    }
    
    public static void initLearnsets() {
        LearnsetLoader loader = LearnsetLoader.getInstance();
        loader.loadFromTypeScriptFile("/resources/learnsets.ts");
    }

    private static void initPokemon() {
        Pokemon charizard = PokemonFactory.createPokemon(6, 36, "Charizard");
        Pokemon bulbasaur = PokemonFactory.createPokemon(1, 5, "Bulbasaur");
        Pokemon charmander = PokemonFactory.createPokemon(4, 8,  "Charmander");
        Pokemon blastoise = PokemonFactory.createPokemon(9, 45, "Blastoise");
        blastoise.damage(30);
        blastoise.holdItem(ItemFactory.createItem("max revive"));
        charizard.getStats().addEVs(100, 140, 80, 0, 100, 0);
        charizard.holdItem(ItemFactory.createItem("great ball"));
        Move flamethrower = MoveFactory.createMove("Flamethrower");
        Move overheat = MoveFactory.createMove("Overheat");
        Move vineWhip = MoveFactory.createMove("Vine Whip");
        Move scratch = MoveFactory.createMove("Scratch");
        Move bubbleBeam = MoveFactory.createMove("Bubble Beam");
        scratch.setPP(0);

        charizard.addMove(flamethrower);
        charizard.addMove(overheat);
        bulbasaur.addMove(vineWhip);
        charmander.addMove(flamethrower);
        charmander.addMove(scratch);
        blastoise.addMove(bubbleBeam);
        player.addPokemonToCurrentTeam(charmander);
        player.addPokemonToCurrentTeam(charizard);
        player.addPokemonToCurrentTeam(bulbasaur);
        player.addPokemonToCurrentTeam(blastoise);
    }

    public static void initItems() {
        player.addToInventory("potion");
        player.addToInventory("master ball");
        player.addToInventory("poke ball");
        player.addToInventory("great ball");
        player.addToInventory("ultra ball");
        player.addToInventory("dusk ball");
        player.addToInventory("luxury ball");
        player.addToInventory("good Rod");
        player.addToInventory("super potion");
    }

    public static void main(String[] args) {
        EncounterTable.initializeEncounterTables();
        initItems();
        initPokemonData();
        initMoves();
        initLearnsets();
        initPokemon();
        initWorlds();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}
