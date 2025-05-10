package ui;

import java.awt.Point;

import javax.swing.*;

import model.Move;
import model.Player;
import model.Move.MoveCategory;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;
import pokes.PokemonFactory;
import pokes.PokemonStatsLoader;
import model.Door;
import model.ItemFactory;


public class App {
    private static Player player = new Player("sarp");
    private static JFrame window = new JFrame("Poke test");
    private static WorldManager worldManager = new WorldManager(window, player);

    // Add this method
    private static void initPokemonData() {
        PokemonStatsLoader statsLoader = PokemonStatsLoader.getInstance();
        statsLoader.loadFromCSV("src/main/resources/pokemon_info.csv");
    }

    public static void initWorlds() {
        // initialize all boards
        Board outsideBoard = new Board(player, "outside");
        Board inside1 = new Board(player, "house_interior");

        // add all boards to the world manager and set the current world
        worldManager.addBoard(outsideBoard);
        worldManager.addBoard(inside1);
        worldManager.setCurrentWorld("outside");

        // add objects to boards (maybe make method for more objects)
        outsideBoard.addObject("/resources/buildings/red_house.png", 40, 40);
        outsideBoard.addObject("/resources/buildings/red_house.png", 140, 40);
        outsideBoard.addDoor(new Door(new Point(80, 40), "/resources/player_sprites/s_facing_front.png", 
                            "house_interior", outsideBoard.getLocation()));

        inside1.addObject("/resources/buildings/red_house.png", 80, 80);
        inside1.addDoor(new Door(new Point(42, 41), "/resources/player_sprites/s_facing_back.png", 
                            "outside", new Point(40, 100)));
    }

    private static void initWindow() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (Board board : worldManager.getWorlds().values()) {
            board.setWorldManager(worldManager);
        }
        
        window.add(worldManager.getCurrentWorld());
        window.addKeyListener(worldManager.getCurrentWorld());
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void initPokemon() {
        Pokemon bulbasaur = PokemonFactory.createPokemon(1, 5, "Bulbasaur");
        Pokemon charizard = PokemonFactory.createPokemon(6, 36, "Charizard");
        Pokemon charizard2 = PokemonFactory.createPokemon(6, 40,  "Charizard");
        Pokemon blastoise = PokemonFactory.createPokemon(9, 45, "Blastoise");
        blastoise.damage(30);
        blastoise.holdItem(ItemFactory.createItem("max revive"));
        charizard.getStats().addEVs(100, 140, 80, 0, 100, 0);
        charizard.holdItem(ItemFactory.createItem("great ball"));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Fly", PokemonType.FLYING, 60, 90, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        player.addPokemonToCurrentTeam(bulbasaur);
        player.addPokemonToCurrentTeam(charizard);
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
        initItems();
        initPokemonData();
        initPokemon();
        initWorlds();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}