package com.the_pathfinders;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Singleton manager for background video that maintains a single MediaPlayer instance
 * across multiple scene transitions to avoid re-initialization and resource conflicts.
 */
public class VideoManager {
    
    private static VideoManager instance;
    
    private MediaPlayer bgPlayer;
    private MediaView bgView;
    private Rectangle videoOverlay;
    private boolean initialized = false;
    private boolean initFailed = false;
    private AtomicBoolean isInitializing = new AtomicBoolean(false);
    
    private VideoManager() {}
    
    public static VideoManager getInstance() {
        if (instance == null) {
            instance = new VideoManager();
        }
        return instance;
    }
    
    /**
     * Initialize video with retry logic. Calls onSuccess when video is ready or onFailure if all retries fail.
     */
    public void initializeWithRetry(int maxRetries, Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (initialized) {
            onSuccess.accept("Video already initialized");
            return;
        }
        
        if (initFailed) {
            onFailure.accept("Video initialization previously failed");
            return;
        }
        
        if (isInitializing.get()) {
            return; // Already in progress
        }
        
        isInitializing.set(true);
        attemptInitialization(0, maxRetries, onSuccess, onFailure);
    }
    
    private void attemptInitialization(int attemptNumber, int maxRetries, Consumer<String> onSuccess, Consumer<String> onFailure) {
        try {
            URL videoUrl = getClass().getResource("/assets/videos/background.mp4");
            if (videoUrl == null) {
                handleInitFailure("Video file not found", maxRetries, onFailure);
                return;
            }
            
            Media media = new Media(videoUrl.toExternalForm());
            bgPlayer = new MediaPlayer(media);
            bgPlayer.setMute(true);
            bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // Success handler
            bgPlayer.setOnReady(() -> {
                System.out.println("Video ready on attempt " + (attemptNumber + 1));
                initialized = true;
                isInitializing.set(false);
                bgPlayer.play();
                Platform.runLater(() -> onSuccess.accept("Video initialized successfully"));
            });
            
            // Error handler with retry
            bgPlayer.setOnError(() -> {
                System.err.println("Video error on attempt " + (attemptNumber + 1) + ": " + bgPlayer.getError());
                cleanup();
                
                if (attemptNumber < maxRetries - 1) {
                    // Retry after delay
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) {}
                        attemptInitialization(attemptNumber + 1, maxRetries, onSuccess, onFailure);
                    });
                } else {
                    handleInitFailure("All retry attempts failed", maxRetries, onFailure);
                }
            });
            
        } catch (Exception ex) {
            System.err.println("Exception during video init: " + ex.getMessage());
            if (attemptNumber < maxRetries - 1) {
                attemptInitialization(attemptNumber + 1, maxRetries, onSuccess, onFailure);
            } else {
                handleInitFailure("Exception: " + ex.getMessage(), maxRetries, onFailure);
            }
        }
    }
    
    private void handleInitFailure(String reason, int maxRetries, Consumer<String> onFailure) {
        System.err.println("Video initialization failed after " + maxRetries + " attempts: " + reason);
        initFailed = true;
        isInitializing.set(false);
        Platform.runLater(() -> onFailure.accept(reason));
    }
    
    /**
     * Attach the video to a root pane. Creates MediaView and overlay if needed.
     */
    public void attachToPane(AnchorPane root) {
        if (!initialized || bgPlayer == null) {
            System.err.println("Cannot attach video - not initialized");
            return;
        }
        
        Platform.runLater(() -> {
            // Create MediaView if not exists
            if (bgView == null) {
                bgView = new MediaView(bgPlayer);
                bgView.setPreserveRatio(false);
                bgView.setSmooth(true);
                bgView.setCache(true);
                bgView.setMouseTransparent(true);
            }
            
            // Create overlay if not exists
            if (videoOverlay == null) {
                videoOverlay = new Rectangle();
                videoOverlay.setFill(Color.rgb(6, 8, 12, 0.30));
                videoOverlay.setMouseTransparent(true);
            }
            
            // Unbind first if already bound (in case of reattachment)
            bgView.fitWidthProperty().unbind();
            bgView.fitHeightProperty().unbind();
            videoOverlay.widthProperty().unbind();
            videoOverlay.heightProperty().unbind();
            
            // Bind to root dimensions for dynamic resizing
            bgView.fitWidthProperty().bind(root.widthProperty());
            bgView.fitHeightProperty().bind(root.heightProperty());
            videoOverlay.widthProperty().bind(root.widthProperty());
            videoOverlay.heightProperty().bind(root.heightProperty());
            
            // Set anchor pane constraints to ensure full coverage
            AnchorPane.setTopAnchor(bgView, 0.0);
            AnchorPane.setBottomAnchor(bgView, 0.0);
            AnchorPane.setLeftAnchor(bgView, 0.0);
            AnchorPane.setRightAnchor(bgView, 0.0);
            
            AnchorPane.setTopAnchor(videoOverlay, 0.0);
            AnchorPane.setBottomAnchor(videoOverlay, 0.0);
            AnchorPane.setLeftAnchor(videoOverlay, 0.0);
            AnchorPane.setRightAnchor(videoOverlay, 0.0);
            
            // Add to scene graph if not already present
            if (!root.getChildren().contains(bgView)) {
                root.getChildren().add(0, bgView);
            }
            if (!root.getChildren().contains(videoOverlay)) {
                root.getChildren().add(1, videoOverlay);
            }
            
            // Ensure video is playing
            if (bgPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                bgPlayer.play();
            }
        });
    }
    
    /**
     * Detach video from current pane but keep MediaPlayer alive for reuse.
     */
    public void detachFromPane(AnchorPane root) {
        if (root == null) return;
        
        Platform.runLater(() -> {
            if (bgView != null) {
                bgView.fitWidthProperty().unbind();
                bgView.fitHeightProperty().unbind();
                root.getChildren().remove(bgView);
            }
            if (videoOverlay != null) {
                videoOverlay.widthProperty().unbind();
                videoOverlay.heightProperty().unbind();
                root.getChildren().remove(videoOverlay);
            }
        });
    }
    
    /**
     * Check if video is initialized and ready.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Clean up resources (only call on application shutdown).
     */
    private void cleanup() {
        if (bgPlayer != null) {
            try {
                bgPlayer.stop();
                bgPlayer.dispose();
            } catch (Exception ignored) {}
            bgPlayer = null;
        }
        bgView = null;
        videoOverlay = null;
        initialized = false;
    }
    
    /**
     * Shutdown and dispose all resources.
     */
    public void shutdown() {
        cleanup();
        initFailed = false;
        isInitializing.set(false);
    }
}
