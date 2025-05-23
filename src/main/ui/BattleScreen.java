package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import model.Item;
import model.Move;
import model.Player;
import model.Pokeball;
import pokes.LevelManager;
import pokes.Pokemon;
import pokes.TypeEffectivenessChart;

public class BattleScreen extends JFrame {
    private Player player;
    private Pokemon wildPokemon;
    private Pokemon playerPokemon;
    private boolean isWildBattle;
    private String battleLocation;
    
    // UI Components
    private Image routeBackgroundImage;
    private static final int BG_WIDTH = 800;
    private static final int BG_HEIGHT = 475;

    private JPanel mainPanel;
    private JPanel currentCategoryPanel;
    private JPanel battlegroundPanel;
    private JPanel actionPanel;
    private JPanel movePanel;
    private JPanel infoPanel;
    
    private JLabel wildPokemonImage;
    private JLabel playerPokemonImage;
    private JLabel wildPokemonInfo;
    private JLabel playerPokemonInfo;
    private JLabel battleMessageLabel;
    private JProgressBar playerExpBar;
    
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

    private Deque<String> messageQueue = new LinkedList<>();
    private boolean isDisplayingMessages = false;
    
    public BattleScreen(Player player, Pokemon wildPokemon, boolean isWildbattle, String battleLocation) {
        this.player = player;
        this.wildPokemon = wildPokemon;
        this.isWildBattle = isWildbattle;

        this.battleLocation = battleLocation != null ? battleLocation : "route"; // Default to route
    
        if ("route".equalsIgnoreCase(this.battleLocation)) {
            try {
                // Load the image and scale it once to the desired size
                Image originalImage = ImageIO.read(getClass().getResource("/resources/backgrounds/route_bg.png"));
                routeBackgroundImage = originalImage.getScaledInstance(BG_WIDTH, BG_HEIGHT, Image.SCALE_SMOOTH);
            } catch (Exception e) {
                System.err.println("Failed to load route background image: " + e.getMessage());
                routeBackgroundImage = null;
            }
        }
            
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
        
        this.battleMessageLabel = new JLabel("A wild " + wildPokemon.getName() + " appeared!");
        this.battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        this.battleMessageLabel.setHorizontalAlignment(JLabel.CENTER);

        // Initialize UI components
        initializeUI();
        
        // Start battle sequence
        startBattleSequence();
    }
    
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        
        createBattlegroundPanel();
        
        createActionPanel();
        
        createMovePanel();
        
        createInfoPanel();
        
        // Add panels to main panel
        mainPanel.add(battlegroundPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void createBattlegroundPanel() {
        if ("route".equalsIgnoreCase(battleLocation) && routeBackgroundImage != null) {
            // Create a custom panel that draws the background image at fixed size
            battlegroundPanel = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the background image at fixed size
                    g.drawImage(routeBackgroundImage, 0, 0, BG_WIDTH, BG_HEIGHT, this);
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(BG_WIDTH, BG_HEIGHT);
                }
            };
        }else {
            // Create a standard panel with color background for other locations
            battlegroundPanel = new JPanel(null);
            
            // Set background color based on location
            switch (battleLocation.toLowerCase()) {
                case "city":
                    battlegroundPanel.setBackground(new Color(180, 210, 230)); // Light blue for city
                    break;
                case "cave":
                    battlegroundPanel.setBackground(new Color(100, 100, 120)); // Dark gray for cave
                    break;
                case "route":
                default:
                    battlegroundPanel.setBackground(new Color(144, 238, 144)); // Light green for routes
                    break;
            }
        }
        
        battlegroundPanel.setPreferredSize(new Dimension(800, 400));
        
        // Wild Pokémon sprite - positioned on top of enemy platform
        wildPokemonImage = new JLabel();
        wildPokemonImage.setBounds(450, 140, 150, 150);
        wildPokemonImage.setIcon(loadPokemonImage(wildPokemon, true));
        battlegroundPanel.add(wildPokemonImage);

        // Player Pokémon sprite - positioned on top of player platform
        playerPokemonImage = new JLabel();
        playerPokemonImage.setBounds(175, 190, 150, 150);
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
        playerInfoBox.setBounds(475, 300, 250, 80);
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

