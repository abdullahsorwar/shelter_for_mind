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
    @FXML private Rectangle whiteOverlay;

    @FXML private Rectangle soulRect, keeperRect;
    @FXML private javafx.scene.text.Text soulText, keeperText;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            contentPane.boundsInParentProperty().addListener((o, ov, nv) -> centerContent());
        });

        trySetImage(bgImage, "/com/the_pathfinders/green_bg.jpg", "/green_bg.jpg");
        trySetImage(logoImage, "/com/the_pathfinders/logo_new.png", "/logo_new.png");

        playIntroSequence();

        // Click wiring
        soulRect.setOnMouseClicked(e -> goToLoginSignup());
        soulText.setOnMouseClicked(e -> goToLoginSignup());
        keeperRect.setOnMouseClicked(e -> {});
        keeperText.setOnMouseClicked(e -> {});
    }

    private void centerContent() {
        double w = contentPane.getBoundsInParent().getWidth();
        double h = contentPane.getBoundsInParent().getHeight();
        double rw = root.getWidth();
        double rh = root.getHeight();
        contentPane.setLayoutX((rw - w) / 2.0);
        contentPane.setLayoutY((rh - h) / 2.0);
    }

    private void playIntroSequence() {
        FadeTransition fadeCover = new FadeTransition(Duration.seconds(5), whiteOverlay);
        fadeCover.setFromValue(1.0);
        fadeCover.setToValue(0.0);
        fadeCover.setInterpolator(Interpolator.EASE_BOTH);
        fadeCover.setOnFinished(e -> whiteOverlay.setMouseTransparent(true));

        loginAsText.setOpacity(0.0);
        loginAsText.setScaleX(0.85);
        loginAsText.setScaleY(0.85);
        FadeTransition loginFade = new FadeTransition(Duration.millis(350), loginAsText);
        loginFade.setToValue(1.0);
        ScaleTransition loginScale = new ScaleTransition(Duration.millis(350), loginAsText);
        loginScale.setToX(1.0); loginScale.setToY(1.0);

        buttonsPane.setOpacity(0.0);
        buttonsPane.setScaleX(0.9);
        buttonsPane.setScaleY(0.9);
        FadeTransition btnFade = new FadeTransition(Duration.millis(300), buttonsPane);
        btnFade.setToValue(1.0);
        ScaleTransition btnScale = new ScaleTransition(Duration.millis(300), buttonsPane);
        btnScale.setToX(1.0); btnScale.setToY(1.0);

        new SequentialTransition(
            fadeCover,
            new ParallelTransition(loginFade, loginScale),
            new PauseTransition(Duration.millis(120)),
            new ParallelTransition(btnFade, btnScale)
        ).play();
    }

    private void goToLoginSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_signup.fxml"));
            Parent loginRoot = loader.load();
            LoginSignupController controller = loader.getController();
            controller.setRepository(new SoulRepository());
            root.getScene().setRoot(loginRoot);
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