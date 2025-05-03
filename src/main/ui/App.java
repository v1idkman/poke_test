package ui;

import javax.swing.*;

import model.Item;
import model.KeyItem;
import model.Medicine;
import model.Medicine.MedicineType;
import model.Pokeball.PokeBallType;
import model.Player;
import model.Pokeball;
import model.KeyItem.KeyItemType;

public class App {
    private static Item potion;
    private static Item pokeball;
    private static Item goodRod;

    private static void initItems() {
        potion = new Medicine(MedicineType.POTION, "/resources/items/potion.png");
        pokeball = new Pokeball(PokeBallType.POKE_BALL, "/resources/items/pokeball.png");
        goodRod = new KeyItem(KeyItemType.GOOD_ROD, "/resources/items/fishing-rod.png");

    }

    private static void initWindow() {
        // create a window frame and set the title in the toolbar
        JFrame window = new JFrame("Poke test");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Player player = new Player("sarp");
        player.addToInventory(potion);
        player.addToInventory(pokeball);
        player.addToInventory(goodRod);
        Board initialBoard = new Board(player);
        window.add(initialBoard);
        window.addKeyListener(initialBoard);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static void main(String[] args) {
        initItems();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}