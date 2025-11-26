package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class MeditationController {

    @FXML private Button backBtn;
    @FXML private Label sessionTitle;
    @FXML private Label timerLabel;
    @FXML private Label guidedText;
    @FXML private Circle breathingCircle;
    @FXML private StackPane breathingLotus;
    @FXML private Label breathLabel;
    
    @FXML private ComboBox<String> sessionTypeCombo;
    @FXML private ComboBox<Integer> durationCombo;
    
    @FXML private ToggleButton rainSoundBtn;
    @FXML private ToggleButton oceanSoundBtn;
    @FXML private ToggleButton forestSoundBtn;
    @FXML private ToggleButton bowlSoundBtn;
    @FXML private Slider volumeSlider;
    @FXML private Button volumeDownBtn;
    @FXML private Button volumeUpBtn;
    
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button stopBtn;
    
    @FXML private ProgressBar sessionProgress;
    @FXML private VBox breathingPatternBox;
    @FXML private RadioButton pattern478;
    @FXML private RadioButton patternBox;
    @FXML private RadioButton patternDeep;

    private Timeline breathingAnimation;
    private Timeline guidedTextAnimation;
    private Timeline timerAnimation;
    
    private final Map<ToggleButton, MediaPlayer> soundPlayers = new HashMap<>();
    private int sessionDuration = 5; // minutes
    private int elapsedSeconds = 0;
    private boolean isSessionActive = false;
    
    // Guided meditation texts for different session types
    private final Map<String, String[]> guidedTexts = new HashMap<>();
    private int textIndex = 0;
    private String currentSessionType = "Mindfulness";
    private static String soulId = "";

    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML
    public void initialize() {
        // Stop background music when entering meditation
        MusicManager.pauseBackgroundMusic();
        
        setupBackButton();
        setupSessionTypes();
        setupDurations();
        setupAmbientSounds();
        setupVolumeButtons();
        setupBreathingPatterns();
        setupGuidedTexts();
        setupControls();
        
        // Set default breathing pattern
        pattern478.setSelected(true);
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            stopSession();
            // Resume background music when leaving meditation
            MusicManager.playBackgroundMusic();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml")
            );
            Parent dashboard = loader.load();
            DashboardController controller = loader.getController();
            if (controller != null && soulId != null && !soulId.isEmpty()) {
                controller.setSoulId(soulId);
            }
            backBtn.getScene().setRoot(dashboard);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupSessionTypes() {
        sessionTypeCombo.getItems().addAll(
            "Mindfulness",
            "Body Scan",
            "Loving Kindness",
            "Breath Awareness",
            "Visualization",
            "Stress Relief",
            "Sleep Preparation"
        );
        sessionTypeCombo.setValue("Mindfulness");
        sessionTypeCombo.setOnAction(e -> {
            currentSessionType = sessionTypeCombo.getValue();
            sessionTitle.setText(currentSessionType + " Meditation");
            // Reset text index and show first prompt of new session
            textIndex = 0;
            if (!isSessionActive) {
                String[] texts = guidedTexts.get(currentSessionType);
                if (texts != null && texts.length > 0) {
                    guidedText.setText("Ready for " + currentSessionType + ": " + texts[0]);
                }
            }
        });
    }

    private void setupDurations() {
        durationCombo.getItems().addAll(3, 5, 10, 15, 20, 30);
        durationCombo.setValue(5);
        durationCombo.setOnAction(e -> sessionDuration = durationCombo.getValue());
    }

    private void setupAmbientSounds() {
        try {
            soundPlayers.put(rainSoundBtn, loadSound("rain.mp4"));
            soundPlayers.put(oceanSoundBtn, loadSound("ocean.mp4"));
            soundPlayers.put(forestSoundBtn, loadSound("storm.mp4"));
            soundPlayers.put(bowlSoundBtn, loadSound("waterfall.mp4"));

            soundPlayers.forEach((toggle, player) -> {
                toggle.setOnAction(e -> {
                    if (toggle.isSelected()) {
                        // Stop all other sounds for smooth transition
                        soundPlayers.forEach((otherToggle, otherPlayer) -> {
                            if (otherToggle != toggle) {
                                otherPlayer.stop();
                                otherToggle.setSelected(false);
                            }
                        });
                        // Play selected sound
                        player.play();
                        updateVolume(player);
                    } else {
                        player.stop();
                    }
                });
            });

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                soundPlayers.values().forEach(player -> updateVolume(player));
            });
        } catch (Exception e) {
            System.err.println("Could not load ambient sounds: " + e.getMessage());
        }
    }

    private MediaPlayer loadSound(String fileName) {
        Media media = new Media(getClass().getResource("/assets/audio/" + fileName).toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(0.4);
        return player;
    }

    private void updateVolume(MediaPlayer player) {
        player.setVolume(volumeSlider.getValue() / 100.0);
    }

    private void setupVolumeButtons() {
        if (volumeUpBtn != null) {
            volumeUpBtn.setOnAction(e -> {
                double newVolume = Math.min(100, volumeSlider.getValue() + 10);
                volumeSlider.setValue(newVolume);
            });
        }
        
        if (volumeDownBtn != null) {
            volumeDownBtn.setOnAction(e -> {
                double newVolume = Math.max(0, volumeSlider.getValue() - 10);
                volumeSlider.setValue(newVolume);
            });
        }
    }

    private void setupBreathingPatterns() {
        ToggleGroup patternGroup = new ToggleGroup();
        pattern478.setToggleGroup(patternGroup);
        patternBox.setToggleGroup(patternGroup);
        patternDeep.setToggleGroup(patternGroup);
        
        pattern478.setOnAction(e -> setupBreathingAnimation(4, 7, 8));
        patternBox.setOnAction(e -> setupBreathingAnimation(4, 4, 4));
        patternDeep.setOnAction(e -> setupBreathingAnimation(5, 5, 5));
        
        // Default pattern
        setupBreathingAnimation(4, 7, 8);
    }

    private void setupBreathingAnimation(int inhale, int hold, int exhale) {
        if (breathingAnimation != null) {
            breathingAnimation.stop();
        }

        breathingAnimation = new Timeline(
            // Inhale - expand lotus flower
            new KeyFrame(Duration.ZERO,
                new KeyValue(breathingLotus.scaleXProperty(), 0.7),
                new KeyValue(breathingLotus.scaleYProperty(), 0.7),
                new KeyValue(breathingLotus.rotateProperty(), 0),
                new KeyValue(breathLabel.textProperty(), "Inhale...")
            ),
            new KeyFrame(Duration.seconds(inhale),
                new KeyValue(breathingLotus.scaleXProperty(), 1.2),
                new KeyValue(breathingLotus.scaleYProperty(), 1.2),
                new KeyValue(breathingLotus.rotateProperty(), 15)
            ),
            // Hold - keep expanded with gentle rotation
            new KeyFrame(Duration.seconds(inhale + 0.1),
                new KeyValue(breathLabel.textProperty(), "Hold...")
            ),
            new KeyFrame(Duration.seconds(inhale + hold),
                new KeyValue(breathingLotus.scaleXProperty(), 1.2),
                new KeyValue(breathingLotus.scaleYProperty(), 1.2),
                new KeyValue(breathingLotus.rotateProperty(), 25)
            ),
            // Exhale - contract lotus flower
            new KeyFrame(Duration.seconds(inhale + hold + 0.1),
                new KeyValue(breathLabel.textProperty(), "Exhale...")
            ),
            new KeyFrame(Duration.seconds(inhale + hold + exhale),
                new KeyValue(breathingLotus.scaleXProperty(), 0.7),
                new KeyValue(breathingLotus.scaleYProperty(), 0.7),
                new KeyValue(breathingLotus.rotateProperty(), 0)
            )
        );

        breathingAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void setupGuidedTexts() {
        guidedTexts.put("Mindfulness", new String[]{
            "Find a comfortable position...",
            "Close your eyes gently...",
            "Notice your breath, without changing it...",
            "Let thoughts pass like clouds in the sky...",
            "Bring your attention back to the present moment...",
            "Feel the weight of your body...",
            "You are here, you are now, you are enough..."
        });

        guidedTexts.put("Body Scan", new String[]{
            "Bring awareness to the top of your head...",
            "Relax your forehead, your eyebrows...",
            "Soften your jaw, let your tongue rest...",
            "Release tension in your shoulders...",
            "Feel your chest rise and fall...",
            "Relax your hands, your fingers...",
            "Notice your legs, heavy and relaxed..."
        });

        guidedTexts.put("Loving Kindness", new String[]{
            "May I be happy...",
            "May I be healthy...",
            "May I be safe...",
            "May I live with ease...",
            "Extend these wishes to someone you love...",
            "Now to someone neutral...",
            "May all beings be happy and free..."
        });

        guidedTexts.put("Breath Awareness", new String[]{
            "Follow the natural rhythm of your breath...",
            "Notice the cool air entering...",
            "Feel the warm air leaving...",
            "Your breath is always with you...",
            "An anchor in the present moment...",
            "Simply observe, without judgment...",
            "Each breath is a new beginning..."
        });

        guidedTexts.put("Visualization", new String[]{
            "Imagine a peaceful place...",
            "See the colors, hear the sounds...",
            "Feel completely safe here...",
            "This is your sanctuary...",
            "You can return here anytime...",
            "Breathe in the peace...",
            "Let it fill every cell of your being..."
        });

        guidedTexts.put("Stress Relief", new String[]{
            "Acknowledge your stress without judgment...",
            "Breathe into the tension...",
            "With each exhale, release a little more...",
            "You don't have to carry it all...",
            "Let go of what you cannot control...",
            "You are doing your best...",
            "Peace is your natural state..."
        });

        guidedTexts.put("Sleep Preparation", new String[]{
            "The day is complete...",
            "Let go of what happened...",
            "Tomorrow will take care of itself...",
            "Your body is ready to rest...",
            "Feel yourself getting heavier...",
            "Sinking into deep relaxation...",
            "Safe, comfortable, at peace..."
        });
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startSession());
        pauseBtn.setOnAction(e -> pauseSession());
        stopBtn.setOnAction(e -> stopSession());
        
        pauseBtn.setDisable(true);
        stopBtn.setDisable(true);
    }

    private void startSession() {
        if (isSessionActive && breathingAnimation.getStatus() == Animation.Status.PAUSED) {
            // Resume
            breathingAnimation.play();
            guidedTextAnimation.play();
            timerAnimation.play();
            startBtn.setText("Start");
            pauseBtn.setDisable(false);
            return;
        }

        isSessionActive = true;
        elapsedSeconds = 0;
        textIndex = 0;
        
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        stopBtn.setDisable(false);
        
        // Start breathing animation
        breathingAnimation.play();
        
        // Start guided text cycle
        setupGuidedTextCycle();
        guidedTextAnimation.play();
        
        // Start timer
        setupTimer();
        timerAnimation.play();
    }

    private void setupGuidedTextCycle() {
        String[] texts = guidedTexts.getOrDefault(currentSessionType, 
            guidedTexts.get("Mindfulness"));
        
        guidedTextAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> {
                guidedText.setText(texts[textIndex]);
                textIndex = (textIndex + 1) % texts.length;
            }),
            new KeyFrame(Duration.seconds(15))
        );
        guidedTextAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void setupTimer() {
        timerAnimation = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                elapsedSeconds++;
                updateTimerDisplay();
                updateProgress();
                
                if (elapsedSeconds >= sessionDuration * 60) {
                    sessionComplete();
                }
            })
        );
        timerAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void updateTimerDisplay() {
        int remaining = (sessionDuration * 60) - elapsedSeconds;
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateProgress() {
        double progress = (double) elapsedSeconds / (sessionDuration * 60);
        sessionProgress.setProgress(progress);
    }

    private void pauseSession() {
        if (breathingAnimation != null) breathingAnimation.pause();
        if (guidedTextAnimation != null) guidedTextAnimation.pause();
        if (timerAnimation != null) timerAnimation.pause();
        
        soundPlayers.values().forEach(MediaPlayer::pause);
        
        pauseBtn.setDisable(true);
        startBtn.setDisable(false);
        startBtn.setText("Resume");
    }

    private void stopSession() {
        isSessionActive = false;
        
        if (breathingAnimation != null) breathingAnimation.stop();
        if (guidedTextAnimation != null) guidedTextAnimation.stop();
        if (timerAnimation != null) timerAnimation.stop();
        
        soundPlayers.values().forEach(player -> {
            player.stop();
        });
        
        // Reset toggles
        soundPlayers.keySet().forEach(toggle -> toggle.setSelected(false));
        
        elapsedSeconds = 0;
        textIndex = 0;
        
        timerLabel.setText(String.format("%02d:00", sessionDuration));
        sessionProgress.setProgress(0);
        guidedText.setText("Select your session and press Start");
        breathLabel.setText("Breathe...");
        
        // Reset lotus flower
        breathingLotus.setScaleX(0.7);
        breathingLotus.setScaleY(0.7);
        breathingLotus.setRotate(0);
        
        startBtn.setDisable(false);
        startBtn.setText("Start");
        pauseBtn.setDisable(true);
        stopBtn.setDisable(true);
    }

    private void sessionComplete() {
        stopSession();
        guidedText.setText("✨ Session Complete! Well done! ✨");
        
        // Play a gentle completion sound or show animation
        Timeline completeAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(guidedText.scaleXProperty(), 1.0),
                new KeyValue(guidedText.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(0.5),
                new KeyValue(guidedText.scaleXProperty(), 1.2),
                new KeyValue(guidedText.scaleYProperty(), 1.2)
            ),
            new KeyFrame(Duration.seconds(1.0),
                new KeyValue(guidedText.scaleXProperty(), 1.0),
                new KeyValue(guidedText.scaleYProperty(), 1.0)
            )
        );
        completeAnimation.setCycleCount(3);
        completeAnimation.play();
    }
}
