package pokes;

import java.util.HashMap;
import java.util.Map;
import pokes.Pokemon.PokemonType;

public class PokemonFactory {
    private static Map<String, PokemonType> typeMap = new HashMap<>();
    
    static {
        // Initialize type mapping
        for (PokemonType type : PokemonType.values()) {
            typeMap.put(type.name(), type);
        }
    }
    
    public static Pokemon createPokemon(int dexNumber, int level, String name) {
        // If name is null, look it up from the PokemonStatsLoader
        if (name == null || name.isEmpty()) {
            name = PokemonStatsLoader.getInstance().getPokemonName(dexNumber);
            
            // If still null (maybe the Pokémon doesn't exist in the database),
            // use a default name based on dex number
            if (name == null || name.isEmpty()) {
                name = "Pokemon #" + dexNumber;
            }
        }
        
        return new GenericPokemon(dexNumber, level, name);
    }
    
    public static PokemonType stringToType(String typeString) {
        if (typeString == null || typeString.isEmpty()) {
            return null;
        }
        
        // Convert to uppercase and trim any whitespace
        String normalizedType = typeString.toUpperCase().trim();
        
        // Handle special cases with spaces (like "Mr. Mime")
        if (normalizedType.contains(" ")) {
            normalizedType = normalizedType.replace(" ", "_");
        }
        
        // Try to get the type, return NORMAL if not found
        PokemonType type = typeMap.get(normalizedType);
        if (type == null) {
            System.out.println("Unknown type: " + typeString);
            return PokemonType.NORMAL; // Default to NORMAL if type not found
        }
        
        return type;
    }    
}
