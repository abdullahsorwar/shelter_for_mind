package com.the_pathfinders.util;

import com.the_pathfinders.Journal;
import com.the_pathfinders.db.JournalRepository;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Utility class for journal-related operations including:
 * - Real-time timestamp updates
 * - Real-time love count updates
 * - Love/unlike toggle functionality
 * - Heart icon creation and management
 */
public class JournalUtils {
    private static final JournalRepository journalRepo = new JournalRepository();
    
    /**
     * Calculate relative time string from LocalDateTime
     */
    public static String getRelativeTime(LocalDateTime dt) {
        if (dt == null) return "";
        long diff = java.time.Duration.between(dt, LocalDateTime.now()).toMillis();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + (days == 1 ? " day ago" : " days ago");
        if (hours > 0) return hours + (hours == 1 ? " hour ago" : " hours ago");
        if (minutes > 0) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (seconds > 0) return seconds + (seconds == 1 ? " second ago" : " seconds ago");
        return "just now";
    }
    
    /**
     * Create a Timeline that updates timestamps every second
     * @param journals Map of journal ID to timestamp label
     * @param journalData Map of journal ID to Journal object
     * @return Timeline for timestamp updates
     */
    public static Timeline createTimestampUpdateTimeline(
            Map<String, Label> journals, 
            Map<String, Journal> journalData) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            try {
                // Create a snapshot to avoid ConcurrentModificationException
                java.util.Set<Map.Entry<String, Label>> entries = new java.util.HashSet<>(journals.entrySet());
                for (Map.Entry<String, Label> entry : entries) {
                    String journalId = entry.getKey();
                    Label timestampLabel = entry.getValue();
                    Journal journal = journalData.get(journalId);
                    if (journal != null && journal.getCreatedAt() != null) {
                        timestampLabel.setText(getRelativeTime(journal.getCreatedAt()));
                    }
                }
            } catch (Exception ex) {
                // Silently handle any concurrent modification issues
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }
    
    /**
     * Create a Timeline that updates love counts every 2 seconds
     * @param journals Map of journal ID to love count label
     * @param journalData Map of journal ID to Journal object (will be updated)
     * @return Timeline for love count updates
     */
    public static Timeline createLoveCountUpdateTimeline(
            Map<String, Label> journals, 
            Map<String, Journal> journalData) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            new Thread(() -> {
                try {
                    // Create a snapshot to avoid ConcurrentModificationException
                    java.util.Set<Map.Entry<String, Label>> entries = new java.util.HashSet<>(journals.entrySet());
                    for (Map.Entry<String, Label> entry : entries) {
                        String journalId = entry.getKey();
                        Label countLabel = entry.getValue();
                        Journal journal = journalData.get(journalId);
                        if (journal != null) {
                            int newCount = journalRepo.getLoveCount(journalId);
                            if (newCount != journal.getLoveCount()) {
                                journal.setLoveCount(newCount);
                                Platform.runLater(() -> countLabel.setText(String.valueOf(newCount)));
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Silently handle any concurrent modification issues
                }
            }).start();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }
    
    /**
     * Create heart icon ImageView
     * @param iconPath Path to heart icon (e.g., "/assets/icons/heart_filled.png")
     * @param size Size of the icon (width and height)
     * @return ImageView with heart icon
     */
    public static ImageView createHeartIcon(String iconPath, double size) {
        ImageView icon = new ImageView();
        try {
            URL url = JournalUtils.class.getResource(iconPath);
            if (url != null) {
                icon.setImage(new Image(url.toExternalForm(), size, size, true, true));
            }
        } catch (Exception e) {
            System.err.println("Failed to load heart icon: " + iconPath + " - " + e.getMessage());
        }
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        return icon;
    }
    
    /**
     * Toggle love status for a journal
     * @param journalId Journal ID
     * @param userId User ID
     * @param loveBtn Button containing heart icon
     * @param countLabel Label showing love count
     * @param journal Journal object (will be updated)
     * @param heartOutline Outline heart icon
     * @param heartFilled Filled heart icon
     */
    public static void toggleLove(
            String journalId, 
            String userId,
            Button loveBtn,
            Label countLabel,
            Journal journal,
            ImageView heartOutline,
            ImageView heartFilled) {
        loveBtn.setDisable(true);
        loveBtn.setFocusTraversable(false); // Prevent focus changes
        new Thread(() -> {
            try {
                boolean nowLoved = journalRepo.toggleLove(journalId, userId);
                int newCount = journalRepo.getLoveCount(journalId);
                journal.setLoveCount(newCount);

                // Update UI without affecting scroll position
                Platform.runLater(() -> {
                    loveBtn.setGraphic(nowLoved ? heartFilled : heartOutline);
                    if (nowLoved) loveBtn.getStyleClass().add("loved"); else loveBtn.getStyleClass().remove("loved");
                    countLabel.setText(String.valueOf(newCount));
                    loveBtn.setDisable(false);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> loveBtn.setDisable(false));
            }
        }).start();
    }
    
    /**
     * Check if user has loved a journal and set initial icon
     * @param journalId Journal ID
     * @param userId User ID
     * @param loveBtn Button containing heart icon
     * @param heartOutline Outline heart icon
     * @param heartFilled Filled heart icon
     */
    public static void setInitialLoveState(
            String journalId,
            String userId,
            Button loveBtn,
            ImageView heartOutline,
            ImageView heartFilled) {
        new Thread(() -> {
            try {
                boolean isLoved = journalRepo.hasUserLoved(journalId, userId);
                Platform.runLater(() -> {
                    loveBtn.setGraphic(isLoved ? heartFilled : heartOutline);
                    if (isLoved) loveBtn.getStyleClass().add("loved"); else loveBtn.getStyleClass().remove("loved");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
