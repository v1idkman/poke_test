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
    
    public PokemonPanelManager(Player player) {
        this.player = player;
    }
    
    public JPanel createPokemonTeamPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        if (player == null || player.getTeam() == null || player.getTeam().isEmpty()) {
            JLabel emptyLabel = new JLabel("You don't have any Pokémon yet", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            emptyLabel.setForeground(new Color(120, 120, 120));
            mainPanel.add(emptyLabel, BorderLayout.CENTER);
            return mainPanel;
        }
        
        // Split screen evenly into two halves
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);
        
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
        teamPanel.setBackground(new Color(70, 130, 180));
        teamPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create vertical list for Pokémon (only actual team members, no empty slots)
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(70, 130, 180));
        
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
        scrollPane.setBackground(new Color(70, 130, 180));
        scrollPane.getViewport().setBackground(new Color(70, 130, 180));
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        teamPanel.add(scrollPane, BorderLayout.CENTER);
        return teamPanel;
    }
    
    
    private JPanel createHorizontalPokemonRow(Pokemon pokemon) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setPreferredSize(new Dimension(350, 90)); // Increased height from 70 to 90
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBackground(new Color(200, 220, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8) // Increased padding
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
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15)); // Increased font size
        nameLabel.setForeground(new Color(40, 40, 40));
        infoPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel levelLabel = new JLabel("Lv." + pokemon.getStats().getLevel());
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 13)); // Increased font size
        levelLabel.setForeground(new Color(60, 60, 60));
        infoPanel.add(levelLabel, gbc);
        
        // HP Bar - made to stretch horizontally
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(3, 0, 3, 0);
        gbc.weightx = 1.0; // Add this line to allow horizontal stretching

        JPanel hpPanel = new JPanel(new BorderLayout(5, 0));
        hpPanel.setOpaque(false);

        JLabel hpLabelText = new JLabel("HP");
        hpLabelText.setFont(new Font("Arial", Font.BOLD, 11));
        hpLabelText.setForeground(new Color(40, 40, 40));
        hpLabelText.setPreferredSize(new Dimension(25, 18));

        JProgressBar hpBar = new JProgressBar(0, pokemon.getStats().getMaxHp());
        hpBar.setValue(pokemon.getStats().getCurrentHp());
        hpBar.setStringPainted(true);
        hpBar.setString(pokemon.getStats().getCurrentHp() + "/" + pokemon.getStats().getMaxHp());
        hpBar.setFont(new Font("Arial", Font.PLAIN, 10));
        // Remove this line: hpBar.setPreferredSize(new Dimension(220, 18));
        hpBar.setPreferredSize(new Dimension(0, 18)); // Set width to 0 to let it expand

        // HP bar color based on percentage
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
        hpBar.setBackground(new Color(220, 220, 220));
        
        hpPanel.add(hpLabelText, BorderLayout.WEST);
        hpPanel.add(hpBar, BorderLayout.CENTER);
        infoPanel.add(hpPanel, gbc);

        hpPanel.add(hpLabelText, BorderLayout.WEST);
        hpPanel.add(hpBar, BorderLayout.CENTER);
        infoPanel.add(hpPanel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);

        // Experience Bar - made to stretch horizontally
        JPanel expPanel = new JPanel(new BorderLayout(5, 0));
        expPanel.setOpaque(false);

        JLabel expLabelText = new JLabel("EXP");
        expLabelText.setFont(new Font("Arial", Font.BOLD, 11));
        expLabelText.setForeground(new Color(40, 40, 40));
        expLabelText.setPreferredSize(new Dimension(25, 18));
        
        // Calculate experience percentage
        int currentLevel = pokemon.getStats().getLevel();
        int expForCurrentLevel = currentLevel * currentLevel * 100;
        int expForNextLevel = (currentLevel + 1) * (currentLevel + 1) * 100;
        int currentExp = expForCurrentLevel + (int)(Math.random() * (expForNextLevel - expForCurrentLevel));
        
        JProgressBar expBar = new JProgressBar(expForCurrentLevel, expForNextLevel);
        expBar.setValue(currentExp);
        expBar.setStringPainted(false);
        expBar.setPreferredSize(new Dimension(0, 18)); 
        expBar.setForeground(new Color(33, 150, 243)); // Blue
        expBar.setBackground(new Color(220, 220, 220));

        expPanel.add(expLabelText, BorderLayout.WEST);
        expPanel.add(expBar, BorderLayout.CENTER);
        infoPanel.add(expPanel, gbc);
        
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
                // If no item, panel remains empty but maintains spacing
            }
        };
        heldItemPanel.setPreferredSize(new Dimension(40, 40));
        heldItemPanel.setOpaque(false);
        heldItemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        row.add(spritePanel, BorderLayout.WEST);
        row.add(infoPanel, BorderLayout.CENTER);
        row.add(heldItemPanel, BorderLayout.EAST); // Add held item panel
        
        // Add click listener
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectPokemon(pokemon);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(220, 235, 250));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(new Color(200, 220, 240));
            }
        });
        
        return row;
    }
    
    private JPanel createPokemonDetailsPanel() {
        pokemonDetailsPanel = new JPanel(new BorderLayout(10, 10));
        pokemonDetailsPanel.setBackground(new Color(250, 250, 250));
        pokemonDetailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top section - Pokémon name
        pokemonNameLabel = new JLabel("Select a Pokémon", JLabel.CENTER);
        pokemonNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        pokemonNameLabel.setForeground(new Color(50, 50, 50));
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
                    
                    // Draw gradient background
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
        pokemonImagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        
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
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Stats"),
            BorderFactory.createLineBorder(new Color(200, 200, 200))
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
        movesPanel.setBackground(Color.WHITE);
        
        // Create moves list panel
        JPanel movesList = new JPanel();
        movesList.setLayout(new BoxLayout(movesList, BoxLayout.Y_AXIS));
        movesList.setBackground(Color.WHITE);
        movesList.setName("movesList"); // For easy identification
        
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
        JPanel moveSlot = new JPanel(new BorderLayout(8, 3));
        moveSlot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        moveSlot.setBackground(new Color(248, 248, 248));
        moveSlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        if (move != null) {
            // Move name
            JLabel nameLabel = new JLabel(move.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            nameLabel.setForeground(new Color(50, 50, 50));
            
            // Move details panel
            JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            detailsPanel.setOpaque(false);
            
            // Type badge
            JLabel typeLabel = new JLabel(move.getType().name());
            typeLabel.setOpaque(true);
            typeLabel.setBackground(UIComponentFactory.getColorForType(move.getType()));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setFont(new Font("Arial", Font.BOLD, 9));
            typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            
            // Power and PP
            JLabel powerLabel = new JLabel("Pow: " + move.getPower());
            powerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            powerLabel.setForeground(new Color(100, 100, 100));
            
            JLabel ppLabel = new JLabel("PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
            ppLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            ppLabel.setForeground(new Color(100, 100, 100));
            
            detailsPanel.add(typeLabel);
            detailsPanel.add(powerLabel);
            detailsPanel.add(ppLabel);
            
            moveSlot.add(nameLabel, BorderLayout.NORTH);
            moveSlot.add(detailsPanel, BorderLayout.CENTER);
        } else {
            JLabel emptyLabel = new JLabel("Empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            emptyLabel.setForeground(new Color(150, 150, 150));
            moveSlot.add(emptyLabel, BorderLayout.CENTER);
        }
        
        return moveSlot;
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
            
            // Calculate XP values
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
        pokemonDetailsPanel.repaint(); // Refresh stats hexagon
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
            leftLabel.setForeground(new Color(80, 80, 80));
            leftLabel.setPreferredSize(new Dimension(70, 20));
            
            JLabel leftValue = new JLabel("-");
            leftValue.setFont(new Font("Arial", Font.PLAIN, 12));
            leftValue.setForeground(new Color(50, 50, 50));
            leftValue.setPreferredSize(new Dimension(80, 20));
            
            leftPair.add(leftLabel);
            leftPair.add(leftValue);
            
            // Right info pair
            JPanel rightPair = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            rightPair.setOpaque(false);
            
            JLabel rightLabel = new JLabel("-");
            rightLabel.setFont(new Font("Arial", Font.BOLD, 12));
            rightLabel.setForeground(new Color(80, 80, 80));
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
                rightValue.setForeground(new Color(50, 50, 50));
                rightValue.setPreferredSize(new Dimension(80, 20));
                rightPair.add(rightValue);
            }
            
            rightPair.add(rightLabel, 0); // Insert label at beginning
            
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
                            if (element instanceof JLabel) {
                                JLabel label = (JLabel) element;
                                if (label.getFont().isBold()) {
                                    // This is a label
                                    label.setText(data[rowIndex][pairIndex * 2] + ":");
                                } else {
                                    // This is a value
                                    label.setText(data[rowIndex][pairIndex * 2 + 1]);
                                }
                            } else if (element instanceof JPanel && "typePanel".equals(element.getName())) {
                                // Handle types specially
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
                            }
                        }
                        pairIndex++;
                    }
                }
                rowIndex++;
            }
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
    
    private void selectPokemon(Pokemon pokemon) {
        this.selectedPokemon = pokemon;
        updatePokemonInfo();
    }
    
    // Keep existing methods for compatibility
    public void updatePokemonDetailsPanel(Pokemon pokemon, JLabel nameLabel, JLabel statsLabel, 
                                        JPanel imagePanel, PokemonView pokemonView) {
        // Legacy method - kept for compatibility
    }
    
    public static JPanel createPokemonSelectionPanel(Pokemon pokemon, boolean enabled) {
        // Legacy method - kept for compatibility
        return new JPanel();
    }
}
