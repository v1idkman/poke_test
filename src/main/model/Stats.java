package model;

import java.util.Random;

public class Stats {
    // Base stats for a Pokémon
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int speed;
    private int specialAtk;
    private int specialDef;
    private int level;
    
    // Individual Values (IVs) - randomized per Pokémon instance (0-31)
    private final int hpIV;
    private final int attackIV;
    private final int defenseIV;
    private final int speedIV;
    private final int specialAtkIV;
    private final int specialDefIV;

    private int hpEV = 0;
    private int attackEV = 0;
    private int defenseEV = 0;
    private int speedEV = 0;
    private int specialAtkEV = 0;
    private int specialDefEV = 0;
    
    // For tracking stat changes during battle
    private int attackModifier;
    private int defenseModifier;
    private int speedModifier;
    private int spAttModifier;
    private int spDefModifier;
    private int accuracyModifier;
    private int evasionModifier;

    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpeed;
    private int baseSpAtk;
    private int baseSpDef;
    
    // Constructor for creating stats with base values
    public Stats(int baseHp, int baseAttack, int baseDefense, int baseSpeed, int baseSpAtt, int baseSpDef, int level) {
        // Generate random IVs (0-15 range for Gen 1 style)
        Random random = new Random();
        this.hpIV = random.nextInt(32);
        this.attackIV = random.nextInt(32);
        this.defenseIV = random.nextInt(32);
        this.speedIV = random.nextInt(32);
        this.specialAtkIV = random.nextInt(32);
        this.specialDefIV = random.nextInt(32);
        
        this.level = level;

        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.baseSpAtk = baseSpAtt;
        this.baseSpDef = baseSpDef;
        
        // Calculate the actual stats based on base stats, IVs and level
        calculateStats(baseHp, baseAttack, baseDefense, baseSpeed, baseSpAtt, baseSpDef);
        
        // Initialize current HP to max HP
        this.currentHp = this.maxHp;
        
        // Initialize battle modifiers
        resetStatModifiers();
    }
    
    // Calculate all stats based on base values, IVs, and level
    private void calculateStats(int baseHp, int baseAttack, int baseDefense, int baseSpeed, int baseSpecialAtk, int baseSpecialDef) {
        // HP calculation with EVs
        int oldHp = getCurrentHp();
        this.maxHp = calculateHp(baseHp, hpEV);
        if (currentHp == oldHp) {
            setHp(maxHp);
        }
        
        // Other stats calculation with EVs
        this.attack = calculateStat(baseAttack, attackIV, attackEV);
        this.defense = calculateStat(baseDefense, defenseIV, defenseEV);
        this.speed = calculateStat(baseSpeed, speedIV, speedEV);
        this.specialAtk = calculateStat(baseSpecialAtk, specialAtkIV, specialAtkEV);
        this.specialDef = calculateStat(baseSpecialDef, specialDefIV, specialDefEV);
    }
    
    private int calculateHp(int baseHp, int ev) {
        // Modern formula would include EVs
        return ((2 * baseHp + hpIV + ev/4) * level / 100) + level + 10;
    }

    public void setHp(int newHp) {
        if (newHp <= maxHp) {
            currentHp = newHp;
        }
    }
    
    // Stat calculation formula for Attack, Defense, Speed, Special (based on Gen 1)
    private int calculateStat(int baseStat, int iv, int ev) {
        // Modern formula: ((2 * Base + IV + EV/4) * Level / 100) + 5
        return ((2 * baseStat + iv + ev/4) * level / 100) + 5;
    }
    
    
    // Reset all battle stat modifiers
    public void resetStatModifiers() {
        attackModifier = 0;
        defenseModifier = 0;
        speedModifier = 0;
        spAttModifier = 0;
        spDefModifier = 0;
        accuracyModifier = 0;
        evasionModifier = 0;
    }
    
