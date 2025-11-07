package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PublicJournalsController {

    @FXML private VBox root;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox journalsContainer;
    @FXML private Button backBtn;

    private String currentSoulId = "";
    private JournalRepository journalRepo;

    public void setSoulId(String id) {
        this.currentSoulId = id == null ? "" : id;
    }

    @FXML
    public void initialize() {
        journalRepo = new JournalRepository();

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToJournal());
        }

        // Load journals in background
        loadJournals();
    }

    private void loadJournals() {
        new Thread(() -> {
            try {
                List<Journal> journals = journalRepo.getAllPublicJournals();

                Platform.runLater(() -> {
                    journalsContainer.getChildren().clear();
                    
                    for (Journal journal : journals) {
                        VBox journalBox = createJournalBox(journal);
                        journalsContainer.getChildren().add(journalBox);
                        
                        // Smooth bump animation
                        journalBox.setScaleX(0);
                        journalBox.setScaleY(0);
                        ScaleTransition bump = new ScaleTransition(Duration.seconds(1), journalBox);
                        bump.setFromX(0);
                        bump.setFromY(0);
                        bump.setToX(1);
                        bump.setToY(1);
                        bump.setDelay(Duration.millis(journalsContainer.getChildren().indexOf(journalBox) * 50));
                        bump.play();
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load journals: " + ex.getMessage());
                    error.setStyle("-fx-text-fill: red;");
                    journalsContainer.getChildren().add(error);
                });
            }
        }).start();
    }

    private VBox createJournalBox(Journal journal) {
        // Outer VBox (solid gray)
        VBox outerBox = new VBox(10);
        outerBox.getStyleClass().add("journal-outer-box");
        outerBox.setPadding(new Insets(15));
        VBox.setMargin(outerBox, new Insets(10, 10, 10, 10));

        // Inner VBox (glassy)
        VBox innerBox = new VBox(10);
        innerBox.getStyleClass().add("journal-inner-box");
        innerBox.setPadding(new Insets(15));

        // Top section: User info
        HBox userInfoBox = new HBox(10);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);

        // User icon
        ImageView userIcon = new ImageView();
        try {
            URL iconUrl = getClass().getResource("/assets/icons/user.png");
            if (iconUrl != null) {
                Image img = new Image(iconUrl.toExternalForm(), 40, 40, true, true);
                userIcon.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Failed to load user icon: " + e.getMessage());
        }
        userIcon.setFitWidth(40);
        userIcon.setFitHeight(40);
        userIcon.setPreserveRatio(true);
        
        // Clip to circle
        Rectangle clip = new Rectangle(40, 40);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        userIcon.setClip(clip);

        // Username and time
        VBox userTextBox = new VBox(2);
        Label usernameLabel = new Label(journal.getSoulId());
        usernameLabel.getStyleClass().add("journal-username");
        usernameLabel.setStyle("-fx-font-weight: bold;"); // Bold username
        
        Label timeLabel = new Label(getRelativeTime(journal.getCreatedAt()));
        timeLabel.getStyleClass().add("journal-time");

        userTextBox.getChildren().addAll(usernameLabel, timeLabel);
        userInfoBox.getChildren().addAll(userIcon, userTextBox);

        // Gray separator line
        Region separator = new Region();
        separator.getStyleClass().add("journal-separator");
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);

        // Journal text
        Label journalText = new Label(journal.getText());
        journalText.setWrapText(true);
        journalText.getStyleClass().add("journal-text");

        // Love section
        HBox loveBox = new HBox(8);
        loveBox.setAlignment(Pos.CENTER_LEFT);

        Button loveBtn = new Button();
        loveBtn.getStyleClass().add("love-button");
        
        // Create ImageViews for heart icons
        ImageView heartOutline = createHeartIcon("/assets/icons/heart_outline.png");
        ImageView heartFilled = createHeartIcon("/assets/icons/heart_filled.png");
        
        // Set initial icon
        loveBtn.setGraphic(heartOutline);
        
        Label loveCountLabel = new Label("0");
        loveCountLabel.getStyleClass().add("love-count");

        // Check if current user has loved this journal
        new Thread(() -> {
            try {
                boolean isLoved = journalRepo.hasUserLoved(journal.getId(), currentSoulId);
                int count = journalRepo.getLoveCount(journal.getId());

                Platform.runLater(() -> {
                    loveBtn.setGraphic(isLoved ? heartFilled : heartOutline);
                    if (isLoved) {
                        loveBtn.getStyleClass().add("loved");
                    }
                    loveCountLabel.setText(String.valueOf(count));
                });

            } catch (Exception ignored) {}
        }).start();

        // Love button action
        loveBtn.setOnAction(e -> {
            loveBtn.setDisable(true);
            new Thread(() -> {
                try {
                    boolean nowLoved = journalRepo.toggleLove(journal.getId(), currentSoulId);
                    int newCount = journalRepo.getLoveCount(journal.getId());

                    Platform.runLater(() -> {
                        loveBtn.setGraphic(nowLoved ? heartFilled : heartOutline);
                        if (nowLoved) {
                            loveBtn.getStyleClass().add("loved");
                        } else {
                            loveBtn.getStyleClass().remove("loved");
                        }
                        loveCountLabel.setText(String.valueOf(newCount));
                        loveBtn.setDisable(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> loveBtn.setDisable(false));
                }
            }).start();
        });

        loveBox.getChildren().addAll(loveBtn, loveCountLabel);

        // Assemble inner box
        innerBox.getChildren().addAll(userInfoBox, separator, journalText, loveBox);
        outerBox.getChildren().add(innerBox);

        return outerBox;
    }

    private ImageView createHeartIcon(String path) {
        ImageView icon = new ImageView();
        try {
            URL iconUrl = getClass().getResource(path);
            if (iconUrl != null) {
                Image img = new Image(iconUrl.toExternalForm(), 24, 24, true, true);
                icon.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Failed to load heart icon: " + path + " - " + e.getMessage());
        }
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        icon.setPreserveRatio(true);
        return icon;
    }

    private String getRelativeTime(LocalDateTime timestamp) {
        if (timestamp == null) return "just now";

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(timestamp, now);

        if (seconds < 60) {
            return seconds + " seconds ago";
        }

        long minutes = ChronoUnit.MINUTES.between(timestamp, now);
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }

        long hours = ChronoUnit.HOURS.between(timestamp, now);
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }

        long days = ChronoUnit.DAYS.between(timestamp, now);
        if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }

        long months = ChronoUnit.MONTHS.between(timestamp, now);
        if (months < 12) {
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        }

        long years = ChronoUnit.YEARS.between(timestamp, now);
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }

    private void goBackToJournal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
            Parent journalRoot = loader.load();

            Object controller = loader.getController();
            if (controller instanceof JournalController jc) {
                jc.setSoulId(this.currentSoulId);
            }

            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(journalRoot);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
