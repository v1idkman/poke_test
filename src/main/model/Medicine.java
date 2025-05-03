package model;

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
    
    public Medicine(MedicineType type, String imagePath) {
        super(type.getName(), type.getDescription(), imagePath);
        this.type = type;
        this.healAmount = type.getHealValue();
        this.stackable = true;
    }
    
    @Override
    public boolean use(Player player) {
        // Implementation for using medicine on a Pokémon
        System.out.println("Using " + name + " to heal " + healAmount + " HP");
        
        // Reduce quantity when used
        return reduceQuantity(1);
    }
    
    public int getHealAmount() {
        return healAmount;
    }
    
    public MedicineType getType() {
        return type;
    }
}
