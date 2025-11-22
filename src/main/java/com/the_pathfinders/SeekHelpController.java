package com.the_pathfinders;

import com.the_pathfinders.db.BloodDonor;
import com.the_pathfinders.db.BloodRequest;
import com.the_pathfinders.db.BloodSupportRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class SeekHelpController {

    @FXML private BorderPane root;
    @FXML private Button backBtn;
    @FXML private Button consultancyBtn;
    @FXML private Button requestBloodBtn;

    @FXML private VBox consultancyPane;
    @FXML private VBox requestBloodPane;

    @FXML private ComboBox<String> requiredBloodCombo;
    @FXML private TextField locationField;
    @FXML private TextField phoneField;
    @FXML private Button submitRequestBtn;
    @FXML private Label requestFeedbackLabel;
    @FXML private Label donorResultsHeader;
    @FXML private VBox donorResultsBox;

    private String soulId = "";
    private final BloodSupportRepository bloodRepository = new BloodSupportRepository();

    @FXML
    public void initialize() {
        attachCss();
        populateBloodTypes();
        showConsultancy();

        if (consultancyBtn != null) consultancyBtn.setOnAction(e -> showConsultancy());
        if (requestBloodBtn != null) requestBloodBtn.setOnAction(e -> showRequestBlood());
        if (backBtn != null) backBtn.setOnAction(e -> goBack());
        if (submitRequestBtn != null) submitRequestBtn.setOnAction(e -> submitBloodRequest());
    }

    private void populateBloodTypes() {
        if (requiredBloodCombo != null) {
            requiredBloodCombo.getItems().setAll(
                    "O+ve", "O-ve",
                    "A+ve", "A-ve",
                    "AB+ve", "AB-ve",
                    "B+ve", "B-ve"
            );
        }
    }

    public void setSoulId(String soulId) {
        this.soulId = soulId == null ? "" : soulId;
    }

    private void attachCss() {
        if (root == null) return;
        root.getStylesheets().clear();
        var seekCss = getClass().getResource("/com/the_pathfinders/css/SeekHelp.css");
        if (seekCss != null) root.getStylesheets().add(seekCss.toExternalForm());
        var loginCss = getClass().getResource("/com/the_pathfinders/css/login_signup.css");
        if (loginCss != null) root.getStylesheets().add(loginCss.toExternalForm());
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            var dc = loader.getController();
            try {
                var m = dc.getClass().getMethod("setUser", String.class, String.class);
                m.invoke(dc, this.soulId, this.soulId);
            } catch (Exception ignored) {}
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showConsultancy() {
        if (consultancyPane != null) {
            consultancyPane.setVisible(true);
            consultancyPane.setManaged(true);
        }
        if (requestBloodPane != null) {
            requestBloodPane.setVisible(false);
            requestBloodPane.setManaged(false);
        }
    }

    private void showRequestBlood() {
        if (consultancyPane != null) {
            consultancyPane.setVisible(false);
            consultancyPane.setManaged(false);
        }
        if (requestBloodPane != null) {
            requestBloodPane.setVisible(true);
            requestBloodPane.setManaged(true);
        }
    }

    private void submitBloodRequest() {
        String bloodGroup = requiredBloodCombo != null ? requiredBloodCombo.getValue() : null;
        String location = locationField != null ? locationField.getText() : "";
        String phone = phoneField != null ? phoneField.getText() : "";

        if (bloodGroup == null || bloodGroup.isBlank()) {
            showFeedback("Please choose a blood group.", false);
            return;
        }
        if (location == null || location.isBlank()) {
            showFeedback("Location is required.", false);
            return;
        }
        if (phone == null || phone.isBlank()) {
            showFeedback("Please add a phone number.", false);
            return;
        }

        BloodRequest request = new BloodRequest(this.soulId, bloodGroup, location.trim(), phone.trim());
        try {
            bloodRepository.saveRequest(request);
            showFeedback("Request saved. Here are available donors:", true);
            List<BloodDonor> donors = bloodRepository.findDonorsByBloodGroup(bloodGroup);
            renderDonorMatches(donors, bloodGroup);
        } catch (SQLException ex) {
            ex.printStackTrace();
            showFeedback("Could not save request. Please try again.", false);
        }
    }

    private void renderDonorMatches(List<BloodDonor> donors, String bloodGroup) {
        if (donorResultsHeader != null) {
            donorResultsHeader.setText("Matching Donors • " + bloodGroup);
            donorResultsHeader.setVisible(true);
            donorResultsHeader.setManaged(true);
        }
        if (donorResultsBox == null) return;

        donorResultsBox.getChildren().clear();
        if (donors == null || donors.isEmpty()) {
            Label empty = new Label("No in-app donors yet for this blood group. Check back soon.");
            empty.getStyleClass().add("muted-text");
            donorResultsBox.getChildren().add(empty);
            return;
        }

        for (BloodDonor donor : donors) {
            VBox card = new VBox(4);
            card.getStyleClass().add("donor-card");

            Label title = new Label(safeText(donor.getArea(), "Area not shared") + " • " + donor.getBloodGroup());
            title.getStyleClass().add("donor-title");

            Label contact = new Label("Contact: " + safeText(donor.getContactNumber(), "Not shared"));
            contact.getStyleClass().add("donor-meta");

            Label lastDonation = new Label("Last donation: " + safeText(donor.getLastDonationInfo(), "Not shared"));
            lastDonation.getStyleClass().add("donor-meta");

            card.getChildren().addAll(title, contact, lastDonation);
            donorResultsBox.getChildren().add(card);
        }
    }

    private void showFeedback(String message, boolean success) {
        if (requestFeedbackLabel == null) return;
        requestFeedbackLabel.setText(message);
        requestFeedbackLabel.setStyle(success ? "-fx-text-fill: #065f46;" : "-fx-text-fill: #b91c1c;");
        requestFeedbackLabel.setVisible(true);
        requestFeedbackLabel.setManaged(true);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
