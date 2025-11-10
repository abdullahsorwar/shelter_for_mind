package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class PrivateJournalsController {

    @FXML private VBox root;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox journalsContainer;
    @FXML private Button backBtn;
    @FXML private Button addBtn;

    private String currentSoulId = "";
    private JournalRepository journalRepo;

    public void setSoulId(String id) { this.currentSoulId = id == null ? "" : id; }

    @FXML
    public void initialize() {
        journalRepo = new JournalRepository();

        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());
        if (addBtn != null) addBtn.setOnAction(e -> createNewJournal());

        loadJournals();
    }

    private void loadJournals() {
        new Thread(() -> {
            try {
                List<Journal> journals = journalRepo.getJournalsBySoulId(currentSoulId);
                Platform.runLater(() -> {
                    journalsContainer.getChildren().clear();
                    for (Journal j : journals) {
                        VBox box = new VBox();
                        box.getStyleClass().add("journal-outer-box");
                        Label title = new Label(j.getEntryDate() == null ? j.getCreatedAt().toString() : j.getEntryDate().toString());
                        title.getStyleClass().add("journal-username");
                        Label preview = new Label(j.getText() == null ? "(empty)" : (j.getText().length() > 140 ? j.getText().substring(0,140)+"..." : j.getText()));
                        preview.getStyleClass().add("journal-text");
                        box.getChildren().addAll(title, preview);
                        box.setOnMouseClicked(ev -> openJournalForEditing(j.getId()));
                        journalsContainer.getChildren().add(box);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void createNewJournal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
            Parent journRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JournalController jc) {
                jc.setSoulId(this.currentSoulId);
                // don't load any existing journal - start fresh
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(journRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openJournalForEditing(String journalId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
            Parent journRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JournalController jc) {
                jc.setSoulId(this.currentSoulId);
                jc.loadJournal(journalId);
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(journRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(this.currentSoulId, "");
            if (root != null && root.getScene() != null) root.getScene().setRoot(dash);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
