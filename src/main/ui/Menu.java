package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

import model.Player;
import model.Item;
import pokes.Pokemon;

import java.util.List;

public class Menu {
    private static Menu instance;
    
    private JButton menuButton;
    private JDialog menuDialog;
    private Timer gameTimer;
    private Player player;
    
    private ItemManager itemManager;
    private PokemonPanelManager pokemonManager;
    
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
        menuButton.setPreferredSize(new Dimension(60, 30));
        
        // Add action listener to open menu
        menuButton.addActionListener(e -> openPlayerMenu(board));
        
        // Don't add to board or position it here - that will be done by WorldManager
    }
    
    
    // Set the current player for the menu
    public void setPlayer(Player player) {
        this.player = player;
        this.itemManager = new ItemManager(player, menuDialog, menuButton);
        this.pokemonManager = new PokemonPanelManager(player, menuDialog);
    }
    
    // Set the game timer for pausing/resuming
    public void setGameTimer(Timer timer) {
        this.gameTimer = timer;
    }
    
    public void openPlayerMenu(JPanel parentBoard) {
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
        
        // Reinitialize managers with the new dialog
        this.itemManager = new ItemManager(player, menuDialog, menuButton);
        this.pokemonManager = new PokemonPanelManager(player, menuDialog);
        
        // Create tabbed pane for different menu sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add tabs for different menu sections
        tabbedPane.addTab("Pokémon", createPokemonPanel());
        tabbedPane.addTab("Inventory", createInventoryPanel());
        tabbedPane.addTab("Gym Badges", createBadgesPanel());
        tabbedPane.addTab("Trainer Card", createTrainerPanel());
        tabbedPane.addTab("Map", createMapPanel());
        
        // Add close button
        JButton closeButton = new JButton("Close Menu");
        closeButton.addActionListener(e -> {
            menuDialog.dispose();
            if (gameTimer != null) {
                gameTimer.start();
            }
            
            SwingUtilities.invokeLater(() -> {
                parentBoard.requestFocusInWindow();
            });
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
            spritePanel.setPreferredSize(new Dimension(60, 60));
            
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
            
            JLabel itemLabel;
            if (pokemon.getHeldItem() != null) {
                itemLabel = new JLabel(new ImageIcon(pokemon.getHeldItem().getImage()));
            } else {
                itemLabel = new JLabel() {
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(30, 30);
                    }
                    
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                    }
                };
            }
            itemLabel.setVerticalAlignment(SwingConstants.TOP);
            itemLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            pokemonPanel.add(itemLabel, BorderLayout.EAST);
            
            // Add click listener to show Pokémon details
            pokemonPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Update details panel with selected Pokémon
                    pokemonManager.updatePokemonDetailsPanel(pokemon, pokemonNameLabel, pokemonStatsLabel, 
                                                           pokemonImagePanel, pokemonView);
                }
            });
            
            pokemonListPanel.add(pokemonPanel);
        }
        
        // Display the first Pokémon's details by default
        if (!team.isEmpty()) {
            pokemonManager.updatePokemonDetailsPanel(team.get(0), pokemonNameLabel, pokemonStatsLabel, 
                                                   pokemonImagePanel, new PokemonView(team.get(0)));
        }
        
        // Create a split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pokemonListPanel, detailsPanel);
        splitPane.setDividerLocation(300);
        splitPane.setEnabled(false); // Prevent user from moving the divider
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    public void refreshPokemonPanel() {
        // Get the current tab index
        int currentTab = 0;
        Container contentPane = menuDialog.getContentPane();
        if (contentPane.getComponent(0) instanceof JPanel) {
            JPanel mainPanel = (JPanel) contentPane.getComponent(0);
            if (mainPanel.getComponent(0) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) mainPanel.getComponent(0);
                currentTab = tabbedPane.getSelectedIndex();
                
                // Replace the Pokémon panel with a fresh one
                tabbedPane.setComponentAt(0, createPokemonPanel());
                
                // Restore the selected tab
                tabbedPane.setSelectedIndex(currentTab);
            }
        }
    }
    
    public void refreshInventoryPanel() {
        if (menuDialog != null && menuDialog.isVisible()) {
            // Get the tabbed pane
            Container contentPane = menuDialog.getContentPane();
            if (contentPane.getComponentCount() > 0 && contentPane.getComponent(0) instanceof JPanel) {
                JPanel mainPanel = (JPanel) contentPane.getComponent(0);
                if (mainPanel.getComponentCount() > 0 && mainPanel.getComponent(0) instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) mainPanel.getComponent(0);
                    
                    // Get the current selected tab index
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    
                    // Replace the inventory tab with a fresh one
                    tabbedPane.setComponentAt(1, createInventoryPanel());
                    
                    // Restore the selected tab
                    tabbedPane.setSelectedIndex(selectedIndex);
                    
                    // Force repaint
                    tabbedPane.revalidate();
                    tabbedPane.repaint();
                }
            }
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
            String type = itemManager.getItemType(item);
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
    
    private JPanel createItemTypePanel(List<Item> items, String itemType) {
        // Main panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        // Left side - scrollable item grid
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Create a panel with FlowLayout instead of GridLayout
        JPanel itemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 10));

        // Calculate rows needed based on item count
        int rows = (int)Math.ceil(items.size() / 5.0); // 5 items per row
        int preferredHeight = rows * 65; // 60px for item + 5px for spacing
        itemsPanel.setPreferredSize(new Dimension(300, preferredHeight));
        
        // Configure scroll pane with proper policies
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getViewport().setAlignmentY(Component.TOP_ALIGNMENT);
        
        // Right side - item information panel
        JPanel infoPanel = new JPanel(new BorderLayout(0, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Item Info"));
        infoPanel.setPreferredSize(new Dimension(180, 0));
        
        // Components for the info panel
        JLabel itemNameLabel = new JLabel();
        itemNameLabel.setFont(new Font("Lato", Font.BOLD, 18));
        itemNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel itemDescLabel = new JLabel();
        itemDescLabel.setFont(new Font("Lato", Font.PLAIN, 14));
        
        // Create a panel for the item image
        JPanel itemImagePanel = new JPanel(new BorderLayout());
        itemImagePanel.setPreferredSize(new Dimension(100, 100));
        itemImagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Layout the info panel
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(itemImagePanel, BorderLayout.CENTER);
        
        infoPanel.add(imageContainer, BorderLayout.NORTH);
        infoPanel.add(itemNameLabel, BorderLayout.CENTER);
        infoPanel.add(itemDescLabel, BorderLayout.SOUTH);
        
        // Check if there are items of this type
        if (items.isEmpty()) {
            itemNameLabel.setText("No Items");
            itemDescLabel.setText("<html>No items of type " + itemType + " available</html>");
        } else {
            // Add each item to the grid panel
            for (Item item : items) {
                // Create a small square panel for each item
                JPanel itemPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (item.getImage() != null) {
                            Image img = item.getImage();
                            int size = Math.min(getWidth(), getHeight()) - 10; // Smaller padding
                            g.drawImage(img, (getWidth() - size)/2, (getHeight() - size)/2, size, size, this);
                        }
                    }
                };
                
                // Set larger fixed size for the item squares
                itemPanel.setPreferredSize(new Dimension(60, 60));
                itemPanel.setMinimumSize(new Dimension(60, 60));
                
                itemPanel.setToolTipText(item.getName());
                
                // Add click listener to show item info
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Update info panel with selected item details
                        itemManager.updateItemInfoPanel(item, itemNameLabel, itemDescLabel, itemImagePanel, infoPanel);
                        
                        if (e.getClickCount() == 2) {
                            // Get a fresh reference to the item from inventory
                            Item currentItem = null;
                            for (Item inventoryItem : player.getInventory()) {
                                if (inventoryItem.equals(item)) {
                                    currentItem = inventoryItem;
                                    break;
                                }
                            }
                            
                            // Use the fresh reference instead of the potentially stale one
                            if (currentItem != null) {
                                boolean used = currentItem.use(player);
                                if (used && currentItem.getQuantity() <= 0) {
                                    player.removeItem(currentItem);
                                    menuDialog.dispose();
                                    openPlayerMenu((JPanel)menuButton.getParent());
                                }
                            }
                        }
                    }
                });
                
                itemsPanel.add(itemPanel);
            }
            
            // Display the first item's info by default
            itemManager.updateItemInfoPanel(items.get(0), itemNameLabel, itemDescLabel, itemImagePanel, infoPanel);
        }
        
        scrollPane.setViewportView(itemsPanel);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add both panels to the main panel
        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.EAST);
        
        return panel;
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

    public void positionMenuButtonForWindow(JLayeredPane layeredPane, int windowWidth, int windowHeight) {
        if (menuButton != null) {
            // Remove button from its current parent if it exists
            Container parent = menuButton.getParent();
            if (parent != null) {
                parent.remove(menuButton);
            }
            
            // Position button at bottom right of window
            int buttonWidth = menuButton.getPreferredSize().width;
            int buttonHeight = menuButton.getPreferredSize().height;
            menuButton.setBounds(windowWidth - buttonWidth - 10, 
                                windowHeight - buttonHeight - 10,
                                buttonWidth, buttonHeight);
            
            // Add to layered pane with higher layer value to ensure visibility
            layeredPane.add(menuButton, JLayeredPane.PALETTE_LAYER);
            layeredPane.setLayer(menuButton, JLayeredPane.PALETTE_LAYER);
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }
    

    public Component createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        return panel;
    }

    public JButton getMenuButton() {
        return menuButton;
    }
}