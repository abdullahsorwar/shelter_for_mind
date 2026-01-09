package com.the_pathfinders;

import com.the_pathfinders.db.SoulRepository;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Action;

public class InitialController implements Initializable {

    @FXML private Pane root;

    @FXML private ImageView bgImage;
    @FXML private ImageView logoImage;

    @FXML private Pane contentPane;
    @FXML private javafx.scene.text.Text loginAsText;

    @FXML private Pane buttonsPane;
    @FXML private ImageView soulImage;
    @FXML private ImageView keeperImage;
    @FXML private Rectangle whiteOverlay;

    @FXML private javafx.scene.text.Text pleaseWaitText;
    
    private boolean videoReady = false;
    private static boolean skipIntro = false;
    // Minimum fade-in time for "please wait" in milliseconds (configurable)
    // Set to 2000 ms (fixed fade-in)
    private double pleaseWaitMinMs = 2000.0;
    // Actual randomized fade duration (ms) computed when showing the text
    private long pleaseWaitFadeDurationMs = 0;
    // Timestamp when pleaseWait was shown
    private long pleaseWaitShownAt = 0;
    // Guard to prevent multiple concurrent please-wait sequences
    private boolean pleaseWaitInProgress = false;
    // Keep references to running transitions so we can stop them for a clean handoff
    private FadeTransition currentFadeIn = null;
    private FadeTransition currentFadeOut = null;
    // Ping-pong loop control
    private boolean pingPongRunning = false;
    private boolean atLeastOneFadeInCompleted = false;
    private Runnable pendingAfter = null;

    public static void setSkipIntro(boolean skip) {
        skipIntro = skip;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load images first
        trySetImage(
            bgImage,
            "/assets/images/green_bg_new.png",
            "/green_bg_new.png",
            "/assets/images/green_bg.jpg",
            "/green_bg.jpg"
        );
        trySetImage(logoImage,
            "/assets/images/shelter_of_mind.png",
            "/assets/images/logo_testing.png",
            "/logo_testing.png");

        Platform.runLater(() -> {
            // Scale down the 972x972px logo to fit within bounds
            logoImage.setFitHeight(300);
            logoImage.setFitWidth(300);
            logoImage.setSmooth(true);
            logoImage.setCache(true);
            
            // Root grows with window
            root.prefWidthProperty().bind(root.getScene().widthProperty());
            root.prefHeightProperty().bind(root.getScene().heightProperty());

            // White overlay covers window
            whiteOverlay.widthProperty().bind(root.widthProperty());
            whiteOverlay.heightProperty().bind(root.heightProperty());

            // Background fills window (no letterbox)
            bgImage.fitWidthProperty().bind(root.widthProperty());
            bgImage.fitHeightProperty().bind(root.heightProperty());
            bgImage.setPreserveRatio(false);
            bgImage.setSmooth(true);

            // NEW: make sure there are no positional offsets on the background
            bgImage.setLayoutX(0);
            bgImage.setLayoutY(0);

            // Center the fixed-size content pane
            centerContent();
            root.widthProperty().addListener((o, ov, nv) -> centerContent());
            root.heightProperty().addListener((o, ov, nv) -> centerContent());
            // Recenter when content's layout size changes (avoid boundsInParent to prevent feedback loop)
            contentPane.layoutBoundsProperty().addListener((o, ov, nv) -> centerContent());
            
            // Delay intro animation until after scene is fully rendered (or skip if requested)
            Platform.runLater(() -> {
                if (skipIntro) {
                    applyFinalState();
                    skipIntro = false;
                } else {
                    // Add a delay to ensure everything is rendered
                    PauseTransition initialDelay = new PauseTransition(Duration.millis(500));
                    initialDelay.setOnFinished(e -> playIntroSequence());
                    initialDelay.play();
                }
            });
        });

        // Click wiring - start video initialization on Soul button click
        if (soulImage != null) {
            soulImage.setOnMouseClicked(e -> onSoulClick());
        }
        if (keeperImage != null) {
            keeperImage.setOnMouseClicked(e -> onKeeperClick());
        }
    }

    private void onSoulClick() {
        // Always show please-wait on click, then proceed.
        showPleaseWait();
        VideoManager videoManager = VideoManager.getInstance();
        if (videoManager.isInitialized()) {
            videoReady = true;
            hidePleaseWaitAndRun(() -> goToLoginSignup());
        } else {
            initializeBackgroundVideo();
        }
    }

