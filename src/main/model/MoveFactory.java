package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pokes.Pokemon.PokemonType;

public class MoveFactory {
    private static final Random random = new Random();
    
    public static Move createMove(String name) {
        return MoveLoader.getInstance().getMoveByName(name);
    }
    
    public static List<Move> getRandomMoves(int count) {
        List<Move> moves = new ArrayList<>();
        Map<String, Move> allMoves = MoveLoader.getInstance().getAllMoves();
        
        if (allMoves.isEmpty()) {
            return moves;
        }
        
        // Get a list of all move names
        List<String> moveNames = new ArrayList<>(allMoves.keySet());
        
        // Select random moves
        for (int i = 0; i < count && !moveNames.isEmpty(); i++) {
            int randomIndex = random.nextInt(moveNames.size());
            String moveName = moveNames.get(randomIndex);
            moves.add(allMoves.get(moveName));
            moveNames.remove(randomIndex); // Ensure no duplicates
        }
        
        return moves;
    }
    
    public static List<Move> getRandomMovesByType(PokemonType type, int count) {
        List<Move> moves = new ArrayList<>();
        Map<String, Move> allMoves = MoveLoader.getInstance().getAllMoves();
        
        if (allMoves.isEmpty()) {
            return moves;
        }
        
        // Filter moves by type
        List<Move> typeMoves = new ArrayList<>();
        for (Move move : allMoves.values()) {
            if (move.getType() == type) {
                typeMoves.add(move);
            }
        }
        
        // If not enough type moves, add some random moves
        if (typeMoves.size() < count) {
            moves.addAll(typeMoves);
            List<Move> additionalMoves = getRandomMoves(count - typeMoves.size());
            for (Move move : additionalMoves) {
                if (!moves.contains(move)) {
                    moves.add(move);
                }
            }
        } else {
            // Select random moves of the specified type
            for (int i = 0; i < count && !typeMoves.isEmpty(); i++) {
                int randomIndex = random.nextInt(typeMoves.size());
                moves.add(typeMoves.get(randomIndex));
                typeMoves.remove(randomIndex); // Ensure no duplicates
            }
        }
        
        return moves;
    }
    
    // Add default moves for Pokémon that don't have any
    public static List<Move> getDefaultMovesForType(PokemonType type) {
        List<Move> defaultMoves = new ArrayList<>();
        
        // Add a basic move based on type
        switch (type) {
            case FIRE:
                defaultMoves.add(createMove("Ember"));
                break;
            case WATER:
                defaultMoves.add(createMove("Water Gun"));
                break;
            case GRASS:
                defaultMoves.add(createMove("Vine Whip"));
                break;
            case ELECTRIC:
                defaultMoves.add(createMove("ThunderShock"));
                break;
            default:
                defaultMoves.add(createMove("Tackle"));
                break;
        }
        
        // Add a common normal-type move as backup
        if (defaultMoves.get(0) == null) {
            defaultMoves.clear();
            defaultMoves.add(new Move("Tackle", PokemonType.NORMAL, 40, 100, 35, Move.MoveCategory.PHYSICAL));
        }
        
        return defaultMoves;
    }
}
