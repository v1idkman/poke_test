package ui;

import java.awt.*;
import javax.swing.*;
import model.Player;

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
    
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] items = {"Poké Ball", "Great Ball", "Ultra Ball", "Potion", 
                          "Super Potion", "Hyper Potion", "Revive", "Escape Rope"};
        
        for (String item : items) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            JLabel itemLabel = new JLabel(item, JLabel.CENTER);
            JLabel countLabel = new JLabel("x5", JLabel.CENTER);
            
            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(countLabel, BorderLayout.SOUTH);
            
            panel.add(itemPanel);
        }
        
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
        statsPanel.add(new JLabel("#" + (int)(Math.random() * 100000)));
        statsPanel.add(new JLabel("Money:"));
        statsPanel.add(new JLabel("₽" + (int)(Math.random() * 10000)));
        statsPanel.add(new JLabel("Pokédex:"));
        statsPanel.add(new JLabel("25 seen, 12 caught"));
        statsPanel.add(new JLabel("Play Time:"));
        statsPanel.add(new JLabel("12:34"));
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
}
