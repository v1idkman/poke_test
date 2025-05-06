package pokes;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class PokemonStatsLoader {
    private static PokemonStatsLoader instance;
    private Map<String, int[]> pokemonBaseStats = new HashMap<>();
    private Map<Integer, String> pokemonNames = new HashMap<>();
    private Map<Integer, String[]> pokemonTypes = new HashMap<>();
    private Map<Integer, Boolean> pokemonLegendary = new HashMap<>();
    private Map<Integer, List<String>> pokemonNamesByDex = new HashMap<>();
    
    private PokemonStatsLoader() {}
    
    public static PokemonStatsLoader getInstance() {
        if (instance == null) {
            instance = new PokemonStatsLoader();
        }
        return instance;
    }

    public void loadFromCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header row
            reader.readNext();
            
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    int pokemonDex = parseIntSafely(nextLine[0], 0);
                    String pokemonName = nextLine[1];
                    String type1 = nextLine[2];
                    String type2 = nextLine[3];
                    int hp = parseIntSafely(nextLine[5], 0);
                    int attack = parseIntSafely(nextLine[6], 0);
                    int defense = parseIntSafely(nextLine[7], 0);
                    int spAtk = parseIntSafely(nextLine[8], 0);
                    int spDef = parseIntSafely(nextLine[9], 0);
                    int speed = parseIntSafely(nextLine[10], 0);
                    boolean legendary = parseBoolSafely(nextLine[12]);
                    
                    // Create a composite key using both dex number and name
                    String compositeKey = pokemonDex + "_" + pokemonName;
                    pokemonBaseStats.put(compositeKey, new int[]{hp, attack, defense, speed, spAtk, spDef});
                    
                    // Store the Pokémon name
                    pokemonNames.put(pokemonDex, pokemonName);
                    
                    // Store the Pokémon types
                    pokemonTypes.put(pokemonDex, new String[]{type1, type2});
                    
                    // Store legendary status
                    pokemonLegendary.put(pokemonDex, legendary);
                    
                    // Store names by dex for forms lookup
                    if (!pokemonNamesByDex.containsKey(pokemonDex)) {
                        pokemonNamesByDex.put(pokemonDex, new ArrayList<>());
                    }
                    pokemonNamesByDex.get(pokemonDex).add(pokemonName);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Skipping malformed row in CSV");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getPokemonTypesByNameAndDex(int dexNumber, String name) {
        // The types are stored by dex number only, not by composite key
        return pokemonTypes.getOrDefault(dexNumber, new String[]{"NORMAL", ""});
    }
    
    // Get base stats using both dex number and name
    public int[] getBaseStats(int dexNumber, String name) {
        String compositeKey = dexNumber + "_" + name;
        return pokemonBaseStats.get(compositeKey);
    }
    
    // Get all forms for a given dex number
    public List<String> getFormsForDex(int dexNumber) {
        return pokemonNamesByDex.getOrDefault(dexNumber, new ArrayList<>());
    }
    
    // Helper method to safely parse integers from strings
    private int parseIntSafely(String value, int defaultValue) {
        if (value == null || value.isEmpty() || "null".equals(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    } 
    
    public boolean parseBoolSafely(String value) {
        if (value == null || value.isEmpty() || "null".equals(value)) {
            return false;
        }
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (Exception e) {
            return false;
        }
    }

    public String getPokemonName(int dexNumber) {
        return pokemonNames.getOrDefault(dexNumber, "MissingNo");
    }
    
    public String[] getPokemonTypes(int dexNumber) {
        return pokemonTypes.getOrDefault(dexNumber, new String[]{"NORMAL", ""});
    }
    
    public boolean isLegendary(int dexNumber) {
        return pokemonLegendary.getOrDefault(dexNumber, false);
    }
}