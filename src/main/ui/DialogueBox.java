package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class DialogueBox extends JPanel implements KeyListener {
    private static final int DIALOGUE_HEIGHT = 180;
    private static final int DIALOGUE_WIDTH_MARGIN = 40;
    private static final int PADDING = 25;
    private static final int BUTTON_HEIGHT = 35;
    private static final int ANIMATION_SPEED = 30;
    
    private Queue<DialogueMessage> messageQueue;
    private DialogueMessage currentMessage;
    private String displayedText;
    private int currentCharIndex;
    private Timer textAnimationTimer;
    private boolean isAnimating;
    private boolean isWaitingForInput;
    private boolean isShowingOptions;
    
    private JLabel nameLabel;
    private JTextArea textArea;
    private JPanel optionPanel;
    private JButton[] optionButtons;
    private int selectedOptionIndex;
    
    private Board parentBoard;
    private Consumer<Integer> optionCallback;
    private Runnable continueCallback;
    
    public DialogueBox(Board parentBoard) {
        this.parentBoard = parentBoard;
        this.messageQueue = new LinkedList<>();
        this.displayedText = "";
        this.currentCharIndex = 0;
        this.selectedOptionIndex = 0;
        
        initializeComponents();
        setupLayout();
        setupAnimationTimer();
        
        setFocusable(true);
        addKeyListener(this);
    }
    
    private void initializeComponents() {
        nameLabel = new JLabel();
        nameLabel.setFont(new Font("Arial", Font.BOLD, 50));
        nameLabel.setForeground(Color.YELLOW);
        nameLabel.setOpaque(false);
        
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 40));
        textArea.setForeground(Color.WHITE);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFocusable(false);
        textArea.setRows(4);
        
        optionPanel = new JPanel();
        optionPanel.setOpaque(false);
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setVisible(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(nameLabel, BorderLayout.NORTH);
        
        JPanel textWrapper = new JPanel(new BorderLayout());
        textWrapper.setOpaque(false);
        textWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        textWrapper.add(textArea, BorderLayout.CENTER);
        
        textPanel.add(textWrapper, BorderLayout.CENTER);
        
        contentPanel.add(textPanel, BorderLayout.CENTER);
        contentPanel.add(optionPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupAnimationTimer() {
        textAnimationTimer = new Timer(ANIMATION_SPEED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentMessage != null && currentCharIndex < currentMessage.getText().length()) {
                    currentCharIndex++;
                    displayedText = currentMessage.getText().substring(0, currentCharIndex);
                    textArea.setText(displayedText);
                    repaint();
                } else {
                    textAnimationTimer.stop();
                    isAnimating = false;
                    onTextAnimationComplete();
                }
            }
        });
    }
    
    // Updated method to use continueCallback
    public void queueMessage(String speaker, String text) {
        queueMessage(speaker, text, null, null, null);
    }
    
    public void queueMessage(String speaker, String text, Runnable continueCallback) {
        queueMessage(speaker, text, null, null, continueCallback);
    }
    
    public void queueMessage(String speaker, String text, String[] options, Consumer<Integer> optionCallback) {
        queueMessage(speaker, text, options, optionCallback, null);
    }
    
    // Main method that properly assigns callbacks
    public void queueMessage(String speaker, String text, String[] options, Consumer<Integer> optionCallback, Runnable continueCallback) {
        DialogueMessage message = new DialogueMessage(speaker, text, options, optionCallback, continueCallback);
        messageQueue.offer(message);
        if (!isVisible()) {
            showNextMessage();
        }
    }
    
    public void queueMessage(String text) {
        queueMessage("", text);
    }
    
    private void onTextAnimationComplete() {
        if (currentMessage.getOptions() != null && currentMessage.getOptions().length > 0) {
            showOptions();
        } else {
            isWaitingForInput = true;
        }
    }
    
    private void showOptions() {
        isShowingOptions = true;
        selectedOptionIndex = 0;
        
        String[] options = currentMessage.getOptions();
        optionButtons = new JButton[options.length];
        
        optionPanel.removeAll();
        optionPanel.setLayout(new GridLayout(options.length, 1, 8, 8));
        
        for (int i = 0; i < options.length; i++) {
            final int optionIndex = i;
            JButton button = new JButton("► " + options[i]);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusable(false);
            button.setPreferredSize(new Dimension(0, BUTTON_HEIGHT));
            button.addActionListener(e -> selectOption(optionIndex));
            
            optionButtons[i] = button;
            optionPanel.add(button);
        }
        
        updateOptionSelection();
        optionPanel.setVisible(true);
        revalidate();
        repaint();
    }
    
    private void updateOptionSelection() {
        if (optionButtons != null) {
            for (int i = 0; i < optionButtons.length; i++) {
                if (i == selectedOptionIndex) {
                    optionButtons[i].setBackground(new Color(70, 130, 255));
                    optionButtons[i].setForeground(Color.WHITE);
                    optionButtons[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                } else {
                    optionButtons[i].setBackground(new Color(40, 40, 40));
                    optionButtons[i].setForeground(Color.WHITE);
                    optionButtons[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }
            }
        }
    }
    
    private void selectOption(int optionIndex) {
        // Use the stored optionCallback
        if (optionCallback != null) {
            optionCallback.accept(optionIndex);
        }
        
        SwingUtilities.invokeLater(() -> {
            if (!messageQueue.isEmpty()) {
                showNextMessage();
            } else {
                hideDialogue();
            }
        });
    }
    
    private void continueDialogue() {
        if (isAnimating) {
            // Skip animation
            textAnimationTimer.stop();
            isAnimating = false;
            currentCharIndex = currentMessage.getText().length();
            displayedText = currentMessage.getText();
            textArea.setText(displayedText);
            onTextAnimationComplete();
        } else if (isWaitingForInput) {
            // Use the stored continueCallback before moving to next message
            if (continueCallback != null) {
                continueCallback.run();
            }
            
            if (!messageQueue.isEmpty()) {
                showNextMessage();
            } else {
                hideDialogue();
            }
        } else {
            // Force hide if in unexpected state
            hideDialogue();
        }
    }
    
    private void showNextMessage() {
        if (messageQueue.isEmpty()) {
            hideDialogue();
            return;
        }
        
        currentMessage = messageQueue.poll();
        
        // Properly assign callbacks from the current message
        this.optionCallback = currentMessage.getOptionCallback();
        this.continueCallback = currentMessage.getContinueCallback();
        
        currentCharIndex = 0;
        displayedText = "";
        isAnimating = true;
        isWaitingForInput = false;
        isShowingOptions = false;
        
        if (currentMessage.getSpeaker() != null && !currentMessage.getSpeaker().isEmpty()) {
            nameLabel.setText(currentMessage.getSpeaker() + ":");
            nameLabel.setVisible(true);
        } else {
            nameLabel.setVisible(false);
        }
        
        textArea.setText("");
        optionPanel.removeAll();
        optionPanel.setVisible(false);
        
        setVisible(true);
        
        // CRITICAL FIX: Proper focus management
        if (parentBoard != null) {
            // Remove focus from board first
            parentBoard.setFocusable(false);
            parentBoard.resetKeyStates();
        }
        
        // Set focus on dialogue box with proper timing
        SwingUtilities.invokeLater(() -> {
            setFocusable(true);
            requestFocusInWindow();
            
            // Ensure we actually got focus
            SwingUtilities.invokeLater(() -> {
                if (!hasFocus()) {
                    grabFocus(); // Force focus if requestFocusInWindow failed
                }
            });
        });
        
        textAnimationTimer.start();
    }
    
    private void hideDialogue() {
        setVisible(false);
        
        // CRITICAL FIX: Restore focus to board properly
        if (parentBoard != null) {
            SwingUtilities.invokeLater(() -> {
                parentBoard.setDialogueActive(false);
                parentBoard.setFocusable(true);
                parentBoard.requestFocusInWindow();
                
                // Ensure board actually gets focus back
                SwingUtilities.invokeLater(() -> {
                    if (!parentBoard.hasFocus()) {
                        parentBoard.grabFocus();
                    }
                });
            });
        }
    }    
    
    // Method to get preferred size using the constants
    public Dimension getPreferredDialogueSize(int screenWidth) {
        int width = screenWidth - (DIALOGUE_WIDTH_MARGIN * 2);
        return new Dimension(width, DIALOGUE_HEIGHT);
    }
    
    // Method to get dialogue bounds using the constants
    public Rectangle getDialogueBounds(int screenWidth, int screenHeight) {
        int width = screenWidth - (DIALOGUE_WIDTH_MARGIN * 2);
        int x = DIALOGUE_WIDTH_MARGIN;
        int y = screenHeight - DIALOGUE_HEIGHT - 20;
        return new Rectangle(x, y, width, DIALOGUE_HEIGHT);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0, 0, 0, 220),
            0, getHeight(), new Color(20, 20, 40, 220)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
        
        if (isWaitingForInput && !isShowingOptions) {
            drawContinueIndicator(g2d);
        }
        
        g2d.dispose();
    }
    
    private void drawContinueIndicator(Graphics2D g2d) {
        int x = getWidth() - 40;
        int y = getHeight() - 25;
        
        long time = System.currentTimeMillis();
        int alpha = (int) (128 + 127 * Math.sin(time * 0.01));
        
        g2d.setColor(new Color(255, 255, 0, alpha));
        g2d.fillPolygon(new int[]{x, x + 15, x + 7}, new int[]{y, y, y + 12}, 3);
        
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("SPACE", x - 35, y + 8);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (!isVisible()) return;
        
        int keyCode = e.getKeyCode();
        System.err.println("Key pressed: " + KeyEvent.getKeyText(keyCode));
        
        if (isShowingOptions) {
            System.out.println("Handling option selection");
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    selectedOptionIndex = Math.max(0, selectedOptionIndex - 1);
                    updateOptionSelection();
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    selectedOptionIndex = Math.min(optionButtons.length - 1, selectedOptionIndex + 1);
                    updateOptionSelection();
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_E:
                    selectOption(selectedOptionIndex);
                    break;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_E:
                    continueDialogue();
                    break;
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    // Updated DialogueMessage class to include continue callback
    private static class DialogueMessage {
        private String speaker;
        private String text;
        private String[] options;
        private Consumer<Integer> optionCallback;
        private Runnable continueCallback;
        
        public DialogueMessage(String speaker, String text, String[] options, 
                             Consumer<Integer> optionCallback, Runnable continueCallback) {
            this.speaker = speaker;
            this.text = text;
            this.options = options;
            this.optionCallback = optionCallback;
            this.continueCallback = continueCallback;
        }
        
        public String getSpeaker() { return speaker; }
        public String getText() { return text; }
        public String[] getOptions() { return options; }
        public Consumer<Integer> getOptionCallback() { return optionCallback; }
        public Runnable getContinueCallback() { return continueCallback; }
    }
}
