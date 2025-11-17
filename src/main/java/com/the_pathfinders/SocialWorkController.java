
package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class SocialWorkController {
    @FXML private VBox root;
    @FXML private Button backBtn;
    @FXML private Button bloodDonationBtn;
    @FXML private Button treePlantationBtn;
    @FXML private Button seminarBtn;
    @FXML private Button donationBtn;

    private String soulId;

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
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController dc = loader.getController();
            dc.setUser(soulId, soulId);
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onBloodDonation() {
        // TODO: Navigate to Blood Donation page
        System.out.println("Blood Donation selected");
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
}