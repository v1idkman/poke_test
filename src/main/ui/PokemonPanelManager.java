package ui;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import java.util.List;

import model.Player;
import model.Item;
import model.KeyItem;
import model.Move;
import pokes.Pokemon;
import pokes.Pokemon.PokemonType;

public class PokemonPanelManager {
    private Player player;
    private JDialog menuDialog;
    
    public PokemonPanelManager(Player player, JDialog menuDialog) {
        this.player = player;
        this.menuDialog = menuDialog;
    }
    
    public void updatePokemonDetailsPanel(Pokemon pokemon, JLabel nameLabel, JLabel statsLabel, 
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
        basicInfoPanel.add(new JLabel("Nature:"), gbc);
        gbc.gridx = 1;
        basicInfoPanel.add(new JLabel(String.valueOf(pokemon.getNature().getDisplayName())), gbc);

        // Type (label)
        gbc.gridx = 0; gbc.gridy = 2;
        basicInfoPanel.add(new JLabel("Type:"), gbc);

        // Type (panel with multiple types)
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        for (PokemonType type : pokemon.getTypes()) {
            JLabel typeLabel = new JLabel(type.name());
            typeLabel.setOpaque(true);
            typeLabel.setBackground(UIComponentFactory.getColorForType(type));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            typePanel.add(typeLabel);
        }
        gbc.gridx = 1;
        basicInfoPanel.add(typePanel, gbc);

        // In the updatePokemonDetailsPanel method, modify the held item section:
        gbc.gridx = 0; gbc.gridy = 3;
        basicInfoPanel.add(new JLabel("Held Item:"), gbc);
        gbc.gridx = 1;

        // Create a panel to hold both the item label and the button
        JPanel heldItemPanel = new JPanel(new BorderLayout(5, 0));
        JLabel itemLabel = new JLabel(pokemon.holdsItem() ? pokemon.getHeldItem().getName() : "Nothing");
        heldItemPanel.add(itemLabel, BorderLayout.CENTER);

        // Create the button based on whether the Pokémon is holding an item
        JButton itemButton = new JButton(pokemon.holdsItem() ? "Take" : "Give");
        itemButton.setFont(new Font("Lato", Font.PLAIN, 10));
        itemButton.setMargin(new Insets(1, 3, 1, 3));
        itemButton.addActionListener(e -> {
            if (pokemon.holdsItem()) {
                // Take the item from the Pokémon
                Item takenItem = pokemon.getHeldItem();
                pokemon.holdItem(null);
                // Add the original item to inventory
                player.addToInventory(takenItem);
                
                // Update the label and button
                itemLabel.setText("Nothing");
                itemButton.setText("Give");
                
                // Refresh both panels
                Menu.getInstance().refreshPokemonPanel();
                Menu.getInstance().refreshInventoryPanel();
            } else {
                // Show dialog to select an item to give
                showItemSelectionDialog(pokemon);
            }
        });        

        heldItemPanel.add(itemButton, BorderLayout.EAST);
        basicInfoPanel.add(heldItemPanel, gbc);
                
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
    
    private void showItemSelectionDialog(Pokemon pokemon) {
        // Create dialog for selecting an item
        JDialog itemDialog = new JDialog(menuDialog, "Select Item", true);
        itemDialog.setSize(300, 400);
        itemDialog.setLocationRelativeTo(menuDialog);
        
        JPanel dialogMainPanel = new JPanel(new BorderLayout(10, 10));
        dialogMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all items from player's inventory
        Set<Item> inventory = player.getInventory();
        
        // Create a panel for the items list
        JPanel itemsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        // Add each item to the list
        for (Item item : inventory) {
            if (!(item instanceof KeyItem)) {
                JPanel itemPanel = new JPanel(new BorderLayout(5, 0));
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
                // Create a panel for the item image
                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (item.getImage() != null) {
                            g.drawImage(item.getImage(), 5, 5, 30, 30, this);
                        }
                    }
                };
                imagePanel.setPreferredSize(new Dimension(40, 40));
                
                JLabel nameLabel = new JLabel(item.getName());
                
                itemPanel.add(imagePanel, BorderLayout.WEST);
                itemPanel.add(nameLabel, BorderLayout.CENTER);
                
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Give the item to the Pokémon
                        pokemon.holdItem(item);
                        player.removeItem(item);
                        
                        // Close dialog before refreshing
                        itemDialog.dispose();
                        
                        // Refresh panels
                        Menu.getInstance().refreshInventoryPanel();
                        Menu.getInstance().refreshPokemonPanel();
                    }
                });
                
                itemsPanel.add(itemPanel);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        
        // Add cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> itemDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);
        
        dialogMainPanel.add(scrollPane, BorderLayout.CENTER);
        dialogMainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        itemDialog.add(dialogMainPanel);
        itemDialog.setVisible(true);
    }
    
    public void openMovesPanel(Pokemon pokemon) {
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
                typeLabel.setBackground(UIComponentFactory.getColorForType(move.getType()));
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
    
    public void openStatsPanel(Pokemon pokemon) {
        JDialog statsDialog = new JDialog(menuDialog, "Stats - " + pokemon.getName(), true);
        statsDialog.setSize(450, 600);
        statsDialog.setResizable(false);
        statsDialog.setLocationRelativeTo(menuDialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));
        
        // Create a more compact Pokémon info panel
        JPanel pokemonInfoPanel = new JPanel(new BorderLayout(5, 0));
        
        JLabel nameLabel = new JLabel(pokemon.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 20));
        
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
        
        // Stat hexagon panel
        JPanel statHexagonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                UIComponentFactory.drawCombinedStatHexagon(g, pokemon);
            }
        };
        statHexagonPanel.setPreferredSize(new Dimension(300, 300));
        
        // Create a legend for the hexagon colors
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        
        JPanel ivColorSample = new JPanel();
        ivColorSample.setBackground(new Color(30, 201, 139, 150)); // Green for IVs
        ivColorSample.setPreferredSize(new Dimension(20, 20));
        legendPanel.add(ivColorSample);
        legendPanel.add(new JLabel("IVs"));
        
        JPanel evColorSample = new JPanel();
        evColorSample.setBackground(new Color(255, 165, 0, 150)); // Orange for EVs
        evColorSample.setPreferredSize(new Dimension(20, 20));
        legendPanel.add(evColorSample);
        legendPanel.add(new JLabel("EVs"));
        
        // Numeric stats panel
        JPanel numericStatsPanel = UIComponentFactory.createNumericStatsPanel(pokemon);
        
        // Assemble the stats display panel
        JPanel statsDisplayPanel = new JPanel(new BorderLayout(10, 10));
        statsDisplayPanel.add(legendPanel, BorderLayout.NORTH);
        statsDisplayPanel.add(statHexagonPanel, BorderLayout.CENTER);
        statsDisplayPanel.add(numericStatsPanel, BorderLayout.SOUTH);
        
        // Close button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> statsDialog.dispose());
        buttonPanel.add(closeButton, BorderLayout.LINE_END);
        
        // Assemble the main panel
        mainPanel.add(pokemonInfoPanel, BorderLayout.NORTH);
        mainPanel.add(statsDisplayPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        statsDialog.add(mainPanel);
        statsDialog.setVisible(true);
    }
    
    public static JPanel createPokemonSelectionPanel(Pokemon pokemon, boolean enabled) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Create a panel for the Pokémon sprite
        JPanel spritePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                PokemonView pokemonView = new PokemonView(pokemon);
                pokemonView.draw(g, this, 5, 5, getWidth() - 10, getHeight() - 10, false, pokemon.getIsShiny());
            }
        };
        spritePanel.setPreferredSize(new Dimension(50, 50));
        
        // Create info panel with name and HP
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel(pokemon.getName()), BorderLayout.NORTH);
        
        JProgressBar hpBar = new JProgressBar(0, pokemon.getStats().getMaxHp());
        hpBar.setValue(pokemon.getStats().getCurrentHp());
        hpBar.setForeground(new Color(30, 201, 139));
        hpBar.setStringPainted(true);
        hpBar.setString(pokemon.getStats().getCurrentHp() + "/" + pokemon.getStats().getMaxHp());
        infoPanel.add(hpBar, BorderLayout.SOUTH);
        
        panel.add(spritePanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Gray out the panel if the item can't be used on this Pokémon
        if (!enabled) {
            panel.setEnabled(false);
            panel.setBackground(Color.LIGHT_GRAY);
            infoPanel.setBackground(Color.LIGHT_GRAY);
            spritePanel.setBackground(Color.LIGHT_GRAY);
        } else {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        return panel;
    }
}
