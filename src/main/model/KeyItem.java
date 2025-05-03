package model;

public class KeyItem extends Item {
    private KeyItemType type;
    
    public enum KeyItemType {
        TOWN_MAP("Town Map", "Shows a map of the region"),
        BICYCLE("Bicycle", "Allows faster movement"),
        FISHING_ROD("Fishing Rod", "Used to catch water Pokémon"),
        OLD_ROD("Old Rod", "A basic fishing rod"),
        GOOD_ROD("Good Rod", "A better fishing rod"),
        SUPER_ROD("Super Rod", "The best fishing rod");
        
        private final String description;
        private final String name;
        
        KeyItemType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }
    }
    
    public KeyItem(KeyItemType type, String imagePath) {
        super(type.getName(), type.getDescription(), imagePath);
        this.type = type;
        this.stackable = false;
    }
    
    @Override
    public boolean use(Player player) {
        // Implementation for using key items
        System.out.println("Using key item: " + name);
        
        // Key items aren't consumed when used
        return true;
    }
    
    public KeyItemType getType() {
        return type;
    }
}
