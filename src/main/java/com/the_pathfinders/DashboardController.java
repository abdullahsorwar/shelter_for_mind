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
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
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
    
    private VideoManager videoManager;
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

    // Help Center elements
    @FXML private Button helpCenterBtn;
    @FXML private StackPane helpCenterOverlay;
    @FXML private VBox helpCenterContentBox;
    @FXML private Button technicalSupportBtn;
    @FXML private Button emergencyHotlinesBtn;
    @FXML private Button helpCenterCloseBtn;
    
    @FXML private StackPane technicalSupportOverlay;
    @FXML private Button techSupportBackBtn;
    @FXML private Button techSupportCloseBtn;
    @FXML private Label email1Label;
    @FXML private Label email2Label;
    @FXML private Label email3Label;
    
    @FXML private StackPane emergencyHotlinesOverlay;
    @FXML private Button emergencyBackBtn;
    @FXML private Button emergencyCloseBtn;
    @FXML private VBox hotlinesContainer;
    @FXML private Button createSafetyPlanBtn;
    
    @FXML private StackPane safetyPlanOverlay;
    @FXML private Button safetyPlanBackBtn;
    @FXML private javafx.scene.control.TextField safetyPlanContact;
    @FXML private javafx.scene.control.TextField safetyPlanCalm;
    @FXML private javafx.scene.control.TextField safetyPlanPlace;
    @FXML private Button saveSafetyPlanBtn;
    @FXML private Button cancelSafetyPlanBtn;

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
    @FXML private Label moodProgressLabel;
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
    
    // Track button hover states to prevent multiple simultaneous displays
    private Button currentlyHoveredButton = null;
    private FadeTransition currentFadeIn = null;
    private FadeTransition currentFadeOut = null;
    private ScaleTransition currentScaleUp = null;
    private ScaleTransition currentScaleDown = null;

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
        // Ensure root pane fills the scene for proper video background coverage
        if (root != null) {
            javafx.application.Platform.runLater(() -> {
                if (root.getScene() != null && root.getScene().getWindow() != null) {
                    root.prefWidthProperty().bind(root.getScene().widthProperty());
                    root.prefHeightProperty().bind(root.getScene().heightProperty());
                }
            });
        }
        
        // Initialize background video
        videoManager = VideoManager.getInstance();
        if (videoManager.isInitialized()) {
            videoManager.attachToPane(root);
        } else {
            videoManager.initializeWithRetry(
                3,
                msg -> videoManager.attachToPane(root),
                err -> System.err.println("Failed to initialize video: " + err)
            );
        }
        
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

        System.out.println("=== Music Toggle Setup ===");
        System.out.println("musicToggle is null: " + (musicToggle == null));
        System.out.println("root children count: " + (root != null ? root.getChildren().size() : "null"));
        if (root != null && root.getChildren().size() > 0) {
            System.out.println("First child: " + root.getChildren().get(0).getClass().getSimpleName());
            System.out.println("First child mouseTransparent: " + root.getChildren().get(0).isMouseTransparent());
        }
        
        if (musicToggle != null) {
            System.out.println("musicToggle found, setting up...");
            System.out.println("musicToggle disabled: " + musicToggle.isDisabled());
            System.out.println("musicToggle visible: " + musicToggle.isVisible());
            System.out.println("musicToggle parent: " + (musicToggle.getParent() != null ? musicToggle.getParent().getClass().getSimpleName() : "null"));
            
            musicToggle.setSelected(MusicManager.isBackgroundMusicEnabled());
            updateMusicToggleText();
            musicToggle.setOnAction(e -> {
                System.out.println(">>> MUSIC TOGGLE ACTION FIRED <<<");
                handleMusicToggle();
            });
            
            // Test direct click handler
            musicToggle.setOnMouseClicked(e -> {
                System.out.println(">>> MOUSE CLICKED ON TOGGLE <<<");
            });
            
            System.out.println("musicToggle setup complete");
        } else {
            System.out.println("ERROR: musicToggle is NULL! Check FXML fx:id");
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
        if (moodTrackerBtn != null) {
            // Remove button action - click will be handled by inner donut only
            // Create the colorful donut ring design
            setupMoodTrackerButtonGraphic();
        }

        // Setup hover effects for all buttons with images
        setupButtonHoverEffect(journalBtn, "STRAY THOUGHTS");
        setupButtonHoverEffect(blogBtn, "MIND DRIFT");
        setupButtonHoverEffect(moodBtn, "SIDEQUESTS");
        setupButtonHoverEffect(insightsBtn, "SEEK HELP");
        setupButtonHoverEffect(SocialWorkBtn, "HOPE SEED");
        setupButtonHoverEffect(tranquilCornerBtn, "TRANQUIL CORNER");

        // Initialize mood tracker popup
        initializeMoodTracker();

        // Initialize Help Center
        if (helpCenterBtn != null) helpCenterBtn.setOnAction(e -> showHelpCenter());
        if (helpCenterCloseBtn != null) helpCenterCloseBtn.setOnAction(e -> hideHelpCenter());
        if (technicalSupportBtn != null) technicalSupportBtn.setOnAction(e -> showTechnicalSupport());
        if (emergencyHotlinesBtn != null) emergencyHotlinesBtn.setOnAction(e -> showEmergencyHotlines());
        if (techSupportBackBtn != null) techSupportBackBtn.setOnAction(e -> {
            hideTechnicalSupport();
            showHelpCenter();
        });
        if (techSupportCloseBtn != null) techSupportCloseBtn.setOnAction(e -> hideTechnicalSupport());
        if (emergencyBackBtn != null) emergencyBackBtn.setOnAction(e -> {
            hideEmergencyHotlines();
            showHelpCenter();
        });
        if (emergencyCloseBtn != null) emergencyCloseBtn.setOnAction(e -> hideEmergencyHotlines());
        if (createSafetyPlanBtn != null) createSafetyPlanBtn.setOnAction(e -> showSafetyPlanPopup());
        
        // Safety Plan overlay handlers
        if (safetyPlanBackBtn != null) safetyPlanBackBtn.setOnAction(e -> hideSafetyPlanPopup());
        if (cancelSafetyPlanBtn != null) cancelSafetyPlanBtn.setOnAction(e -> hideSafetyPlanPopup());
        
        // Setup email click handlers
        if (email1Label != null) {
            email1Label.setOnMouseClicked(e -> openEmail("raisatabassum2023115989@cs.du.ac.bd"));
        }
        if (email2Label != null) {
            email2Label.setOnMouseClicked(e -> openEmail("mdabdullah-2023715965@cs.du.ac.bd"));
        }
        if (email3Label != null) {
            email3Label.setOnMouseClicked(e -> openEmail("the.pathfinders.dev@gmail.com"));
        }
        
        // Setup Safety Plan save button
        if (saveSafetyPlanBtn != null) {
            saveSafetyPlanBtn.setOnAction(e -> saveSafetyPlan());
        }
        
        // Close overlays when clicking outside
        if (helpCenterOverlay != null) {
            helpCenterOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == helpCenterOverlay) hideHelpCenter();
            });
        }
        if (technicalSupportOverlay != null) {
            technicalSupportOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == technicalSupportOverlay) hideTechnicalSupport();
            });
        }
        if (emergencyHotlinesOverlay != null) {
            emergencyHotlinesOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == emergencyHotlinesOverlay) hideEmergencyHotlines();
            });
        }
        if (safetyPlanOverlay != null) {
            safetyPlanOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == safetyPlanOverlay) hideSafetyPlanPopup();
            });
        }
        
        // Populate emergency hotlines
        populateEmergencyHotlines();

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

        // Hide bgRect to show video background
        try {
            if (bgRect != null) {
                bgRect.setVisible(false);
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
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/SocialWork.fxml"));
                Parent p = loader.load();

                SocialWorkController controller = loader.getController();
                if (controller != null) {
                    controller.setSoulId(this.soulId == null ? "" : this.soulId);
                }

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) {
                        root.getScene().setRoot(p);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
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
    
    // Load in background thread to prevent UI freezing
    new Thread(() -> {
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
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                root.getScene().setRoot(p);
                System.out.println("Scene root set successfully!");
            });
        } catch (Exception ex) {
            System.err.println("ERROR in loadPage: " + ex.getMessage());
            ex.printStackTrace();
        }
    }).start();
} 





    private void openToDo() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
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

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) {
                        System.out.println("Setting scene root...");
                        root.getScene().setRoot(p);
                        System.out.println("Scene root set successfully");
                    } else {
                        System.err.println("ERROR: root or scene is null!");
                    }
                });
            } catch (Exception ex) {
                System.err.println("ERROR in openToDo:");
                ex.printStackTrace();
            }
        }).start();
    }

    private void openSeekHelp() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/SeekHelp.fxml"));
                Parent p = loader.load();

                SeekHelpController controller = loader.getController();
                if (controller != null) {
                    controller.setSoulId(this.soulId == null ? "" : this.soulId);
                }

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    var scene = root != null ? root.getScene() : (insightsBtn != null ? insightsBtn.getScene() : null);
                    if (scene != null) {
                        scene.setRoot(p);
                    } else {
                        System.err.println("SeekHelp navigation failed: scene is null");
                    }
                });
            } catch (Exception ex) {
                System.err.println("SeekHelp navigation error:");
                ex.printStackTrace();
            }
        }).start();
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
            // Removed inline style to allow CSS to apply (white color for visibility on video background)
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

    private void setupMoodTrackerButtonGraphic() {
        try {
            // Create a StackPane to layer the images and text
            StackPane buttonGraphic = new StackPane();
            buttonGraphic.setPrefSize(200, 200); // Size matches inner donut display size
            buttonGraphic.setPickOnBounds(false); // Only respond to opaque pixels
            
            // Load outer arc image - LARGER than inner for the ring effect
            URL outerArcUrl = getClass().getResource("/assets/images/outer_arc.png");
            ImageView outerArcView = null;
            if (outerArcUrl != null) {
                outerArcView = new ImageView(new Image(outerArcUrl.toExternalForm()));
                outerArcView.setFitWidth(480); // Even larger outer ring
                outerArcView.setFitHeight(480);
                outerArcView.setPreserveRatio(true);
                outerArcView.setSmooth(true);
                outerArcView.setPickOnBounds(false); // Only respond to opaque pixels
                outerArcView.setMouseTransparent(true); // Don't block inner donut clicks
                buttonGraphic.getChildren().add(outerArcView);
            }
            
            // Load inner donut image - 578x578 trimmed image scaled to 200x200
            URL innerDonutUrl = getClass().getResource("/assets/images/inner_donut.png");
            ImageView innerDonutView = null;
            if (innerDonutUrl != null) {
                innerDonutView = new ImageView(new Image(innerDonutUrl.toExternalForm()));
                innerDonutView.setFitWidth(200); // Exact match to StackPane size
                innerDonutView.setFitHeight(200);
                innerDonutView.setPreserveRatio(true);
                innerDonutView.setSmooth(true);
                innerDonutView.setPickOnBounds(false); // Only detect clicks on opaque pixels
                innerDonutView.setTranslateY(-8); // Shift up to align with outer circle
                innerDonutView.setTranslateX(-2); // Shift left slightly for better centering
                innerDonutView.setMouseTransparent(false); // Ensure it can receive mouse events
                innerDonutView.setCursor(javafx.scene.Cursor.HAND); // Hand cursor only on inner donut
                buttonGraphic.getChildren().add(innerDonutView);
                
                // Add smooth pop-up transition on hover for inner donut only
                // Apply hover to the imageView itself, not the button
                final ImageView finalInnerDonutView = innerDonutView;
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), finalInnerDonutView);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), finalInnerDonutView);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                
                // Attach hover events to the inner donut imageView, not the button
                finalInnerDonutView.setOnMouseEntered(e -> {
                    scaleUp.playFromStart();
                    e.consume(); // Consume event to prevent button from receiving it
                });
                finalInnerDonutView.setOnMouseExited(e -> {
                    scaleDown.playFromStart();
                    e.consume();
                });
                
                // Attach click event to inner donut only - open assessment
                finalInnerDonutView.setOnMouseClicked(e -> {
                    System.out.println("Inner donut clicked!");
                    showMoodTrackerPopup();
                    e.consume(); // Consume event to prevent button from receiving it
                });
            }
            
            // No text label needed - image has text built-in
            
            // Set the graphic to the button and clear any default text
            moodTrackerBtn.setGraphic(buttonGraphic);
            moodTrackerBtn.setText(""); // Clear text to avoid overlay
            moodTrackerBtn.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            moodTrackerBtn.setPrefSize(200, 200); // Match inner donut size
            moodTrackerBtn.setPickOnBounds(false); // Don't respond to bounding box
            // No shape set - button doesn't handle clicks, only ImageView does
            
        } catch (Exception e) {
            System.err.println("Error creating mood tracker button graphic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupButtonHoverEffect(Button button, String labelText) {
        if (button == null) return;

        // Create a StackPane to overlay text on the image
        StackPane stackPane = new StackPane();
        
        // Get the existing ImageView from the button's graphic
        ImageView imageView = (ImageView) button.getGraphic();
        if (imageView != null) {
            // Create text label for hover - black text with white shadow, no background
            Label hoverLabel = new Label(labelText);
            hoverLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; " +
                              "-fx-text-fill: #1f2937; " +
                              "-fx-effect: dropshadow(gaussian, rgba(255, 255, 255, 0.95), 6, 0.9, 0, 0); " +
                              "-fx-padding: 8 16;");
            hoverLabel.setWrapText(true);
            hoverLabel.setAlignment(Pos.CENTER);
            hoverLabel.setMaxWidth(220);
            hoverLabel.setOpacity(0); // Initially invisible
            hoverLabel.setMouseTransparent(true); // Don't block button clicks
            hoverLabel.setViewOrder(-1); // Ensure it's on top
            
            // Add image and label to StackPane
            stackPane.getChildren().addAll(imageView, hoverLabel);
            stackPane.setAlignment(Pos.CENTER);
            StackPane.setAlignment(hoverLabel, Pos.CENTER);
            
            // Set the StackPane as the button graphic
            button.setGraphic(stackPane);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            
            // Create fade transition for text - extremely fast fade out (0.1s)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), hoverLabel);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(100), hoverLabel); // 0.1s super fast
            fadeOut.setToValue(0);
            
            // Create scale transition for button pop effect
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            
            // Handle hover events - stop previous animations to prevent multiple displays
            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> {
                // Stop all previous animations immediately
                if (currentFadeIn != null) currentFadeIn.stop();
                if (currentFadeOut != null) currentFadeOut.stop();
                if (currentScaleUp != null) currentScaleUp.stop();
                if (currentScaleDown != null) currentScaleDown.stop();
                
                // If there was a previously hovered button, fade it out from current opacity
                if (currentlyHoveredButton != null && currentlyHoveredButton != button) {
                    Button prevButton = currentlyHoveredButton;
                    var prevGraphic = prevButton.getGraphic();
                    if (prevGraphic instanceof StackPane) {
                        StackPane prevStack = (StackPane) prevGraphic;
                        if (prevStack.getChildren().size() > 1) {
                            var prevLabel = prevStack.getChildren().get(1);
                            if (prevLabel instanceof Label) {
                                Label label = (Label) prevLabel;
                                // Get current opacity (relative to fade in progress)
                                double currentOpacity = label.getOpacity();
                                if (currentOpacity > 0) {
                                    // Create a quick fade out from current opacity
                                    FadeTransition quickFade = new FadeTransition(Duration.millis(100), label);
                                    quickFade.setFromValue(currentOpacity);
                                    quickFade.setToValue(0);
                                    quickFade.play();
                                } else {
                                    label.setOpacity(0);
                                }
                            }
                        }
                    }
                    // Reset scale of previous button immediately
                    prevButton.setScaleX(1.0);
                    prevButton.setScaleY(1.0);
                }
                
                // Update current button reference BEFORE starting new animations
                currentlyHoveredButton = button;
                currentFadeIn = fadeIn;
                currentFadeOut = fadeOut;
                currentScaleUp = scaleUp;
                currentScaleDown = scaleDown;
                
                // Start fade in from current opacity (in case re-entering quickly)
                double currentOpacity = hoverLabel.getOpacity();
                fadeIn.setFromValue(currentOpacity);
                fadeIn.playFromStart();
                scaleUp.playFromStart();
            });
            
            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> {
                // Check if we're exiting to empty space (not to another button)
                // If currentlyHoveredButton is still this button, we're going to empty space
                if (currentlyHoveredButton == button) {
                    // Fade out from current opacity (relative to fade in progress)
                    double currentOpacity = hoverLabel.getOpacity();
                    fadeOut.setFromValue(currentOpacity);
                    fadeOut.playFromStart();
                    scaleDown.playFromStart();
                    
                    // Clear after a short delay to allow button-to-button transitions
                    javafx.application.Platform.runLater(() -> {
                        if (currentlyHoveredButton == button) {
                            currentlyHoveredButton = null;
                        }
                    });
                }
                // If currentlyHoveredButton is different, another button's MOUSE_ENTERED already handled it
            });
        }
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
        String[] items = {"My Profile", "Messages", "Log Out"};
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
            case "Log Out" -> onLogout();
        }
    }
    
    private void openMessages() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/user_messages.fxml"));
                Parent messagesRoot = loader.load();
                Object controller = loader.getController();
                if (controller instanceof UserMessagesController umc) {
                    umc.setSoulId(this.soulId);
                }
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) {
                        root.getScene().setRoot(messagesRoot);
                    }
                });
            } catch (Exception ex) { 
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to load messages page: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void openProfile() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
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
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) {
                        root.getScene().setRoot(profileRoot);
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
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
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/private_journals_view.fxml"));
                Parent p = loader.load();
                Object controller = loader.getController();
                if (controller instanceof PrivateJournalsController pc) pc.setSoulId(this.soulId);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) root.getScene().setRoot(p);
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void openPublicJournals() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
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
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) root.getScene().setRoot(p);
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void openBlogs() {
        com.the_pathfinders.util.ActivityTracker.updateActivity(this.soulId);
        
        // Load in background thread to prevent UI freezing
        new Thread(() -> {
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
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) root.getScene().setRoot(p);
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void handleMusicToggle() {
        if (musicToggle != null) {
            boolean enableMusic = musicToggle.isSelected();
            System.out.println("Music toggle clicked: " + (enableMusic ? "ON" : "OFF"));
            
            // Set the preference first
            MusicManager.setBackgroundMusicEnabled(enableMusic);
            updateMusicToggleText();
            
            // Apply the change
            if (enableMusic) {
                System.out.println("Starting background music...");
                MusicManager.playBackgroundMusic();
            } else {
                System.out.println("Stopping background music...");
                MusicManager.stopBackgroundMusic();
            }
        }
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

            VBox questionBox = new VBox(30);
            questionBox.getStyleClass().add("question-box");

            // Question label
            Label questionLabel = new Label(question.question);
            questionLabel.getStyleClass().add("question-label");
            questionLabel.setWrapText(true);
            questionLabel.setMaxWidth(Double.MAX_VALUE);
            questionLabel.setAlignment(javafx.geometry.Pos.CENTER);

            // Create slider container with labels and emojis
            HBox sliderContainer = new HBox(20);
            sliderContainer.getStyleClass().add("slider-container");
            sliderContainer.setAlignment(javafx.geometry.Pos.CENTER);

            // Left side - option labels
            VBox labelsBox = new VBox(0);
            labelsBox.setAlignment(javafx.geometry.Pos.CENTER);
            labelsBox.setPrefWidth(130);

            // Right side - emoji circles
            VBox emojiBox = new VBox(0);
            emojiBox.setAlignment(javafx.geometry.Pos.CENTER);
            emojiBox.setPrefWidth(60);

            // Define emojis and colors (from best to worst)
            String[] emojis = {"😊", "🙂", "😐", "☹️", "😢"};
            String[] emojiStyles = {"emoji-excellent", "emoji-good", "emoji-fair", "emoji-poor", "emoji-worst"};
            String[] subLabels = {"Best", "Good", "Okay", "Not Great", "Worst"};

            // Create labels and emojis for each option (reverse order for top-to-bottom)
            // Loop: i goes from last option to first (3→0), displayIndex goes from 0→3
            for (int i = question.options.size() - 1, displayIndex = 0; i >= 0; i--, displayIndex++) {
                // Option label
                VBox optionLabelBox = new VBox(3);
                optionLabelBox.getStyleClass().add("option-label-box");
                optionLabelBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label mainLabel = new Label(question.options.get(i));
                mainLabel.getStyleClass().add("option-main-label");

                // Use displayIndex for sublabels so they show correctly (Best at top, Worst at bottom)
                Label subLabel = new Label(displayIndex < subLabels.length ? subLabels[displayIndex] : "");
                subLabel.getStyleClass().add("option-sub-label");

                optionLabelBox.getChildren().addAll(mainLabel, subLabel);
                labelsBox.getChildren().add(optionLabelBox);

                // Emoji circle - use displayIndex so emojis show correctly (😊 at top, 😢 at bottom)
                Label emojiCircle = new Label(displayIndex < emojis.length ? emojis[displayIndex] : "");
                emojiCircle.getStyleClass().addAll("emoji-circle", displayIndex < emojiStyles.length ? emojiStyles[displayIndex] : "");
                emojiBox.getChildren().add(emojiCircle);
            }

            // Create vertical slider
            Slider slider = new Slider(1, question.options.size(), question.options.size());
            slider.setOrientation(javafx.geometry.Orientation.VERTICAL);
            slider.getStyleClass().add("mood-slider");
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            slider.setShowTickLabels(false);
            slider.setShowTickMarks(false);
            slider.setPrefHeight(260);

            // Check if already answered
            String savedAnswer = moodAnswers.get(String.valueOf(index));
            boolean hasAnswer = savedAnswer != null;

            if (hasAnswer) {
                // Pre-select saved answer
                // Fix: if answer is at index i, slider value should be i + 1
                for (int i = 0; i < question.options.size(); i++) {
                    if (savedAnswer.equals(question.options.get(i))) {
                        slider.setValue(i + 1);
                        break;
                    }
                }
            } else {
                // Start at middle position but don't save as answer
                double middleValue = (question.options.size() + 1) / 2.0;
                slider.setValue(middleValue);
            }

            // Track if user has interacted with slider
            final boolean[] userInteracted = {hasAnswer};

            // Slider change listener - only save when user actually interacts
            slider.setOnMousePressed(e -> userInteracted[0] = true);
            slider.setOnKeyPressed(e -> userInteracted[0] = true);

            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (userInteracted[0]) {
                    // Fix: slider value maps directly to option index (top=best, bottom=worst)
                    // Slider value 4 (top) should map to index 3 (last option, best score)
                    // Slider value 1 (bottom) should map to index 0 (first option, worst score)
                    int selectedIndex = newVal.intValue() - 1;
                    if (selectedIndex >= 0 && selectedIndex < question.options.size()) {
                        String option = question.options.get(selectedIndex);
                        int score = question.scores[selectedIndex];

                        moodAnswers.put(String.valueOf(currentMoodQuestion), option);
                        moodAnswers.put(String.valueOf(currentMoodQuestion) + "_score", String.valueOf(score));
                        moodAnswers.put(String.valueOf(currentMoodQuestion) + "_category", question.category);
                    }
                }
            });

            sliderContainer.getChildren().addAll(labelsBox, slider, emojiBox);

            questionBox.getChildren().addAll(questionLabel, sliderContainer);
            moodQuestionsContainer.getChildren().add(questionBox);
        }
    }

    private void updateMoodProgress() {
        if (progressCircles == null) return;

        // Update the progress label text
        if (moodProgressLabel != null && moodQuestions != null) {
            moodProgressLabel.setText((currentMoodQuestion + 1) + " OF " + moodQuestions.size());
        }

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

    // ==================== Help Center Methods ====================
    
    private void showHelpCenter() {
        if (helpCenterOverlay != null) {
            helpCenterOverlay.setVisible(true);
            helpCenterOverlay.setManaged(true);
            
            // Fade in animation
            FadeTransition fade = new FadeTransition(Duration.millis(200), helpCenterOverlay);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
            
            // Scale animation for content box
            if (helpCenterContentBox != null) {
                helpCenterContentBox.setScaleX(0.8);
                helpCenterContentBox.setScaleY(0.8);
                ScaleTransition scale = new ScaleTransition(Duration.millis(250), helpCenterContentBox);
                scale.setFromX(0.8);
                scale.setFromY(0.8);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                scale.play();
            }
        }
    }
    
    private void hideHelpCenter() {
        if (helpCenterOverlay != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(150), helpCenterOverlay);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                helpCenterOverlay.setVisible(false);
                helpCenterOverlay.setManaged(false);
            });
            fade.play();
        }
    }
    
    private void showTechnicalSupport() {
        hideHelpCenter();
        if (technicalSupportOverlay != null) {
            technicalSupportOverlay.setVisible(true);
            technicalSupportOverlay.setManaged(true);
            
            FadeTransition fade = new FadeTransition(Duration.millis(200), technicalSupportOverlay);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }
    
    private void hideTechnicalSupport() {
        if (technicalSupportOverlay != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(150), technicalSupportOverlay);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                technicalSupportOverlay.setVisible(false);
                technicalSupportOverlay.setManaged(false);
            });
            fade.play();
        }
    }
    
    private void showEmergencyHotlines() {
        hideHelpCenter();
        if (emergencyHotlinesOverlay != null) {
            emergencyHotlinesOverlay.setVisible(true);
            emergencyHotlinesOverlay.setManaged(true);
            
            FadeTransition fade = new FadeTransition(Duration.millis(200), emergencyHotlinesOverlay);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }
    
    private void hideEmergencyHotlines() {
        if (emergencyHotlinesOverlay != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(150), emergencyHotlinesOverlay);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                emergencyHotlinesOverlay.setVisible(false);
                emergencyHotlinesOverlay.setManaged(false);
            });
            fade.play();
        }
    }
    
    private void populateEmergencyHotlines() {
        if (hotlinesContainer == null) return;
        
        String[][] hotlines = {
            {"Emergency Services", "999", "🚨"},
            {"Helpline (Emotional Support & Suicide Prevention)", "+880 9612-119911", "💚"},
            {"Child Support", "1098", "👶"},
            {"Women & Children Violence", "109 / 10921", "🆘"},
            {"Public Law Services", "16430", "⚖️"},
            {"Disaster Hotline", "10941", "🌪️"},
            {"ACC Hotline", "106", "📞"},
            {"National Information Service", "333", "ℹ️"}
        };
        
        for (String[] hotline : hotlines) {
            VBox card = createHotlineCard(hotline[0], hotline[1], hotline[2]);
            hotlinesContainer.getChildren().add(card);
        }
    }
    
    private VBox createHotlineCard(String service, String number, String emoji) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: linear-gradient(to right, #fff9f9, #ffe6e6); " +
                      "-fx-padding: 15; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 2);");
        
        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px;");
        
        VBox textBox = new VBox(3);
        Label serviceLabel = new Label(service);
        serviceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c62828;");
        
        Label numberLabel = new Label(number);
        numberLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1565c0;");
        
        textBox.getChildren().addAll(serviceLabel, numberLabel);
        header.getChildren().addAll(emojiLabel, textBox);
        
        // Add call button
        Button callBtn = new Button("📞 Call");
        callBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                         "-fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand; " +
                         "-fx-font-weight: bold;");
        callBtn.setOnAction(e -> {
            // Open phone dialer
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("tel:" + number.replaceAll(" / ", "")));
            } catch (Exception ex) {
                // Copy to clipboard as fallback
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(number);
                clipboard.setContent(content);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Number Copied");
                alert.setHeaderText(null);
                alert.setContentText("Phone number copied to clipboard: " + number);
                alert.showAndWait();
            }
        });
        
        card.getChildren().addAll(header, callBtn);
        return card;
    }
    
    private void openEmail(String email) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.MAIL)) {
                desktop.mail(new java.net.URI("mailto:" + email + "?subject=Shelter%20of%20Mind%20Support%20Request"));
            } else {
                // Fallback: copy to clipboard
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(email);
                clipboard.setContent(content);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Email Copied");
                alert.setHeaderText(null);
                alert.setContentText("Email address copied to clipboard:\n" + email);
                alert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open email client. Please contact: " + email);
            alert.showAndWait();
        }
    }
    
    private void saveSafetyPlan() {
        if (soulId == null || soulId.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not Logged In");
            alert.setHeaderText(null);
            alert.setContentText("Please log in to save your safety plan.");
            alert.showAndWait();
            return;
        }
        
        String contact = safetyPlanContact != null ? safetyPlanContact.getText() : "";
        String calm = safetyPlanCalm != null ? safetyPlanCalm.getText() : "";
        String place = safetyPlanPlace != null ? safetyPlanPlace.getText() : "";
        
        if (contact.isBlank() && calm.isBlank() && place.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Safety Plan");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in at least one field to save your safety plan.");
            alert.showAndWait();
            return;
        }
        
        try {
            // Save to database
            SoulRepository soulRepo = new SoulRepository();
            soulRepo.updateSafetyPlan(soulId, contact, calm, place);
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Safety Plan Saved");
            alert.setHeaderText(null);
            alert.setContentText("Your safety plan has been saved successfully.\nIt's also visible to administrators for support purposes.");
            alert.showAndWait();
            
            // Close the popup after successful save
            hideSafetyPlanPopup();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save safety plan: " + ex.getMessage());
            alert.showAndWait();
        }
    }
    
    private void loadSafetyPlan() {
        if (soulId == null || soulId.isBlank()) return;
        
        try {
            SoulRepository soulRepo = new SoulRepository();
            Map<String, String> safetyPlan = soulRepo.getSafetyPlan(soulId);
            
            if (safetyPlan != null) {
                if (safetyPlanContact != null && safetyPlan.get("contact") != null) {
                    safetyPlanContact.setText(safetyPlan.get("contact"));
                }
                if (safetyPlanCalm != null && safetyPlan.get("calm") != null) {
                    safetyPlanCalm.setText(safetyPlan.get("calm"));
                }
                if (safetyPlanPlace != null && safetyPlan.get("place") != null) {
                    safetyPlanPlace.setText(safetyPlan.get("place"));
                }
            }
        } catch (Exception ex) {
            // Silently fail - safety plan is optional
            ex.printStackTrace();
        }
    }
    
    private void showSafetyPlanPopup() {
        if (safetyPlanOverlay != null) {
            // Load existing safety plan data
            loadSafetyPlan();
            
            safetyPlanOverlay.setVisible(true);
            safetyPlanOverlay.setManaged(true);
            
            FadeTransition fade = new FadeTransition(Duration.millis(250), safetyPlanOverlay);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }
    
    private void hideSafetyPlanPopup() {
        if (safetyPlanOverlay != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(200), safetyPlanOverlay);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                safetyPlanOverlay.setVisible(false);
                safetyPlanOverlay.setManaged(false);
            });
            fade.play();
        }
    }
}
