package ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import model.Player;
import model.Player.MovementState;

public class Menu {
    private static Menu instance;
    
    private JButton menuButton;
    private JPanel menuOverlay;
    private Timer gameTimer;
    private Player player;
    private JLayeredPane gameLayeredPane;
    private Board gameBoard;
    
    private ItemManager itemManager;
    private PokemonPanelManager pokemonManager;
    
    private boolean menuVisible = false;
    
    // Updated Menu Colors - lighter backgrounds
    private static final Color MENU_BG = new Color(60, 60, 60, 240);
    private static final Color PANEL_BG = new Color(220, 220, 220);
    private static final Color PANEL_HOVER = new Color(240, 240, 240);
    private static final Color PANEL_SELECTED = new Color(200, 200, 200);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    
    private Menu() {}
    
    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }
    
    public JLayeredPane getGameLayeredPane() {
        return this.gameLayeredPane;
    }
    
    public void initializeMenuButton(JPanel board, int tileSize, int columns, int rows) {
        this.gameBoard = (Board) board;
        
        menuButton = new JButton("MENU");
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.setBackground(new Color(70, 130, 180));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusPainted(false);
        menuButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        menuButton.setPreferredSize(new Dimension(70, 35));
        
        menuButton.addActionListener(e -> toggleMenu());
        
        board.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeMenu");
        board.getActionMap().put("closeMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuVisible) {
                    hideMenu();
                }
            }
        });
    }
    
    public void setPlayer(Player player) {
        this.player = player;
        this.itemManager = new ItemManager(player);
        this.pokemonManager = new PokemonPanelManager(player);
    }
    
    public void setGameTimer(Timer timer) {
        this.gameTimer = timer;
    }
    
    public void setGameLayeredPane(JLayeredPane layeredPane) {
        this.gameLayeredPane = layeredPane;
    }
    
    private void toggleMenu() {
        if (menuVisible) {
            hideMenu();
        } else {
            showMenu();
        }
    }
    
    public void showMenu() {
        if (menuVisible || gameLayeredPane == null) return;
        
        // CRITICAL FIX: Properly freeze player and clear input
        if (player != null) {
            player.setMovementState(MovementState.FROZEN);
        }

        // Stop the game timer
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // CRITICAL FIX: Clear all key states and remove focus from board
        if (gameBoard != null) {
            gameBoard.resetKeyStates();
            gameBoard.setFocusable(false);
        }
        
        createSidePanelMenu();
        
        gameLayeredPane.add(menuOverlay, JLayeredPane.MODAL_LAYER);
        gameLayeredPane.setLayer(menuOverlay, JLayeredPane.MODAL_LAYER);
        
        menuVisible = true;
        
        // CRITICAL FIX: Proper focus management
        SwingUtilities.invokeLater(() -> {
            menuOverlay.setFocusable(true);
            menuOverlay.requestFocusInWindow();
        });
        
        gameLayeredPane.revalidate();
        gameLayeredPane.repaint();
    }
    
    public void hideMenu() {
        if (!menuVisible || menuOverlay == null) return;
        
        removePaused();
        
        gameLayeredPane.remove(menuOverlay);
        menuOverlay = null;
        menuVisible = false;
        
        // CRITICAL FIX: Proper restoration sequence
        SwingUtilities.invokeLater(() -> {
            // Restore game timer first
            if (gameTimer != null) {
                gameTimer.start();
            }
            
            // Restore player movement state
            if (player != null) {
                player.setMovementState(MovementState.FREE);
            }
            
            // Restore board focus
            if (gameBoard != null) {
                gameBoard.setFocusable(true);
                gameBoard.requestFocusInWindow();
                
                // Double-check focus restoration
                SwingUtilities.invokeLater(() -> {
                    if (!gameBoard.hasFocus()) {
                        gameBoard.grabFocus();
                    }
                });
            }
            
            gameLayeredPane.revalidate();
            gameLayeredPane.repaint();
        });
    }
    
    public boolean isMenuVisible() {
        return menuVisible;
    }
    
    private void createSidePanelMenu() {
        menuOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Semi-transparent overlay that preserves center visibility
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        menuOverlay.setOpaque(false);
        menuOverlay.setLayout(new BorderLayout());
        menuOverlay.setBounds(0, 0, gameLayeredPane.getWidth(), gameLayeredPane.getHeight());
        
        // CRITICAL FIX: Make menu overlay focusable and add key listener
        menuOverlay.setFocusable(true);
        menuOverlay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideMenu();
                }
            }
        });
        
        // Create larger left panel
        JPanel leftPanel = createLeftMenuPanel();
        
        // Create larger right panel
        JPanel rightPanel = createRightMenuPanel();
        
        // Add panels to overlay with BorderLayout
        menuOverlay.add(leftPanel, BorderLayout.WEST);
        menuOverlay.add(rightPanel, BorderLayout.EAST);
        
        // Create "PAUSED" text as a separate overlay component
        JLabel pausedLabel = new JLabel("PAUSED", JLabel.CENTER);
        pausedLabel.setFont(new Font("Arial", Font.BOLD, 48));
        pausedLabel.setForeground(Color.WHITE);
        pausedLabel.setOpaque(false);
        
        // Position the label at a higher layer
        int centerAreaWidth = gameLayeredPane.getWidth() - 600;
        int labelWidth = 200;
        int labelHeight = 60;
        int labelX = 300 + (centerAreaWidth - labelWidth) / 2;
        int labelY = 30;
        
        pausedLabel.setBounds(labelX, labelY, labelWidth, labelHeight);
        
        // Add the label directly to the layered pane at a higher layer
        gameLayeredPane.add(pausedLabel, JLayeredPane.POPUP_LAYER);
        
        // Store reference to remove it later
        menuOverlay.putClientProperty("pausedLabel", pausedLabel);
        
        // Add click listener to close menu when clicking center area
        menuOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int leftPanelWidth = 300;
                int rightPanelWidth = 300;
                if (e.getX() > leftPanelWidth && e.getX() < (menuOverlay.getWidth() - rightPanelWidth)) {
                    hideMenu();
                }
            }
        });
    }

    private JPanel createLeftMenuPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(MENU_BG);
        leftPanel.setPreferredSize(new Dimension(300, gameLayeredPane.getHeight())); // Increased from 200 to 300
        leftPanel.setBorder(BorderFactory.createEmptyBorder(80, 40, 80, 40)); // Increased padding
        
        // Group 1: Pokémon, Bag, Trainer Info - larger buttons
        JPanel pokemonPanel = createMenuItemPanel("POKEMON", "🔴", () -> openPokemonMenu());
        JPanel bagPanel = createMenuItemPanel("BAG", "🎒", () -> openBagMenu());
        JPanel trainerPanel = createMenuItemPanel("ID", "👤", () -> openTrainerMenu());
        
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(pokemonPanel);
        leftPanel.add(Box.createVerticalStrut(30)); // Increased spacing
        leftPanel.add(bagPanel);
        leftPanel.add(Box.createVerticalStrut(30)); // Increased spacing
        leftPanel.add(trainerPanel);
        leftPanel.add(Box.createVerticalGlue());
        
        return leftPanel;
    }
    
    private JPanel createRightMenuPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(MENU_BG);
        rightPanel.setPreferredSize(new Dimension(300, gameLayeredPane.getHeight())); // Increased from 200 to 300
        rightPanel.setBorder(BorderFactory.createEmptyBorder(80, 40, 80, 40)); // Increased padding
        
        // Group 2: Settings, Save, Map
        JPanel mapPanel = createMenuItemPanel("MAP", "🗺️", () -> openMapMenu());
        JPanel settingsPanel = createMenuItemPanel("SETTINGS", "⚙️", () -> openSettingsMenu());
        JPanel savePanel = createMenuItemPanel("SAVE", "💾", () -> openSaveMenu());
        
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(mapPanel);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(savePanel);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(settingsPanel);
        rightPanel.add(Box.createVerticalGlue());
        
        return rightPanel;
    }
    
    private JPanel createMenuItemPanel(String text, String icon, Runnable action) {
        JPanel panel = new JPanel() {
            private boolean isHovered = false;
            private boolean isPressed = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = isPressed ? PANEL_SELECTED : (isHovered ? PANEL_HOVER : PANEL_BG);
                
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Slightly more rounded corners
                
                // Add subtle border
                g2d.setColor(new Color(180, 180, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                
                g2d.dispose();
            }
        };
        
        panel.setLayout(new BorderLayout(15, 10)); // Increased spacing
        panel.setPreferredSize(new Dimension(220, 100)); // Increased from 160x60 to 220x100
        panel.setMaximumSize(new Dimension(220, 100));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Larger icon
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36)); // Increased from 24 to 36
        
        // Larger text
        JLabel textLabel = new JLabel(text, JLabel.CENTER);
        textLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Increased from 14 to 18
        textLabel.setForeground(TEXT_COLOR);
        
        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(textLabel, BorderLayout.SOUTH);
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    panel.getClass().getDeclaredField("isHovered").setBoolean(panel, true);
                } catch (Exception ex) {}
                panel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                try {
                    panel.getClass().getDeclaredField("isHovered").setBoolean(panel, false);
                } catch (Exception ex) {}
                panel.repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    panel.getClass().getDeclaredField("isPressed").setBoolean(panel, true);
                } catch (Exception ex) {}
                panel.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    panel.getClass().getDeclaredField("isPressed").setBoolean(panel, false);
                } catch (Exception ex) {}
                panel.repaint();
                if (panel.contains(e.getPoint())) {
                    action.run();
                }
            }
        });
        
        return panel;
    }

    private void removePaused() {
        JLabel pausedLabel = (JLabel) menuOverlay.getClientProperty("pausedLabel");
        if (pausedLabel != null) {
            gameLayeredPane.remove(pausedLabel);
        }
    }
    
    private void openPokemonMenu() {
        removePaused();
        SwingUtilities.invokeLater(() -> createFullScreenSubMenu("POKÉMON", createPokemonPanel()));
    }
    
    private void openBagMenu() {
        removePaused();
        SwingUtilities.invokeLater(() -> createFullScreenSubMenu("BAG", createInventoryPanel()));
    }
    
    private void openTrainerMenu() {
        removePaused();
        SwingUtilities.invokeLater(() -> createFullScreenSubMenu("TRAINER", createTrainerPanel()));
    }
    
    private void openMapMenu() {
        removePaused();
        SwingUtilities.invokeLater(() -> createFullScreenSubMenu("MAP", createMapPanel()));
    }
    
    private void openSaveMenu() {
        removePaused();
        JOptionPane.showMessageDialog(gameLayeredPane, "Game Saved!", "Save", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openSettingsMenu() {
        removePaused();
        SwingUtilities.invokeLater(() -> createFullScreenSubMenu("SETTINGS", createSettingsPanel()));
    }
    
    private void createFullScreenSubMenu(String title, JComponent content) {
        JPanel subMenuOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        subMenuOverlay.setOpaque(false);
        subMenuOverlay.setLayout(new BorderLayout());
        subMenuOverlay.setBounds(0, 0, gameLayeredPane.getWidth(), gameLayeredPane.getHeight());
        
        // CRITICAL FIX: Make submenu focusable
        subMenuOverlay.setFocusable(true);
        
        JPanel subMenuPanel = new JPanel(new BorderLayout());
        subMenuPanel.setBackground(PANEL_BG);
        subMenuPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 120, 120), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 40, 40));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JButton backButton = new JButton("← BACK");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        backButton.addActionListener(e -> {
            gameLayeredPane.remove(subMenuOverlay);
            gameLayeredPane.revalidate();
            gameLayeredPane.repaint();
            showMenu();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        
        subMenuPanel.add(titleLabel, BorderLayout.NORTH);
        subMenuPanel.add(content, BorderLayout.CENTER);
        subMenuPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        int menuWidth = Math.min(900, gameLayeredPane.getWidth() - 100);
        int menuHeight = Math.min(700, gameLayeredPane.getHeight() - 100);
        int x = (gameLayeredPane.getWidth() - menuWidth) / 2;
        int y = (gameLayeredPane.getHeight() - menuHeight) / 2;
        
        subMenuPanel.setBounds(x, y, menuWidth, menuHeight);
        subMenuOverlay.add(subMenuPanel, BorderLayout.CENTER);
        
        gameLayeredPane.add(subMenuOverlay, JLayeredPane.POPUP_LAYER);
        gameLayeredPane.revalidate();
        gameLayeredPane.repaint();
        
        // CRITICAL FIX: Proper key binding for submenu
        subMenuOverlay.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSubMenu");
        subMenuOverlay.getActionMap().put("closeSubMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameLayeredPane.remove(subMenuOverlay);
                gameLayeredPane.revalidate();
                gameLayeredPane.repaint();
                showMenu();
            }
        });
        
        // CRITICAL FIX: Proper focus for submenu
        SwingUtilities.invokeLater(() -> {
            subMenuOverlay.requestFocusInWindow();
        });
    }
    private JPanel createPokemonPanel() {
        if (pokemonManager != null) {
            return pokemonManager.createPokemonTeamPanel();
        }
        
        // Fallback if pokemonManager is null
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(new Color(245, 245, 245));
        JLabel emptyLabel = new JLabel("You don't have any Pokémon yet", JLabel.CENTER);
        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        emptyLabel.setForeground(new Color(120, 120, 120));
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        return emptyPanel;
    }

    private JComponent createInventoryPanel() {
        if (itemManager != null) {
            JPanel inventoryPanel = itemManager.createInventoryPanel();
            inventoryPanel.setBackground(new Color(245, 245, 245)); // Light background
            return inventoryPanel;
        }
        
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(new Color(245, 245, 245));
        JLabel emptyLabel = new JLabel("Your bag is empty", JLabel.CENTER);
        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        emptyLabel.setForeground(new Color(80, 80, 80)); // Dark text
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        return emptyPanel;
    }
    
    private JPanel createTrainerPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(245, 245, 245)); // Light background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String playerName = player != null ? player.getName() : "Unknown";
        JLabel nameLabel = new JLabel("Trainer: " + playerName, JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(new Color(40, 40, 40)); // Dark text
        
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.setOpaque(false);
        
        addTrainerStat(statsPanel, "Trainer ID:", "#" + (player != null ? player.getId() : "000001"));
        addTrainerStat(statsPanel, "Money:", "₽" + (player != null ? player.getMoney() : "0"));
        addTrainerStat(statsPanel, "Pokédex:", "25 seen, 12 caught");
        addTrainerStat(statsPanel, "Play Time:", "12:34");
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addTrainerStat(JPanel parent, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 14));
        labelComponent.setForeground(new Color(100, 100, 100));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 14));
        valueComponent.setForeground(new Color(40, 40, 40)); // Dark text
        
        parent.add(labelComponent);
        parent.add(valueComponent);
    }
    
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245)); // Light background
        
        JLabel label = new JLabel("Map View - Coming Soon", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setForeground(new Color(80, 80, 80)); // Dark text
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245)); // Light background
        
        JLabel label = new JLabel("Settings - Coming Soon", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setForeground(new Color(80, 80, 80)); // Dark text
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    public void positionMenuButtonForWindow(JLayeredPane layeredPane, int windowWidth, int windowHeight) {
        this.gameLayeredPane = layeredPane;
        
        if (menuButton != null) {
            Container parent = menuButton.getParent();
            if (parent != null) {
                parent.remove(menuButton);
            }
            
            int buttonWidth = menuButton.getPreferredSize().width;
            int buttonHeight = menuButton.getPreferredSize().height;
            menuButton.setBounds(windowWidth - buttonWidth - 15, 
                                windowHeight - buttonHeight - 15,
                                buttonWidth, buttonHeight);
            
            layeredPane.add(menuButton, JLayeredPane.PALETTE_LAYER);
            layeredPane.setLayer(menuButton, JLayeredPane.PALETTE_LAYER);
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }
    
    public JButton getMenuButton() {
        return menuButton;
    }
    
    public void refreshPokemonPanel() {
        if (menuVisible && menuOverlay != null) {
            SwingUtilities.invokeLater(() -> {
                hideMenu();
                showMenu();
            });
        }
    }
    
    public void refreshInventoryPanel() {
        if (menuVisible && menuOverlay != null) {
            SwingUtilities.invokeLater(() -> {
                hideMenu();
                showMenu();
            });
        }
    }
}