    // Apply damage to the Pokémon
    public void takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) {
            currentHp = 0;
        }
    }
    
    // Heal the Pokémon
    public void heal(int amount) {
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }
    
    // Fully restore HP
    public void fullyRestore() {
        currentHp = maxHp;
    }
    
    // Check if Pokémon has fainted
    public boolean hasFainted() {
        return currentHp <= 0;
    }
    
    // Modify a stat in battle (range from -6 to +6)
    public boolean modifyStat(String statName, int stages) {
        switch (statName.toLowerCase()) {
            case "attack":
                return adjustModifier(attackModifier, stages, "attackModifier");
            case "defense":
                return adjustModifier(defenseModifier, stages, "defenseModifier");
            case "speed":
                return adjustModifier(speedModifier, stages, "speedModifier");
            case "sp.Att":
                return adjustModifier(spAttModifier, stages, "spAttModifier");
            case "sp.Def":
                return adjustModifier(spDefModifier, stages, "spDefModifier");
            case "accuracy":
                return adjustModifier(accuracyModifier, stages, "accuracyModifier");
            case "evasion":
                return adjustModifier(evasionModifier, stages, "evasionModifier");
            default:
                return false;
        }
    }
    
    // Helper method to adjust a stat modifier and ensure it stays within -6 to +6 range
    private boolean adjustModifier(int currentMod, int stages, String fieldName) {
        int newMod = currentMod + stages;
        if (newMod > 6) newMod = 6;
        if (newMod < -6) newMod = -6;
        
        // If no change would occur, return false
        if (newMod == currentMod) {
            return false;
        }
        
        // Use reflection to set the appropriate field
        try {
            this.getClass().getDeclaredField(fieldName).setInt(this, newMod);
            return true;
        } catch (Exception e) {
            System.err.println("Error modifying stat: " + e.getMessage());
            return false;
        }
    }
    
    // Get the actual multiplier for a stat based on its modifier
    public double getStatMultiplier(String statName) {
        int modifier;
        
        switch (statName.toLowerCase()) {
            case "attack":
                modifier = attackModifier;
                break;
            case "defense":
                modifier = defenseModifier;
                break;
            case "speed":
                modifier = speedModifier;
                break;
            case "sp.Att":
                modifier = spAttModifier;
                break;
            case "sp.Def":
                modifier = spDefModifier;
                break;
            case "accuracy":
                modifier = accuracyModifier;
                break;
            case "evasion":
                modifier = evasionModifier;
                break;
            default:
                return 1.0;
        }
        
        // Convert modifier to actual multiplier (Gen 1-3 formula)
        if (modifier >= 0) {
            return (modifier + 2) / 2.0;
        } else {
            return 2.0 / (Math.abs(modifier) + 2);
        }
    }
    
    // Get the effective stat value after applying battle modifiers
    public int getEffectiveStat(String statName) {
        double multiplier = getStatMultiplier(statName);
        
        switch (statName.toLowerCase()) {
            case "attack":
                return (int)(attack * multiplier);
            case "defense":
                return (int)(defense * multiplier);
            case "speed":
                return (int)(speed * multiplier);
            case "sp. attack":
                return (int)(specialAtk * multiplier);
            case "sp.defense":
                return (int)(specialDef * multiplier);
            default:
                return 0;
        }
    }
    
    // Level up the Pokémon
    public void levelUp() {
        level++;
        // Recalculate stats with the new level
        // This assumes we have stored the base stats somewhere
        // You might need to adjust this based on your implementation
    }
    
    // Getters
    public int getMaxHp() {
        return maxHp;
    }
    
    public int getCurrentHp() {
        return currentHp;
    }

    public int getHpEV() {
        return hpEV;
    }
    
    public int getAttackEV() {
        return attackEV;
    }
    
    public int getDefenseEV() {
        return defenseEV;
    }
    
    public int getSpeedEV() {
        return speedEV;
    }
    
    public int getSpecialAtkEV() {
        return specialAtkEV;
    }

    public int getSpecialDefEV() {
        return specialDefEV;
    }

    public int getHpIV() {
        return hpIV;
    }

    public int getAttackIV() {
        return attackIV;
    }

    public int getDefenseIV() {
        return defenseIV;
    }

    public int getSpecialAtkIV() {
        return specialAtkIV;
    }

    public int getSpecialDefIV() {
        return specialDefIV;
    }

    public int getSpeedIV() {
        return speedIV;
    }
    
    public int getLevel() {
        return level;
    }
    
    // Setters
    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.min(currentHp, maxHp);
        if (this.currentHp < 0) this.currentHp = 0;
    }
    
    public void setLevel(int level) {
        this.level = level;
        // Recalculate stats with the new level
    }

    // Add these setter methods for EVs (with 252 max per stat)
    public void setHpEV(int value) {
        this.hpEV = Math.min(252, Math.max(0, value));
    }

    public void setAttackEV(int value) {
        this.attackEV = Math.min(252, Math.max(0, value));
    }

    public void setDefenseEV(int value) {
        this.defenseEV = Math.min(252, Math.max(0, value));
    }

    public void setSpeedEV(int value) {
        this.speedEV = Math.min(252, Math.max(0, value));
    }

    public void setSpecialAtkEV(int value) {
        this.specialAtkEV = Math.min(252, Math.max(0, value));
    }

    public void setSpecialDefEV(int value) {
        this.specialDefEV = Math.min(252, Math.max(0, value));
    }

    // Method to add EVs after battles
    public void addEVs(int hp, int atk, int def, int spd, int spAtk, int spDef) {
        setHpEV(hpEV + hp);
        setAttackEV(attackEV + atk);
        setDefenseEV(defenseEV + def);
        setSpeedEV(speedEV + spd);
        setSpecialAtkEV(specialAtkEV + spAtk);
        setSpecialDefEV(specialDefEV + spDef);
        
        // Recalculate stats with the new EVs
        calculateStats(baseHp, baseAttack, baseDefense, baseSpeed, baseSpAtk, baseSpDef);
    }
    

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSpecialAtk() {
        return specialAtk;
    }

    public int getSpecialDef() {
        return specialDef;
    }
    
    @Override
    public String toString() {
        return "Stats{" +
                "HP=" + currentHp + "/" + maxHp +
                ", Attack=" + attack + (attackModifier != 0 ? " (" + attackModifier + ")" : "") +
                ", Defense=" + defense + (defenseModifier != 0 ? " (" + defenseModifier + ")" : "") +
                ", Speed=" + speed + (speedModifier != 0 ? " (" + speedModifier + ")" : "") +
                ", Sp.Atk=" + specialAtk + (spAttModifier != 0 ? " (" + spAttModifier + ")" : "") +
                ", Sp.Def=" + specialDef + (spDefModifier != 0 ? " (" + spDefModifier + ")" : "") +
                ", Level=" + level +
                '}';
    }
}