    private void initializeBackgroundVideo() {
        VideoManager videoManager = VideoManager.getInstance();

        // If already initialized, set ready and wait for the please-wait transition
        if (videoManager.isInitialized()) {
            videoReady = true;
            hidePleaseWaitAndRun(() -> goToLoginSignup());
            return;
        }

        // Show "Please wait..." message (idempotent)
        showPleaseWait();

        // Initialize with 500 retry attempts
        videoManager.initializeWithRetry(
            500, // max retries
            (successMsg) -> {
                // On success
                System.out.println("Video initialization succeeded: " + successMsg);
                // Wait for the please-wait fade-in to complete before navigating
                hidePleaseWaitAndRun(() -> {
                    videoReady = true;
                    goToLoginSignup();
                });
            },
            (failureMsg) -> {
                // On failure - still navigate but without video
                System.err.println("Video initialization failed: " + failureMsg);
                videoReady = true; // Allow navigation anyway
                hidePleaseWaitAndRun(() -> goToLoginSignup());
            }
        );
    }
    
    private void showPleaseWait() {
        if (pleaseWaitText == null) return;
        // If a fade-out is running, stop it so fade-in is clear
        if (currentFadeOut != null) {
            currentFadeOut.stop();
            currentFadeOut = null;
        }
        // If already in progress, do nothing (ping-pong already running)
        if (pleaseWaitInProgress) return;
        pleaseWaitInProgress = true;
        atLeastOneFadeInCompleted = false;
        pingPongRunning = true;
        pleaseWaitFadeDurationMs = (long) pleaseWaitMinMs;
        pleaseWaitShownAt = System.currentTimeMillis();

        startPingPongLoop();
    }

    private void startPingPongLoop() {
        // Initial fixed fade-in (2000ms)
        Platform.runLater(() -> {
            try { pleaseWaitText.setOpacity(0.0); } catch (Exception ignored) {}
            if (currentFadeIn != null) currentFadeIn.stop();
            currentFadeIn = new FadeTransition(Duration.millis(pleaseWaitFadeDurationMs), pleaseWaitText);
            currentFadeIn.setFromValue(0.0);
            currentFadeIn.setToValue(1.0);
            currentFadeIn.setInterpolator(Interpolator.EASE_BOTH);
            currentFadeIn.setOnFinished(e -> {
                currentFadeIn = null;
                atLeastOneFadeInCompleted = true;
                // If someone requested hide before first fade-in completed, honor it now
                if (pendingAfter != null) {
                    Runnable a = pendingAfter;
                    pendingAfter = null;
                    hidePleaseWait(a);
                    return;
                }
                // Continue ping-pong cycles while running
                if (pingPongRunning) {
                    schedulePingPongCycle();
                }
            });
            currentFadeIn.play();
        });
    }

    private void schedulePingPongCycle() {
        // Fade-out (random 1-2s)
        if (!pingPongRunning) return;
        long outMs = 1000 + (long) (Math.random() * 1000.0);
        Platform.runLater(() -> {
            if (currentFadeOut != null) currentFadeOut.stop();
            currentFadeOut = new FadeTransition(Duration.millis(outMs), pleaseWaitText);
            currentFadeOut.setFromValue(1.0);
            currentFadeOut.setToValue(0.0);
            currentFadeOut.setInterpolator(Interpolator.EASE_BOTH);
            currentFadeOut.setOnFinished(ev -> {
                currentFadeOut = null;
                if (!pingPongRunning) return;
                // Fade-in next (random base 2s + 0..5s)
                long inMs = 2000 + (long) (Math.random() * 5000.0);
                if (currentFadeIn != null) currentFadeIn.stop();
                currentFadeIn = new FadeTransition(Duration.millis(inMs), pleaseWaitText);
                currentFadeIn.setFromValue(0.0);
                currentFadeIn.setToValue(1.0);
                currentFadeIn.setInterpolator(Interpolator.EASE_BOTH);
                currentFadeIn.setOnFinished(e2 -> {
                    currentFadeIn = null;
                    atLeastOneFadeInCompleted = true;
                    // If hide was requested while cycling, perform it now
                    if (pendingAfter != null) {
                        Runnable a = pendingAfter;
                        pendingAfter = null;
                        hidePleaseWait(a);
                        return;
                    }
                    // Continue cycles
                    if (pingPongRunning) schedulePingPongCycle();
                });
                currentFadeIn.play();
            });
            currentFadeOut.play();
        });
    }
    
