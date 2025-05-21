package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;

import javax.swing.*;

import model.Move;
import model.Player;
import model.MoveFactory;
import model.MoveLoader;
import pokes.Pokemon;
import pokes.PokemonFactory;
import pokes.PokemonStatsLoader;
import model.Door;
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
    static final int FIXED_HEIGHT = 640;
    static final int FIXED_WIDTH = 960;

    // Add this method
    private static void initPokemonData() {
        PokemonStatsLoader statsLoader = PokemonStatsLoader.getInstance();
        statsLoader.loadFromCSV("src/main/resources/pokemon_info.csv");
    }

    public static void initWorlds() {
        // initialize all boards
        Board outsideBoard = new Board(player, "outside", 30, 46);
        Board inside1 = new Board(player, "house_interior", 10, 15);

        // add all boards to the world manager and set the current world
        worldManager.addBoard(outsideBoard);
        worldManager.addBoard(inside1);
        worldManager.setCurrentWorld("outside");

        // add objects to boards (maybe make method for more objects)
        outsideBoard.addObject("/resources/buildings/red_house.png", 2, 4);
        outsideBoard.addObject("/resources/buildings/green_spruce.png", 8, 5);
        outsideBoard.addDoor(new Door(new Point(10, 10), "/resources/player_sprites/s_facing_front.png", 
                            "house_interior", outsideBoard.getLocation()));

        inside1.addObject("/resources/buildings/marroon_single_bed.png", 10, 0);
        inside1.addDoor(new Door(new Point(5, 5), "/resources/player_sprites/s_facing_back.png", 
                            "outside", new Point(10, 15)));
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
    
    private static void initPokemon() {
        Pokemon charizard = PokemonFactory.createPokemon(6, 36, "Charizard");
        Pokemon bulbasaur = PokemonFactory.createPokemon(1, 5, "Bulbasaur");
        Pokemon charizard2 = PokemonFactory.createPokemon(6, 40,  "Charizard");
        Pokemon blastoise = PokemonFactory.createPokemon(9, 45, "Blastoise");
        blastoise.damage(30);
        blastoise.holdItem(ItemFactory.createItem("max revive"));
        charizard.getStats().addEVs(100, 140, 80, 0, 100, 0);
        charizard.holdItem(ItemFactory.createItem("great ball"));
        Move flamethrower = MoveFactory.createMove("Flamethrower");
        Move overheat = MoveFactory.createMove("Overheat");
        Move vineWhip = MoveFactory.createMove("Vine Whip");

        charizard.addMove(flamethrower);
        charizard.addMove(overheat);
        bulbasaur.addMove(vineWhip);
        player.addPokemonToCurrentTeam(charizard);
        player.addPokemonToCurrentTeam(bulbasaur);
        player.addPokemonToCurrentTeam(charizard2);
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
        initPokemon();
        initWorlds();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}