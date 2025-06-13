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
import pokes.Pokemon;
import pokes.TypeEffectivenessChart;

public abstract class BattleScreen extends JFrame {
    protected Player player;
    protected Pokemon currentOpponentPokemon;
    protected Pokemon playerPokemon;
    protected String battleLocation;
    
    // UI Components
    protected Image routeBackgroundImage;
    protected static final int BG_WIDTH = 800;
    protected static final int BG_HEIGHT = 475;

    protected JPanel mainPanel;
    protected JPanel currentCategoryPanel;
    protected JPanel battlegroundPanel;
    protected JPanel actionPanel;
    protected JPanel movePanel;
    protected JPanel infoPanel;
    
    protected JLabel opponentPokemonImage;
    protected JLabel playerPokemonImage;
    protected JLabel opponentPokemonInfo;
    protected JLabel playerPokemonInfo;
    protected JLabel battleMessageLabel;
    protected JProgressBar playerExpBar;
    
    protected JProgressBar opponentPokemonHP;
    protected JProgressBar playerPokemonHP;
    
    protected JButton fightButton;
    protected JButton bagButton;
    protected JButton pokemonButton;
    protected JButton runButton;

    protected JLabel ppLabel;
    protected JLabel hpValueLabel;
    
    protected JButton[] moveButtons = new JButton[4];
    protected JButton backButton;
    
    protected Timer animationTimer;
    protected int animationStep = 0;
    
    // Battle state
    protected boolean playerTurn = true;
    protected boolean battleEnded = false;

    protected Deque<String> messageQueue = new LinkedList<>();
    protected boolean isDisplayingMessages = false;

    // Add these fields to BattleScreen class
    protected PlayerBattleView playerBattleView;
    protected boolean isPlayingThrowAnimation = false;
    protected Timer throwAnimationTimer;
        
