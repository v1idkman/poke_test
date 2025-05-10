package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import exceptions.NoSuchWorldException;

public class WorldManager {
    private Map<String, Board> worlds = new HashMap<>();
    private String currentWorld;
    private JFrame window;
    
    public WorldManager(JFrame window) {
        this.window = window;
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
    
    // In the WorldManager's switchWorld method
    public void switchWorld(String worldName, Point spawnPoint) throws NoSuchWorldException {
        if (!worlds.containsKey(worldName)) {
            throw new NoSuchWorldException("World " + worldName + " does not exist.");
        }
        
        currentWorld = worldName;
        Board targetBoard = worlds.get(worldName);
        
        // Set player position to the provided spawn point
        targetBoard.getPlayer().setPosition(spawnPoint);
        
        // Update player's exact coordinates to match the new position
        targetBoard.getPlayer().updateExactCoordinates();
        
        window.getContentPane().removeAll();
        
        // Create a layered pane to hold both the game board and menu button
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(window.getWidth(), window.getHeight()));
        layeredPane.setBounds(0, 0, window.getWidth(), window.getHeight());
        
        // Determine if this is a small board based on row and column count
        boolean isSmallBoard = targetBoard.rows < 120 && targetBoard.columns < 180;
        
        if (isSmallBoard) {
            // This is a small board - center it in black background
            JPanel centeringPanel = new JPanel(new GridBagLayout());
            centeringPanel.setBackground(Color.BLACK);
            centeringPanel.setBounds(0, 0, window.getWidth(), window.getHeight());
            centeringPanel.add(targetBoard);
            
            layeredPane.add(centeringPanel, JLayeredPane.DEFAULT_LAYER);
        } else {
            // This is a large board - fill the window
            targetBoard.setBounds(0, 0, targetBoard.getPreferredSize().width, 
                                targetBoard.getPreferredSize().height);
            layeredPane.add(targetBoard, JLayeredPane.DEFAULT_LAYER);
        }
        
        // Position menu button consistently at bottom right of window
        Menu menu = Menu.getInstance();
        menu.positionMenuButtonForWindow(layeredPane, window.getWidth(), window.getHeight());
        
        window.add(layeredPane);
        window.addKeyListener(targetBoard);
        window.revalidate();
        window.repaint();
        
        // Update camera to center on player's new position
        if (targetBoard.getCamera() != null) {
            targetBoard.getCamera().update(targetBoard.getPlayer().getWorldX(), 
                                     targetBoard.getPlayer().getWorldY());
        }
        
        targetBoard.requestFocusInWindow();
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