    private void hidePleaseWait() {
        hidePleaseWait(null);
    }

    /**
     * Hide please-wait with an optional action to run after the fade-out completes.
     */
    private void hidePleaseWait(Runnable after) {
        if (pleaseWaitText == null) {
            if (after != null) Platform.runLater(after);
            return;
        }
        // Stop the ping-pong loop so we can perform final fade-out
        pingPongRunning = false;
        // If a fade is currently running, stop it to ensure a clean final fade-out
        if (currentFadeOut != null) {
            currentFadeOut.stop();
            currentFadeOut = null;
        }
        // Start final fade-out immediately (random 1-2s)
        Platform.runLater(() -> {
            try { pleaseWaitText.setOpacity(1.0); } catch (Exception ignored) {}
            long fadeOutMs = 1000 + (long) (Math.random() * 1000.0);
            fadeOutMs = Math.max(800, fadeOutMs);
            if (currentFadeOut != null) currentFadeOut.stop();
            FadeTransition fade = new FadeTransition(Duration.millis(fadeOutMs), pleaseWaitText);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setInterpolator(Interpolator.EASE_BOTH);
            fade.setOnFinished(e -> {
                currentFadeOut = null;
                pleaseWaitInProgress = false;
                pleaseWaitShownAt = 0;
                try { pleaseWaitText.setOpacity(0.0); } catch (Exception ignored) {}
            });
            currentFadeOut = fade;
            fade.play();
            // Run follow-up action immediately when fade-out starts
            if (after != null) Platform.runLater(after);
        });
        
    }

