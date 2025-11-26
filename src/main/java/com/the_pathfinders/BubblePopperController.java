package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubblePopperController {

    @FXML private Button backBtn;
    @FXML private Pane bubblePane;
    @FXML private Label scoreLabel;
    @FXML private Label messageLabel;
    @FXML private Button startBtn;
    @FXML private Button resetBtn;

    private int score = 0;
    private Timeline bubbleSpawner;
    private List<Circle> activeBubbles = new ArrayList<>();
    private Random random = new Random();
    private boolean isGameActive = false;

    // Pastel gradient colors for bubbles
    private final Color[][] gradientColors = {
        {Color.web("#FFB6D9"), Color.web("#FFC9E5")},
        {Color.web("#B799D9"), Color.web("#D4C5F9")},
        {Color.web("#93C9E8"), Color.web("#B8E0F6")},
        {Color.web("#A8D8B9"), Color.web("#C9EDD4")},
        {Color.web("#FFD4A3"), Color.web("#FFE5C2")},
        {Color.web("#F5C6EC"), Color.web("#FADDF5")},
        {Color.web("#C2E9FB"), Color.web("#E0F4FF")},
        {Color.web("#FFDEE9"), Color.web("#FFE8F0")}
    };

    private final String[] motivationalMessages = {
        "Keep popping! 💫",
        "You're doing great! ✨",
        "So relaxing! 🌸",
        "Beautiful! 🌈",
        "Wonderful! 💖",
        "Peaceful vibes! 🫧",
        "Amazing! 🌟",
        "Stay calm! 🦋"
    };

    @FXML
    public void initialize() {
        setupBackButton();
        setupControls();
        scoreLabel.setText("Score: 0");
        messageLabel.setText("Click bubbles to pop them and relax 🫧");
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            stopGame();
            Parent calmActivities = FXMLLoader.load(
                getClass().getResource("/com/the_pathfinders/fxml/CalmActivities.fxml")
            );
            backBtn.getScene().setRoot(calmActivities);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startGame());
        resetBtn.setOnAction(e -> resetGame());
        resetBtn.setDisable(true);
    }

    private void startGame() {
        if (!isGameActive) {
            isGameActive = true;
            startBtn.setDisable(true);
            resetBtn.setDisable(false);
            
            // Spawn bubbles at regular intervals
            bubbleSpawner = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> spawnBubble())
            );
            bubbleSpawner.setCycleCount(Animation.INDEFINITE);
            bubbleSpawner.play();
            
            messageLabel.setText("Pop the bubbles! 🫧");
        }
    }

    private void spawnBubble() {
        double radius = 25 + random.nextDouble() * 40; // 25-65px radius
        double x = radius + random.nextDouble() * (bubblePane.getWidth() - 2 * radius);
        double y = bubblePane.getHeight() + radius;

        Circle bubble = new Circle(radius);
        bubble.setCenterX(x);
        bubble.setCenterY(y);

        // Random pastel gradient
        Color[] colors = gradientColors[random.nextInt(gradientColors.length)];
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, colors[0]),
            new Stop(1, colors[1])
        );
        bubble.setFill(gradient);
        bubble.setStroke(Color.WHITE);
        bubble.setStrokeWidth(2);
        bubble.setOpacity(0.85);

        // Add subtle glow effect
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(colors[0]);
        glow.setRadius(15);
        glow.setSpread(0.5);
        bubble.setEffect(glow);

        // Bubble click handler
        bubble.setOnMouseClicked(e -> popBubble(bubble));

        // Floating animation
        double duration = 5 + random.nextDouble() * 3; // 5-8 seconds
        TranslateTransition floatUp = new TranslateTransition(Duration.seconds(duration), bubble);
        floatUp.setToY(-bubblePane.getHeight() - radius * 2);
        floatUp.setOnFinished(e -> {
            bubblePane.getChildren().remove(bubble);
            activeBubbles.remove(bubble);
        });

        // Gentle sway animation
        double swayAmount = 30 + random.nextDouble() * 40;
        TranslateTransition sway = new TranslateTransition(Duration.seconds(2 + random.nextDouble()), bubble);
        sway.setByX(random.nextBoolean() ? swayAmount : -swayAmount);
        sway.setAutoReverse(true);
        sway.setCycleCount(Animation.INDEFINITE);

        activeBubbles.add(bubble);
        bubblePane.getChildren().add(bubble);
        floatUp.play();
        sway.play();
    }

    private void popBubble(Circle bubble) {
        if (!isGameActive) return;

        score += 10;
        scoreLabel.setText("Score: " + score);
        
        // Show random motivational message every 5 pops
        if (score % 50 == 0) {
            messageLabel.setText(motivationalMessages[random.nextInt(motivationalMessages.length)]);
        }

        // Pop animation
        ScaleTransition shrink = new ScaleTransition(Duration.millis(200), bubble);
        shrink.setToX(1.5);
        shrink.setToY(1.5);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), bubble);
        fade.setToValue(0);
        
        ParallelTransition pop = new ParallelTransition(shrink, fade);
        pop.setOnFinished(e -> {
            bubblePane.getChildren().remove(bubble);
            activeBubbles.remove(bubble);
        });
        
        pop.play();
    }

    private void stopGame() {
        if (bubbleSpawner != null) {
            bubbleSpawner.stop();
        }
        
        // Remove all active bubbles
        bubblePane.getChildren().removeAll(activeBubbles);
        activeBubbles.clear();
        
        isGameActive = false;
    }

    private void resetGame() {
        stopGame();
        score = 0;
        scoreLabel.setText("Score: 0");
        messageLabel.setText("Click bubbles to pop them and relax 🫧");
        startBtn.setDisable(false);
        resetBtn.setDisable(true);
    }
}
