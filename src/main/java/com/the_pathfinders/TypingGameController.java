package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Random;

public class TypingGameController {

    private static String soulId = "";
    
    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML private Button backBtn;
    @FXML private Label phraseLabel;
    @FXML private TextField typingField;
    @FXML private Label feedbackLabel;
    @FXML private Label statsLabel;
    @FXML private Button nextBtn;
    @FXML private Button startBtn;

    private final String[] motivationalPhrases = {
        "I am capable of achieving my goals.",
        "Every day, I grow stronger and more resilient.",
        "I choose peace over worry.",
        "My mind is calm, my heart is at peace.",
        "I am worthy of love and respect.",
        "I release what I cannot control.",
        "Today, I focus on what brings me joy.",
        "I am enough, just as I am.",
        "I trust the journey of my life.",
        "I embrace challenges as opportunities to grow.",
        "My thoughts are powerful, and I choose positivity.",
        "I am grateful for this moment.",
        "I breathe in peace, I breathe out stress.",
        "I am creating a life I love.",
        "My potential is limitless.",
        "I deserve happiness and success.",
        "I am proud of how far I have come.",
        "I choose to see the good in every situation.",
        "I am in charge of how I feel, and I choose happiness.",
        "I am surrounded by love and support.",
        "Each breath fills me with calm energy.",
        "I let go of past mistakes and embrace new beginnings.",
        "I am patient with myself and my progress.",
        "My presence makes a positive difference.",
        "I trust in my ability to overcome obstacles."
    };

    private Random random = new Random();
    private String currentPhrase;
    private int phrasesCompleted = 0;
    private long startTime;
    private boolean isGameActive = false;

    @FXML
    public void initialize() {
        setupBackButton();
        setupControls();
        typingField.setDisable(true);
        nextBtn.setDisable(true);
        statsLabel.setText("Phrases completed: 0");
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/the_pathfinders/fxml/CalmActivities.fxml")
            );
            Parent calmActivities = loader.load();
            if (soulId != null && !soulId.isEmpty()) {
                CalmActivitiesController.setSoulId(soulId);
            }
            backBtn.getScene().setRoot(calmActivities);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startGame());
        nextBtn.setOnAction(e -> loadNextPhrase());
        
        typingField.textProperty().addListener((obs, oldText, newText) -> {
            if (isGameActive) {
                checkTyping(newText);
            }
        });
    }

    private void startGame() {
        isGameActive = true;
        phrasesCompleted = 0;
        startBtn.setDisable(true);
        typingField.setDisable(false);
        typingField.requestFocus();
        loadNextPhrase();
    }

    private void loadNextPhrase() {
        currentPhrase = motivationalPhrases[random.nextInt(motivationalPhrases.length)];
        phraseLabel.setText(currentPhrase);
        typingField.clear();
        typingField.setDisable(false);
        typingField.requestFocus();
        nextBtn.setDisable(true);
        feedbackLabel.setText("Start typing...");
        feedbackLabel.setStyle("-fx-text-fill: #8B7BA8;");
        startTime = System.currentTimeMillis();
    }

    private void checkTyping(String typed) {
        if (typed.equals(currentPhrase)) {
            // Perfect match!
            phraseCompleted();
        } else if (currentPhrase.startsWith(typed)) {
            // Correct so far
            feedbackLabel.setText("Keep going... ✨");
            feedbackLabel.setStyle("-fx-text-fill: #93C9E8;");
        } else {
            // Mistake
            feedbackLabel.setText("Check your typing... 💭");
            feedbackLabel.setStyle("-fx-text-fill: #FFB6D9;");
        }
    }

    private void phraseCompleted() {
        phrasesCompleted++;
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime) / 1000; // seconds
        
        typingField.setDisable(true);
        nextBtn.setDisable(false);
        
        feedbackLabel.setText(String.format("✨ Perfect! Completed in %d seconds ✨", timeTaken));
        feedbackLabel.setStyle("-fx-text-fill: #A8D8B9; -fx-font-weight: bold;");
        
        statsLabel.setText(String.format("Phrases completed: %d", phrasesCompleted));
        
        // Celebration animation
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), feedbackLabel);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        fade.setCycleCount(4);
        fade.setAutoReverse(true);
        fade.play();
        
        // Show encouraging message every 5 phrases
        if (phrasesCompleted % 5 == 0) {
            showEncouragingMessage();
        }
    }

    private void showEncouragingMessage() {
        String[] encouragements = {
            "You're doing wonderfully! 🌟",
            "What a peaceful practice! 🌸",
            "Your focus is amazing! ✨",
            "Keep up the great work! 💖",
            "You're radiating calm energy! 🫧"
        };
        
        Label temp = new Label(encouragements[random.nextInt(encouragements.length)]);
        temp.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #B799D9;");
        
        // This would need to be added to a parent container dynamically
        // For now, just update the stats label temporarily
        String originalText = statsLabel.getText();
        statsLabel.setText(encouragements[random.nextInt(encouragements.length)]);
        
        FadeTransition restore = new FadeTransition(Duration.seconds(2), statsLabel);
        restore.setOnFinished(e -> statsLabel.setText(originalText));
        restore.play();
    }
}
