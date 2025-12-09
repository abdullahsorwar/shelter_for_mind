package com.the_pathfinders;

import com.the_pathfinders.db.MoodTrackerRepository;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.chart.PieChart;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.util.*;

public class MoodTrackerController {

    @FXML private VBox contentBox;
    @FXML private VBox resultsBox;
    @FXML private Button backBtn;
    @FXML private Button closeBtn;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;
    @FXML private Circle progress1, progress2, progress3, progress4, progress5;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox questionsContainer;
    @FXML private PieChart moodPieChart;
    @FXML private Label summaryLabel;
    @FXML private Label scoreLabel;

    private String soulId;
    private int currentQuestionIndex = 0;
    private List<Circle> progressCircles;
    private List<QuestionData> questions;
    private Map<String, String> answers;
    private MoodTrackerRepository repository;

    private static class QuestionData {
        String question;
        String category; // stress, anxiety, energy, social, sleep
        List<String> options;
        int[] scores; // scores for each option

        QuestionData(String question, String category, List<String> options, int[] scores) {
            this.question = question;
            this.category = category;
            this.options = options;
            this.scores = scores;
        }
    }

    @FXML
    public void initialize() {
        progressCircles = Arrays.asList(progress1, progress2, progress3, progress4, progress5);
        answers = new HashMap<>();
        repository = new MoodTrackerRepository();

        // Initialize questions with emojis
        questions = new ArrayList<>();
        questions.add(new QuestionData(
            "How many pending tasks do you have?",
            "stress",
            Arrays.asList("😱 Too Much", "😓 Much", "😌 A Little", "😊 None"),
            new int[]{1, 2, 3, 4}
        ));
        questions.add(new QuestionData(
            "How would you rate your stress level today?",
            "stress",
            Arrays.asList("😤 Very High", "😣 High", "😐 Moderate", "😌 Low"),
            new int[]{1, 2, 3, 4}
        ));
        questions.add(new QuestionData(
            "How anxious do you feel right now?",
            "anxiety",
            Arrays.asList("😰 Very Anxious", "😟 Anxious", "😕 Slightly Anxious", "😊 Calm"),
            new int[]{1, 2, 3, 4}
        ));
        questions.add(new QuestionData(
            "How is your energy level today?",
            "energy",
            Arrays.asList("😴 Very Low", "😪 Low", "🙂 Moderate", "🤩 High"),
            new int[]{1, 2, 3, 4}
        ));
        questions.add(new QuestionData(
            "How well did you sleep last night?",
            "sleep",
            Arrays.asList("😫 Very Poor", "😞 Poor", "😐 Fair", "😴 Good"),
            new int[]{1, 2, 3, 4}
        ));

        loadQuestion(0);
        updateProgressIndicator();
    }

    public void setSoulId(String soulId) {
        this.soulId = soulId;
    }

    private void loadQuestion(int index) {
        questionsContainer.getChildren().clear();

        if (index >= 0 && index < questions.size()) {
            QuestionData question = questions.get(index);

            VBox questionBox = new VBox(15);
            questionBox.getStyleClass().add("question-box");

            Label questionLabel = new Label(question.question);
            questionLabel.getStyleClass().add("question-label");
            questionLabel.setWrapText(true);

            ToggleGroup toggleGroup = new ToggleGroup();

            for (int i = 0; i < question.options.size(); i++) {
                RadioButton radio = new RadioButton(question.options.get(i));
                radio.getStyleClass().add("answer-radio");
                radio.setToggleGroup(toggleGroup);

                // Pre-select if already answered
                String savedAnswer = answers.get(String.valueOf(index));
                if (savedAnswer != null && savedAnswer.equals(question.options.get(i))) {
                    radio.setSelected(true);
                }

                final int score = question.scores[i];
                final String option = question.options.get(i);
                radio.setOnAction(e -> {
                    answers.put(String.valueOf(currentQuestionIndex), option);
                    answers.put(String.valueOf(currentQuestionIndex) + "_score", String.valueOf(score));
                    answers.put(String.valueOf(currentQuestionIndex) + "_category", question.category);
                });

                questionBox.getChildren().add(radio);
            }

            questionBox.getChildren().add(0, questionLabel);
            questionsContainer.getChildren().add(questionBox);
        }
    }

    private void updateProgressIndicator() {
        for (int i = 0; i < progressCircles.size(); i++) {
            if (i == currentQuestionIndex) {
                progressCircles.get(i).getStyleClass().add("active");
            } else {
                progressCircles.get(i).getStyleClass().remove("active");
            }
        }
    }

