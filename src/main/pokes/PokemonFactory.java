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
        // If name is null or empty, look it up from the PokemonStatsLoader
        if (name == null || name.isEmpty()) {
            name = PokemonStatsLoader.getInstance().getPokemonName(dexNumber);
        }
        
        // If still null or problematic, use a fallback
        if (name == null || name.isEmpty() || name.equals("Unknown")) {
            name = "Pokemon #" + dexNumber;
        }
        
        return new GenericPokemon(dexNumber, level, name);
    }
    
    public static PokemonType stringToType(String typeString) {
        if (typeString == null || typeString.isEmpty() || "NULL".equals(typeString)) {
            return null;
        }
        
        // Convert to uppercase and trim any whitespace, remove quotes
        String normalizedType = typeString.replace("\"", "").toUpperCase().trim();
        
        // Handle special cases and normalize type names from CSV
        switch (normalizedType) {
            case "NORMAL":
                return PokemonType.NORMAL;
            case "FIRE":
                return PokemonType.FIRE;
            case "WATER":
                return PokemonType.WATER;
            case "ELECTRIC":
                return PokemonType.ELECTRIC;
            case "GRASS":
                return PokemonType.GRASS;
            case "ICE":
                return PokemonType.ICE;
            case "FIGHTING":
                return PokemonType.FIGHTING;
            case "POISON":
                return PokemonType.POISON;
            case "GROUND":
                return PokemonType.GROUND;
            case "FLYING":
                return PokemonType.FLYING;
            case "PSYCHIC":
                return PokemonType.PSYCHIC;
            case "BUG":
                return PokemonType.BUG;
            case "ROCK":
                return PokemonType.ROCK;
            case "GHOST":
                return PokemonType.GHOST;
            case "DRAGON":
                return PokemonType.DRAGON;
            case "DARK":
                return PokemonType.DARK;
            case "STEEL":
                return PokemonType.STEEL;
            case "FAIRY":
                return PokemonType.FAIRY;
            default:
                System.out.println("Unknown type: " + typeString + " - defaulting to NORMAL");
                return PokemonType.NORMAL;
        }
    }
    
}
