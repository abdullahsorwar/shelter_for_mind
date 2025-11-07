package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class JournalController {

    @FXML private VBox root;
    @FXML private DatePicker datePicker;
    @FXML private TextArea textArea;
    @FXML private Button saveBtn;
    @FXML private Button loadBtn;
    @FXML private ComboBox<String> fontCombo;
    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private ColorPicker colorPicker;
    @FXML private Button backBtn;

    private String soulId = "";
    private JournalRepository journalRepo;

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
    }
    @FXML
    public void initialize() {
        // Initialize repository
        journalRepo = new JournalRepository();
        
        // Populate font and size options
        List<String> fonts = Arrays.asList("System", "Segoe UI", "Arial", "Georgia", "Courier New");
        fontCombo.getItems().setAll(fonts);
        fontCombo.getSelectionModel().selectFirst();

        List<Integer> sizes = Arrays.asList(12, 14, 16, 18, 20, 24);
        sizeCombo.getItems().setAll(sizes);
        sizeCombo.getSelectionModel().select(Integer.valueOf(14));

        colorPicker.setValue(Color.web("#264653")); // calming text color default

        // Apply initial style
        applyTextStyle();

        // Listeners to update TextArea style live
        fontCombo.setOnAction(e -> applyTextStyle());
        sizeCombo.setOnAction(e -> applyTextStyle());
        colorPicker.setOnAction(e -> applyTextStyle());

        saveBtn.setOnAction(e -> onSave());
        loadBtn.setOnAction(e -> onLoad());

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToDashboard());
        }
    }


    private void applyTextStyle() {
        String font = fontCombo.getSelectionModel().getSelectedItem();
        Integer size = sizeCombo.getSelectionModel().getSelectedItem();
        Color col = colorPicker.getValue();

        if (font == null) font = "System";
        if (size == null) size = 14;
        if (col == null) col = Color.BLACK;

        String colorWeb = toWebColor(col);
        // Update inline style of the text area content
        textArea.setStyle(String.format("-fx-font-family: '%s'; -fx-font-size: %dpx; -fx-text-fill: %s;",
                font, size, colorWeb));
    }

    private String toWebColor(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private void onSave() {
        final LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        String content = textArea.getText() == null ? "" : textArea.getText().trim();

        if (content.isEmpty()) {
            showAlert("Empty Journal", "Please write something before saving!", Alert.AlertType.WARNING);
            return;
        }

        if (soulId == null || soulId.isEmpty()) {
            showAlert("Error", "User not logged in!", Alert.AlertType.ERROR);
            return;
        }

        // Disable button during save operation
        saveBtn.setDisable(true);

        // Run database operations in background thread to keep UI responsive
        new Thread(() -> {
            try {
                String journalId = journalRepo.saveJournal(soulId, content);
                
                Platform.runLater(() -> {
                    System.out.println("Journal saved successfully!");
                    System.out.println("Journal ID: " + journalId);
                    System.out.println("Soul ID: " + soulId);
                    System.out.println("Date: " + date);
                    System.out.println("Text: " + content);
                    
                    showAlert("Success", "Journal saved successfully!\nJournal ID: " + journalId, Alert.AlertType.INFORMATION);
                    saveBtn.setDisable(false);
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    System.err.println("Failed to save journal: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("Error", "Failed to save journal: " + ex.getMessage(), Alert.AlertType.ERROR);
                    saveBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void onLoad() {
        if (soulId == null || soulId.isEmpty()) {
            showAlert("Error", "User not logged in!", Alert.AlertType.ERROR);
            return;
        }

        // Disable button during load operation
        loadBtn.setDisable(true);

        // Run database operations in background thread
        new Thread(() -> {
            try {
                Journal latest = journalRepo.getLatestJournalForUser(soulId);
                
                Platform.runLater(() -> {
                    if (latest != null) {
                        datePicker.setValue(latest.getEntryDate());
                        textArea.setText(latest.getText());
                        System.out.println("Loaded latest journal:");
                        System.out.println("Journal ID: " + latest.getId());
                        System.out.println("Date: " + latest.getEntryDate());
                        System.out.println("Text: " + latest.getText());
                    } else {
                        showAlert("No Journals", "No journal entries found for this user.", Alert.AlertType.INFORMATION);
                        System.out.println("No journal entries found for user: " + soulId);
                    }
                    loadBtn.setDisable(false);
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    System.err.println("Failed to load journal: " + ex.getMessage());
                    ex.printStackTrace();
                    showAlert("Error", "Failed to load journal: " + ex.getMessage(), Alert.AlertType.ERROR);
                    loadBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();

            DashboardController controller = loader.getController();
            controller.setUser(this.soulId, ""); // pass user info

            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
