package pokes;

import model.Medicine;
import model.Stats;
import model.Medicine.MedicineType;

public class Charizard extends Pokemon {

    public Charizard() {
        name = "Charizard";
        dex = 6;
        id = (int)(Math.random() * 100000);
        isShiny = false; // change to odds later on
        stats = new Stats(dex, dex, dex, dex, id, dex); // placeholder values
        heldItem = new Medicine(MedicineType.POTION, "/resources/items/potion.png");
    }
}
