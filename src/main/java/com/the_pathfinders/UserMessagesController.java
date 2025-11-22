package com.the_pathfinders;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import com.the_pathfinders.db.ModerationRepository;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UserMessagesController implements Initializable {

    @FXML private Pane root;
    @FXML private Pane backgroundPane;
    @FXML private Rectangle gradientRect;
    @FXML private VBox mainContainer;
    @FXML private VBox headerBox;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Button backButton;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private VBox emptyState;
    @FXML private Label unreadCountLabel;
    @FXML private Button markAllReadBtn;
    
    private String soulId;
    
    public void setSoulId(String soulId) {
        this.soulId = soulId;
        loadMessages();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind scene size to background panes and main container
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, ov, nv) -> {
                    double width = nv.doubleValue();
                    if (backgroundPane != null) {
                        backgroundPane.setPrefWidth(width);
                    }
                    if (gradientRect != null) {
                        gradientRect.setWidth(width);
                    }
                    if (mainContainer != null) {
                        mainContainer.setPrefWidth(width);
                    }
                    if (headerBox != null) {
                        headerBox.setPrefWidth(width);
                    }
                });
                
                newScene.heightProperty().addListener((o, ov, nv) -> {
                    double height = nv.doubleValue();
                    if (backgroundPane != null) {
                        backgroundPane.setPrefHeight(height);
                    }
                    if (gradientRect != null) {
                        gradientRect.setHeight(height);
                    }
                    if (mainContainer != null) {
                        mainContainer.setPrefHeight(height);
                    }
                });
                
                // Initialize dimensions
                Platform.runLater(() -> {
                    double width = newScene.getWidth();
                    double height = newScene.getHeight();
                    
                    if (backgroundPane != null) {
                        backgroundPane.setPrefWidth(width);
                        backgroundPane.setPrefHeight(height);
                    }
                    if (gradientRect != null) {
                        gradientRect.setWidth(width);
                        gradientRect.setHeight(height);
                    }
                    if (mainContainer != null) {
                        mainContainer.setPrefWidth(width);
                        mainContainer.setPrefHeight(height);
                    }
                    if (headerBox != null) {
                        headerBox.setPrefWidth(width);
                    }
                });
            }
        });
        
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            DashboardController controller = loader.getController();
            controller.setSoulId(this.soulId);
            
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dashboardRoot);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleMarkAllRead() {
        new Thread(() -> {
            try {
                ModerationRepository.markAllMessagesAsRead(this.soulId);
                Platform.runLater(() -> {
                    loadMessages();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to mark messages as read: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void loadMessages() {
        if (this.soulId == null) {
            return;
        }
        
        messagesContainer.getChildren().clear();
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        
        new Thread(() -> {
            try {
                List<ModerationRepository.ModerationMessage> messages = 
                    ModerationRepository.getMessagesForSoul(this.soulId);
                
                int unreadCount = (int) messages.stream().filter(m -> !m.isRead).count();
                
                Platform.runLater(() -> {
                    if (messages.isEmpty()) {
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                        unreadCountLabel.setText("No messages");
                        markAllReadBtn.setDisable(true);
                    } else {
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        
                        if (unreadCount > 0) {
                            unreadCountLabel.setText(unreadCount + " unread message" + (unreadCount > 1 ? "s" : ""));
                            markAllReadBtn.setDisable(false);
                        } else {
                            unreadCountLabel.setText("All messages read");
                            markAllReadBtn.setDisable(true);
                        }
                        
                        for (ModerationRepository.ModerationMessage msg : messages) {
                            messagesContainer.getChildren().add(createMessageCard(msg));
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load messages: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load messages: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private VBox createMessageCard(ModerationRepository.ModerationMessage msg) {
        VBox card = new VBox(14);
        card.getStyleClass().add("message-card");
        if (!msg.isRead) {
            card.getStyleClass().add("unread-message");
        }
        
        // Header with date and read status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label(msg.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        dateLabel.getStyleClass().add("message-date-label");
        HBox.setHgrow(dateLabel, Priority.ALWAYS);
        
        Label statusLabel = new Label(msg.isRead ? "✓ Read" : "● Unread");
        statusLabel.getStyleClass().addAll("status-badge", msg.isRead ? "status-read" : "status-unread");
        
        header.getChildren().addAll(dateLabel, statusLabel);
        
        // Journal reference
        if (msg.journalTitle != null && !msg.journalTitle.isEmpty()) {
            Label journalLabel = new Label("📝 Regarding: " + msg.journalTitle);
            journalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            journalLabel.getStyleClass().add("message-journal-label");
            card.getChildren().add(journalLabel);
        }
        
        card.getChildren().add(header);
        
        // Message content
        Label messageLabel = new Label(msg.messageContent);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4px;");
        messageLabel.getStyleClass().add("message-content-label");
        messageLabel.setWrapText(true);
        
        // Sender info (hidden from user, but tracked in system)
        Label senderLabel = new Label("From: Moderation Team");
        senderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        senderLabel.getStyleClass().add("message-sender-label");
        
        // Mark as read button
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (!msg.isRead) {
            Button markReadBtn = new Button("✓ Mark as Read");
            markReadBtn.getStyleClass().add("mark-read-btn");
            markReadBtn.setOnAction(e -> markMessageAsRead(msg.messageId));
            actionBox.getChildren().add(markReadBtn);
        }
        
        card.getChildren().addAll(messageLabel, senderLabel, actionBox);
        
        return card;
    }
    
    private void markMessageAsRead(int messageId) {
        new Thread(() -> {
            try {
                ModerationRepository.markMessageAsRead(messageId);
                Platform.runLater(() -> {
                    loadMessages();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to mark message as read: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
