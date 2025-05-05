package pokes;

import model.Stats;

public class Charizard extends Pokemon {

    public Charizard() {
        name = "Charizard";
        dex = 6;
        id = (int)(Math.random() * 100000);
        isShiny = false; // change to odds later on
        stats = new Stats(dex, dex, dex, dex, dex, dex, dex); // placeholder values
        types.add(PokemonType.FIRE);
        types.add(PokemonType.FLYING);
    }
}
