package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SeekHelpController {

    @FXML private BorderPane root;
    @FXML private Button backBtn;
    @FXML private Button consultancyBtn;
    @FXML private Button requestBloodBtn;

    @FXML private VBox consultancyPane;
    @FXML private VBox requestBloodPane;

    @FXML private TextField requiredBloodField;
    @FXML private TextField locationField;
    @FXML private TextField phoneField;

    private String soulId = "";

    @FXML
    public void initialize() {
        attachCss();
        showConsultancy();

        if (consultancyBtn != null) consultancyBtn.setOnAction(e -> showConsultancy());
        if (requestBloodBtn != null) requestBloodBtn.setOnAction(e -> showRequestBlood());
        if (backBtn != null) backBtn.setOnAction(e -> goBack());
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
}
