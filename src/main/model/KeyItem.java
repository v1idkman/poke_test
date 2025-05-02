package model;

public class KeyItem extends Item {
    private KeyItemType type;
    
    public enum KeyItemType {
        TOWN_MAP("Shows a map of the region"),
        BICYCLE("Allows faster movement"),
        FISHING_ROD("Used to catch water Pokémon"),
        OLD_ROD("A basic fishing rod"),
        GOOD_ROD("A better fishing rod"),
        SUPER_ROD("The best fishing rod");
        
        private final String description;
        
        KeyItemType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public KeyItem(KeyItemType type, String imagePath) {
        super(type.name(), type.getDescription(), imagePath);
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
