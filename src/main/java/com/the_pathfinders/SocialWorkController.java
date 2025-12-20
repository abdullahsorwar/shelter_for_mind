package com.the_pathfinders;

import com.the_pathfinders.db.BloodDonor;
import com.the_pathfinders.db.BloodSupportRepository;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;

public class SocialWorkController {
    @FXML private ScrollPane scrollWrapper;
    @FXML private VBox contentWrapper;
    @FXML private Button backBtn;
    @FXML private Button bloodDonationBtn;
    @FXML private Button treePlantationBtn;
    @FXML private Button seminarBtn;
    @FXML private Button donationBtn;
    @FXML private Button donateInAppBtn;
    
    // Blood Donation Popup
    @FXML private StackPane bloodDonationOverlay;
    @FXML private VBox overlayContentBox;
    @FXML private ImageView questionIcon;
    @FXML private Button redCrescentBtn;
    @FXML private Button roktoBtn;
    @FXML private Button donateBloodBDBtn;
    @FXML private Button closeOverlayBtn;
    
    // Tree Plantation Popup
    @FXML private StackPane treePlantationOverlay;
    @FXML private VBox treeOverlayContentBox;
    @FXML private ImageView treeIcon;
    @FXML private Button treeNationBtn;
    @FXML private Button oneTreePlantedBtn;
    @FXML private Button teamTreesBtn;
    @FXML private Button plantTreeBtn;
    @FXML private Button closeTreeOverlayBtn;
    
    // Donation Popup
    @FXML private StackPane donationOverlay;
    @FXML private VBox donationOverlayContentBox;
    @FXML private ImageView donationIcon;
    @FXML private Button jaagoBtn;
    @FXML private Button mercyBtn;
    @FXML private Button saveChildrenBtn;
    @FXML private Button friendshipBtn;
    @FXML private Button closeDonationOverlayBtn;

    // Seminar Popup
    @FXML private StackPane seminarOverlay;
    @FXML private VBox seminarOverlayContentBox;
    @FXML private ImageView seminarIcon;
    @FXML private Button allConferenceBtn;
    @FXML private Button mentalHealthSummitBtn;
    @FXML private Button quillTherapyBtn;
    @FXML private Button globallyMindedBtn;
    @FXML private Button closeSeminarOverlayBtn;

    // Browser Overlay
    @FXML private StackPane browserOverlay;
    @FXML private WebView webView;
    @FXML private Button browserBackBtn;
    @FXML private Label browserTitleLabel;

    // In-app donation overlay
    @FXML private StackPane inAppDonationOverlay;
    @FXML private VBox inAppFormBox;
    @FXML private TextField donorContactField;
    @FXML private ComboBox<String> donorBloodGroupCombo;
    @FXML private TextField donorLastDonationField;
    @FXML private TextField donorAreaField;
    @FXML private Button donorSubmitBtn;
    @FXML private Button donorCancelBtn;
    @FXML private Label donorFormFeedbackLabel;

    private String soulId;
    private final BloodSupportRepository bloodRepository = new BloodSupportRepository();
    private static final String[] BLOOD_GROUPS = {
        "O+ve", "O-ve", "A+ve", "A-ve", "B+ve", "B-ve", "AB+ve", "AB-ve"
    };
    
    // Blood donation URLs
    private static final String RED_CRESCENT_URL = "https://bdrcs.org/donate-blood/";
    private static final String ROKTO_URL = "https://www.rokto.co/";
    private static final String DONATE_BLOOD_BD_URL = "https://donatebloodbd.com/";

    // Tree plantation URLs
    private static final String TREE_NATION_URL = "https://tree-nation.com/";
    private static final String ONE_TREE_PLANTED_URL = "https://onetreeplanted.org/";
    private static final String TEAM_TREES_URL = "https://teamtrees.org/#planting-projects";
    private static final String PLANT_A_TREE_URL = "https://plantatreeproject.com/";

    // Donation URLs
    private static final String JAAGO_URL = "https://jaago.com.bd/";
    private static final String MERCY_URL = "https://mwlimits.org/";
    private static final String SAVE_CHILDREN_URL = "https://www.savethechildren.org/us/where-we-work/bangladesh";
    private static final String FRIENDSHIP_URL = "https://friendship.ngo/donate/bangladesh/";

