package moves;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LearnsetLoader {
    private static LearnsetLoader instance;
    private Map<String, Map<String, Integer>> learnsets = new HashMap<>();
    private Map<String, PokemonMoveData> pokemonMoveData = new HashMap<>();
    
    private LearnsetLoader() {
    }
    
    public static LearnsetLoader getInstance() {
        if (instance == null) {
            instance = new LearnsetLoader();
        }
        return instance;
    }
    
    public void loadFromTypeScriptFile(String resourcePath) {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
    
        if (inputStream == null) {
            inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        }
        
        if (inputStream == null) {
            System.err.println("Resource not found: " + resourcePath);
            loadDefaultLearnsets();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            parseTypeScriptLearnsetsGen7Only(reader);
            System.out.println("Loaded learnsets for " + learnsets.size() + " Pokemon (Gen 1-7 only)");
            
        } catch (IOException e) {
            System.err.println("Error loading learnsets: " + e.getMessage());
            loadDefaultLearnsets();
        }
    }
    
    private void parseTypeScriptLearnsetsGen7Only(BufferedReader reader) throws IOException {
        String line;
        String currentPokemon = null;
        StringBuilder learnsetContent = new StringBuilder();
        boolean inPokemonBlock = false;
        boolean inLearnsetBlock = false;
        int pokemonBraceDepth = 0;
        int learnsetBraceDepth = 0;
        
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            
            // Skip empty lines, comments, and export statement
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("//") || 
                trimmedLine.startsWith("export") || trimmedLine.equals("};")) {
                continue;
            }
            
            // Detect Pokemon name: word followed by colon and opening brace, but NOT "learnset:", "eventData:", or "encounters:"
            if (trimmedLine.matches("^\\w+:\\s*\\{.*") && 
                !trimmedLine.startsWith("learnset:") && 
                !trimmedLine.startsWith("eventData:") && 
                !trimmedLine.startsWith("encounters:")) {
                
                // Process previous Pokemon if exists
                if (currentPokemon != null && learnsetContent.length() > 0) {
                    processPokemonLearnsetGen7Only(currentPokemon, learnsetContent.toString());
                }
                
                // Start new Pokemon
                String[] parts = trimmedLine.split(":", 2);
                currentPokemon = parts[0].trim().toLowerCase();
                learnsetContent = new StringBuilder();
                inPokemonBlock = true;
                inLearnsetBlock = false;
                pokemonBraceDepth = countBraces(trimmedLine, '{') - countBraces(trimmedLine, '}');
                learnsetBraceDepth = 0;
                
                // System.out.println("Found Pokemon: " + currentPokemon);
                continue;
            }
            
            if (inPokemonBlock) {
                // Update Pokemon block brace depth
                pokemonBraceDepth += countBraces(trimmedLine, '{') - countBraces(trimmedLine, '}');
                
                // Detect learnset start
                if (trimmedLine.startsWith("learnset:") && trimmedLine.contains("{")) {
                    inLearnsetBlock = true;
                    learnsetBraceDepth = countBraces(trimmedLine, '{') - countBraces(trimmedLine, '}');
                    //System.out.println("  Starting learnset for: " + currentPokemon);
                    continue;
                }
                
                // Collect learnset moves (only Gen 1-7)
                if (inLearnsetBlock) {
                    learnsetBraceDepth += countBraces(trimmedLine, '{') - countBraces(trimmedLine, '}');
                    
                    // If this line contains move data, filter for Gen 1-7 only
                    if (trimmedLine.contains(":") && trimmedLine.contains("[") && 
                        !trimmedLine.contains("learnset:") && !trimmedLine.contains("eventData:") && 
                        !trimmedLine.contains("encounters:")) {
                        
                        String filteredLine = filterGen7Moves(trimmedLine);
                        if (!filteredLine.isEmpty()) {
                            learnsetContent.append(filteredLine).append("\n");
                        }
                    }
                    
                    // End of learnset block
                    if (learnsetBraceDepth <= 0) {
                        inLearnsetBlock = false;
                        // System.out.println("  Finished learnset for: " + currentPokemon);
                    }
                }
                
                // Skip eventData and encounters sections entirely
                if (trimmedLine.startsWith("eventData:") || trimmedLine.startsWith("encounters:")) {
                    // Skip these sections by not processing them
                    continue;
                }
                
                // End of Pokemon block
                if (pokemonBraceDepth <= 0) {
                    if (currentPokemon != null && learnsetContent.length() > 0) {
                        processPokemonLearnsetGen7Only(currentPokemon, learnsetContent.toString());
                    }
                    inPokemonBlock = false;
                    currentPokemon = null;
                    learnsetContent = new StringBuilder();
                }
            }
        }
        
        // Process final Pokemon
        if (currentPokemon != null && learnsetContent.length() > 0) {
            processPokemonLearnsetGen7Only(currentPokemon, learnsetContent.toString());
        }
        
        ensureMinimumMoves();
    }
    
    /**
     * Filter move entries to only include Generation 1-7 data
     */
    private String filterGen7Moves(String moveLine) {
        // Extract move name and data
        Pattern movePattern = Pattern.compile("(\\w+):\\s*\\[([^\\]]+)\\]");
        Matcher matcher = movePattern.matcher(moveLine);
        
        if (matcher.find()) {
            String moveName = matcher.group(1);
            String moveData = matcher.group(2);
            
            // Filter move data to only include Gen 1-7 entries
            String[] entries = moveData.split(",");
            List<String> gen7Entries = new ArrayList<>();
            
            for (String entry : entries) {
                entry = entry.trim().replaceAll("\"", "");
                
                // Check if entry is from Gen 1-7
                if (isGen7OrEarlier(entry)) {
                    gen7Entries.add("\"" + entry + "\"");
                }
            }
            
            // Only return the line if there are valid Gen 1-7 entries
            if (!gen7Entries.isEmpty()) {
                return moveName + ": [" + String.join(", ", gen7Entries) + "],";
            }
        }
        
        return "";
    }
    
    private boolean isGen7OrEarlier(String entry) {
        if (entry.length() == 0) return false;
        
        char firstChar = entry.charAt(0);
        
        // Handle numbered generations 1-7
        if (firstChar >= '1' && firstChar <= '7') {
            return true;
        }
        
        // Reject Gen 8+ explicitly
        if (firstChar >= '8' && firstChar <= '9') {
            return false;
        }
        
        // Handle legacy formats without generation prefix (assume early gen)
        if (entry.matches("^[LMTEVSD]\\d*")) {
            return true;
        }
        
        return false;
    }
    
    private int countBraces(String str, char brace) {
        return (int) str.chars().filter(c -> c == brace).count();
    }
    
    private void processPokemonLearnsetGen7Only(String pokemonName, String learnsetContent) {
        if (pokemonName.equals("beldum")) {
            System.out.println("Raw learnset data for Beldum:");
            System.out.println(learnsetContent);
            System.out.println("=== Processing each move ===");
        }
        
        PokemonMoveData moveData = parseMovesFromLearnsetSeparately(learnsetContent);
        
        if (pokemonName.equals("beldum")) {
            System.out.println("=== Final Results for Beldum ===");
            System.out.println("Level-up moves: " + moveData.getLevelUpMoves());
            System.out.println("Tutor moves: " + moveData.getTutorMoves());
            System.out.println("TM moves: " + moveData.getTmMoves());
            System.out.println("Event moves: " + moveData.getEventMoves());
        }
        
        if (moveData != null && !moveData.getAllMoves().isEmpty()) {
            pokemonMoveData.put(pokemonName, moveData);
            
            // Also maintain backward compatibility with the old system
            Map<String, Integer> levelUpOnly = moveData.getLevelUpMoves();
            if (!levelUpOnly.isEmpty()) {
                learnsets.put(pokemonName, levelUpOnly);
            }
            
            System.out.println("Successfully processed " + pokemonName + " with " + 
                             moveData.getLevelUpMoves().size() + " level-up moves, " +
                             moveData.getTmMoves().size() + " TM moves, " +
                             moveData.getTutorMoves().size() + " tutor moves, " +
                             moveData.getEggMoves().size() + " egg moves");
        }
    }
    
    private PokemonMoveData parseMovesFromLearnsetSeparately(String learnsetContent) {
        PokemonMoveData moveData = new PokemonMoveData();
        
        Pattern movePattern = Pattern.compile("(\\w+):\\s*\\[([^\\]]+)\\]");
        Matcher moveMatcher = movePattern.matcher(learnsetContent);
        
        while (moveMatcher.find()) {
            String moveName = moveMatcher.group(1).toLowerCase();
            String levelData = moveMatcher.group(2);
            
            categorizeMove(moveData, moveName, levelData);
        }
        
        return moveData;
    }
    
    private void categorizeMove(PokemonMoveData moveData, String moveName, String levelData) {
        String[] entries = levelData.replaceAll("\"", "").split(",");
        
        boolean hasLevelUp = false;
        boolean hasTutor = false;
        boolean hasTm = false;
        boolean hasEgg = false;
        boolean hasEvent = false;
        int earliestLevel = Integer.MAX_VALUE;
        
        for (String entry : entries) {
            entry = entry.trim();
            
            // Skip Gen 8+ entries
            if (!isGen7OrEarlier(entry)) {
                continue;
            }
            
            // Level-up moves (format: "1L5", "3L12")
            if (entry.matches("\\d+L\\d+")) {
                String[] parts = entry.split("L");
                if (parts.length == 2) {
                    try {
                        int level = Integer.parseInt(parts[1]);
                        earliestLevel = Math.min(earliestLevel, level);
                        hasLevelUp = true;
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            // TM/HM moves (format: "7M", etc.)
            else if (entry.matches("\\d+M")) {
                hasTm = true;
            }
            // Tutor moves (format: "7T", etc.)
            else if (entry.matches("\\d+T")) {
                hasTutor = true;
            }
            // Egg moves (format: "7E", etc.)
            else if (entry.matches("\\d+E")) {
                hasEgg = true;
            }
            // Event/Special moves (format: "6S0", "5D", etc.)
            else if (entry.matches("\\d+[VS]\\d*") || entry.matches("\\d+D")) {
                hasEvent = true;
            }
        }
        
        // PRIORITY SYSTEM: Add to only ONE category based on hierarchy
        // Priority: Level-up > TM > Tutor > Egg > Event
        if (hasLevelUp && earliestLevel != Integer.MAX_VALUE) {
            moveData.addLevelUpMove(moveName, earliestLevel);
        }
        else if (hasTm) {
            moveData.addTmMove(moveName);
        }
        else if (hasTutor) {
            moveData.addTutorMove(moveName);
        }
        else if (hasEgg) {
            moveData.addEggMove(moveName);
        }
        else if (hasEvent) {
            moveData.addEventMove(moveName);
        }
    }
    
    private void ensureMinimumMoves() {
        for (Map.Entry<String, Map<String, Integer>> entry : learnsets.entrySet()) {
            String pokemonName = entry.getKey();
            Map<String, Integer> moves = entry.getValue();
            
            // Check if Pokemon has any moves available at level 1
            boolean hasLevel1Move = moves.values().stream().anyMatch(level -> level <= 1);
            
            if (!hasLevel1Move && !moves.isEmpty()) {
                // Find the earliest move and make it available at level 1
                String earliestMove = moves.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
                
                if (earliestMove != null) {
                    moves.put(earliestMove, 1);
                }
            } else if (moves.isEmpty()) {
                // Add a default move if Pokemon has no moves
                addDefaultMoveForPokemon(pokemonName, moves);
            }
        }
    }
    
    private void addDefaultMoveForPokemon(String pokemonName, Map<String, Integer> moves) {
        // Add type-appropriate default moves based on Pokemon name patterns
        if (pokemonName.contains("char") || pokemonName.contains("fire")) {
            moves.put("ember", 1);
        } else if (pokemonName.contains("squir") || pokemonName.contains("water")) {
            moves.put("watergun", 1);
        } else if (pokemonName.contains("bulb") || pokemonName.contains("grass")) {
            moves.put("vinewhip", 1);
        } else if (pokemonName.contains("pika") || pokemonName.contains("electric")) {
            moves.put("thundershock", 1);
        } else {
            moves.put("tackle", 1);
        }
    }
    
    private void loadDefaultLearnsets() {
        String[] commonPokemon = {"bulbasaur", "charmander", "squirtle", "pikachu", "caterpie"};
        String[] defaultMoves = {"tackle", "growl", "vinewhip", "ember", "watergun"};
        
        for (int i = 0; i < commonPokemon.length; i++) {
            Map<String, Integer> moves = new HashMap<>();
            moves.put("tackle", 1);
            if (i < defaultMoves.length) {
                moves.put(defaultMoves[i], 1);
            }
            learnsets.put(commonPokemon[i], moves);
        }
    }
    
    public List<String> getAvailableMoves(String pokemonName, int level) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        List<String> available = new ArrayList<>();
        
        if (moveData != null) {
            // Only level-up moves are restricted by level
            for (Map.Entry<String, Integer> entry : moveData.getLevelUpMoves().entrySet()) {
                if (entry.getValue() <= level) {
                    available.add(entry.getKey());
                }
            }
            
            // TM, Tutor, and Egg moves can be learned anytime (if available)
            available.addAll(moveData.getTmMoves());
            available.addAll(moveData.getTutorMoves());
            available.addAll(moveData.getEggMoves());
        }
        
        if (available.isEmpty()) {
            available.add("tackle");
        }
        
        return available;
    }
    
    public List<String> getAvailableLevelUpMovesOnly(String pokemonName, int level) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        List<String> available = new ArrayList<>();
        
        if (moveData != null) {
            for (Map.Entry<String, Integer> entry : moveData.getLevelUpMoves().entrySet()) {
                if (entry.getValue() <= level) {
                    available.add(entry.getKey());
                }
            }
        }
        
        return available;
    }
    
    public List<String> getAllMovesForPokemon(String pokemonName) {
        Map<String, Integer> moves = learnsets.get(pokemonName.toLowerCase());
        if (moves != null) {
            return new ArrayList<>(moves.keySet());
        }
        return Arrays.asList("tackle");
    }
    
    public boolean canLearnMove(String pokemonName, String moveName) {
        Map<String, Integer> moves = learnsets.get(pokemonName.toLowerCase());
        return moves != null && moves.containsKey(moveName.toLowerCase());
    }
    
    public int getMoveLearnLevel(String pokemonName, String moveName) {
        Map<String, Integer> moves = learnsets.get(pokemonName.toLowerCase());
        if (moves != null) {
            return moves.getOrDefault(moveName.toLowerCase(), -1);
        }
        return -1;
    }

    /**
     * Print and return all moves that a specific Pokemon can learn with their levels
     */
    public Map<String, Integer> getMoves(String pokemonName) {
        String normalizedName = pokemonName.toLowerCase();
        Map<String, Integer> moves = learnsets.get(normalizedName);
        
        if (moves == null || moves.isEmpty()) {
            System.out.println("No learnset found for Pokemon: " + pokemonName);
            return new HashMap<>();
        }
        
        System.out.println("\n=== Learnset for " + pokemonName.toUpperCase() + " ===");
        System.out.println("Total moves: " + moves.size());
        System.out.println();
        
        // Sort moves by level, then alphabetically
        List<Map.Entry<String, Integer>> sortedMoves = moves.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue()
                    .thenComparing(Map.Entry.comparingByKey()))
            .collect(Collectors.toList());
        
        // Group moves by level for better readability
        Map<Integer, List<String>> movesByLevel = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : sortedMoves) {
            int level = entry.getValue();
            String moveName = entry.getKey();
            
            movesByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(moveName);
        }
        
        // Print moves grouped by level
        for (Map.Entry<Integer, List<String>> levelEntry : movesByLevel.entrySet()) {
            int level = levelEntry.getKey();
            List<String> levelMoves = levelEntry.getValue();
            
            System.out.println("Level " + level + ":");
            for (String move : levelMoves) {
                System.out.println("  - " + capitalizeMoveName(move));
            }
            System.out.println();
        }
        
        return new HashMap<>(moves);
    }

    /**
     * Helper method to capitalize move names properly
     */
    private String capitalizeMoveName(String moveName) {
        if (moveName == null || moveName.isEmpty()) {
            return moveName;
        }
        
        // Handle special cases for multi-word moves
        String[] specialCases = {
            "watergun", "Water Gun",
            "vinewhip", "Vine Whip", 
            "thundershock", "Thunder Shock",
            "stringshot", "String Shot",
            "doubleedge", "Double-Edge",
            "takedown", "Take Down"
        };
        
        for (int i = 0; i < specialCases.length; i += 2) {
            if (moveName.equals(specialCases[i])) {
                return specialCases[i + 1];
            }
        }
        
        // Default capitalization
        return moveName.substring(0, 1).toUpperCase() + moveName.substring(1);
    }

    /**
     * Get moves available at a specific level (convenience method)
     */
    public List<String> getMovesAtLevel(String pokemonName, int targetLevel) {
        Map<String, Integer> allMoves = getMoves(pokemonName);
        List<String> movesAtLevel = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : allMoves.entrySet()) {
            if (entry.getValue() == targetLevel) {
                movesAtLevel.add(entry.getKey());
            }
        }
        
        return movesAtLevel;
    }

    /**
     * Print a summary of Pokemon's learnset statistics
     */
    public void printLearnsetSummary(String pokemonName) {
        Map<String, Integer> moves = getMoves(pokemonName);
        
        if (moves.isEmpty()) {
            return;
        }
        
        int totalMoves = moves.size();
        int level1Moves = (int) moves.values().stream().filter(level -> level == 1).count();
        int earlyMoves = (int) moves.values().stream().filter(level -> level <= 10).count();
        int lateMoves = (int) moves.values().stream().filter(level -> level > 30).count();
        
        System.out.println("=== Learnset Summary for " + pokemonName.toUpperCase() + " ===");
        System.out.println("Total moves: " + totalMoves);
        System.out.println("Level 1 moves: " + level1Moves);
        System.out.println("Early moves (1-10): " + earlyMoves);
        System.out.println("Late moves (30+): " + lateMoves);
        System.out.println("Earliest move level: " + moves.values().stream().min(Integer::compareTo).orElse(0));
        System.out.println("Latest move level: " + moves.values().stream().max(Integer::compareTo).orElse(0));
    }

    public PokemonMoveData getPokemonMoveData(String pokemonName) {
        return pokemonMoveData.get(pokemonName.toLowerCase());
    }
    
    public List<String> getAvailableLevelUpMoves(String pokemonName, int level) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        List<String> available = new ArrayList<>();
        
        if (moveData != null) {
            for (Map.Entry<String, Integer> entry : moveData.getLevelUpMoves().entrySet()) {
                if (entry.getValue() <= level) {
                    available.add(entry.getKey());
                }
            }
        }
        
        return available;
    }
    
    public Set<String> getAllLearnableMoves(String pokemonName) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        return moveData != null ? moveData.getAllMoves() : new HashSet<>();
    }
    
    public Set<String> getTmMoves(String pokemonName) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        return moveData != null ? moveData.getTmMoves() : new HashSet<>();
    }
    
    public Set<String> getTutorMoves(String pokemonName) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        return moveData != null ? moveData.getTutorMoves() : new HashSet<>();
    }
    
    public void printComprehensiveLearnset(String pokemonName) {
        PokemonMoveData moveData = pokemonMoveData.get(pokemonName.toLowerCase());
        
        if (moveData == null) {
            System.out.println("No learnset found for Pokemon: " + pokemonName);
            return;
        }
        
        System.out.println("\n=== Comprehensive Learnset for " + pokemonName.toUpperCase() + " ===");
        
        // Level-up moves (restricted by level)
        System.out.println("\n** Level-up Moves (learned when leveling up) **");
        Map<Integer, List<String>> movesByLevel = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : moveData.getLevelUpMoves().entrySet()) {
            movesByLevel.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        
        for (Map.Entry<Integer, List<String>> levelEntry : movesByLevel.entrySet()) {
            System.out.println("Level " + levelEntry.getKey() + ": " + 
                             String.join(", ", levelEntry.getValue()));
        }
        
        // TM moves (can be learned anytime with TM)
        if (!moveData.getTmMoves().isEmpty()) {
            System.out.println("\n** TM/HM Moves (can be learned anytime with TM/HM) **");
            System.out.println(String.join(", ", moveData.getTmMoves()));
        }
        
        // Tutor moves (can be learned anytime from move tutor)
        if (!moveData.getTutorMoves().isEmpty()) {
            System.out.println("\n** Tutor Moves (can be learned anytime from move tutor) **");
            System.out.println(String.join(", ", moveData.getTutorMoves()));
        }
        
        // Egg moves (inherited from breeding)
        if (!moveData.getEggMoves().isEmpty()) {
            System.out.println("\n** Egg Moves (inherited from breeding) **");
            System.out.println(String.join(", ", moveData.getEggMoves()));
        }
        
        // Event moves
        if (!moveData.getEventMoves().isEmpty()) {
            System.out.println("\n** Event Moves (from special events) **");
            System.out.println(String.join(", ", moveData.getEventMoves()));
        }
        
        System.out.println("\nTotal learnable moves: " + moveData.getAllMoves().size());
        System.out.println("Level-up only: " + moveData.getLevelUpMoves().size());
        System.out.println("Available anytime: " + (moveData.getTmMoves().size() + 
                                                    moveData.getTutorMoves().size() + 
                                                    moveData.getEggMoves().size()));
    }
    
}