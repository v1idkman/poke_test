package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import model.Player;
import model.Item;
import model.KeyItem;

public class ItemManager {
    private Player player;
    private Item selectedItem;
    private JPanel itemDisplayPanel;
    private JPanel itemInfoPanel;
    private JLabel itemNameLabel;
    private JLabel itemDescLabel;
    private JPanel itemImagePanel;
    private JButton useButton;
    private String currentCategory = "Poké Balls";
    
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
        } else if (item.getName().toLowerCase().contains("tm") || 
                   item.getName().toLowerCase().contains("hm")) {
            return "TMs & HMs";
        } else {
            return "Items";
        }
    }
    
    public JPanel createInventoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (player == null || player.getInventory() == null || player.getInventory().isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(new Color(245, 245, 245));
            
            JLabel emptyLabel = new JLabel("Your inventory is empty", JLabel.CENTER);
            emptyLabel.setFont(new Font("Lato", Font.PLAIN, 18));
            emptyLabel.setForeground(new Color(120, 120, 120));
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            return emptyPanel;
        }
        
        // Left section - Large bag and category buttons
        JPanel leftSection = createLeftSection();
        leftSection.setPreferredSize(new Dimension(450, 0));
        
        // Right section - Items display (much longer to fill empty space)
        itemDisplayPanel = createItemDisplayPanel();
        // Remove fixed height to allow it to expand and fill available space
        itemDisplayPanel.setPreferredSize(new Dimension(200, 0)); // Height set to 0 to fill available space
        
        // Bottom section - Selected item info (full width)
        itemInfoPanel = createBottomItemInfoPanel();
        itemInfoPanel.setPreferredSize(new Dimension(0, 120));
        
        // Create right panel container that fills all available space
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(itemDisplayPanel, BorderLayout.CENTER); // Changed from NORTH to CENTER to fill space
        
        mainPanel.add(leftSection, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(itemInfoPanel, BorderLayout.SOUTH);
        
        // Load initial category
        updateItemDisplay(currentCategory);
        
        return mainPanel;
    }
    
    private JPanel createLeftSection() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10)); // Reduced gap from 15 to 10
        leftPanel.setBackground(new Color(245, 245, 245));
        
        // Much larger bag icon
        JPanel bagPanel = createBagImagePanel();
        bagPanel.setPreferredSize(new Dimension(400, 320)); // Reduced height from 350 to 320
        
        // Category buttons below the bag (in a row)
        JPanel categoryPanel = createHorizontalCategoryButtonPanel();
        
        leftPanel.add(bagPanel, BorderLayout.CENTER);
        leftPanel.add(categoryPanel, BorderLayout.SOUTH);
        
        return leftPanel;
    }
    
    private JPanel createBagImagePanel() {
        JPanel bagPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon bagIcon = new ImageIcon("src/main/resources/button_icons/bag.png");
                    if (bagIcon.getIconWidth() > 0) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        
                        // Scale to fit the larger panel while maintaining aspect ratio
                        int panelWidth = getWidth();
                        int panelHeight = getHeight();
                        int imageWidth = bagIcon.getIconWidth();
                        int imageHeight = bagIcon.getIconHeight();
                        
                        double scaleX = (double) panelWidth / imageWidth;
                        double scaleY = (double) panelHeight / imageHeight;
                        double scale = Math.min(scaleX, scaleY) * 0.85;
                        
                        int scaledWidth = (int) (imageWidth * scale);
                        int scaledHeight = (int) (imageHeight * scale);
                        
                        int x = (panelWidth - scaledWidth) / 2;
                        int y = (panelHeight - scaledHeight) / 2;
                        
                        g2d.drawImage(bagIcon.getImage(), x, y, scaledWidth, scaledHeight, this);
                        g2d.dispose();
                    }
                } catch (Exception e) {
                    // Fallback: draw a larger simple bag representation
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(139, 69, 19));
                    g2d.fillRoundRect(20, 40, getWidth() - 40, getHeight() - 80, 25, 25);
                    g2d.setColor(new Color(101, 67, 33));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawRoundRect(20, 40, getWidth() - 40, getHeight() - 80, 25, 25);
                    
                    // Add bag handle
                    g2d.setStroke(new BasicStroke(6));
                    int handleWidth = getWidth() / 3;
                    int handleX = (getWidth() - handleWidth) / 2;
                    g2d.drawArc(handleX, 20, handleWidth, 40, 0, 180);
                    
                    g2d.dispose();
                }
            }
        };
        
        bagPanel.setPreferredSize(new Dimension(400, 350)); // Increased width from 350 to 400
        bagPanel.setBackground(new Color(230, 230, 230));
        bagPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        return bagPanel;
    }
    
    private JPanel createHorizontalCategoryButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8)); // Centered layout
        panel.setBackground(new Color(230, 230, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] categories = {"Poké Balls", "Medicine", "Berries", "Key Items", "TMs & HMs"};
        String[] iconPaths = {
            "src/main/resources/button_icons/pokeball.png",
            "src/main/resources/button_icons/potion.png", 
            "src/main/resources/button_icons/berry.png",
            "src/main/resources/button_icons/key_item.png",
            "src/main/resources/button_icons/hm_tm.png"
        };
        
        for (int i = 0; i < categories.length; i++) {
            JButton categoryButton = createCategoryButton(categories[i], iconPaths[i]);
            panel.add(categoryButton);
        }
        
        return panel;
    }
    
    private JButton createCategoryButton(String category, String iconPath) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                try {
                    ImageIcon icon = new ImageIcon(iconPath);
                    if (icon.getIconWidth() > 0) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        
                        g2d.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                        
                        // Add semi-transparent overlay for text readability
                        g2d.setColor(new Color(0, 0, 0, 100));
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                        
                        g2d.dispose();
                    } else {
                        super.paintComponent(g);
                    }
                } catch (Exception e) {
                    super.paintComponent(g);
                }
                
                // Draw the text
                FontMetrics fm = g.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - fm.getDescent();
                
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                g.drawString(text, x, y);
            }
        };
        
        button.setText(category);
        button.setFont(new Font("Lato", Font.BOLD, 11)); // Reduced font size from 12 to 11
        button.setPreferredSize(new Dimension(75, 55)); // Reduced size from 80x60 to 75x55
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        
        updateButtonAppearance(button, category.equals(currentCategory));
        
        button.addActionListener(e -> {
            currentCategory = category;
            updateCategoryButtons();
            updateItemDisplay(category);
        });
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!category.equals(currentCategory)) {
                    button.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                updateButtonAppearance(button, category.equals(currentCategory));
            }
        });
        
        return button;
    }
    
    private void updateButtonAppearance(JButton button, boolean isSelected) {
        if (isSelected) {
            button.setBorder(BorderFactory.createLineBorder(new Color(50, 100, 150), 3));
        } else {
            button.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1));
        }
    }
    
    private void updateCategoryButtons() {
        // Find and update all category buttons
        updateButtonsRecursively(itemDisplayPanel.getParent());
    }
    
    private void updateButtonsRecursively(Container container) {
        if (container == null) return;
        
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                updateButtonAppearance(button, button.getText().equals(currentCategory));
            } else if (comp instanceof Container) {
                updateButtonsRecursively((Container) comp);
            }
        }
    }
    
    private JPanel createItemDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Items",
                0, 0,
                new Font("Lato", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        return panel;
    }
    
    
    private void updateItemDisplay(String category) {
        itemDisplayPanel.removeAll();
        
        // Get items for this category
        List<Item> categoryItems = new ArrayList<>();
        if (player.getInventory() != null) {
            for (Item item : player.getInventory()) {
                if (getItemType(item).equals(category)) {
                    categoryItems.add(item);
                }
            }
        }
        
        if (categoryItems.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No " + category.toLowerCase() + " in inventory", JLabel.CENTER);
            noItemsLabel.setFont(new Font("Lato", Font.ITALIC, 16)); // Increased font size
            noItemsLabel.setForeground(new Color(120, 120, 120));
            itemDisplayPanel.add(noItemsLabel, BorderLayout.CENTER);
            
            // Clear selected item if no items in category
            selectedItem = null;
            updateItemInfoDisplay();
        } else {
            // Create vertical list of items
            JPanel itemsList = new JPanel();
            itemsList.setLayout(new BoxLayout(itemsList, BoxLayout.Y_AXIS));
            itemsList.setBackground(new Color(250, 250, 250));
            
            for (Item item : categoryItems) {
                JPanel itemRow = createVerticalItemRow(item);
                itemsList.add(itemRow);
                itemsList.add(Box.createVerticalStrut(8)); // Increased spacing from 3 to 8
            }
            
            JScrollPane scrollPane = new JScrollPane(itemsList);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(new Color(250, 250, 250));
            
            itemDisplayPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Auto-select first item in the list
            if (!categoryItems.isEmpty()) {
                selectItem(categoryItems.get(0));
                // updateItemSelection();
            }
        }
        
        itemDisplayPanel.revalidate();
        itemDisplayPanel.repaint();
    }
    
    private JPanel createVerticalItemRow(Item item) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setPreferredSize(new Dimension(0, 80));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        row.setBackground(Color.WHITE);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Item image on the left
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
        imagePanel.setPreferredSize(new Dimension(60, 60));
        imagePanel.setOpaque(false);
        
        // Item info in center
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Lato", Font.BOLD, 16));
        nameLabel.setForeground(new Color(60, 60, 60));
        
        JLabel quantityLabel = new JLabel("Qty: " + item.getQuantity());
        quantityLabel.setFont(new Font("Lato", Font.PLAIN, 14));
        quantityLabel.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(nameLabel);
        infoPanel.add(quantityLabel);
        
        row.add(imagePanel, BorderLayout.WEST);
        row.add(infoPanel, BorderLayout.CENTER);
        
        // Add click functionality only (no hover effects)
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectItem(item);
                // updateItemSelection();
            }
        });
        
        return row;
    }
    
    private JPanel createBottomItemInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Item Details",
                0, 0,
                new Font("Lato", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Item image on the left
        itemImagePanel = new JPanel();
        itemImagePanel.setPreferredSize(new Dimension(80, 80));
        itemImagePanel.setBackground(new Color(248, 248, 248));
        itemImagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        // Item info in center
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        itemNameLabel = new JLabel("Select an item");
        itemNameLabel.setFont(new Font("Lato", Font.BOLD, 24));
        itemNameLabel.setForeground(new Color(50, 50, 50));
        itemNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Initialize without HTML formatting - let layout manager handle wrapping
        itemDescLabel = new JLabel("Click on an item to view details");
        itemDescLabel.setFont(new Font("Lato", Font.PLAIN, 20));
        itemDescLabel.setForeground(new Color(80, 80, 80));
        itemDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(itemNameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(itemDescLabel);
        
        // Use button on the right
        useButton = new JButton("Use");
        useButton.setFont(new Font("Lato", Font.BOLD, 14));
        useButton.setBackground(new Color(70, 130, 180));
        useButton.setForeground(Color.WHITE);
        useButton.setFocusPainted(false);
        useButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        useButton.setEnabled(false);
        useButton.addActionListener(e -> useSelectedItem());
        
        panel.add(itemImagePanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(useButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private void selectItem(Item item) {
        selectedItem = item;
        updateItemInfoDisplay();
    }
    
    private void updateItemInfoDisplay() {
        if (selectedItem == null) {
            itemNameLabel.setText("Select an item");
            itemDescLabel.setText("Click on an item to view details");
            itemImagePanel.removeAll();
            useButton.setEnabled(false);
        } else {
            String displayName = selectedItem.getName();
            if (selectedItem.getQuantity() > 1) {
                displayName += " ×" + selectedItem.getQuantity();
            }
            itemNameLabel.setText(displayName);
            
            String description = selectedItem.getDescription();
            if (description == null || description.isEmpty()) {
                description = "No description available.";
            }
            
            // Remove HTML formatting and let the layout manager handle wrapping naturally
            itemDescLabel.setText(description);
            
            updateItemImage();
            useButton.setEnabled(true);
        }
        
        itemInfoPanel.revalidate();
        itemInfoPanel.repaint();
    }
    
    private void updateItemImage() {
        itemImagePanel.removeAll();
        itemImagePanel.setLayout(new BorderLayout());
        
        // CRITICAL FIX: Add null check for selectedItem
        if (selectedItem != null && selectedItem.getImage() != null) {
            JPanel imageDisplayPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // CRITICAL FIX: Double-check selectedItem is not null
                    if (selectedItem != null && selectedItem.getImage() != null) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        
                        Image img = selectedItem.getImage();
                        int size = Math.min(getWidth(), getHeight()) - 10;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        g2d.drawImage(img, x, y, size, size, this);
                        g2d.dispose();
                    }
                }
            };
            imageDisplayPanel.setOpaque(false);
            itemImagePanel.add(imageDisplayPanel, BorderLayout.CENTER);
        } else {
            JLabel placeholderLabel = new JLabel("No Image", JLabel.CENTER);
            placeholderLabel.setFont(new Font("Lato", Font.ITALIC, 12));
            placeholderLabel.setForeground(new Color(150, 150, 150));
            itemImagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }
        
        itemImagePanel.revalidate();
        itemImagePanel.repaint();
    }
    
    private void useSelectedItem() {
        if (selectedItem != null) {
            boolean success = selectedItem.use(player);
            
            if (success) {
                updateItemDisplay(currentCategory);
                if (selectedItem.getQuantity() <= 0) {
                    selectedItem = null;
                    updateItemInfoDisplay();
                } else {
                    updateItemInfoDisplay();
                }
            }
        }
    }
    
    public void refreshCurrentMenu() {
        if (itemDisplayPanel != null) {
            updateItemDisplay(currentCategory);
        }
    }
}
