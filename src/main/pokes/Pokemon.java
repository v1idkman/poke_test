package pokes;

import java.util.List;

import model.Item;
import model.Move;
import model.Stats;

public abstract class Pokemon {
    protected String name;
    protected int id;
    protected int dex;
    protected List<Move> moves;
    protected List<PokemonType> types; // Pokémon can have up to 2 types
    protected Stats stats;
    protected boolean isShiny;
    protected Item heldItem;

    public enum PokemonType {
        NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON, GROUND, 
        FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY;
        
        // Methods for type effectiveness could go here
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public int getDex() {
        return dex;
    }

    public boolean getIsShiny() {
        return isShiny;
    }

    public Item getHeldItem() {
        return heldItem;
    }

    public Stats getStats() {
        return stats;
    }
}
