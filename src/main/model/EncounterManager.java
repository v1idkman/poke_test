package model;

import java.util.*;
import pokes.Pokemon;

public class EncounterManager {
    private static final Random random = new Random();
    private static final int DEFAULT_ENCOUNTER_RATE = 10; // 10% chance per step
    private static final int ENCOUNTER_CHECK_DELAY = 5; // Check every 5 frames
    
    private int encounterRate;
    private int frameCounter;
    private Map<String, List<EncounterTable.EncounterEntry>> locationTables;
    
    public EncounterManager() {
        this.encounterRate = DEFAULT_ENCOUNTER_RATE;
        this.frameCounter = 0;
        initializeEncounterTables();
    }
    
    private void initializeEncounterTables() {
        locationTables = new HashMap<>();
        
        // Use the existing encounter tables from EncounterTable class
        for (String location : getAvailableLocations()) {
            locationTables.put(location, EncounterTable.getEncounterTable(location));
        }
    }
    
    private List<String> getAvailableLocations() {
        // Return all available location keys from EncounterTable
        return EncounterTable.getAvailableLocations();
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
        // Normalize location string and get the appropriate encounter table
        String normalizedLocation = normalizeLocation(location);
        
        // Use the EncounterTable class to get a random encounter
        return EncounterTable.getRandomEncounter(normalizedLocation);
    }
    
    private String normalizeLocation(String location) {
        if (location == null) {
            return "route1";
        }
        
        location = location.toLowerCase().trim();
        
        // Check for location types
        if (location.contains("route")) {
            // Extract route number if possible
            if (location.matches(".*\\d+.*")) {
                return "route" + location.replaceAll("\\D+", "");
            }
            return "route1"; // Default to route1 if no number
        } else if (location.contains("forest") || location.contains("woods")) {
            return "forest";
        } else if (location.contains("cave") || location.contains("tunnel") || location.contains("mountain")) {
            return "cave";
        } else if (location.contains("city") || location.contains("town")) {
            return "city";
        }
        
        // Check if the location exists in our tables
        if (locationTables.containsKey(location)) {
            return location;
        }
        
        // Default fallback
        return "route1";
    }
}
