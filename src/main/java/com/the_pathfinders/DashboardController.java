package com.the_pathfinders;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;

import com.the_pathfinders.db.MoodTrackerRepository;
import com.the_pathfinders.db.SoulRepository;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.chart.PieChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class DashboardController {

    @FXML private AnchorPane root;
    @FXML private Label userLabel;
    @FXML private ImageView userImage;
    @FXML private ImageView logoImage;
    @FXML private VBox userDropdown;
    @FXML private Rectangle bgRect;
    @FXML private Rectangle vignetteRect;
    @FXML private Label greetingLabel;
    @FXML private ToggleButton musicToggle;
    
    @FXML private Button journalBtn;
    @FXML private Button blogBtn;
    @FXML private Button moodBtn;
    @FXML private Button insightsBtn;
    @FXML private Button SocialWorkBtn;
    @FXML private Button logoutBtn;
    @FXML private Button tranquilCornerBtn;
    @FXML private Button moodTrackerBtn;
    @FXML private VBox buttonCardsBox;

    // Mood Tracker popup elements
    @FXML private StackPane moodTrackerOverlay;
    @FXML private VBox moodTrackerContentBox;
    @FXML private VBox moodResultsBox;
    @FXML private Button moodBackBtn;
    @FXML private Button moodCloseBtn;
    @FXML private Button moodPrevBtn;
    @FXML private Button moodNextBtn;
    @FXML private Button moodSubmitBtn;
    @FXML private Button moodDoneBtn;
    @FXML private javafx.scene.shape.Circle progress1, progress2, progress3, progress4, progress5;
    @FXML private ScrollPane moodScrollPane;
    @FXML private VBox moodQuestionsContainer;
    @FXML private javafx.scene.chart.PieChart moodPieChart;
    @FXML private Label moodSummaryLabel;
    @FXML private Label moodScoreLabel;

    // Journaling popup overlay elements
    @FXML private StackPane journalingOverlay;
    @FXML private VBox journalingContentBox;
    @FXML private ImageView journalingIcon;
    @FXML private Label journalingTitle;
    @FXML private Label journalingSubtitle;
    @FXML private VBox journalingButtonsBox;
    @FXML private Button createJournalBtn;
    @FXML private Button viewJournalsBtn;
    @FXML private StackPane tranquilOverlay;
    @FXML private VBox tranquilContentBox;
    @FXML private Button meditationBtn;
    @FXML private Button calmActivitiesBtn;
    @FXML private Button pomodoroBtn;
    @FXML private Button tranquilBackBtn;




    private String soulId;
    private double dragStartX = 0;

    // Mood tracker state
    private int currentMoodQuestion = 0;
    private List<javafx.scene.shape.Circle> progressCircles;
    private List<QuestionData> moodQuestions;
    private Map<String, String> moodAnswers;
    private MoodTrackerRepository moodRepository;
    private FadeTransition moodTrackerFadeTransition; // Track the current animation

    private static class QuestionData {
        String question;
        String category;
        List<String> options;
        int[] scores;

        QuestionData(String question, String category, List<String> options, int[] scores) {
            this.question = question;
            this.category = category;
            this.options = options;
            this.scores = scores;
        }
    }

    public void initialize() {
        setLogo();
        
        // Ensure user image is visible and clickable
        if (userImage != null) {
            userImage.setVisible(true);
            userImage.setOpacity(1.0);
            userImage.setOnMouseClicked(e -> toggleUserMenu());
            // Set default image if not already set
            if (userImage.getImage() == null) {
                try {
                    URL u = getClass().getResource("/assets/icons/user.png");
                    if (u != null) {
                        Image img = new Image(u.toExternalForm(), 50, 50, true, true);
                        userImage.setImage(img);
                    }
                } catch (Exception ignored) {}
            }
        }
        if (userLabel != null) {
            userLabel.setVisible(true);
            userLabel.setOpacity(1.0);
        }
        
        try {
            if (greetingLabel != null) {
                updateGreeting();
            }
        } catch (Exception ignored) {}

        if (musicToggle != null) {
            musicToggle.setSelected(MusicManager.isBackgroundMusicEnabled());
            updateMusicToggleText();
            musicToggle.setOnAction(e -> handleMusicToggle());
        }

        // Initialize journaling popup
        if (journalingIcon != null) {
            try {
                URL iconUrl = getClass().getResource("/assets/icons/ques.png");
                if (iconUrl != null) {
                    journalingIcon.setImage(new Image(iconUrl.toExternalForm()));
                }
            } catch (Exception ignored) {}
        }
        
        // Replace context menu with popup overlay
        if (journalBtn != null) {
            journalBtn.setOnAction(e -> showJournalingPopup());
        }
        
        // Setup popup button handlers
        if (createJournalBtn != null) createJournalBtn.setOnAction(e -> onCreateJournal());
        if (viewJournalsBtn != null) viewJournalsBtn.setOnAction(e -> onViewJournals());
        
        // Click outside popup to close
        if (journalingOverlay != null) {
            journalingOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == journalingOverlay) {
                    hideJournalingPopup();
                }
            });
        }
        if (blogBtn != null) blogBtn.setOnAction(e -> openBlogs());
        if (moodBtn != null) moodBtn.setOnAction(e -> openToDo());
        if (moodTrackerBtn != null) moodTrackerBtn.setOnAction(e -> {
            System.out.println("Mood tracker button clicked!");
            showMoodTrackerPopup();
        });

        // Initialize mood tracker popup
        initializeMoodTracker();

        // ─── Tranquil Corner Popup Setup ───────────────────
        System.out.println("Setting up tranquil corner buttons...");
        System.out.println("meditationBtn: " + meditationBtn);
        System.out.println("calmActivitiesBtn: " + calmActivitiesBtn);
        System.out.println("pomodoroBtn: " + pomodoroBtn);
        
