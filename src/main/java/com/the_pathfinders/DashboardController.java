package com.the_pathfinders;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalTime;
import java.util.Optional;

import com.the_pathfinders.db.SoulRepository;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
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
    @FXML private VBox buttonCardsBox;

    // Journaling popup overlay elements
    @FXML private StackPane journalingOverlay;
    @FXML private VBox journalingContentBox;
    @FXML private ImageView journalingIcon;
    @FXML private Label journalingTitle;
    @FXML private Label journalingSubtitle;
    @FXML private VBox journalingButtonsBox;
    @FXML private Button createJournalBtn;
    @FXML private Button viewJournalsBtn;

    private String soulId;
    private double dragStartX = 0;

    public void initialize() {
        setLogo();
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

        if (userImage != null) {
            userImage.setOnMouseClicked(e -> toggleUserMenu());
        }
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
    private void openToDo() {
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

    public void setUser(String id, String name) {
        this.soulId = id == null ? "" : id;
        if (userLabel != null) userLabel.setText(this.soulId);
        // Ensure greeting reflects current time when user is set (scene may be swapped after initialize)
        try { if (greetingLabel != null) updateGreeting(); } catch (Exception ignored) {}
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + this.soulId + ".jpg");
            if (u == null) u = getClass().getResource("/assets/icons/user.png");
            if (u != null && userImage != null) {
                Image img = new Image(u.toExternalForm(), 38, 38, true, true);
                userImage.setImage(img);
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
            u = getClass().getResource("/assets/images/logo_new.png");
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
        String[] items = {"My Profile", "Starred Journals", "Log Out"};
        for (int i = 0; i < items.length; i++) {
            Button b = new Button(items[i]);
            b.getStyleClass().add("dropdown-item");
            userDropdown.getChildren().add(b);
            final int idx = i;
            b.setOnAction(e -> handleDropdownSelection(items[idx]));
        }
        userDropdown.getStyleClass().add("dropdown-container");
    }

    private void handleDropdownSelection(String which) {
        switch (which) {
            case "My Profile" -> openProfile();
            case "Starred Journals" -> showStarredPlaceholder();
            case "Log Out" -> onLogout();
        }
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/profile.fxml"));
            Parent profileRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ProfileController pc) {
                pc.setSoulId(this.soulId);
                pc.onShown();
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(profileRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showStarredPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Starred journals feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/private_journals_view.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PrivateJournalsController pc) pc.setSoulId(this.soulId);
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openPublicJournals() {
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

    private void showMoodPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Mood Tracker feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }

    private void showInsightsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Insights feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }

    private void showSettingsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Settings feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
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
}