        hpValueLabel = new JLabel(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
        hpValueLabel.setBounds(130, 60, 100, 15);
        hpValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        hpValueLabel.setHorizontalAlignment(JLabel.RIGHT);
        playerInfoBox.add(hpValueLabel);

        // Add experience bar below the HP bar
        playerExpBar = new JProgressBar();
        playerExpBar.setBounds(50, 55, 150, 5); // Position it just below the HP bar, make it thinner
        playerExpBar.setForeground(new Color(30, 144, 255)); // Medium blue color
        playerExpBar.setBackground(new Color(224, 224, 224));
        playerExpBar.setBorderPainted(false);
        playerExpBar.setStringPainted(false);

        int currentLevelExp = playerPokemon.getLevelManager().getCurrentLevelExp();
        int expToNextLevel = playerPokemon.getLevelManager().getExpToNextLevel();
        
        // Update the progress bar
        playerExpBar.setMinimum(0);
        playerExpBar.setMaximum(currentLevelExp + expToNextLevel);
        playerExpBar.setValue(currentLevelExp);

        playerInfoBox.add(playerExpBar);
        battlegroundPanel.add(playerInfoBox);
    }

    private void awardExperience() {
        if (!isWildBattle) {
            queueMessage("Gained experience points!");
        }
        
        // Count how many Pokémon participated in the battle
        int participantCount = 1;
        
        // Calculate experience gain
        int expGain = LevelManager.calculateExpGain(wildPokemon, participantCount, isWildBattle);
        
        // Show experience gain message
        queueMessage(playerPokemon.getName() + " gained " + expGain + " EXP. Points!");
        
        // Award experience to the player's Pokémon
        boolean leveledUp = playerPokemon.gainExperience(expGain);
        
        // Update the experience bar
        updatePlayerExpBar();
        
        // If the Pokémon leveled up, show a level up message
        if (leveledUp) {
            queueMessage(playerPokemon.getName() + " grew to level " + playerPokemon.getLevel() + "!");
            
            // Update the player Pokémon info display
            playerPokemonInfo.setText(playerPokemon.getName() + " L" + playerPokemon.getLevel());
            
            // Update HP display since max HP might have increased
            playerPokemonHP.setMaximum(playerPokemon.getStats().getMaxHp());
            playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
            hpValueLabel.setText(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
        }
    }
    
    private void createActionPanel() {
        actionPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // Changed to direct GridLayout
        actionPanel.setPreferredSize(new Dimension(800, 150)); // Reduced height to match move panel
        actionPanel.setBackground(new Color(100, 100, 100));
        actionPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Added padding around buttons
        
        fightButton = createActionButton("FIGHT", new Color(240, 80, 80));
        bagButton = createActionButton("BAG", new Color(80, 80, 240));
        pokemonButton = createActionButton("POKÉMON", new Color(80, 200, 80));
        runButton = createActionButton("RUN", new Color(240, 240, 80));
        
        fightButton.addActionListener(e -> showMovePanel());
        bagButton.addActionListener(e -> openBag());
        pokemonButton.addActionListener(e -> switchPokemon());
        runButton.addActionListener(e -> attemptRun());
        
        actionPanel.add(fightButton);
        actionPanel.add(bagButton);
        actionPanel.add(pokemonButton);
        actionPanel.add(runButton);
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20)); // Increased from 18
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        
        // Set preferred size to make buttons larger
        button.setPreferredSize(new Dimension(150, 60)); // Added explicit size
        
