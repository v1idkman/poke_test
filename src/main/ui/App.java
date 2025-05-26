package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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

import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;

public class App {
    private static Player player = new Player("sarp");
    private static JFrame window = new JFrame("Poke test");
    private static WorldManager worldManager = new WorldManager(window);
    public static final int FIXED_HEIGHT = 640;
    public static final int FIXED_WIDTH = 960;

    public static void initPokemonData() {
        PokemonStatsLoader loader = PokemonStatsLoader.getInstance();
        loader.loadFromCSV("/resources/pokemon_information.csv");
    }

    public static void initWorlds() {
        worldManager.initBoards(player);
        worldManager.setCurrentWorld("outside");
    }

    private static void initWindow() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (Board board : worldManager.getWorlds().values()) {
            board.setWorldManager(worldManager);
        }
        
        Board currentBoard = worldManager.getCurrentWorld();
        Camera camera = Camera.getInstance();
        camera.setWorldDimensions(
            currentBoard.columns * Board.TILE_SIZE,
            currentBoard.rows * Board.TILE_SIZE
        );
        camera.setActive(currentBoard.isLarge());
        camera.update(player);
        
        Dimension windowSize;
        if (currentBoard.isLarge()) {
            windowSize = new Dimension(FIXED_WIDTH, FIXED_HEIGHT);
        } else {
            windowSize = currentBoard.getPreferredSize();
        }
        
        window.setSize(windowSize);
        
        // Create a layered pane to hold both the game board and menu button
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(window.getWidth(), window.getHeight()));
        layeredPane.setBounds(0, 0, window.getWidth(), window.getHeight());
        
        if (!currentBoard.isLarge()) {
            // This is a small board - center it in black background
            JPanel centeringPanel = new JPanel(new GridBagLayout());
            centeringPanel.setBackground(Color.BLACK);
            centeringPanel.setBounds(0, 0, window.getWidth(), window.getHeight());
            centeringPanel.add(currentBoard);
            
            layeredPane.add(centeringPanel, JLayeredPane.DEFAULT_LAYER);
        } else {
            // This is a large board - fit it to the fixed dimensions
            currentBoard.setBounds(0, 0, FIXED_WIDTH, FIXED_HEIGHT);
            layeredPane.add(currentBoard, JLayeredPane.DEFAULT_LAYER);
        }
        
        // Position menu button consistently at bottom right of window
        Menu menu = Menu.getInstance();
        menu.positionMenuButtonForWindow(layeredPane, window.getWidth() - 40, window.getHeight() - 40);
        
        window.add(layeredPane);
        window.addKeyListener(currentBoard);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // Set a custom focus traversal policy to prioritize the game board
        window.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
            @Override
            public Component getDefaultComponent(Container container) {
                Board currentBoard = worldManager.getCurrentWorld();
                return currentBoard != null ? currentBoard : super.getDefaultComponent(container);
            }
        });


        // Add this to the initWindow method in App.java
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                Board currentBoard = worldManager.getCurrentWorld();
                if (currentBoard != null && !currentBoard.hasFocus()) {
                    // Only handle key events if the board doesn't have focus
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
        Pokemon rayquaza = PokemonFactory.createPokemon(384, 70, "Rayquaza");
        Pokemon caterpie = PokemonFactory.createPokemon(10, 5, "Caterpie");
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
        player.addPokemonToCurrentTeam(rayquaza);
        player.addPokemonToCurrentTeam(caterpie);

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