    @FXML
    private void onPrevious() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            loadQuestion(currentQuestionIndex);
            updateProgressIndicator();
            updateNavigationButtons();
            scrollPane.setVvalue(0);
        }
    }

    @FXML
    private void onNext() {
        if (!isCurrentQuestionAnswered()) {
            showAlert("Please answer the current question before proceeding.");
            return;
        }

        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            loadQuestion(currentQuestionIndex);
            updateProgressIndicator();
            updateNavigationButtons();
            scrollPane.setVvalue(0);
        }
    }

    @FXML
    private void onSubmit() {
        if (!isCurrentQuestionAnswered()) {
            showAlert("Please answer all questions before submitting.");
            return;
        }

        // Calculate results and show pie chart
        showResults();
    }

    private boolean isCurrentQuestionAnswered() {
        return answers.containsKey(String.valueOf(currentQuestionIndex));
    }

    private void updateNavigationButtons() {
        prevBtn.setDisable(currentQuestionIndex == 0);

        if (currentQuestionIndex == questions.size() - 1) {
            nextBtn.setVisible(false);
            submitBtn.setVisible(true);
        } else {
            nextBtn.setVisible(true);
            submitBtn.setVisible(false);
        }
    }

    private void showResults() {
        // Calculate category scores
        Map<String, Integer> categoryScores = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (String key : answers.keySet()) {
            if (key.endsWith("_category")) {
                String questionIdx = key.replace("_category", "");
                String category = answers.get(key);
                String scoreKey = questionIdx + "_score";

                if (answers.containsKey(scoreKey)) {
                    int score = Integer.parseInt(answers.get(scoreKey));
                    categoryScores.put(category, categoryScores.getOrDefault(category, 0) + score);
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
            }
        }

        // Calculate overall mood score (0-100)
        int totalPossibleScore = questions.size() * 4; // max score per question is 4
        int actualScore = 0;
        for (int i = 0; i < questions.size(); i++) {
            String scoreKey = i + "_score";
            if (answers.containsKey(scoreKey)) {
                actualScore += Integer.parseInt(answers.get(scoreKey));
            }
        }

        double moodScore = (actualScore / (double) totalPossibleScore) * 100;

        // Save to database
        try {
            MoodTrackerRepository.MoodEntry entry = new MoodTrackerRepository.MoodEntry();
            entry.setSoulId(soulId);
            entry.setMoodScore((int) moodScore);
            entry.setStressScore(categoryScores.getOrDefault("stress", 0));
            entry.setAnxietyScore(categoryScores.getOrDefault("anxiety", 0));
            entry.setEnergyScore(categoryScores.getOrDefault("energy", 0));
            entry.setSleepScore(categoryScores.getOrDefault("sleep", 0));
            entry.setSocialScore(categoryScores.getOrDefault("social", 0));
            entry.setAnswers(answers.toString());

            repository.saveMoodEntry(entry);
        } catch (Exception e) {
            System.err.println("Error saving mood entry: " + e.getMessage());
            e.printStackTrace();
        }

        // Calculate percentages for pie chart
        moodPieChart.getData().clear();

        double totalScore = 0;
        for (String category : categoryScores.keySet()) {
            totalScore += categoryScores.get(category);
        }

        for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
            String categoryName = capitalizeCategory(entry.getKey());
            double percentage = (entry.getValue() / totalScore) * 100;
            PieChart.Data slice = new PieChart.Data(categoryName + " (" + String.format("%.1f", percentage) + "%)", entry.getValue());
            moodPieChart.getData().add(slice);
        }

        scoreLabel.setText(String.format("Overall Mood Score: %.0f/100", moodScore));

        // Generate summary
        String summary = generateSummary(moodScore, categoryScores, categoryCounts);
        summaryLabel.setText(summary);

        // Fade transition from questions to results
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), contentBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            contentBox.setVisible(false);
            contentBox.setManaged(false);
            resultsBox.setVisible(true);
            resultsBox.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), resultsBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private String capitalizeCategory(String category) {
        if (category == null || category.isEmpty()) return category;
        return category.substring(0, 1).toUpperCase() + category.substring(1);
    }

    private String generateSummary(double moodScore, Map<String, Integer> categoryScores, Map<String, Integer> categoryCounts) {
        StringBuilder summary = new StringBuilder();

        if (moodScore >= 75) {
            summary.append("You're doing great! Your mood is positive and balanced. ");
        } else if (moodScore >= 50) {
            summary.append("Your mood is moderate. There's room for improvement. ");
        } else {
            summary.append("You might be going through a tough time. Consider seeking support. ");
        }

        // Find the area that needs most attention (lowest average score)
        String lowestCategory = null;
        double lowestAverage = 5.0;

        for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
            double average = entry.getValue() / (double) categoryCounts.get(entry.getKey());
            if (average < lowestAverage) {
                lowestAverage = average;
                lowestCategory = entry.getKey();
            }
        }

        if (lowestCategory != null && lowestAverage < 2.5) {
            summary.append("\n\nPay attention to your ").append(lowestCategory)
                   .append(" - it seems to need extra care.");
        }

        return summary.toString();
    }

    @FXML
    private void onBack() {
        closePopup();
    }

    @FXML
    private void onClose() {
        closePopup();
    }

    @FXML
    private void onCloseResults() {
        closePopup();
    }

    private void closePopup() {
        if (contentBox.getScene() != null && contentBox.getScene().getRoot() instanceof javafx.scene.layout.StackPane) {
            javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) contentBox.getScene().getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    // Navigate back to dashboard
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml")
                    );
                    javafx.scene.Parent dashboardRoot = loader.load();

                    DashboardController controller = loader.getController();
                    if (controller != null) {
                        controller.setUser(soulId, soulId);
                    }

                    contentBox.getScene().setRoot(dashboardRoot);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Incomplete");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

