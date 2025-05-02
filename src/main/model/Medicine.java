package model;

public class Medicine extends Item {
    private int healAmount;
    private MedicineType type;
    
    public enum MedicineType {
        POTION(20, "Restores 20 HP"),
        SUPER_POTION(50, "Restores 50 HP"),
        HYPER_POTION(200, "Restores 200 HP"),
        MAX_POTION(999, "Fully restores HP"),
        REVIVE(0, "Revives a fainted Pokémon with half HP"),
        MAX_REVIVE(0, "Revives a fainted Pokémon with full HP");
        
        private final int healValue;
        private final String description;
        
        MedicineType(int healValue, String description) {
            this.healValue = healValue;
            this.description = description;
        }
        
        public int getHealValue() {
            return healValue;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public Medicine(MedicineType type, String imagePath) {
        super(type.name(), type.getDescription(), imagePath);
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
