package model;

import java.util.*;
import pokes.Pokemon;
import pokes.PokemonFactory;
import pokes.PokemonStatsLoader;

public class EncounterTable {
    private static Map<String, List<EncounterEntry>> locationTables = new HashMap<>();
    private static Random random = new Random();
    private static boolean initialized = false;
    
    // Inner class to represent an encounter entry with weight
    public static class EncounterEntry {
        private int dexNumber;
        private int minLevel;
        private int maxLevel;
        private int weight; // Higher weight = more common
        private String specificForm; // For alternate forms like Alola, Galar, etc.
        
        public EncounterEntry(int dexNumber, int minLevel, int maxLevel, int weight) {
            this(dexNumber, minLevel, maxLevel, weight, null);
        }
        
        public EncounterEntry(int dexNumber, int minLevel, int maxLevel, int weight, String specificForm) {
            this.dexNumber = dexNumber;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.weight = weight;
            this.specificForm = specificForm;
        }
        
        public int getDexNumber() { return dexNumber; }
        public int getMinLevel() { return minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public int getWeight() { return weight; }
        public String getSpecificForm() { return specificForm; }
    }
    
    // Initialize encounter tables for different locations using CSV data
    public static void initializeEncounterTables() {
        if (initialized) return;
        // Route 1 encounters - Early game Pokémon
        List<EncounterEntry> route1Encounters = new ArrayList<>();
        route1Encounters.add(new EncounterEntry(16, 3, 5, 30)); // Pidgey
        route1Encounters.add(new EncounterEntry(19, 2, 4, 30)); // Rattata
        route1Encounters.add(new EncounterEntry(10, 3, 5, 15)); // Caterpie
        route1Encounters.add(new EncounterEntry(13, 3, 5, 15)); // Weedle
        route1Encounters.add(new EncounterEntry(25, 4, 6, 10)); // Pikachu
        locationTables.put("route1", route1Encounters);
        
        // Route 2 encounters - Slightly higher level
        List<EncounterEntry> route2Encounters = new ArrayList<>();
        route2Encounters.add(new EncounterEntry(16, 4, 6, 25)); // Pidgey
        route2Encounters.add(new EncounterEntry(19, 3, 5, 25)); // Rattata
        route2Encounters.add(new EncounterEntry(21, 4, 6, 20)); // Spearow
        route2Encounters.add(new EncounterEntry(32, 4, 6, 15)); // Nidoran♂
        route2Encounters.add(new EncounterEntry(29, 4, 6, 15)); // Nidoran♀
        locationTables.put("route2", route2Encounters);
        
        // Forest encounters - Bug types and forest dwellers
        List<EncounterEntry> forestEncounters = new ArrayList<>();
        forestEncounters.add(new EncounterEntry(10, 4, 7, 25)); // Caterpie
        forestEncounters.add(new EncounterEntry(11, 5, 7, 15)); // Metapod
        forestEncounters.add(new EncounterEntry(13, 4, 7, 25)); // Weedle
        forestEncounters.add(new EncounterEntry(14, 5, 7, 15)); // Kakuna
        forestEncounters.add(new EncounterEntry(25, 5, 8, 10)); // Pikachu
        forestEncounters.add(new EncounterEntry(127, 6, 9, 5)); // Pinsir
        forestEncounters.add(new EncounterEntry(43, 5, 8, 15)); // Oddish
        locationTables.put("forest", forestEncounters);
        
        // Cave encounters - Rock, Ground, and cave-dwelling Pokémon
        List<EncounterEntry> caveEncounters = new ArrayList<>();
        caveEncounters.add(new EncounterEntry(41, 8, 12, 30)); // Zubat
        caveEncounters.add(new EncounterEntry(74, 9, 13, 25)); // Geodude
        caveEncounters.add(new EncounterEntry(95, 10, 14, 15)); // Onix
        caveEncounters.add(new EncounterEntry(66, 9, 13, 20)); // Machop
        caveEncounters.add(new EncounterEntry(104, 10, 15, 10)); // Cubone
        locationTables.put("cave", caveEncounters);
        
        // Water encounters - Water types
        List<EncounterEntry> waterEncounters = new ArrayList<>();
        waterEncounters.add(new EncounterEntry(129, 5, 15, 40)); // Magikarp
        waterEncounters.add(new EncounterEntry(54, 10, 20, 25)); // Psyduck
        waterEncounters.add(new EncounterEntry(60, 10, 20, 20)); // Poliwag
        waterEncounters.add(new EncounterEntry(118, 10, 20, 10)); // Goldeen
        waterEncounters.add(new EncounterEntry(120, 15, 25, 5)); // Staryu
        locationTables.put("water", waterEncounters);
        
        // City encounters - Urban Pokémon
        List<EncounterEntry> cityEncounters = new ArrayList<>();
        cityEncounters.add(new EncounterEntry(19, 5, 10, 30)); // Rattata
        cityEncounters.add(new EncounterEntry(20, 10, 15, 15)); // Raticate
        cityEncounters.add(new EncounterEntry(81, 10, 15, 20)); // Magnemite
        cityEncounters.add(new EncounterEntry(100, 10, 15, 20)); // Voltorb
        cityEncounters.add(new EncounterEntry(52, 8, 12, 15)); // Meowth
        locationTables.put("city", cityEncounters);
        
        // Mountain encounters - High-level mountain Pokémon
        List<EncounterEntry> mountainEncounters = new ArrayList<>();
        mountainEncounters.add(new EncounterEntry(74, 15, 25, 25)); // Geodude
        mountainEncounters.add(new EncounterEntry(75, 20, 30, 15)); // Graveler
        mountainEncounters.add(new EncounterEntry(95, 15, 25, 20)); // Onix
        mountainEncounters.add(new EncounterEntry(77, 15, 25, 15)); // Ponyta
        mountainEncounters.add(new EncounterEntry(27, 12, 20, 15)); // Sandshrew
        mountainEncounters.add(new EncounterEntry(142, 20, 30, 10)); // Aerodactyl
        locationTables.put("mountain", mountainEncounters);
        
        // Grass encounters - General grassland Pokémon
        List<EncounterEntry> grassEncounters = new ArrayList<>();
        grassEncounters.add(new EncounterEntry(16, 5, 10, 25)); // Pidgey
        grassEncounters.add(new EncounterEntry(19, 5, 10, 25)); // Rattata
        grassEncounters.add(new EncounterEntry(43, 8, 12, 20)); // Oddish
        grassEncounters.add(new EncounterEntry(46, 8, 12, 15)); // Paras
        grassEncounters.add(new EncounterEntry(48, 10, 15, 10)); // Venonat
        grassEncounters.add(new EncounterEntry(114, 12, 18, 5)); // Tangela
        locationTables.put("grass", grassEncounters);
        
        // Default encounters (fallback) - Starter Pokémon
        List<EncounterEntry> defaultEncounters = new ArrayList<>();
        defaultEncounters.add(new EncounterEntry(1, 5, 10, 33)); // Bulbasaur
        defaultEncounters.add(new EncounterEntry(4, 5, 10, 33)); // Charmander
        defaultEncounters.add(new EncounterEntry(7, 5, 10, 34)); // Squirtle
        locationTables.put("default", defaultEncounters);
        
        initialized = true;
    }
    
