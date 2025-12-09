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
                // Mark both moderation messages and user messages as read
                ModerationRepository.markAllMessagesAsRead(this.soulId);
                com.the_pathfinders.db.UserMessageRepository.markAllAsRead(this.soulId);
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
                // Load both moderation messages AND appointment messages
                List<ModerationRepository.ModerationMessage> moderationMessages =
                    ModerationRepository.getMessagesForSoul(this.soulId);

                List<com.the_pathfinders.db.UserMessage> userMessages =
                    com.the_pathfinders.db.UserMessageRepository.getMessagesForUser(this.soulId);

                int moderationUnread = (int) moderationMessages.stream().filter(m -> !m.isRead).count();
                int userUnread = (int) userMessages.stream().filter(m -> !m.isRead()).count();
                int totalUnread = moderationUnread + userUnread;

                Platform.runLater(() -> {
                    if (moderationMessages.isEmpty() && userMessages.isEmpty()) {
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                        unreadCountLabel.setText("No messages");
                        markAllReadBtn.setDisable(true);
                    } else {
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        
                        if (totalUnread > 0) {
                            unreadCountLabel.setText(totalUnread + " unread message" + (totalUnread > 1 ? "s" : ""));
                            markAllReadBtn.setDisable(false);
                        } else {
                            unreadCountLabel.setText("All messages read");
                            markAllReadBtn.setDisable(true);
                        }
                        
                        // Display appointment messages first (newer feature)
                        for (com.the_pathfinders.db.UserMessage msg : userMessages) {
                            messagesContainer.getChildren().add(createUserMessageCard(msg));
                        }

                        // Then moderation messages
                        for (ModerationRepository.ModerationMessage msg : moderationMessages) {
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

    private void markUserMessageAsRead(long messageId) {
        new Thread(() -> {
            try {
                com.the_pathfinders.db.UserMessageRepository.markAsRead(messageId);
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

    private VBox createUserMessageCard(com.the_pathfinders.db.UserMessage msg) {
        VBox card = new VBox(14);
        card.getStyleClass().add("message-card");
        if (!msg.isRead()) {
            card.getStyleClass().add("unread-message");
        }

        // Header with date and read status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label typeIcon = new Label(getMessageIcon(msg.getMessageType()));
        typeIcon.setStyle("-fx-font-size: 20px;");

        Label subjectLabel = new Label(msg.getSubject());
        subjectLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        subjectLabel.getStyleClass().add("message-subject-label");
        HBox.setHgrow(subjectLabel, Priority.ALWAYS);

        Label statusLabel = new Label(msg.isRead() ? "✓ Read" : "● Unread");
        statusLabel.getStyleClass().addAll("status-badge", msg.isRead() ? "status-read" : "status-unread");

        header.getChildren().addAll(typeIcon, subjectLabel, statusLabel);

        // Date
        Label dateLabel = new Label(msg.getCreatedAt());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        dateLabel.getStyleClass().add("message-date-label");

        card.getChildren().addAll(header, dateLabel);

        // Separator
        Separator separator = new Separator();
        card.getChildren().add(separator);

        // Message content
        Label messageLabel = new Label(msg.getMessageContent());
        messageLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4px;");
        messageLabel.getStyleClass().add("message-content-label");
        messageLabel.setWrapText(true);

        card.getChildren().add(messageLabel);

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        if (!msg.isRead()) {
            Button markReadBtn = new Button("✓ Mark as Read");
            markReadBtn.getStyleClass().add("mark-read-btn");
            markReadBtn.setOnAction(e -> markUserMessageAsRead(msg.getId()));
            actionBox.getChildren().add(markReadBtn);
        }

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> deleteUserMessage(msg.getId()));
        actionBox.getChildren().add(deleteBtn);

        card.getChildren().add(actionBox);

        return card;
    }

    private String getMessageIcon(String messageType) {
        return switch (messageType) {
            case "APPOINTMENT_CONFIRMED" -> "✅";
            case "APPOINTMENT_RESCHEDULED" -> "📅";
            case "MODERATION" -> "✉️";
            case "SYSTEM" -> "ℹ️";
            default -> "📬";
        };
    }

    private void deleteUserMessage(long messageId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Message");
        confirm.setHeaderText("Delete this message?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        com.the_pathfinders.db.UserMessageRepository.deleteMessage(messageId);
                        Platform.runLater(() -> {
                            loadMessages();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to delete message: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