    public BattleScreen(Player player, Pokemon initialOpponent, String battleLocation) {
        this.player = player;
        this.currentOpponentPokemon = initialOpponent;
        this.battleLocation = battleLocation != null ? battleLocation : "route";
    
        // Ensure opponent Pokemon has appropriate moves
        if (currentOpponentPokemon.getMoves().isEmpty()) {
            currentOpponentPokemon.generateWildMoves();
        }

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(App.CURRENT_WIDTH, App.CURRENT_HEIGHT);
            
        validateOpponentPokemonMoves();
    
        if ("route".equalsIgnoreCase(this.battleLocation)) {
            try {
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
            JOptionPane.showMessageDialog(this, "You have no usable Pokémon!", "Battle Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Pokémon Battle");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Initialize battleMessageLabel with a default message instead of calling getInitialBattleMessage()
        this.battleMessageLabel = new JLabel("Battle is starting...");
        this.battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        this.battleMessageLabel.setHorizontalAlignment(JLabel.CENTER);
    
        initializeUI();
        startBattleSequence();
    }

    public BattleScreen(Player player, Pokemon initialOpponent, String battleLocation, String initialMessage) {
        this.player = player;
        this.currentOpponentPokemon = initialOpponent;
        this.battleLocation = battleLocation != null ? battleLocation : "route";
    
        // Ensure opponent Pokemon has appropriate moves
        if (currentOpponentPokemon.getMoves().isEmpty()) {
            currentOpponentPokemon.generateWildMoves();
        }
        
        validateOpponentPokemonMoves();
    
        if ("route".equalsIgnoreCase(this.battleLocation)) {
            try {
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
            JOptionPane.showMessageDialog(this, "You have no usable Pokémon!", "Battle Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        setTitle("Pokémon Battle");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Use the provided initial message instead of calling getInitialBattleMessage()
        String message = initialMessage != null ? initialMessage : "Battle is starting...";
        this.battleMessageLabel = new JLabel(message);
        this.battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        this.battleMessageLabel.setHorizontalAlignment(JLabel.CENTER);
    
        initializeUI();
        startBattleSequence();
    }    
    
    // Abstract methods that subclasses must implement
    protected abstract String getInitialBattleMessage();
    protected abstract void handleOpponentFainted();
    protected abstract boolean canUsePokeballs();
    protected abstract boolean canRun();
    protected abstract void handleBattleEnd(boolean playerWon);
    protected abstract void switchOpponentPokemon(Pokemon newPokemon);
    
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        
        createBattlegroundPanel();
        createActionPanel();
        createMovePanel();
        createInfoPanel();
        
        mainPanel.add(battlegroundPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    protected void createBattlegroundPanel() {
        if ("route".equalsIgnoreCase(battleLocation) && routeBackgroundImage != null) {
            battlegroundPanel = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(routeBackgroundImage, 0, 0, BG_WIDTH, BG_HEIGHT, this);
                    
                    // Draw player using PlayerBattleView
                    if (playerBattleView != null) {
                        playerBattleView.draw(g, this);
                    }
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(BG_WIDTH, BG_HEIGHT);
                }
            };
        } else {
            battlegroundPanel = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    
                    // Draw player using PlayerBattleView
                    if (playerBattleView != null) {
                        playerBattleView.draw(g, this);
                    }
                }
            };
            
            switch (battleLocation.toLowerCase()) {
                case "city":
                    battlegroundPanel.setBackground(new Color(180, 210, 230));
                    break;
                case "cave":
                    battlegroundPanel.setBackground(new Color(100, 100, 120));
                    break;
                case "route":
                default:
                    battlegroundPanel.setBackground(new Color(144, 238, 144));
                    break;
            }
        }
        
        battlegroundPanel.setPreferredSize(new Dimension(800, 400));
        
        // Opponent Pokémon sprite
        opponentPokemonImage = new JLabel();
        opponentPokemonImage.setBounds(450, 140, 150, 150);
        opponentPokemonImage.setIcon(loadPokemonImage(currentOpponentPokemon, true));
        battlegroundPanel.add(opponentPokemonImage);
    
        // Player Pokémon sprite - INITIALLY HIDDEN
        playerPokemonImage = new JLabel();
        playerPokemonImage.setBounds(175, 190, 150, 150);
        // DON'T set the icon yet - it will be set after the throwing animation
        playerPokemonImage.setVisible(false); // Start hidden
        battlegroundPanel.add(playerPokemonImage);
        
        // Initialize player battle view
        initializePlayerBattleView();
        
        // Rest of the info boxes setup...
        createInfoBoxes();
    }
    
    // Separate method for creating info boxes to keep code clean
    private void createInfoBoxes() {
        // Opponent Pokémon info box
        JPanel opponentInfoBox = new JPanel(null);
        opponentInfoBox.setBounds(50, 50, 250, 80);
        opponentInfoBox.setBackground(new Color(248, 248, 240));
        opponentInfoBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        opponentPokemonInfo = new JLabel(currentOpponentPokemon.getName() + " L" + currentOpponentPokemon.getLevel());
        opponentPokemonInfo.setBounds(20, 10, 200, 30);
        opponentPokemonInfo.setFont(new Font("Arial", Font.BOLD, 16));
        opponentInfoBox.add(opponentPokemonInfo);
        
        JLabel opponentHpText = new JLabel("HP:");
        opponentHpText.setBounds(20, 40, 30, 15);
        opponentHpText.setFont(new Font("Arial", Font.PLAIN, 12));
        opponentInfoBox.add(opponentHpText);
        
        opponentPokemonHP = new JProgressBar(0, currentOpponentPokemon.getStats().getMaxHp());
        opponentPokemonHP.setValue(currentOpponentPokemon.getStats().getCurrentHp());
        opponentPokemonHP.setBounds(50, 40, 150, 10);
        opponentPokemonHP.setForeground(new Color(96, 192, 96));
        opponentPokemonHP.setBackground(new Color(224, 224, 224));
        opponentPokemonHP.setBorderPainted(false);
        opponentPokemonHP.setStringPainted(false);
        opponentInfoBox.add(opponentPokemonHP);
        
        battlegroundPanel.add(opponentInfoBox);
        
        // Player Pokémon info box
        JPanel playerInfoBox = new JPanel(null);
        playerInfoBox.setBounds(475, 300, 250, 80);
        playerInfoBox.setBackground(new Color(248, 248, 240));
        playerInfoBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        playerPokemonInfo = new JLabel(playerPokemon.getName() + " L" + playerPokemon.getLevel());
        playerPokemonInfo.setBounds(20, 10, 200, 30);
        playerPokemonInfo.setFont(new Font("Arial", Font.BOLD, 16));
        playerInfoBox.add(playerPokemonInfo);
        
        JLabel playerHpText = new JLabel("HP:");
        playerHpText.setBounds(20, 40, 30, 15);
        playerHpText.setFont(new Font("Arial", Font.PLAIN, 12));
        playerInfoBox.add(playerHpText);
        
        playerPokemonHP = new JProgressBar(0, playerPokemon.getStats().getMaxHp());
        playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
        playerPokemonHP.setBounds(50, 40, 150, 10);
        playerPokemonHP.setForeground(new Color(96, 192, 96));
        playerPokemonHP.setBackground(new Color(224, 224, 224));
        playerPokemonHP.setBorderPainted(false);
        playerPokemonHP.setStringPainted(false);
        playerInfoBox.add(playerPokemonHP);
    
        hpValueLabel = new JLabel(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
        hpValueLabel.setBounds(130, 60, 100, 15);
        hpValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        hpValueLabel.setHorizontalAlignment(JLabel.RIGHT);
        playerInfoBox.add(hpValueLabel);
    
        playerExpBar = new JProgressBar();
        playerExpBar.setBounds(50, 55, 150, 5);
        playerExpBar.setForeground(new Color(30, 144, 255));
        playerExpBar.setBackground(new Color(224, 224, 224));
        playerExpBar.setBorderPainted(false);
        playerExpBar.setStringPainted(false);
    
        int currentLevelExp = playerPokemon.getLevelManager().getCurrentLevelExp();
        int expToNextLevel = playerPokemon.getLevelManager().getExpToNextLevel();
        
        playerExpBar.setMinimum(0);
        playerExpBar.setMaximum(currentLevelExp + expToNextLevel);
        playerExpBar.setValue(currentLevelExp);
    
        playerInfoBox.add(playerExpBar);
        battlegroundPanel.add(playerInfoBox);
    }
    
    // Initialize PlayerBattleView
    private void initializePlayerBattleView() {
        int playerBattleX = 175;
        int playerBattleY = 190;
        
        playerBattleView = new PlayerBattleView(player, playerBattleX, playerBattleY);
        playerBattleView.updateDirection(Player.Direction.BACK);
        
        // Ensure player starts hidden
        playerBattleView.hidePlayer();
    }
    
    protected void updateOpponentDisplay(Pokemon newOpponent) {
        this.currentOpponentPokemon = newOpponent;
        
        // Update sprite
        opponentPokemonImage.setIcon(loadPokemonImage(currentOpponentPokemon, true));
        
        // Update info
        opponentPokemonInfo.setText(currentOpponentPokemon.getName() + " L" + currentOpponentPokemon.getLevel());
        
        // Update HP bar
        opponentPokemonHP.setMaximum(currentOpponentPokemon.getStats().getMaxHp());
        opponentPokemonHP.setValue(currentOpponentPokemon.getStats().getCurrentHp());
        opponentPokemonHP.setForeground(new Color(96, 192, 96));
        
        battlegroundPanel.revalidate();
        battlegroundPanel.repaint();
    }
    
    protected void createActionPanel() {
        actionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        actionPanel.setPreferredSize(new Dimension(800, 150));
        actionPanel.setBackground(new Color(100, 100, 100));
        actionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        fightButton = createActionButton("FIGHT", new Color(240, 80, 80));
        bagButton = createActionButton("BAG", new Color(80, 80, 240));
        pokemonButton = createActionButton("POKÉMON", new Color(80, 200, 80));
        runButton = createActionButton("RUN", new Color(240, 240, 80));
        
        fightButton.addActionListener(e -> showMovePanel());
        bagButton.addActionListener(e -> openBag());
        pokemonButton.addActionListener(e -> switchPokemon());
        runButton.addActionListener(e -> attemptRun());
        
        // Disable run button for trainer battles
        runButton.setEnabled(canRun());
        
        actionPanel.add(fightButton);
        actionPanel.add(bagButton);
        actionPanel.add(pokemonButton);
        actionPanel.add(runButton);
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        button.setPreferredSize(new Dimension(150, 60));
        return button;
    }
    
    protected void createMovePanel() {
        movePanel = new JPanel(new BorderLayout());
        movePanel.setPreferredSize(new Dimension(800, 150));
        movePanel.setBackground(new Color(100, 100, 100));
        movePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel movesGrid = new JPanel(new GridLayout(2, 2, 5, 5));
        movesGrid.setBackground(new Color(100, 100, 100));
        
        for (int i = 0; i < 4; i++) {
            if (i < playerPokemon.getMoves().size() && playerPokemon.getMoves().get(i) != null) {
                Move move = playerPokemon.getMoves().get(i);
                moveButtons[i] = createMoveButton(move);
                
                final Move finalMove = move;
                moveButtons[i].addActionListener(e -> useMove(finalMove));
            } else {
                moveButtons[i] = createMoveButton(null);
                moveButtons[i].setEnabled(false);
            }
            movesGrid.add(moveButtons[i]);
        }
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(100, 100, 100));
        bottomPanel.setPreferredSize(new Dimension(800, 30));
        
        JPanel ppPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ppPanel.setBackground(new Color(100, 100, 100));
        ppLabel = new JLabel("PP: --/--");
        ppLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ppLabel.setForeground(Color.WHITE);
        ppPanel.add(ppLabel);
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setBackground(new Color(100, 100, 100));
        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setPreferredSize(new Dimension(80, 25));
        backButton.addActionListener(e -> showActionPanel());
        backPanel.add(backButton);
        
        bottomPanel.add(ppPanel, BorderLayout.WEST);
        bottomPanel.add(backPanel, BorderLayout.EAST);
        
        movePanel.add(movesGrid, BorderLayout.CENTER);
        movePanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JButton createMoveButton(Move move) {
        JButton button = new JButton();
        
        if (move != null) {
            button.setText(move.getName());
            
            Color typeColor = UIComponentFactory.getColorForType(move.getType());
            button.setBackground(typeColor);
            button.setForeground(Color.WHITE);
            
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ppLabel.setText("PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    ppLabel.setText("PP: --/--");
                }
            });
        } else {
            button.setText("-");
            button.setBackground(Color.GRAY);
            
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
        
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.BLACK, 2));
        button.setPreferredSize(new Dimension(150, 60));
        
        return button;
    }
    