if (tranquilCornerBtn != null) {
    tranquilCornerBtn.setOnAction(e -> showTranquilPopup());
}

if (tranquilBackBtn != null) {
    tranquilBackBtn.setOnAction(e -> hideTranquilPopup());
}

if (meditationBtn != null) {
    meditationBtn.setOnAction(e -> {
        System.out.println("Meditation button clicked!");
        try {
            MeditationController.setSoulId(this.soulId);
            loadPage("/com/the_pathfinders/fxml/Meditation.fxml");
        } catch (Exception ex) {
            System.err.println("Error loading Meditation: " + ex.getMessage());
            ex.printStackTrace();
        }
    });
}
if (calmActivitiesBtn != null) {
    calmActivitiesBtn.setOnAction(e -> {
        System.out.println("Calm Activities button clicked!");
        try {
            CalmActivitiesController.setSoulId(this.soulId);
            loadPage("/com/the_pathfinders/fxml/CalmActivities.fxml");
        } catch (Exception ex) {
            System.err.println("Error loading Calm Activities: " + ex.getMessage());
            ex.printStackTrace();
        }
    });
}
if (pomodoroBtn != null) {
    pomodoroBtn.setOnAction(e -> {
        System.out.println("Pomodoro button clicked!");
        try {
            PomodoroController.setSoulId(this.soulId);
            loadPage("/com/the_pathfinders/fxml/Pomodoro.fxml");
        } catch (Exception ex) {
            System.err.println("Error loading Pomodoro: " + ex.getMessage());
            ex.printStackTrace();
        }
    });
}



        if (insightsBtn != null) insightsBtn.setOnAction(e -> openSeekHelp());
       // if (insightsBtn != null) insightsBtn.setOnAction(e -> openToDo());

        if (SocialWorkBtn != null) SocialWorkBtn.setOnAction(e -> openSocialWork());
        if (logoutBtn != null) logoutBtn.setOnAction(e -> onLogout());

        // Animate buttons with staggered pop-up effect
        animateButtonsPopup();

        if (buttonCardsBox != null) {
            buttonCardsBox.getChildren().forEach(row -> {
                // Add smooth hover animations to each button in the row
                if (row instanceof javafx.scene.layout.HBox hbox) {
                    hbox.getChildren().forEach(btn -> {
                        if (btn instanceof Button) {
                            setupButtonHoverAnimation((Button) btn);
                        }
                    });
                }
                
                row.setOnMousePressed(e -> dragStartX = e.getSceneX());
                row.setOnMouseDragged(e -> {
                    double dragDelta = e.getSceneX() - dragStartX;
                    if (Math.abs(dragDelta) > 20) {
                        int currentIdx = buttonCardsBox.getChildren().indexOf(row);
                        if (dragDelta > 20 && currentIdx < buttonCardsBox.getChildren().size() - 1) {
                            swapButtons(currentIdx, currentIdx + 1);
                            dragStartX = e.getSceneX();
                        } else if (dragDelta < -20 && currentIdx > 0) {
                            swapButtons(currentIdx, currentIdx - 1);
                            dragStartX = e.getSceneX();
                        }
                    }
                });
            });
        }

        // Setup vignette gradient effect
        if (vignetteRect != null) {
            vignetteRect.heightProperty().bind(root.heightProperty());
            
            // Create linear gradient from left (40% opacity) to right (0% opacity)
            Stop[] stops = new Stop[] {
                new Stop(0.0, Color.rgb(0, 0, 0, 0.4)),
                new Stop(0.5, Color.rgb(0, 0, 0, 0.2)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.0))
            };
            
            LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, // startX, startY, endX, endY (0,0 to 1,0 = left to right)
                true, // proportional
                CycleMethod.NO_CYCLE,
                stops
            );
            
            vignetteRect.setFill(gradient);
        }

        try {
            if (bgRect != null) {
                bgRect.widthProperty().bind(root.widthProperty());
                bgRect.heightProperty().bind(root.heightProperty());
                Color[] colors = new Color[] { Color.web("#E9D5FF"), Color.web("#D1FAE5"), Color.web("#FFE7D9") };
                
                // Start with first color
                bgRect.setFill(colors[0]);
                
                // Create smooth infinite loop through all colors
                final int[] currentIndex = {0};
                FillTransition[] ft = {null};
                
                ft[0] = new FillTransition(Duration.seconds(6), bgRect);
                ft[0].setFromValue(colors[0]);
                ft[0].setToValue(colors[1]);
                
                ft[0].setOnFinished(e -> {
                    currentIndex[0] = (currentIndex[0] + 1) % colors.length;
                    int nextIndex = (currentIndex[0] + 1) % colors.length;
                    
                    ft[0].setFromValue(colors[currentIndex[0]]);
                    ft[0].setToValue(colors[nextIndex]);
                    ft[0].playFromStart();
                });
                
                ft[0].play();
            }
        } catch (Exception ignored) {}
    }

    private void swapButtons(int idx1, int idx2) {
        if (buttonCardsBox == null) return;
        javafx.scene.Node btn1 = buttonCardsBox.getChildren().get(idx1);
        javafx.scene.Node btn2 = buttonCardsBox.getChildren().get(idx2);
        
        TranslateTransition tt1 = new TranslateTransition(Duration.millis(300), btn1);
        tt1.setByX(btn2.getLayoutBounds().getWidth() + 16);
        
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(300), btn2);
        tt2.setByX(-(btn1.getLayoutBounds().getWidth() + 16));
        
        tt1.play();
        tt2.play();
        
        tt1.setOnFinished(e -> {
            buttonCardsBox.getChildren().remove(btn1);
            buttonCardsBox.getChildren().add(idx2, btn1);
            btn1.setTranslateX(0);
            btn2.setTranslateX(0);
        });
    }

    private void setupButtonHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            Timeline hoverIn = new Timeline(
                new KeyFrame(Duration.millis(300),
                    new KeyValue(button.scaleXProperty(), 1.05),
                    new KeyValue(button.scaleYProperty(), 1.05)
                )
            );
            hoverIn.play();
        });
        
        button.setOnMouseExited(e -> {
            Timeline hoverOut = new Timeline(
                new KeyFrame(Duration.millis(300),
                    new KeyValue(button.scaleXProperty(), 1.0),
                    new KeyValue(button.scaleYProperty(), 1.0)
                )
            );
            hoverOut.play();
        });
    }
    private void openSocialWork() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/SocialWork.fxml"));
            Parent p = loader.load();

            SocialWorkController controller = loader.getController();
            if (controller != null) {
                controller.setSoulId(this.soulId == null ? "" : this.soulId);
            }

            root.getScene().setRoot(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void showTranquilPopup() {
    if (tranquilOverlay == null) return;

    tranquilOverlay.setVisible(true);
    tranquilOverlay.setManaged(true);
    tranquilOverlay.setOpacity(0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tranquilOverlay);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);
    fadeIn.play();
}

