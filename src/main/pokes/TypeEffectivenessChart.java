package pokes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import pokes.Pokemon.PokemonType;

public class TypeEffectivenessChart {
    private static TypeEffectivenessChart instance;
    private Map<PokemonType, Map<PokemonType, Double>> effectivenessChart;
    
    private TypeEffectivenessChart() {
        effectivenessChart = new HashMap<>();
        loadTypeChart();
    }
    
    public static TypeEffectivenessChart getInstance() {
        if (instance == null) {
            instance = new TypeEffectivenessChart();
        }
        return instance;
    }
    
    private void loadTypeChart() {
        try {
            // Get the resource as a stream
            InputStream is = getClass().getClassLoader().getResourceAsStream("resources/type_effectiveness.csv");
            
            if (is == null) {
                System.err.println("Could not find type_effectiveness.csv");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            
            // Read header line to get column types
            line = reader.readLine();
            if (line == null) {
                System.err.println("Type chart file is empty");
                return;
            }
            
            String[] typeNames = line.split(",");
            
            // Skip the first column header ("Attacking")
            PokemonType[] columnTypes = new PokemonType[typeNames.length - 1];
            for (int i = 1; i < typeNames.length; i++) {
                columnTypes[i - 1] = PokemonType.valueOf(typeNames[i].toUpperCase());
            }
            
            // Read each row
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 2) continue;
                
                // First column is the attacking type
                PokemonType attackingType = PokemonType.valueOf(values[0].toUpperCase());
                
                // Create a map for this attacking type
                Map<PokemonType, Double> typeEffectiveness = new HashMap<>();
                
                // Read effectiveness values for each defending type
                for (int i = 1; i < values.length; i++) {
                    if (i - 1 < columnTypes.length) {
                        PokemonType defendingType = columnTypes[i - 1];
                        double effectiveness = Double.parseDouble(values[i]);
                        typeEffectiveness.put(defendingType, effectiveness);
                    }
                }
                
                effectivenessChart.put(attackingType, typeEffectiveness);
            }
            
            reader.close();
            System.out.println("Type effectiveness chart loaded successfully");
            
        } catch (IOException e) {
            System.err.println("Error loading type chart: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing type name: " + e.getMessage());
        }
    }
    
    public double getEffectiveness(PokemonType attackingType, PokemonType defendingType) {
        if (effectivenessChart.containsKey(attackingType) && 
            effectivenessChart.get(attackingType).containsKey(defendingType)) {
            return effectivenessChart.get(attackingType).get(defendingType);
        }
        return 1.0; // Default to normal effectiveness
    }
    
    public double getEffectiveness(PokemonType attackingType, java.util.List<PokemonType> defendingTypes) {
        double effectiveness = 1.0;
        for (PokemonType defendingType : defendingTypes) {
            effectiveness *= getEffectiveness(attackingType, defendingType);
        }
        return effectiveness;
    }
}