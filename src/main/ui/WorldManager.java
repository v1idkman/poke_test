package ui;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Door;
import model.Player;

public class WorldManager {
    private Map<String, Board> worlds = new HashMap<>();
    private String currentWorld;
    private JFrame window;
    private JPanel mainPanel;
    private Player player;
    
    public WorldManager(JFrame window, Player player) {
        this.window = window;
        this.player = player;
        
        // Create the outside world
        Board outsideWorld = new Board(player, "outside");
        worlds.put("outside", outsideWorld);
        
        // Create house interior
        Board houseInterior = new Board(player, "house_interior");
        Point exitDoorPos = new Point(40, 40);
        Door exitDoor = new Door(exitDoorPos, "/resources/player_sprites/s_facing_back.png", 
                            "outside", new Point(80, 80));
        houseInterior.addDoor(exitDoor);
        worlds.put("house_interior", houseInterior);
        
        // Set initial world
        currentWorld = "outside";
    }

    public void addBoard(Board board, String name) {
        if (!worlds.containsKey(name)) {
            worlds.put(name, board);
        }
    }

    public void removeBoard(String name) {
        if (worlds.containsKey(name)) {
            worlds.remove(name);
        }
    }
    
    public void switchWorld(String worldName, Point spawnPoint) {
        if (!worlds.containsKey(worldName)) {
            loadWorld(worldName);
        }
        if (worlds.containsKey(worldName)) {
            currentWorld = worldName;
            Board targetBoard = worlds.get(worldName);
            
            targetBoard.getPlayer().setPosition(spawnPoint);
            
            window.getContentPane().removeAll();
            window.add(targetBoard);
            window.addKeyListener(targetBoard);
            window.revalidate();
            window.repaint();
            
            targetBoard.requestFocusInWindow();
        }
    }

    private void loadWorld(String worldName) {
        // Load world data from files or create dynamically
        Board newWorld = new Board(player, worldName);
        worlds.put(worldName, newWorld);
        mainPanel.add(newWorld, worldName);
    }
    
    public Board getCurrentWorld() {
        return worlds.get(currentWorld);
    }

    public Map<String, Board> getWorlds() {
        return worlds;
    }
}
