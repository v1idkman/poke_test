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
import model.Player;

public class WorldManager {
    private Map<String, Board> worlds = new HashMap<>();
    private Board currentWorld;
    private JFrame window;
    private Camera camera;
    
    public WorldManager(JFrame window) {
        this.window = window;
        camera = Camera.getInstance();
    }

    public void addBoard(Board board) {
        if (!worlds.containsKey(board.getWorldName())) {
            worlds.put(board.getWorldName(), board);
            
            // Set the first added board as current world if not set yet
            if (currentWorld == null) {
                currentWorld = board;
            }
        }
    }

    public void removeBoard(String name) {
        if (worlds.containsKey(name)) {
            worlds.remove(name);
        }
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public void switchWorld(String worldName, Point spawnPoint) throws NoSuchWorldException {
        if (!worlds.containsKey(worldName)) {
            throw new NoSuchWorldException(worldName);
        }
        
        // Get the new world
        Board newWorld = worlds.get(worldName);
        
        // Set player position in the new world
        Player player = currentWorld.getPlayer();
        if (spawnPoint != null) {
            player.setPosition(spawnPoint);
            player.updateExactCoordinates();
        }
        
        // Update the current world
        currentWorld = newWorld;
        
        // Update camera with new world information
        camera.setWorldDimensions(
            currentWorld.columns * Board.TILE_SIZE,
            currentWorld.rows * Board.TILE_SIZE
        );
        camera.setActive(currentWorld.isLarge());
        
        // Update camera position with player
        camera.update(player);
        
        // Update the window content
        updateWindowContent();
    }

    public Board getCurrentWorld() {
        return currentWorld;
    }

    public Map<String, Board> getWorlds() {
        return worlds;
    }

    public void setCurrentWorld(String worldName) {
        if (worlds.containsKey(worldName)) {
            currentWorld = worlds.get(worldName);
            if (currentWorld.isLarge()) {
                camera.setActive(true);
            } else {
                camera.setActive(false);
            }
        }
    }
    
    private void updateWindowContent() {
        // Remove all components
        window.getContentPane().removeAll();
        
        // Create new layered pane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(window.getWidth(), window.getHeight()));
        layeredPane.setBounds(0, 0, window.getWidth(), window.getHeight());
        
        if (!currentWorld.isLarge()) {
            // Small board - center it in black background
            JPanel centeringPanel = new JPanel(new GridBagLayout());
            centeringPanel.setBackground(Color.BLACK);
            centeringPanel.setBounds(0, 0, window.getWidth(), window.getHeight());
            centeringPanel.add(currentWorld);
            
            layeredPane.add(centeringPanel, JLayeredPane.DEFAULT_LAYER);
        } else {
            // Large board - fit it to the fixed dimensions
            currentWorld.setBounds(0, 0, App.FIXED_WIDTH, App.FIXED_HEIGHT);
            layeredPane.add(currentWorld, JLayeredPane.DEFAULT_LAYER);
        }
        
        // Position menu button
        Menu menu = Menu.getInstance();
        menu.positionMenuButtonForWindow(layeredPane, window.getWidth() - 40, window.getHeight() - 40);
        
        // Add layered pane to window
        window.add(layeredPane);
        window.addKeyListener(currentWorld);
        
        // Request focus and repaint
        currentWorld.requestFocusInWindow();
        window.revalidate();
        window.repaint();
    }
}
