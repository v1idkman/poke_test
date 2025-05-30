package model;

import java.awt.*;
import ui.Board;

public class CivilianNpc extends Npc {
    private String occupation; // e.g., "Nurse", "Shop Clerk", "Civilian"
    private String[] dialogueOptions; // Multiple dialogue lines
    private int currentDialogueIndex = 0;
    
    public CivilianNpc(Point position, String name, String spritePath, Direction facing, 
                      Board board, String occupation, boolean canMove) {
        super(position, name, spritePath, facing, board, canMove);
        this.occupation = occupation;
        this.dialogueOptions = new String[]{"Hello there!", "How can I help you?"};
        this.dialogueText = dialogueOptions[0];
    }
    
    public CivilianNpc(Point position, String name, String spritePath, Direction facing, 
                      Board board, String occupation, boolean canMove, String[] dialogueOptions) {
        super(position, name, spritePath, facing, board, canMove);
        this.occupation = occupation;
        this.dialogueOptions = dialogueOptions != null ? dialogueOptions : 
                              new String[]{"Hello there!", "How can I help you?"};
        this.dialogueText = this.dialogueOptions[0];
    }
    
    @Override
    public NpcType getNpcType() {
        return NpcType.CIVILIAN;
    }
    
    @Override
    public boolean canInitiateBattle() {
        return false; // Civilians never battle
    }
    
    @Override
    public String getInteractionDialogue() {
        // Cycle through dialogue options
        String dialogue = dialogueOptions[currentDialogueIndex];
        currentDialogueIndex = (currentDialogueIndex + 1) % dialogueOptions.length;
        return dialogue;
    }
    
    // Civilian-specific methods
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public void setDialogueOptions(String[] dialogueOptions) {
        this.dialogueOptions = dialogueOptions;
        this.currentDialogueIndex = 0;
        if (dialogueOptions.length > 0) {
            this.dialogueText = dialogueOptions[0];
        }
    }
    
    public String[] getDialogueOptions() { return dialogueOptions; }
}
