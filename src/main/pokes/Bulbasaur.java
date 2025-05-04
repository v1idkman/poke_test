package pokes;

import model.Stats;

public class Bulbasaur extends Pokemon {

    public Bulbasaur() {
        name = "Bulbasaur";
        dex = 1;
        id = (int)(Math.random() * 100000);
        isShiny = false; // change to odds later on
        stats = new Stats(dex, dex, dex, dex, id, dex);
    }
}
