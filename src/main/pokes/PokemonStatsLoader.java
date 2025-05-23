package pokes;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class PokemonStatsLoader {
    private static PokemonStatsLoader instance;
    private Map<String, int[]> pokemonBaseStats = new HashMap<>();
    private Map<Integer, String> pokemonNames = new HashMap<>();
    private Map<String, String[]> pokemonTypes = new HashMap<>(); // Changed to use composite key
    private Map<Integer, Boolean> pokemonLegendary = new HashMap<>();
    private Map<Integer, List<String>> pokemonNamesByDex = new HashMap<>();
    private Map<String, String> pokemonClassifications = new HashMap<>();
    private Map<String, String> pokemonAbilities = new HashMap<>();
    private Map<String, String> pokemonExpGrowth = new HashMap<>();
    
    private PokemonStatsLoader() {}
    
    public static PokemonStatsLoader getInstance() {
        if (instance == null) {
            instance = new PokemonStatsLoader();
        }
        return instance;
    }

    public void loadFromCSV(String filePath) {
        try {
            // First try to load as a classpath resource
            InputStream inputStream = getClass().getResourceAsStream(filePath);
            
            if (inputStream != null) {
                loadFromInputStream(inputStream);
            } else {
                // Fallback to file path
                loadFromFile(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromInputStream(InputStream inputStream) throws IOException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            loadCSVData(reader);
        }
    }

    private void loadFromFile(String filePath) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            loadCSVData(reader);
        }
    }

    private void loadCSVData(CSVReader reader) throws IOException {
        // Skip header row
        reader.readNext();
        
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            try {
                // Parse the CSV structure based on the provided file
                int pokemonId = parseIntSafely(nextLine[0], 0);
                int pokemonDex = parseIntSafely(nextLine[1], 0);
                String pokemonName = cleanPokemonName(nextLine[2]); // Column 3 (index 2)
                String classification = cleanString(nextLine[3]);
                String alternateForm = cleanString(nextLine[4]);
                String originalPokemonId = cleanString(nextLine[5]);
                String legendaryType = cleanString(nextLine[6]);
                
                // Skip height and weight (columns 7-8)
                String primaryType = cleanString(nextLine[9]);
                String secondaryType = cleanString(nextLine[10]);
                String primaryAbility = cleanString(nextLine[11]);
                String hiddenAbility = cleanString(nextLine[15]);
                
                // Stats are in columns 22-27
                int hp = parseIntSafely(nextLine[22], 50);
                int attack = parseIntSafely(nextLine[23], 50);
                int defense = parseIntSafely(nextLine[24], 50);
                int spAtk = parseIntSafely(nextLine[25], 50);
                int spDef = parseIntSafely(nextLine[26], 50);
                int speed = parseIntSafely(nextLine[27], 50);
                
                // Experience growth is in column 37
                String expGrowth = cleanString(nextLine[37]);
                
                // Handle alternate forms in the name
                String fullName = pokemonName;
                if (alternateForm != null && !alternateForm.isEmpty() && !alternateForm.equals("NULL")) {
                    fullName = pokemonName + " (" + alternateForm + ")";
                }
                
                // Create composite keys
                String compositeKey = pokemonDex + "_" + fullName;
                
                // Store base stats
                pokemonBaseStats.put(compositeKey, new int[]{hp, attack, defense, speed, spAtk, spDef});
                
                // Store the Pokemon name - FIXED: Always store the name for each dex number
                // If this is the first time we see this dex number, or if it's the base form (no alternate form)
                if (!pokemonNames.containsKey(pokemonDex) || alternateForm == null || alternateForm.equals("NULL")) {
                    pokemonNames.put(pokemonDex, pokemonName);
                }
                
                // Store the Pokemon types using both composite key and dex-only key
                pokemonTypes.put(compositeKey, new String[]{primaryType, secondaryType});
                // Also store by dex number for fallback
                if (!pokemonTypes.containsKey(String.valueOf(pokemonDex))) {
                    pokemonTypes.put(String.valueOf(pokemonDex), new String[]{primaryType, secondaryType});
                }
                
                // Store legendary status
                boolean isLegendary = legendaryType != null && !legendaryType.isEmpty() && !legendaryType.equals("NULL");
                pokemonLegendary.put(pokemonDex, isLegendary);
                
                // Store additional data
                pokemonClassifications.put(compositeKey, classification);
                pokemonAbilities.put(compositeKey, primaryAbility);
                pokemonExpGrowth.put(compositeKey, expGrowth);
                
                // Store names by dex for forms lookup
                if (!pokemonNamesByDex.containsKey(pokemonDex)) {
                    pokemonNamesByDex.put(pokemonDex, new ArrayList<>());
                }
                if (!pokemonNamesByDex.get(pokemonDex).contains(fullName)) {
                    pokemonNamesByDex.get(pokemonDex).add(fullName);
                }
                
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Skipping malformed row in CSV: " + e.getMessage());
            }
        }
        
        System.out.println("Loaded " + pokemonBaseStats.size() + " Pokemon from CSV");
    }

    // Clean Pokemon name by removing quotes and handling special characters
    private String cleanPokemonName(String name) {
        if (name == null) return "Unknown";
        
        // Remove quotes and trim whitespace
        String cleanedName = name.replace("\"", "").trim();
        
        // Handle special cases
        if (cleanedName.isEmpty() || cleanedName.equals("NULL")) {
            return "Unknown";
        }
        
        return cleanedName;
    }

    // Update the getPokemonName method to ensure it returns the correct name
    public String getPokemonName(int dexNumber) {
        String name = pokemonNames.get(dexNumber);
        if (name != null && !name.isEmpty() && !name.equals("Unknown")) {
            return name;
        }
        
        // If not found in the main map, try to find any form of this Pokemon
        List<String> forms = pokemonNamesByDex.get(dexNumber);
        if (forms != null && !forms.isEmpty()) {
            // Return the first form (usually the base form)
            String firstForm = forms.get(0);
            // Extract just the name part if it contains form information
            if (firstForm.contains(" (")) {
                return firstForm.substring(0, firstForm.indexOf(" ("));
            }
            return firstForm;
        }
        
        // Final fallback
        return "Pokemon #" + dexNumber;
    }
    
    // Clean general strings
    private String cleanString(String value) {
        if (value == null || value.equals("NULL") || value.isEmpty()) {
            return null;
        }
        return value.replace("\"", "").trim();
    }

    public String[] getPokemonTypesByNameAndDex(int dexNumber, String name) {
        // First try to find by composite key (for specific forms)
        String compositeKey = dexNumber + "_" + name;
        String[] types = pokemonTypes.get(compositeKey);
        
        if (types != null) {
            return types;
        }
        
        // If not found, try to find by dex number only
        types = pokemonTypes.get(String.valueOf(dexNumber));
        if (types != null) {
            return types;
        }
        
        // If still not found, try to find any form of this Pokemon
        for (String key : pokemonTypes.keySet()) {
            if (key.startsWith(dexNumber + "_")) {
                types = pokemonTypes.get(key);
                if (types != null) {
                    return types;
                }
            }
        }
        
        // Final fallback
        return new String[]{"Normal", ""};
    }
    
    // Get base stats using both dex number and name
    public int[] getBaseStats(int dexNumber, String name) {
        String compositeKey = dexNumber + "_" + name;
        int[] stats = pokemonBaseStats.get(compositeKey);
        
        // If exact match not found, try to find by dex number only
        if (stats == null) {
            for (String key : pokemonBaseStats.keySet()) {
                if (key.startsWith(dexNumber + "_")) {
                    stats = pokemonBaseStats.get(key);
                    break;
                }
            }
        }
        
        return stats != null ? stats : new int[]{50, 50, 50, 50, 50, 50}; // Default stats
    }
    
    // Get all forms for a given dex number
    public List<String> getFormsForDex(int dexNumber) {
        return pokemonNamesByDex.getOrDefault(dexNumber, new ArrayList<>());
    }
    
    // Get Pokemon classification
    public String getPokemonClassification(int dexNumber, String name) {
        String compositeKey = dexNumber + "_" + name;
        return pokemonClassifications.getOrDefault(compositeKey, "Unknown Pokémon");
    }
    
    // Get Pokemon ability
    public String getPokemonAbility(int dexNumber, String name) {
        String compositeKey = dexNumber + "_" + name;
        return pokemonAbilities.getOrDefault(compositeKey, "No Ability");
    }
    
    // Get Pokemon experience growth rate
    public String getPokemonExpGrowth(int dexNumber, String name) {
        String compositeKey = dexNumber + "_" + name;
        return pokemonExpGrowth.getOrDefault(compositeKey, "Medium Fast");
    }
    
    // Helper method to safely parse integers from strings
    private int parseIntSafely(String value, int defaultValue) {
        if (value == null || value.isEmpty() || "null".equals(value) || "NULL".equals(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    } 
    
    public boolean parseBoolSafely(String value) {
        if (value == null || value.isEmpty() || "null".equals(value) || "NULL".equals(value)) {
            return false;
        }
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (Exception e) {
            return false;
        }
    }
    
    public String[] getPokemonTypes(int dexNumber) {
        // Try to find any form of this Pokemon
        for (String key : pokemonTypes.keySet()) {
            if (key.startsWith(dexNumber + "_")) {
                return pokemonTypes.get(key);
            }
        }
        return new String[]{"NORMAL", ""};
    }
    
    public boolean isLegendary(int dexNumber) {
        return pokemonLegendary.getOrDefault(dexNumber, false);
    }
}
