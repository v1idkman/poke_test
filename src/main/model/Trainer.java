package model;

import java.util.List;
import java.util.ArrayList;

// abstract class of a trainer
public abstract class Trainer {
    private String name;
    private List<Pokemon> pokes;
    private Inventory inventory;
    
    public Trainer() {
        pokes = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Pokemon> getPokes() {
        return pokes;
    }

    public Inventory getInv() {
        return inventory;
    }
}
