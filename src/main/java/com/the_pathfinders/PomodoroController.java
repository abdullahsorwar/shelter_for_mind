package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class PomodoroController {

    private static String soulId = "";
    
    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML private Button backBtn;
    @FXML private Label timerLabel;
    @FXML private Label phaseLabel;
    @FXML private Label sessionCountLabel;
    
    @FXML private ComboBox<Integer> workDurationCombo;
    @FXML private ComboBox<Integer> shortBreakCombo;
    @FXML private ComboBox<Integer> longBreakCombo;
    @FXML private ComboBox<String> themeCombo;
    
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resetBtn;
    @FXML private Button skipBtn;
    
    @FXML private Label motivationLabel;

    private Timeline timer;
    private int totalSeconds;
    private int remainingSeconds;
    private int completedPomodoros = 0;
    private boolean isWorkPhase = true;
    private boolean isActive = false;
    
    // Default durations in minutes
    private int workDuration = 25;
    private int shortBreakDuration = 5;
    private int longBreakDuration = 15;

    private final String[] workMotivations = {
        "Stay focused!",
        "You're doing great! 💪",
        "Keep the momentum going! ✨",
        "One step closer to your goal! 🎯",
        "You've got this! 🌟"
    };

    private final String[] breakMotivations = {
        "Well deserved break! 🌸",
        "Relax and recharge! 💆",
        "You earned this! ✨",
        "Take a deep breath! 🫧",
        "Rest is productive too! 🌈"
    };

    @FXML
    public void initialize() {
        setupBackButton();
        setupDurationCombos();
        setupThemeCombo();
        setupControls();
        updateDisplay();
        
        pauseBtn.setDisable(true);
        skipBtn.setDisable(true);
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            stopTimer();
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

    private void setupDurationCombos() {
        // Work duration options
        workDurationCombo.getItems().addAll(15, 20, 25, 30, 45, 60);
        workDurationCombo.setValue(25);
        workDurationCombo.setOnAction(e -> workDuration = workDurationCombo.getValue());
        
        // Short break options
        shortBreakCombo.getItems().addAll(3, 5, 7, 10);
        shortBreakCombo.setValue(5);
        shortBreakCombo.setOnAction(e -> shortBreakDuration = shortBreakCombo.getValue());
        
        // Long break options
        longBreakCombo.getItems().addAll(15, 20, 25, 30);
        longBreakCombo.setValue(15);
        longBreakCombo.setOnAction(e -> longBreakDuration = longBreakCombo.getValue());
    }

    private void setupThemeCombo() {
        themeCombo.getItems().addAll(
            "🌊 Ocean Serenity",
            "🌸 Cherry Blossom",
            "🌿 Forest Calm", 
            "💜 Lavender Peace",
            "🌅 Sunset Harmony",
            "🌙 Moonlight Zen"
        );
        themeCombo.setValue("🌊 Ocean Serenity");
        themeCombo.setOnAction(e -> applyTheme(themeCombo.getValue()));
    }

    private void applyTheme(String theme) {
        Parent root = backBtn.getScene().getRoot();
        String backgroundGradient = "";
        
        switch (theme) {
            case "🌸 Cherry Blossom":
                backgroundGradient = "linear-gradient(to bottom right, #FFE8F0, #FFD4E5, #FFC9E0, #FFE0EB)";
                break;
            case "🌿 Forest Calm":
                backgroundGradient = "linear-gradient(to bottom right, #E8F5E9, #C8E6C9, #A5D6A7, #DCF2DD)";
                break;
            case "💜 Lavender Peace":
                backgroundGradient = "linear-gradient(to bottom right, #F3E5F5, #E1BEE7, #CE93D8, #EDD7F0)";
                break;
            case "🌅 Sunset Harmony":
                backgroundGradient = "linear-gradient(to bottom right, #FFF3E0, #FFE0B2, #FFCC80, #FFECC7)";
                break;
            case "🌙 Moonlight Zen":
                backgroundGradient = "linear-gradient(to bottom right, #E8EAF6, #C5CAE9, #9FA8DA, #D9DCF2)";
                break;
            default: // Ocean Serenity
                backgroundGradient = "linear-gradient(to bottom right, #E0F7FA, #B2EBF2, #80DEEA, #D4F1F4)";
                break;
        }
        
        root.setStyle("-fx-background-color: " + backgroundGradient + ";");
    }

    private void setupControls() {
        startBtn.setOnAction(e -> startTimer());
        pauseBtn.setOnAction(e -> pauseTimer());
        resetBtn.setOnAction(e -> resetTimer());
        skipBtn.setOnAction(e -> skipPhase());
        
        // Apply modern pastel styling
        applyModernButtonStyle(startBtn, "#A7C7E7", "#92B4D4"); // Pastel blue
        applyModernButtonStyle(pauseBtn, "#FFD4A3", "#FFC18A"); // Pastel peach
        applyModernButtonStyle(resetBtn, "#D4A5E8", "#C192D5"); // Pastel purple
        applyModernButtonStyle(skipBtn, "#B4E5A8", "#A1D296"); // Pastel green
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

    private void startTimer() {
        if (!isActive) {
            isActive = true;
            
            if (remainingSeconds == 0) {
                // Starting new phase
                if (isWorkPhase) {
                    totalSeconds = workDuration * 60;
                    phaseLabel.setText(" FOCUS TIME");
                    motivationLabel.setText(workMotivations[(int)(Math.random() * workMotivations.length)]);
                } else {
                    // Determine if long break (every 4 pomodoros)
                    boolean isLongBreak = (completedPomodoros % 4 == 0 && completedPomodoros > 0);
                    totalSeconds = isLongBreak ? longBreakDuration * 60 : shortBreakDuration * 60;
                    phaseLabel.setText(isLongBreak ? "☕ LONG BREAK" : "🌸 SHORT BREAK");
                    motivationLabel.setText(breakMotivations[(int)(Math.random() * breakMotivations.length)]);
                }
                remainingSeconds = totalSeconds;
            }
            
            startBtn.setDisable(true);
            pauseBtn.setDisable(false);
            skipBtn.setDisable(false);
            
            // Disable duration combos during active session
            workDurationCombo.setDisable(true);
            shortBreakCombo.setDisable(true);
            longBreakCombo.setDisable(true);
            
            timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    remainingSeconds--;
                    updateDisplay();
                    
                    if (remainingSeconds <= 0) {
                        phaseComplete();
                    }
                })
            );
            timer.setCycleCount(Animation.INDEFINITE);
            timer.play();
            
            // Animate tomato
            animateTomato();
        }
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.pause();
        }
        isActive = false;
        startBtn.setDisable(false);
        startBtn.setText("Resume");
        pauseBtn.setDisable(true);
    }

    private void resetTimer() {
        stopTimer();
        remainingSeconds = 0;
        isWorkPhase = true;
        completedPomodoros = 0;
        updateDisplay();
        sessionCountLabel.setText("Pomodoros: 0");
        motivationLabel.setText("Ready to start your focused session?");
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        isActive = false;
        startBtn.setDisable(false);
        startBtn.setText("Start");
        pauseBtn.setDisable(true);
        skipBtn.setDisable(true);
        
        // Re-enable duration combos
        workDurationCombo.setDisable(false);
        shortBreakCombo.setDisable(false);
        longBreakCombo.setDisable(false);
    }

    private void skipPhase() {
        remainingSeconds = 0;
        phaseComplete();
    }

    private void phaseComplete() {
        stopTimer();
        
        if (isWorkPhase) {
            // Work phase completed
            completedPomodoros++;
            sessionCountLabel.setText("Pomodoros: " + completedPomodoros);
            // Removed addCompletedTomato() - replaced with theme selector
            
            // Celebration animation
            celebrateCompletion();
        }
        
        // Switch phase
        isWorkPhase = !isWorkPhase;
        remainingSeconds = 0;
        
        // Auto-start next phase option (commented out for user control)
        // startTimer();
    }

    private void updateDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void animateTomato() {
        // Animation removed - no visual element to animate
    }

    private void celebrateCompletion() {
        // Scale animation for timer
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.3), timerLabel);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
        
        // Show completion message
        motivationLabel.setText("✨ Pomodoro Complete! Great work! ✨");
        motivationLabel.setStyle("-fx-text-fill: #A8D8B9; -fx-font-weight: bold; -fx-font-size: 18px;");
        
        // Reset style after animation
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            motivationLabel.setStyle("-fx-text-fill: #8B7BA8; -fx-font-size: 16px;");
        });
        pause.play();
    }
}
