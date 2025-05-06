package pokes;

import java.util.ArrayList;
import java.util.List;

import model.Item;
import model.Move;
import model.Move.StatusEffect;
import model.Stats;

public abstract class Pokemon {
    protected String name;
    protected int id;
    protected int dex;
    protected List<Move> moves = new ArrayList<>();
    protected List<PokemonType> types = new ArrayList<>();
    protected Stats stats;
    protected boolean isShiny;
    protected Item heldItem;
    protected StatusEffect status;
    protected StatusEffect previousStatus;

    public enum PokemonType {
        NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON, GROUND, 
        FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY;
        
        // Methods for type effectiveness could go here
    }

    protected Pokemon(int dexNumber, String name, int level) {
        this.name = name;
        this.dex = dexNumber;
        this.id = (int)(Math.random() * 100000);
        this.isShiny = Math.random() < 0.0122; // Shiny chance
        
        // Get base stats from the loader using both dex and name
        int[] baseStats = PokemonStatsLoader.getInstance().getBaseStats(dexNumber, name);
        if (baseStats != null) {
            this.stats = new Stats(
                baseStats[0], // HP
                baseStats[1], // Attack
                baseStats[2], // Defense
                baseStats[3], // Speed
                baseStats[4], // Sp. Attack
                baseStats[5], // Sp. Defense
                level
            );
        } else {
            // Fallback if stats not found
            this.stats = new Stats(50, 50, 50, 50, 50, 50, level);
        }
    }
    

    public List<PokemonType> getTypes() {
        return types;
    }

    public boolean holdsItem() {
        if (heldItem != null) {
            return true;
        } else {
            return false;
        }
    }

    public void holdItem(Item item) {
        heldItem = item;
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

    public void addMove(Move move) {
        if (moves.size() < 4) {
            moves.add(move);
        }
    }

    /**
     * Applies a status condition to this Pokémon
     * @param statusEffect The status condition to apply
     * @return true if the status was successfully applied, false otherwise
     */
    public boolean applyStatus(StatusEffect statusEffect) {
        // If the Pokémon already has this status, don't apply it again
        if (this.status == statusEffect) {
            return false;
        }
        
        // Check for immunity based on Pokémon type
        for (PokemonType type : this.types) {
            // Type-based immunities (following Pokémon game rules)
            if ((statusEffect == StatusEffect.POISON && type == PokemonType.POISON) ||
                (statusEffect == StatusEffect.POISON && type == PokemonType.STEEL) ||
                (statusEffect == StatusEffect.PARALYSIS && type == PokemonType.ELECTRIC) ||
                (statusEffect == StatusEffect.BURN && type == PokemonType.FIRE) ||
                (statusEffect == StatusEffect.FREEZE && type == PokemonType.ICE)) {
                return false; // Immune to this status
            }
        }
        
        // A Pokémon can only have one non-volatile status at a time
        // (BURN, FREEZE, PARALYSIS, POISON, SLEEP are non-volatile)
        if (hasNonVolatileStatus() && 
            (statusEffect == StatusEffect.BURN || 
            statusEffect == StatusEffect.FREEZE || 
            statusEffect == StatusEffect.PARALYSIS || 
            statusEffect == StatusEffect.POISON || 
            statusEffect == StatusEffect.SLEEP)) {
            return false;
        }
        
        // Apply the status effect
        this.status = statusEffect;
        
        // Apply stat modifications based on status
        switch (statusEffect) {
            case BURN:
                // Burn halves Attack
                stats.modifyStat("attack", -2);
                break;
            case PARALYSIS:
                // Paralysis quarters Speed
                stats.modifyStat("speed", -4);
                break;
            case POISON:
            case SLEEP:
            case FREEZE:
            case CONFUSION:
                // These don't modify stats directly
                break;
            case NONE:
                // Clear all status-related stat modifications
                if (previousStatus == StatusEffect.BURN) {
                    stats.modifyStat("attack", 2); // Restore Attack
                } else if (previousStatus == StatusEffect.PARALYSIS) {
                    stats.modifyStat("speed", 4); // Restore Speed
                }
                break;
        }
        return true;
    }

    /**
     * Checks if the Pokémon has any non-volatile status condition
     * @return true if the Pokémon has a non-volatile status
     */
    private boolean hasNonVolatileStatus() {
        return status == StatusEffect.BURN || 
            status == StatusEffect.FREEZE || 
            status == StatusEffect.PARALYSIS || 
            status == StatusEffect.POISON || 
            status == StatusEffect.SLEEP;
    }

    /**
     * Cures the Pokémon of its current status condition
     */
    public void cureStatus() {
        // Store the status before clearing it
        StatusEffect oldStatus = this.status;
        
        // Clear the status
        this.status = StatusEffect.NONE;
        
        // Reverse stat modifications
        if (oldStatus == StatusEffect.BURN) {
            stats.modifyStat("attack", 2); // Restore Attack
        } else if (oldStatus == StatusEffect.PARALYSIS) {
            stats.modifyStat("speed", 4); // Restore Speed
        }
    }

}
