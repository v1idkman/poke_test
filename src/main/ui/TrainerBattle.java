package ui;

import model.Player;
import model.Npc;
import pokes.Pokemon;

import java.awt.Component;
import java.util.List;
import javax.swing.Timer;

public class TrainerBattle extends BattleScreen {
    private Npc trainer;
    private List<Pokemon> trainerTeam;
    private int currentTrainerPokemonIndex;
    
    public TrainerBattle(Player player, Npc trainer, String battleLocation) {
        // Pass the initial message directly to super() to avoid method call
        super(player, getFirstUsablePokemon(trainer), battleLocation, 
              trainer.getName() + " wants to battle!");
        
        // Initialize fields AFTER super() call
        this.trainer = trainer;
        this.trainerTeam = trainer.getTeam();
        this.currentTrainerPokemonIndex = findFirstUsablePokemonIndex(trainer);
    }
    
    // Keep your static helper methods as they are
    private static Pokemon getFirstUsablePokemon(Npc trainer) {
        for (Pokemon pokemon : trainer.getTeam()) {
            if (pokemon.getStats().getCurrentHp() > 0) {
                return pokemon;
            }
        }
        throw new IllegalStateException("Trainer has no usable Pokemon!");
    }
    
    private static int findFirstUsablePokemonIndex(Npc trainer) {
        for (int i = 0; i < trainer.getTeam().size(); i++) {
            if (trainer.getTeam().get(i).getStats().getCurrentHp() > 0) {
                return i;
            }
        }
        return 0;
    }
    
    @Override
    protected String getInitialBattleMessage() {
        // This method should now only be called after initialization
        return trainer.getName() + " wants to battle!";
    }
    
    @Override
    protected void handleOpponentFainted() {
        Pokemon nextPokemon = getNextTrainerPokemon();
        
        if (nextPokemon != null) {
            // Stop any existing timers to prevent conflicts
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            
            awardExperience();
            queueMessage(trainer.getName() + " sent out " + nextPokemon.getName() + "!");
            
            Timer switchTimer = new Timer(2000, e -> {
                // Switch to the new Pokemon
                switchOpponentPokemon(nextPokemon);
                
                // **CRITICAL**: Reset all battle state variables
                animationStep = 0;
                playerTurn = true;
                battleEnded = false;
                isDisplayingMessages = false;
                
                // Clear any remaining messages that might interfere
                messageQueue.clear();
                
                Timer continueTimer = new Timer(1000, event -> {
                    battleMessageLabel.setText("What will " + playerPokemon.getName() + " do?");
                    switchToPanel(actionPanel);
                    
                    // Ensure action buttons are enabled
                    enableActionButtons();
                });
                continueTimer.setRepeats(false);
                continueTimer.start();
            });
            switchTimer.setRepeats(false);
            switchTimer.start();
        } else {
            battleEnded = true;
            awardExperience();
            handleBattleEnd(true);
        }
    }

    private void enableActionButtons() {
        if (fightButton != null) fightButton.setEnabled(true);
        if (bagButton != null) bagButton.setEnabled(true);
        if (pokemonButton != null) pokemonButton.setEnabled(true);
        if (runButton != null) runButton.setEnabled(canRun());
        
        // Ensure the action panel is properly displayed
        if (actionPanel != null) {
            for (Component component : actionPanel.getComponents()) {
                component.setEnabled(true);
            }
        }
    }
    

    @Override
    protected boolean canUsePokeballs() {
        return false;
    }
    
    @Override
    protected boolean canRun() {
        return false;
    }
    
    @Override
    protected void handleBattleEnd(boolean playerWon) {
        if (playerWon) {
            queueMessage("You defeated " + trainer.getName() + "!");
            queueMessage(trainer.getName() + ": " + trainer.getDialogueText());
            trainer.setDefeated(true);
        } else {
            queueMessage(trainer.getName() + " defeated you!");
            queueMessage("You have no usable Pokemon left!");
            queueMessage("You blacked out!");
        }
        
        Timer closeTimer = new Timer(4000, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }
    
    @Override
    protected void switchOpponentPokemon(Pokemon newPokemon) {
        // Update the current opponent Pokemon reference
        this.currentOpponentPokemon = newPokemon;
        
        // Update the display
        updateOpponentDisplay(newPokemon);
        
        // Find and update the trainer's current Pokemon index
        for (int i = 0; i < trainerTeam.size(); i++) {
            if (trainerTeam.get(i) == newPokemon) {
                currentTrainerPokemonIndex = i;
                break;
            }
        }
        
        // Ensure the new Pokemon has valid moves
        if (newPokemon.getMoves().isEmpty()) {
            newPokemon.generateWildMoves();
        }
        
        // Refresh the UI
        battlegroundPanel.revalidate();
        battlegroundPanel.repaint();
    }

    private Pokemon getNextTrainerPokemon() {
        // Look for the next usable Pokemon starting from the current index + 1
        for (int i = currentTrainerPokemonIndex + 1; i < trainerTeam.size(); i++) {
            Pokemon pokemon = trainerTeam.get(i);
            if (pokemon.getStats().getCurrentHp() > 0) {
                return pokemon;
            }
        }
        
        // If no Pokemon found after current index, check from the beginning
        // (in case there's a gap in the team)
        for (int i = 0; i < currentTrainerPokemonIndex; i++) {
            Pokemon pokemon = trainerTeam.get(i);
            if (pokemon.getStats().getCurrentHp() > 0) {
                return pokemon;
            }
        }
        
        return null; // No usable Pokemon left
    }
    
    
    private void awardExperience() {
        int participantCount = 1;
        int expGain = pokes.LevelManager.calculateExpGain(currentOpponentPokemon, participantCount, false);
        
        queueMessage(playerPokemon.getName() + " gained " + expGain + " EXP. Points!");
        
        boolean leveledUp = playerPokemon.gainExperience(expGain);
        updatePlayerExpBar();
        
        if (leveledUp) {
            queueMessage(playerPokemon.getName() + " grew to level " + playerPokemon.getLevel() + "!");
            playerPokemonInfo.setText(playerPokemon.getName() + " L" + playerPokemon.getLevel());
            playerPokemonHP.setMaximum(playerPokemon.getStats().getMaxHp());
            playerPokemonHP.setValue(playerPokemon.getStats().getCurrentHp());
            hpValueLabel.setText(playerPokemon.getStats().getCurrentHp() + "/" + playerPokemon.getStats().getMaxHp());
        }
    }
}
