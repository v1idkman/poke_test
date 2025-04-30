package model;

import java.util.List;

public abstract class Pokemon {
    protected String name;
    protected int id;
    protected List<Move> moves;
    protected Stats stats;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Move> getMoves() {
        return moves;
    }
}
