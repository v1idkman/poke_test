package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import model.Player;
import model.Item;
import model.KeyItem;

public class ItemManager {
    private Player player;
    
    public ItemManager(Player player) {
        this.player = player;
    }
    
    public String getItemType(Item item) {
        if (item instanceof KeyItem) {
            return "Key Items";
        } else if (item.getName().toLowerCase().contains("ball")) {
            return "Poké Balls";
        } else if (item.getName().toLowerCase().contains("potion") || 
                   item.getName().toLowerCase().contains("revive") ||
                   item.getName().toLowerCase().contains("heal")) {
            return "Medicine";
        } else if (item.getName().toLowerCase().contains("berry")) {
            return "Berries";
        } else {
            return "Items";
        }
    }
    
    public void updateItemInfoPanel(Item item, JLabel nameLabel, JLabel descLabel, 
                                  JPanel imagePanel, JPanel infoPanel) {
        if (item == null) {
            nameLabel.setText("No Item Selected");
            nameLabel.setFont(new Font("Lato", Font.BOLD, 18));
            nameLabel.setForeground(new Color(100, 100, 100));
            descLabel.setText("");
            imagePanel.removeAll();
            imagePanel.revalidate();
            imagePanel.repaint();
            return;
        }
        
        // Update name with enhanced styling
        String displayName = item.getName();
        if (item.getQuantity() > 1) {
            displayName = item.getName() + " ×" + item.getQuantity();
        }
        nameLabel.setText(displayName);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 18));
        nameLabel.setForeground(new Color(50, 50, 50));
        
        // Update description with better formatting
        String description = item.getDescription();
        if (description == null || description.isEmpty()) {
            description = "No description available.";
        }
        
        // Enhanced HTML formatting for description
        descLabel.setText("<html><div style='width: 180px; text-align: center; " +
                         "font-family: Arial, sans-serif; font-size: 12px; " +
                         "color: #555555; line-height: 1.4;'>" + 
                         description + "</div></html>");
        
        // Update image with enhanced styling
        imagePanel.removeAll();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        imagePanel.setBackground(new Color(248, 248, 248));
        
        if (item.getImage() != null) {
            JPanel imageDisplayPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    if (item.getImage() != null) {
                        Image img = item.getImage();
                        int size = Math.min(getWidth(), getHeight()) - 20;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        g2d.drawImage(img, x, y, size, size, this);
                    }
                    g2d.dispose();
                }
            };
            imageDisplayPanel.setOpaque(false);
            imagePanel.add(imageDisplayPanel, BorderLayout.CENTER);
        } else {
            // Add placeholder when no image is available
            JLabel placeholderLabel = new JLabel("No Image", JLabel.CENTER);
            placeholderLabel.setFont(new Font("Lato", Font.ITALIC, 12));
            placeholderLabel.setForeground(new Color(150, 150, 150));
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }
        
        // Force refresh with smooth animation
        imagePanel.revalidate();
        imagePanel.repaint();
        infoPanel.revalidate();
        infoPanel.repaint();
    }
    
    public JPanel createInventoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        if (player == null || player.getInventory() == null || player.getInventory().isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(new Color(245, 245, 245));
            
            JLabel emptyLabel = new JLabel("Your inventory is empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Lato", Font.PLAIN, 18));
            emptyLabel.setForeground(new Color(120, 120, 120));
            
            JLabel hintLabel = new JLabel("Items you collect will appear here", JLabel.CENTER);
            hintLabel.setFont(new Font("Lato", Font.ITALIC, 14));
            hintLabel.setForeground(new Color(150, 150, 150));
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            textPanel.setOpaque(false);
            textPanel.add(emptyLabel);
            textPanel.add(hintLabel);
            
            emptyPanel.add(textPanel, BorderLayout.CENTER);
            return emptyPanel;
        }
        
        // Create categories panel
        Map<String, java.util.List<Item>> categorizedItems = new HashMap<>();
        for (Item item : player.getInventory()) {
            String category = getItemType(item);
            categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        }
        
        JTabbedPane categoryTabs = new JTabbedPane();
        categoryTabs.setFont(new Font("Lato", Font.BOLD, 12));
        categoryTabs.setBackground(new Color(245, 245, 245));
        
        for (Map.Entry<String, java.util.List<Item>> entry : categorizedItems.entrySet()) {
            JPanel categoryPanel = createCategoryPanel(entry.getValue());
            categoryTabs.addTab(entry.getKey(), categoryPanel);
        }
        
        mainPanel.add(categoryTabs, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createCategoryPanel(java.util.List<Item> items) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create items grid
        JPanel itemsGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        itemsGrid.setBackground(new Color(250, 250, 250));
        
        for (Item item : items) {
            JPanel itemPanel = createItemPanel(item);
            itemsGrid.add(itemPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsGrid);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(250, 250, 250));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createItemPanel(Item item) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 248, 255));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(30, 201, 139), 2),
                    BorderFactory.createEmptyBorder(7, 7, 7, 7)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }
        });
        
        // Item image
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (item.getImage() != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int size = Math.min(getWidth(), getHeight()) - 4;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2d.drawImage(item.getImage(), x, y, size, size, this);
                    g2d.dispose();
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(40, 40));
        imagePanel.setOpaque(false);
        
        // Item name and quantity
        JLabel nameLabel = new JLabel(item.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Lato", Font.BOLD, 12));
        nameLabel.setForeground(new Color(60, 60, 60));
        
        if (item.getQuantity() > 1) {
            JLabel quantityLabel = new JLabel("×" + item.getQuantity(), JLabel.CENTER);
            quantityLabel.setFont(new Font("Lato", Font.PLAIN, 10));
            quantityLabel.setForeground(new Color(100, 100, 100));
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(quantityLabel);
            
            panel.add(textPanel, BorderLayout.SOUTH);
        } else {
            panel.add(nameLabel, BorderLayout.SOUTH);
        }
        
        panel.add(imagePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void refreshCurrentMenu() {
        Menu menu = Menu.getInstance();
        if (menu.isMenuVisible()) {
            SwingUtilities.invokeLater(() -> {
                menu.refreshInventoryPanel();
            });
        }
    }
}
