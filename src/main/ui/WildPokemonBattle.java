package ui;

import model.Player;
import pokes.Pokemon;
import javax.swing.Timer;

public class WildPokemonBattle extends BattleScreen {
    private Pokemon wildPokemon;
    
    public WildPokemonBattle(Player player, Pokemon wildPokemon, String battleLocation) {
        super(player, wildPokemon, battleLocation);
        this.wildPokemon = wildPokemon;
    }
    
    @Override
    protected String getInitialBattleMessage() {
        return "A wild " + currentOpponentPokemon.getName() + " appeared!";
    }
    
    @Override
    protected void handleOpponentFainted() {
        battleEnded = true;
        
        // Award experience
        Timer expTimer = new Timer(1500, e -> {
            awardExperience();
            
            // Close battle after experience is awarded
            Timer closeTimer = new Timer(2000, event -> dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
        expTimer.setRepeats(false);
        expTimer.start();
    }
    
    @Override
    protected boolean canUsePokeballs() {
        return true; // Can use Pokéballs in wild battles
    }
    
    @Override
    protected boolean canRun() {
        return true; // Can run from wild battles
    }
    
    @Override
    protected void handleBattleEnd(boolean playerWon) {
        if (playerWon) {
            queueMessage("You won the battle!");
        } else {
            queueMessage("You have no usable Pokemon left!");
            queueMessage("You blacked out!");
        }
        
        Timer closeTimer = new Timer(3000, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }
    
    @Override
    protected void switchOpponentPokemon(Pokemon newPokemon) {
        // Wild battles don't switch Pokemon - this shouldn't be called
        throw new UnsupportedOperationException("Wild Pokemon don't switch!");
    }
    
    private void awardExperience() {
        int participantCount = 1;
        int expGain = pokes.LevelManager.calculateExpGain(wildPokemon, participantCount, true);
        
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