    protected void createInfoPanel() {
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(800, 100));
        infoPanel.setBorder(new LineBorder(Color.BLACK, 2));
        infoPanel.setBackground(Color.WHITE);
        
        // Don't call getInitialBattleMessage() here - use the already initialized battleMessageLabel
        if (battleMessageLabel == null) {
            battleMessageLabel = new JLabel("Battle is starting...");
        }
        // Remove the else clause that calls getInitialBattleMessage()
        
        battleMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        battleMessageLabel.setHorizontalAlignment(JLabel.CENTER);
        battleMessageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        infoPanel.add(battleMessageLabel, BorderLayout.CENTER);
    }
    
    protected void startBattleSequence() {
        mainPanel.remove(actionPanel);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        battleMessageLabel.setText(getInitialBattleMessage());
                        break;
                    case 2:
                        battleMessageLabel.setText("Go, " + playerPokemon.getName() + "!");
                        // Play pokeball throwing animation - Pokemon sprite appears AFTER this
                        playPokeballThrowAnimation(() -> {
                            Timer continueTimer = new Timer(500, event -> {
                                battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                                
                                mainPanel.remove(infoPanel);
                                mainPanel.add(actionPanel, BorderLayout.SOUTH);
                                mainPanel.revalidate();
                                mainPanel.repaint();
                            });
                            continueTimer.setRepeats(false);
                            continueTimer.start();
                        });
                        animationTimer.stop();
                        break;
                }
            }
        });
        
        animationTimer.start();
    }
    
    protected void showMovePanel() {
        mainPanel.remove(actionPanel);
        mainPanel.add(movePanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    protected void showActionPanel() {
        mainPanel.remove(movePanel);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    protected void showInfoPanel(String message) {
        battleMessageLabel.setText(message);
        switchToPanel(infoPanel);
    }
    
    protected void useMove(Move move) {
        if (!playerTurn || battleEnded) return;
    
        playerTurn = false;
        
        if (move.getCurrentPP() <= 0) {
            showInfoPanel("No PP left for " + move.getName() + "!");
            
            animationTimer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationTimer.stop();
                    animationStep = 0;
                    opponentPokemonAttack();
                }
            });
            
            animationTimer.start();
            return;
        }
        
        move.decreasePP();
        showInfoPanel(playerPokemon.getName() + " used " + move.getName() + "!");
        
        int damage = calculateDamage(playerPokemon, currentOpponentPokemon, move);
        double typeEffectiveness = calculateTypeEffectiveness(move.getType(), currentOpponentPokemon.getTypes());
        
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        if (typeEffectiveness > 1.9) {
                            battleMessageLabel.setText("It's super effective!");
                        } else if (typeEffectiveness < 0.1) {
                            battleMessageLabel.setText("It has no effect...");
                        } else if (typeEffectiveness < 0.6) {
                            battleMessageLabel.setText("It's not very effective...");
                        }
                        break;
                    case 2:
                        currentOpponentPokemon.damage(damage);
                        updateOpponentPokemonHP();
                        
                        if (damage > 0) {
                            battleMessageLabel.setText("It dealt " + damage + " damage!");
                        } else {
                            battleMessageLabel.setText("It had no effect...");
                        }
                        break;
                    case 3:
                        if (currentOpponentPokemon.getStats().getCurrentHp() <= 0) {
                            battleMessageLabel.setText("The " + currentOpponentPokemon.getName() + " fainted!");
                            battleEnded = true;
                            
                            Timer expTimer = new Timer(1500, event -> handleOpponentFainted());
                            expTimer.setRepeats(false);
                            expTimer.start();
                        } else {
                            animationTimer.stop();
                            opponentPokemonAttack();
                        }
                        break;
                }
            }
        });
        
        animationStep = 0;
        animationTimer.start();
    }
    
    protected void opponentPokemonAttack() {
        List<Move> opponentMoves = currentOpponentPokemon.getMoves();
        List<Move> usableMoves = new ArrayList<>();
        
        for (Move move : opponentMoves) {
            if (move != null && move.getCurrentPP() > 0) {
                usableMoves.add(move);
            }
        }
        
        if (usableMoves.isEmpty()) {
            queueMessage("The " + currentOpponentPokemon.getName() + " used Struggle!");
            
            int damage = Math.max(1, currentOpponentPokemon.getStats().getMaxHp() / 4);
            playerPokemon.damage(damage);
            currentOpponentPokemon.damage(currentOpponentPokemon.getStats().getMaxHp() / 4);
            
            updatePlayerPokemonHP();
            updateOpponentPokemonHP();
            
            queueMessage("It dealt " + damage + " damage!");
            queueMessage("The " + currentOpponentPokemon.getName() + " is hurt by recoil!");
        } else {
            Move selectedMove = selectBestMove(usableMoves);
            selectedMove.decreasePP();
            
            queueMessage("The " + currentOpponentPokemon.getName() + " used " + selectedMove.getName() + "!");
            
            int damage = calculateDamage(currentOpponentPokemon, playerPokemon, selectedMove);
            double typeEffectiveness = calculateTypeEffectiveness(selectedMove.getType(), playerPokemon.getTypes());
            
            playerPokemon.damage(damage);
            updatePlayerPokemonHP();
            
            if (typeEffectiveness > 1.9) {
                queueMessage("It's super effective!");
            } else if (typeEffectiveness < 0.1) {
                queueMessage("It has no effect...");
            } else if (typeEffectiveness < 0.6) {
                queueMessage("It's not very effective...");
            }
            
            queueMessage("It dealt " + damage + " damage!");
        }
        
        checkBattleEnd();
    }
    
    protected void checkBattleEnd() {
        if (currentOpponentPokemon.getStats().getCurrentHp() <= 0) {
            queueMessage("The " + currentOpponentPokemon.getName() + " fainted!");
            handleOpponentFainted();
            return;
        }
        
        if (playerPokemon.getStats().getCurrentHp() <= 0) {
            queueMessage(playerPokemon.getName() + " fainted!");
            
            boolean hasUsablePokemon = false;
            for (Pokemon p : player.getTeam()) {
                if (p != playerPokemon && p.getStats().getCurrentHp() > 0) {
                    hasUsablePokemon = true;
                    break;
                }
            }
            
            if (hasUsablePokemon) {
                queueMessage("Choose your next Pokemon!");
                
                Timer forceSwitch = new Timer(2000, e -> {
                    switchPokemon();
                });
                forceSwitch.setRepeats(false);
                forceSwitch.start();
            } else {
                battleEnded = true;
                handleBattleEnd(false);
            }
            
            return;
        }
        
        if (!battleEnded) {
            Timer returnTimer = new Timer(1500, e -> {
                battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                switchToPanel(actionPanel);
                playerTurn = true;
            });
            returnTimer.setRepeats(false);
            returnTimer.start();
        }
    }
    
    private Move selectBestMove(List<Move> usableMoves) {
        Random random = new Random();
        
        if (random.nextDouble() < 0.7) {
            Move bestMove = usableMoves.get(0);
            for (Move move : usableMoves) {
                if (move.getPower() > bestMove.getPower()) {
                    bestMove = move;
                }
            }
            return bestMove;
        } else {
            return usableMoves.get(random.nextInt(usableMoves.size()));
        }
    }
    
    protected void switchToPanel(JPanel newPanel) {
        for (Component comp : mainPanel.getComponents()) {
            if (mainPanel.getLayout() instanceof BorderLayout) {
                Object constraints = ((BorderLayout)mainPanel.getLayout()).getConstraints(comp);
                if (constraints != null && constraints.equals(BorderLayout.SOUTH)) {
                    mainPanel.remove(comp);
                    break;
                }
            }
        }
        
        mainPanel.add(newPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    protected void updatePlayerExpBar() {
        int currentLevelExp = playerPokemon.getLevelManager().getCurrentLevelExp();
        int expToNextLevel = playerPokemon.getLevelManager().getExpToNextLevel();
        
        playerExpBar.setMinimum(0);
        playerExpBar.setMaximum(currentLevelExp + expToNextLevel);
        playerExpBar.setValue(currentLevelExp);
    }
    
    protected void updateOpponentPokemonHP() {
        int currentHP = currentOpponentPokemon.getStats().getCurrentHp();
        int maxHP = currentOpponentPokemon.getStats().getMaxHp();
        
        currentHP = Math.max(0, currentHP);
        opponentPokemonHP.setValue(currentHP);
        
        float percentage = (float) currentHP / maxHP;
        if (percentage < 0.2) {
            opponentPokemonHP.setForeground(Color.RED);
        } else if (percentage < 0.5) {
            opponentPokemonHP.setForeground(Color.ORANGE);
        }
    }
    
    protected void updatePlayerPokemonHP() {
        int currentHP = playerPokemon.getStats().getCurrentHp();
        int maxHP = playerPokemon.getStats().getMaxHp();
        
        currentHP = Math.max(0, currentHP);
        playerPokemonHP.setValue(currentHP);
        hpValueLabel.setText(currentHP + "/" + maxHP);
        
        float percentage = (float) currentHP / maxHP;
        if (percentage < 0.2) {
            playerPokemonHP.setForeground(Color.RED);
        } else if (percentage < 0.5) {
            playerPokemonHP.setForeground(Color.ORANGE);
        } else {
            playerPokemonHP.setForeground(new Color(96, 192, 96));
        }
    }

    protected int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        int attackStat = (move.getCategory() == Move.MoveCategory.PHYSICAL) ? 
                          attacker.getStats().getAttack() : 
                          attacker.getStats().getSpecialAtk();
        
        int defenseStat = (move.getCategory() == Move.MoveCategory.PHYSICAL) ? 
                           defender.getStats().getDefense() : 
                           defender.getStats().getSpecialDef();
        
        double damage = (((2 * attacker.getLevel() / 5.0) + 2) * move.getPower() * attackStat / defenseStat) / 50 + 2;
        
        if (attacker.getTypes().contains(move.getType())) {
            damage *= 1.5;
        }
        
        double typeEffectiveness = calculateTypeEffectiveness(move.getType(), defender.getTypes());
        damage *= typeEffectiveness;
        
        damage *= (0.85 + new Random().nextDouble() * 0.15);
        
        return (int) damage;
    }
    
    protected double calculateTypeEffectiveness(Pokemon.PokemonType moveType, List<Pokemon.PokemonType> defenderTypes) {
        return TypeEffectivenessChart.getInstance().getEffectiveness(moveType, defenderTypes);
    }
    
    protected void openBag() {
        JPanel bagPanel = createMainBagPanel();
        
        mainPanel.remove(actionPanel);
        mainPanel.add(bagPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private JPanel createMainBagPanel() {
        JPanel bagPanel = new JPanel(new BorderLayout());
        bagPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bagPanel.setBackground(new Color(100, 100, 100));
        bagPanel.setPreferredSize(new Dimension(800, 150));
        
        JPanel categoryPanel = new JPanel(new GridBagLayout());
        categoryPanel.setBackground(new Color(100, 100, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JButton medicineButton = createCategoryButton("Medicine", "/resources/button_icons/potion.png");
        medicineButton.addActionListener(e -> showItemCategory("Medicine"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        categoryPanel.add(medicineButton, gbc);
        
        if (canUsePokeballs()) {
            JButton pokeballsButton = createCategoryButton("Poké Balls", "/resources/button_icons/pokeball.png");
            pokeballsButton.addActionListener(e -> showItemCategory("Poké Balls"));
            gbc.gridx = 1;
            gbc.gridy = 0;
            categoryPanel.add(pokeballsButton, gbc);
            
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
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(100, 100, 100));
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButtonPanel.setBackground(new Color(100, 100, 100));
        
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(e -> switchToPanel(actionPanel));
        
        backButtonPanel.add(backButton);
        bottomPanel.add(backButtonPanel, BorderLayout.EAST);
        
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
        Set<Item> inventory = player.getInventory();
        
        List<Item> categoryItems = new ArrayList<>();
        for (Item item : inventory) {
            String itemType = getItemType(item);
            if (itemType.equals(category)) {
                categoryItems.add(item);
            }
        }
        
        currentCategoryPanel = new JPanel(new BorderLayout());
        currentCategoryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        currentCategoryPanel.setBackground(new Color(248, 248, 240));
        
        JLabel titleLabel = new JLabel(category);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        currentCategoryPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (categoryItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("No " + category + " available", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            itemsPanel.add(emptyLabel);
        } else {
            for (Item item : categoryItems) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                
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
                
                JLabel nameLabel = new JLabel(item.getName() + " x" + item.getQuantity());
                nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                
                JButton useButton = new JButton("Use");
                useButton.addActionListener(e -> {
                    useItem(item);
                    
                    mainPanel.remove(currentCategoryPanel);
                    mainPanel.add(infoPanel, BorderLayout.SOUTH);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
                
                itemPanel.add(imagePanel, BorderLayout.WEST);
                itemPanel.add(nameLabel, BorderLayout.CENTER);
                itemPanel.add(useButton, BorderLayout.EAST);
                
                itemsPanel.add(itemPanel);
                itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        currentCategoryPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Back to Bag");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            mainPanel.remove(currentCategoryPanel);
            
            JPanel bagPanel = createMainBagPanel();
            
            mainPanel.add(bagPanel, BorderLayout.SOUTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        currentCategoryPanel.add(buttonPanel, BorderLayout.SOUTH);
        
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
    
    protected void queueMessage(String message) {
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

    protected void useItem(Item item) {
        String itemName = item.getName().toLowerCase();
        
        if (!canUsePokeballs() && itemName.contains("ball") && !itemName.contains("berry")) {
            showInfoPanel("You can't use Poké Balls in this battle!");
            
            Timer returnTimer = new Timer(1500, e -> {
                openBag();
            });
            returnTimer.setRepeats(false);
            returnTimer.start();
            return;
        }
        
        showInfoPanel("Used " + item.getName() + "!");
        
        boolean itemUsed = false;
        
        if (itemName.contains("ball") && !itemName.contains("berry")) {
            attemptCatch((Pokeball) item);
            itemUsed = true;
        } else if (itemName.contains("potion") || itemName.contains("heal")) {
            int healAmount = 20;
            
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
            
            animationTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationStep++;
                    
                    switch (animationStep) {
                        case 1:
                            battleMessageLabel.setText(playerPokemon.getName() + " was healed!");
                            break;
                        case 2:
                            playerTurn = false;
                            opponentPokemonAttack();
                            break;
                        case 3:
                            if (playerPokemon.getStats().getCurrentHp() <= 0) {
                                battleMessageLabel.setText(playerPokemon.getName() + " fainted!");
                                battleEnded = true;
                            } else {
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
            
            animationStep = 0;
            animationTimer.start();
        } else if (itemName.contains("berry")) {
            if (itemName.contains("oran")) {
                int currentHP = playerPokemon.getStats().getCurrentHp();
                int maxHP = playerPokemon.getStats().getMaxHp();
                int newHP = Math.min(currentHP + 10, maxHP);
                
                playerPokemon.getStats().setCurrentHp(newHP);
                updatePlayerPokemonHP();
                itemUsed = true;
            } else if (itemName.contains("sitrus")) {
                int maxHP = playerPokemon.getStats().getMaxHp();
                int healAmount = maxHP / 4;
                int currentHP = playerPokemon.getStats().getCurrentHp();
                int newHP = Math.min(currentHP + healAmount, maxHP);
                
                playerPokemon.getStats().setCurrentHp(newHP);
                updatePlayerPokemonHP();
                itemUsed = true;
            }
            
            animationTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationStep++;
                    
                    switch (animationStep) {
                        case 1:
                            battleMessageLabel.setText(playerPokemon.getName() + " ate the " + item.getName() + "!");
                            break;
                        case 2:
                            playerTurn = false;
                            opponentPokemonAttack();
                            break;
                        case 3:
                            if (playerPokemon.getStats().getCurrentHp() <= 0) {
                                battleMessageLabel.setText(playerPokemon.getName() + " fainted!");
                                battleEnded = true;
                            } else {
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
            
            animationStep = 0;
            animationTimer.start();
        }
        
        if (itemUsed) {
            item.decreaseQuantity();
            if (item.getQuantity() <= 0) {
                player.removeItem(item);
            }
        }
    }
    
    private void attemptCatch(Pokeball pokeball) {
        double catchRate = pokeball.getCatchRate();
        
        double hpFactor = 1.0 - ((double) currentOpponentPokemon.getStats().getCurrentHp() / currentOpponentPokemon.getStats().getMaxHp()) * 0.7;
        catchRate *= (1.0 + hpFactor);
        
        catchRate = Math.min(1.0, Math.max(0.0, catchRate));
        
        boolean catchSuccess = Math.random() < catchRate;
        
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
                            battleMessageLabel.setText("Gotcha! " + currentOpponentPokemon.getName() + " was caught!");
                            player.addPokemonToCurrentTeam(currentOpponentPokemon);
                            battleEnded = true;
                            
                            Timer closeTimer = new Timer(2000, event -> dispose());
                            closeTimer.setRepeats(false);
                            closeTimer.start();
                        } else {
                            battleMessageLabel.setText("Oh no! The Pokémon broke free!");
                            
                            Timer nextTurn = new Timer(1500, event -> {
                                playerTurn = false;
                                opponentPokemonAttack();
                                
                                animationStep = 0;
                            });
                            nextTurn.setRepeats(false);
                            nextTurn.start();
                        }
                        break;
                }
            }
        });
        
        animationStep = 0;
        animationTimer.start();
    }

    protected void switchPokemon() {
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
            JPanel pokemonSelectionPanel = createPokemonSelectionPanel();
            switchToPanel(pokemonSelectionPanel);
        }
    }
    
    private JPanel createPokemonSelectionPanel() {
        JPanel pokemonSelectionPanel = new JPanel(new BorderLayout());
        pokemonSelectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pokemonSelectionPanel.setBackground(new Color(248, 248, 240));
        
        JLabel titleLabel = new JLabel("Choose a Pokémon");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        pokemonSelectionPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel pokemonListPanel = new JPanel();
        pokemonListPanel.setLayout(new BoxLayout(pokemonListPanel, BoxLayout.Y_AXIS));
        pokemonListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (Pokemon p : player.getTeam()) {
            if (p == playerPokemon) {
                continue;
            }
            
            if (p.getStats().getCurrentHp() <= 0) {
                continue;
            }
            
            JPanel pokemonPanel = new JPanel(new BorderLayout());
            pokemonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            pokemonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            JLabel nameLabel = new JLabel(p.getName() + " Lv." + p.getLevel());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            JProgressBar hpBar = new JProgressBar(0, p.getStats().getMaxHp());
            hpBar.setValue(p.getStats().getCurrentHp());
            hpBar.setString("HP: " + p.getStats().getCurrentHp() + "/" + p.getStats().getMaxHp());
            hpBar.setStringPainted(true);
            
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
            
            JButton switchButton = new JButton("Switch");
            switchButton.addActionListener(e -> {
                final Pokemon selectedPokemon = p;
                performSwitch(selectedPokemon);
            });
            
            pokemonPanel.add(infoPanel, BorderLayout.CENTER);
            pokemonPanel.add(switchButton, BorderLayout.EAST);
            
            pokemonListPanel.add(pokemonPanel);
            pokemonListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JScrollPane scrollPane = new JScrollPane(pokemonListPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        pokemonSelectionPanel.add(scrollPane, BorderLayout.CENTER);
        
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
        switchToPanel(infoPanel);
        
        battleMessageLabel.setText("Come back " + playerPokemon.getName() + "!");
        
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                switch (animationStep) {
                    case 1:
                        // Hide current Pokemon sprite
                        playerPokemonImage.setVisible(false);
                        
                        playerPokemon = newPokemon;
                        battleMessageLabel.setText("Go, " + playerPokemon.getName() + "!");
                        
                        // Play throwing animation for new Pokemon
                        playPokeballThrowAnimation(() -> {
                            // Update UI after animation completes and sprite is shown
                            playerPokemonInfo.setText(playerPokemon.getName() + " L" + playerPokemon.getLevel());
                            
                            playerPokemonHP.setMaximum(playerPokemon.getStats().getMaxHp());
                            playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
                            hpValueLabel.setText(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
                            
                            createMovePanel();
                            updatePlayerExpBar();
                            
                            // Continue with opponent's turn
                            Timer opponentTurnTimer = new Timer(1000, event -> {
                                playerTurn = false;
                                opponentPokemonAttack();
                                animationTimer.stop();
                            });
                            opponentTurnTimer.setRepeats(false);
                            opponentTurnTimer.start();
                        });
                        break;
                }
            }
        });
        
        animationStep = 0;
        animationTimer.start();
    }
    
    protected void attemptRun() {
        if (!canRun()) {
            showInfoPanel("You can't run from a trainer battle!");
            
            Timer returnTimer = new Timer(1500, e -> {
                switchToPanel(actionPanel);
            });
            returnTimer.setRepeats(false);
            returnTimer.start();
            return;
        }
        
        showInfoPanel("Got away safely!");
        
        Timer runTimer = new Timer(2000, e -> dispose());
        runTimer.setRepeats(false);
        runTimer.start();
    }
    
    private ImageIcon loadPokemonImage(Pokemon pokemon, boolean isWild) {
        BufferedImage image = null;
        int dexNumber = pokemon.getDex();
        
        try {
            String spritePath = "sprites/sprites/pokemon/";
            
            if (!isWild) {
                spritePath += "/back";
            }
            
            if (pokemon.getIsShiny()) {
                spritePath += "/shiny";
            }
            
            spritePath += "/" + dexNumber + ".png";
            
            File file = new File(spritePath);
            if (file.exists()) {
                image = javax.imageio.ImageIO.read(file);
            }
            
            if (image != null) {
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
        
        return createPlaceholderImage(pokemon, isWild);
    }
    
    private ImageIcon createPlaceholderImage(Pokemon pokemon, boolean isWild) {
        int width = 150;
        int height = 150;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        Color mainColor = Color.GRAY;
        if (!pokemon.getTypes().isEmpty()) {
            mainColor = UIComponentFactory.getColorForType(pokemon.getTypes().get(0));
        }
        
        g2d.setColor(mainColor);
        if (isWild) {
            g2d.fillOval(0, 0, width, height);
        } else {
            g2d.fillRect(0, 0, width, height);
        }
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(pokemon.getName(), 10, height / 2);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    private void validateOpponentPokemonMoves() {
        List<Move> usableMoves = new ArrayList<>();
        
        for (Move move : currentOpponentPokemon.getMoves()) {
            if (move != null && move.getCurrentPP() > 0) {
                usableMoves.add(move);
            }
        }
        
        if (usableMoves.isEmpty()) {
            System.out.println("Opponent " + currentOpponentPokemon.getName() + " has no usable moves, regenerating...");
            currentOpponentPokemon.generateWildMoves();
        }
    }

    protected void playPokeballThrowAnimation(Runnable onComplete) {
        if (isPlayingThrowAnimation) return;
        
        isPlayingThrowAnimation = true;
        
        // Show player and start throwing animation
        playerBattleView.showForThrow();
        playerBattleView.startThrowingAnimation();
        
        // Create pokeball sprite
        JLabel pokeballSprite = new JLabel();
        try {
            Image pokeballImage = javax.imageio.ImageIO.read(getClass().getResource("/resources/items/pokeball.png"));
            if (pokeballImage != null) {
                pokeballSprite.setIcon(new javax.swing.ImageIcon(pokeballImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
            }
        } catch (Exception e) {
            pokeballSprite.setText("●");
            pokeballSprite.setForeground(Color.RED);
            pokeballSprite.setFont(new Font("Arial", Font.BOLD, 24));
        }
        
        Point throwingCenter = playerBattleView.getThrowingCenter();
        int startX = throwingCenter.x - 16;
        int startY = throwingCenter.y - 16;
        int endX = 450 + 75;
        int endY = 140 + 75;
        
        pokeballSprite.setBounds(startX, startY, 32, 32);
        battlegroundPanel.add(pokeballSprite);
        battlegroundPanel.setComponentZOrder(pokeballSprite, 0);
        
        final int[] animationStep = {0};
        final int totalSteps = 20;
        
        throwAnimationTimer = new Timer(50, e -> {
            animationStep[0]++;
            
            // Update player throwing animation every 5th step
            if (animationStep[0] % 5 == 0) {
                playerBattleView.advanceThrowingFrame();
            }
            
            // Move pokeball in an arc
            float progress = (float) animationStep[0] / totalSteps;
            int currentX = (int) (startX + (endX - startX) * progress);
            int arcHeight = 50;
            int currentY = (int) (startY + (endY - startY) * progress - 
                                arcHeight * Math.sin(Math.PI * progress));
            
            pokeballSprite.setLocation(currentX, currentY);
            battlegroundPanel.repaint();
            
            // Animation complete
            if (animationStep[0] >= totalSteps) {
                throwAnimationTimer.stop();
                battlegroundPanel.remove(pokeballSprite);
                
                // Hide player after throw is complete
                playerBattleView.stopThrowingAnimation();
                playerBattleView.hidePlayer();
                
                // NOW show the player's Pokemon sprite
                playerPokemonImage.setIcon(loadPokemonImage(playerPokemon, false));
                playerPokemonImage.setVisible(true);
                
                isPlayingThrowAnimation = false;
                battlegroundPanel.repaint();
                
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        throwAnimationTimer.start();
    }

}
