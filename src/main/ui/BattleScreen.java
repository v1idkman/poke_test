package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;

import java.util.List;

import model.Item;
import model.Move;
import model.Player;
import pokes.Pokemon;

public class BattleScreen extends JFrame {
    private Player player;
    private Pokemon wildPokemon;
    private Pokemon playerPokemon;
    
    // UI Components
    private JPanel mainPanel;
    private JPanel battlegroundPanel;
    private JPanel actionPanel;
    private JPanel movePanel;
    private JPanel infoPanel;
    
    private JLabel wildPokemonImage;
    private JLabel playerPokemonImage;
    private JLabel wildPokemonInfo;
    private JLabel playerPokemonInfo;
    private JLabel battleMessageLabel;
    
    private JProgressBar wildPokemonHP;
    private JProgressBar playerPokemonHP;
    
    private JButton fightButton;
    private JButton bagButton;
    private JButton pokemonButton;
    private JButton runButton;

    private JLabel ppLabel;
    
    private JButton[] moveButtons = new JButton[4];
    private JButton backButton;
    
    private Timer animationTimer;
    private int animationStep = 0;
    
    // Battle state
    private boolean playerTurn = true;
    private boolean battleEnded = false;
    
    public BattleScreen(Player player, Pokemon wildPokemon) {
        this.player = player;
        this.wildPokemon = wildPokemon;
        
        // Get the first non-fainted Pokémon from player's team
        for (Pokemon p : player.getTeam()) {
            if (p.getStats().getCurrentHp() > 0) {
                this.playerPokemon = p;
                break;
            }
        }
        
        if (this.playerPokemon == null) {
            // Handle case where player has no usable Pokémon
            JOptionPane.showMessageDialog(this, "You have no usable Pokémon!", "Battle Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Pokémon Battle");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Initialize UI components
        initializeUI();
        
        // Start battle sequence
        startBattleSequence();
    }
    
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // Create battleground panel (top part with Pokémon sprites and HP bars)
        createBattlegroundPanel();
        
        // Create action panel (bottom part with buttons)
        createActionPanel();
        
        // Create move selection panel (initially hidden)
        createMovePanel();
        
        // Create info panel for battle messages
        createInfoPanel();
        
        // Add panels to main panel
        mainPanel.add(battlegroundPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        setContentPane(mainPanel);
    }
    
    private void createBattlegroundPanel() {
        battlegroundPanel = new JPanel(null); // Using null layout for precise positioning
        battlegroundPanel.setPreferredSize(new Dimension(800, 400));
        battlegroundPanel.setBackground(new Color(144, 238, 144)); // Light green background like in GBA games
        
        // Create battle platform for wild Pokémon (enemy)
        JPanel enemyPlatform = new JPanel();
        enemyPlatform.setBounds(500, 150, 200, 30);
        enemyPlatform.setBackground(new Color(210, 180, 140)); // Tan platform
        battlegroundPanel.add(enemyPlatform);
        
        // Create battle platform for player Pokémon
        JPanel playerPlatform = new JPanel();
        playerPlatform.setBounds(150, 250, 200, 30);
        playerPlatform.setBackground(new Color(210, 180, 140)); // Tan platform
        battlegroundPanel.add(playerPlatform);
        
        // Wild Pokémon sprite - positioned on top of enemy platform
        wildPokemonImage = new JLabel();
        wildPokemonImage.setBounds(500, 50, 150, 150);
        wildPokemonImage.setIcon(loadPokemonImage(wildPokemon, true));
        battlegroundPanel.add(wildPokemonImage);
        
        // Player Pokémon sprite - positioned on top of player platform
        playerPokemonImage = new JLabel();
        playerPokemonImage.setBounds(150, 150, 150, 150);
        playerPokemonImage.setIcon(loadPokemonImage(playerPokemon, false));
        battlegroundPanel.add(playerPokemonImage);
        
        // Wild Pokémon info box (like in the image - top left)
        JPanel wildInfoBox = new JPanel(null);
        wildInfoBox.setBounds(50, 50, 250, 80);
        wildInfoBox.setBackground(new Color(248, 248, 240));
        wildInfoBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Wild Pokémon name and level
        wildPokemonInfo = new JLabel(wildPokemon.getName() + " L" + wildPokemon.getLevel());
        wildPokemonInfo.setBounds(20, 10, 200, 30);
        wildPokemonInfo.setFont(new Font("Arial", Font.BOLD, 16));
        wildInfoBox.add(wildPokemonInfo);
        
        // HP text
        JLabel wildHpText = new JLabel("HP:");
        wildHpText.setBounds(20, 40, 30, 15);
        wildHpText.setFont(new Font("Arial", Font.PLAIN, 12));
        wildInfoBox.add(wildHpText);
        
        // Wild Pokémon HP bar
        wildPokemonHP = new JProgressBar(0, wildPokemon.getStats().getMaxHp());
        wildPokemonHP.setValue(wildPokemon.getStats().getCurrentHp());
        wildPokemonHP.setBounds(50, 40, 150, 10);
        wildPokemonHP.setForeground(new Color(96, 192, 96)); // Green HP bar
        wildPokemonHP.setBackground(new Color(224, 224, 224));
        wildPokemonHP.setBorderPainted(false);
        wildPokemonHP.setStringPainted(false);
        wildInfoBox.add(wildPokemonHP);
        
        battlegroundPanel.add(wildInfoBox);
        
        // Player Pokémon info box (like in the image - bottom right)
        JPanel playerInfoBox = new JPanel(null);
        playerInfoBox.setBounds(500, 250, 250, 80);
        playerInfoBox.setBackground(new Color(248, 248, 240));
        playerInfoBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Player Pokémon name and level
        playerPokemonInfo = new JLabel(playerPokemon.getName() + " L" + playerPokemon.getLevel());
        playerPokemonInfo.setBounds(20, 10, 200, 30);
        playerPokemonInfo.setFont(new Font("Arial", Font.BOLD, 16));
        playerInfoBox.add(playerPokemonInfo);
        
        // HP text
        JLabel playerHpText = new JLabel("HP:");
        playerHpText.setBounds(20, 40, 30, 15);
        playerHpText.setFont(new Font("Arial", Font.PLAIN, 12));
        playerInfoBox.add(playerHpText);
        
        // Player Pokémon HP bar
        playerPokemonHP = new JProgressBar(0, playerPokemon.getStats().getMaxHp());
        playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
        playerPokemonHP.setBounds(50, 40, 150, 10);
        playerPokemonHP.setForeground(new Color(96, 192, 96)); // Green HP bar
        playerPokemonHP.setBackground(new Color(224, 224, 224));
        playerPokemonHP.setBorderPainted(false);
        playerPokemonHP.setStringPainted(false);
        playerInfoBox.add(playerPokemonHP);
        
        // Current/Max HP display
        hpValueLabel = new JLabel(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
        hpValueLabel.setBounds(130, 60, 100, 15);
        hpValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        hpValueLabel.setHorizontalAlignment(JLabel.RIGHT);
        playerInfoBox.add(hpValueLabel);
        
        battlegroundPanel.add(playerInfoBox);
    }
    
    private void createActionPanel() {
        actionPanel = new JPanel(new BorderLayout());
        actionPanel.setPreferredSize(new Dimension(800, 150));
        
        // Create the message box (top part of action panel)
        JPanel messageBox = new JPanel();
        messageBox.setBackground(new Color(248, 248, 240));
        messageBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        messageBox.setLayout(new BorderLayout());
        
        battleMessageLabel = new JLabel("What will " + playerPokemon.getName() + " do?");
        battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        battleMessageLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        messageBox.add(battleMessageLabel, BorderLayout.CENTER);
        
        // Create the button panel (bottom part of action panel)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 2, 2));
        buttonPanel.setBackground(new Color(80, 80, 80));
        buttonPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        fightButton = createActionButton("FIGHT", new Color(240, 80, 80));
        bagButton = createActionButton("BAG", new Color(80, 80, 240));
        pokemonButton = createActionButton("POKÉMON", new Color(80, 200, 80));
        runButton = createActionButton("RUN", new Color(240, 240, 80));
        
        fightButton.addActionListener(e -> showMovePanel());
        bagButton.addActionListener(e -> openBag());
        pokemonButton.addActionListener(e -> switchPokemon());
        runButton.addActionListener(e -> attemptRun());
        
        buttonPanel.add(fightButton);
        buttonPanel.add(bagButton);
        buttonPanel.add(pokemonButton);
        buttonPanel.add(runButton);
        
        // Add both panels to the action panel
        actionPanel.add(messageBox, BorderLayout.CENTER);
        actionPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        return button;
    }
    
    private void createMovePanel() {
        // Create a new panel with GridLayout for the moves
        JPanel movesGrid = new JPanel(new GridLayout(2, 2, 5, 5));
        movesGrid.setBackground(new Color(240, 240, 240));
        
        // Debug the available moves
        System.out.println("Creating move panel for " + playerPokemon.getName());
        System.out.println("Moves available: " + playerPokemon.getMoves().size());
        
        // Create move buttons based on player Pokémon's moves
        for (int i = 0; i < 4; i++) {
            if (i < playerPokemon.getMoves().size() && playerPokemon.getMoves().get(i) != null) {
                Move move = playerPokemon.getMoves().get(i);
                System.out.println("Adding move: " + move.getName());
                moveButtons[i] = createMoveButton(move);
                
                // Create a final reference to the move for the lambda
                final Move finalMove = move;
                moveButtons[i].addActionListener(e -> useMove(finalMove));
            } else {
                moveButtons[i] = createMoveButton(null);
                moveButtons[i].setEnabled(false);
            }
            movesGrid.add(moveButtons[i]);
        }
        
        // Create bottom panel for PP display and back button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 240, 240));
        
        // Add PP display
        JPanel ppPanel = new JPanel(new BorderLayout());
        ppPanel.setBackground(new Color(240, 240, 240));
        ppLabel = new JLabel("PP: --/--");
        ppPanel.add(ppLabel, BorderLayout.WEST);
        
        // Add back button
        JPanel backPanel = new JPanel(new BorderLayout());
        backPanel.setBackground(new Color(240, 240, 240));
        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(200, 200, 200));
        backButton.addActionListener(e -> showActionPanel());
        backPanel.add(backButton, BorderLayout.EAST);
        
        // Add panels to bottom panel
        bottomPanel.add(ppPanel, BorderLayout.WEST);
        bottomPanel.add(backPanel, BorderLayout.EAST);
        
        // Create a container panel for the entire move panel
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 240, 240));
        container.add(movesGrid, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);
        
