package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.util.Duration;
import java.awt.Desktop;
import java.net.URI;
import java.util.*;

public class CalmActivitiesController {

    private static String soulId = "";
    
    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML private BorderPane root;
    @FXML private Button backBtn;
    @FXML private Button hamburgerBtn;
    @FXML private VBox sidebarPanel;
    
    // Inner Voice Console Elements
    @FXML private Pane driftingNotesPane;
    @FXML private VBox consoleContent;
    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private Label promptLabel1;
    @FXML private TextField wordInput;
    @FXML private Label responseLabel1;
    @FXML private Label promptLabel2;
    @FXML private TextField letterInput;
    @FXML private Label responseLabel2;
    @FXML private StackPane letterAnimationPane;
    @FXML private VBox letterCard;
    @FXML private Label letterText;
    
    // Sidebar state
    private boolean sidebarVisible = false;
    
    // Inner Voice state
    private List<VBox> driftingNotes = new ArrayList<>();
    private Random random = new Random();
    
    // Questionnaire state
    private static Map<String, String> userLastCompletionDate = new HashMap<>();
    private int currentQuestionIndex = 0;
    private Map<String, String> userResponses = new HashMap<>();
    private List<Question> questions = new ArrayList<>();
    
    // Left sidebar navigation buttons
    @FXML private Button drawingBtn;
    @FXML private Button breathingBallBtn;
    @FXML private Button bubblePopperBtn;
    @FXML private Button typingGameBtn;
    @FXML private Button gratitudeGardenBtn;

    @FXML
    public void initialize() {
        attachCss();
        setupNavigationButtons();
        startInnerVoiceConsole();
        
        if (backBtn != null) backBtn.setOnAction(e -> goBack());
        if (hamburgerBtn != null) hamburgerBtn.setOnAction(e -> toggleSidebar());
    }
    
    private void attachCss() {
        if (root == null) return;
        root.getStylesheets().clear();
        var css = getClass().getResource("/com/the_pathfinders/css/calm_activities.css");
        if (css != null) root.getStylesheets().add(css.toExternalForm());
    }
    
