package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

import model.Player;
import model.Item;
import model.Medicine;
import model.Pokeball;
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
    
    // The panel creation methods remain mostly the same
    private JPanel createPokemonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Your Pokémon team will appear here", JLabel.CENTER);
        placeholder.setFont(new Font("Lato", Font.PLAIN, 18));
        panel.add(placeholder, BorderLayout.CENTER);
        return panel;
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
            
            boolean hasBadge = Math.random() > 0.5; // Random for demo
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