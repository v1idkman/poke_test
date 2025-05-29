package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Npc;

public class NpcDialogueScreen extends JFrame {
    private Npc npc;
    private Board board;
    
    public NpcDialogueScreen(Npc npc, Board board) {
        this.npc = npc;
        this.board = board;
        
        setTitle("Trainer Encounter");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // NPC name
        JLabel nameLabel = new JLabel(npc.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Dialogue text
        JTextArea dialogueArea = new JTextArea(npc.getDialogueText());
        dialogueArea.setFont(new Font("Arial", Font.PLAIN, 14));
        dialogueArea.setWrapStyleWord(true);
        dialogueArea.setLineWrap(true);
        dialogueArea.setEditable(false);
        dialogueArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogueArea.setBackground(getBackground());
        
        // Continue button
        JButton continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Arial", Font.BOLD, 16));
        continueButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(continueButton);
        
        add(nameLabel, BorderLayout.NORTH);
        add(dialogueArea, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
