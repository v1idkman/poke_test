package ui;

import pokes.Pokemon;

import java.awt.*;

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
        normalIconPath = String.format("/resources/pokemon/icon/%s.png", pokemon.getDex());
        shinyIconPath = String.format("/resources/pokemon/shiny/%s.png", pokemon.getDex());
        normalBackPath = String.format("/resources/pokemon/back/%s.png", pokemon.getDex());
        shinyBackPath = String.format("/resources/pokemon/shiny_back/%s.png", pokemon.getDex());

        try {
            normalIcon = ImageIO.read(getClass().getResource(normalIconPath));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening normal icon: " + exc.getMessage());
            normalIcon = null;
        }

        try {
            shinyIcon = ImageIO.read(getClass().getResource(shinyIconPath));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening shiny icon: " + exc.getMessage());
            shinyIcon = null;
        }

        try {
            normalBack = ImageIO.read(getClass().getResource(normalBackPath));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening shiny icon: " + exc.getMessage());
            normalBack = null;
        }

        try {
            shinyBack = ImageIO.read(getClass().getResource(shinyBackPath));
        } catch (IOException | IllegalArgumentException exc) {
            System.out.println("Error opening shiny icon: " + exc.getMessage());
            shinyBack = null;
        }
    }

    public void draw(Graphics g, JPanel panel, int x, int y, int width, int height, boolean battleView) {
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
