package pokes;

import java.util.HashMap;
import java.util.Map;

public class LevelManager {
    // Constants for experience growth rates
    public enum ExpGrowthRate {
        ERRATIC,       // 600,000 total exp to level 100
        FAST,          // 800,000 total exp to level 100
        MEDIUM_FAST,   // 1,000,000 total exp to level 100 (most common)
        MEDIUM_SLOW,   // 1,059,860 total exp to level 100
        SLOW,          // 1,250,000 total exp to level 100
        FLUCTUATING    // 1,640,000 total exp to level 100
    }
    
    private int level;
    private int currentExp;
    private int expToNextLevel;
    private int currentLevelExp; // New property for exp within current level
    private ExpGrowthRate growthRate;
    private Pokemon pokemon;
    
    // Cache for experience requirements to avoid recalculating
    private static final Map<String, Integer> expCache = new HashMap<>();
    
    public LevelManager(Pokemon pokemon, int startingLevel, ExpGrowthRate growthRate) {
        this.pokemon = pokemon;
        this.level = startingLevel;
        this.growthRate = growthRate;
        this.currentExp = calculateTotalExpForLevel(startingLevel);
        
        // Calculate exp to next level
        int nextLevelExp = calculateTotalExpForLevel(startingLevel + 1);
        this.expToNextLevel = Math.max(0, nextLevelExp - currentExp);
        
        // Calculate current level exp (exp gained in current level)
        this.currentLevelExp = currentExp - calculateTotalExpForLevel(startingLevel);
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getCurrentExp() {
        return currentExp;
    }
    
    public int getExpToNextLevel() {
        return expToNextLevel;
    }
    
    /**
     * Get the experience points gained within the current level
     * @return Experience points gained since reaching the current level
     */
    public int getCurrentLevelExp() {
        return currentLevelExp;
    }
    
    public ExpGrowthRate getGrowthRate() {
        return growthRate;
    }
    
    /**
     * Add experience points to the Pokémon
     * @param exp The amount of experience to add
     * @return true if the Pokémon leveled up, false otherwise
     */
    public boolean addExperience(int exp) {
        boolean leveledUp = false;
        currentExp += exp;
        
        // Check for level up
        while (currentExp >= calculateTotalExpForLevel(level + 1) && level < 100) {
            level++;
            leveledUp = true;
            
            // Update the Pokémon's stats for the new level
            pokemon.getStats().updateLevel(level);
            
            // Check if the Pokémon learns any moves at this level
            checkForNewMoves();
        }
        
        // Update exp required for next level
        if (level < 100) {
            int nextLevelExp = calculateTotalExpForLevel(level + 1);
            expToNextLevel = Math.max(0, nextLevelExp - currentExp);
            
            // Update current level exp
            currentLevelExp = currentExp - calculateTotalExpForLevel(level);
        } else {
            expToNextLevel = 0; // Max level reached
            currentLevelExp = 0; // No more exp needed at max level
        }
        
        return leveledUp;
    }
    
    /**
     * Calculate the total experience required to reach a specific level
     */
    public int calculateTotalExpForLevel(int targetLevel) {
        // Check cache first
        String cacheKey = growthRate.name() + "_" + targetLevel;
        if (expCache.containsKey(cacheKey)) {
            return expCache.get(cacheKey);
        }
        
        // Ensure level is within valid range
        if (targetLevel <= 1) return 0;
        if (targetLevel > 100) targetLevel = 100;
        
        int exp;
        switch (growthRate) {
            case ERRATIC:
                if (targetLevel <= 50) {
                    exp = (int) ((Math.pow(targetLevel, 3) * (100 - targetLevel)) / 50);
                } else if (targetLevel <= 68) {
                    exp = (int) ((Math.pow(targetLevel, 3) * (150 - targetLevel)) / 100);
                } else if (targetLevel <= 98) {
                    exp = (int) ((Math.pow(targetLevel, 3) * ((1911 - 10 * targetLevel) / 3.0)) / 500);
                } else {
                    exp = (int) ((Math.pow(targetLevel, 3) * (160 - targetLevel)) / 100);
                }
                break;
                
            case FAST:
                exp = (int) (4 * Math.pow(targetLevel, 3) / 5);
                break;
                
            case MEDIUM_FAST:
                exp = (int) Math.pow(targetLevel, 3);
                break;
                
            case MEDIUM_SLOW:
                exp = (int) ((6 * Math.pow(targetLevel, 3) / 5.0) - (15 * Math.pow(targetLevel, 2)) + (100 * targetLevel) - 140);
                // Fix for level 1 underflow
                if (targetLevel == 1) exp = 0;
                break;
                
            case SLOW:
                exp = (int) (5 * Math.pow(targetLevel, 3) / 4);
                break;
                
            case FLUCTUATING:
                if (targetLevel <= 15) {
                    exp = (int) (Math.pow(targetLevel, 3) * (((targetLevel + 1) / 3.0 + 24) / 50));
                } else if (targetLevel <= 36) {
                    exp = (int) (Math.pow(targetLevel, 3) * ((targetLevel + 14) / 50.0));
                } else {
                    // Fix for levels 37-100
                    exp = (int) (Math.pow(targetLevel, 3) * (((targetLevel / 2.0) + 32) / 50));
                }
                break;
                
            default:
                exp = (int) Math.pow(targetLevel, 3);
        }
        
        // Cache the result
        expCache.put(cacheKey, exp);
        return exp;
    }
    
    /**
     * Calculate experience required for a specific level
     */
    public int calculateExpRequiredForLevel(int targetLevel) {
        return calculateTotalExpForLevel(targetLevel) - calculateTotalExpForLevel(targetLevel - 1);
    }
    
    /**
     * Check if the Pokémon learns any new moves at the current level
     */
    private void checkForNewMoves() {
        // This would connect to a move database to check for level-up moves
        // For now, we'll just log that we're checking
        System.out.println("Checking if " + pokemon.getName() + " learns any moves at level " + level);
    }
    
    /**
     * Calculate experience gained from defeating a Pokémon
     * @param defeatedPokemon The defeated Pokémon
     * @param participantCount Number of Pokémon that participated in the battle
     * @param isWildBattle Whether this was a wild battle or trainer battle
     * @return The amount of experience gained
     */
    public static int calculateExpGain(Pokemon defeatedPokemon, int participantCount, boolean isWildBattle) {
        // Base formula: (a * L * b) / (7 * c)
        // a = base exp yield of defeated Pokémon (using level as approximation)
        // L = level of defeated Pokémon
        // b = 1.5 if trainer battle, 1 if wild battle
        // c = number of Pokémon that participated in battle
        
        int baseExpYield = defeatedPokemon.getLevel() * 5; // Simplified approximation
        double trainerBonus = isWildBattle ? 1.0 : 1.5;
        
        int expGain = (int) ((baseExpYield * defeatedPokemon.getLevel() * trainerBonus) / (7 * Math.max(1, participantCount)));
        
        // Ensure minimum experience gain
        return Math.max(1, expGain);
    }
}
