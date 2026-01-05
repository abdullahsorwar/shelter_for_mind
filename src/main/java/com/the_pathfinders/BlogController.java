package com.the_pathfinders;

import com.the_pathfinders.db.BlogHistoryRepository;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class BlogController {

    @FXML private VBox root;
    @FXML private TextField searchBar;
    @FXML private VBox categoriesContainer;
    @FXML private Button backBtn;
    @FXML private Label pageTitle;
    
    // Side Panel Controls
    @FXML private Button shuffleBtn;
    @FXML private Button listenModeBtn;
    @FXML private Button expertPickBtn;
    @FXML private ComboBox<String> sectionFilter;
    @FXML private ComboBox<String> lengthFilter;
    @FXML private Button clearFiltersBtn;
    @FXML private Label statsLabel;
    @FXML private Label progressLabel;
    @FXML private Label savedCountLabel;
    @FXML private Button viewHistoryBtn;
    @FXML private Button viewSavedBtn;

    private String soulId = "";
    private SavedBlogsManager savedBlogsManager;
    private BlogHistoryRepository blogHistoryRepo;
    private final ObservableList<Blog> allPosts = FXCollections.observableArrayList();
    private final List<Blog> filteredPosts = new ArrayList<>();
    private final Set<String> viewedArticlesToday = new HashSet<>();
    private final List<String> readingHistory = new ArrayList<>();
    
    // TTS Management
    private Process ttsProcess = null;
    private volatile boolean isSpeaking = false;

    // Category sections with emoji icons
    private static class CategorySection {
        String emoji;
        String title;
        String description;
        List<String> categories;
        String color;
        String borderColor;

        CategorySection(String emoji, String title, String description, List<String> categories, String color, String borderColor) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.categories = categories;
            this.color = color;
            this.borderColor = borderColor;
        }
    }

    private final List<CategorySection> sections = Arrays.asList(
            new CategorySection("🧠", "Mood & Anxiety Disorders", 
                "Common mental health conditions affecting mood, thoughts, and daily functioning",
                Arrays.asList("Depression", "Bipolar Disorder", "Anxiety Disorders", "Panic Disorder", "Social Anxiety Disorder", "Generalized Anxiety Disorder"),
                "#e8f4fd", "#90cdf4"),
                
            new CategorySection("😰", "Phobias & Specific Fears", 
                "Understanding and overcoming intense, irrational fears",
                Arrays.asList("Agoraphobia", "Social Phobia", "Specific Phobias"),
                "#ffe6f0", "#f687b3"),
                
            new CategorySection("⚡", "Trauma & Abuse Recovery", 
                "Healing from traumatic experiences and toxic relationships",
                Arrays.asList("Post-Traumatic Stress Disorder", "Childhood Trauma", "Emotional Abuse", "Toxic Relationships", "Healing Attachment Styles"),
                "#fef3e2", "#f6ad55"),
                
            new CategorySection("🩺", "Neurocognitive & Brain Health", 
                "Memory, cognitive function, and neurological conditions",
                Arrays.asList("Dementia", "Alzheimer's", "Brain Fog", "Age-Related Cognitive Decline", "Memory Strengthening"),
                "#f3e8ff", "#d6bcfa"),
                
            new CategorySection("💬", "Emotional & Interpersonal", 
                "Building healthy relationships and emotional well-being",
                Arrays.asList("Loneliness & Emotional Support", "Relationship Anxiety", "Self-Esteem & Confidence", "Friendship Problems", "Grief & Loss"),
                "#e6fffa", "#81e6d9"),
                
            new CategorySection("📚", "Stress & Lifestyle Management", 
                "Managing daily pressures and building healthy habits",
                Arrays.asList("Burnout & Stress", "Overthinking & Rumination", "Sleep & Insomnia", "Productivity & Motivation", "Burnout & Workplace Stress"),
                "#fff5f5", "#fc8181"),
                
            new CategorySection("🎓", "Student Life & Growth", 
                "Navigating academic pressures and personal development",
                Arrays.asList("Academic Stress", "Social Pressure", "Digital Addiction", "Identity & Self-Discovery"),
                "#e6f7ff", "#63b3ed"),
                
            new CategorySection("🔬", "Clinical & Specialized Conditions", 
                "Specific mental health diagnoses requiring professional treatment",
                Arrays.asList("Schizophrenia", "Obsessive-Compulsive Disorder", "Eating Disorder", "Personality Disorder", "ADHD", "Neurodevelopmental Disorder", "Panic & Grounding Techniques"),
                "#fef5e7", "#f59e0b")
    );

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
        
        // Initialize blog history repository
        if (this.blogHistoryRepo == null) {
            this.blogHistoryRepo = new BlogHistoryRepository();
        }
        
        // Load reading history from database
        loadReadingHistoryFromDatabase();
        
        if (savedBlogsManager != null && !allPosts.isEmpty()) {
            for (Blog b : allPosts) {
                b.setSavedForLater(savedBlogsManager.isBlogSaved(this.soulId, b.getId()));
            }
        }
        
        // Update progress labels with persisted data
        updateProgressLabels();
    }
    
    private void loadReadingHistoryFromDatabase() {
        try {
            if (blogHistoryRepo != null && soulId != null && !soulId.isEmpty()) {
                viewedArticlesToday.clear();
                viewedArticlesToday.addAll(blogHistoryRepo.getViewedTodayBlogIds(soulId));
                
                readingHistory.clear();
                readingHistory.addAll(blogHistoryRepo.getReadingHistoryCategories(soulId, 50));
                
                System.out.println("✓ Loaded reading history: " + viewedArticlesToday.size() + " articles read today");
            }
        } catch (Exception e) {
            System.err.println("Failed to load reading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        if (pageTitle != null) {
            pageTitle.setText("Mental Wellness Library 🧠");
            pageTitle.setStyle(pageTitle.getStyle() + " -fx-font-family: 'Segoe UI', 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
        }
        
        savedBlogsManager = new SavedBlogsManager();
        
        // Initialize all blog posts
        initializeBlogPosts();
        
        // Build categorized UI
        buildCategorizedSections();
        
        // Setup side panel
        setupSidePanel();
        
        // Back button
        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());
        
        // Cleanup TTS on window close
        if (root != null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                        if (newWin != null) {
                            newWin.setOnCloseRequest(e -> cleanupTTS());
                        }
                    });
                }
            });
        }
        
        updateStats();
    }

    private void initializeBlogPosts() {
        int id = 1;
        for (CategorySection section : sections) {
            for (String category : section.categories) {
                String title = category + " — Understanding";
                String shortIntro = "Comprehensive guide to " + category.toLowerCase();
                Blog blog = new Blog("b" + id, title, shortIntro, category);
                
                // Load full content from file
                String fullContent = BlogContentLoader.loadContentForCategory(category, 
                    "A comprehensive guide to understanding " + category + ".\n\nContent file not found. Please check that the file exists in data/blogs/ directory.");
                blog.setFullDescription(fullContent);
                blog.setSavedForLater(savedBlogsManager.isBlogSaved(soulId, blog.getId()));
                
                // Debug: Print if content loaded successfully
                if (fullContent != null && fullContent.length() > 100) {
                    System.out.println("✓ Loaded: " + category + " (" + fullContent.length() + " chars)");
                } else {
                    System.err.println("✗ Failed to load: " + category);
                }
                
                allPosts.add(blog);
                id++;
            }
        }
        filteredPosts.addAll(allPosts);
    }

    private void buildCategorizedSections() {
        categoriesContainer.getChildren().clear();
        
        for (CategorySection section : sections) {
            VBox sectionBox = new VBox(15);
            sectionBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                              "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
            sectionBox.setMaxWidth(Double.MAX_VALUE);
            
            // Section Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label emoji = new Label(section.emoji);
            emoji.setStyle("-fx-font-size: 24px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
            
            VBox titleBox = new VBox(5);
            Label title = new Label(section.title);
            title.getStyleClass().add("section-header");
            
            Label description = new Label(section.description);
            description.getStyleClass().add("section-description");
            description.setWrapText(true);
            description.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titleBox, Priority.ALWAYS);
            
            titleBox.getChildren().addAll(title, description);
            header.getChildren().addAll(emoji, titleBox);
            
            // Category Pills
            FlowPane pillsPane = new FlowPane();
            pillsPane.setHgap(8);
            pillsPane.setVgap(8);
            pillsPane.setMaxWidth(Double.MAX_VALUE);
            
            for (String category : section.categories) {
                Button pill = createPillButton(category, section.color, section.borderColor);
                pillsPane.getChildren().add(pill);
            }
            
            sectionBox.getChildren().addAll(header, pillsPane);
            categoriesContainer.getChildren().add(sectionBox);
        }
    }

    private Button createPillButton(String category, String bgColor, String borderColor) {
        Button pill = new Button(category);
        pill.getStyleClass().add("pill-button");
        pill.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor + ";");
        pill.setWrapText(true);
        pill.setMaxWidth(Double.MAX_VALUE);
        pill.setMinWidth(100);
        
        // Hover animation
        pill.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), pill);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        pill.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), pill);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        // Click to show blog detail
        pill.setOnAction(e -> {
            System.out.println("Button clicked: " + category);
            Blog blog = findBlogByCategory(category);
            if (blog != null) {
                System.out.println("Found blog, showing detail...");
                showBlogDetail(blog);
            } else {
                System.err.println("ERROR: Blog not found for category: " + category);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Content Not Found");
                alert.setHeaderText("Unable to load article");
                alert.setContentText("The article for '" + category + "' could not be found.");
                alert.showAndWait();
            }
        });
        
        return pill;
    }

    private void setupSidePanel() {
        // Quick Actions
        if (shuffleBtn != null) {
            shuffleBtn.setOnAction(e -> shuffleTopics());
        }
        
        if (listenModeBtn != null) {
            listenModeBtn.setOnAction(e -> {
                if (isSpeaking) {
                    stopSpeaking();
                    listenModeBtn.setText("🎧 Listen Instead");
                } else {
                    showTTSSelectionDialog();
                }
            });
        }
        
        if (expertPickBtn != null) {
            expertPickBtn.setOnAction(e -> filterByExpertPick());
        }
        
        // Filters
        if (sectionFilter != null) {
            List<String> sectionNames = new ArrayList<>();
            sectionNames.add("All Sections");
            for (CategorySection section : sections) {
                sectionNames.add(section.title);
            }
            sectionFilter.setItems(FXCollections.observableArrayList(sectionNames));
            sectionFilter.setOnAction(e -> applyFilters());
        }
        
        if (lengthFilter != null) {
            lengthFilter.setItems(FXCollections.observableArrayList(
                "Any Length", "Quick Read (< 2 min)", "Medium (2-5 min)", "In-Depth (> 5 min)"
            ));
            lengthFilter.setOnAction(e -> applyFilters());
        }
        
        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnAction(e -> clearAllFilters());
        }
        
        // Reading Progress Features
        if (viewHistoryBtn != null) {
            viewHistoryBtn.setOnAction(e -> showReadingHistory());
        }
        
        if (viewSavedBtn != null) {
            viewSavedBtn.setOnAction(e -> showSavedArticles());
        }
        
        // Update progress labels
        updateProgressLabels();
    }
    
    // TTS Helper Methods
    private void showTTSSelectionDialog() {
        System.out.println("Opening TTS dialog with " + allPosts.size() + " articles");
        
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                              "-fx-background-radius: 15; " +
                              "-fx-padding: 25; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("🎧 Select Article to Listen");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e40af;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; " +
                         "-fx-text-fill: #64748b; " +
                         "-fx-font-size: 20px; " +
                         "-fx-cursor: hand; " +
                         "-fx-padding: 5 10;");
        closeBtn.setOnAction(e -> stage.close());
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: #ef4444;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtn.getStyle().replace("-fx-text-fill: #ef4444;", "-fx-text-fill: #64748b;")));
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        
        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search articles...");
        searchField.setStyle("-fx-background-color: #f1f5f9; " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 12; " +
                            "-fx-font-size: 14px; " +
                            "-fx-border-color: #cbd5e1; " +
                            "-fx-border-radius: 10;");
        
        // Articles container
        VBox articlesBox = new VBox(12);
        articlesBox.setStyle("-fx-background-color: transparent;");
        
        ScrollPane scrollPane = new ScrollPane(articlesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; " +
                           "-fx-background: transparent;");
        scrollPane.setPrefHeight(400);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Populate articles
        ObservableList<Blog> displayList = FXCollections.observableArrayList(allPosts);
        for (Blog blog : displayList) {
            VBox card = createArticleCard(blog, stage);
            articlesBox.getChildren().add(card);
        }
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            articlesBox.getChildren().clear();
            String query = newV == null ? "" : newV.toLowerCase().trim();
            
            for (Blog blog : displayList) {
                if (query.isEmpty() || 
                    blog.getTitle().toLowerCase().contains(query) ||
                    blog.getCategory().toLowerCase().contains(query)) {
                    articlesBox.getChildren().add(createArticleCard(blog, stage));
                }
            }
        });
        
        mainContainer.getChildren().addAll(header, searchField, scrollPane);
        mainContainer.setPrefWidth(500);
        
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(mainContainer);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        root.setPadding(new javafx.geometry.Insets(20));
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        
        // Position to right of center
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX() + bounds.getWidth() * 0.6 - 250);
        stage.setY(bounds.getMinY() + bounds.getHeight() * 0.5 - 250);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        stage.show();
    }
    
    private VBox createArticleCard(Blog blog, javafx.stage.Stage parentStage) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 10; " +
                     "-fx-padding: 15; " +
                     "-fx-border-color: #e2e8f0; " +
                     "-fx-border-radius: 10; " +
                     "-fx-border-width: 1; " +
                     "-fx-cursor: hand; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        
        Label title = new Label(blog.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        title.setWrapText(true);
        
        Label category = new Label("📚 " + blog.getCategory());
        category.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        HBox playBox = new HBox(8);
        playBox.setAlignment(Pos.CENTER_LEFT);
        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 14px;");
        Label playText = new Label("Click to play");
        playText.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 12px;");
        playBox.getChildren().addAll(playIcon, playText);
        
        card.getChildren().addAll(title, category, playBox);
        
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace("rgba(0,0,0,0.05)", "rgba(0,0,0,0.15)") +
                         "-fx-background-color: #f8fafc;");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), card);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; " +
                         "-fx-background-radius: 10; " +
                         "-fx-padding: 15; " +
                         "-fx-border-color: #e2e8f0; " +
                         "-fx-border-radius: 10; " +
                         "-fx-border-width: 1; " +
                         "-fx-cursor: hand; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            ScaleTransition st = new ScaleTransition(Duration.millis(100), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        card.setOnMouseClicked(e -> {
            parentStage.close();
            speakBlog(blog);
            System.out.println("User selected: " + blog.getTitle());
        });
        
        return card;
    }
    
    private void speakBlog(Blog blog) {
        if (blog == null || blog.getFullDescription() == null) {
            System.err.println("ERROR: Cannot speak blog - null or no content");
            return;
        }
        
        System.out.println("Starting TTS for: " + blog.getTitle());
        stopSpeaking(); // Stop any currently playing audio
        
        isSpeaking = true;
        if (listenModeBtn != null) {
            listenModeBtn.setText("⏸️ Stop Audio");
        }
        
        // Truncate text to prevent PowerShell command length issues
        String textToSpeak = blog.getTitle() + ". " + blog.getFullDescription();
        if (textToSpeak.length() > 3000) {
            textToSpeak = textToSpeak.substring(0, 3000) + "... Article truncated for audio playback.";
        }
        
        System.out.println("Text to speak length: " + textToSpeak.length() + " chars");
        
        // Save text to temp file to avoid command line length issues
        final String finalText = textToSpeak;
        
        new Thread(() -> {
            try {
                // Create temp file with text
                java.io.File tempFile = java.io.File.createTempFile("tts_", ".txt");
                tempFile.deleteOnExit();
                java.nio.file.Files.write(tempFile.toPath(), finalText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.println("Created temp file: " + tempFile.getAbsolutePath());
                
                // Use PowerShell to read file and speak
                String command = "$text = Get-Content -Path '" + tempFile.getAbsolutePath().replace("\\", "/") + "' -Raw; " +
                               "Add-Type -AssemblyName System.Speech; " +
                               "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                               "$speak.Rate = 1; " +    // Changed from 0 to 1 for slightly faster
                               "$speak.Volume = 100; " + // Maximum volume
                               "Write-Host 'Starting speech...'; " +
                               "$speak.Speak($text); " +
                               "Write-Host 'Speech completed.';";
                
                System.out.println("Executing PowerShell command...");
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", command);
                pb.redirectErrorStream(true);
                
                synchronized (this) {
                    ttsProcess = pb.start();
                }
                
                // Read output for debugging
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(ttsProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("TTS Output: " + line);
                }
                
                int exitCode = ttsProcess.waitFor();
                System.out.println("TTS process finished with exit code: " + exitCode);
                
            } catch (InterruptedException e) {
                // Thread was interrupted - normal stop behavior
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Only show error if it's not a result of forcibly destroying the process
                if (ttsProcess != null && ttsProcess.isAlive()) {
                    javafx.application.Platform.runLater(() -> {
                        showModernError("TTS Error", "Unable to read the article aloud", e.getMessage());
                    });
                }
            } finally {
                synchronized (this) {
                    isSpeaking = false;
                    ttsProcess = null;
                }
                javafx.application.Platform.runLater(() -> {
                    if (listenModeBtn != null) {
                        listenModeBtn.setText("🎧 Listen Instead");
                    }
                });
            }
        }).start();
        
        showModernNowPlaying(blog.getTitle());
    }
    
    private void stopSpeaking() {
        synchronized (this) {
            if (ttsProcess != null && ttsProcess.isAlive()) {
                ttsProcess.destroyForcibly();
                ttsProcess = null;
            }
            isSpeaking = false;
        }
    }
    
    private void cleanupTTS() {
        stopSpeaking();
    }
    
    private void showModernNowPlaying(String articleTitle) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        popup.initModality(javafx.stage.Modality.NONE);
        
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #2563eb); " +
                          "-fx-background-radius: 15; " +
                          "-fx-padding: 20 25; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);");
        container.setPrefWidth(350);
        
        // Animated speaker icon
        Label icon = new Label("🎧");
        icon.setStyle("-fx-font-size: 40px;");
        
        // Pulsing animation for icon
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), icon);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        
        Label title = new Label("Now Playing");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label article = new Label(articleTitle);
        article.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9); -fx-wrap-text: true; -fx-text-alignment: center;");
        article.setWrapText(true);
        article.setMaxWidth(300);
        
        Label instruction = new Label("Click '⏸️ Stop Audio' to stop playback");
        instruction.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7); -fx-wrap-text: true; -fx-text-alignment: center;");
        instruction.setWrapText(true);
        
        // Progress indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setStyle("-fx-progress-color: white;");
        progress.setPrefSize(30, 30);
        
        // Close button
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                         "-fx-text-fill: white; " +
                         "-fx-font-size: 16px; " +
                         "-fx-cursor: hand; " +
                         "-fx-padding: 5 12; " +
                         "-fx-background-radius: 20;");
        closeBtn.setOnAction(e -> {
            pulse.stop();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), container);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> popup.close());
            fadeOut.play();
        });
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtn.getStyle() + "-fx-background-color: rgba(255,255,255,0.3);"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.3);", "-fx-background-color: rgba(255,255,255,0.2);")));
        
        container.getChildren().addAll(icon, title, article, progress, instruction, closeBtn);
        
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        root.setPadding(new javafx.geometry.Insets(15));
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);
        
        // Position to right side of screen
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        popup.setX(bounds.getMaxX() - 400);
        popup.setY(bounds.getMinY() + 100);
        
        // Slide in from right animation
        popup.setOpacity(0);
        popup.show();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Auto-close after 4 seconds
        javafx.animation.PauseTransition autoClose = new javafx.animation.PauseTransition(Duration.seconds(4));
        autoClose.setOnFinished(e -> {
            if (popup.isShowing()) {
                pulse.stop();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> popup.close());
                fadeOut.play();
            }
        });
        autoClose.play();
    }
    
    private void showModernError(String title, String header, String content) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #fee2e2, #fecaca); " +
                          "-fx-background-radius: 15; " +
                          "-fx-padding: 25; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        container.setPrefWidth(400);
        
        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 35px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #991b1b;");
        
        Label headerLabel = new Label(header);
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #b91c1c;");
        
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f1d1d; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(350);
        
        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: #dc2626; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-size: 13px; " +
                      "-fx-font-weight: bold; " +
                      "-fx-cursor: hand; " +
                      "-fx-padding: 10 30; " +
                      "-fx-background-radius: 8;");
        okBtn.setOnAction(e -> popup.close());
        okBtn.setOnMouseEntered(e -> okBtn.setStyle(okBtn.getStyle() + "-fx-background-color: #b91c1c;"));
        okBtn.setOnMouseExited(e -> okBtn.setStyle(okBtn.getStyle().replace("-fx-background-color: #b91c1c;", "-fx-background-color: #dc2626;")));
        
        HBox buttonBox = new HBox(okBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        container.getChildren().addAll(icon, titleLabel, headerLabel, contentLabel, buttonBox);
        
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        root.setPadding(new javafx.geometry.Insets(20));
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);
        popup.centerOnScreen();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        popup.showAndWait();
    }

    private void setupSearch() {
        if (searchBar == null) return;
        
        ContextMenu suggestions = new ContextMenu();
        
        searchBar.textProperty().addListener((obs, oldV, newV) -> {
            suggestions.getItems().clear();
            String query = newV == null ? "" : newV.trim().toLowerCase();
            
            if (query.isEmpty()) {
                suggestions.hide();
                buildCategorizedSections(); // Show all when search is cleared
                return;
            }
            
            if (query.length() < 2) {
                suggestions.hide();
                return;
            }
            
            Set<String> matches = new LinkedHashSet<>();
            
            // Search across all blog attributes
            for (Blog blog : allPosts) {
                if (matches.size() >= 10) break;
                
                String category = blog.getCategory();
                if (category != null && category.toLowerCase().contains(query)) {
                    matches.add(category);
                }
            }
            
            for (String match : matches) {
                MenuItem item = new MenuItem("📄 " + match);
                item.setOnAction(e -> {
                    searchBar.setText(match);
                    performSearch(match);
                    suggestions.hide();
                });
                suggestions.getItems().add(item);
            }
            
            // Add a "Search All" option
            if (!matches.isEmpty()) {
                MenuItem searchAll = new MenuItem("🔍 Search all for '" + query + "'");
                searchAll.setStyle("-fx-font-weight: bold;");
                searchAll.setOnAction(e -> {
                    performSearch(query);
                    suggestions.hide();
                });
                suggestions.getItems().add(0, searchAll);
            }
            
            if (!suggestions.getItems().isEmpty()) {
                suggestions.show(searchBar, javafx.geometry.Side.BOTTOM, 0, 0);
            } else {
                suggestions.hide();
            }
        });
        
        searchBar.setOnAction(e -> {
            performSearch(searchBar.getText());
            suggestions.hide();
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            buildCategorizedSections();
            return;
        }
        
        String q = query.trim().toLowerCase();
        Set<String> matchingCategories = new LinkedHashSet<>();
        
        // Search across title, category, and content
        for (Blog blog : allPosts) {
            boolean matches = false;
            
            // Check category
            if (blog.getCategory() != null && blog.getCategory().toLowerCase().contains(q)) {
                matches = true;
            }
            
            // Check title
            if (blog.getTitle() != null && blog.getTitle().toLowerCase().contains(q)) {
                matches = true;
            }
            
            // Check content
            if (blog.getFullDescription() != null && blog.getFullDescription().toLowerCase().contains(q)) {
                matches = true;
            }
            
            if (matches) {
                matchingCategories.add(blog.getCategory());
            }
        }
        
        if (!matchingCategories.isEmpty()) {
            highlightCategories(new ArrayList<>(matchingCategories));
        } else {
            // Show no results message
            categoriesContainer.getChildren().clear();
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle("-fx-padding: 60px;");
            
            Label emoji = new Label("🔍");
            emoji.setStyle("-fx-font-size: 48px;");
            
            Label message = new Label("No results found for '" + query + "'");
            message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555;");
            
            Label suggestion = new Label("Try different keywords or browse categories below");
            suggestion.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
            
            Button showAllBtn = new Button("Show All Articles");
            showAllBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;");
            showAllBtn.setOnAction(e -> {
                searchBar.clear();
                buildCategorizedSections();
            });
            
            emptyState.getChildren().addAll(emoji, message, suggestion, showAllBtn);
            categoriesContainer.getChildren().add(emptyState);
        }
    }

    private void highlightCategories(List<String> categories) {
        // Rebuild sections highlighting only matching categories
        categoriesContainer.getChildren().clear();
        
        for (CategorySection section : sections) {
            List<String> matchingInSection = section.categories.stream()
                .filter(categories::contains)
                .collect(Collectors.toList());
            
            if (matchingInSection.isEmpty()) continue;
            
            VBox sectionBox = new VBox(15);
            sectionBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                              "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
            
            // Section Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label emoji = new Label(section.emoji);
            emoji.setStyle("-fx-font-size: 28px;");
            
            VBox titleBox = new VBox(5);
            Label title = new Label(section.title);
            title.getStyleClass().add("section-header");
            
            Label description = new Label(section.description);
            description.getStyleClass().add("section-description");
            description.setWrapText(true);
            description.setMaxWidth(700);
            
            titleBox.getChildren().addAll(title, description);
            header.getChildren().addAll(emoji, titleBox);
            
            FlowPane pillsPane = new FlowPane();
            pillsPane.setHgap(10);
            pillsPane.setVgap(10);
            pillsPane.setPrefWrapLength(900);
            
            for (String category : matchingInSection) {
                Button pill = createPillButton(category, section.color, section.borderColor);
                pillsPane.getChildren().add(pill);
            }
            
            sectionBox.getChildren().addAll(header, pillsPane);
            categoriesContainer.getChildren().add(sectionBox);
        }
    }

    private void shuffleTopics() {
        // Topic Shuffle - randomly select 8-12 articles from different categories for discovery
        categoriesContainer.getChildren().clear();
        
        // Create a shuffled list of all blogs
        List<Blog> shuffledBlogs = new ArrayList<>(allPosts);
        Collections.shuffle(shuffledBlogs);
        
        // Take 8-12 random articles (or all if fewer)
        int displayCount = Math.min(shuffledBlogs.size(), 8 + (int)(Math.random() * 5));
        List<Blog> selectedBlogs = shuffledBlogs.subList(0, displayCount);
        
        // Create a special shuffle section
        VBox shuffleBox = new VBox(20);
        shuffleBox.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                          "-fx-background-radius: 15; -fx-padding: 30; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 3);");
        
        // Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        
        Label emoji = new Label("🎲");
        emoji.setStyle("-fx-font-size: 42px;");
        
        Label title = new Label("Topic Shuffle: Discover Something New");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("Randomly selected " + displayCount + " articles from different categories to expand your understanding");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9); -fx-wrap-text: true;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(700);
        
        Button reshuffleBtn = new Button("🔄 Shuffle Again");
        reshuffleBtn.setStyle("-fx-background-color: white; -fx-text-fill: #667eea; " +
                            "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 20; " +
                            "-fx-cursor: hand; -fx-font-weight: bold;");
        reshuffleBtn.setOnAction(e -> shuffleTopics());
        
        headerBox.getChildren().addAll(emoji, title, subtitle, reshuffleBtn);
        
        // Pills container
        FlowPane pillsPane = new FlowPane();
        pillsPane.setHgap(12);
        pillsPane.setVgap(12);
        pillsPane.setPrefWrapLength(900);
        pillsPane.setAlignment(Pos.CENTER);
        
        for (Blog blog : selectedBlogs) {
            // Find the section this blog belongs to for color coding
            CategorySection matchingSection = null;
            for (CategorySection section : sections) {
                if (section.categories.contains(blog.getCategory())) {
                    matchingSection = section;
                    break;
                }
            }
            
            if (matchingSection != null) {
                Button pill = createPillButton(blog.getCategory(), matchingSection.color, matchingSection.borderColor);
                // Add category badge
                pill.setText(matchingSection.emoji + " " + blog.getCategory());
                pillsPane.getChildren().add(pill);
            }
        }
        
        shuffleBox.getChildren().addAll(headerBox, pillsPane);
        categoriesContainer.getChildren().add(shuffleBox);
        
        // Add a tip section
        VBox tipBox = new VBox(10);
        tipBox.setStyle("-fx-background-color: #fff9e6; -fx-background-radius: 10; " +
                       "-fx-padding: 20; -fx-border-color: #ffd700; -fx-border-width: 2; " +
                       "-fx-border-radius: 10;");
        tipBox.setAlignment(Pos.CENTER);
        
        Label tipIcon = new Label("💡");
        tipIcon.setStyle("-fx-font-size: 28px;");
        
        Label tipText = new Label("Discovery Tip: Exploring diverse topics can provide unexpected insights and coping strategies!");
        tipText.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-wrap-text: true; -fx-text-alignment: center;");
        tipText.setWrapText(true);
        tipText.setMaxWidth(600);
        
        tipBox.getChildren().addAll(tipIcon, tipText);
        categoriesContainer.getChildren().add(tipBox);
        
        System.out.println("Topic Shuffle: Displaying " + displayCount + " random articles");
    }

    private void filterByExpertPick() {
        // Show recommended articles
        List<String> expertPicks = Arrays.asList(
            "Depression", "Anxiety Disorders", "Burnout & Stress", 
            "Self-Esteem & Confidence", "Academic Stress"
        );
        highlightCategories(expertPicks);
    }

    private void applyFilters() {
        // Start with all blogs
        List<Blog> matchingBlogs = new ArrayList<>(allPosts);
        
        // Apply section filter
        if (sectionFilter != null && sectionFilter.getValue() != null && 
            !sectionFilter.getValue().equals("All Sections")) {
            String selectedSection = sectionFilter.getValue();
            // Find which categories belong to this section
            List<String> sectionCategories = new ArrayList<>();
            for (CategorySection section : sections) {
                if (section.title.equals(selectedSection)) {
                    sectionCategories.addAll(section.categories);
                    break;
                }
            }
            matchingBlogs = matchingBlogs.stream()
                .filter(blog -> sectionCategories.contains(blog.getCategory()))
                .collect(Collectors.toList());
        }
        
        // Apply length filter
        if (lengthFilter != null && lengthFilter.getValue() != null && 
            !lengthFilter.getValue().equals("Any Length")) {
            String lengthValue = lengthFilter.getValue();
            matchingBlogs = matchingBlogs.stream()
                .filter(blog -> matchesLengthFilter(blog, lengthValue))
                .collect(Collectors.toList());
        }
        
        // Convert blogs to categories
        List<String> matchingCategories = matchingBlogs.stream()
            .map(Blog::getCategory)
            .distinct()
            .collect(Collectors.toList());
        
        // Display results
        if (!matchingCategories.isEmpty()) {
            highlightCategories(matchingCategories);
        } else {
            // Show empty state
            categoriesContainer.getChildren().clear();
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle("-fx-padding: 60px;");
            
            Label emoji = new Label("🔍");
            emoji.setStyle("-fx-font-size: 48px;");
            
            Label message = new Label("No articles match your filters");
            message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555;");
            
            Label suggestion = new Label("Try adjusting your filter selections");
            suggestion.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
            
            Button clearBtn = new Button("Clear All Filters");
            clearBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;");
            clearBtn.setOnAction(e -> clearAllFilters());
            
            emptyState.getChildren().addAll(emoji, message, suggestion, clearBtn);
            categoriesContainer.getChildren().add(emptyState);
        }
        
        updateStats();
    }
    
    private boolean matchesLengthFilter(Blog blog, String lengthFilter) {
        if (blog == null || blog.getFullDescription() == null) return false;
        
        String content = blog.getFullDescription();
        int wordCount = content.split("\\s+").length;
        int readingTimeMinutes = wordCount / 200; // Average reading speed: 200 words/min
        
        switch (lengthFilter) {
            case "Quick Read (< 2 min)":
                return readingTimeMinutes < 2;
            case "Medium (2-5 min)":
                return readingTimeMinutes >= 2 && readingTimeMinutes <= 5;
            case "In-Depth (> 5 min)":
                return readingTimeMinutes > 5;
            default:
                return true;
        }
    }

    private void clearAllFilters() {
        if (sectionFilter != null) sectionFilter.setValue(null);
        if (lengthFilter != null) lengthFilter.setValue(null);
        if (searchBar != null) searchBar.clear();
        
        buildCategorizedSections();
        updateStats();
    }
    
    private void updateProgressLabels() {
        // Update reading progress
        if (progressLabel != null) {
            int readCount = viewedArticlesToday.size();
            progressLabel.setText(readCount + (readCount == 1 ? " read today" : " read today"));
        }
        
        // Update saved count - refresh from SavedBlogsManager
        if (savedCountLabel != null) {
            int savedCount = 0;
            for (Blog blog : allPosts) {
                if (savedBlogsManager.isBlogSaved(soulId, blog.getId())) {
                    savedCount++;
                }
            }
            savedCountLabel.setText(savedCount + (savedCount == 1 ? " saved" : " saved"));
        }
    }
    
    private void showReadingHistory() {
        if (readingHistory.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📚 Reading History");
            alert.setHeaderText("No Reading History Yet");
            alert.setContentText("Start reading articles to build your reading history!\n\nYour history will show:\n• Articles you've viewed\n• Reading patterns\n• Most visited topics");
            alert.showAndWait();
        } else {
            // Show dialog with reading history
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("📚 Reading History");
            dialog.setHeaderText("Articles You've Read Today: " + viewedArticlesToday.size());
            
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            Label info = new Label("Recently viewed articles:");
            info.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            content.getChildren().add(info);
            
            // Show up to 10 most recent
            int displayCount = Math.min(10, readingHistory.size());
            for (int i = 0; i < displayCount; i++) {
                String category = readingHistory.get(i);
                Label item = new Label((i + 1) + ". " + category);
                item.setStyle("-fx-font-size: 12px; -fx-padding: 4 0;");
                content.getChildren().add(item);
            }
            
            Button viewAllBtn = new Button("🔍 Show All Articles from History");
            viewAllBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;");
            viewAllBtn.setOnAction(e -> {
                highlightCategories(readingHistory);
                dialog.close();
            });
            content.getChildren().add(viewAllBtn);
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        }
    }
    
    private void showSavedArticles() {
        // Refresh saved status from SavedBlogsManager
        for (Blog blog : allPosts) {
            blog.setSavedForLater(savedBlogsManager.isBlogSaved(soulId, blog.getId()));
        }
        
        List<String> savedCategories = new ArrayList<>();
        for (Blog blog : allPosts) {
            if (blog.isSavedForLater()) {
                savedCategories.add(blog.getCategory());
            }
        }
        
        updateProgressLabels(); // Update the count display
        
        if (savedCategories.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("⭐ Saved Articles");
            alert.setHeaderText("No Saved Articles Yet");
            alert.setContentText("Start saving articles by clicking the 'Save for Later' button when reading!");
            alert.showAndWait();
        } else {
            highlightCategories(savedCategories);
            if (searchBar != null) searchBar.setPromptText("Showing " + savedCategories.size() + " saved articles...");
        }
    }

    private void updateStats() {
        if (statsLabel != null) {
            int total = allPosts.size();
            statsLabel.setText(total + " articles available across " + sections.size() + " categories");
        }
    }

    private Blog findBlogByCategory(String category) {
        for (Blog blog : allPosts) {
            if (category.equals(blog.getCategory())) {
                return blog;
            }
        }
        return null;
    }

    private void showBlogDetail(Blog blog) {
        try {
            // Track reading history in memory
            viewedArticlesToday.add(blog.getId());
            if (!readingHistory.contains(blog.getCategory())) {
                readingHistory.add(0, blog.getCategory()); // Add to beginning
            }
            
            // Save to database for persistence
            try {
                if (blogHistoryRepo != null) {
                    blogHistoryRepo.recordBlogView(soulId, blog.getId(), blog.getCategory());
                }
            } catch (Exception e) {
                System.err.println("Failed to record blog view: " + e.getMessage());
            }
            
            updateProgressLabels();
            
            System.out.println("=== showBlogDetail START ===");
            System.out.println("Blog title: " + blog.getTitle());
            System.out.println("Blog category: " + blog.getCategory());
            System.out.println("Blog content length: " + (blog.getFullDescription() != null ? blog.getFullDescription().length() : "NULL"));
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/blog_detail.fxml"));
            Parent detail = loader.load();
            System.out.println("✓ FXML loaded successfully");
            
            BlogDetailController controller = loader.getController();
            System.out.println("✓ Controller obtained: " + (controller != null));
            
            controller.setBlog(blog);
            System.out.println("✓ Blog set in controller");

            javafx.scene.layout.StackPane popup = new javafx.scene.layout.StackPane();
            popup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);"); // Softer overlay
            popup.setPickOnBounds(true);
            popup.setMouseTransparent(false);
            popup.getChildren().add(detail);
            javafx.scene.layout.StackPane.setAlignment(detail, Pos.CENTER);
            System.out.println("✓ Popup created");

            if (detail instanceof javafx.scene.layout.Region && root != null) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) detail;
                region.setPrefWidth(root.getWidth() * 0.55);  // Better proportions
                region.setPrefHeight(root.getHeight() * 0.80); // More breathing room
                region.setMaxWidth(900);  // Cap maximum width for better readability
                region.setMinWidth(500);  // Ensure minimum readable width
                region.setMinHeight(550); 
                region.setStyle("-fx-background-color: #fafbfc; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.25), 40, 0.3, 0, 8);");
                region.setVisible(true);
                region.setManaged(true);
                System.out.println("✓ Region sized: " + region.getPrefWidth() + "x" + region.getPrefHeight());
            }

            if (root != null && root.getScene() != null) {
                javafx.scene.Scene scene = root.getScene();
                javafx.scene.Parent sceneRoot = scene.getRoot();
                System.out.println("✓ Scene root type: " + sceneRoot.getClass().getSimpleName());
                
                // If root is NOT already a StackPane, we need to wrap it
                if (!(sceneRoot instanceof javafx.scene.layout.StackPane)) {
                    System.out.println("⚠ Scene root is not StackPane, wrapping it...");
                    javafx.scene.layout.StackPane newRoot = new javafx.scene.layout.StackPane();
                    newRoot.getChildren().add(sceneRoot);
                    scene.setRoot(newRoot);
                    sceneRoot = newRoot;
                    System.out.println("✓ Wrapped with StackPane");
                }
                
                javafx.scene.layout.StackPane stackRoot = (javafx.scene.layout.StackPane) sceneRoot;
                
                // Set popup to fill the entire scene
                popup.prefWidthProperty().bind(scene.widthProperty());
                popup.prefHeightProperty().bind(scene.heightProperty());
                
                // Add popup on top of everything
                stackRoot.getChildren().add(popup);
                popup.toFront();
                
                System.out.println("✓ Popup added to StackPane root, children count: " + stackRoot.getChildren().size());
                System.out.println("✓ Popup size: " + scene.getWidth() + "x" + scene.getHeight());
                
                // Start with invisible popup for fade-in effect
                popup.setOpacity(0.0);
                popup.setVisible(true);
                
                // Fade in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popup);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                // Force layout
                javafx.application.Platform.runLater(() -> {
                    popup.requestFocus();
                    popup.toFront();
                    System.out.println("✓ Popup bounds after layout: " + popup.getBoundsInParent());
                });
            } else {
                System.err.println("ERROR: No valid scene found for popup!");
            }
            
            // Click background to close
            popup.setOnMouseClicked(e -> {
                if (e.getTarget() == popup) {
                    System.out.println("Background clicked - closing popup (feature disabled - use X button)");
                }
            });

            final boolean[] isClosing = {false};
            controller.setOnClose(() -> {
                if (isClosing[0]) return;
                isClosing[0] = true;
                System.out.println("Closing popup...");
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), popup);
                fadeOut.setFromValue(popup.getOpacity());
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    javafx.application.Platform.runLater(() -> {
                        if (root != null && root.getScene() != null && root.getScene().getRoot() instanceof javafx.scene.layout.Pane) {
                            javafx.scene.layout.Pane container = (javafx.scene.layout.Pane) root.getScene().getRoot();
                            if (container.getChildren().contains(popup)) container.getChildren().remove(popup);
                        } else if (root != null && root.getChildren().contains(popup)) {
                            root.getChildren().remove(popup);
                        }
                    });
                });
                fadeOut.play();
            });
            
            controller.setOnSave(() -> {
                if (soulId == null || soulId.isBlank()) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Please login to save blogs.", ButtonType.OK);
                    a.setHeaderText("Sign in required");
                    a.showAndWait();
                    blog.setSavedForLater(false);
                    controller.setBlog(blog);
                } else {
                    if (blog.isSavedForLater()) {
                        savedBlogsManager.saveBlog(soulId, blog);
                    } else {
                        savedBlogsManager.removeSavedBlog(soulId, blog.getId());
                    }
                }
            });
            
            System.out.println("=== showBlogDetail END - SUCCESS ===");

        } catch (Exception e) {
            System.err.println("=== showBlogDetail ERROR ===");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to show blog detail");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goBackToDashboard() {
        cleanupTTS(); // Stop any playing audio before leaving
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(this.soulId, "");
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
