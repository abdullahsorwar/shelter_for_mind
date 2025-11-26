package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GalaxyStargazingController {

    @FXML private Button backBtn;
    @FXML private Pane galaxyPane;
    @FXML private Label floatingTextLabel;
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;

    private Timeline starAnimation;
    private Timeline textAnimation;
    private List<Circle> stars = new ArrayList<>();
    private Random random = new Random();
    private boolean isActive = false;
    private int textIndex = 0;

    private final String[] inspirationalTexts = {
        "You are made of stardust and dreams ✨",
        "The universe believes in you 🌟",
        "Your light shines bright in the cosmos 💫",
        "Every star represents a hope 🌠",
        "You are infinite potential ✨",
        "The galaxy holds endless possibilities 🌌",
        "Your journey is written in the stars ⭐",
        "Peace flows through you like cosmic energy 🌟",
        "You belong among the stars 💫",
        "Your spirit is as vast as space ✨",
        "Let go and float among the stars 🌠",
        "You are exactly where you need to be 🌟",
        "The universe is your home 🌌",
        "Breathe in the cosmic calm ✨",
        "Your soul sparkles like starlight 💫",
        "Trust the rhythm of the universe 🌟",
        "You are a constellation of strengths ⭐",
        "The cosmos embraces you with love 💫",
        "Your energy radiates through galaxies ✨",
        "Find peace in the infinite expanse 🌌"
    };

    @FXML
    public void initialize() {
        setupBackButton();
        setupControls();
        createStarField();
        floatingTextLabel.setVisible(false);
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            stopExperience();
            Parent calmActivities = FXMLLoader.load(
                getClass().getResource("/com/the_pathfinders/fxml/CalmActivities.fxml")
            );
            backBtn.getScene().setRoot(calmActivities);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startExperience());
        pauseBtn.setOnAction(e -> pauseExperience());
        pauseBtn.setDisable(true);
    }

    private void createStarField() {
        // Create initial star field
        for (int i = 0; i < 200; i++) {
            addStar();
        }
    }

    private void addStar() {
        double x = random.nextDouble() * 900; // pane width
        double y = random.nextDouble() * 600; // pane height
        double radius = 0.5 + random.nextDouble() * 2.5;

        Circle star = new Circle(x, y, radius);
        
        // Random star colors (white to soft pastels)
        Color[] starColors = {
            Color.WHITE,
            Color.web("#FFF9E3"),
            Color.web("#E8F4F8"),
            Color.web("#FFE5EC"),
            Color.web("#F5E6FF"),
            Color.web("#E0F2E9")
        };
        star.setFill(starColors[random.nextInt(starColors.length)]);
        
        // Twinkle effect
        FadeTransition twinkle = new FadeTransition(
            Duration.seconds(1 + random.nextDouble() * 3), star
        );
        twinkle.setFromValue(0.3);
        twinkle.setToValue(1.0);
        twinkle.setCycleCount(Animation.INDEFINITE);
        twinkle.setAutoReverse(true);
        twinkle.play();

        stars.add(star);
        galaxyPane.getChildren().add(star);
    }

    private void startExperience() {
        if (!isActive) {
            isActive = true;
            startBtn.setDisable(true);
            pauseBtn.setDisable(false);
            floatingTextLabel.setVisible(true);

            // Animate stars moving slowly
            starAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0.1), e -> {
                    for (Circle star : stars) {
                        // Slow downward drift
                        star.setCenterY(star.getCenterY() + 0.2);
                        
                        // Wrap around if star goes off screen
                        if (star.getCenterY() > 620) {
                            star.setCenterY(-20);
                            star.setCenterX(random.nextDouble() * 900);
                        }
                    }
                })
            );
            starAnimation.setCycleCount(Animation.INDEFINITE);
            starAnimation.play();

            // Show inspirational texts periodically
            showFloatingText();
            textAnimation = new Timeline(
                new KeyFrame(Duration.seconds(8), e -> showFloatingText())
            );
            textAnimation.setCycleCount(Animation.INDEFINITE);
            textAnimation.play();
        }
    }

    private void showFloatingText() {
        floatingTextLabel.setText(inspirationalTexts[textIndex]);
        textIndex = (textIndex + 1) % inspirationalTexts.length;

        // Reset position
        floatingTextLabel.setTranslateX(0);
        floatingTextLabel.setTranslateY(0);
        floatingTextLabel.setOpacity(0);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), floatingTextLabel);
        fadeIn.setToValue(1.0);

        // Float upward
        TranslateTransition floatUp = new TranslateTransition(Duration.seconds(6), floatingTextLabel);
        floatUp.setByY(-50);

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), floatingTextLabel);
        fadeOut.setDelay(Duration.seconds(5));
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(
            new ParallelTransition(fadeIn, floatUp),
            fadeOut
        );
        sequence.play();
    }

    private void pauseExperience() {
        if (starAnimation != null) {
            starAnimation.pause();
        }
        if (textAnimation != null) {
            textAnimation.pause();
        }
        
        isActive = false;
        startBtn.setDisable(false);
        startBtn.setText("Resume");
        pauseBtn.setDisable(true);
    }

    private void stopExperience() {
        if (starAnimation != null) {
            starAnimation.stop();
        }
        if (textAnimation != null) {
            textAnimation.stop();
        }
        isActive = false;
    }
}