        return button;
    }
    
    private void createMovePanel() {
        // Create the main move panel with the same structure as action panel
        movePanel = new JPanel(new BorderLayout());
        movePanel.setPreferredSize(new Dimension(800, 150));
        movePanel.setBackground(new Color(100, 100, 100));
        movePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create a grid panel for the moves (2x2 like action panel)
        JPanel movesGrid = new JPanel(new GridLayout(2, 2, 5, 5));
        movesGrid.setBackground(new Color(100, 100, 100));
        
        // Create move buttons based on player Pokémon's moves
        for (int i = 0; i < 4; i++) {
            if (i < playerPokemon.getMoves().size() && playerPokemon.getMoves().get(i) != null) {
                Move move = playerPokemon.getMoves().get(i);
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
        bottomPanel.setBackground(new Color(100, 100, 100)); // Match background
        bottomPanel.setPreferredSize(new Dimension(800, 30)); // Fixed height for bottom panel
        
        // Add PP display on the left
        JPanel ppPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ppPanel.setBackground(new Color(100, 100, 100));
        ppLabel = new JLabel("PP: --/--");
        ppLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ppLabel.setForeground(Color.WHITE); // Make text visible on dark background
        ppPanel.add(ppLabel);
        
        // Add back button on the right
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setBackground(new Color(100, 100, 100));
        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setPreferredSize(new Dimension(80, 25)); // Smaller back button
        backButton.addActionListener(e -> showActionPanel());
        backPanel.add(backButton);
        
        // Add panels to bottom panel
        bottomPanel.add(ppPanel, BorderLayout.WEST);
        bottomPanel.add(backPanel, BorderLayout.EAST);
        
        // Add components to main move panel
        movePanel.add(movesGrid, BorderLayout.CENTER);
        movePanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JButton createMoveButton(Move move) {
        JButton button = new JButton();
        
        if (move != null) {
            button.setText(move.getName());
            
            // Set button color based on move type
            Color typeColor = UIComponentFactory.getColorForType(move.getType());
            button.setBackground(typeColor);
            button.setForeground(Color.WHITE);
            
            // Add mouse listeners for PP display
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    // Update PP display when mouse enters button
                    ppLabel.setText("PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    // Reset PP display when mouse exits button
                    ppLabel.setText("PP: --/--");
                }
            });
            
            // Remove the tooltip since we're using the PP label instead
            button.setToolTipText(null);
        } else {
            button.setText("-");
            button.setBackground(Color.GRAY);
            
            // Add mouse listener for empty buttons too
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ppLabel.setText("PP: --/--");
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    ppLabel.setText("PP: --/--");
                }
            });
        }
        
        button.setFont(new Font("Arial", Font.BOLD, 20)); // Match action button font size
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        button.setPreferredSize(new Dimension(150, 60)); // Match action button size
        
        return button;
    }
    
    private void createInfoPanel() {
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(800, 100));
        infoPanel.setBorder(new LineBorder(Color.BLACK, 2));
        infoPanel.setBackground(Color.WHITE);
        
        // Don't create a new label here, just set the text on the existing one
        if (battleMessageLabel == null) {
            battleMessageLabel = new JLabel("A wild " + wildPokemon.getName() + " appeared!");
        } else {
            battleMessageLabel.setText("A wild " + wildPokemon.getName() + " appeared!");
        }
        
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
        switchToPanel(infoPanel);
    }
    
    
    private void useMove(Move move) {
        if (!playerTurn || battleEnded) return;
    
        playerTurn = false;
        
        if (move.getCurrentPP() <= 0) {
            showInfoPanel("No PP left for " + move.getName() + "!");
            
            // Create a proper timer to show the message for 2 seconds, then let wild Pokémon attack
            animationTimer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Stop the timer
                    animationTimer.stop();
                    
                    // Reset animation step
                    animationStep = 0;
                    
                    // Now let the wild Pokémon attack
                    wildPokemonAttack();
                }
            });
            
            // Start the timer
            animationTimer.start();
            return;
        }
        
        // Decrease PP only if the move has PP
        move.decreasePP();
        
        // Show attack message
        showInfoPanel(playerPokemon.getName() + " used " + move.getName() + "!");
        
        // Calculate damage
        int damage = calculateDamage(playerPokemon, wildPokemon, move);
        double typeEffectiveness = calculateTypeEffectiveness(move.getType(), wildPokemon.getTypes());
        
        // Create animation sequence for attack
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        // Show effectiveness message
                        if (typeEffectiveness > 1.9) {
                            battleMessageLabel.setText("It's super effective!");
                        } else if (typeEffectiveness < 0.1) {
                            battleMessageLabel.setText("It has no effect...");
                        } else if (typeEffectiveness < 0.6) {
                            battleMessageLabel.setText("It's not very effective...");
                        }
                        break;
                    case 2:
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
                    case 3:
                        // Check if wild Pokémon fainted
                        if (wildPokemon.getStats().getCurrentHp() <= 0) {
                            battleMessageLabel.setText("The wild " + wildPokemon.getName() + " fainted!");
                            battleEnded = true;
                            
                            // Award experience if the wild Pokémon fainted
                            Timer expTimer = new Timer(1500, event -> awardExperience());
                            updatePlayerExpBar();
                            expTimer.setRepeats(false);
                            expTimer.start();
                        } else {
                            // Wild Pokémon's turn - IMPORTANT: Stop the timer before calling wildPokemonAttack
                            animationTimer.stop();
                            wildPokemonAttack();
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
            queueMessage("The wild " + wildPokemon.getName() + " used Tackle!");
            
            // Calculate a simple damage
            int damage = 5 + new Random().nextInt(5);
            
            // Apply damage to player's Pokémon
            playerPokemon.damage(damage);
            updatePlayerPokemonHP();
            
            // Show damage message
            queueMessage("It dealt " + damage + " damage!");
        } else {
            // Select a random move
            Move selectedMove = wildMoves.get(new Random().nextInt(wildMoves.size()));
            
            queueMessage("The wild " + wildPokemon.getName() + " used " + selectedMove.getName() + "!");
            
            // Calculate damage
            int damage = calculateDamage(wildPokemon, playerPokemon, selectedMove);
            
            // Apply damage to player's Pokémon
            playerPokemon.damage(damage);
            updatePlayerPokemonHP();
            
            // Show damage message
            queueMessage("It dealt " + damage + " damage!");
        }
        
        // IMPORTANT: Instead of creating a new timer, continue with the main animation sequence
        // This prevents the recursion issue
        
        // Check if player Pokémon fainted
        if (playerPokemon.getStats().getCurrentHp() <= 0) {
            // Player Pokémon fainted
            queueMessage(playerPokemon.getName() + " fainted!");
            
            // Check if player has any usable Pokémon left
            boolean hasUsablePokemon = false;
            for (Pokemon p : player.getTeam()) {
                if (p != playerPokemon && p.getStats().getCurrentHp() > 0) {
                    hasUsablePokemon = true;
                    break;
                }
            }
            
            if (hasUsablePokemon) {
                // Force player to switch
                Timer forceSwitch = new Timer(1500, event -> {
                    switchPokemon();
                });
                forceSwitch.setRepeats(false);
                forceSwitch.start();
            } else {
                // All Pokémon fainted, battle is over
                battleEnded = true;
                queueMessage("You have no usable Pokémon left!");
                
                Timer closeTimer = new Timer(2000, event -> dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        } else {
            // Return to action selection after a delay
            Timer returnTimer = new Timer(1500, event -> {
                battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                
                switchToPanel(actionPanel);
                
                animationStep = 0;
                playerTurn = true;
            });
            returnTimer.setRepeats(false);
            returnTimer.start();
        }
        
    }

    private void switchToPanel(JPanel newPanel) {
        // Remove all panels from the SOUTH position
        for (Component comp : mainPanel.getComponents()) {
            if (mainPanel.getLayout() instanceof BorderLayout) {
                Object constraints = ((BorderLayout)mainPanel.getLayout()).getConstraints(comp);
                if (constraints != null && constraints.equals(BorderLayout.SOUTH)) {
                    mainPanel.remove(comp);
                    break;
                }
            }
        }
        
        // Add the new panel
        mainPanel.add(newPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    
    private void updatePlayerExpBar() {
        int currentLevelExp = playerPokemon.getLevelManager().getCurrentLevelExp();
        int expToNextLevel = playerPokemon.getLevelManager().getExpToNextLevel();
        
        // Update the progress bar
        playerExpBar.setMinimum(0);
        playerExpBar.setMaximum(currentLevelExp + expToNextLevel);
        playerExpBar.setValue(currentLevelExp);
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
        // Use the TypeEffectivenessChart to get the effectiveness
        return TypeEffectivenessChart.getInstance().getEffectiveness(moveType, defenderTypes);
    }

    private void openBag() {
        // Create the main bag panel
        JPanel bagPanel = createMainBagPanel();
        
        // Show the bag panel
        mainPanel.remove(actionPanel);
        mainPanel.add(bagPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private JPanel createMainBagPanel() {
        // Create a panel for the bag interface
        JPanel bagPanel = new JPanel(new BorderLayout());
        bagPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bagPanel.setBackground(new Color(100, 100, 100));
        bagPanel.setPreferredSize(new Dimension(800, 150));
        
        // Create a panel for category buttons with centered layout
        JPanel categoryPanel = new JPanel(new GridBagLayout());
        categoryPanel.setBackground(new Color(100, 100, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Add Medicine button with rounded edges
        JButton medicineButton = createCategoryButton("Medicine", "/resources/button_icons/potion.png");
        medicineButton.addActionListener(e -> showItemCategory("Medicine"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        categoryPanel.add(medicineButton, gbc);
        
        // Add Poké Balls button (only for wild battles)
        if (isWildBattle) {
            JButton pokeballsButton = createCategoryButton("Poké Balls", "/resources/button_icons/pokeball.png");
            pokeballsButton.addActionListener(e -> showItemCategory("Poké Balls"));
            gbc.gridx = 1;
            gbc.gridy = 0;
            categoryPanel.add(pokeballsButton, gbc);
            
            // Add Berries button
            JButton berriesButton = createCategoryButton("Berries", "/resources/button_icons/berry.png");
            berriesButton.addActionListener(e -> showItemCategory("Berries"));
            gbc.gridx = 2;
            gbc.gridy = 0;
            categoryPanel.add(berriesButton, gbc);
        } else {
            JButton berriesButton = createCategoryButton("Berries", "/resources/button_icons/berry.png");
            berriesButton.addActionListener(e -> showItemCategory("Berries"));
            gbc.gridx = 1;
            gbc.gridy = 0;
            categoryPanel.add(berriesButton, gbc);
        }
        
        // Create bottom panel for back button positioning
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(100, 100, 100));
        
        // Add back button to bottom right corner
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButtonPanel.setBackground(new Color(100, 100, 100));
        
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(e -> switchToPanel(actionPanel));
        
        backButtonPanel.add(backButton);
        bottomPanel.add(backButtonPanel, BorderLayout.EAST);
        
        // Add components to main bag panel
        bagPanel.add(categoryPanel, BorderLayout.CENTER);
        bagPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return bagPanel;
    }
    
    private JButton createCategoryButton(String text, String path) {
        RoundedImageButton button = new RoundedImageButton(path);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 80));
        button.setMinimumSize(new Dimension(120, 80));
        button.setMaximumSize(new Dimension(120, 80));
        
        return button;
    }
    
    private void showItemCategory(String category) {
        // Get player's inventory
        Set<Item> inventory = player.getInventory();
        
        // Filter items by category
        List<Item> categoryItems = new ArrayList<>();
        for (Item item : inventory) {
            String itemType = getItemType(item);
            if (itemType.equals(category)) {
                categoryItems.add(item);
            }
        }
        
        // Create a panel for the category items
        currentCategoryPanel = new JPanel(new BorderLayout());
        currentCategoryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        currentCategoryPanel.setBackground(new Color(248, 248, 240));
        
        // Add a title label
        JLabel titleLabel = new JLabel(category);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        currentCategoryPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create a panel for the items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (categoryItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("No " + category + " available", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            itemsPanel.add(emptyLabel);
        } else {
            // Add each item to the panel
            for (Item item : categoryItems) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                
                // Item image on the left
                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (item.getImage() != null) {
                            Image img = item.getImage();
                            int size = Math.min(getWidth(), getHeight()) - 10;
                            g.drawImage(img, (getWidth() - size)/2, (getHeight() - size)/2, size, size, this);
                        }
                    }
                };
                imagePanel.setPreferredSize(new Dimension(50, 50));
                
                // Item name and quantity
                JLabel nameLabel = new JLabel(item.getName() + " x" + item.getQuantity());
                nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                
                // Use button
                JButton useButton = new JButton("Use");
                useButton.addActionListener(e -> {
                    useItem(item);
                    
                    // Find the category panel and remove it safely
                    mainPanel.remove(currentCategoryPanel);
                    mainPanel.add(infoPanel, BorderLayout.SOUTH);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
                
                // Add components to the item panel
                itemPanel.add(imagePanel, BorderLayout.WEST);
                itemPanel.add(nameLabel, BorderLayout.CENTER);
                itemPanel.add(useButton, BorderLayout.EAST);
                
                // Add to the items panel with some spacing
                itemsPanel.add(itemPanel);
                itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        currentCategoryPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add back button
        JButton backButton = new JButton("Back to Bag");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            // Remove the current category panel first
            mainPanel.remove(currentCategoryPanel);
            
            // Then create a new bag panel
            JPanel bagPanel = createMainBagPanel();
            
            // Add the bag panel to the main panel
            mainPanel.add(bagPanel, BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        currentCategoryPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show the category panel
        // First, remove whatever is currently in the SOUTH position
        for (Component comp : mainPanel.getComponents()) {
            if (mainPanel.getLayout() instanceof BorderLayout) {
                Object constraints = ((BorderLayout)mainPanel.getLayout()).getConstraints(comp);
                if (constraints != null && constraints.equals(BorderLayout.SOUTH)) {
                    mainPanel.remove(comp);
                    break;
                }
            }
        }
        
        mainPanel.add(currentCategoryPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void queueMessage(String message) {
        messageQueue.add(message);
        if (!isDisplayingMessages) {
            displayNextMessage();
        }
    }

    private void displayNextMessage() {
        if (messageQueue.isEmpty()) {
            isDisplayingMessages = false;
            return;
        }
        
        isDisplayingMessages = true;
        String message = messageQueue.poll();
        battleMessageLabel.setText(message);
        
        // Display each message for 1.5 seconds
        Timer messageTimer = new Timer(1500, e -> displayNextMessage());
        messageTimer.setRepeats(false);
        messageTimer.start();
    }


    private String getItemType(Item item) {
        String name = item.getName().toLowerCase();
        
        if (name.contains("ball") && !name.contains("berry")) {
            return "Poké Balls";
        } else if (name.contains("potion") || name.contains("revive") || 
                   name.contains("heal") || name.contains("ether") || 
                   name.contains("elixir") || name.contains("medicine")) {
            return "Medicine";
        } else if (name.contains("berry")) {
            return "Berries";
        } else if (name.contains("rod") || name.contains("key") || 
                   name.contains("ticket") || name.contains("card") || 
                   name.contains("scope") || name.contains("flute")) {
            return "Key Items";
        } else {
            return "Items";
        }
    }    

    private void useItem(Item item) {
        String itemName = item.getName().toLowerCase();
        
        // Check if this is a Poké Ball and we're in a trainer battle
        if (!isWildBattle && itemName.contains("ball") && !itemName.contains("berry")) {
            showInfoPanel("You can't use Poké Balls in a trainer battle!");
            
            Timer returnTimer = new Timer(1500, e -> {
                openBag(); // Reopen the bag
            });
            returnTimer.setRepeats(false);
            returnTimer.start();
            return;
        }
        
        // Show item use message
        showInfoPanel("Used " + item.getName() + "!");
        
        // Process item effects based on item type
        boolean itemUsed = false;
        
        if (itemName.contains("ball") && !itemName.contains("berry")) {
            // Pokéball - attempt to catch the wild Pokémon
            attemptCatch((Pokeball) item);
            itemUsed = true;
        } else if (itemName.contains("potion") || itemName.contains("heal")) {
            // Potion - heal the active Pokémon
            int healAmount = 20; // Basic potion
            
            if (itemName.contains("super")) {
                healAmount = 50;
            } else if (itemName.contains("hyper")) {
                healAmount = 200;
            } else if (itemName.contains("max")) {
                healAmount = playerPokemon.getStats().getMaxHp();
            }
            
            int currentHP = playerPokemon.getStats().getCurrentHp();
            int maxHP = playerPokemon.getStats().getMaxHp();
            int newHP = Math.min(currentHP + healAmount, maxHP);
            
            playerPokemon.getStats().setCurrentHp(newHP);
            updatePlayerPokemonHP();
            itemUsed = true;
            
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
        } else if (itemName.contains("berry")) {
            // Berry effects
            if (itemName.contains("oran")) {
                // Heal 10 HP
                int currentHP = playerPokemon.getStats().getCurrentHp();
                int maxHP = playerPokemon.getStats().getMaxHp();
                int newHP = Math.min(currentHP + 10, maxHP);
                
                playerPokemon.getStats().setCurrentHp(newHP);
                updatePlayerPokemonHP();
                itemUsed = true;
            } else if (itemName.contains("sitrus")) {
                // Heal 25% of max HP
                int maxHP = playerPokemon.getStats().getMaxHp();
                int healAmount = maxHP / 4;
                int currentHP = playerPokemon.getStats().getCurrentHp();
                int newHP = Math.min(currentHP + healAmount, maxHP);
                
                playerPokemon.getStats().setCurrentHp(newHP);
                updatePlayerPokemonHP();
                itemUsed = true;
            }
            
            // Create animation sequence for berry effects
            animationTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationStep++;
                    
                    switch (animationStep) {
                        case 1:
                            battleMessageLabel.setText(playerPokemon.getName() + " ate the " + item.getName() + "!");
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
        }
        
        // Decrease item quantity if used
        if (itemUsed) {
            item.decreaseQuantity();
            if (item.getQuantity() <= 0) {
                player.removeItem(item);
            }
        }
    }

    private void attemptCatch(Pokeball pokeball) {
        // Calculate catch rate
        double catchRate = pokeball.getCatchRate();
        
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
                    switchToPanel(actionPanel);
                });
                returnTimer.setRepeats(false);
                returnTimer.start();
            });
            switchTimer.setRepeats(false);
            switchTimer.start();
        } else {
            // Create the Pokémon selection panel
            JPanel pokemonSelectionPanel = createPokemonSelectionPanel();
            switchToPanel(pokemonSelectionPanel);
        }
    }

    private JPanel createPokemonSelectionPanel() {
        // Create a panel for the Pokémon selection
        JPanel pokemonSelectionPanel = new JPanel(new BorderLayout());
        pokemonSelectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pokemonSelectionPanel.setBackground(new Color(248, 248, 240));
        
        // Add a title label
        JLabel titleLabel = new JLabel("Choose a Pokémon");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        pokemonSelectionPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create a panel for the Pokémon list
        JPanel pokemonListPanel = new JPanel();
        pokemonListPanel.setLayout(new BoxLayout(pokemonListPanel, BoxLayout.Y_AXIS));
        pokemonListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add each Pokémon to the panel
        for (Pokemon p : player.getTeam()) {
            // Skip the current active Pokémon
            if (p == playerPokemon) {
                continue;
            }
            
            // Skip fainted Pokémon
            if (p.getStats().getCurrentHp() <= 0) {
                continue;
            }
            
            JPanel pokemonPanel = new JPanel(new BorderLayout());
            pokemonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            pokemonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            
            // Pokémon info (name, level, HP)
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            JLabel nameLabel = new JLabel(p.getName() + " Lv." + p.getLevel());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            JProgressBar hpBar = new JProgressBar(0, p.getStats().getMaxHp());
            hpBar.setValue(p.getStats().getCurrentHp());
            hpBar.setString("HP: " + p.getStats().getCurrentHp() + "/" + p.getStats().getMaxHp());
            hpBar.setStringPainted(true);
            
            // Set color based on HP percentage
            float percentage = (float) p.getStats().getCurrentHp() / p.getStats().getMaxHp();
            if (percentage < 0.2) {
                hpBar.setForeground(Color.RED);
            } else if (percentage < 0.5) {
                hpBar.setForeground(Color.ORANGE);
            } else {
                hpBar.setForeground(new Color(96, 192, 96));
            }
            
            infoPanel.add(nameLabel);
            infoPanel.add(hpBar);
            
            // Switch button
            JButton switchButton = new JButton("Switch");
            switchButton.addActionListener(e -> {
                // Store reference to the selected Pokémon
                final Pokemon selectedPokemon = p;
                
                // Call performSwitch with the selected Pokémon
                performSwitch(selectedPokemon);
            });
            
            // Add components to the Pokémon panel
            pokemonPanel.add(infoPanel, BorderLayout.CENTER);
            pokemonPanel.add(switchButton, BorderLayout.EAST);
            
            // Add to the list panel with some spacing
            pokemonListPanel.add(pokemonPanel);
            pokemonListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(pokemonListPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        pokemonSelectionPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add back button
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            switchToPanel(actionPanel);
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        pokemonSelectionPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return pokemonSelectionPanel;
    }
    
    
    private void performSwitch(Pokemon newPokemon) {
        // First, ensure we remove any existing panels from the SOUTH position
        switchToPanel(infoPanel);
        
        // First message - recall the current Pokémon
        battleMessageLabel.setText("Come back " + playerPokemon.getName() + "!");
        
        // Create animation sequence for switching with proper timing
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        // Update the player's active Pokémon
                        playerPokemon = newPokemon;
                        
                        // Second message - send out the new Pokémon
                        battleMessageLabel.setText("Go, " + playerPokemon.getName() + "!");
                        break;
                        
                    case 2:
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
                        
                        // Update exp bar for the new Pokémon
                        updatePlayerExpBar();
                        break;
                        
                    case 3:
                        // Wild Pokémon's turn after switching
                        playerTurn = false;
                        wildPokemonAttack();
                        
                        // Stop the animation timer to prevent recursion
                        animationTimer.stop();
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
