package ui;

import javax.swing.*;

import model.Move;
import pokes.Bulbasaur;
import pokes.Charizard;
import model.Player;
import model.Move.MoveCategory;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;
import model.ItemFactory;


public class App {
    private static Player player;

    private static void initWindow() {
        // create a window frame and set the title in the toolbar
        JFrame window = new JFrame("Poke test");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        player.addToInventory("potion");
        player.addToInventory("master ball");
        player.addToInventory("good Rod");
        Board initialBoard = new Board(player);
        window.add(initialBoard);
        window.addKeyListener(initialBoard);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void initPokemon() {
        Pokemon bulbasaur = new Bulbasaur();
        Pokemon charizard = new Charizard();
        Pokemon charizard2 = new Charizard();
        charizard.getStats().setLevel(36);
        charizard.getStats().addEVs(100, 140, 80, 70, 100, 150);
        charizard2.getStats().addEVs(100, 140, 80, 70, 100, 150);
        charizard.holdItem(ItemFactory.createItem("great ball"));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Fly", PokemonType.FLYING, 60, 90, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        charizard.addMove(new Move("Flamethrower", PokemonType.FIRE, 90, 80, 15, MoveCategory.SPECIAL));
        player.addPokemonToCurrentTeam(bulbasaur);
        player.addPokemonToCurrentTeam(charizard);
        player.addPokemonToCurrentTeam(charizard2);

    }

    public static void main(String[] args) {
        player = new Player("sarp");
        initPokemon();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}