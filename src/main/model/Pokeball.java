package model;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Pokeball extends Item {
    private double catchRate;
    private PokeBallType type;
    
    public enum PokeBallType {
        POKE_BALL("Poke Ball", 1.0, "Standard Poké Ball"),
        GREAT_BALL("Great Ball", 1.5, "Better than a standard Poké Ball"),
        ULTRA_BALL("Ultra Ball", 2.0, "High-performance Poké Ball"),
        MASTER_BALL("Master Ball", 255.0, "The best Poké Ball with the ultimate performance"),
        LUXURY_BALL("Luxury Ball", 1, "Doubles the rate at which the contained Pokémon's friendship increases."),
        SAFARI("Safari Ball", 1, "Prior to Generation VIII, it was only usable within Safari Zones."),
        DUSK_BALL("Dusk Ball", 2.0, "Works especially well in the dark");

        
        private final String name;
        private final double multiplier;
        private final String description;
        
        PokeBallType(String name, double multiplier, String description) {
            this.name = name;
            this.multiplier = multiplier;
            this.description = description;
        }

        public String getName() {
            return name;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public Pokeball(PokeBallType type) {
        super(type.getName(), type.getDescription());
        this.type = type;
        this.catchRate = type.getMultiplier();
        this.stackable = true;
        loadImage();
    }

    public void loadImage() {
        if (type == null) {
            System.out.println("type is null");
            return;
        }
        String lookupName = type.getName().replace(" ", "-").toLowerCase();
        String imagePath = "sprites/sprites/items/" + lookupName + ".png";
        try {
            // Use File instead of getResource for external files
            java.io.File file = new java.io.File(imagePath);
            if (file.exists()) {
                image = new ImageIcon(file.getAbsolutePath()).getImage();
            } else {
                System.err.println("File not found: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading item image: " + e.getMessage());
        }
    }
    
    
    @Override
    public boolean use(Player player) {
        // Check if player is in battle
        if (!player.isInBattle()) {
            JOptionPane.showMessageDialog(null, 
                "You can't use a " + name + " outside of battle!", 
                "Cannot Use Item", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        
        // Implementation for using a Poké Ball in battle
        System.out.println("Using " + name + " with catch rate: " + catchRate);
        
        // Reduce quantity when used
        return reduceQuantity(1);
    }

    
    public double getCatchRate() {
        return catchRate;
    }
    
    public PokeBallType getType() {
        return type;
    }
}
