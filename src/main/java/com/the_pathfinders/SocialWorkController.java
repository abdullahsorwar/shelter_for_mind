
package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;

public class SocialWorkController {
    @FXML private VBox contentWrapper;
    @FXML private Button backBtn;
    @FXML private Button bloodDonationBtn;
    @FXML private Button treePlantationBtn;
    @FXML private Button seminarBtn;
    @FXML private Button donationBtn;
    
    // Blood Donation Popup
    @FXML private StackPane bloodDonationOverlay;
    @FXML private VBox overlayContentBox;
    @FXML private ImageView questionIcon;
    @FXML private Button redCrescentBtn;
    @FXML private Button roktoBtn;
    @FXML private Button donateBloodBDBtn;
    @FXML private Button closeOverlayBtn;
    
    // Browser Overlay
    @FXML private StackPane browserOverlay;
    @FXML private WebView webView;
    @FXML private Button browserBackBtn;
    @FXML private Label browserTitleLabel;

    private String soulId;
    
    // Blood donation URLs
    private static final String RED_CRESCENT_URL = "https://bdrcs.org/donate-blood/";
    private static final String ROKTO_URL = "https://www.rokto.co/";
    private static final String DONATE_BLOOD_BD_URL = "https://donatebloodbd.com/";

    public void setSoulId(String id) {
        this.soulId = id;
    }

    @FXML
    public void initialize() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
        if (bloodDonationBtn != null) {
            bloodDonationBtn.setOnAction(e -> onBloodDonation());
        }
        if (treePlantationBtn != null) {
            treePlantationBtn.setOnAction(e -> onTreePlantation());
        }
        if (seminarBtn != null) {
            seminarBtn.setOnAction(e -> onSeminar());
        }
        if (donationBtn != null) {
            donationBtn.setOnAction(e -> onDonation());
        }
        
        // Blood donation popup buttons
        if (redCrescentBtn != null) {
            redCrescentBtn.setOnAction(e -> openWebsite("Red Crescent Blood Donation", RED_CRESCENT_URL));
        }
        if (roktoBtn != null) {
            roktoBtn.setOnAction(e -> openWebsite("Rokto", ROKTO_URL));
        }
        if (donateBloodBDBtn != null) {
            donateBloodBDBtn.setOnAction(e -> openWebsite("DonateBloodBD", DONATE_BLOOD_BD_URL));
        }
        if (closeOverlayBtn != null) {
            closeOverlayBtn.setOnAction(e -> hideBloodDonationOverlay());
        }
        
        // Browser back button
        if (browserBackBtn != null) {
            browserBackBtn.setOnAction(e -> closeBrowser());
        }
        
        // Set question icon
        setQuestionIcon();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController dc = loader.getController();
            dc.setUser(soulId, soulId);
            if (contentWrapper != null && contentWrapper.getScene() != null) {
                contentWrapper.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onBloodDonation() {
        showBloodDonationOverlay();
    }

    private void onTreePlantation() {
        // TODO: Navigate to Tree Plantation page
        System.out.println("Tree Plantation selected");
    }

    private void onSeminar() {
        // TODO: Navigate to Seminar page
        System.out.println("Seminar selected");
    }

    private void onDonation() {
        // TODO: Navigate to Donation page
        System.out.println("Donation selected");
    }
    
    private void setQuestionIcon() {
        try {
            if (questionIcon != null) {
                URL iconUrl = getClass().getResource("/assets/icons/ques.png");
                if (iconUrl != null) {
                    questionIcon.setImage(new Image(iconUrl.toExternalForm(), 56, 56, true, true));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void showBloodDonationOverlay() {
        if (bloodDonationOverlay == null || contentWrapper == null) return;
        
        bloodDonationOverlay.setVisible(true);
        bloodDonationOverlay.setManaged(true);
        
        // Apply blur to content wrapper
        GaussianBlur blur = new GaussianBlur(0);
        contentWrapper.setEffect(blur);
        
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(280), new KeyValue(blur.radiusProperty(), 18))
        );
        blurTimeline.play();
        
        // Fade in overlay
        bloodDonationOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), bloodDonationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Scale animation for content box
        if (overlayContentBox != null) {
            overlayContentBox.setScaleX(0.96);
            overlayContentBox.setScaleY(0.96);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(260), overlayContentBox);
            scaleIn.setFromX(0.96);
            scaleIn.setFromY(0.96);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.play();
        }
    }
    
    private void hideBloodDonationOverlay() {
        if (bloodDonationOverlay == null || contentWrapper == null) return;
        
        // Reverse blur
        if (contentWrapper.getEffect() instanceof GaussianBlur blur) {
            Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), blur.getRadius())),
                new KeyFrame(Duration.millis(220), new KeyValue(blur.radiusProperty(), 0))
            );
            blurTimeline.setOnFinished(e -> contentWrapper.setEffect(null));
            blurTimeline.play();
        }
        
        // Fade out overlay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), bloodDonationOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            bloodDonationOverlay.setVisible(false);
            bloodDonationOverlay.setManaged(false);
        });
        fadeOut.play();
    }
    
    private void openWebsite(String title, String url) {
        // Hide blood donation overlay first
        hideBloodDonationOverlay();
        
        // Wait for overlay to hide, then show browser
        Platform.runLater(() -> {
            try {
                Thread.sleep(250); // Wait for overlay animation to complete
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            Platform.runLater(() -> {
                if (browserOverlay == null || webView == null) return;
                
                // Set title
                if (browserTitleLabel != null) {
                    browserTitleLabel.setText(title);
                }
                
                // Load URL
                webView.getEngine().load(url);
                
                // Show browser overlay
                browserOverlay.setVisible(true);
                browserOverlay.setManaged(true);
                
                // Fade in browser
                browserOverlay.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), browserOverlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
        });
    }
    
    private void closeBrowser() {
        if (browserOverlay == null) return;
        
        // Fade out browser
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), browserOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            browserOverlay.setVisible(false);
            browserOverlay.setManaged(false);
            
            // Clear webview
            if (webView != null) {
                webView.getEngine().load(null);
            }
        });
        fadeOut.play();
    }
}