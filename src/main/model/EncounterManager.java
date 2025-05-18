package model;

import java.util.Random;
import pokes.Pokemon;
import pokes.PokemonFactory;

public class EncounterManager {
    private static final Random random = new Random();
    private static final int DEFAULT_ENCOUNTER_RATE = 10; // 10% chance per step
    private static final int ENCOUNTER_CHECK_DELAY = 5; // Check every 5 frames
    
    private int encounterRate;
    private int frameCounter;
    
    public EncounterManager() {
        this.encounterRate = DEFAULT_ENCOUNTER_RATE;
        this.frameCounter = 0;
    }
    
    public void setEncounterRate(int rate) {
        this.encounterRate = rate;
    }
    
    public boolean checkEncounter(boolean isInGrass, boolean isMoving) {
        if (isInGrass && isMoving) {
            frameCounter++;
            
            if (frameCounter >= ENCOUNTER_CHECK_DELAY) {
                frameCounter = 0;
                
                int chance = random.nextInt(100);
                
                if (chance < encounterRate) {
                    return true;
                }
            }
        } else {
            frameCounter = 0;
        }
        
        return false;
    }    
    
    public Pokemon generateWildPokemon(String location) {
        // This is where you'd implement logic to determine which Pokémon appears
        // based on the current location, time of day, etc.
        
        // For now, just return a random Pokémon between ID 1-10 at level 5-10
        // int pokemonId = random.nextInt(10) + 1;
        int level = random.nextInt(6) + 5;
        
        return PokemonFactory.createPokemon(4, level, "Charmander");
    }
}
