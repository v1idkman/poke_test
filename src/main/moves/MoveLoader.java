package moves;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import pokes.Pokemon.PokemonType;
import model.Move;
import model.Move.MoveCategory;
import model.Move.StatusEffect;

import au.com.bytecode.opencsv.CSVReader;

public class MoveLoader {
    private static MoveLoader instance;
    private Map<String, Move> movesByName;
    
    private MoveLoader() {
        movesByName = new HashMap<>();
    }
    
    public static MoveLoader getInstance() {
        if (instance == null) {
            instance = new MoveLoader();
        }
        return instance;
    }
    
    public void loadFromCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header row
            reader.readNext();
            
            String[] data;
            while ((data = reader.readNext()) != null) {
                try {
                    // Skip empty lines
                    if (data.length == 0 || (data.length == 1 && data[0].trim().isEmpty())) {
                        continue;
                    }
                    
                    // Extract data
                    String name = data[1].trim();
                    // Skip if name is empty
                    if (name.isEmpty()) {
                        continue;
                    }
                    
                    String effect = data[2].trim();
                    PokemonType type = parseType(data[3].trim());
                    MoveCategory category = parseCategory(data[4].trim());
                    int power = parseIntOrDefault(data[5], 0);
                    int accuracy = parseAccuracy(data[6].trim());
                    int pp = parseIntOrDefault(data[7], 0);
                    
                    // Create move based on category
                    Move move;
                    if (category == MoveCategory.STATUS) {
                        StatusEffect statusEffect = parseStatusEffect(effect);
                        int statusChance = parseStatusChance(effect);
                        move = new Move(name, type, accuracy, pp, statusEffect, statusChance);
                    } else {
                        move = new Move(name, type, power, accuracy, pp, category);
                    }
                    
                    // Store move by name
                    movesByName.put(name.toLowerCase(), move);
                    
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Skipping malformed row in CSV");
                }
            }
            System.out.println("Loaded " + movesByName.size() + " moves from CSV file.");
        } catch (IOException e) {
            System.err.println("Error loading moves from CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadFromResource(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            
            // Skip header line
            String line = br.readLine();
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Parse CSV line
                String[] data = line.split(",");
                if (data.length < 8) {
                    continue; // Skip incomplete lines
                }
                
                // Extract data
                String name = data[1].trim();
                // Skip if name is empty
                if (name.isEmpty()) {
                    continue;
                }
                
                String effect = data[2].trim();
                PokemonType type = parseType(data[3].trim());
                MoveCategory category = parseCategory(data[4].trim());
                int power = parseIntOrDefault(data[5], 0);
                int accuracy = parseAccuracy(data[6].trim());
                int pp = parseIntOrDefault(data[7], 0);
                
                // Create move based on category
                Move move;
                if (category == MoveCategory.STATUS) {
                    StatusEffect statusEffect = parseStatusEffect(effect);
                    int statusChance = parseStatusChance(effect);
                    move = new Move(name, type, accuracy, pp, statusEffect, statusChance);
                } else {
                    move = new Move(name, type, power, accuracy, pp, category);
                }
                
                // Store move by name
                movesByName.put(name.toLowerCase(), move);
            }
            
            System.out.println("Loaded " + movesByName.size() + " moves from resource.");
            
        } catch (IOException e) {
            System.err.println("Error loading moves from resource: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private int parseAccuracy(String accuracy) {
        if (accuracy == null || accuracy.trim().isEmpty()) {
            return 100; // Default accuracy
        }
        
        try {
            if (accuracy.contains("%")) {
                return parseIntOrDefault(accuracy.replace("%", ""), 100);
            }
            return parseIntOrDefault(accuracy, 100);
        } catch (Exception e) {
            return 100;
        }
    }
    
    private PokemonType parseType(String type) {
        try {
            return PokemonType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (type.equalsIgnoreCase("Typeless")) {
                return PokemonType.NORMAL; // Default to NORMAL for typeless moves
            }
            return PokemonType.NORMAL;
        }
    }
    
    private MoveCategory parseCategory(String category) {
        if (category.equalsIgnoreCase("Physical")) {
            return MoveCategory.PHYSICAL;
        } else if (category.equalsIgnoreCase("Special")) {
            return MoveCategory.SPECIAL;
        } else {
            return MoveCategory.STATUS;
        }
    }
    
    private StatusEffect parseStatusEffect(String effect) {
        if (effect.toLowerCase().contains("burn")) {
            return StatusEffect.BURN;
        } else if (effect.toLowerCase().contains("freeze")) {
            return StatusEffect.FREEZE;
        } else if (effect.toLowerCase().contains("paralyze") || effect.toLowerCase().contains("paralysis")) {
            return StatusEffect.PARALYSIS;
        } else if (effect.toLowerCase().contains("poison")) {
            return StatusEffect.POISON;
        } else if (effect.toLowerCase().contains("sleep")) {
            return StatusEffect.SLEEP;
        } else if (effect.toLowerCase().contains("confuse")) {
            return StatusEffect.CONFUSION;
        } else {
            return StatusEffect.NONE;
        }
    }
    
    private int parseStatusChance(String effect) {
        // Extract percentage from effect description
        // Example: "Has a 30% chance to paralyze the target."
        try {
            if (effect.contains("%")) {
                int index = effect.indexOf("%");
                int startIndex = effect.lastIndexOf(" ", index) + 1;
                String percentage = effect.substring(startIndex, index);
                return Integer.parseInt(percentage);
            }
        } catch (Exception e) {
            // If parsing fails, return default
        }
        
        // Default chances based on common move effects
        if (effect.toLowerCase().contains("burn") || 
            effect.toLowerCase().contains("freeze") || 
            effect.toLowerCase().contains("paralyze") || 
            effect.toLowerCase().contains("poison") || 
            effect.toLowerCase().contains("sleep") || 
            effect.toLowerCase().contains("confuse")) {
            return 10; // Default 10% chance if not specified
        }
        
        return 0;
    }
    
    public Move getMoveByName(String name) {
        return movesByName.get(name.toLowerCase());
    }
    
    public Map<String, Move> getAllMoves() {
        return new HashMap<>(movesByName);
    }

    public void clearMoves() {
        movesByName.clear();
    }

    public Move getMove(String name) {
        return movesByName.get(name.toLowerCase());
    }
}
