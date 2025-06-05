package model;

import javax.swing.ImageIcon;

public class Berry extends Item {
    private BerryType type;
    
    public enum BerryType {
        ORAN_BERRY("Oran Berry", "Restores 10 HP when HP is below 50%"),
        SITRUS_BERRY("Sitrus Berry", "Restores 25% of max HP"),
        PECHA_BERRY("Pecha Berry", "Cures poison status"),
        CHERI_BERRY("Cheri Berry", "Cures paralysis"),
        RAWST_BERRY("Rawst Berry", "Cures burn status");
        
        private final String name;
        private final String description;
        
        BerryType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    public Berry(BerryType type) {
        super(type.getName(), type.getDescription());
        this.type = type;
        this.stackable = true;
        loadImage();
    }
    
    @Override
    public void loadImage() {
        String lookupName = type.getName().replace(" ", "-").toLowerCase();
        String imagePath = "sprites/sprites/items/berries/" + lookupName + ".png";
        try {
            java.io.File file = new java.io.File(imagePath);
            if (file.exists()) {
                image = new ImageIcon(file.getAbsolutePath()).getImage();
            } else {
                System.err.println("Berry image not found: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading berry image: " + e.getMessage());
        }
    }
    
    @Override
    public boolean use(Player player) {
        // Berry usage logic would go here
        return true;
    }
    
    public BerryType getType() { return type; }
}