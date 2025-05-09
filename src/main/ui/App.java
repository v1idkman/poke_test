package ui;

import javax.swing.*;

import model.Move;
import model.Player;
import model.Move.MoveCategory;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;
import pokes.PokemonFactory;
import pokes.PokemonStatsLoader;
import model.ItemFactory;


public class App {
    private static Player player;
    private static PokemonStatsLoader statsLoader;

    // Add this method
    private static void initPokemonData() {
        statsLoader = PokemonStatsLoader.getInstance();
        statsLoader.loadFromCSV("src/main/resources/pokemon_info.csv");
    }

    private static void initWindow() {
        // create a window frame and set the title in the toolbar
        JFrame window = new JFrame("Poke test");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        WorldManager worldManager = new WorldManager(window, player);
        
        // Set the world manager reference in all boards
        for (Board board : worldManager.getWorlds().values()) {
            board.setWorldManager(worldManager);
        }
        
        // Add the initial board to the window
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
        player = new Player("sarp");
        initItems();
        initPokemonData();
        initPokemon();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}