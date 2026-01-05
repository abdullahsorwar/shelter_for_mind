package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class BreathingBallController {

    private static String soulId = "";
    
    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML private Button backBtn;
    @FXML private Circle breathingBall;
    @FXML private Label phaseLabel;
    @FXML private Label instructionLabel;
    @FXML private ComboBox<String> patternCombo;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;

    private Timeline breathingAnimation;
    private boolean isActive = false;

    @FXML
    public void initialize() {
        setupBackButton();
        setupPatternCombo();
        setupControls();
        
        // Default pattern
        setupPattern(4, 4, 8, 2); // 4-7-8 breathing
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            stopBreathing();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/the_pathfinders/fxml/CalmActivities.fxml")
            );
            Parent calmActivities = loader.load();
            CalmActivitiesController controller = loader.getController();
            if (controller != null && soulId != null && !soulId.isEmpty()) {
                CalmActivitiesController.setSoulId(soulId);
            }
            backBtn.getScene().setRoot(calmActivities);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupPatternCombo() {
        patternCombo.getItems().addAll(
            "4-7-8 (Relaxing)",
            "Box Breathing (4-4-4-4)",
            "Deep Calm (5-5-5-5)",
            "Energizing (3-3-3-3)",
            "Extended Exhale (4-2-6-2)"
        );
        patternCombo.setValue("4-7-8 (Relaxing)");
        
        patternCombo.setOnAction(e -> {
            String selected = patternCombo.getValue();
            switch (selected) {
                case "4-7-8 (Relaxing)":
                    setupPattern(4, 7, 8, 2);
                    break;
                case "Box Breathing (4-4-4-4)":
                    setupPattern(4, 4, 4, 4);
                    break;
                case "Deep Calm (5-5-5-5)":
                    setupPattern(5, 5, 5, 5);
                    break;
                case "Energizing (3-3-3-3)":
                    setupPattern(3, 3, 3, 3);
                    break;
                case "Extended Exhale (4-2-6-2)":
                    setupPattern(4, 2, 6, 2);
                    break;
            }
        });
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startBreathing());
        stopBtn.setOnAction(e -> stopBreathing());
        stopBtn.setDisable(true);
        
        // Apply modern pastel styling
        applyModernButtonStyle(startBtn, "#A7C7E7", "#92B4D4"); // Pastel blue
        applyModernButtonStyle(stopBtn, "#FFB6C1", "#FFA0AD"); // Pastel pink
    }
    
    private void applyModernButtonStyle(Button btn, String color1, String color2) {
        btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + color1 + ", " + color2 + "); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 10 20; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + color2 + ", " + color1 + "); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + color1 + ", " + color2 + "); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");
        });
    }

    private void setupPattern(int inhale, int hold1, int exhale, int hold2) {
        if (breathingAnimation != null) {
            breathingAnimation.stop();
        }

        breathingAnimation = new Timeline();
        double currentTime = 0;

        // INHALE phase
        breathingAnimation.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(currentTime),
                new KeyValue(breathingBall.scaleXProperty(), 0.5),
                new KeyValue(breathingBall.scaleYProperty(), 0.5),
                new KeyValue(phaseLabel.textProperty(), "Inhale"),
                new KeyValue(instructionLabel.textProperty(), "Breathe in slowly through your nose...")
            ),
            new KeyFrame(Duration.seconds(currentTime + inhale),
                new KeyValue(breathingBall.scaleXProperty(), 1.5),
                new KeyValue(breathingBall.scaleYProperty(), 1.5)
            )
        );
        currentTime += inhale;

        // HOLD phase 1
        breathingAnimation.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(currentTime),
                new KeyValue(phaseLabel.textProperty(), "Hold"),
                new KeyValue(instructionLabel.textProperty(), "Hold your breath gently...")
            ),
            new KeyFrame(Duration.seconds(currentTime + hold1),
                new KeyValue(breathingBall.scaleXProperty(), 1.5),
                new KeyValue(breathingBall.scaleYProperty(), 1.5)
            )
        );
        currentTime += hold1;

        // EXHALE phase
        breathingAnimation.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(currentTime),
                new KeyValue(phaseLabel.textProperty(), "Exhale"),
                new KeyValue(instructionLabel.textProperty(), "Breathe out slowly through your mouth...")
            ),
            new KeyFrame(Duration.seconds(currentTime + exhale),
                new KeyValue(breathingBall.scaleXProperty(), 0.5),
                new KeyValue(breathingBall.scaleYProperty(), 0.5)
            )
        );
        currentTime += exhale;

        // HOLD phase 2
        if (hold2 > 0) {
            breathingAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(currentTime),
                    new KeyValue(phaseLabel.textProperty(), "Rest"),
                    new KeyValue(instructionLabel.textProperty(), "Rest before the next breath...")
                ),
                new KeyFrame(Duration.seconds(currentTime + hold2),
                    new KeyValue(breathingBall.scaleXProperty(), 0.5),
                    new KeyValue(breathingBall.scaleYProperty(), 0.5)
                )
            );
        }

        breathingAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void startBreathing() {
        if (!isActive) {
            breathingAnimation.play();
            isActive = true;
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
            patternCombo.setDisable(true);
        }
    }

    private void stopBreathing() {
        if (breathingAnimation != null) {
            breathingAnimation.stop();
        }
        
        // Reset to initial state
        breathingBall.setScaleX(0.5);
        breathingBall.setScaleY(0.5);
        phaseLabel.setText("Ready");
        instructionLabel.setText("Press Start to begin your breathing exercise");
        
        isActive = false;
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        patternCombo.setDisable(false);
    }
}
