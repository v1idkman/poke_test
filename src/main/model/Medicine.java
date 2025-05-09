package model;

import javax.swing.ImageIcon;

import pokes.Pokemon;

public class Medicine extends Item {
    private int healAmount;
    private MedicineType type;
    
    public enum MedicineType {
        POTION("Potion", 20, "Restores 20 HP"),
        SUPER_POTION("Super Potion", 50, "Restores 50 HP"),
        HYPER_POTION("Hyper Potion", 200, "Restores 200 HP"),
        MAX_POTION("Max Potion", 999, "Fully restores HP"),
        REVIVE("Revive", 0, "Revives a fainted Pokémon with half HP"),
        MAX_REVIVE("Max Revive", 0, "Revives a fainted Pokémon with full HP");
        
        private final String name;
        private final int healValue;
        private final String description;
        
        MedicineType(String name, int healValue, String description) {
            this.name = name;
            this.healValue = healValue;
            this.description = description;
        }

        public String getName() {
            return name;
        }
        
        public int getHealValue() {
            return healValue;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public Medicine(MedicineType type) {
        super(type.getName(), type.getDescription());
        this.type = type;
        this.healAmount = type.getHealValue();
        this.stackable = true;
        loadImage();
    }

    public void loadImage() {
        String lookupName = type.getName().replace(" ", "-").toLowerCase();
        String imagePath = "sprites/sprites/items/" + lookupName + ".png"; // Add file extension
        imagePath = imagePath.trim();
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
    
    
    // Apply medicine effects to a Pokémon
    public boolean applyTo(Pokemon pokemon) {
        if (type == MedicineType.REVIVE || type == MedicineType.MAX_REVIVE) {
            // Revives can only be used on fainted Pokémon
            if (!pokemon.getStats().hasFainted()) {
                return false;
            }
            
            int newHp = (type == MedicineType.REVIVE) 
            ? pokemon.getStats().getMaxHp() / 2 : 
            pokemon.getStats().getMaxHp();
            
            pokemon.getStats().setCurrentHp(newHp);
            return true;
        } else {
            // Healing items can only be used on non-fainted Pokémon that aren't at full health
            if (pokemon.getStats().hasFainted() || 
                pokemon.getStats().getCurrentHp() >= pokemon.getStats().getMaxHp()) {
                return false;
            }
            
            // Apply healing
            int currentHp = pokemon.getStats().getCurrentHp();
            int newHp = Math.min(currentHp + healAmount, pokemon.getStats().getMaxHp());
            pokemon.getStats().setCurrentHp(newHp);
            return true;
        }
    }
    
    @Override
    public boolean use(Player player) {
        // This method now just signals that the item should be used
        // The actual application is handled in the UI layer
        return true;
    }
    
    public int getHealAmount() {
        return healAmount;
    }
    
    public MedicineType getType() {
        return type;
    }

    public boolean isRevive() {
        return type == MedicineType.REVIVE || type == MedicineType.MAX_REVIVE;
    }
}
