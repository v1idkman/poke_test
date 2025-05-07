package ui;

import javax.swing.*;
import java.awt.*;

import pokes.Pokemon;
import pokes.Pokemon.PokemonType;

public class UIComponentFactory {
    
    public static Color getColorForType(PokemonType type) {
        switch (type) {
            case NORMAL: return new Color(168, 168, 120);
            case FIRE: return new Color(240, 128, 48);
            case WATER: return new Color(104, 144, 240);
            case GRASS: return new Color(120, 200, 80);
            case ELECTRIC: return new Color(248, 208, 48);
            case ICE: return new Color(152, 216, 216);
            case FIGHTING: return new Color(192, 48, 40);
            case POISON: return new Color(160, 64, 160);
            case GROUND: return new Color(224, 192, 104);
            case FLYING: return new Color(168, 144, 240);
            case PSYCHIC: return new Color(248, 88, 136);
            case BUG: return new Color(168, 184, 32);
            case ROCK: return new Color(184, 160, 56);
            case GHOST: return new Color(112, 88, 152);
            case DRAGON: return new Color(112, 56, 248);
            default: return Color.GRAY;
        }
    }
    
    public static void drawCombinedStatHexagon(Graphics g, Pokemon pokemon) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = g.getClipBounds().width / 2;
        int centerY = g.getClipBounds().height / 2;
        int radius = Math.min(centerX, centerY) - 30;
        
        // Get the IV stats (normalized to 0.0-1.0 scale)
        double hpIVRatio = normalizeStatValue(pokemon.getStats().getHpIV(), 31);
        double attackIVRatio = normalizeStatValue(pokemon.getStats().getAttackIV(), 31);
        double defenseIVRatio = normalizeStatValue(pokemon.getStats().getDefenseIV(), 31);
        double speedIVRatio = normalizeStatValue(pokemon.getStats().getSpeedIV(), 31);
        double specialAtkIVRatio = normalizeStatValue(pokemon.getStats().getSpecialAtkIV(), 31);
        double specialDefIVRatio = normalizeStatValue(pokemon.getStats().getSpecialDefIV(), 31);
        
        // Get the EV stats (normalized to 0.0-1.0 scale)
        double hpEVRatio = normalizeStatValue(pokemon.getStats().getHpEV(), 252);
        double attackEVRatio = normalizeStatValue(pokemon.getStats().getAttackEV(), 252);
        double defenseEVRatio = normalizeStatValue(pokemon.getStats().getDefenseEV(), 252);
        double speedEVRatio = normalizeStatValue(pokemon.getStats().getSpeedEV(), 252);
        double specialAtkEVRatio = normalizeStatValue(pokemon.getStats().getSpecialAtkEV(), 252);
        double specialDefEVRatio = normalizeStatValue(pokemon.getStats().getSpecialDefEV(), 252);
        
        // Calculate points for the hexagon axes (6 stats)
        int sides = 6; // HP, Attack, Defense, Speed, Sp.Atk, Sp.Def
        double[] ivValues = {hpIVRatio, attackIVRatio, defenseIVRatio, speedIVRatio, specialAtkIVRatio, specialDefIVRatio};
        double[] evValues = {hpEVRatio, attackEVRatio, defenseEVRatio, speedEVRatio, specialAtkEVRatio, specialDefEVRatio};
        
        int[] ivXPoints = new int[sides];
        int[] ivYPoints = new int[sides];
        int[] evXPoints = new int[sides];
        int[] evYPoints = new int[sides];
        
        // Draw axes and labels
        g2d.setColor(Color.LIGHT_GRAY);
        String[] labels = {"HP", "Atk", "Def", "Spd", "Sp.Atk", "Sp.Def"};
        g2d.setFont(new Font("Lato", Font.BOLD, 14));
        
