package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import model.Player;
import model.Pokeball;
import model.Item;
import model.Medicine;
import model.KeyItem;
import pokes.Pokemon;

public class ItemManager {
    private Player player;
    private JDialog menuDialog;
    private JButton menuButton;
    
    public ItemManager(Player player, JDialog menuDialog, JButton menuButton) {
        this.player = player;
        this.menuDialog = menuDialog;
        this.menuButton = menuButton;
    }
    
    public void updateItemInfoPanel(Item item, JLabel nameLabel, JLabel descLabel, JPanel imagePanel, JPanel infoPanel) {
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

        // Remove any existing button panel
        for (Component comp : infoPanel.getComponents()) {
            if (comp instanceof JPanel && comp == infoPanel.getComponent(infoPanel.getComponentCount() - 1)) {
                infoPanel.remove(comp);
                break;
            }
        }
        
        // Add a new "Use" button for usable items
        if (item instanceof Medicine || item instanceof KeyItem) {
            JPanel buttonPanel = new JPanel();
            JButton useButton = new JButton("Use");
            useButton.setBackground(new Color(30, 201, 139));
            useButton.setForeground(Color.WHITE);
            
            if (item instanceof Medicine) {
                useButton.addActionListener(e -> openMedicineSelectionDialog((Medicine)item));
            } else if (item instanceof KeyItem) {
                useButton.addActionListener(e -> {
                    boolean used = item.use(player);
                    if (used && item.getQuantity() <= 0) {
                        player.removeItem(item);
                        menuDialog.dispose();
                        openPlayerMenu((JPanel)menuButton.getParent());
                    }
                });
            }
            
            buttonPanel.add(useButton);
            infoPanel.add(buttonPanel, BorderLayout.PAGE_END);
        }
        
        infoPanel.revalidate();
        infoPanel.repaint();
    }
    
    public void openMedicineSelectionDialog(Medicine medicine) {
        // Get a fresh reference to the medicine item
        Medicine currentMedicine = null;
        for (Item item : player.getInventory()) {
            if (item.equals(medicine) && item instanceof Medicine) {
                currentMedicine = (Medicine) item;
                break;
            }
        }
        
        // If the item is no longer in inventory, return
        if (currentMedicine == null) return;
        
        // Create dialog to select which Pokémon to use the item on
        JDialog selectDialog = new JDialog(menuDialog, "Use " + currentMedicine.getName(), true);
        selectDialog.setSize(400, 300);
        selectDialog.setLocationRelativeTo(menuDialog);
        
        JPanel dialogMainPanel = new JPanel(new BorderLayout(10, 10));
        dialogMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create a panel to display Pokémon list
        JPanel pokemonListPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        pokemonListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        boolean anyValidTarget = false;
        final Medicine finalMedicine = currentMedicine; // For use in lambda
        
        // Add each Pokémon to the list
        for (Pokemon pokemon : player.getTeam()) {
            boolean canUseItem = false;
            
            // Check if the item can be used on this Pokémon
            if (finalMedicine.isRevive()) {
                // Revives can only be used on fainted Pokémon
                canUseItem = pokemon.getStats().hasFainted();
            } else {
                // Healing items can only be used on non-fainted Pokémon that aren't at full health
                canUseItem = !pokemon.getStats().hasFainted() && 
                             pokemon.getStats().getCurrentHp() < pokemon.getStats().getMaxHp();
            }
            
            JPanel pokemonPanel = PokemonPanelManager.createPokemonSelectionPanel(pokemon, canUseItem);
            
            if (canUseItem) {
                anyValidTarget = true;
                // Add click listener to use item on this Pokémon
                pokemonPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Apply medicine and close dialog before refreshing
                        applyMedicineEffect(finalMedicine, pokemon);
                        selectDialog.dispose();
                        
                        if (finalMedicine.getQuantity() <= 0) {
                            player.removeItem(finalMedicine);
                        }
                        
                        // Refresh both panels
                        Menu.getInstance().refreshInventoryPanel();
                        Menu.getInstance().refreshPokemonPanel();
                    }
                });
            }
            
            pokemonListPanel.add(pokemonPanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(pokemonListPanel);
        dialogMainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> selectDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);
        dialogMainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // If no valid targets, show message and return
        if (!anyValidTarget) {
            JOptionPane.showMessageDialog(menuDialog, 
                "Cannot use " + finalMedicine.getName() + " on any Pokémon in your team.", 
                "Cannot Use Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        selectDialog.add(dialogMainPanel);
        selectDialog.setVisible(true);
    }
    
    private void applyMedicineEffect(Medicine medicine, Pokemon pokemon) {
        boolean applied = medicine.applyTo(pokemon);
        if (applied) {
            // Reduce quantity after successful use
            medicine.reduceQuantity(1);
        }
    }
    
    private void openPlayerMenu(JPanel parentBoard) {
        Menu.getInstance().openPlayerMenu(parentBoard);
    }
    
    public String getItemType(Item item) {
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
}
