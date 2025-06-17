package ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import model.Player;
import model.Move;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;

public class PokemonPanelManager {
    private Player player;
    private Pokemon selectedPokemon;
    private JPanel pokemonDetailsPanel;
    private JPanel pokemonImagePanel;
    private JLabel pokemonNameLabel;
    private static final Color PANEL_BG = new Color(220, 220, 220);
    private static final Color MENU_BG = new Color(60, 60, 60, 240);
    
    public PokemonPanelManager(Player player) {
        this.player = player;
    }
    
    public JPanel createPokemonTeamPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MENU_BG);
        
        if (player == null || player.getTeam() == null || player.getTeam().isEmpty()) {
            JLabel emptyLabel = new JLabel("You don't have any Pokémon yet", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            emptyLabel.setForeground(Color.WHITE);
            mainPanel.add(emptyLabel, BorderLayout.CENTER);
            return mainPanel;
        }
        
        // Split screen evenly into two halves
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);
        splitPane.setBackground(MENU_BG);
        
        // Left side - Team display (horizontal list)
        JPanel leftPanel = createHorizontalTeamPanel();
        
        // Right side - Selected Pokémon details
        JPanel rightPanel = createPokemonDetailsPanel();
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Select first Pokémon by default
        if (!player.getTeam().isEmpty()) {
            selectPokemon(player.getTeam().get(0));
        }
        
        return mainPanel;
    }

    private JPanel createHorizontalTeamPanel() {
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBackground(MENU_BG);
        teamPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create vertical list for Pokémon (only actual team members, no empty slots)
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(MENU_BG);
        
        List<Pokemon> team = player.getTeam();
        
        for (int i = 0; i < team.size(); i++) {
            Pokemon pokemon = team.get(i);
            JPanel pokemonRow = createHorizontalPokemonRow(pokemon);
            listPanel.add(pokemonRow);
            
            if (i < team.size() - 1) {
                listPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBackground(MENU_BG);
        scrollPane.getViewport().setBackground(MENU_BG);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        teamPanel.add(scrollPane, BorderLayout.CENTER);
        return teamPanel;
    }
    
    
    private JPanel createHorizontalPokemonRow(Pokemon pokemon) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setPreferredSize(new Dimension(350, 90));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBackground(PANEL_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MENU_BG, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Pokémon sprite on the left
        JPanel spritePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                PokemonView pokemonView = new PokemonView(pokemon);
                pokemonView.draw(g2d, this, 2, 2, getWidth() - 4, getHeight() - 4, 
                               false, pokemon.getIsShiny());
                g2d.dispose();
            }
        };
        spritePanel.setPreferredSize(new Dimension(70, 70));
        spritePanel.setOpaque(false);
        
        // Info panel with name, level, and bars
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Name and Level
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 3, 0);
        JLabel nameLabel = new JLabel(pokemon.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(Color.BLACK);
        infoPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel levelLabel = new JLabel("Lv." + pokemon.getStats().getLevel());
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        levelLabel.setForeground(Color.BLACK);
        infoPanel.add(levelLabel, gbc);
        
        // HP Bar - made to stretch horizontally
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(3, 0, 3, 0);
        gbc.weightx = 1.0;
        
        JPanel hpPanel = new JPanel(new BorderLayout(5, 0));
        hpPanel.setOpaque(false);

        JLabel hpLabelText = new JLabel("HP");
        hpLabelText.setFont(new Font("Arial", Font.BOLD, 11));
        hpLabelText.setForeground(Color.BLACK);
        hpLabelText.setPreferredSize(new Dimension(25, 18));

        JProgressBar hpBar = new JProgressBar(0, pokemon.getStats().getMaxHp());
        hpBar.setValue(pokemon.getStats().getCurrentHp());
        hpBar.setStringPainted(true);
        hpBar.setString(pokemon.getStats().getCurrentHp() + "/" + pokemon.getStats().getMaxHp());
        hpBar.setFont(new Font("Arial", Font.PLAIN, 10));
        hpBar.setPreferredSize(new Dimension(0, 18));

        // HP bar color based on percentage (EXCEPTION - keep original colors)
        double hpPercentage = (double) pokemon.getStats().getCurrentHp() / pokemon.getStats().getMaxHp();
        Color hpColor;
        if (hpPercentage > 0.5) {
            hpColor = new Color(76, 175, 80); // Green
        } else if (hpPercentage > 0.25) {
            hpColor = new Color(255, 193, 7); // Yellow
        } else {
            hpColor = new Color(244, 67, 54); // Red
        }
        hpBar.setForeground(hpColor);
        hpBar.setBackground(MENU_BG);
        
        hpPanel.add(hpLabelText, BorderLayout.WEST);
        hpPanel.add(hpBar, BorderLayout.CENTER);
        infoPanel.add(hpPanel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);

        // Experience Bar - made to stretch horizontally
        JPanel expPanel = new JPanel(new BorderLayout(5, 0));
        expPanel.setOpaque(false);
        
        JLabel expLabelText = new JLabel("EXP");
        expLabelText.setFont(new Font("Arial", Font.BOLD, 11));
        expLabelText.setForeground(Color.BLACK);
        expLabelText.setPreferredSize(new Dimension(25, 18));
        
        // Calculate experience percentage
        int currentLevelExp = pokemon.getLevelManager().getCurrentLevelExp();
        int expToNextLevel = pokemon.getLevelManager().getExpToNextLevel();
        
        JProgressBar expBar = new JProgressBar(0, currentLevelExp + expToNextLevel);
        expBar.setValue(currentLevelExp);
        expBar.setStringPainted(false);
        expBar.setPreferredSize(new Dimension(0, 18));
        expBar.setForeground(new Color(33, 150, 243)); // Blue (EXCEPTION - keep original color)
        expBar.setBackground(MENU_BG);
        
        expPanel.add(expLabelText, BorderLayout.WEST);
        expPanel.add(expBar, BorderLayout.CENTER);
        infoPanel.add(expPanel, gbc);
        
        // Held item panel on the right
        JPanel heldItemPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (pokemon.holdsItem()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    Image itemImage = pokemon.getHeldItem().getImage();
                    if (itemImage != null) {
                        int size = Math.min(getWidth(), getHeight()) - 4;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        g2d.drawImage(itemImage, x, y, size, size, this);
                    }
                    g2d.dispose();
                }
            }
        };
        heldItemPanel.setPreferredSize(new Dimension(40, 40));
        heldItemPanel.setOpaque(false);
        heldItemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        row.add(spritePanel, BorderLayout.WEST);
        row.add(infoPanel, BorderLayout.CENTER);
        row.add(heldItemPanel, BorderLayout.EAST);
        
        // Add click listener
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectPokemon(pokemon);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(MENU_BG);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(PANEL_BG);
            }
        });
        
        return row;
    }
    
    private JPanel createPokemonDetailsPanel() {
        pokemonDetailsPanel = new JPanel(new BorderLayout(10, 10));
        pokemonDetailsPanel.setBackground(PANEL_BG);
        pokemonDetailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top section - Pokémon name
        pokemonNameLabel = new JLabel("Select a Pokémon", JLabel.CENTER);
        pokemonNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        pokemonNameLabel.setForeground(Color.BLACK);
        pokemonNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Middle-top section - Pokémon image (proportionate)
        pokemonImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (selectedPokemon != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    // Draw gradient background (EXCEPTION - keep original gradient colors)
                    Color startColor = new Color(240, 248, 255);
                    Color endColor = new Color(220, 235, 250);
                    if (!selectedPokemon.getTypes().isEmpty()) {
                        Color typeColor = UIComponentFactory.getColorForType(selectedPokemon.getTypes().get(0));
                        startColor = new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 30);
                        endColor = new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 80);
                    }
                    
                    GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    
                    // Calculate proportionate size for Pokémon image
                    int imageSize = Math.min(getWidth() - 40, getHeight() - 40);
                    int x = (getWidth() - imageSize) / 2;
                    int y = (getHeight() - imageSize) / 2;
                    
                    // Draw Pokémon maintaining aspect ratio
                    PokemonView pokemonView = new PokemonView(selectedPokemon);
                    pokemonView.draw(g2d, this, x, y, imageSize, imageSize, 
                                   false, selectedPokemon.getIsShiny());
                    g2d.dispose();
                }
            }
        };
        pokemonImagePanel.setPreferredSize(new Dimension(180, 180));
        pokemonImagePanel.setBorder(BorderFactory.createLineBorder(MENU_BG, 2));
        
        // Create main content panel with info, stats, and moves
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        
        // Info section
        JPanel infoSection = createInfoTablePanel();
        
        // Bottom section with stats hexagon and moves side by side
        JPanel bottomSection = createStatsAndMovesPanel();
        
        contentPanel.add(infoSection, BorderLayout.NORTH);
        contentPanel.add(bottomSection, BorderLayout.CENTER);
        
        pokemonDetailsPanel.add(pokemonNameLabel, BorderLayout.NORTH);
        pokemonDetailsPanel.add(pokemonImagePanel, BorderLayout.CENTER);
        pokemonDetailsPanel.add(contentPanel, BorderLayout.SOUTH);
        
        return pokemonDetailsPanel;
    }
    
    private JPanel createStatsAndMovesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setOpaque(false);
        
        // Left side - Stats hexagon
        JPanel statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (selectedPokemon != null) {
                    UIComponentFactory.drawCombinedStatHexagon(g, selectedPokemon);
                }
            }
        };
        statsPanel.setPreferredSize(new Dimension(200, 200));
        statsPanel.setBackground(PANEL_BG);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Stats"),
            BorderFactory.createLineBorder(MENU_BG)
        ));
        
        // Right side - Moves panel
        JPanel movesPanel = createMovesDisplayPanel();
        
        mainPanel.add(statsPanel, BorderLayout.WEST);
        mainPanel.add(movesPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createMovesDisplayPanel() {
        JPanel movesPanel = new JPanel(new BorderLayout());
        movesPanel.setBorder(BorderFactory.createTitledBorder("Moves"));
        movesPanel.setBackground(PANEL_BG);
        
        // Create moves list panel
        JPanel movesList = new JPanel();
        movesList.setLayout(new BoxLayout(movesList, BoxLayout.Y_AXIS));
        movesList.setBackground(PANEL_BG);
        movesList.setName("movesList");
        
        // Initialize with empty moves
        for (int i = 0; i < 4; i++) {
            JPanel moveSlot = createMoveSlot(null);
            movesList.add(moveSlot);
            if (i < 3) {
                movesList.add(Box.createVerticalStrut(5));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(movesList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        
        movesPanel.add(scrollPane, BorderLayout.CENTER);
        
        return movesPanel;
    }
    
    private JPanel createMoveSlot(Move move) {
        JPanel moveSlot = new JPanel(new BorderLayout(8, 0));
        moveSlot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MENU_BG, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        moveSlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        if (move != null) {
            // Set background color based on move type (EXCEPTION - keep type colors)
            Color typeColor = UIComponentFactory.getColorForType(move.getType());
            moveSlot.setBackground(typeColor);
            
            // Create main panel with BorderLayout to separate left and right groups
            JPanel mainInfoPanel = new JPanel(new BorderLayout());
            mainInfoPanel.setOpaque(false);
            
            // Left group: Move name and type
            JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            leftGroup.setOpaque(false);
            
            // Move name
            JLabel nameLabel = new JLabel(move.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            nameLabel.setForeground(Color.WHITE);
            
            // Type badge
            JLabel typeLabel = new JLabel(move.getType().name());
            typeLabel.setOpaque(true);
            typeLabel.setBackground(UIComponentFactory.getColorForType(move.getType()));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setFont(new Font("Arial", Font.BOLD, 9));
            typeLabel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
            
            leftGroup.add(nameLabel);
            leftGroup.add(typeLabel);
            
            // Right group: Power, PP, and category icon
            JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            rightGroup.setOpaque(false);
            
            // Power with white text
            JLabel powerLabel = new JLabel("Pow: " + move.getPower());
            powerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            powerLabel.setForeground(Color.WHITE);
            
            // PP with white text
            JLabel ppLabel = new JLabel("PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
            ppLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            ppLabel.setForeground(Color.WHITE);
            
            // Category icon based on move category
            JLabel categoryIcon = new JLabel();
            categoryIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            categoryIcon.setForeground(Color.WHITE);
            
            switch (move.getCategory()) {
                case PHYSICAL:
                    categoryIcon.setText("👊");
                    break;
                case SPECIAL:
                    categoryIcon.setText("💥");
                    break;
                case STATUS:
                    categoryIcon.setText("⚡");
                    break;
                default:
                    categoryIcon.setText("");
                    break;
            }
            
            rightGroup.add(powerLabel);
            rightGroup.add(ppLabel);
            rightGroup.add(categoryIcon);
            
            // Add both groups to main panel
            mainInfoPanel.add(leftGroup, BorderLayout.WEST);
            mainInfoPanel.add(rightGroup, BorderLayout.EAST);
            
            moveSlot.add(mainInfoPanel, BorderLayout.CENTER);
        } else {
            // Empty slot styling
            moveSlot.setBackground(PANEL_BG);
            JLabel emptyLabel = new JLabel("Empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            emptyLabel.setForeground(Color.BLACK);
            moveSlot.add(emptyLabel, BorderLayout.CENTER);
        }
        
        return moveSlot;
    }
    
    private void updateInfoPanel(JPanel gridPanel, int currentExp, int xpToNext, String heldItemText) {
        Component[] rows = gridPanel.getComponents();
        
        String[][] data = {
            {"Level", String.valueOf(selectedPokemon.getStats().getLevel()), "XP", String.valueOf(currentExp)},
            {"Nature", selectedPokemon.getNature().getDisplayName(), "XP to Next", String.valueOf(xpToNext)},
            {"ID", String.valueOf(selectedPokemon.getId()), "Ability", selectedPokemon.getAbilities().get(0)},
            {"Held Item", heldItemText, "Type(s)", ""}
        };
        
        int rowIndex = 0;
        for (Component row : rows) {
            if (row instanceof JPanel && rowIndex < data.length) {
                Component[] pairs = ((JPanel) row).getComponents();
                int pairIndex = 0;
                
                for (Component pair : pairs) {
                    if (pair instanceof JPanel && pairIndex < 2) {
                        Component[] elements = ((JPanel) pair).getComponents();
                        
                        for (Component element : elements) {
                             if (element instanceof JPanel && "typePanel".equals(element.getName())) {
                                // Handle types specially (EXCEPTION - keep type colors)
                                JPanel typePanel = (JPanel) element;
                                typePanel.removeAll();
                                
                                for (PokemonType type : selectedPokemon.getTypes()) {
                                    JLabel typeLabel = new JLabel(type.name());
                                    typeLabel.setOpaque(true);
                                    typeLabel.setBackground(UIComponentFactory.getColorForType(type));
                                    typeLabel.setForeground(Color.WHITE);
                                    typeLabel.setFont(new Font("Arial", Font.BOLD, 10));
                                    typeLabel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(UIComponentFactory.getColorForType(type).darker(), 1),
                                        BorderFactory.createEmptyBorder(2, 6, 2, 6)
                                    ));
                                    typePanel.add(typeLabel);
                                }
                                typePanel.revalidate();
                                typePanel.repaint();
                            } else if (element instanceof JLabel) {
                                JLabel label = (JLabel) element;
                                if (label.getFont().isBold()) {
                                    // This is a label
                                    label.setText(data[rowIndex][pairIndex * 2] + ":");
                                } else {
                                    // This is a value
                                    label.setText(data[rowIndex][pairIndex * 2 + 1]);
                                    
                                    // Add button for held item row
                                    if (rowIndex == 3 && pairIndex == 0) {
                                        // Remove any existing button first
                                        JPanel parentPanel = (JPanel) label.getParent();
                                        Component[] parentComponents = parentPanel.getComponents();
                                        for (Component comp : parentComponents) {
                                            if (comp instanceof JButton) {
                                                parentPanel.remove(comp);
                                                break;
                                            }
                                        }
                                        
                                        // Create the button
                                        JButton itemButton = new JButton();
                                        itemButton.setFont(new Font("Arial", Font.BOLD, 10));
                                        itemButton.setBackground(MENU_BG);
                                        itemButton.setForeground(Color.WHITE);
                                        itemButton.setFocusPainted(false);
                                        itemButton.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createRaisedBevelBorder(),
                                            BorderFactory.createEmptyBorder(2, 8, 2, 8)
                                        ));
                                        
                                        // Set button text based on whether Pokemon has item
                                        if (selectedPokemon.holdsItem()) {
                                            itemButton.setText("Take Item");
                                        } else {
                                            itemButton.setText("Give Item");
                                        }
                                        
                                        if (itemButton.getText().equals("Take Item")) {
                                            itemButton.addActionListener(e -> {
                                                // TAKE ITEM METHOD
                                            });
                                        } else {
                                            itemButton.addActionListener(e -> {
                                                // GIVE ITEM METHOD
                                            });
                                        }
                                        
                                        // Add button to the panel
                                        // parentPanel.add(itemButton);
                                        parentPanel.revalidate();
                                        parentPanel.repaint();
                                    }
                                }
                            }
                        }
                        pairIndex++;
                    }
                }
                rowIndex++;
            }
        }
    }
    
    private JPanel createInfoTablePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create a panel with BoxLayout for natural spacing
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setName("infoGrid");
        
        // Create 4 rows of info
        for (int i = 0; i < 4; i++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
            rowPanel.setOpaque(false);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            
            // Left info pair
            JPanel leftPair = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            leftPair.setOpaque(false);
            
            JLabel leftLabel = new JLabel("-");
            leftLabel.setFont(new Font("Arial", Font.BOLD, 12));
            leftLabel.setForeground(Color.BLACK);
            leftLabel.setPreferredSize(new Dimension(70, 20));
            
            JLabel leftValue = new JLabel("-");
            leftValue.setFont(new Font("Arial", Font.PLAIN, 12));
            leftValue.setForeground(Color.BLACK);
            leftValue.setPreferredSize(new Dimension(80, 20));
            
            leftPair.add(leftLabel);
            leftPair.add(leftValue);
            
            // Right info pair
            JPanel rightPair = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            rightPair.setOpaque(false);
            
            JLabel rightLabel = new JLabel("-");
            rightLabel.setFont(new Font("Arial", Font.BOLD, 12));
            rightLabel.setForeground(Color.BLACK);
            rightLabel.setPreferredSize(new Dimension(70, 20));
            
            // Special handling for types row
            if (i == 3) {
                JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
                typePanel.setOpaque(false);
                typePanel.setName("typePanel");
                rightPair.add(typePanel);
            } else {
                JLabel rightValue = new JLabel("-");
                rightValue.setFont(new Font("Arial", Font.PLAIN, 12));
                rightValue.setForeground(Color.BLACK);
                rightValue.setPreferredSize(new Dimension(80, 20));
                rightPair.add(rightValue);
            }
            
            rightPair.add(rightLabel, 0);
            
            rowPanel.add(leftPair);
            rowPanel.add(rightPair);
            infoPanel.add(rowPanel);
            
            if (i < 3) {
                infoPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private void selectPokemon(Pokemon pokemon) {
        this.selectedPokemon = pokemon;
        updatePokemonInfo();
    }
    
    private void updatePokemonInfo() {
        if (selectedPokemon == null) {
            pokemonNameLabel.setText("Select a Pokémon");
            
            // Find and clear grid data
            JPanel gridPanel = findInfoGridPanel(pokemonDetailsPanel);
            if (gridPanel != null) {
                clearInfoPanel(gridPanel);
            }
            
            // Clear moves
            updateMovesDisplay(null);
        } else {
            pokemonNameLabel.setText(selectedPokemon.getName());
            
            // Calculate XP values using LevelManager
            int currentExp = selectedPokemon.getLevelManager().getCurrentExp();
            int xpToNext = selectedPokemon.getLevelManager().getExpToNextLevel();
            
            // Held item
            String heldItemText = selectedPokemon.holdsItem() ? 
                selectedPokemon.getHeldItem().getName() : "None";
            
            // Find and update grid data
            JPanel gridPanel = findInfoGridPanel(pokemonDetailsPanel);
            if (gridPanel != null) {
                updateInfoPanel(gridPanel, currentExp, xpToNext, heldItemText);
            }
            
            // Update moves display
            updateMovesDisplay(selectedPokemon);
        }
        
        pokemonImagePanel.repaint();
        pokemonDetailsPanel.repaint();
    }
    
    // Helper methods that are also needed
    private void clearInfoPanel(JPanel gridPanel) {
        Component[] rows = gridPanel.getComponents();
        for (Component row : rows) {
            if (row instanceof JPanel) {
                Component[] pairs = ((JPanel) row).getComponents();
                for (Component pair : pairs) {
                    if (pair instanceof JPanel) {
                        Component[] elements = ((JPanel) pair).getComponents();
                        for (Component element : elements) {
                            if (element instanceof JLabel) {
                                ((JLabel) element).setText("-");
                            } else if (element instanceof JPanel && "typePanel".equals(element.getName())) {
                                ((JPanel) element).removeAll();
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateMovesDisplay(Pokemon pokemon) {
        // Find the moves list panel
        JPanel movesList = findMovesListPanel(pokemonDetailsPanel);
        if (movesList != null) {
            movesList.removeAll();
            
            if (pokemon != null && pokemon.getMoves() != null) {
                List<Move> moves = pokemon.getMoves();
                for (int i = 0; i < 4; i++) {
                    Move move = i < moves.size() ? moves.get(i) : null;
                    JPanel moveSlot = createMoveSlot(move);
                    movesList.add(moveSlot);
                    if (i < 3) {
                        movesList.add(Box.createVerticalStrut(5));
                    }
                }
            } else {
                // Add empty slots
                for (int i = 0; i < 4; i++) {
                    JPanel moveSlot = createMoveSlot(null);
                    movesList.add(moveSlot);
                    if (i < 3) {
                        movesList.add(Box.createVerticalStrut(5));
                    }
                }
            }
            
            movesList.revalidate();
            movesList.repaint();
        }
    }
    
    // Helper method to find the info grid panel
    private JPanel findInfoGridPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel && "infoGrid".equals(component.getName())) {
                return (JPanel) component;
            }
            if (component instanceof Container) {
                JPanel found = findInfoGridPanel((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    // Helper method to find the moves list panel
    private JPanel findMovesListPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel && "movesList".equals(component.getName())) {
                return (JPanel) component;
            }
            if (component instanceof Container) {
                JPanel found = findMovesListPanel((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