        for (int i = 0; i < sides; i++) {
            double angle = Math.PI * 2 * i / sides - Math.PI / 2;
            int xEnd = centerX + (int)(Math.cos(angle) * radius);
            int yEnd = centerY + (int)(Math.sin(angle) * radius);
            
            // Draw axis line
            g2d.drawLine(centerX, centerY, xEnd, yEnd);
            
            // Draw stat label
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = centerX + (int)(Math.cos(angle) * (radius + 20)) - fm.stringWidth(labels[i])/2;
            int labelY = centerY + (int)(Math.sin(angle) * (radius + 20)) + fm.getHeight()/2;
            g2d.drawString(labels[i], labelX, labelY);
            
            // Calculate points for both polygons
            double ivRadius = radius * ivValues[i];
            ivXPoints[i] = centerX + (int)(Math.cos(angle) * ivRadius);
            ivYPoints[i] = centerY + (int)(Math.sin(angle) * ivRadius);
            
            double evRadius = radius * evValues[i];
            evXPoints[i] = centerX + (int)(Math.cos(angle) * evRadius);
            evYPoints[i] = centerY + (int)(Math.sin(angle) * evRadius);
        }
        
        // Draw concentric circles for reference
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 1; i <= 5; i++) {
            int circleRadius = radius * i / 5;
            g2d.drawOval(centerX - circleRadius, centerY - circleRadius, 
                        circleRadius * 2, circleRadius * 2);
        }
        
        // Draw the EV polygon first (so IV polygon appears on top)
        g2d.setColor(new Color(255, 165, 0, 150)); // Semi-transparent orange for EVs
        g2d.fillPolygon(evXPoints, evYPoints, sides);
        g2d.setColor(new Color(255, 165, 0));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(evXPoints, evYPoints, sides);
        
        // Draw the IV polygon on top
        g2d.setColor(new Color(30, 201, 139, 150)); // Semi-transparent green for IVs
        g2d.fillPolygon(ivXPoints, ivYPoints, sides);
        g2d.setColor(new Color(30, 201, 139));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawPolygon(ivXPoints, ivYPoints, sides);
    }
    
    private static double normalizeStatValue(int statValue, int maxPossibleValue) {
        return Math.min(1.0, Math.max(0.0, (double)statValue / maxPossibleValue));
    }
    
    public static JPanel createNumericStatsPanel(Pokemon pokemon) {
        JPanel numericStatsPanel = new JPanel(new GridLayout(7, 4, 10, 8));
        numericStatsPanel.setBorder(BorderFactory.createTitledBorder("Numeric Values"));
        
        Font labelFont = new Font("Lato", Font.BOLD, 12);
        
        // Header row
        numericStatsPanel.add(new JLabel("Stat", JLabel.CENTER));
        numericStatsPanel.add(new JLabel("Value", JLabel.CENTER));
        numericStatsPanel.add(new JLabel("IV", JLabel.CENTER));
        numericStatsPanel.add(new JLabel("EV", JLabel.CENTER));
        
        // HP row
        JLabel hpLabel = new JLabel("HP:", JLabel.RIGHT);
        hpLabel.setFont(labelFont);
        numericStatsPanel.add(hpLabel);
        
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getMaxHp()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getHpIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getHpEV()), JLabel.CENTER));
        
        // Attack row
        numericStatsPanel.add(new JLabel("Attack:", JLabel.RIGHT));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getAttack()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getAttackIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getAttackEV()), JLabel.CENTER));
        
        // Defense row
        numericStatsPanel.add(new JLabel("Defense:", JLabel.RIGHT));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getDefense()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getDefenseIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getDefenseEV()), JLabel.CENTER));
        
        // Speed row
        numericStatsPanel.add(new JLabel("Speed:", JLabel.RIGHT));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpeed()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpeedIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpeedEV()), JLabel.CENTER));
        
        // Sp.Attack row
        numericStatsPanel.add(new JLabel("Sp.Attack:", JLabel.RIGHT));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialAtk()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialAtkIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialAtkEV()), JLabel.CENTER));
        
        // Sp.Defense row
        numericStatsPanel.add(new JLabel("Sp.Defense:", JLabel.RIGHT));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialDef()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialDefIV()), JLabel.CENTER));
        numericStatsPanel.add(new JLabel(String.valueOf(pokemon.getStats().getSpecialDefEV()), JLabel.CENTER));
        
        return numericStatsPanel;
    }
}
