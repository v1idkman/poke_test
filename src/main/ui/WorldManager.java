package ui;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import model.Player;

public class WorldManager {
    private Map<String, Board> worlds = new HashMap<>();
    private String currentWorld;
    private JFrame window;
    private Player player;
    
    public WorldManager(JFrame window, Player player) {
        this.window = window;
        this.player = player;
    }

    public void addBoard(Board board) {
        if (!worlds.containsKey(board.getWorldName())) {
            worlds.put(board.getWorldName(), board);
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
        window.add(newWorld, worldName);
    }
    
    public Board getCurrentWorld() {
        return worlds.get(currentWorld);
    }

    public Map<String, Board> getWorlds() {
        return worlds;
    }

    public void setCurrentWorld(String worldName) {
        if (worlds.containsKey(worldName)) {
            currentWorld = worldName;
        }
    }
}
