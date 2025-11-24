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
            
            // Delay intro animation until after scene is fully rendered
            // This prevents stuttering during initialization
            Platform.runLater(() -> {
                // Add a delay to ensure everything is rendered
                PauseTransition initialDelay = new PauseTransition(Duration.millis(500));
                initialDelay.setOnFinished(e -> playIntroSequence());
                initialDelay.play();
            });
        });

        // Click wiring - start video initialization on Soul button click
        if (soulImage != null) {
            soulImage.setOnMouseClicked(e -> onSoulClick());
        }
        if (keeperImage != null) {
            keeperImage.setOnMouseClicked(e -> goToAdminLogin());
        }
    }
    
    private void onSoulClick() {
        // Start video initialization on first click
        if (!videoReady) {
            initializeBackgroundVideo();
        } else {
            goToLoginSignup();
        }
    }
    
    private void initializeBackgroundVideo() {
        VideoManager videoManager = VideoManager.getInstance();
        
        // If already initialized, proceed immediately
        if (videoManager.isInitialized()) {
            videoReady = true;
            goToLoginSignup();
            return;
        }
        
        // Show "Please wait..." message
        showPleaseWait();
        
        // Initialize with 5 retry attempts
        videoManager.initializeWithRetry(
            500, // max retries
            (successMsg) -> {
                // On success
                System.out.println("Video initialization succeeded: " + successMsg);
                videoReady = true;
                hidePleaseWait();
                goToLoginSignup();
            },
            (failureMsg) -> {
                // On failure - still navigate but without video
                System.err.println("Video initialization failed: " + failureMsg);
                videoReady = true; // Allow navigation anyway
                hidePleaseWait();
                goToLoginSignup();
            }
        );
    }
    
    private void showPleaseWait() {
        if (pleaseWaitText != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), pleaseWaitText);
            fade.setToValue(1.0);
            fade.play();
        }
    }
    
    private void hidePleaseWait() {
        if (pleaseWaitText != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), pleaseWaitText);
            fade.setToValue(0.0);
            fade.play();
        }
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
