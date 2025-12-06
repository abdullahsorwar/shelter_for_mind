package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
    @FXML private ComboBox<String> disorderFilter;
    @FXML private ComboBox<String> emotionFilter;
    @FXML private ComboBox<String> lengthFilter;
    @FXML private ComboBox<String> moodFilter;
    @FXML private Button clearFiltersBtn;
    @FXML private Label statsLabel;

    private String soulId = "";
    private SavedBlogsManager savedBlogsManager;
    private final ObservableList<Blog> allPosts = FXCollections.observableArrayList();
    private final List<Blog> filteredPosts = new ArrayList<>();
    
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
        if (savedBlogsManager != null && !allPosts.isEmpty()) {
            for (Blog b : allPosts) {
                b.setSavedForLater(savedBlogsManager.isBlogSaved(this.soulId, b.getId()));
            }
        }
    }

    @FXML
    public void initialize() {
        if (pageTitle != null) pageTitle.setText("Mental Wellness Library 🧠");
        
        savedBlogsManager = new SavedBlogsManager();
        
        // Initialize all blog posts
        initializeBlogPosts();
        
        // Build categorized UI
        buildCategorizedSections();
        
        // Setup side panel
        setupSidePanel();
        
        // Setup search
        setupSearch();
        
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
            
            // Category Pills
            FlowPane pillsPane = new FlowPane();
            pillsPane.setHgap(10);
            pillsPane.setVgap(10);
            pillsPane.setPrefWrapLength(900);
            
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
        if (disorderFilter != null) {
            List<String> allCategories = new ArrayList<>();
            allCategories.add("All Categories");
            for (CategorySection section : sections) {
                allCategories.addAll(section.categories);
            }
            disorderFilter.setItems(FXCollections.observableArrayList(allCategories));
            disorderFilter.setOnAction(e -> applyFilters());
        }
        
        if (emotionFilter != null) {
            emotionFilter.setItems(FXCollections.observableArrayList(
                "Any Emotion", "Anxious", "Sad", "Stressed", "Overwhelmed", "Lonely", "Confused", "Hopeful"
            ));
            emotionFilter.setOnAction(e -> applyFilters());
        }
        
        if (lengthFilter != null) {
            lengthFilter.setItems(FXCollections.observableArrayList(
                "Any Length", "Quick Read (< 2 min)", "Medium (2-5 min)", "In-Depth (> 5 min)"
            ));
            lengthFilter.setOnAction(e -> applyFilters());
        }
        
        if (moodFilter != null) {
            moodFilter.setItems(FXCollections.observableArrayList(
                "Current Mood", "Need Immediate Help", "Learning & Exploring", "Crisis Support", "Daily Management"
            ));
            moodFilter.setOnAction(e -> applyFilters());
        }
        
        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnAction(e -> clearAllFilters());
        }
    }
    
    // TTS Helper Methods
    private void showTTSSelectionDialog() {
        System.out.println("Opening TTS dialog with " + allPosts.size() + " articles");
        Dialog<Blog> dialog = new Dialog<>();
        dialog.setTitle("🎧 Text-to-Speech");
        dialog.setHeaderText("Select an article to listen to:");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        ListView<Blog> listView = new ListView<>();
        listView.setItems(allPosts);
        listView.setCellFactory(lv -> new ListCell<Blog>() {
            @Override
            protected void updateItem(Blog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Choose an article from the list below:"),
            listView
        );
        content.setPrefHeight(400);
        listView.setPrefHeight(350);
        
        dialogPane.setContent(content);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && listView.getSelectionModel().getSelectedItem() != null) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<Blog> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("User selected: " + result.get().getTitle());
            speakBlog(result.get());
        } else {
            System.out.println("User cancelled TTS dialog");
        }
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
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("TTS Error");
                    alert.setHeaderText("Text-to-Speech Failed");
                    alert.setContentText("Unable to read the article aloud. Error: " + e.getMessage());
                    alert.showAndWait();
                });
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
        
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("🎧 Now Playing");
        info.setHeaderText("Reading: " + blog.getTitle());
        info.setContentText("Audio playback has started. Click the '⏸️ Stop Audio' button to stop.");
        info.show();
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

    private void setupSearch() {
        if (searchBar == null) return;
        
        ContextMenu suggestions = new ContextMenu();
        
        searchBar.textProperty().addListener((obs, oldV, newV) -> {
            suggestions.getItems().clear();
            String query = newV == null ? "" : newV.trim().toLowerCase();
            
            if (query.isEmpty()) {
                suggestions.hide();
                return;
            }
            
            Set<String> matches = new LinkedHashSet<>();
            
            // Search categories
            for (CategorySection section : sections) {
                for (String cat : section.categories) {
                    if (cat.toLowerCase().contains(query)) {
                        matches.add(cat);
                        if (matches.size() >= 8) break;
                    }
                }
                if (matches.size() >= 8) break;
            }
            
            for (String match : matches) {
                MenuItem item = new MenuItem(match);
                item.setOnAction(e -> {
                    searchBar.setText(match);
                    performSearch(match);
                    suggestions.hide();
                });
                suggestions.getItems().add(item);
            }
            
            if (!suggestions.getItems().isEmpty()) {
                suggestions.show(searchBar, javafx.geometry.Side.BOTTOM, 0, 0);
            } else {
                suggestions.hide();
            }
        });
        
        searchBar.setOnAction(e -> performSearch(searchBar.getText()));
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            buildCategorizedSections();
            return;
        }
        
        String q = query.trim().toLowerCase();
        List<String> matchingCategories = new ArrayList<>();
        
        for (CategorySection section : sections) {
            for (String category : section.categories) {
                if (category.toLowerCase().contains(q)) {
                    matchingCategories.add(category);
                }
            }
        }
        
        if (!matchingCategories.isEmpty()) {
            highlightCategories(matchingCategories);
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
        List<String> matchingCategories = new ArrayList<>();
        
        // Category filter
        if (disorderFilter != null && disorderFilter.getValue() != null && 
            !disorderFilter.getValue().equals("All Categories")) {
            matchingCategories.add(disorderFilter.getValue());
        } else {
            // If no category filter, show all
            for (CategorySection section : sections) {
                matchingCategories.addAll(section.categories);
            }
        }
        
        // Apply length filter
        if (lengthFilter != null && lengthFilter.getValue() != null && 
            !lengthFilter.getValue().equals("Any Length")) {
            String lengthValue = lengthFilter.getValue();
            List<String> filteredByLength = new ArrayList<>();
            
            for (String category : matchingCategories) {
                Blog blog = findBlogByCategory(category);
                if (blog != null && matchesLengthFilter(blog, lengthValue)) {
                    filteredByLength.add(category);
                }
            }
            matchingCategories = filteredByLength;
        }
        
        // Apply other filters as needed
        if (!matchingCategories.isEmpty()) {
            highlightCategories(matchingCategories);
        } else {
            // Show empty state
            categoriesContainer.getChildren().clear();
            Label noResults = new Label("No articles match your filters. Try adjusting your selection.");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 40px;");
            categoriesContainer.getChildren().add(noResults);
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
        if (disorderFilter != null) disorderFilter.setValue(null);
        if (emotionFilter != null) emotionFilter.setValue(null);
        if (lengthFilter != null) lengthFilter.setValue(null);
        if (moodFilter != null) moodFilter.setValue(null);
        if (searchBar != null) searchBar.clear();
        
        buildCategorizedSections();
        updateStats();
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
            popup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
            popup.setPickOnBounds(true);
            popup.setMouseTransparent(false); // Ensure it receives mouse events
            popup.getChildren().add(detail);
            javafx.scene.layout.StackPane.setAlignment(detail, Pos.CENTER);
            System.out.println("✓ Popup created");

            if (detail instanceof javafx.scene.layout.Region && root != null) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) detail;
                region.setPrefWidth(root.getWidth() * 0.6);  // Increased from 0.45 to 0.6
                region.setPrefHeight(root.getHeight() * 0.9); // Increased from 0.85 to 0.9
                region.setMinWidth(400);  // Increased from 300
                region.setMinHeight(600); // Increased from 550
                region.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);");
                region.setVisible(true);
                region.setManaged(true);
                System.out.println("✓ Region sized: " + region.getPrefWidth() + "x" + region.getPrefHeight());
                System.out.println("✓ Region style: " + region.getStyle());
                System.out.println("✓ Region visible: " + region.isVisible() + ", managed: " + region.isManaged());
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
                
                // Make fully visible
                popup.setOpacity(1.0);
                popup.setVisible(true);
                
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
