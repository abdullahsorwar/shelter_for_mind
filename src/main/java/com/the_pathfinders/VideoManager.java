package com.the_pathfinders;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * across multiple scene transitions. Falls back to static image if video fails on Linux.
 */
public class VideoManager {
    
    private static VideoManager instance;
    
    private MediaPlayer bgPlayer;
    private MediaView bgView;
    private ImageView fallbackImageView;
    private Rectangle videoOverlay;
    private boolean initialized = false;
    private boolean initFailed = false;
    private boolean usingFallback = false;
    private AtomicBoolean isInitializing = new AtomicBoolean(false);
    // Guards to avoid adding duplicate size listeners
    private boolean sizeListenersAdded = false;
    
    private VideoManager() {}
    
    public static VideoManager getInstance() {
        if (instance == null) {
            instance = new VideoManager();
        }
        return instance;
    }
    
    /**
     * Initialize video with retry logic. Tries up to 20 times, then falls back to image.
     */
    public void initializeWithRetry(int maxRetries, Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (initialized) {
            onSuccess.accept(usingFallback ? "Using fallback image" : "Video already initialized");
            return;
        }
        
        if (initFailed && usingFallback) {
            onSuccess.accept("Using fallback image");
            return;
        }
        
        if (isInitializing.get()) {
            return; // Already in progress
        }
        
        isInitializing.set(true);
        attemptInitialization(0, Math.min(maxRetries, 20), onSuccess, onFailure);
    }
    
