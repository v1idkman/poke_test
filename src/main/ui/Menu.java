package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

import model.Player;
import model.Item;
import model.Medicine;
import model.Move;
import model.Pokeball;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;
import model.KeyItem;

import java.util.List;


public class Menu {
    private static Menu instance;
    
    private JButton menuButton;
    private JDialog menuDialog;
    private Timer gameTimer;
    private Player player;
    
    private Menu() {
        // Initialize any constant properties here
    }
    
    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }
    
    public void initializeMenuButton(JPanel board, int tileSize, int columns, int rows) {
        // Create menu button
        menuButton = new JButton("MENU");
        menuButton.setFont(new Font("Lato", Font.BOLD, 16));
        menuButton.setBackground(new Color(30, 201, 139));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusPainted(false);
        menuButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Position the button in the bottom-right corner
        menuButton.setBounds(tileSize * columns - 100, tileSize * rows - 40, 80, 30);
        
        // Add action listener to open menu
        menuButton.addActionListener(e -> openPlayerMenu(board));
        
        // Add button to the panel
        board.setLayout(null); // Use absolute positioning
        board.add(menuButton);
    }
    
    // Set the current player for the menu
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    // Set the game timer for pausing/resuming
    public void setGameTimer(Timer timer) {
        this.gameTimer = timer;
    }
    
    private void openPlayerMenu(JPanel parentBoard) {
        // Pause the game timer when menu is open
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // Create dialog for the menu
        menuDialog = new JDialog();
        menuDialog.setUndecorated(true);
        menuDialog.setSize(500, 400);
        menuDialog.setLocationRelativeTo(parentBoard);
        menuDialog.setModal(true);
        
        // Create tabbed pane for different menu sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add tabs for different menu sections
        tabbedPane.addTab("Pokémon", createPokemonPanel());
        tabbedPane.addTab("Inventory", createInventoryPanel());
        tabbedPane.addTab("Gym Badges", createBadgesPanel());
        tabbedPane.addTab("Trainer Card", createTrainerPanel());
        
        // Add close button
        JButton closeButton = new JButton("Close Menu");
        closeButton.addActionListener(e -> {
            menuDialog.dispose();
            if (gameTimer != null) {
                gameTimer.start(); // Resume game when menu is closed
            }
            parentBoard.requestFocusInWindow(); // Restore focus to game panel
        });
        
        // Layout components
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create a panel for the button to center it
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        menuDialog.add(mainPanel);
        menuDialog.setVisible(true);
    }
    
    private JPanel createPokemonPanel() {
        // Main panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout());

        // Check if player is available or has Pokémon
        if (player == null || player.getTeam() == null || player.getTeam().isEmpty()) {
            JLabel placeholder = new JLabel("You don't have any Pokémon yet", JLabel.CENTER);
            placeholder.setFont(new Font("Lato", Font.PLAIN, 18));
            panel.add(placeholder, BorderLayout.CENTER);
            return panel;
        }
        
        // Create a panel to display Pokémon list (similar to Pokétch app)
        JPanel pokemonListPanel = new JPanel(new GridLayout(6, 1, 5, 5)); // Max 6 Pokémon in team
        pokemonListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Right side - Pokémon details panel
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Pokémon Info"));
        detailsPanel.setPreferredSize(new Dimension(200, 0));
        
        // Components for the details panel
        JLabel pokemonNameLabel = new JLabel();
        pokemonNameLabel.setFont(new Font("Lato", Font.BOLD, 18));
        pokemonNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel pokemonStatsLabel = new JLabel();
        pokemonStatsLabel.setFont(new Font("Lato", Font.PLAIN, 14));
        
        // Panel for Pokémon image
        JPanel pokemonImagePanel = new JPanel(new BorderLayout());
        pokemonImagePanel.setPreferredSize(new Dimension(120, 120));
        pokemonImagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Layout the details panel
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(pokemonImagePanel, BorderLayout.CENTER);
        
        detailsPanel.add(imageContainer, BorderLayout.NORTH);
        detailsPanel.add(pokemonNameLabel, BorderLayout.CENTER);
        // detailsPanel.add(pokemonStatsLabel, BorderLayout.SOUTH);
        
        // Add each Pokémon to the list
        List<Pokemon> team = player.getTeam();
        for (Pokemon pokemon : team) {
            // Create a panel for each Pokémon in the list
            JPanel pokemonPanel = new JPanel(new BorderLayout());
            pokemonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            // Create a PokemonView to handle drawing
            PokemonView pokemonView = new PokemonView(pokemon);
            
            // Create a custom panel to display the Pokémon sprite
            JPanel spritePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the Pokémon icon (using PokemonView)
                    pokemonView.draw(g, this, 5, 5, getWidth() - 10, getHeight() - 10, false, pokemon.getIsShiny());
                }
            };
            spritePanel.setPreferredSize(new Dimension(50, 50));
            
            // Create HP bar similar to Pokétch
            JProgressBar hpBar = new JProgressBar(0, pokemon.getStats().getMaxHp());
            hpBar.setValue(pokemon.getStats().getCurrentHp());
            hpBar.setForeground(new Color(30, 201, 139));
            hpBar.setStringPainted(false);
            
            // Display Pokémon name
            JLabel nameLabel = new JLabel(pokemon.getName(), JLabel.CENTER);
            
            // Add components to the Pokémon panel
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.add(nameLabel, BorderLayout.NORTH);
            infoPanel.add(hpBar, BorderLayout.SOUTH);
            
            pokemonPanel.add(spritePanel, BorderLayout.WEST);
            pokemonPanel.add(infoPanel, BorderLayout.CENTER);
            
            // Add item icon if Pokémon is holding an item
            if (pokemon.getHeldItem() != null) {
                JLabel itemLabel = new JLabel(new ImageIcon(pokemon.getHeldItem().getImage()));
                pokemonPanel.add(itemLabel, BorderLayout.EAST);
            }
            
            // Add click listener to show Pokémon details
            pokemonPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Update details panel with selected Pokémon
                    updatePokemonDetailsPanel(pokemon, pokemonNameLabel, pokemonStatsLabel, pokemonImagePanel, pokemonView);
                }
            });
            
            pokemonListPanel.add(pokemonPanel);
        }
        
        // Display the first Pokémon's details by default
        if (!team.isEmpty()) {
            updatePokemonDetailsPanel(team.get(0), pokemonNameLabel, pokemonStatsLabel, pokemonImagePanel, 
                                    new PokemonView(team.get(0)));
        }
        
        // Create a split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pokemonListPanel, detailsPanel);
        splitPane.setDividerLocation(300);
        splitPane.setEnabled(false); // Prevent user from moving the divider
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // Helper method to update the Pokémon details panel
    private void updatePokemonDetailsPanel(Pokemon pokemon, JLabel nameLabel, JLabel statsLabel, 
                                    JPanel imagePanel, PokemonView pokemonView) {
        if (pokemon == null || pokemon.getStats() == null) {
            nameLabel.setText("No Pokémon selected");
            statsLabel.setText("");
            return;
        }
        
        // Update name with larger, centered text
        nameLabel.setText(pokemon.getName());
        nameLabel.setFont(new Font("Lato", Font.BOLD, 22));
        
        // Create a panel for the Pokémon image with a border
        imagePanel.removeAll();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Create a custom panel to draw the Pokémon
        JPanel largeImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the Pokémon (larger view)
                pokemonView.draw(g, this, 0, 0, getWidth(), getHeight(), false, pokemon.getIsShiny());
            }
        };
        imagePanel.add(largeImagePanel, BorderLayout.CENTER);
        
        // Create a panel for basic stats information
        JPanel statsInfoPanel = new JPanel(new BorderLayout(10, 10));
        
        // Create HP bar
        JPanel hpPanel = new JPanel(new BorderLayout(5, 0));
        hpPanel.add(new JLabel("HP:"), BorderLayout.WEST);
        
        JProgressBar hpBar = new JProgressBar(0, pokemon.getStats().getMaxHp());
        hpBar.setValue(pokemon.getStats().getCurrentHp());
        hpBar.setForeground(new Color(30, 201, 139));
        hpBar.setStringPainted(true);
        hpBar.setString(pokemon.getStats().getCurrentHp() + "/" + pokemon.getStats().getMaxHp());
        hpPanel.add(hpBar, BorderLayout.CENTER);
        
        JPanel basicInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        // Level
        gbc.gridx = 0; gbc.gridy = 0;
        basicInfoPanel.add(new JLabel("Level:"), gbc);
        gbc.gridx = 1;
        basicInfoPanel.add(new JLabel(String.valueOf(pokemon.getStats().getLevel())), gbc);

        // Dex #
        gbc.gridx = 0; gbc.gridy = 1;
        basicInfoPanel.add(new JLabel("Dex #:"), gbc);
        gbc.gridx = 1;
        basicInfoPanel.add(new JLabel(String.valueOf(pokemon.getDex())), gbc);

        // Type (label)
        gbc.gridx = 0; gbc.gridy = 2;
        basicInfoPanel.add(new JLabel("Type:"), gbc);

        // Type (panel with multiple types)
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        for (PokemonType type : pokemon.getTypes()) {
            JLabel typeLabel = new JLabel(type.name());
            typeLabel.setOpaque(true);
            typeLabel.setBackground(getColorForType(type));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            typePanel.add(typeLabel);
        }
        gbc.gridx = 1;
        basicInfoPanel.add(typePanel, gbc);

        // held item
        gbc.gridx = 0; gbc.gridy = 3;
        basicInfoPanel.add(new JLabel("Held Item:"), gbc);
        gbc.gridx = 1;
        basicInfoPanel.add(new JLabel(String.valueOf(pokemon.holdsItem() ? pokemon.getHeldItem() : " nothing")), gbc);
        

        
        // Add HP and basic info to the stats panel
        JPanel topStatsPanel = new JPanel(new BorderLayout(0, 10));
        topStatsPanel.add(hpPanel, BorderLayout.NORTH);
        topStatsPanel.add(basicInfoPanel, BorderLayout.CENTER);
        
        // Create buttons panel for Moves and Stats
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        // Create Moves button
        JButton movesButton = new JButton("Moves");
        movesButton.setBackground(new Color(30, 201, 139));
        movesButton.setForeground(Color.WHITE);
        movesButton.setFocusPainted(false);
        movesButton.addActionListener(e -> openMovesPanel(pokemon));
        
        // Create Stats button
        JButton statsButton = new JButton("Stats");
        statsButton.setBackground(new Color(30, 201, 139));
        statsButton.setForeground(Color.WHITE);
        statsButton.setFocusPainted(false);
        statsButton.addActionListener(e -> openStatsPanel(pokemon));
        
        // Add buttons to panel
        buttonsPanel.add(movesButton);
        buttonsPanel.add(statsButton);
        
        // Add all components to the stats info panel
        statsInfoPanel.add(topStatsPanel, BorderLayout.CENTER);
        statsInfoPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Get the parent container (details panel)
        Container parent = imagePanel.getParent();
        if (parent != null && parent instanceof JPanel) {
            JPanel detailsPanel = (JPanel) parent;
            
            // Clear the details panel and rebuild it
            detailsPanel.removeAll();
            detailsPanel.add(imagePanel, BorderLayout.NORTH);
            detailsPanel.add(nameLabel, BorderLayout.CENTER);
            detailsPanel.add(statsInfoPanel, BorderLayout.SOUTH);
            
            // Force the panel to refresh
            detailsPanel.revalidate();
            detailsPanel.repaint();
        }
    }

    private void openStatsPanel(Pokemon pokemon) {
        // Create a dialog to show the stats - make it larger
        JDialog statsDialog = new JDialog(menuDialog, "Stats - " + pokemon.getName(), true);
        statsDialog.setSize(450, 450); // Increased size
        statsDialog.setLocationRelativeTo(menuDialog);
        
        // Create the main panel for the stats
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create a panel for the Pokémon info at the top
        JPanel pokemonInfoPanel = new JPanel(new BorderLayout());
        
        // Show Pokémon name and image
        JLabel nameLabel = new JLabel(pokemon.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 22)); // Larger font
        
        // Create a panel for the Pokémon image
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                PokemonView pokemonView = new PokemonView(pokemon);
                pokemonView.draw(g, this, 0, 0, getWidth(), getHeight(), false, pokemon.getIsShiny());
            }
        };
        imagePanel.setPreferredSize(new Dimension(100, 100)); // Larger image
        
        pokemonInfoPanel.add(imagePanel, BorderLayout.WEST);
        pokemonInfoPanel.add(nameLabel, BorderLayout.CENTER);
        
        // Create a panel for the stat hexagon - make it much larger
        JPanel statHexagonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawStatHexagon(g, pokemon);
            }
        };
        statHexagonPanel.setPreferredSize(new Dimension(300, 300)); // Much larger hexagon
        
        // Create a panel for numeric stat values - improved layout
        JPanel numericStatsPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        numericStatsPanel.setBorder(BorderFactory.createTitledBorder("Numeric Values"));
        
        // Add labels with bold text
        Font labelFont = new Font("Lato", Font.BOLD, 12);
        Font valueFont = new Font("Lato", Font.PLAIN, 14);
        
        // First row
        JLabel hpLabel = new JLabel("HP:", JLabel.RIGHT);
        hpLabel.setFont(labelFont);
        numericStatsPanel.add(hpLabel);
        
        JLabel hpValue = new JLabel(String.valueOf(pokemon.getStats().getMaxHp()));
        hpValue.setFont(valueFont);
        numericStatsPanel.add(hpValue);
        
        JLabel atkLabel = new JLabel("Attack:", JLabel.RIGHT);
        atkLabel.setFont(labelFont);
        numericStatsPanel.add(atkLabel);
        
        JLabel atkValue = new JLabel(String.valueOf(pokemon.getStats().getAttack()));
        atkValue.setFont(valueFont);
        numericStatsPanel.add(atkValue);
        
        // Second row
        JLabel defLabel = new JLabel("Defense:", JLabel.RIGHT);
        defLabel.setFont(labelFont);
        numericStatsPanel.add(defLabel);
        
        JLabel defValue = new JLabel(String.valueOf(pokemon.getStats().getDefense()));
        defValue.setFont(valueFont);
        numericStatsPanel.add(defValue);
        
        JLabel spdLabel = new JLabel("Speed:", JLabel.RIGHT);
        spdLabel.setFont(labelFont);
        numericStatsPanel.add(spdLabel);
        
        JLabel spdValue = new JLabel(String.valueOf(pokemon.getStats().getSpeed()));
        spdValue.setFont(valueFont);
        numericStatsPanel.add(spdValue);
        
        // Third row
        JLabel spAtkLabel = new JLabel("Sp.Attack:", JLabel.RIGHT);
        spAtkLabel.setFont(labelFont);
        numericStatsPanel.add(spAtkLabel);
        
        JLabel spAtkValue = new JLabel(String.valueOf(pokemon.getStats().getSpecialAtk()));
        spAtkValue.setFont(valueFont);
        numericStatsPanel.add(spAtkValue);
        
        JLabel spDefLabel = new JLabel("Sp.Defense:", JLabel.RIGHT);
        spDefLabel.setFont(labelFont);
        numericStatsPanel.add(spDefLabel);
        
        JLabel spDefValue = new JLabel(String.valueOf(pokemon.getStats().getSpecialDef()));
        spDefValue.setFont(valueFont);
        numericStatsPanel.add(spDefValue);
        
        // Create a panel to hold both stat visualizations
        JPanel statsDisplayPanel = new JPanel(new BorderLayout(10, 10));
        statsDisplayPanel.add(statHexagonPanel, BorderLayout.CENTER);
        statsDisplayPanel.add(numericStatsPanel, BorderLayout.SOUTH);
        
        // Create close button at bottom right
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> statsDialog.dispose());
        buttonPanel.add(closeButton, BorderLayout.LINE_END);
        
        // Add all components to the main panel
        mainPanel.add(pokemonInfoPanel, BorderLayout.NORTH);
        mainPanel.add(statsDisplayPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set up and show the dialog
        statsDialog.add(mainPanel);
        statsDialog.setVisible(true);
    }
    
    // Helper method to draw the stat hexagon
    private void drawStatHexagon(Graphics g, Pokemon pokemon) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = g.getClipBounds().width / 2;
        int centerY = g.getClipBounds().height / 2;
        int radius = Math.min(centerX, centerY) - 30; // Slightly smaller to ensure labels fit
        
        // Get the stats (normalized to 0.0-1.0 scale)
        double hpRatio = normalizeStatValue(pokemon.getStats().getMaxHp(), 255);
        double attackRatio = normalizeStatValue(pokemon.getStats().getAttack(), 255);
        double defenseRatio = normalizeStatValue(pokemon.getStats().getDefense(), 255);
        double speedRatio = normalizeStatValue(pokemon.getStats().getSpeed(), 255);
        double specialAtkRatio = normalizeStatValue(pokemon.getStats().getSpecialAtk(), 255);
        double specialDefRatio = normalizeStatValue(pokemon.getStats().getSpecialDef(), 255);
        
        // Calculate points for the hexagon axes (6 stats in modern games)
        int sides = 6; // HP, Attack, Defense, Speed, Sp.Atk, Sp.Def
        double[] statValues = {hpRatio, attackRatio, defenseRatio, speedRatio, specialAtkRatio, specialDefRatio};
        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];
        
        // Draw axes
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2 * i / sides - Math.PI / 2;
            int xEnd = centerX + (int)(Math.cos(angle) * radius);
            int yEnd = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, xEnd, yEnd);
            
            // Draw stat labels with larger font
            String[] labels = {"HP", "Atk", "Def", "Spd", "Sp.Atk", "Sp.Def"};
            g2d.setFont(new Font("Lato", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = centerX + (int)(Math.cos(angle) * (radius + 20)) - fm.stringWidth(labels[i])/2;
            int labelY = centerY + (int)(Math.sin(angle) * (radius + 20)) + fm.getHeight()/2;
            g2d.drawString(labels[i], labelX, labelY);
            
            // Draw stat values near the axes
            String statValue = "";
            switch (i) {
                case 0: statValue = String.valueOf(pokemon.getStats().getMaxHp()); break;
                case 1: statValue = String.valueOf(pokemon.getStats().getAttack()); break;
                case 2: statValue = String.valueOf(pokemon.getStats().getDefense()); break;
                case 3: statValue = String.valueOf(pokemon.getStats().getSpeed()); break;
                case 4: statValue = String.valueOf(pokemon.getStats().getSpecialAtk()); break;
                case 5: statValue = String.valueOf(pokemon.getStats().getSpecialDef()); break;
            }
            
            // Position the stat values closer to the polygon
            double valueAngle = angle;
            int valueRadius = (int)(radius * 0.7); // Position values at 70% of the radius
            int valueX = centerX + (int)(Math.cos(valueAngle) * valueRadius) - fm.stringWidth(statValue)/2;
            int valueY = centerY + (int)(Math.sin(valueAngle) * valueRadius) + fm.getHeight()/2;
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(statValue, valueX, valueY);
            g2d.setColor(Color.LIGHT_GRAY);
        }
        
        // Draw concentric circles for reference
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 1; i <= 5; i++) {
            int circleRadius = radius * i / 5;
            g2d.drawOval(centerX - circleRadius, centerY - circleRadius, 
                        circleRadius * 2, circleRadius * 2);
        }
        
        // Calculate points for the stat polygon
        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2 * i / sides - Math.PI / 2;
            double statRadius = radius * statValues[i];
            xPoints[i] = centerX + (int)(Math.cos(angle) * statRadius);
            yPoints[i] = centerY + (int)(Math.sin(angle) * statRadius);
        }
        
        // Draw the stat polygon
        g2d.setColor(new Color(30, 201, 139, 150)); // Semi-transparent green
        g2d.fillPolygon(xPoints, yPoints, sides);
        
        // Draw the outline
        g2d.setColor(new Color(30, 201, 139));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawPolygon(xPoints, yPoints, sides);
    }    

    private void openMovesPanel(Pokemon pokemon) {
        // Create a dialog to show the moves
        JDialog movesDialog = new JDialog(menuDialog, "Moves - " + pokemon.getName(), true);
        movesDialog.setSize(400, 300);
        movesDialog.setLocationRelativeTo(menuDialog);
        
        // Create the main panel for the moves
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create a panel for the Pokémon info at the top
        JPanel pokemonInfoPanel = new JPanel(new BorderLayout());
        
        // Show Pokémon name and image
        JLabel nameLabel = new JLabel(pokemon.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 18));
        
        // Create a panel for the Pokémon image
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                PokemonView pokemonView = new PokemonView(pokemon);
                pokemonView.draw(g, this, 0, 0, getWidth(), getHeight(), false, pokemon.getIsShiny());
            }
        };
        imagePanel.setPreferredSize(new Dimension(80, 80));
        
        pokemonInfoPanel.add(imagePanel, BorderLayout.WEST);
        pokemonInfoPanel.add(nameLabel, BorderLayout.CENTER);
        
        // Create a panel for the moves list
        JPanel movesPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        movesPanel.setBorder(BorderFactory.createTitledBorder("Moves"));
        
        // Add each move to the panel
        List<Move> moves = pokemon.getMoves();
        if (moves != null && !moves.isEmpty()) {
            for (Move move : moves) {
                JPanel movePanel = new JPanel(new BorderLayout(5, 0));
                movePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                
                // Move name with type color indicator
                JLabel moveNameLabel = new JLabel(move.getName());
                moveNameLabel.setFont(new Font("Lato", Font.BOLD, 14));
                
                // Move type with background color
                JLabel typeLabel = new JLabel(move.getType().toString());
                typeLabel.setOpaque(true);
                typeLabel.setBackground(getColorForType(move.getType()));
                typeLabel.setForeground(Color.WHITE);
                typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                
                // Move details (power, accuracy, PP)
                JPanel detailsPanel = new JPanel(new GridLayout(1, 3, 5, 0));
                detailsPanel.add(new JLabel("Power: " + move.getPower()));
                detailsPanel.add(new JLabel("Acc: " + move.getAccuracy() + "%"));
                detailsPanel.add(new JLabel("PP: " + move.getCurrentPP() + "/" + move.getMaxPP()));
                
                // Add components to the move panel
                JPanel leftPanel = new JPanel(new BorderLayout(5, 0));
                leftPanel.add(moveNameLabel, BorderLayout.CENTER);
                leftPanel.add(typeLabel, BorderLayout.EAST);
                
                movePanel.add(leftPanel, BorderLayout.WEST);
                movePanel.add(detailsPanel, BorderLayout.EAST);
                
                movesPanel.add(movePanel);
            }
        } else {
            movesPanel.add(new JLabel("No moves learned", JLabel.CENTER));
        }
        
        // Add scroll capability for many moves
        JScrollPane scrollPane = new JScrollPane(movesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Create close button at bottom right
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> movesDialog.dispose());
        buttonPanel.add(closeButton, BorderLayout.LINE_END);
        
        // Add all components to the main panel
        mainPanel.add(pokemonInfoPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set up and show the dialog
        movesDialog.add(mainPanel);
        movesDialog.setVisible(true);
    }

    // Helper method to normalize stat values to 0.0-1.0 scale
    private double normalizeStatValue(int statValue, int maxPossibleValue) {
        return Math.min(1.0, Math.max(0.0, (double)statValue / maxPossibleValue));
    }

    // TODO: implement visual for normalized hex values
    // Helper method to get color based on Pokémon type
    private Color getColorForType(PokemonType type) {
        // You'll need to implement this method with appropriate colors for each type
        switch (type) {
            case NORMAL: return new Color(168, 168, 120);
            case FIRE: return new Color(240, 128, 48);
            case WATER: return new Color(104, 144, 240);
            case GRASS: return new Color(120, 200, 80);
            case ELECTRIC: return new Color(248, 208, 48);
            case ICE: return new Color(152, 216, 216);
            case FIGHTING: return new Color(192, 48, 40);
            case POISON: return new Color(160, 64, 160);
            case GROUND: return new Color(224, 192, 104);
            case FLYING: return new Color(168, 144, 240);
            case PSYCHIC: return new Color(248, 88, 136);
            case BUG: return new Color(168, 184, 32);
            case ROCK: return new Color(184, 160, 56);
            case GHOST: return new Color(112, 88, 152);
            case DRAGON: return new Color(112, 56, 248);
            default: return Color.GRAY;
        }
    }
    
    private JComponent createInventoryPanel() {
        // Check if player is available
        if (player == null || player.getInventory() == null || player.getInventory().isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            JLabel emptyLabel = new JLabel("Your inventory is empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Lato", Font.PLAIN, 18));
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            return emptyPanel;
        }
        
        // Create a tabbed pane for different item categories
        JTabbedPane itemCategories = new JTabbedPane();
        
        // Get player's inventory
        Set<Item> inventory = player.getInventory();
        
        // Create maps to organize items by type
        Map<String, List<Item>> itemsByType = new HashMap<>();
        
        // Sort items by their types
        for (Item item : inventory) {
            String type = getItemType(item);
            if (!itemsByType.containsKey(type)) {
                itemsByType.put(type, new ArrayList<>());
            }
            itemsByType.get(type).add(item);
        }
        
        // Add tabs for each item type
        for (String type : itemsByType.keySet()) {
            JPanel typePanel = createItemTypePanel(itemsByType.get(type), type);
            itemCategories.addTab(type, typePanel);
        }
        
        // If there are no categories (shouldn't happen if inventory has items)
        if (itemCategories.getTabCount() == 0) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            JLabel emptyLabel = new JLabel("Your inventory is empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Lato", Font.PLAIN, 18));
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            return emptyPanel;
        }
        
        return itemCategories;
    }
    
    private String getItemType(Item item) {
        if (item instanceof Medicine) {
            return "Medicine";
        } else if (item instanceof Pokeball) {
            return "Poké Balls";
        } else if (item instanceof KeyItem) {
            return "Key Items";
        } else {
            // For any other item types that might be added later
            return "Other Items";
        }
    }
    
    private JPanel createItemTypePanel(List<Item> items, String itemType) {
        // Main panel with BorderLayout to have items on left, info on right
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        // Left side - scrollable item grid
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Use a scroll pane for the items
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Create a panel with grid layout for the items - make it 5 columns to make items smaller
        JPanel itemsPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Right side - item information panel
        JPanel infoPanel = new JPanel(new BorderLayout(0, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Item Info"));
        infoPanel.setPreferredSize(new Dimension(180, 0)); // Set width for info panel
        
        // Components for the info panel
        JLabel itemNameLabel = new JLabel();
        itemNameLabel.setFont(new Font("Lato", Font.BOLD, 18)); // Larger font
        itemNameLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text
        
        JLabel itemDescLabel = new JLabel();
        itemDescLabel.setFont(new Font("Lato", Font.PLAIN, 14)); // Larger font
        
        // Create a panel for the item image with BorderLayout to center it
        JPanel itemImagePanel = new JPanel(new BorderLayout());
        itemImagePanel.setPreferredSize(new Dimension(100, 100)); // Larger image size
        itemImagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Layout the info panel - image at top, name below it, description at bottom
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(itemImagePanel, BorderLayout.CENTER);
        
        infoPanel.add(imageContainer, BorderLayout.NORTH);
        infoPanel.add(itemNameLabel, BorderLayout.CENTER);
        infoPanel.add(itemDescLabel, BorderLayout.SOUTH);
        
        // Check if there are items of this type
        if (items.isEmpty()) {
            // No items of this type
            itemNameLabel.setText("No Items");
            itemDescLabel.setText("<html>No items of type " + itemType + " available</html>");
        } else {
            // Add each item to the grid panel
            for (Item item : items) {
                JPanel itemPanel = new JPanel(new BorderLayout(2, 2));
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                
                // Create a panel for the item image
                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (item.getImage() != null) {
                            Image img = item.getImage();
                            int imgWidth = img.getWidth(this);
                            int imgHeight = img.getHeight(this);
                            
                            // Calculate scaling factor to maintain aspect ratio
                            double scale = Math.min(
                                (double)getWidth() / imgWidth,
                                (double)getHeight() / imgHeight
                            );
                            
                            // Calculate new dimensions
                            int scaledWidth = (int)(imgWidth * scale);
                            int scaledHeight = (int)(imgHeight * scale);
                            
                            // Calculate position to center the image
                            int x = (getWidth() - scaledWidth) / 2;
                            int y = (getHeight() - scaledHeight) / 2;
                            
                            // Draw the scaled image
                            g.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                        }
                    }
                };
                imagePanel.setPreferredSize(new Dimension(24, 24)); // Smaller image size
                
                JLabel itemLabel = new JLabel(item.getName(), JLabel.CENTER);
                itemLabel.setFont(new Font("Lato", Font.PLAIN, 10)); // Smaller font
                
                itemPanel.add(imagePanel, BorderLayout.CENTER);
                itemPanel.add(itemLabel, BorderLayout.SOUTH);
                
                // Add tooltip with description
                itemPanel.setToolTipText(item.getDescription());
                
                // Add click listener to show item info and use item
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Update info panel with selected item details
                        updateItemInfoPanel(item, itemNameLabel, itemDescLabel, itemImagePanel);
                        
                        if (e.getClickCount() == 2) {
                            // Double-click to use item
                            boolean used = item.use(player);
                            if (used && item.getQuantity() <= 0) {
                                player.removeItem(item);
                                
                                // Refresh inventory panel
                                menuDialog.dispose();
                                openPlayerMenu((JPanel)menuButton.getParent());
                            }
                        }
                    }
                });
                
                itemsPanel.add(itemPanel);
            }
            
            // Display the first item's info by default
            updateItemInfoPanel(items.get(0), itemNameLabel, itemDescLabel, itemImagePanel);
        }
        
        scrollPane.setViewportView(itemsPanel);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add both panels to the main panel
        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    // Helper method to update the item info panel
    private void updateItemInfoPanel(Item item, JLabel nameLabel, JLabel descLabel, JPanel imagePanel) {
        // Update name with quantity if stackable
        nameLabel.setText(item.getName() + (item.isStackable() ? " (x" + item.getQuantity() + ")" : ""));
        
        // Update description
        descLabel.setText("<html>" + item.getDescription() + "</html>");
        
        // Update image panel
        imagePanel.removeAll();
        
        // Create a custom panel to draw the image with proper scaling
        if (item.getImage() != null) {
            final Image itemImage = item.getImage();
            
            JPanel scaledImagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    
                    int imgWidth = itemImage.getWidth(this);
                    int imgHeight = itemImage.getHeight(this);
                    
                    // Calculate scaling factor to maintain aspect ratio
                    double scale = Math.min(
                        (double)getWidth() / imgWidth,
                        (double)getHeight() / imgHeight
                    );
                    
                    // Calculate new dimensions
                    int scaledWidth = (int)(imgWidth * scale);
                    int scaledHeight = (int)(imgHeight * scale);
                    
                    // Calculate position to center the image
                    int x = (getWidth() - scaledWidth) / 2;
                    int y = (getHeight() - scaledHeight) / 2;
                    
                    // Draw the scaled image
                    g.drawImage(itemImage, x, y, scaledWidth, scaledHeight, this);
                }
            };
            
            imagePanel.setLayout(new BorderLayout());
            imagePanel.add(scaledImagePanel, BorderLayout.CENTER);
        }
        
        imagePanel.revalidate();
        imagePanel.repaint();
    }
    
    private JPanel createBadgesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] badges = {"Boulder", "Cascade", "Thunder", "Rainbow", 
                           "Soul", "Marsh", "Volcano", "Earth"};
        
        for (String badge : badges) {
            JPanel badgePanel = new JPanel(new BorderLayout());
            JLabel badgeLabel = new JLabel(badge + " Badge", JLabel.CENTER);
            
            boolean hasBadge = Math.random() > 0.5;
            if (!hasBadge) {
                badgeLabel.setForeground(Color.GRAY);
            }
            
            badgePanel.add(badgeLabel, BorderLayout.CENTER);
            panel.add(badgePanel);
        }
        
        return panel;
    }
    
    private JPanel createTrainerPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String playerName = player != null ? player.getName() : "Unknown";
        JLabel nameLabel = new JLabel("Trainer: " + playerName, JLabel.CENTER);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 20));
        
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.add(new JLabel("Trainer ID:"));
        statsPanel.add(new JLabel("#" + player.getId()));
        statsPanel.add(new JLabel("Money:"));
        statsPanel.add(new JLabel("₽" + player.getMoney()));
        statsPanel.add(new JLabel("Pokédex:"));
        statsPanel.add(new JLabel("25 seen, 12 caught"));
        statsPanel.add(new JLabel("Play Time:"));
        statsPanel.add(new JLabel("12:34"));
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
}