    public static Pokemon getRandomEncounter(String location) {
        if (!initialized) {
            initializeEncounterTables();
        }
        
        if (!locationTables.containsKey(location)) {
            location = "default";
        }
        
        List<EncounterEntry> encounters = locationTables.get(location);
        if (encounters == null || encounters.isEmpty()) {
            return PokemonFactory.createPokemon(1, 5, "Bulbasaur");
        }
        
        EncounterEntry selectedEntry = getWeightedRandomEncounter(encounters);
        
        int level = selectedEntry.getMinLevel() + 
                    random.nextInt(selectedEntry.getMaxLevel() - selectedEntry.getMinLevel() + 1);
        
        PokemonStatsLoader loader = PokemonStatsLoader.getInstance();
        String pokemonName;
        
        if (selectedEntry.getSpecificForm() != null) {
            // Use specific form name if specified
            pokemonName = selectedEntry.getSpecificForm();
        } else {
            // Get the default name for this dex number
            pokemonName = loader.getPokemonName(selectedEntry.getDexNumber());
            
            // Debug output to see what name we're getting
            System.out.println("Retrieved name for dex #" + selectedEntry.getDexNumber() + ": " + pokemonName);
            
            // If name is still problematic, try alternative approaches
            if (pokemonName == null || pokemonName.startsWith("Pokemon #") || pokemonName.equals("Unknown")) {
                List<String> availableForms = loader.getFormsForDex(selectedEntry.getDexNumber());
                if (!availableForms.isEmpty()) {
                    pokemonName = availableForms.get(0);
                    // Extract base name if it contains form info
                    if (pokemonName.contains(" (")) {
                        pokemonName = pokemonName.substring(0, pokemonName.indexOf(" ("));
                    }
                }
            }
        }
        
        return PokemonFactory.createPokemon(selectedEntry.getDexNumber(), level, pokemonName);
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
        if (!initialized) {
            initializeEncounterTables();
        }
        return locationTables.getOrDefault(location, locationTables.get("default"));
    }
    
    // Get all available locations
    public static List<String> getAvailableLocations() {
        if (!initialized) {
            initializeEncounterTables();
        }
        return new ArrayList<>(locationTables.keySet());
    }
    
    // Add method to create regional variant encounters
    public static void addRegionalVariants() {
        // Example: Add Alolan forms to tropical locations
        List<EncounterEntry> tropicalEncounters = new ArrayList<>();
        tropicalEncounters.add(new EncounterEntry(19, 5, 10, 30, "Rattata (Alola)"));
        tropicalEncounters.add(new EncounterEntry(20, 10, 15, 15, "Raticate (Alola)"));
        tropicalEncounters.add(new EncounterEntry(27, 8, 12, 25, "Sandshrew (Alola)"));
        tropicalEncounters.add(new EncounterEntry(28, 15, 20, 10, "Sandslash (Alola)"));
        tropicalEncounters.add(new EncounterEntry(37, 8, 12, 20, "Vulpix (Alola)"));
        locationTables.put("tropical", tropicalEncounters);
        
        // Add Galarian forms to certain locations
        List<EncounterEntry> galarEncounters = new ArrayList<>();
        galarEncounters.add(new EncounterEntry(77, 10, 15, 30, "Ponyta (Galar)"));
        galarEncounters.add(new EncounterEntry(78, 20, 25, 15, "Rapidash (Galar)"));
        galarEncounters.add(new EncounterEntry(83, 8, 12, 25, "Farfetch'd (Galar)"));
        galarEncounters.add(new EncounterEntry(52, 5, 10, 30, "Meowth (Galar)"));
        locationTables.put("galar", galarEncounters);
    }
}
