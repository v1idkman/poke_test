package pokes;

public class GenericPokemon extends Pokemon {
    public GenericPokemon(int dexNumber, int level, String name) {
        super(dexNumber, name, level);
        
        // Add types from CSV
        PokemonStatsLoader loader = PokemonStatsLoader.getInstance();
        String[] typeStrings = loader.getPokemonTypesByNameAndDex(dexNumber, name);
        
        // Add primary type (always exists)
        if (typeStrings[0] != null && !typeStrings[0].isEmpty()) {
            PokemonType primaryType = PokemonFactory.stringToType(typeStrings[0]);
            if (primaryType != null) {
                types.add(primaryType);
            }
        }
        
        // Add secondary type (if exists)
        if (typeStrings[1] != null && !typeStrings[1].isEmpty()) {
            PokemonType secondaryType = PokemonFactory.stringToType(typeStrings[1]);
            if (secondaryType != null) {
                types.add(secondaryType);
            }
        }
    }
}
