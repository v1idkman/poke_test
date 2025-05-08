package ui;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import model.Door;
import model.Player;

public class WorldManager {
    private Map<String, Board> worlds = new HashMap<>();
    private String currentWorld;
    private JFrame window;
    
    public WorldManager(JFrame window, Player player) {
        this.window = window;
        
        // Create the outside world
        Board outsideWorld = new Board(player, "outside");
        worlds.put("outside", outsideWorld);
        
        // Create house interior
        Board houseInterior = new Board(player, "house_interior");
        worlds.put("house_interior", houseInterior);
        
        // Add doors to both worlds
        Point houseDoorPos = new Point(41, 41); // Position in front of house
        Door houseDoor = new Door(houseDoorPos, "/resources/player_sprites/s_facing_back.png", "house_interior", new Point(5, 8));
        outsideWorld.addDoor(houseDoor);
        outsideWorld.addDoor(houseDoor);
        
        Point exitDoorPos = new Point(5, 9); // Position inside house
        Door exitDoor = new Door(exitDoorPos, "/resources/player_sprites/s_facing_back.png", "outside", new Point(41, 42));
        houseInterior.addDoor(exitDoor);
        
        // Set initial world
        currentWorld = "outside";
    }
    
    public void switchWorld(String worldName, Point spawnPoint) {
        if (worlds.containsKey(worldName)) {
            currentWorld = worldName;
            Board targetBoard = worlds.get(worldName);
            
            targetBoard.getPlayer().setPosition(spawnPoint);
            
            window.getContentPane().removeAll();
            window.add(targetBoard);
            window.addKeyListener(targetBoard);
            window.revalidate();
            window.repaint();
            
            // Request focus for key events
            targetBoard.requestFocusInWindow();
        }
    }
    
    public Board getCurrentWorld() {
        return worlds.get(currentWorld);
    }

    public Map<String, Board> getWorlds() {
        return worlds;
    }
}
