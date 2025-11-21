package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class TranquilCornerController {

    @FXML private Circle breathingCircle;
    @FXML private Label breathLabel;
    @FXML private Label meditationText;

    @FXML private ToggleButton rainToggle;
    @FXML private ToggleButton oceanToggle;
    @FXML private ToggleButton stormToggle;
    @FXML private ToggleButton waterfallToggle;

    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button endBtn;
    @FXML private Button backBtn;

    private Timeline breathingAnimation;
    private Timeline meditationTextAnimation;

    private final Map<ToggleButton, MediaPlayer> soundPlayers = new HashMap<>();

    private final String[] meditationLines = {
            "Let your shoulders relax…",
            "Notice your breath without control…",
            "Let thoughts pass like clouds…",
            "You are safe. You are present.",
            "Relax your jaw, soften your face…"
    };

    private int meditationIndex = 0;

    @FXML
    public void initialize() {

        // BACK BUTTON FIX
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }

        setupBreathingAnimation();
        setupMeditationTextCycle();
        setupAmbientSounds();

        startBtn.setOnAction(e -> startSession());
        pauseBtn.setOnAction(e -> pauseSession());
        endBtn.setOnAction(e -> endSession());
    }

    // 🔙 RETURN TO POPUP (correct path!)
    private void goBack() {
        try {
            Parent popup = FXMLLoader.load(
                    getClass().getResource("/com/the_pathfinders/fxml/TranquilOptionsPopup.fxml")
            );
            backBtn.getScene().setRoot(popup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupBreathingAnimation() {
        breathingAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(breathingCircle.scaleXProperty(), 0.7),
                        new KeyValue(breathingCircle.scaleYProperty(), 0.7),
                        new KeyValue(breathLabel.textProperty(), "Inhale…")
                ),

                new KeyFrame(Duration.seconds(4),
                        new KeyValue(breathingCircle.scaleXProperty(), 1.2),
                        new KeyValue(breathingCircle.scaleYProperty(), 1.2)
                ),

                new KeyFrame(Duration.seconds(6),
                        new KeyValue(breathLabel.textProperty(), "Hold…")
                ),

                new KeyFrame(Duration.seconds(12),
                        new KeyValue(breathingCircle.scaleXProperty(), 0.6),
                        new KeyValue(breathingCircle.scaleYProperty(), 0.6),
                        new KeyValue(breathLabel.textProperty(), "Exhale slowly…")
                )
        );

        breathingAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void setupMeditationTextCycle() {
        meditationTextAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e ->
                        meditationText.setText(meditationLines[meditationIndex])
                ),
                new KeyFrame(Duration.seconds(10), e -> {
                    meditationIndex = (meditationIndex + 1) % meditationLines.length;
                    meditationText.setText(meditationLines[meditationIndex]);
                })
        );
        meditationTextAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void setupAmbientSounds() {
        soundPlayers.put(rainToggle, loadSound("rain.mp4"));
        soundPlayers.put(oceanToggle, loadSound("ocean.mp4"));
        soundPlayers.put(stormToggle, loadSound("storm.mp4"));
        soundPlayers.put(waterfallToggle, loadSound("waterfall.mp4"));

        soundPlayers.forEach((toggle, player) -> {
            toggle.setOnAction(e -> {
                if (toggle.isSelected()) player.play();
                else player.stop();
            });
        });
    }

    private MediaPlayer loadSound(String fileName) {
        Media media = new Media(getClass().getResource("/assets/audio/" + fileName).toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(0.4);
        return player;
    }

    private void startSession() {
        breathingAnimation.play();
        meditationTextAnimation.play();
    }

    private void pauseSession() {
        breathingAnimation.pause();
        meditationTextAnimation.pause();
        soundPlayers.values().forEach(MediaPlayer::pause);
    }

    private void endSession() {
        breathingAnimation.stop();
        meditationTextAnimation.stop();
        breathLabel.setText("Inhale…");
        meditationText.setText("Let your body relax…");
        breathingCircle.setScaleX(1.0);
        breathingCircle.setScaleY(1.0);

        soundPlayers.values().forEach(MediaPlayer::stop);
    }
}
