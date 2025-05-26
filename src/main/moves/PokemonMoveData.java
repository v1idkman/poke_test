package moves;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class PokemonMoveData {
    private Map<String, Integer> levelUpMoves;
    private Set<String> tmMoves;
    private Set<String> tutorMoves;
    private Set<String> eggMoves;
    private Set<String> eventMoves;
    
    public PokemonMoveData() {
        this.levelUpMoves = new HashMap<>();
        this.tmMoves = new HashSet<>();
        this.tutorMoves = new HashSet<>();
        this.eggMoves = new HashSet<>();
        this.eventMoves = new HashSet<>();
    }
    
    // Getters and setters
    public Map<String, Integer> getLevelUpMoves() { return levelUpMoves; }
    public Set<String> getTmMoves() { return tmMoves; }
    public Set<String> getTutorMoves() { return tutorMoves; }
    public Set<String> getEggMoves() { return eggMoves; }
    public Set<String> getEventMoves() { return eventMoves; }
    
    public void addLevelUpMove(String move, int level) {
        levelUpMoves.put(move, level);
    }
    
    public void addTmMove(String move) { tmMoves.add(move); }
    public void addTutorMove(String move) { tutorMoves.add(move); }
    public void addEggMove(String move) { eggMoves.add(move); }
    public void addEventMove(String move) { eventMoves.add(move); }
    
    public Set<String> getAllMoves() {
        Set<String> allMoves = new HashSet<>();
        allMoves.addAll(levelUpMoves.keySet());
        allMoves.addAll(tmMoves);
        allMoves.addAll(tutorMoves);
        allMoves.addAll(eggMoves);
        allMoves.addAll(eventMoves);
        return allMoves;
    }
}
