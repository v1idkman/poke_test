package model;

public class ItemFactory {
    public static Item createItem(String itemName) {
        // Normalize the item name for lookup
        String normalizedName = itemName.toLowerCase().replace(" ", "-");
        
        for (Medicine.MedicineType type : Medicine.MedicineType.values()) {
            String enumName = type.getName().replace(" ", "-");
            if (normalizedName.trim().equalsIgnoreCase(enumName.trim())) {
                return new Medicine(type);
            }
        }
        
        for (Pokeball.PokeBallType type : Pokeball.PokeBallType.values()) {
            String enumName = type.getName().replace(" ", "-");
            if (normalizedName.trim().equalsIgnoreCase(enumName.trim())) {
                return new Pokeball(type);
            }
        }
        
        for (KeyItem.KeyItemType type : KeyItem.KeyItemType.values()) {
            String enumName = type.getName().replace(" ", "-");
            if (normalizedName.trim().equalsIgnoreCase(enumName.trim())) {
                return new KeyItem(type);
            }
        }
        
        // If no match found, log error and return null
        System.err.println("Unknown item: " + itemName);
        return null;
    }
}
