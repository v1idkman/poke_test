package model;

import pokes.Pokemon;
import pokes.Pokemon.PokemonType;

public class Move {
    // Basic properties
    private String name;
    private PokemonType type;
    private int power;
    private int accuracy;
    private int maxPP;
    private int currentPP;
    private MoveCategory category;
    private int priority;
    private boolean makesContact;
    
    // Status effect properties
    private StatusEffect statusEffect;
    private int statusChance; // Percentage chance (0-100)
    
    // Stat modification properties
    private StatModifier[] statModifiers;
    
    // Enums for move properties
    public enum MoveCategory {
        PHYSICAL, SPECIAL, STATUS
    }
    
    public enum StatusEffect {
        NONE, BURN, FREEZE, PARALYSIS, POISON, SLEEP, CONFUSION
    }
    
    public class StatModifier {
        private String stat; // "attack", "defense", etc.
        private int stages; // -6 to +6
        private boolean affectsUser; // true if affects the user, false if affects the target
        
        public StatModifier(String stat, int stages, boolean affectsUser) {
            this.stat = stat;
            this.stages = Math.max(-6, Math.min(6, stages)); // Clamp between -6 and +6
            this.affectsUser = affectsUser;
        }
        
        public String getStat() {
            return stat;
        }
        
        public int getStages() {
            return stages;
        }
        
        public boolean affectsUser() {
            return affectsUser;
        }
    }
    
