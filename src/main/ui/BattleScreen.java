package ui;

import java.awt.*;
import javax.swing.*;
import model.Player;
import pokes.Pokemon;

public class BattleScreen extends JFrame {
    private Player player;
    private Pokemon wildPokemon;
    
    public BattleScreen(Player player, Pokemon wildPokemon) {
        this.player = player;
        this.wildPokemon = wildPokemon;
        
        setTitle("Battle");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create battle UI components
        initializeUI(); 
    }
    
    private void initializeUI() {
        // This is where you'd create your battle UI
        // For now, just a simple message and button to end battle
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel encounterLabel = new JLabel("A wild " + wildPokemon.getName() + 
                                          " (Level " + wildPokemon.getLevel() + ") appeared!");
        encounterLabel.setHorizontalAlignment(JLabel.CENTER);
        encounterLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(encounterLabel, BorderLayout.NORTH);
        
        JButton endBattleButton = new JButton("End Battle");
        endBattleButton.addActionListener(e -> dispose());
        panel.add(endBattleButton, BorderLayout.SOUTH);
        
        add(panel);
    }
}