        // Replace the old movePanel with the new one
        movePanel = container;
    }
    
    private JButton createMoveButton(Move move) {
        JButton button = new JButton();
        
        if (move != null) {
            button.setText(move.getName());
            
            // Set button color based on move type
            Color typeColor = UIComponentFactory.getColorForType(move.getType());
            button.setBackground(typeColor);
            button.setForeground(Color.WHITE);
            
            // Add PP information
            button.setToolTipText("PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
        } else {
            button.setText("-");
            button.setBackground(Color.GRAY);
        }
        
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        
        return button;
    }
    
    private void createInfoPanel() {
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(800, 100));
        infoPanel.setBorder(new LineBorder(Color.BLACK, 2));
        infoPanel.setBackground(Color.WHITE);
        
        battleMessageLabel = new JLabel("A wild " + wildPokemon.getName() + " appeared!");
        battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        battleMessageLabel.setHorizontalAlignment(JLabel.CENTER);
        battleMessageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        infoPanel.add(battleMessageLabel, BorderLayout.CENTER);
    }
    
    private void startBattleSequence() {
        // Hide action panel initially
        mainPanel.remove(actionPanel);
        
        // Show info panel with encounter message
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Create animation sequence
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        battleMessageLabel.setText("Go, " + playerPokemon.getName() + "!");
                        break;
                    case 2:
                        battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                        
                        // Show action panel after intro sequence
                        mainPanel.remove(infoPanel);
                        mainPanel.add(actionPanel, BorderLayout.SOUTH);
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        
                        // Stop the timer
                        animationTimer.stop();
                        break;
                }
            }
        });
        
        // Start the animation sequence
        animationTimer.start();
    }
    
    private void showMovePanel() {
        mainPanel.remove(actionPanel);
        mainPanel.add(movePanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void showActionPanel() {
        mainPanel.remove(movePanel);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void showInfoPanel(String message) {
        battleMessageLabel.setText(message);
        
        mainPanel.remove(actionPanel);
        mainPanel.remove(movePanel);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void useMove(Move move) {
        if (!playerTurn || battleEnded) return;
        
        playerTurn = false;
        
        // Decrease PP
        move.decreasePP();
        
        // Show attack message
        showInfoPanel(playerPokemon.getName() + " used " + move.getName() + "!");
        
        // Calculate damage
        int damage = calculateDamage(playerPokemon, wildPokemon, move);
        
        // Create animation sequence for attack
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        // Apply damage
                        wildPokemon.damage(damage);
                        updateWildPokemonHP();
                        
                        // Show damage message
                        if (damage > 0) {
                            battleMessageLabel.setText("It dealt " + damage + " damage!");
                        } else {
                            battleMessageLabel.setText("It had no effect...");
                        }
                        break;
                    case 2:
                        // Check if wild Pokémon fainted
                        if (wildPokemon.getStats().getCurrentHp() <= 0) {
                            battleMessageLabel.setText("The wild " + wildPokemon.getName() + " fainted!");
                            battleEnded = true;
                        } else {
                            // Wild Pokémon's turn
                            wildPokemonAttack();
                        }
                        break;
                    case 3:
                        if (battleEnded) {
                            // Battle is over, show victory message
                            battleMessageLabel.setText("You won the battle!");
                        } else if (playerPokemon.getStats().getCurrentHp() <= 0) {
                            // Player Pokémon fainted
                            battleMessageLabel.setText(playerPokemon.getName() + " fainted!");
                            battleEnded = true;
                        } else {
                            // Return to action selection
                            battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                            animationTimer.stop();
                            animationStep = 0;
                            playerTurn = true;
                            
                            mainPanel.remove(infoPanel);
                            mainPanel.add(actionPanel, BorderLayout.SOUTH);
                            mainPanel.revalidate();
                            mainPanel.repaint();
                        }
                        break;
                    case 4:
                        // End battle if it's over
                        if (battleEnded) {
                            animationTimer.stop();
                            
                            // Add a delay before closing
                            Timer closeTimer = new Timer(2000, event -> dispose());
                            closeTimer.setRepeats(false);
                            closeTimer.start();
                        }
                        break;
                }
            }
        });
        
        // Reset animation step and start timer
        animationStep = 0;
        animationTimer.start();
    }
    
    private void wildPokemonAttack() {
        // Select a random move for the wild Pokémon
        List<Move> wildMoves = wildPokemon.getMoves();
        
        if (wildMoves.isEmpty()) {
            // If no moves, use a default "Tackle" move
            battleMessageLabel.setText("The wild " + wildPokemon.getName() + " used Tackle!");
            
            // Calculate a simple damage
            int damage = 5 + new Random().nextInt(5);
            
            // Apply damage to player's Pokémon
            playerPokemon.damage(damage);
            updatePlayerPokemonHP();
            
            // Show damage message in next step
            battleMessageLabel.setText("It dealt " + damage + " damage!");
        } else {
            // Select a random move
            Move selectedMove = wildMoves.get(new Random().nextInt(wildMoves.size()));
            
            battleMessageLabel.setText("The wild " + wildPokemon.getName() + " used " + selectedMove.getName() + "!");
            
            // Calculate damage
            int damage = calculateDamage(wildPokemon, playerPokemon, selectedMove);
            
            // Apply damage to player's Pokémon
            playerPokemon.damage(damage);
            updatePlayerPokemonHP();
            
            // Show damage message in next step
            battleMessageLabel.setText("It dealt " + damage + " damage!");
        }
    }
    
    private void updateWildPokemonHP() {
        int currentHP = wildPokemon.getStats().getCurrentHp();
        int maxHP = wildPokemon.getStats().getMaxHp();
        
        // Ensure HP doesn't go below 0
        currentHP = Math.max(0, currentHP);
        
        // Update progress bar
        wildPokemonHP.setValue(currentHP);
        wildPokemonHP.setString("HP: " + currentHP + "/" + maxHP);
        
        // Change color based on HP percentage
        float percentage = (float) currentHP / maxHP;
        if (percentage < 0.2) {
            wildPokemonHP.setForeground(Color.RED);
        } else if (percentage < 0.5) {
            wildPokemonHP.setForeground(Color.ORANGE);
        }
    }
    
    // Add this as a class field
    private JLabel hpValueLabel;

    private void updatePlayerPokemonHP() {
        int currentHP = playerPokemon.getStats().getCurrentHp();
        int maxHP = playerPokemon.getStats().getMaxHp();
        
        // Ensure HP doesn't go below 0
        currentHP = Math.max(0, currentHP);
        
        // Update progress bar
        playerPokemonHP.setValue(currentHP);
        
        // Update HP text display
        hpValueLabel.setText(currentHP + "/" + maxHP);
        
        // Change color based on HP percentage
        float percentage = (float) currentHP / maxHP;
        if (percentage < 0.2) {
            playerPokemonHP.setForeground(Color.RED);
        } else if (percentage < 0.5) {
            playerPokemonHP.setForeground(Color.ORANGE);
        } else {
            playerPokemonHP.setForeground(new Color(96, 192, 96));
        }
    }

    private int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        // This is a simplified damage calculation
        // In a real Pokémon game, this would be much more complex
        
        int attackStat = (move.getCategory() == Move.MoveCategory.PHYSICAL) ? 
                          attacker.getStats().getAttack() : 
                          attacker.getStats().getSpecialAtk();
        
        int defenseStat = (move.getCategory() == Move.MoveCategory.PHYSICAL) ? 
                           defender.getStats().getDefense() : 
                           defender.getStats().getSpecialDef();
        
        // Calculate base damage
        double damage = (((2 * attacker.getLevel() / 5.0) + 2) * move.getPower() * attackStat / defenseStat) / 50 + 2;
        
        // Apply STAB (Same Type Attack Bonus)
        if (attacker.getTypes().contains(move.getType())) {
            damage *= 1.5;
        }
        
        // Apply type effectiveness
        double typeEffectiveness = calculateTypeEffectiveness(move.getType(), defender.getTypes());
        damage *= typeEffectiveness;
        
        // Apply random factor (85-100%)
        damage *= (0.85 + new Random().nextDouble() * 0.15);
        
        return (int) damage;
    }
    
    private double calculateTypeEffectiveness(Pokemon.PokemonType moveType, List<Pokemon.PokemonType> defenderTypes) {
        // This is a simplified type effectiveness calculation
        // In a real implementation, you would have a complete type chart
        
        double effectiveness = 1.0;
        
        for (Pokemon.PokemonType defenderType : defenderTypes) {
            // Super effective combinations
            if ((moveType == Pokemon.PokemonType.WATER && defenderType == Pokemon.PokemonType.FIRE) ||
                (moveType == Pokemon.PokemonType.FIRE && defenderType == Pokemon.PokemonType.GRASS) ||
                (moveType == Pokemon.PokemonType.GRASS && defenderType == Pokemon.PokemonType.WATER)) {
                effectiveness *= 2.0;
            }
            
            // Not very effective combinations
            else if ((moveType == Pokemon.PokemonType.WATER && defenderType == Pokemon.PokemonType.GRASS) ||
                     (moveType == Pokemon.PokemonType.FIRE && defenderType == Pokemon.PokemonType.WATER) ||
                     (moveType == Pokemon.PokemonType.GRASS && defenderType == Pokemon.PokemonType.FIRE)) {
                effectiveness *= 0.5;
            }
        }
        
        return effectiveness;
    }
    
    private void openBag() {
        // Create a panel for the bag interface
        JPanel bagPanel = new JPanel();
        bagPanel.setLayout(new BoxLayout(bagPanel, BoxLayout.Y_AXIS));
        bagPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bagPanel.setBackground(new Color(248, 248, 240));
        
        // Add a title label
        JLabel titleLabel = new JLabel("Bag");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bagPanel.add(titleLabel);
        bagPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Get items from player's inventory
        Set<Item> inventory = player.getInventory();
        
        if (inventory.isEmpty()) {
            JLabel emptyLabel = new JLabel("Your bag is empty!");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bagPanel.add(emptyLabel);
        } else {
            // Create a scroll pane for items
            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            itemsPanel.setBackground(Color.WHITE);
            
            for (Item item : inventory) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
                itemPanel.setMaximumSize(new Dimension(700, 40));
                
                // Item name and quantity
                JLabel itemLabel = new JLabel(item.getName() + " x" + item.getQuantity());
                itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                itemPanel.add(itemLabel, BorderLayout.WEST);
                
                // Use button
                JButton useButton = new JButton("Use");
                useButton.addActionListener(e -> {
                    useItem(item);
                    mainPanel.remove(bagPanel);
                    mainPanel.add(infoPanel, BorderLayout.SOUTH);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
                itemPanel.add(useButton, BorderLayout.EAST);
                
                itemsPanel.add(itemPanel);
                itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            
            JScrollPane scrollPane = new JScrollPane(itemsPanel);
            scrollPane.setPreferredSize(new Dimension(700, 200));
            scrollPane.setBorder(null);
            bagPanel.add(scrollPane);
        }
        
        // Add back button
        bagPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            mainPanel.remove(bagPanel);
            mainPanel.add(actionPanel, BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        bagPanel.add(backButton);
        
        // Show the bag panel
        mainPanel.remove(actionPanel);
        mainPanel.add(bagPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void useItem(Item item) {
        // Show item use message
        showInfoPanel("Used " + item.getName() + "!");
        
        // Process item effects based on item type
        boolean itemUsed = false;
        
        if (item.getName().toLowerCase().contains("ball")) {
            // Pokéball - attempt to catch the wild Pokémon
            attemptCatch(item);
            itemUsed = true;
        } else if (item.getName().toLowerCase().contains("potion")) {
            // Potion - heal the active Pokémon
            int healAmount = 20; // Basic potion
            
            if (item.getName().toLowerCase().contains("super")) {
                healAmount = 50;
            } else if (item.getName().toLowerCase().contains("hyper")) {
                healAmount = 200;
            } else if (item.getName().toLowerCase().contains("max")) {
                healAmount = playerPokemon.getStats().getMaxHp();
            }
            
            int currentHP = playerPokemon.getStats().getCurrentHp();
            int maxHP = playerPokemon.getStats().getMaxHp();
            int newHP = Math.min(currentHP + healAmount, maxHP);
            
            playerPokemon.getStats().setCurrentHp(newHP);
            updatePlayerPokemonHP();
            
            // Create animation sequence for healing
            animationTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationStep++;
                    
                    switch (animationStep) {
                        case 1:
                            battleMessageLabel.setText(playerPokemon.getName() + " was healed!");
                            break;
                        case 2:
                            // Wild Pokémon's turn after using item
                            playerTurn = false;
                            wildPokemonAttack();
                            break;
                        case 3:
                            if (playerPokemon.getStats().getCurrentHp() <= 0) {
                                // Player Pokémon fainted
                                battleMessageLabel.setText(playerPokemon.getName() + " fainted!");
                                battleEnded = true;
                            } else {
                                // Return to action selection
                                battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                                animationTimer.stop();
                                animationStep = 0;
                                playerTurn = true;
                                
                                mainPanel.remove(infoPanel);
                                mainPanel.add(actionPanel, BorderLayout.SOUTH);
                                mainPanel.revalidate();
                                mainPanel.repaint();
                            }
                            break;
                    }
                }
            });
            
            // Reset animation step and start timer
            animationStep = 0;
            animationTimer.start();
            
            itemUsed = true;
        }
        
        // Decrease item quantity if used
        if (itemUsed) {
            item.decreaseQuantity();
            if (item.getQuantity() <= 0) {
                player.removeItem(item);
            }
        }
    }

    private void attemptCatch(Item pokeball) {
        // Calculate catch rate
        double catchRate = 0.5; // Base catch rate
        
        // Adjust based on pokeball type
        if (pokeball.getName().toLowerCase().contains("great")) {
            catchRate = 0.7;
        } else if (pokeball.getName().toLowerCase().contains("ultra")) {
            catchRate = 0.85;
        } else if (pokeball.getName().toLowerCase().contains("master")) {
            catchRate = 1.0;
        }
        
        // Adjust based on remaining HP percentage
        double hpFactor = 1.0 - ((double) wildPokemon.getStats().getCurrentHp() / wildPokemon.getStats().getMaxHp()) * 0.7;
        catchRate *= (1.0 + hpFactor);
        
        // Clamp catch rate between 0 and 1
        catchRate = Math.min(1.0, Math.max(0.0, catchRate));
        
        // Determine if catch is successful
        boolean catchSuccess = Math.random() < catchRate;
        
        // Create animation sequence for catch attempt
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        battleMessageLabel.setText("You threw a " + pokeball.getName() + "!");
                        break;
                    case 2:
                        battleMessageLabel.setText("Shake...");
                        break;
                    case 3:
                        battleMessageLabel.setText("Shake...");
                        break;
                    case 4:
                        battleMessageLabel.setText("Shake...");
                        break;
                    case 5:
                        if (catchSuccess) {
                            battleMessageLabel.setText("Gotcha! " + wildPokemon.getName() + " was caught!");
                            player.addPokemonToCurrentTeam(wildPokemon);
                            battleEnded = true;
                            
                            // Close battle screen after a delay
                            Timer closeTimer = new Timer(2000, event -> dispose());
                            closeTimer.setRepeats(false);
                            closeTimer.start();
                        } else {
                            battleMessageLabel.setText("Oh no! The Pokémon broke free!");
                            
                            // Wild Pokémon's turn after failed catch
                            Timer nextTurn = new Timer(1500, event -> {
                                playerTurn = false;
                                wildPokemonAttack();
                                
                                // Reset animation step for the next sequence
                                animationStep = 0;
                            });
                            nextTurn.setRepeats(false);
                            nextTurn.start();
                        }
                        break;
                }
            }
        });
        
        // Reset animation step and start timer
        animationStep = 0;
        animationTimer.start();
    }

    private void switchPokemon() {
        if (player.getTeam().size() <= 1) {
            showInfoPanel("You tried to switch Pokémon...");
            Timer switchTimer = new Timer(2000, e -> {
                showInfoPanel("But you have no other Pokémon to switch to!");
                
                Timer returnTimer = new Timer(1500, event -> {
                    mainPanel.remove(infoPanel);
                    mainPanel.add(actionPanel, BorderLayout.SOUTH);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
                returnTimer.setRepeats(false);
                returnTimer.start();
            });
            switchTimer.setRepeats(false);
            switchTimer.start();
        } else {
            // Create a panel to display the player's Pokémon team
            JPanel pokemonSelectionPanel = new JPanel();
            pokemonSelectionPanel.setLayout(new BoxLayout(pokemonSelectionPanel, BoxLayout.Y_AXIS));
            pokemonSelectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            pokemonSelectionPanel.setBackground(new Color(248, 248, 240));
            
            // Add a title label
            JLabel titleLabel = new JLabel("Choose a Pokémon to switch to:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            pokemonSelectionPanel.add(titleLabel);
            pokemonSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            
            // Create a button for each Pokémon in the team
            for (Pokemon pokemon : player.getTeam()) {
                // Skip the current Pokémon and fainted Pokémon
                if (pokemon == playerPokemon || pokemon.getStats().getCurrentHp() <= 0) {
                    continue;
                }
                
                // Create a panel for each Pokémon with info
                JPanel pokemonPanel = new JPanel(new BorderLayout(10, 0));
                pokemonPanel.setBackground(Color.WHITE);
                pokemonPanel.setBorder(new LineBorder(Color.BLACK, 1));
                pokemonPanel.setMaximumSize(new Dimension(700, 50));
                
                // Add Pokémon name and level
                JLabel nameLabel = new JLabel(pokemon.getName() + " (Lv. " + pokemon.getLevel() + ")");
                nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                pokemonPanel.add(nameLabel, BorderLayout.WEST);
                
                // Add HP info
                int currentHP = pokemon.getStats().getCurrentHp();
                int maxHP = pokemon.getStats().getMaxHp();
                JProgressBar hpBar = new JProgressBar(0, maxHP);
                hpBar.setValue(currentHP);
                hpBar.setStringPainted(true);
                hpBar.setString("HP: " + currentHP + "/" + maxHP);
                
                // Set color based on HP percentage
                float percentage = (float) currentHP / maxHP;
                if (percentage < 0.2) {
                    hpBar.setForeground(Color.RED);
                } else if (percentage < 0.5) {
                    hpBar.setForeground(Color.ORANGE);
                } else {
                    hpBar.setForeground(new Color(96, 192, 96));
                }
                
                pokemonPanel.add(hpBar, BorderLayout.CENTER);
                
                // Add select button
                JButton selectButton = new JButton("Select");
                selectButton.addActionListener(e -> {
                    performSwitch(pokemon);
                    mainPanel.remove(pokemonSelectionPanel);
                    mainPanel.add(infoPanel, BorderLayout.SOUTH);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
                pokemonPanel.add(selectButton, BorderLayout.EAST);
                
                pokemonSelectionPanel.add(pokemonPanel);
                pokemonSelectionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            
            // Add back button
            JButton backButton = new JButton("Back");
            backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            backButton.addActionListener(e -> {
                mainPanel.remove(pokemonSelectionPanel);
                mainPanel.add(actionPanel, BorderLayout.SOUTH);
                mainPanel.revalidate();
                mainPanel.repaint();
            });
            
            pokemonSelectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            pokemonSelectionPanel.add(backButton);
            
            // Show the Pokémon selection panel
            mainPanel.remove(actionPanel);
            mainPanel.add(pokemonSelectionPanel, BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
    
    // Helper method to perform the actual switch
    private void performSwitch(Pokemon newPokemon) {
        // Show switch message
        showInfoPanel(playerPokemon.getName() + ", come back!");
        
        // Create animation sequence for switching
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        // Update the player's active Pokémon
                        playerPokemon = newPokemon;
                        battleMessageLabel.setText("Go, " + playerPokemon.getName() + "!");
                        
                        // Update the player Pokémon sprite and info
                        playerPokemonImage.setIcon(loadPokemonImage(playerPokemon, false));
                        playerPokemonInfo.setText(playerPokemon.getName() + " L" + playerPokemon.getLevel());
                        
                        // Update HP bar
                        playerPokemonHP.setMaximum(playerPokemon.getStats().getMaxHp());
                        playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
                        hpValueLabel.setText(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
                        
                        // Recreate move panel with new Pokémon's moves
                        createMovePanel();
                        
                        // Update color based on HP percentage
                        float percentage = (float) playerPokemon.getStats().getCurrentHp() / 
                                          playerPokemon.getStats().getMaxHp();
                        if (percentage < 0.2) {
                            playerPokemonHP.setForeground(Color.RED);
                        } else if (percentage < 0.5) {
                            playerPokemonHP.setForeground(Color.ORANGE);
                        } else {
                            playerPokemonHP.setForeground(new Color(96, 192, 96));
                        }
                        break;
                    case 2:
                        // Wild Pokémon's turn after switching
                        playerTurn = false;
                        wildPokemonAttack();
                        break;
                    case 3:
                        if (playerPokemon.getStats().getCurrentHp() <= 0) {
                            // Player Pokémon fainted
                            battleMessageLabel.setText(playerPokemon.getName() + " fainted!");
                            
                            // Check if player has any usable Pokémon left
                            boolean hasUsablePokemon = false;
                            for (Pokemon p : player.getTeam()) {
                                if (p.getStats().getCurrentHp() > 0) {
                                    hasUsablePokemon = true;
                                    break;
                                }
                            }
                            
                            if (hasUsablePokemon) {
                                // Force player to switch again
                                Timer forceSwitch = new Timer(1500, event -> switchPokemon());
                                forceSwitch.setRepeats(false);
                                forceSwitch.start();
                            } else {
                                // All Pokémon fainted, battle is over
                                battleEnded = true;
                                battleMessageLabel.setText("You have no usable Pokémon left!");
                                
                                Timer closeTimer = new Timer(2000, event -> dispose());
                                closeTimer.setRepeats(false);
                                closeTimer.start();
                            }
                        } else {
                            // Return to action selection
                            battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                            animationTimer.stop();
                            animationStep = 0;
                            playerTurn = true;
                            
                            mainPanel.remove(infoPanel);
                            mainPanel.add(actionPanel, BorderLayout.SOUTH);
                            mainPanel.revalidate();
                            mainPanel.repaint();
                        }
                        break;
                }
            }
        });
        
        // Reset animation step and start timer
        animationStep = 0;
        animationTimer.start();
    }
    
    private void attemptRun() {
        showInfoPanel("Got away safely!");
        
        // Create a simple timer before closing the battle screen
        Timer runTimer = new Timer(2000, e -> dispose());
        runTimer.setRepeats(false);
        runTimer.start();
    }
    
    private ImageIcon loadPokemonImage(Pokemon pokemon, boolean isWild) {
        BufferedImage image = null;
        int dexNumber = pokemon.getDex();
        
        try {
            // Construct the path to the Pokémon sprite
            String spritePath = "sprites/sprites/pokemon/";
            
            // Add suffix for back sprites (player's Pokémon)
            if (!isWild) {
                spritePath += "/back";
            }
            
            // Add shiny folder if the Pokémon is shiny
            if (pokemon.getIsShiny()) {
                spritePath += "/shiny";
            }
            
            // Add the file name (usually 1.png for the default sprite)
            spritePath += "/" + dexNumber + ".png";
            
            // Try to load the image
            File file = new File(spritePath);
            if (file.exists()) {
                image = javax.imageio.ImageIO.read(file);
            }
            
            // If image was loaded successfully, scale it to appropriate size
            if (image != null) {
                // Scale the image to fit the battle screen
                int width = 150;
                int height = 150;
                
                BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(image, 0, 0, width, height, null);
                g2d.dispose();
                
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading Pokémon sprite: " + e.getMessage());
        }
        
        // Fallback to placeholder if sprite loading fails
        return createPlaceholderImage(pokemon, isWild);
    }
    
    private ImageIcon createPlaceholderImage(Pokemon pokemon, boolean isWild) {
        int width = 150;
        int height = 150;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set color based on Pokémon type
        Color mainColor = Color.GRAY;
        if (!pokemon.getTypes().isEmpty()) {
            mainColor = UIComponentFactory.getColorForType(pokemon.getTypes().get(0));
        }
        
        // Draw a simple shape
        g2d.setColor(mainColor);
        if (isWild) {
            g2d.fillOval(0, 0, width, height);
        } else {
            g2d.fillRect(0, 0, width, height);
        }
        
        // Add Pokémon name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(pokemon.getName(), 10, height / 2);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    
}