    // Constructor for basic move
    public Move(String name, PokemonType type, int power, int accuracy, int pp, MoveCategory category) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.maxPP = pp;
        this.currentPP = pp;
        this.category = category;
        this.priority = 0;
        this.makesContact = (category == MoveCategory.PHYSICAL);
        this.statusEffect = StatusEffect.NONE;
        this.statusChance = 0;
        this.statModifiers = new StatModifier[0];
    }
    
    // Constructor for status move
    public Move(String name, PokemonType type, int accuracy, int pp, StatusEffect statusEffect, int statusChance) {
        this(name, type, 0, accuracy, pp, MoveCategory.STATUS);
        this.statusEffect = statusEffect;
        this.statusChance = statusChance;
    }
    
    // Constructor for stat-modifying move
    public Move(String name, PokemonType type, int accuracy, int pp, StatModifier[] statModifiers) {
        this(name, type, 0, accuracy, pp, MoveCategory.STATUS);
        this.statModifiers = statModifiers;
    }
    
    // Use the move in battle
    public boolean use(Pokemon user, Pokemon target) {
        if (currentPP <= 0) {
            return false; // No PP left
        }
        
        // Reduce PP
        currentPP--;
        
        // Check accuracy
        if (!checkAccuracy()) {
            return false; // Move missed
        }
        
        // Apply damage if applicable
        if (category != MoveCategory.STATUS && power > 0) {
            applyDamage(user, target);
        }
        
        // Apply status effects if applicable
        if (statusEffect != StatusEffect.NONE && Math.random() * 100 <= statusChance) {
            applyStatus(target);
        }
        
        // Apply stat modifications if applicable
        if (statModifiers.length > 0) {
            applyStatModifiers(user, target);
        }
        
        return true;
    }
    
    // Check if the move hits based on accuracy
    private boolean checkAccuracy() {
        // If accuracy is 0, it never misses (like Swift)
        if (accuracy == 0) {
            return true;
        }
        
        // Otherwise, random check based on accuracy percentage
        return Math.random() * 100 <= accuracy;
    }
    
    // Apply damage to the target
    private void applyDamage(Pokemon user, Pokemon target) {
        // Basic damage formula (simplified from actual Pokémon games)
        double level = user.getStats().getLevel();
        double attackStat = (category == MoveCategory.PHYSICAL) ? 
                            user.getStats().getEffectiveStat("attack") : 
                            user.getStats().getEffectiveStat("special");
        double defenseStat = (category == MoveCategory.PHYSICAL) ? 
                             target.getStats().getEffectiveStat("defense") : 
                             target.getStats().getEffectiveStat("special");
        
        // Type effectiveness
        double typeEffectiveness = calculateTypeEffectiveness(target);
        
        // STAB (Same Type Attack Bonus)
        double stab = 1.0;
        for (PokemonType userType : user.getTypes()) {
            if (userType == type) {
                stab = 1.5;
                break;
            }
        }
        
        // Random factor (0.85 to 1.0)
        double random = 0.85 + (Math.random() * 0.15);
        
        // Calculate damage
        double damage = ((2 * level / 5 + 2) * power * (attackStat / defenseStat) / 50 + 2) 
                        * stab * typeEffectiveness * random;
        
        // Apply damage to target
        target.getStats().takeDamage((int)damage);
    }
    
    // Calculate type effectiveness
    private double calculateTypeEffectiveness(Pokemon target) {
        double effectiveness = 1.0;
        
        for (PokemonType targetType : target.getTypes()) {
            if (isTypeEffective(type, targetType)) {
                effectiveness *= 2.0;
            } else if (isTypeResistant(type, targetType)) {
                effectiveness *= 0.5;
            } else if (isTypeImmune(type, targetType)) {
                effectiveness = 0;
                break;
            }
        }
        
        return effectiveness;
    }
    
    // Type effectiveness checks (simplified)
    private boolean isTypeEffective(PokemonType attackType, PokemonType defenseType) {
        // Implement type effectiveness chart
        // Example: WATER is super effective against FIRE
        if (attackType == PokemonType.WATER && defenseType == PokemonType.FIRE) {
            return true;
        }
        // Add more type matchups here
        return false;
    }
    
    private boolean isTypeResistant(PokemonType attackType, PokemonType defenseType) {
        // Implement type resistance chart
        // Example: FIRE is not very effective against WATER
        if (attackType == PokemonType.FIRE && defenseType == PokemonType.WATER) {
            return true;
        }
        // Add more type matchups here
        return false;
    }
    
    private boolean isTypeImmune(PokemonType attackType, PokemonType defenseType) {
        // Implement type immunity chart
        // Example: NORMAL has no effect on GHOST
        if (attackType == PokemonType.NORMAL && defenseType == PokemonType.GHOST) {
            return true;
        }
        // Add more type matchups here
        return false;
    }
    
    // Apply status effect to target
    private void applyStatus(Pokemon target) {
        // Implement status application logic
        target.applyStatus(statusEffect);
    }
    
    // Apply stat modifications
    private void applyStatModifiers(Pokemon user, Pokemon target) {
        for (StatModifier modifier : statModifiers) {
            Pokemon affected = modifier.affectsUser() ? user : target;
            affected.getStats().modifyStat(modifier.getStat(), modifier.getStages());
        }
    }
    
    // Restore PP
    public void restorePP(int amount) {
        currentPP = Math.min(currentPP + amount, maxPP);
    }
    
    // Restore all PP
    public void restoreAllPP() {
        currentPP = maxPP;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public PokemonType getType() {
        return type;
    }
    
    public int getPower() {
        return power;
    }
    
    public int getAccuracy() {
        return accuracy;
    }
    
    public int getMaxPP() {
        return maxPP;
    }
    
    public int getCurrentPP() {
        return currentPP;
    }
    
    public MoveCategory getCategory() {
        return category;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean makesContact() {
        return makesContact;
    }
    
    public StatusEffect getStatusEffect() {
        return statusEffect;
    }
    
    public int getStatusChance() {
        return statusChance;
    }
    
    public StatModifier[] getStatModifiers() {
        return statModifiers;
    }
    
    // For comparing moves
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Move other = (Move) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return name + " (" + type + ", " + power + " power, " + accuracy + "% accuracy, " + currentPP + "/" + maxPP + " PP)";
    }
}
