package ui;

import pokes.Pokemon;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class PokemonView {
    private final Pokemon pokemon;
    private Image normalIcon;
    private Image shinyIcon;
    private Image normalBack;
    private Image shinyBack;

    public PokemonView(Pokemon pokemon) {
        this.pokemon = pokemon;
        loadImage();
    }

    public void loadImage() {
        String normalIconPath, shinyIconPath, normalBackPath, shinyBackPath;
        normalIconPath = "sprites/sprites/pokemon/" + pokemon.getDex() + ".png";
        shinyIconPath = "sprites/sprites/pokemon/shiny/" + pokemon.getDex() + ".png";
        normalBackPath = "sprites/sprites/pokemon/back/" + pokemon.getDex() + ".png";
        shinyBackPath = "sprites/sprites/pokemon/back/shiny/" + pokemon.getDex() + ".png";

        try {
            normalIcon = ImageIO.read(new File(normalIconPath));
            shinyIcon = ImageIO.read(new File(shinyIconPath));
            normalBack = ImageIO.read(new File(normalBackPath));
            shinyBack = ImageIO.read(new File(shinyBackPath));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
            normalIcon = null;
            shinyIcon = null;
            normalBack = null;
            shinyBack = null;
        }
    }

    public void draw(Graphics g, JPanel panel, int x, int y, int width, int height, boolean battleView, boolean isShiny) {
        if (normalIcon == null) {
            // Draw a placeholder or return
            return;
        }
        Image imageToDraw = null;
        if (battleView) {
            imageToDraw = pokemon.getIsShiny() ? shinyBack : normalBack;
        } else {
            imageToDraw = pokemon.getIsShiny() ? shinyIcon : normalIcon;
        }
        
        if (imageToDraw != null) {
            g.drawImage(imageToDraw, x, y, width, height, panel);
        }
    }
    }