    /**
     * Ensures the please-wait text is visible for at least the randomized fade-in
     * duration before hiding it and optionally running a follow-up action.
     */
    private void hidePleaseWaitAndRun(Runnable after) {
        if (pleaseWaitText == null) {
            if (after != null) Platform.runLater(after);
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = (pleaseWaitShownAt > 0) ? (now - pleaseWaitShownAt) : 0;
        long remaining = pleaseWaitFadeDurationMs - elapsed;
        if (pleaseWaitShownAt == 0) remaining = pleaseWaitFadeDurationMs;
        if (remaining <= 0) {
            // Enough time already elapsed, hide immediately then run after fade finishes
            Platform.runLater(() -> hidePleaseWait(after));
        } else {
            PauseTransition wait = new PauseTransition(Duration.millis(remaining));
            wait.setOnFinished(ev -> hidePleaseWait(after));
            wait.play();
        }
    }

    private void onKeeperClick() {
        if (pleaseWaitInProgress) return;
        showPleaseWait();
        hidePleaseWaitAndRun(() -> goToAdminLogin());
    }

    private void centerContent() {
        // Use layoutBounds to avoid triggering boundsInParent updates when moving the node
        double w = contentPane.getLayoutBounds().getWidth();
        double h = contentPane.getLayoutBounds().getHeight();
        double rw = root.getWidth();
        double rh = root.getHeight();
        double newX = (rw - w) / 2.0;
        double newY = (rh - h) / 2.0;

        // Only update when value meaningfully changes to avoid redundant layout passes
        if (Math.abs(contentPane.getLayoutX() - newX) > 0.5) {
            contentPane.setLayoutX(newX);
        }
        if (Math.abs(contentPane.getLayoutY() - newY) > 0.5) {
            contentPane.setLayoutY(newY);
        }
    }

    private void playIntroSequence() {
        FadeTransition fadeCover = new FadeTransition(Duration.seconds(2), whiteOverlay);
        fadeCover.setFromValue(1.0);
        fadeCover.setToValue(0.0);
        fadeCover.setInterpolator(Interpolator.EASE_BOTH);
        fadeCover.setOnFinished(e -> whiteOverlay.setMouseTransparent(true));

        PauseTransition revealDelay = new PauseTransition(Duration.seconds(1.2));

        ParallelTransition reveal = new ParallelTransition();

        if (logoImage != null) {
            logoImage.setOpacity(0.0);
            logoImage.setScaleX(0.85);
            logoImage.setScaleY(0.85);
            FadeTransition logoFade = new FadeTransition(Duration.millis(520), logoImage);
            logoFade.setToValue(1.0);
            ScaleTransition logoScale = new ScaleTransition(Duration.millis(520), logoImage);
            logoScale.setToX(1.0);
            logoScale.setToY(1.0);
            reveal.getChildren().add(new ParallelTransition(logoFade, logoScale));
        }

        if (loginAsText != null) {
            loginAsText.setOpacity(0.0);
            loginAsText.setScaleX(0.85);
            loginAsText.setScaleY(0.85);
            FadeTransition loginFade = new FadeTransition(Duration.millis(420), loginAsText);
            loginFade.setToValue(1.0);
            ScaleTransition loginScale = new ScaleTransition(Duration.millis(420), loginAsText);
            loginScale.setToX(1.0);
            loginScale.setToY(1.0);
            reveal.getChildren().add(new ParallelTransition(loginFade, loginScale));
        }

        if (buttonsPane != null) {
            buttonsPane.setOpacity(0.0);
            buttonsPane.setScaleX(0.85);
            buttonsPane.setScaleY(0.85);
            FadeTransition btnFade = new FadeTransition(Duration.millis(420), buttonsPane);
            btnFade.setToValue(1.0);
            ScaleTransition btnScale = new ScaleTransition(Duration.millis(420), buttonsPane);
            btnScale.setToX(1.0);
            btnScale.setToY(1.0);
            reveal.getChildren().add(new ParallelTransition(btnFade, btnScale));
        }

        if (soulImage != null) {
            soulImage.setOpacity(0.0);
            soulImage.setScaleX(0.9);
            soulImage.setScaleY(0.9);
            FadeTransition soulFade = new FadeTransition(Duration.millis(360), soulImage);
            soulFade.setToValue(1.0);
            ScaleTransition soulScale = new ScaleTransition(Duration.millis(360), soulImage);
            soulScale.setToX(1.0);
            soulScale.setToY(1.0);
            reveal.getChildren().add(new ParallelTransition(soulFade, soulScale));
        }

        if (keeperImage != null) {
            keeperImage.setOpacity(0.0);
            keeperImage.setScaleX(0.9);
            keeperImage.setScaleY(0.9);
            FadeTransition keeperFade = new FadeTransition(Duration.millis(360), keeperImage);
            keeperFade.setToValue(1.0);
            ScaleTransition keeperScale = new ScaleTransition(Duration.millis(360), keeperImage);
            keeperScale.setToX(1.0);
            keeperScale.setToY(1.0);
            reveal.getChildren().add(new ParallelTransition(keeperFade, keeperScale));
        }

        if (reveal.getChildren().isEmpty()) {
            reveal.getChildren().add(new PauseTransition(Duration.ZERO));
        }

        new SequentialTransition(fadeCover, revealDelay, reveal).play();
    }

    private void goToLoginSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/login_signup.fxml"));
            Parent loginRoot = loader.load();
            LoginSignupController controller = loader.getController();
            controller.setRepository(new SoulRepository());
            root.getScene().setRoot(loginRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void goToAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/admin_login.fxml"));
            Parent adminRoot = loader.load();
            root.getScene().setRoot(adminRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Immediately set all visual nodes to their final (post-intro) state
     * so the initial page appears as if the intro already ran.
     */
    private void applyFinalState() {
        try {
            if (whiteOverlay != null) {
                whiteOverlay.setOpacity(0.0);
                whiteOverlay.setMouseTransparent(true);
            }

            if (logoImage != null) {
                logoImage.setOpacity(1.0);
                logoImage.setScaleX(1.0);
                logoImage.setScaleY(1.0);
            }
            if (loginAsText != null) {
                loginAsText.setOpacity(1.0);
                loginAsText.setScaleX(1.0);
                loginAsText.setScaleY(1.0);
            }
            if (buttonsPane != null) {
                buttonsPane.setOpacity(1.0);
                buttonsPane.setScaleX(1.0);
                buttonsPane.setScaleY(1.0);
            }
            if (soulImage != null) {
                soulImage.setOpacity(1.0);
                soulImage.setScaleX(1.0);
                soulImage.setScaleY(1.0);
            }
            if (keeperImage != null) {
                keeperImage.setOpacity(1.0);
                keeperImage.setScaleX(1.0);
                keeperImage.setScaleY(1.0);
            }
        } catch (Exception ignored) {}
    }

    private void trySetImage(ImageView iv, String... paths) {
        if (iv.getImage() != null) return;
        for (String p : paths) {
            try {
                URL u = getClass().getResource(p);
                if (u != null) {
                    iv.setImage(new Image(u.toExternalForm(), true));
                    return;
                }
            } catch (Exception ignored) {}
        }
    }
}