private void hideTranquilPopup() {
    if (tranquilOverlay == null) return;

    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), tranquilOverlay);
    fadeOut.setFromValue(1);
    fadeOut.setToValue(0);
    fadeOut.setOnFinished(e -> {
        tranquilOverlay.setVisible(false);
        tranquilOverlay.setManaged(false);
    });
    fadeOut.play();
}
private void loadPage(String path) {
    System.out.println("loadPage called with path: " + path);
    com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
    try {
        System.out.println("Getting resource for: " + path);
        java.net.URL resourceUrl = getClass().getResource(path);
        System.out.println("Resource URL: " + resourceUrl);
        
        if (resourceUrl == null) {
            System.err.println("ERROR: Resource not found at path: " + path);
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(resourceUrl);
        System.out.println("Loading FXML...");
        Parent p = loader.load();
        System.out.println("FXML loaded successfully, setting scene root...");
        root.getScene().setRoot(p);
        System.out.println("Scene root set successfully!");
    } catch (Exception ex) {
        System.err.println("ERROR in loadPage: " + ex.getMessage());
        ex.printStackTrace();
    }
} 





    private void openToDo() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            System.out.println("=== Opening ToDo page ===");
            URL fxmlUrl = getClass().getResource("/com/the_pathfinders/fxml/ToDo.fxml");
            System.out.println("FXML URL: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent p = loader.load();
            System.out.println("FXML loaded successfully");

            ToDoController controller = loader.getController();
            if (controller != null) {
                controller.setSoulId(this.soulId == null ? "" : this.soulId);
            }
            System.out.println("Controller: " + controller);

            // The FXML already has the stylesheet reference, just set the root
            if (root != null && root.getScene() != null) {
                System.out.println("Setting scene root...");
                root.getScene().setRoot(p);
                System.out.println("Scene root set successfully");
            } else {
                System.err.println("ERROR: root or scene is null!");
            }
        } catch (Exception ex) {
            System.err.println("ERROR in openToDo:");
            ex.printStackTrace();
        }
    }

    private void openSeekHelp() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/SeekHelp.fxml"));
            Parent p = loader.load();

            SeekHelpController controller = loader.getController();
            if (controller != null) {
                controller.setSoulId(this.soulId == null ? "" : this.soulId);
            }

            var scene = root != null ? root.getScene() : (insightsBtn != null ? insightsBtn.getScene() : null);
            if (scene != null) {
                scene.setRoot(p);
            } else {
                System.err.println("SeekHelp navigation failed: scene is null");
            }
        } catch (Exception ex) {
            System.err.println("SeekHelp navigation error:");
            ex.printStackTrace();
        }
    }



    private void animateButtonsPopup() {
        if (buttonCardsBox == null) return;
        
        // Get all button rows
        var rows = buttonCardsBox.getChildren();
        
        // Animate each row with staggered delay
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            
            // Set initial state: scaled down and transparent
            row.setScaleX(0.3);
            row.setScaleY(0.3);
            row.setOpacity(0.0);
            
            // Create timeline for smooth pop-up animation
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(row.scaleXProperty(), 0.3),
                    new KeyValue(row.scaleYProperty(), 0.3),
                    new KeyValue(row.opacityProperty(), 0.0)
                ),
                new KeyFrame(Duration.millis(500),
                    new KeyValue(row.scaleXProperty(), 1.0),
                    new KeyValue(row.scaleYProperty(), 1.0),
                    new KeyValue(row.opacityProperty(), 1.0)
                )
            );
            
            // Stagger delay: 150ms between each row
            timeline.setDelay(Duration.millis(i * 150));
            timeline.play();
        }
    }

    public void setSoulId(String soulId) {
        this.soulId = soulId;
        setUser(soulId, null);
    }
    
    public void setUser(String id, String name) {
        this.soulId = id == null ? "" : id;
        if (userLabel != null) {
            userLabel.setText(this.soulId);
            userLabel.setVisible(true);
            userLabel.setOpacity(1.0);
            userLabel.setStyle("-fx-text-fill: #4a4a4a; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
        
        // Track activity when dashboard loads
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Ensure greeting reflects current time when user is set (scene may be swapped after initialize)
        try { if (greetingLabel != null) updateGreeting(); } catch (Exception ignored) {}
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + this.soulId + ".jpg");
            if (u == null) u = getClass().getResource("/assets/icons/user.png");
            if (u != null && userImage != null) {
                Image img = new Image(u.toExternalForm(), 50, 50, true, true);
                userImage.setImage(img);
                userImage.setVisible(true);
                userImage.setOpacity(1.0);
            }
        } catch (Exception ignored) {}
    }

    private void updateGreeting() {
        int h = LocalTime.now().getHour();
        String g;
        if (h < 12) {
            g = "Good Morning 🌞 Have a good day!";
        } else if (h < 18) {
            g = "Good Afternoon ☀️ Did you eat?";
        } else if (h < 22) {
            g = "Good Evening — ready to unwind? 🌙";
        } else {
            g = "Good Night 🌌 Time to sleep..";
        }

        greetingLabel.setText(g);
    }

    public void setLogo() {
        try {
            URL u = getClass().getResource("/assets/images/shelter_of_mind.svg");
            u = getClass().getResource("/assets/images/logo_testing.png");
            if (u != null && logoImage != null) {
                Image img = new Image(u.toExternalForm(), 0, 0, true, true);
                logoImage.setImage(img);
                logoImage.setFitWidth(100);
                logoImage.setFitHeight(100);
                logoImage.setSmooth(true);
                logoImage.setCache(true);
            }
        } catch (Exception ignored) {}
    }

    private void toggleUserMenu() {
        if (userDropdown == null) return;
        if (userDropdown.isVisible()) {
            userDropdown.setVisible(false);
            userDropdown.setManaged(false);
            userDropdown.getChildren().clear();
            return;
        }
        userDropdown.setVisible(true);
        userDropdown.setManaged(true);
        userDropdown.getChildren().clear();
        String[] items = {"My Profile", "Messages", "Starred Journals", "Log Out"};
        for (int i = 0; i < items.length; i++) {
            Button b = new Button(items[i]);
            b.getStyleClass().add("dropdown-item");
            
            // Add unread message badge if this is the Messages option
            if (items[i].equals("Messages")) {
                HBox buttonContent = new HBox(8);
                buttonContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                Label textLabel = new Label("Messages");
                buttonContent.getChildren().add(textLabel);
                
                // Add notification badge asynchronously
                Label badge = new Label();
                badge.setVisible(false);
                badge.setManaged(false);
                badge.getStyleClass().add("notification-badge");
                badge.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; " +
                               "-fx-padding: 2 6 2 6; -fx-background-radius: 10; " +
                               "-fx-font-size: 10px; -fx-font-weight: bold;");
                buttonContent.getChildren().add(badge);
                
                b.setGraphic(buttonContent);
                b.setText("");
                
                // Load unread count in background
                new Thread(() -> {
                    try {
                        int unreadCount = com.the_pathfinders.db.ModerationRepository.getUnreadMessageCount(this.soulId);
                        if (unreadCount > 0) {
                            javafx.application.Platform.runLater(() -> {
                                badge.setText(String.valueOf(unreadCount));
                                badge.setVisible(true);
                                badge.setManaged(true);
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
            
            userDropdown.getChildren().add(b);
            final int idx = i;
            b.setOnAction(e -> handleDropdownSelection(items[idx]));
        }
        userDropdown.getStyleClass().add("dropdown-container");
    }

    private void handleDropdownSelection(String which) {
        switch (which) {
            case "My Profile" -> openProfile();
            case "Messages" -> openMessages();
            case "Starred Journals" -> showStarredPlaceholder();
            case "Log Out" -> onLogout();
        }
    }
    
    private void openMessages() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/user_messages.fxml"));
            Parent messagesRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof UserMessagesController umc) {
                umc.setSoulId(this.soulId);
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(messagesRoot);
            }
        } catch (Exception ex) { 
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load messages page: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void openProfile() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            System.out.println("Opening profile for soulId: " + this.soulId);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/profile.fxml"));
            Parent profileRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ProfileController pc) {
                if (this.soulId == null || this.soulId.isEmpty()) {
                    System.err.println("WARNING: soulId is empty in openProfile!");
                }
                pc.setSoulId(this.soulId);
                System.out.println("Set soulId in ProfileController: " + this.soulId);
                pc.onShown();
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(profileRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showStarredPlaceholder() {
        // Navigate to profile page with starred journals section
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/profile.fxml"));
            Parent profileRoot = loader.load();
            ProfileController controller = loader.getController();
            if (controller != null) {
                controller.setSoulId(soulId);
                controller.showStarredJournalsSection(); // Show starred journals directly
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(profileRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void onLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Log out");
        a.setHeaderText("Confirm log out");
        a.setContentText("Do you want to log out?");
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/login_signup.fxml"));
                Parent loginRoot = loader.load();
                Object loginController = loader.getController();
                if (loginController != null) {
                    try {
                        Method m = loginController.getClass().getMethod("setRepository", SoulRepository.class);
                        m.invoke(loginController, new SoulRepository());
                    } catch (NoSuchMethodException ignored) {}
                }
                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(loginRoot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openPrivateJournals() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/private_journals_view.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PrivateJournalsController pc) pc.setSoulId(this.soulId);
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openPublicJournals() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/public_journals_view.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method m = controller.getClass().getMethod("setSoulId", String.class);
                    m.invoke(controller, this.soulId == null ? "" : this.soulId);
                } catch (NoSuchMethodException ignored) {}
            }
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openBlogs() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/blog.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method m = controller.getClass().getMethod("setSoulId", String.class);
                    m.invoke(controller, this.soulId == null ? "" : this.soulId);
                } catch (NoSuchMethodException ignored) {}
            }
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleMusicToggle() {
        boolean enableMusic = musicToggle != null && musicToggle.isSelected();
        MusicManager.setBackgroundMusicEnabled(enableMusic);
        updateMusicToggleText();
    }

    private void updateMusicToggleText() {
        if (musicToggle != null) {
            musicToggle.setText(musicToggle.isSelected() ? "Music: On" : "Music: Off");
        }
    }

    // ========== Journaling Popup Methods ==========
    
    private void showJournalingPopup() {
        if (journalingOverlay == null) return;
        
        // Reset to initial state
        if (journalingTitle != null) journalingTitle.setText("Journaling");
        if (journalingSubtitle != null) journalingSubtitle.setText("Choose Option");
        
        // Clear and add initial buttons
        if (journalingButtonsBox != null) {
            journalingButtonsBox.getChildren().clear();
            if (createJournalBtn != null) journalingButtonsBox.getChildren().add(createJournalBtn);
            if (viewJournalsBtn != null) journalingButtonsBox.getChildren().add(viewJournalsBtn);
        }
        
        // Show overlay with fade animation
        journalingOverlay.setVisible(true);
        journalingOverlay.setManaged(true);
        journalingOverlay.setOpacity(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), journalingOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void hideJournalingPopup() {
        if (journalingOverlay == null) return;
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), journalingOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            journalingOverlay.setVisible(false);
            journalingOverlay.setManaged(false);
        });
        fadeOut.play();
    }
    
    private void onCreateJournal() {
        hideJournalingPopup();
        // Navigate to journal creation page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
            Parent journRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JournalController jc) {
                jc.setSoulId(this.soulId);
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(journRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    private void onViewJournals() {
        // Transition to second state with fade
        if (journalingContentBox == null) return;
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), journalingContentBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            // Change content to "View Journal" options
            if (journalingTitle != null) journalingTitle.setText("View Journal");
            if (journalingSubtitle != null) journalingSubtitle.setText("Choose Option");
            
            // Replace buttons with new options
            if (journalingButtonsBox != null) {
                journalingButtonsBox.getChildren().clear();
                
                Button myJournalsBtn = new Button("My Journals");
                myJournalsBtn.getStyleClass().add("journaling-btn");
                myJournalsBtn.setPrefWidth(220);
                myJournalsBtn.setPrefHeight(48);
                myJournalsBtn.setOnAction(ev -> onMyJournals());
                
                Button publicJournalsBtn = new Button("Public Journals");
                publicJournalsBtn.getStyleClass().add("journaling-btn");
                publicJournalsBtn.setPrefWidth(220);
                publicJournalsBtn.setPrefHeight(48);
                publicJournalsBtn.setOnAction(ev -> {
                    hideJournalingPopup();
                    openPublicJournals();
                });
                
                journalingButtonsBox.getChildren().addAll(myJournalsBtn, publicJournalsBtn);
            }
            
            // Fade in new content
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), journalingContentBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }
    
    private void onMyJournals() {
        hideJournalingPopup();
        openPrivateJournals();
    }

    // ========== Mood Tracker Popup Methods ==========

    private void initializeMoodTracker() {
        if (moodTrackerOverlay == null) return;

        moodRepository = new MoodTrackerRepository();
        moodAnswers = new HashMap<>();

        // Initialize overlay state
        moodTrackerOverlay.setMouseTransparent(true); // Don't block clicks when hidden
        System.out.println("Mood tracker initialized. Overlay visible: " + moodTrackerOverlay.isVisible());

        // Initialize progress circles only if they exist
        if (progress1 != null && progress2 != null && progress3 != null &&
            progress4 != null && progress5 != null) {
            progressCircles = Arrays.asList(progress1, progress2, progress3, progress4, progress5);
        } else {
            System.err.println("Warning: Some progress circle elements are null in dashboard.fxml");
            progressCircles = new ArrayList<>();
        }

        // Initialize questions
        moodQuestions = new ArrayList<>();
        moodQuestions.add(new QuestionData(
            "How many pending tasks do you have?",
            "stress",
            Arrays.asList("Too Much", "Much", "A Little", "None"),
            new int[]{1, 2, 3, 4}
        ));
        moodQuestions.add(new QuestionData(
            "How would you rate your stress level today?",
            "stress",
            Arrays.asList("Very High", "High", "Moderate", "Low"),
            new int[]{1, 2, 3, 4}
        ));
        moodQuestions.add(new QuestionData(
            "How anxious do you feel right now?",
            "anxiety",
            Arrays.asList("Very Anxious", "Anxious", "Slightly Anxious", "Calm"),
            new int[]{1, 2, 3, 4}
        ));
        moodQuestions.add(new QuestionData(
            "How is your energy level today?",
            "energy",
            Arrays.asList("Very Low", "Low", "Moderate", "High"),
            new int[]{1, 2, 3, 4}
        ));
        moodQuestions.add(new QuestionData(
            "How well did you sleep last night?",
            "sleep",
            Arrays.asList("Very Poor", "Poor", "Fair", "Good"),
            new int[]{1, 2, 3, 4}
        ));

        // Setup button handlers
        if (moodBackBtn != null) moodBackBtn.setOnAction(e -> hideMoodTrackerPopup());
        if (moodCloseBtn != null) moodCloseBtn.setOnAction(e -> hideMoodTrackerPopup());
        if (moodPrevBtn != null) moodPrevBtn.setOnAction(e -> onMoodPrevious());
        if (moodNextBtn != null) moodNextBtn.setOnAction(e -> onMoodNext());
        if (moodSubmitBtn != null) moodSubmitBtn.setOnAction(e -> onMoodSubmit());
        if (moodDoneBtn != null) moodDoneBtn.setOnAction(e -> hideMoodTrackerPopup());

        // Click outside to close
        if (moodTrackerOverlay != null) {
            moodTrackerOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == moodTrackerOverlay) {
                    hideMoodTrackerPopup();
                }
            });
        }
    }

    private void showMoodTrackerPopup() {
        if (moodTrackerOverlay == null) {
            System.err.println("Error: moodTrackerOverlay is null!");
            return;
        }

        System.out.println("Opening mood tracker popup...");
        System.out.println("Overlay visible: " + moodTrackerOverlay.isVisible() + ", managed: " + moodTrackerOverlay.isManaged());

        // Stop any running animation first
        if (moodTrackerFadeTransition != null) {
            System.out.println("Stopping previous animation...");
            moodTrackerFadeTransition.stop();
            moodTrackerFadeTransition = null;
        }

        // Reset state
        currentMoodQuestion = 0;
        moodAnswers.clear();

        // Clear all progress circle styles first
        if (progressCircles != null) {
            for (Circle circle : progressCircles) {
                circle.getStyleClass().removeAll("active");
            }
        }

        // Show question view, hide results
        if (moodTrackerContentBox != null) {
            moodTrackerContentBox.setVisible(true);
            moodTrackerContentBox.setManaged(true);
            moodTrackerContentBox.setOpacity(1.0);
        }
        if (moodResultsBox != null) {
            moodResultsBox.setVisible(false);
            moodResultsBox.setManaged(false);
        }

        loadMoodQuestion(0);
        updateMoodProgress();
        updateMoodNavButtons();

        // Show overlay with fade animation
        moodTrackerOverlay.setVisible(true);
        moodTrackerOverlay.setManaged(true);
        moodTrackerOverlay.setMouseTransparent(false); // Allow mouse events
        moodTrackerOverlay.setOpacity(0);
        moodTrackerOverlay.toFront(); // Ensure it's on top

        moodTrackerFadeTransition = new FadeTransition(Duration.millis(300), moodTrackerOverlay);
        moodTrackerFadeTransition.setFromValue(0);
        moodTrackerFadeTransition.setToValue(1);
        moodTrackerFadeTransition.setOnFinished(e -> {
            moodTrackerFadeTransition = null;
            System.out.println("Mood tracker fully opened. Opacity: " + moodTrackerOverlay.getOpacity());
        });
        moodTrackerFadeTransition.play();
    }

    private void hideMoodTrackerPopup() {
        if (moodTrackerOverlay == null) return;

        System.out.println("Closing mood tracker popup...");
        System.out.println("Current opacity: " + moodTrackerOverlay.getOpacity());

        // Stop any running animation first
        if (moodTrackerFadeTransition != null) {
            System.out.println("Stopping previous animation...");
            moodTrackerFadeTransition.stop();
            moodTrackerFadeTransition = null;
        }

        moodTrackerFadeTransition = new FadeTransition(Duration.millis(300), moodTrackerOverlay);
        moodTrackerFadeTransition.setFromValue(moodTrackerOverlay.getOpacity()); // Use current opacity
        moodTrackerFadeTransition.setToValue(0);
        moodTrackerFadeTransition.setOnFinished(e -> {
            moodTrackerOverlay.setVisible(false);
            moodTrackerOverlay.setManaged(false);
            moodTrackerOverlay.setMouseTransparent(true); // Don't block clicks when hidden
            moodTrackerFadeTransition = null;
            System.out.println("Mood tracker popup closed. Visible: " + moodTrackerOverlay.isVisible());
        });
        moodTrackerFadeTransition.play();
    }

    private void loadMoodQuestion(int index) {
        if (moodQuestionsContainer == null || moodQuestions == null) return;
        moodQuestionsContainer.getChildren().clear();

        if (index >= 0 && index < moodQuestions.size()) {
            QuestionData question = moodQuestions.get(index);

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
                String savedAnswer = moodAnswers.get(String.valueOf(index));
                if (savedAnswer != null && savedAnswer.equals(question.options.get(i))) {
                    radio.setSelected(true);
                }

                final int score = question.scores[i];
                final String option = question.options.get(i);
                radio.setOnAction(e -> {
                    moodAnswers.put(String.valueOf(currentMoodQuestion), option);
                    moodAnswers.put(String.valueOf(currentMoodQuestion) + "_score", String.valueOf(score));
                    moodAnswers.put(String.valueOf(currentMoodQuestion) + "_category", question.category);
                });

                questionBox.getChildren().add(radio);
            }

            questionBox.getChildren().add(0, questionLabel);
            moodQuestionsContainer.getChildren().add(questionBox);
        }
    }

    private void updateMoodProgress() {
        if (progressCircles == null) return;

        // First remove active class from all circles
        for (Circle circle : progressCircles) {
            circle.getStyleClass().removeAll("active");
        }

        // Then add active class to current question's circle
        if (currentMoodQuestion >= 0 && currentMoodQuestion < progressCircles.size()) {
            progressCircles.get(currentMoodQuestion).getStyleClass().add("active");
        }
    }

    private void updateMoodNavButtons() {
        if (moodPrevBtn != null) {
            moodPrevBtn.setDisable(currentMoodQuestion == 0);
        }

        if (moodQuestions != null && currentMoodQuestion == moodQuestions.size() - 1) {
            if (moodNextBtn != null) {
                moodNextBtn.setVisible(false);
                moodNextBtn.setManaged(false);
            }
            if (moodSubmitBtn != null) {
                moodSubmitBtn.setVisible(true);
                moodSubmitBtn.setManaged(true);
            }
        } else {
            if (moodNextBtn != null) {
                moodNextBtn.setVisible(true);
                moodNextBtn.setManaged(true);
            }
            if (moodSubmitBtn != null) {
                moodSubmitBtn.setVisible(false);
                moodSubmitBtn.setManaged(false);
            }
        }
    }

    private void onMoodPrevious() {
        if (currentMoodQuestion > 0) {
            currentMoodQuestion--;
            loadMoodQuestion(currentMoodQuestion);
            updateMoodProgress();
            updateMoodNavButtons();
            if (moodScrollPane != null) moodScrollPane.setVvalue(0);
        }
    }

    private void onMoodNext() {
        if (!isMoodQuestionAnswered()) {
            showMoodAlert("Please answer the current question before proceeding.");
            return;
        }

        if (moodQuestions != null && currentMoodQuestion < moodQuestions.size() - 1) {
            currentMoodQuestion++;
            loadMoodQuestion(currentMoodQuestion);
            updateMoodProgress();
            updateMoodNavButtons();
            if (moodScrollPane != null) moodScrollPane.setVvalue(0);
        }
    }

    private void onMoodSubmit() {
        if (!isMoodQuestionAnswered()) {
            showMoodAlert("Please answer all questions before submitting.");
            return;
        }

        showMoodResults();
    }

    private boolean isMoodQuestionAnswered() {
        return moodAnswers.containsKey(String.valueOf(currentMoodQuestion));
    }

    private void showMoodResults() {
        // Calculate category scores
        Map<String, Integer> categoryScores = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (String key : moodAnswers.keySet()) {
            if (key.endsWith("_category")) {
                String questionIdx = key.replace("_category", "");
                String category = moodAnswers.get(key);
                String scoreKey = questionIdx + "_score";

                if (moodAnswers.containsKey(scoreKey)) {
                    int score = Integer.parseInt(moodAnswers.get(scoreKey));
                    categoryScores.put(category, categoryScores.getOrDefault(category, 0) + score);
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
            }
        }

        // Calculate overall mood score
        int totalPossibleScore = moodQuestions.size() * 4;
        int actualScore = 0;
        for (int i = 0; i < moodQuestions.size(); i++) {
            String scoreKey = i + "_score";
            if (moodAnswers.containsKey(scoreKey)) {
                actualScore += Integer.parseInt(moodAnswers.get(scoreKey));
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
            entry.setAnswers(moodAnswers.toString());

            moodRepository.saveMoodEntry(entry);
        } catch (Exception e) {
            System.err.println("Error saving mood entry: " + e.getMessage());
            e.printStackTrace();
        }

        // Update pie chart
        if (moodPieChart != null) {
            moodPieChart.getData().clear();

            double totalScore = 0;
            for (String category : categoryScores.keySet()) {
                totalScore += categoryScores.get(category);
            }

            for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
                String categoryName = capitalizeFirst(entry.getKey());
                double percentage = (entry.getValue() / totalScore) * 100;
                PieChart.Data slice = new PieChart.Data(
                    categoryName + " (" + String.format("%.1f", percentage) + "%)",
                    entry.getValue()
                );
                moodPieChart.getData().add(slice);
            }
        }

        // Update score label
        if (moodScoreLabel != null) {
            moodScoreLabel.setText(String.format("Overall Mood Score: %.0f/100", moodScore));
        }

        // Generate summary
        if (moodSummaryLabel != null) {
            String summary = generateMoodSummary(moodScore, categoryScores, categoryCounts);
            moodSummaryLabel.setText(summary);
        }

        // Fade transition from questions to results
        if (moodTrackerContentBox != null && moodResultsBox != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), moodTrackerContentBox);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                moodTrackerContentBox.setVisible(false);
                moodTrackerContentBox.setManaged(false);
                moodResultsBox.setVisible(true);
                moodResultsBox.setManaged(true);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), moodResultsBox);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String generateMoodSummary(double moodScore, Map<String, Integer> categoryScores,
                                       Map<String, Integer> categoryCounts) {
        StringBuilder summary = new StringBuilder();

        if (moodScore >= 75) {
            summary.append("You're doing great! Your mood is positive and balanced. ");
        } else if (moodScore >= 50) {
            summary.append("Your mood is moderate. There's room for improvement. ");
        } else {
            summary.append("You might be going through a tough time. Consider seeking support. ");
        }

        // Find the area that needs most attention
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

    private void showMoodAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Incomplete");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

