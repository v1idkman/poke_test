package model;

import java.util.*;
import pokes.Pokemon;
import pokes.PokemonFactory;

public class EncounterTable {
    private static Map<String, List<EncounterEntry>> locationTables = new HashMap<>();
    private static Random random = new Random();
    
    // Inner class to represent an encounter entry with weight
    public static class EncounterEntry {
        private int dexNumber;
        private int minLevel;
        private int maxLevel;
        private int weight; // Higher weight = more common
        
        public EncounterEntry(int dexNumber, int minLevel, int maxLevel, int weight) {
            this.dexNumber = dexNumber;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.weight = weight;
        }
        
        public int getDexNumber() { return dexNumber; }
        public int getMinLevel() { return minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public int getWeight() { return weight; }
    }
    
    // Initialize encounter tables for different locations
    public static void initializeEncounterTables() {
        // Route 1 encounters
        List<EncounterEntry> route1Encounters = new ArrayList<>();
        route1Encounters.add(new EncounterEntry(16, 3, 5, 30)); // Pidgey (common)
        route1Encounters.add(new EncounterEntry(19, 2, 4, 30)); // Rattata (common)
        route1Encounters.add(new EncounterEntry(10, 3, 5, 15)); // Caterpie (uncommon)
        route1Encounters.add(new EncounterEntry(13, 3, 5, 15)); // Weedle (uncommon)
        route1Encounters.add(new EncounterEntry(25, 4, 6, 10)); // Pikachu (rare)
        locationTables.put("route1", route1Encounters);
        
        // Forest encounters
        List<EncounterEntry> forestEncounters = new ArrayList<>();
        forestEncounters.add(new EncounterEntry(10, 4, 7, 25)); // Caterpie (common)
        forestEncounters.add(new EncounterEntry(11, 5, 7, 15)); // Metapod (uncommon)
        forestEncounters.add(new EncounterEntry(13, 4, 7, 25)); // Weedle (common)
        forestEncounters.add(new EncounterEntry(14, 5, 7, 15)); // Kakuna (uncommon)
        forestEncounters.add(new EncounterEntry(25, 5, 8, 10)); // Pikachu (rare)
        forestEncounters.add(new EncounterEntry(127, 6, 9, 5)); // Pinsir (very rare)
        locationTables.put("forest", forestEncounters);
        
        // Cave encounters
        List<EncounterEntry> caveEncounters = new ArrayList<>();
        caveEncounters.add(new EncounterEntry(41, 8, 12, 30)); // Zubat (very common)
        caveEncounters.add(new EncounterEntry(74, 9, 13, 25)); // Geodude (common)
        caveEncounters.add(new EncounterEntry(95, 10, 14, 15)); // Onix (uncommon)
        caveEncounters.add(new EncounterEntry(66, 9, 13, 20)); // Machop (uncommon)
        caveEncounters.add(new EncounterEntry(104, 10, 15, 10)); // Cubone (rare)
        locationTables.put("cave", caveEncounters);
        
        // City encounters (rare urban Pokémon)
        List<EncounterEntry> cityEncounters = new ArrayList<>();
        cityEncounters.add(new EncounterEntry(19, 5, 10, 30)); // Rattata (common)
        cityEncounters.add(new EncounterEntry(20, 10, 15, 15)); // Raticate (uncommon)
        cityEncounters.add(new EncounterEntry(81, 10, 15, 20)); // Magnemite (uncommon)
        cityEncounters.add(new EncounterEntry(100, 10, 15, 20)); // Voltorb (uncommon)
        cityEncounters.add(new EncounterEntry(132, 10, 15, 15)); // Ditto (rare)
        locationTables.put("city", cityEncounters);
        
        // Default encounters (fallback)
        List<EncounterEntry> defaultEncounters = new ArrayList<>();
        defaultEncounters.add(new EncounterEntry(4, 5, 10, 100)); // Charmander
        locationTables.put("default", defaultEncounters);
    }
    
    // Get a random encounter for a specific location
    public static Pokemon getRandomEncounter(String location) {
        if (!locationTables.containsKey(location)) {
            // Default to route1 if location not found
            location = "default";
        }
        
        List<EncounterEntry> encounters = locationTables.get(location);
        if (encounters == null || encounters.isEmpty()) {
            // Fallback to a default Pokémon if no encounters defined
            return PokemonFactory.createPokemon(4, 5, "Charmander");
        }
        
        // Select a random Pokémon based on weight
        EncounterEntry selectedEntry = getWeightedRandomEncounter(encounters);
        
        // Generate a random level within the min-max range
        int level = selectedEntry.getMinLevel() + 
                    random.nextInt(selectedEntry.getMaxLevel() - selectedEntry.getMinLevel() + 1);
        
        // Create and return the Pokémon
        return PokemonFactory.createPokemon(selectedEntry.getDexNumber(), level, null);
    }
    
    private static EncounterEntry getWeightedRandomEncounter(List<EncounterEntry> encounters) {
        // Calculate total weight
        int totalWeight = 0;
        for (EncounterEntry entry : encounters) {
            totalWeight += entry.getWeight();
        }
        
        // Select a random value within the total weight
        int randomValue = random.nextInt(totalWeight);
        
        // Find the corresponding entry
        int currentWeight = 0;
        for (EncounterEntry entry : encounters) {
            currentWeight += entry.getWeight();
            if (randomValue < currentWeight) {
                return entry;
            }
        }
        
        // Fallback (should never happen)
        return encounters.get(0);
    }
    
    // Get encounter table for a specific location
    public static List<EncounterEntry> getEncounterTable(String location) {
        return locationTables.getOrDefault(location, locationTables.get("default"));
    }
    
    // Get all available locations
    public static List<String> getAvailableLocations() {
        return new ArrayList<>(locationTables.keySet());
    }
}
