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
import model.Berry;
import model.Door;
import model.InteractableObject;
import model.Player;
import model.TrainerNpc;
import model.Npc.Direction;
import pokes.Pokemon;
import pokes.PokemonFactory;

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
            // Create a defensive copy to prevent reference corruption
            Point safeSpawnPoint = new Point(spawnPoint.x, spawnPoint.y);
            player.setPosition(safeSpawnPoint);
            player.updateExactCoordinates();
            
            // Debug output to verify correct spawn point
            System.out.println("Teleporting player to: (" + safeSpawnPoint.x + ", " + safeSpawnPoint.y + ")");
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
            currentWorld.setBounds(0, 0, App.CURRENT_WIDTH, App.CURRENT_HEIGHT);
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

    public void initBoards(Player player) {
        // NOTE: resize buildings 150% x 175%
        createOutsideBoard(player);
        createInsideBoard(player);
    }

    public void createOutsideBoard(Player player) {
        Board outsideBoard = new Board(player, "outside", 30, 46);

        outsideBoard.addDoor(new Door(new Point(12, 7), "/resources/buildings/tree2.png", 
                            "house_interior", new Point(5, 8), Door.Direction.FRONT));
        
        // Door facing down (player must face down to enter) 
        outsideBoard.addDoor(new Door(new Point(19, 7), "/resources/buildings/tree2.png", 
                            "house_interior", new Point(5, 8), Door.Direction.FRONT));

        addBoard(outsideBoard);

        // objects
        outsideBoard.placeManyObjects("/resources/buildings/tree2.png", 0, 0, 4, 16, 2, 1);
        outsideBoard.placeManyObjects("/resources/buildings/tree2.png", 0, 21, 4, outsideBoard.getRows(), 2, 1);
        outsideBoard.placeManyObjects("/resources/buildings/tree2.png", 6, 0, outsideBoard.getColumns(), 4, 2, 1);
        outsideBoard.placeManyObjects("/resources/buildings/tree2.png", 6, outsideBoard.getRows() - 4, outsideBoard.getColumns(), 
                                        outsideBoard.getRows(), 2, 1);
        outsideBoard.placeManyObjects("/resources/buildings/tree2.png", outsideBoard.getColumns() - 6, 0, 
                                        outsideBoard.getColumns(), outsideBoard.getRows(), 2, 1);
        
        outsideBoard.addObject("/resources/buildings/green_roof_two_floor_house.png", 10, 4);
        outsideBoard.addObject("/resources/buildings/blue_roof_two_floor_house.png", 17, 4);
        outsideBoard.addObject("/resources/buildings/green_roof_one_floor_house.png", 23, 5);
        outsideBoard.addObject("/resources/buildings/green_fat_tree.png", 11, 11);
        outsideBoard.addObject("/resources/buildings/tiny_tree1.png", 8, 11);
        outsideBoard.addObject("/resources/buildings/tiny_tree1.png", 8, 12);

        outsideBoard.addBerryTree(Berry.BerryType.ORAN_BERRY, 4, 14, 15);

        // Create an NPC
        TrainerNpc bugCatcher = new TrainerNpc(new Point(20, 10), "Bug Catcher", "/resources/npc_sprites/bug_catcher/facing_back.png", 
                        Direction.BACK, outsideBoard, "Bug Catcher Class", true);
        bugCatcher.setDialogueText("I love catching bugs! Want to battle?");

        // Add Pokemon to the NPC
        Pokemon caterpie = PokemonFactory.createPokemon(10, 8, "Caterpie");
        Pokemon weedle = PokemonFactory.createPokemon(13, 9, "Weedle");
        bugCatcher.addPokemon(caterpie);
        bugCatcher.addPokemon(weedle);

        outsideBoard.addTrainer(bugCatcher);

        addBoard(outsideBoard);
    }

    public void createInsideBoard(Player player) {
        Board inside1 = new Board(player, "house_interior", 10, 15);
        inside1.addObject("/resources/buildings/marroon_single_bed.png", 10, 0);
        
        // Add doors
        inside1.addDoor(new Door(new Point(5, 5), "/resources/player_sprites/s_facing_back.png", 
                            "outside", new Point(10, 15), InteractableObject.Direction.ANY));
        inside1.addDoor(new Door(new Point(5, 8), "/resources/doors/exit_door.png", 
                            "outside", new Point(12, 8), InteractableObject.Direction.BACK));
        
        inside1.addPokeball("poke ball", 3, 2, 3);
        inside1.addMedicine("potion", 2, 7, 8);
        inside1.addKeyItem("good rod", 8, 5);
        
        inside1.addCustomItem("master ball", 1, 1, 1, "You found a rare Master Ball!");
            
        addBoard(inside1);
    }

    public void createOutside2Board() {
        
    }
}