    // Seminar URLs
    private static final String ALL_CONFERENCE_URL = "https://www.allconferencealert.com/mental-health.html";
    private static final String MENTAL_HEALTH_SUMMIT_URL = "https://mentalhealth.global-summit.com/";
    private static final String QUILL_THERAPY_URL = "https://quilltherapysolutions.com/conferences/";
    private static final String GLOBALLY_MINDED_URL = "https://www.globallyminded.org/home/global-mental-health-events-list/";

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
        if (donateInAppBtn != null) {
            donateInAppBtn.setOnAction(e -> openInAppDonationFlow());
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
        
        // Tree plantation popup buttons
        if (treeNationBtn != null) {
            treeNationBtn.setOnAction(e -> openWebsite("Tree Nation", TREE_NATION_URL));
        }
        if (oneTreePlantedBtn != null) {
            oneTreePlantedBtn.setOnAction(e -> openWebsite("One Tree Planted", ONE_TREE_PLANTED_URL));
        }
        if (teamTreesBtn != null) {
            teamTreesBtn.setOnAction(e -> openWebsite("Team Trees", TEAM_TREES_URL));
        }
        if (plantTreeBtn != null) {
            plantTreeBtn.setOnAction (e -> openWebsite("Plant A Tree Project", PLANT_A_TREE_URL));
        }
        if (closeTreeOverlayBtn != null) {
            closeTreeOverlayBtn.setOnAction(e -> hideTreePlantationOverlay());
        }
        
        // Donation popup buttons
        if (jaagoBtn != null) {
            jaagoBtn.setOnAction(e -> openWebsite("JAAGO Foundation", JAAGO_URL));
        }
        if (mercyBtn != null) {
            mercyBtn.setOnAction(e -> openWebsite("Mercy Without Limits", MERCY_URL));
        }
        if (saveChildrenBtn != null) {
            saveChildrenBtn.setOnAction(e -> openWebsite("Save the Children", SAVE_CHILDREN_URL));
        }
        if (friendshipBtn != null) {
            friendshipBtn.setOnAction(e -> openWebsite("Friendship NGO", FRIENDSHIP_URL));
        }
        if (closeDonationOverlayBtn != null) {
            closeDonationOverlayBtn.setOnAction(e -> hideDonationOverlay());
        }

        // Seminar popup buttons
        if (allConferenceBtn != null) {
            allConferenceBtn.setOnAction(e -> openWebsite("All Conference Alert", ALL_CONFERENCE_URL));
        }
        if (mentalHealthSummitBtn != null) {
            mentalHealthSummitBtn.setOnAction(e -> openWebsite("Mental Health Summit", MENTAL_HEALTH_SUMMIT_URL));
        }
        if (quillTherapyBtn != null) {
            quillTherapyBtn.setOnAction(e -> openWebsite("Quill Therapy Conferences", QUILL_THERAPY_URL));
        }
        if (globallyMindedBtn != null) {
            globallyMindedBtn.setOnAction(e -> openWebsite("Globally Minded Events", GLOBALLY_MINDED_URL));
        }
        if (closeSeminarOverlayBtn != null) {
            closeSeminarOverlayBtn.setOnAction(e -> hideSeminarOverlay());
        }

        // Browser back button
        if (browserBackBtn != null) {
            browserBackBtn.setOnAction(e -> closeBrowser());
        }

        if (donorSubmitBtn != null) donorSubmitBtn.setOnAction(e -> submitDonorDetails());
        if (donorCancelBtn != null) donorCancelBtn.setOnAction(e -> hideInAppDonationForm());
        if (donorBloodGroupCombo != null) {
            donorBloodGroupCombo.getItems().setAll(BLOOD_GROUPS);
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
        showTreePlantationOverlay();
    }

    private void onSeminar() {
        showSeminarOverlay();
    }

    private void onDonation() {
        showDonationOverlay();
    }
    
    private void setQuestionIcon() {
        try {
            URL iconUrl = getClass().getResource("/assets/icons/ques.png");
            if (iconUrl != null) {
                Image quesImage = new Image(iconUrl.toExternalForm(), 56, 56, true, true);
                if (questionIcon != null) {
                    questionIcon.setImage(quesImage);
                }
                if (treeIcon != null) {
                    treeIcon.setImage(quesImage);
                }
                if (donationIcon != null) {
                    donationIcon.setImage(quesImage);
                }
                if (seminarIcon != null) {
                    seminarIcon.setImage(quesImage);
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
    
    private void showTreePlantationOverlay() {
        if (treePlantationOverlay == null || contentWrapper == null) return;

        treePlantationOverlay.setVisible(true);
        treePlantationOverlay.setManaged(true);

        // Apply blur to content wrapper
        GaussianBlur blur = new GaussianBlur(0);
        contentWrapper.setEffect(blur);

        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(280), new KeyValue(blur.radiusProperty(), 18))
        );
        blurTimeline.play();

        // Fade in overlay
        treePlantationOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), treePlantationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Scale animation for content box
        if (treeOverlayContentBox != null) {
            treeOverlayContentBox.setScaleX(0.96);
            treeOverlayContentBox.setScaleY(0.96);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(260), treeOverlayContentBox);
            scaleIn.setFromX(0.96);
            scaleIn.setFromY(0.96);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.play();
        }
    }

    private void hideTreePlantationOverlay() {
        if (treePlantationOverlay == null || contentWrapper == null) return;

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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), treePlantationOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            treePlantationOverlay.setVisible(false);
            treePlantationOverlay.setManaged(false);
        });
        fadeOut.play();
    }
    
    private void showDonationOverlay() {
        if (donationOverlay == null || contentWrapper == null) return;

        donationOverlay.setVisible(true);
        donationOverlay.setManaged(true);

        // Apply blur to content wrapper
        GaussianBlur blur = new GaussianBlur(0);
        contentWrapper.setEffect(blur);

        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(280), new KeyValue(blur.radiusProperty(), 18))
        );
        blurTimeline.play();

        // Fade in overlay
        donationOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), donationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Scale animation for content box
        if (donationOverlayContentBox != null) {
            donationOverlayContentBox.setScaleX(0.96);
            donationOverlayContentBox.setScaleY(0.96);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(260), donationOverlayContentBox);
            scaleIn.setFromX(0.96);
            scaleIn.setFromY(0.96);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.play();
        }
    }

    private void hideDonationOverlay() {
        if (donationOverlay == null || contentWrapper == null) return;

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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), donationOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            donationOverlay.setVisible(false);
            donationOverlay.setManaged(false);
        });
        fadeOut.play();
    }

    private void showSeminarOverlay() {
        if (seminarOverlay == null || contentWrapper == null) return;

        seminarOverlay.setVisible(true);
        seminarOverlay.setManaged(true);

        // Apply blur to content wrapper
        GaussianBlur blur = new GaussianBlur(0);
        contentWrapper.setEffect(blur);

        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(280), new KeyValue(blur.radiusProperty(), 18))
        );
        blurTimeline.play();

        // Fade in overlay
        seminarOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), seminarOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Scale animation for content box
        if (seminarOverlayContentBox != null) {
            seminarOverlayContentBox.setScaleX(0.96);
            seminarOverlayContentBox.setScaleY(0.96);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(260), seminarOverlayContentBox);
            scaleIn.setFromX(0.96);
            scaleIn.setFromY(0.96);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.play();
        }
    }

    private void hideSeminarOverlay() {
        if (seminarOverlay == null || contentWrapper == null) return;

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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), seminarOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            seminarOverlay.setVisible(false);
            seminarOverlay.setManaged(false);
        });
        fadeOut.play();
    }

    private void openWebsite(String title, String url) {
        // Hide blood donation overlay first if visible
        if (bloodDonationOverlay != null && bloodDonationOverlay.isVisible()) {
            hideBloodDonationOverlay();
        }

        // Hide tree plantation overlay first if visible
        if (treePlantationOverlay != null && treePlantationOverlay.isVisible()) {
            hideTreePlantationOverlay();
        }
        
        // Hide donation overlay first if visible
        if (donationOverlay != null && donationOverlay.isVisible()) {
            hideDonationOverlay();
        }

        // Hide seminar overlay first if visible
        if (seminarOverlay != null && seminarOverlay.isVisible()) {
            hideSeminarOverlay();
        }

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

    private void openInAppDonationFlow() {
        hideBloodDonationOverlay();
        PauseTransition delay = new PauseTransition(Duration.millis(260));
        delay.setOnFinished(e -> showInAppDonationForm());
        delay.play();
    }

    private void showInAppDonationForm() {
        if (inAppDonationOverlay == null || contentWrapper == null) return;
        resetDonorForm();

        inAppDonationOverlay.setVisible(true);
        inAppDonationOverlay.setManaged(true);

        GaussianBlur blur = new GaussianBlur(0);
        contentWrapper.setEffect(blur);
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(280), new KeyValue(blur.radiusProperty(), 18))
        );
        blurTimeline.play();

        inAppDonationOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), inAppDonationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        if (inAppFormBox != null) {
            inAppFormBox.setScaleX(0.96);
            inAppFormBox.setScaleY(0.96);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(260), inAppFormBox);
            scaleIn.setFromX(0.96);
            scaleIn.setFromY(0.96);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.play();
        }
    }

    private void hideInAppDonationForm() {
        if (inAppDonationOverlay == null || contentWrapper == null) return;

        if (contentWrapper.getEffect() instanceof GaussianBlur blur) {
            Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), blur.getRadius())),
                new KeyFrame(Duration.millis(220), new KeyValue(blur.radiusProperty(), 0))
            );
            blurTimeline.setOnFinished(e -> contentWrapper.setEffect(null));
            blurTimeline.play();
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), inAppDonationOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            inAppDonationOverlay.setVisible(false);
            inAppDonationOverlay.setManaged(false);
        });
        fadeOut.play();
    }

    private void resetDonorForm() {
        if (donorContactField != null) donorContactField.clear();
        if (donorLastDonationField != null) donorLastDonationField.clear();
        if (donorAreaField != null) donorAreaField.clear();
        if (donorBloodGroupCombo != null) donorBloodGroupCombo.getSelectionModel().clearSelection();
        setDonorFeedback(null, true);
    }

    private void submitDonorDetails() {
        String contact = donorContactField != null ? donorContactField.getText() : "";
        String bloodGroup = donorBloodGroupCombo != null ? donorBloodGroupCombo.getValue() : null;
        String lastDonation = donorLastDonationField != null ? donorLastDonationField.getText() : "";
        String area = donorAreaField != null ? donorAreaField.getText() : "";

        if (contact == null || contact.isBlank()) {
            setDonorFeedback("Please enter a contact number.", false);
            return;
        }
        if (bloodGroup == null || bloodGroup.isBlank()) {
            setDonorFeedback("Select a blood group.", false);
            return;
        }
        if (area == null || area.isBlank()) {
            setDonorFeedback("Area of residence helps seekers reach you.", false);
            return;
        }

        BloodDonor donor = new BloodDonor(
            this.soulId,
            bloodGroup,
            contact.trim(),
            lastDonation == null ? null : lastDonation.trim(),
            area.trim()
        );

        try {
            bloodRepository.saveDonor(donor);
            setDonorFeedback("Thank you! You're now visible to matching requests.", true);
            if (donorBloodGroupCombo != null) donorBloodGroupCombo.getSelectionModel().clearSelection();
            if (donorLastDonationField != null) donorLastDonationField.clear();
            if (donorAreaField != null) donorAreaField.clear();
            if (donorContactField != null) donorContactField.clear();
        } catch (SQLException ex) {
            ex.printStackTrace();
            setDonorFeedback("Could not save right now. Please try again.", false);
        }
    }

    private void setDonorFeedback(String message, boolean success) {
        if (donorFormFeedbackLabel == null) return;
        if (message == null || message.isBlank()) {
            donorFormFeedbackLabel.setVisible(false);
            donorFormFeedbackLabel.setManaged(false);
            donorFormFeedbackLabel.setText("");
            return;
        }
        donorFormFeedbackLabel.setText(message);
        donorFormFeedbackLabel.setStyle(success ? "-fx-text-fill: #065f46;" : "-fx-text-fill: #b91c1c;");
        donorFormFeedbackLabel.setVisible(true);
        donorFormFeedbackLabel.setManaged(true);
    }
}