    private void setupNavigationButtons() {
        if (drawingBtn != null) drawingBtn.setOnAction(e -> openDrawingTool());
        if (breathingBallBtn != null) breathingBallBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/BreathingBall.fxml"));
        if (bubblePopperBtn != null) bubblePopperBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/BubblePopper.fxml"));
        if (typingGameBtn != null) typingGameBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/TypingGame.fxml"));
        if (gratitudeGardenBtn != null) gratitudeGardenBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/GratitudeGarden.fxml"));
    }
    
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml")
            );
            Parent dashboard = loader.load();
            DashboardController controller = loader.getController();
            if (controller != null && soulId != null && !soulId.isEmpty()) {
                controller.setSoulId(soulId);
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dashboard);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void toggleSidebar() {
        if (sidebarPanel == null) return;
        
        TranslateTransition slideTransition = new TranslateTransition(Duration.millis(300), sidebarPanel);
        
        if (sidebarVisible) {
            // Hide sidebar
            slideTransition.setToX(-200);
            sidebarVisible = false;
        } else {
            // Show sidebar
            slideTransition.setToX(0);
            sidebarVisible = true;
        }
        
        slideTransition.play();
    }
    
    private void openDrawingTool() {
        try {
            String url = "https://www.youidraw.com/apps/painter/";
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadActivity(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent p = loader.load();
            
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setSoulId", String.class).invoke(controller, soulId);
                } catch (Exception e) {
                    // Controller doesn't have setSoulId method
                }
            }
            
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(p);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    // ============ Inner Voice Console Methods ============
    
    private void startInnerVoiceConsole() {
        if (promptLabel1 == null) return;
        
        // Check if user already completed questionnaire today
        String today = java.time.LocalDate.now().toString();
        String lastCompletion = userLastCompletionDate.get(soulId);
        
        if (today.equals(lastCompletion)) {
            showWelcomeBack();
            return;
        }
        
        // Reset for new session
        currentQuestionIndex = 0;
        userResponses.clear();
        
        // Initialize questions
        initializeQuestions();
        
        // Start questionnaire
        showNextQuestion();
    }
    
    private void initializeQuestions() {
        questions.clear();
        
        // Question 1: One word feeling
        questions.add(new Question(
            "Hey… what's one word your mind would whisper right now?",
            QuestionType.TEXT,
            null,
            "q1_feeling"
        ));
        
        // Question 2: Future message
        questions.add(new Question(
            "And what would you like your future self to remember from this moment?",
            QuestionType.TEXT,
            null,
            "q2_future_message"
        ));
        
        // Question 3: Current mood
        questions.add(new Question(
            "How are you feeling right now?",
            QuestionType.CHOICE,
            Arrays.asList("Anxious", "Sad", "Stressed", "Restless", "Overwhelmed", "Calm"),
            "q3_mood"
        ));
        
        // Question 4: Energy level
        questions.add(new Question(
            "What's your energy level like?",
            QuestionType.CHOICE,
            Arrays.asList("Very low", "Low", "Moderate", "High", "Very energetic"),
            "q4_energy"
        ));
        
        // Question 5: What helps
        questions.add(new Question(
            "What usually helps you feel better?",
            QuestionType.CHOICE,
            Arrays.asList("Creative expression", "Breathing exercises", "Physical activity", "Writing thoughts", "Gentle interactions"),
            "q5_preference"
        ));
    }
    
    private void showNextQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            // All questions answered - show recommendations
            analyzeAndRecommend();
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Clear previous content
        if (step1Container != null) {
            step1Container.getChildren().clear();
            step1Container.setVisible(true);
            step1Container.setManaged(true);
        }
        if (step2Container != null) {
            step2Container.setVisible(false);
            step2Container.setManaged(false);
        }
        if (letterAnimationPane != null) {
            letterAnimationPane.setVisible(false);
            letterAnimationPane.setManaged(false);
        }
        
        // Show question with typewriter effect
        Label questionLabel = new Label();
        questionLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #7a9cc6; -fx-font-weight: 400; -fx-line-spacing: 4px;");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(550);
        questionLabel.setAlignment(Pos.CENTER);
        step1Container.getChildren().add(questionLabel);
        
        typewriterEffect(questionLabel, currentQuestion.text, () -> {
            if (currentQuestion.type == QuestionType.TEXT) {
                showTextInput();
            } else {
                showChoiceButtons(currentQuestion.choices);
            }
        });
    }
    
    private void showTextInput() {
        TextField input = new TextField();
        input.setStyle("-fx-background-color: #BCD4FF; -fx-background-radius: 25; -fx-border-radius: 25; " +
                      "-fx-border-color: transparent; -fx-padding: 12 24 12 24; -fx-font-size: 16px; " +
                      "-fx-text-fill: #5a6b7d; -fx-effect: dropshadow(gaussian, rgba(218,232,255,0.6), 12, 0, 0, 2);");
        input.setPromptText("Type your answer...");
        input.setMaxWidth(400);
        input.setAlignment(Pos.CENTER);
        
        input.setOnAction(e -> {
            String answer = input.getText().trim();
            if (!answer.isEmpty()) {
                handleAnswer(answer, input);
            }
        });
        
        step1Container.getChildren().add(input);
        input.requestFocus();
    }
    
    private void showChoiceButtons(List<String> choices) {
        VBox choiceBox = new VBox(12);
        choiceBox.setAlignment(Pos.CENTER);
        choiceBox.setMaxWidth(500);
        
        for (String choice : choices) {
            Button btn = new Button(choice);
            btn.setStyle("-fx-background-color: linear-gradient(to right, #BCD4FF, #DAE8FF); " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 12 24; " +
                        "-fx-font-size: 15px; -fx-text-fill: #5a6b7d; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(188,212,255,0.4), 10, 0, 0, 3);");
            btn.setMaxWidth(Double.MAX_VALUE);
            
            btn.setOnMouseEntered(ev -> {
                btn.setStyle("-fx-background-color: linear-gradient(to right, #A9C2E8, #BCD4FF); " +
                           "-fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 12 24; " +
                           "-fx-font-size: 15px; -fx-text-fill: #5a6b7d; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(188,212,255,0.6), 14, 0, 0, 4); -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
            });
            
            btn.setOnMouseExited(ev -> {
                btn.setStyle("-fx-background-color: linear-gradient(to right, #BCD4FF, #DAE8FF); " +
                           "-fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 12 24; " +
                           "-fx-font-size: 15px; -fx-text-fill: #5a6b7d; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(188,212,255,0.4), 10, 0, 0, 3);");
            });
            
            btn.setOnAction(e -> handleAnswer(choice, btn));
            
            choiceBox.getChildren().add(btn);
        }
        
        step1Container.getChildren().add(choiceBox);
    }
    
    private void handleAnswer(String answer, javafx.scene.Node answerNode) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        userResponses.put(currentQuestion.id, answer);
        
        // Show acknowledgment
        Label ack = new Label(currentQuestionIndex < 2 ? "Noted. Your mind is speaking." : "Thank you.");
        ack.setStyle("-fx-font-size: 18px; -fx-text-fill: #A9C2E8; -fx-font-style: italic; -fx-font-weight: 300;");
        ack.setOpacity(0);
        step1Container.getChildren().add(ack);
        
        // Hide answer input
        answerNode.setVisible(false);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), ack);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        // Move to next question after brief pause
        currentQuestionIndex++;
        PauseTransition pause = new PauseTransition(Duration.millis(1200));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), step1Container);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                // Reset opacity before showing next question
                step1Container.setOpacity(1);
                showNextQuestion();
            });
            fadeOut.play();
        });
        pause.play();
    }
    
    private void analyzeAndRecommend() {
        // Mark questionnaire as completed for today
        String today = java.time.LocalDate.now().toString();
        userLastCompletionDate.put(soulId, today);
        
        // Analyze responses
        String mood = userResponses.getOrDefault("q3_mood", "");
        String energy = userResponses.getOrDefault("q4_energy", "");
        String preference = userResponses.getOrDefault("q5_preference", "");
        
        List<String> recommendations = new ArrayList<>();
        
        // Recommendation logic
        if (mood.contains("Anxious") || mood.contains("Stressed") || mood.contains("Overwhelmed")) {
            recommendations.add("Breathing Ball - Calm your nervous system");
            recommendations.add("Bubble Popper - Release tension playfully");
        }
        
        if (mood.contains("Restless") || energy.contains("High") || energy.contains("energetic")) {
            recommendations.add("Typing Zen - Channel energy mindfully");
            recommendations.add("Drawing Canvas - Express yourself creatively");
        }
        
        if (mood.contains("Sad") || energy.contains("Very low") || energy.contains("Low")) {
            recommendations.add("Gratitude Garden - Cultivate positive thoughts");
            recommendations.add("Breathing Ball - Gentle energy restoration");
        }
        
        if (preference.contains("Creative")) {
            recommendations.add("Drawing Canvas - Let your creativity flow");
            recommendations.add("Gratitude Garden - Creative positivity practice");
        }
        
        if (preference.contains("Breathing")) {
            recommendations.add("Breathing Ball - Guided breathing exercises");
        }
        
        if (preference.contains("Physical") || preference.contains("Gentle interactions")) {
            recommendations.add("Bubble Popper - Interactive relaxation");
            recommendations.add("Typing Zen - Mindful finger movement");
        }
        
        if (preference.contains("Writing")) {
            recommendations.add("Typing Zen - Therapeutic typing practice");
            recommendations.add("Gratitude Garden - Write positive affirmations");
        }
        
        // Remove duplicates and limit to top 3
        Set<String> uniqueRecs = new LinkedHashSet<>(recommendations);
        List<String> finalRecs = new ArrayList<>(uniqueRecs);
        if (finalRecs.size() > 3) {
            finalRecs = finalRecs.subList(0, 3);
        }
        
        // If no specific recommendations, provide defaults
        if (finalRecs.isEmpty()) {
            finalRecs.add("Breathing Ball - Start with calm");
            finalRecs.add("Gratitude Garden - Build positivity");
            finalRecs.add("Bubble Popper - Gentle relaxation");
        }
        
        showRecommendations(finalRecs);
    }
    
    private void showRecommendations(List<String> recommendations) {
        step1Container.getChildren().clear();
        step1Container.setVisible(true);
        step1Container.setManaged(true);
        step1Container.setOpacity(0);
        
        Label title = new Label("Based on how you're feeling...");
        title.setStyle("-fx-font-size: 26px; -fx-text-fill: #7a9cc6; -fx-font-weight: 600;");
        title.setAlignment(Pos.CENTER);
        
        Label subtitle = new Label("We recommend these activities for you:");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #A9C2E8; -fx-font-style: italic; -fx-padding: 0 0 20 0;");
        subtitle.setAlignment(Pos.CENTER);
        
        step1Container.getChildren().addAll(title, subtitle);
        
        VBox recBox = new VBox(15);
        recBox.setAlignment(Pos.CENTER);
        recBox.setMaxWidth(500);
        
        for (String rec : recommendations) {
            String[] parts = rec.split(" - ");
            String activityName = parts[0];
            String description = parts.length > 1 ? parts[1] : "";
            
            VBox recCard = new VBox(8);
            recCard.setAlignment(Pos.CENTER_LEFT);
            recCard.setPadding(new Insets(16, 20, 16, 20));
            recCard.setStyle("-fx-background-color: linear-gradient(to right, rgba(188,212,255,0.3), rgba(218,232,255,0.2)); " +
                           "-fx-background-radius: 15; -fx-border-radius: 15; " +
                           "-fx-border-color: rgba(169,194,232,0.4); -fx-border-width: 1.5; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3); -fx-cursor: hand;");
            
            Label actLabel = new Label("→ " + activityName);
            actLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #5a6b7d; -fx-font-weight: 600;");
            
            Label descLabel = new Label(description);
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8b96a5; -fx-font-style: italic;");
            descLabel.setWrapText(true);
            
            recCard.getChildren().addAll(actLabel, descLabel);
            
            recCard.setOnMouseEntered(ev -> {
                recCard.setStyle("-fx-background-color: linear-gradient(to right, rgba(169,194,232,0.4), rgba(188,212,255,0.3)); " +
                               "-fx-background-radius: 15; -fx-border-radius: 15; " +
                               "-fx-border-color: rgba(169,194,232,0.6); -fx-border-width: 1.5; " +
                               "-fx-effect: dropshadow(gaussian, rgba(147,184,217,0.2), 15, 0, 0, 5); -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
            });
            
            recCard.setOnMouseExited(ev -> {
                recCard.setStyle("-fx-background-color: linear-gradient(to right, rgba(188,212,255,0.3), rgba(218,232,255,0.2)); " +
                               "-fx-background-radius: 15; -fx-border-radius: 15; " +
                               "-fx-border-color: rgba(169,194,232,0.4); -fx-border-width: 1.5; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3); -fx-cursor: hand;");
            });
            
            recCard.setOnMouseClicked(ev -> openRecommendedActivity(activityName));
            
            recBox.getChildren().add(recCard);
        }
        
        step1Container.getChildren().add(recBox);
        
        FadeTransition fade = new FadeTransition(Duration.millis(800), step1Container);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    private void openRecommendedActivity(String activityName) {
        String path = null;
        if (activityName.contains("Breathing Ball")) {
            path = "/com/the_pathfinders/fxml/BreathingBall.fxml";
        } else if (activityName.contains("Bubble Popper")) {
            path = "/com/the_pathfinders/fxml/BubblePopper.fxml";
        } else if (activityName.contains("Typing Zen")) {
            path = "/com/the_pathfinders/fxml/TypingGame.fxml";
        } else if (activityName.contains("Gratitude Garden")) {
            path = "/com/the_pathfinders/fxml/GratitudeGarden.fxml";
        } else if (activityName.contains("Drawing Canvas")) {
            openDrawingTool();
            return;
        }
        
        if (path != null) {
            loadActivity(path);
        }
    }
    
    private void showWelcomeBack() {
        step1Container.getChildren().clear();
        step1Container.setVisible(true);
        step1Container.setManaged(true);
        step1Container.setOpacity(0);
        
        Label welcome = new Label("Welcome back");
        welcome.setStyle("-fx-font-size: 32px; -fx-text-fill: #7a9cc6; -fx-font-weight: 600;");
        welcome.setAlignment(Pos.CENTER);
        
        Label message = new Label("Your personalized calm space is ready");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #A9C2E8; -fx-font-style: italic; -fx-padding: 10 0 30 0;");
        message.setAlignment(Pos.CENTER);
        
        step1Container.getChildren().addAll(welcome, message);
        
        FadeTransition fade = new FadeTransition(Duration.millis(1000), step1Container);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        // Continue drifting notes in background
        if (driftingNotesPane != null && driftingNotes.isEmpty()) {
            addSomeDriftingNotes();
        }
    }
    
    private void addSomeDriftingNotes() {
        String[] sampleNotes = {"peace", "calm", "breathe", "present", "mindful"};
        for (int i = 0; i < 3; i++) {
            addDriftingNote(sampleNotes[random.nextInt(sampleNotes.length)]);
        }
    }
    
    private void typewriterEffect(Label label, String text, Runnable onComplete) {
        label.setText("");
        label.setVisible(true);
        label.setManaged(true);
        
        Timeline timeline = new Timeline();
        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(50 * i),
                event -> label.setText(text.substring(0, index))
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        if (onComplete != null) {
            timeline.setOnFinished(e -> onComplete.run());
        }
        
        timeline.play();
    }
    
    private void addDriftingNote(String text) {
        if (driftingNotesPane == null) return;
        
        // Create a small drifting note
        VBox note = new VBox();
        note.setAlignment(Pos.CENTER);
        note.setPadding(new Insets(8, 12, 8, 12));
        note.setMaxWidth(120);
        note.setMaxHeight(80);
        note.setStyle(generateNoteStyle());
        
        Label noteLabel = new Label(text);
        noteLabel.setWrapText(true);
        noteLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7a8fa3; -fx-font-style: italic;");
        note.getChildren().add(noteLabel);
        
        // Random position
        double startX = random.nextDouble() * 1000;
        double startY = 700 + random.nextDouble() * 100;
        
        note.setLayoutX(startX);
        note.setLayoutY(startY);
        note.setOpacity(0.4);
        
        driftingNotesPane.getChildren().add(note);
        driftingNotes.add(note);
        
        // Animate slow drift
        animateDrift(note);
    }
    
    private String generateNoteStyle() {
        String[] colors = {
            "-fx-background-color: linear-gradient(to bottom, #E8D7F1, #D6C4E5);",
            "-fx-background-color: linear-gradient(to bottom, #D4F1E8, #C4E5D6);",
            "-fx-background-color: linear-gradient(to bottom, #FFE5CC, #FFD9B3);"
        };
        String color = colors[random.nextInt(colors.length)];
        return color + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(150, 170, 200, 0.3); -fx-border-width: 1;";
    }
    
    private void animateDrift(VBox note) {
        double endX = note.getLayoutX() + (random.nextDouble() * 200 - 100);
        double endY = -100 - random.nextDouble() * 100;
        
        Path path = new Path();
        path.getElements().add(new MoveTo(note.getLayoutX(), note.getLayoutY()));
        
        // Bezier curve for smooth drift
        path.getElements().add(new CubicCurveTo(
            note.getLayoutX() + random.nextDouble() * 100 - 50, 
            note.getLayoutY() - 200,
            endX - 50, 
            endY + 100,
            endX, 
            endY
        ));
        
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(30 + random.nextDouble() * 20));
        pathTransition.setPath(path);
        pathTransition.setNode(note);
        pathTransition.setOnFinished(e -> {
            driftingNotesPane.getChildren().remove(note);
            driftingNotes.remove(note);
        });
        pathTransition.play();
    }
    
    // Inner class for Question structure
    private static class Question {
        String text;
        QuestionType type;
        List<String> choices;
        String id;
        
        Question(String text, QuestionType type, List<String> choices, String id) {
            this.text = text;
            this.type = type;
            this.choices = choices;
            this.id = id;
        }
    }
    
    private enum QuestionType {
        TEXT, CHOICE
    }
}
