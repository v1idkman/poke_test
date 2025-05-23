package model;

import java.util.*;
import pokes.Pokemon;

public class EncounterManager {
    private static final Random random = new Random();
    private static final int DEFAULT_ENCOUNTER_RATE = 30; // 30% chance per step
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
        
        // Initialize the encounter tables
        EncounterTable.initializeEncounterTables();
        
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
        this.encounterRate = Math.max(0, Math.min(100, rate)); // Clamp between 0-100
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
        
        // Check for specific location patterns
        if (location.contains("route")) {
            // Extract route number if possible
            if (location.matches(".*\\d+.*")) {
                String routeNumber = location.replaceAll("\\D+", "");
                String routeKey = "route" + routeNumber;
                // Check if this specific route exists, otherwise default to route1
                if (locationTables.containsKey(routeKey)) {
                    return routeKey;
                }
            }
            return "route1"; // Default to route1 if no number or route doesn't exist
        } else if (location.contains("forest") || location.contains("woods")) {
            return "forest";
        } else if (location.contains("cave") || location.contains("tunnel")) {
            return "cave";
        } else if (location.contains("mountain") || location.contains("peak")) {
            return "mountain";
        } else if (location.contains("water") || location.contains("sea") || location.contains("ocean") || 
                   location.contains("lake") || location.contains("river")) {
            return "water";
        } else if (location.contains("city") || location.contains("town")) {
            return "city";
        } else if (location.contains("grass") || location.contains("meadow") || location.contains("field")) {
            return "grass";
        } else if (location.contains("tropical") || location.contains("island")) {
            return "tropical";
        } else if (location.contains("galar") || location.contains("crown")) {
            return "galar";
        }
        
        // Check if the exact location exists in our tables
        if (locationTables.containsKey(location)) {
            return location;
        }
        
        // Default fallback
        return "route1";
    }
    
    // Method to get encounter information for a location
    public List<EncounterTable.EncounterEntry> getLocationEncounters(String location) {
        String normalizedLocation = normalizeLocation(location);
        return locationTables.getOrDefault(normalizedLocation, locationTables.get("default"));
    }
    
    // Method to check if a location has encounters
    public boolean hasEncounters(String location) {
        String normalizedLocation = normalizeLocation(location);
        return locationTables.containsKey(normalizedLocation);
    }
    
    // Method to add custom encounters for modding/testing
    public void addCustomLocation(String locationName, List<EncounterTable.EncounterEntry> encounters) {
        locationTables.put(locationName.toLowerCase(), encounters);
    }
}
