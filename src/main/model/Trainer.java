package model;

import java.util.List;

import pokes.Pokemon;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// abstract class of a trainer
public abstract class Trainer {
    protected String name;
    protected List<Pokemon> pokes;
    protected List<Pokemon> currentTeam;
    protected BufferedImage image;
    protected Point pos;
    protected String currentSprite;
    protected int width;
    protected int height;
    
    public Trainer(String name) {
        currentSprite = "NEEDS TO BE SET INDIVIDUALLY";
        this.name = name;
        pokes = new ArrayList<>();
        currentTeam = new ArrayList<>();
    }

    public void setSprite(String filePath) {
        currentSprite = filePath;
    }

    public String getSpriteLocation() {
        return currentSprite;
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

    public void addPokemonToCurrentTeam(Pokemon pokemon) {
        if (currentTeam.size() < 6) {
            currentTeam.add(pokemon);
        } else {
            // add to pc
        }
    }

    public List<Pokemon> getTeam() {
        return currentTeam;
    }
}