    private void attemptInitialization(int attemptNumber, int maxRetries, Consumer<String> onSuccess, Consumer<String> onFailure) {
        try {
            URL videoUrl = getClass().getResource("/assets/videos/background.mp4");
            if (videoUrl == null) {
                useFallbackImage(onSuccess);
                return;
            }
            
            Media media = new Media(videoUrl.toExternalForm());
            bgPlayer = new MediaPlayer(media);
            bgPlayer.setMute(true);
            bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // Success handler
            bgPlayer.setOnReady(() -> {
                System.out.println("✓ Video ready on attempt " + (attemptNumber + 1));
                initialized = true;
                usingFallback = false;
                isInitializing.set(false);
                bgPlayer.play();
                Platform.runLater(() -> onSuccess.accept("Video initialized successfully"));
            });
            
            // Error handler with retry
            bgPlayer.setOnError(() -> {
                System.err.println("✗ Video error on attempt " + (attemptNumber + 1) + ": " + bgPlayer.getError());
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
                    System.out.println("→ Video failed after " + maxRetries + " attempts, using fallback image");
                    useFallbackImage(onSuccess);
                }
            });
            
        } catch (Exception ex) {
            System.err.println("Exception during video init attempt " + (attemptNumber + 1) + ": " + ex.getMessage());
            if (attemptNumber < maxRetries - 1) {
                attemptInitialization(attemptNumber + 1, maxRetries, onSuccess, onFailure);
            } else {
                System.out.println("→ Video failed with exception, using fallback image");
                useFallbackImage(onSuccess);
            }
        }
    }
    
    private void useFallbackImage(Consumer<String> onSuccess) {
        try {
            URL imageUrl = getClass().getResource("/assets/images/background.jpg");
            if (imageUrl != null) {
                Image bgImage = new Image(imageUrl.toExternalForm());
                fallbackImageView = new ImageView(bgImage);
                fallbackImageView.setPreserveRatio(false);
                fallbackImageView.setSmooth(true);
                fallbackImageView.setCache(true);
                fallbackImageView.setMouseTransparent(true);
                fallbackImageView.setManaged(false);
                fallbackImageView.setLayoutX(0);
                fallbackImageView.setLayoutY(0);
                
                initialized = true;
                usingFallback = true;
                initFailed = true;
                isInitializing.set(false);
                System.out.println("✓ Fallback image loaded successfully");
                Platform.runLater(() -> onSuccess.accept("Using fallback background image"));
            } else {
                handleInitFailure("Fallback image not found");
            }
        } catch (Exception e) {
            System.err.println("Failed to load fallback image: " + e.getMessage());
            handleInitFailure("Fallback image load failed");
        }
    }
    
    private void handleInitFailure(String reason) {
        System.err.println("✗ Complete initialization failure: " + reason);
        initFailed = true;
        isInitializing.set(false);
    }
    
    /**
     * Attach the video or fallback image to a root pane.
     */
    public void attachToPane(AnchorPane root) {
        if (!initialized) {
            System.err.println("Cannot attach - not initialized");
            return;
        }
        
        Platform.runLater(() -> {
            if (usingFallback) {
                attachFallbackImage(root);
            } else {
                attachVideo(root);
            }
        });
    }
    
    private void attachFallbackImage(AnchorPane root) {
        if (fallbackImageView == null) return;
        
        // Unbind first if already bound (in case of reattachment)
        try {
            fallbackImageView.fitWidthProperty().unbind();
            fallbackImageView.fitHeightProperty().unbind();
        } catch (Exception ignored) {}
        
        // Create overlay if not exists
        if (videoOverlay == null) {
            videoOverlay = new Rectangle();
            videoOverlay.setFill(Color.rgb(6, 8, 12, 0.30));
            videoOverlay.setMouseTransparent(true);
            videoOverlay.setManaged(false);
            videoOverlay.setLayoutX(0);
            videoOverlay.setLayoutY(0);
        }
        
        // Unbind overlay too
        try {
            videoOverlay.widthProperty().unbind();
            videoOverlay.heightProperty().unbind();
        } catch (Exception ignored) {}
        
        // Set initial sizes
        fallbackImageView.setFitWidth(root.getWidth());
        fallbackImageView.setFitHeight(root.getHeight());
        videoOverlay.setWidth(root.getWidth());
        videoOverlay.setHeight(root.getHeight());
        
        // Bind to scene size if available, otherwise bind to root
        if (root.getScene() != null) {
            fallbackImageView.fitWidthProperty().bind(root.getScene().widthProperty());
            fallbackImageView.fitHeightProperty().bind(root.getScene().heightProperty());
            videoOverlay.widthProperty().bind(root.getScene().widthProperty());
            videoOverlay.heightProperty().bind(root.getScene().heightProperty());
        } else {
            fallbackImageView.fitWidthProperty().bind(root.widthProperty());
            fallbackImageView.fitHeightProperty().bind(root.heightProperty());
            videoOverlay.widthProperty().bind(root.widthProperty());
            videoOverlay.heightProperty().bind(root.heightProperty());
            
            // Listen for scene to become available
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        try {
                            fallbackImageView.fitWidthProperty().unbind();
                            fallbackImageView.fitHeightProperty().unbind();
                            videoOverlay.widthProperty().unbind();
                            videoOverlay.heightProperty().unbind();
                        } catch (Exception ignored) {}
                        
                        fallbackImageView.fitWidthProperty().bind(newScene.widthProperty());
                        fallbackImageView.fitHeightProperty().bind(newScene.heightProperty());
                        videoOverlay.widthProperty().bind(newScene.widthProperty());
                        videoOverlay.heightProperty().bind(newScene.heightProperty());
                    });
                }
            });
        }
        
        // Set anchor constraints
        AnchorPane.setTopAnchor(fallbackImageView, 0.0);
        AnchorPane.setBottomAnchor(fallbackImageView, 0.0);
        AnchorPane.setLeftAnchor(fallbackImageView, 0.0);
        AnchorPane.setRightAnchor(fallbackImageView, 0.0);
        
        AnchorPane.setTopAnchor(videoOverlay, 0.0);
        AnchorPane.setBottomAnchor(videoOverlay, 0.0);
        AnchorPane.setLeftAnchor(videoOverlay, 0.0);
        AnchorPane.setRightAnchor(videoOverlay, 0.0);
        
        // Remove from old parent if exists
        if (fallbackImageView.getParent() != null && fallbackImageView.getParent() != root) {
            ((AnchorPane) fallbackImageView.getParent()).getChildren().remove(fallbackImageView);
        }
        if (videoOverlay.getParent() != null && videoOverlay.getParent() != root) {
            ((AnchorPane) videoOverlay.getParent()).getChildren().remove(videoOverlay);
        }
        
        // Add to scene graph if not already present
        if (!root.getChildren().contains(fallbackImageView)) {
            root.getChildren().add(0, fallbackImageView);
        }
        if (!root.getChildren().contains(videoOverlay)) {
            root.getChildren().add(1, videoOverlay);
        }
    }
    
    private void attachVideo(AnchorPane root) {
        if (bgPlayer == null) return;
        
        // Create MediaView if not exists
        if (bgView == null) {
            bgView = new MediaView(bgPlayer);
            bgView.setPreserveRatio(false);
            bgView.setSmooth(true);
            bgView.setCache(true);
            bgView.setMouseTransparent(true);
            // Let us manage sizing manually to avoid AnchorPane layout races
            bgView.setManaged(false);
            bgView.setLayoutX(0);
            bgView.setLayoutY(0);
        }
        
        // Create overlay if not exists
        if (videoOverlay == null) {
            videoOverlay = new Rectangle();
            videoOverlay.setFill(Color.rgb(6, 8, 12, 0.30));
            videoOverlay.setMouseTransparent(true);
            videoOverlay.setManaged(false);
            videoOverlay.setLayoutX(0);
            videoOverlay.setLayoutY(0);
        }
        
        // Unbind first if already bound (in case of reattachment)
        bgView.fitWidthProperty().unbind();
        bgView.fitHeightProperty().unbind();
        videoOverlay.widthProperty().unbind();
        videoOverlay.heightProperty().unbind();
        
        // We'll set fit sizes directly and listen for changes to avoid binding conflicts
        bgView.setFitWidth(root.getWidth());
        bgView.setFitHeight(root.getHeight());
        videoOverlay.setWidth(root.getWidth());
        videoOverlay.setHeight(root.getHeight());

        if (!sizeListenersAdded) {
            sizeListenersAdded = true;
            // React to root size changes
            root.widthProperty().addListener((obs, oldV, newV) -> {
                Platform.runLater(() -> {
                    if (bgView != null) bgView.setFitWidth(newV.doubleValue());
                    if (videoOverlay != null) videoOverlay.setWidth(newV.doubleValue());
                });
            });
            root.heightProperty().addListener((obs, oldV, newV) -> {
                Platform.runLater(() -> {
                    if (bgView != null) bgView.setFitHeight(newV.doubleValue());
                    if (videoOverlay != null) videoOverlay.setHeight(newV.doubleValue());
                });
            });
        }

        // If scene is already present, prefer binding to scene size (ensures fullscreen on maximize)
        if (root.getScene() != null) {
            try { bgView.fitWidthProperty().unbind(); } catch (Exception ignored) {}
            try { bgView.fitHeightProperty().unbind(); } catch (Exception ignored) {}
            try { videoOverlay.widthProperty().unbind(); } catch (Exception ignored) {}
            try { videoOverlay.heightProperty().unbind(); } catch (Exception ignored) {}
            bgView.fitWidthProperty().bind(root.getScene().widthProperty());
            bgView.fitHeightProperty().bind(root.getScene().heightProperty());
            videoOverlay.widthProperty().bind(root.getScene().widthProperty());
            videoOverlay.heightProperty().bind(root.getScene().heightProperty());
        }

        // Also listen for the scene to become available later and rebind to it
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            Platform.runLater(() -> {
                if (newScene != null) {
                    try { bgView.fitWidthProperty().unbind(); } catch (Exception ignored) {}
                    try { bgView.fitHeightProperty().unbind(); } catch (Exception ignored) {}
                    try { videoOverlay.widthProperty().unbind(); } catch (Exception ignored) {}
                    try { videoOverlay.heightProperty().unbind(); } catch (Exception ignored) {}
                    // Also listen to scene size changes so maximizing/restoring is handled
                    Platform.runLater(() -> {
                        if (newScene != null) {
                            double w = newScene.getWidth();
                            double h = newScene.getHeight();
                            bgView.setFitWidth(w);
                            bgView.setFitHeight(h);
                            videoOverlay.setWidth(w);
                            videoOverlay.setHeight(h);
                            newScene.widthProperty().addListener((o, ov, nv) -> {
                                Platform.runLater(() -> {
                                    if (bgView != null) bgView.setFitWidth(nv.doubleValue());
                                    if (videoOverlay != null) videoOverlay.setWidth(nv.doubleValue());
                                });
                            });
                            newScene.heightProperty().addListener((o, ov, nv) -> {
                                Platform.runLater(() -> {
                                    if (bgView != null) bgView.setFitHeight(nv.doubleValue());
                                    if (videoOverlay != null) videoOverlay.setHeight(nv.doubleValue());
                                });
                            });
                        }
                    });
                } else {
                    try { bgView.fitWidthProperty().unbind(); } catch (Exception ignored) {}
                    try { bgView.fitHeightProperty().unbind(); } catch (Exception ignored) {}
                    try { videoOverlay.widthProperty().unbind(); } catch (Exception ignored) {}
                    try { videoOverlay.heightProperty().unbind(); } catch (Exception ignored) {}
                    bgView.fitWidthProperty().bind(root.widthProperty());
                    bgView.fitHeightProperty().bind(root.heightProperty());
                    videoOverlay.widthProperty().bind(root.widthProperty());
                    videoOverlay.heightProperty().bind(root.heightProperty());
                }
            });
        });
        
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
    }
    
    /**
     * Detach video/image from current pane but keep resources alive for reuse.
     */
    public void detachFromPane(AnchorPane root) {
        if (root == null) return;
        
        Platform.runLater(() -> {
            if (usingFallback && fallbackImageView != null) {
                fallbackImageView.fitWidthProperty().unbind();
                fallbackImageView.fitHeightProperty().unbind();
                root.getChildren().remove(fallbackImageView);
            }
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
        fallbackImageView = null;
        videoOverlay = null;
        initialized = false;
        usingFallback = false;
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
