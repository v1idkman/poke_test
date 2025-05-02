package model;

public class Pokeball extends Item {
    private double catchRate;
    private PokeBallType type;
    
    public enum PokeBallType {
        POKE_BALL(1.0, "Standard Poké Ball"),
        GREAT_BALL(1.5, "Better than a standard Poké Ball"),
        ULTRA_BALL(2.0, "High-performance Poké Ball"),
        MASTER_BALL(255.0, "The best Poké Ball with the ultimate performance");
        
        private final double multiplier;
        private final String description;
        
        PokeBallType(double multiplier, String description) {
            this.multiplier = multiplier;
            this.description = description;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public Pokeball(PokeBallType type, String imagePath) {
        super(type.name(), type.getDescription(), imagePath);
        this.type = type;
        this.catchRate = type.getMultiplier();
        this.stackable = true;
    }
    
    @Override
    public boolean use(Player player) {
        // Implementation for using a Poké Ball in battle
        // This would be connected to the battle system
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
