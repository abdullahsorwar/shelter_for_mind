package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class JournalController {

    @FXML private StackPane mainContainer;
    @FXML private VBox root;
    @FXML private VBox menuPanel;
    @FXML private StackPane menuOverlay;
    @FXML private TextArea textArea;
    @FXML private TextField titleField;
    @FXML private Button saveBtn;
    @FXML private Button menuBtn;
    @FXML private Button closeMenuBtn;
    @FXML private ComboBox<String> fontCombo;
    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private ColorPicker colorPicker;
    @FXML private Button backBtn;
    @FXML private Button publicBtn;
    @FXML private Button privateBtn;
    @FXML private ComboBox<String> themeCombo;

    private String soulId = "";
    private JournalRepository journalRepo;
    private String currentJournalId = null;
    private boolean isPublic = true; // Default to public
    private boolean menuOpen = false; // Track menu state

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
    }
    @FXML
    public void initialize() {
        // Initialize repository
        journalRepo = new JournalRepository();
        
        System.out.println("🎨 JournalController initializing...");

        // Apply default theme to mainContainer at startup
        if (mainContainer != null) {
            mainContainer.getStyleClass().add("theme-default");
            System.out.println("✅ Default theme applied to mainContainer");
            System.out.println("   MainContainer style classes: " + mainContainer.getStyleClass());
        } else {
            System.err.println("❌ ERROR: mainContainer is null during initialization!");
        }

        // Populate theme options
        if (themeCombo != null) {
            List<String> themes = Arrays.asList("Default", "Vintage", "Pastel", "Starry Night", "Lavender");
            themeCombo.getItems().setAll(themes);
            themeCombo.getSelectionModel().selectFirst();
            themeCombo.setOnAction(e -> applyTheme(themeCombo.getSelectionModel().getSelectedItem()));
        }

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

        // Setup visibility toggle buttons
        if (publicBtn != null && privateBtn != null) {
            setToggleState(true); // Default to public
            publicBtn.setOnAction(e -> setToggleState(true));
            privateBtn.setOnAction(e -> setToggleState(false));
        }

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToDashboard());
        }
    }

    private void setToggleState(boolean publicState) {
        this.isPublic = publicState;
        
        if (publicBtn != null && privateBtn != null) {
            if (publicState) {
                // Public is active
                publicBtn.getStyleClass().remove("toggle-inactive");
                publicBtn.getStyleClass().add("toggle-active");
                privateBtn.getStyleClass().remove("toggle-active");
                privateBtn.getStyleClass().add("toggle-inactive");
            } else {
                // Private is active
                privateBtn.getStyleClass().remove("toggle-inactive");
                privateBtn.getStyleClass().add("toggle-active");
                publicBtn.getStyleClass().remove("toggle-active");
                publicBtn.getStyleClass().add("toggle-inactive");
            }
        }
    }

    /**
     * Load an existing journal by id into the editor for editing/resuming.
     */
    public void loadJournal(String journalId) {
        if (journalId == null || journalId.isBlank()) return;
        try {
            JournalRepository repo = new JournalRepository();
            Journal j = repo.getJournalById(journalId);
            if (j != null) {
                this.currentJournalId = j.getId();
                // populate fields
                if (titleField != null && j.getTitle() != null) {
                    titleField.setText(j.getTitle());
                }
                this.textArea.setText(j.getText() == null ? "" : j.getText());
                if (j.getFontFamily() != null) fontCombo.getSelectionModel().select(j.getFontFamily());
                if (j.getFontSize() != null) sizeCombo.getSelectionModel().select(j.getFontSize());
                if (j.getFontFamily() != null || j.getFontSize() != null) applyTextStyle();
                
                // Load and set visibility state
                if (j.getIsPublic() != null) {
                    setToggleState(j.getIsPublic());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        String content = textArea.getText() == null ? "" : textArea.getText().trim();
        
        // Capture font selections
        String fontFamily = fontCombo.getSelectionModel().getSelectedItem();
        Integer fontSize = sizeCombo.getSelectionModel().getSelectedItem();
        
        // Default values if nothing selected
        if (fontFamily == null) fontFamily = "System";
        if (fontSize == null) fontSize = 14;

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

        // Capture final values for use in thread
        final String finalFontFamily = fontFamily;
        final Integer finalFontSize = fontSize;

            // Run database operations in background thread to keep UI responsive
            new Thread(() -> {
                try {
                    String journalId;
                    if (currentJournalId != null) {
                        // update existing
                        boolean ok = journalRepo.updateJournal(currentJournalId, content, finalFontFamily, finalFontSize, isPublic);
                        journalId = currentJournalId;
                        if (!ok) throw new RuntimeException("Failed to update journal");
                    } else {
                        journalId = journalRepo.saveJournal(soulId, content, finalFontFamily, finalFontSize, isPublic);
                        currentJournalId = journalId;
                    }

                    final String finalJournalId = journalId;
                    Platform.runLater(() -> {
                        System.out.println("Journal saved successfully!");
                        System.out.println("Journal ID: " + finalJournalId);
                        System.out.println("Soul ID: " + soulId);
                        System.out.println("Visibility: " + (isPublic ? "Public" : "Private"));
                        System.out.println("Font: " + finalFontFamily + " " + finalFontSize + "px");
                        System.out.println("Text: " + content);

                        showAlert("Success", "Journal saved successfully as " + (isPublic ? "Public" : "Private") + "!\nJournal ID: " + finalJournalId, Alert.AlertType.INFORMATION);
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void applyTheme(String theme) {
        if (theme == null || mainContainer == null) {
            System.err.println("ERROR: Cannot apply theme - theme=" + theme + ", mainContainer=" + mainContainer);
            return;
        }

        // Remove all theme style classes from mainContainer
        mainContainer.getStyleClass().removeAll("theme-default", "theme-vintage", "theme-pastel", "theme-starry", "theme-lavender");

        // Add the selected theme class to mainContainer
        String themeClass = "";
        switch (theme) {
            case "Vintage":
                themeClass = "theme-vintage";
                mainContainer.getStyleClass().add(themeClass);
                break;
            case "Pastel":
                themeClass = "theme-pastel";
                mainContainer.getStyleClass().add(themeClass);
                break;
            case "Starry Night":
                themeClass = "theme-starry";
                mainContainer.getStyleClass().add(themeClass);
                break;
            case "Lavender":
                themeClass = "theme-lavender";
                mainContainer.getStyleClass().add(themeClass);
                break;
            default:
                themeClass = "theme-default";
                mainContainer.getStyleClass().add(themeClass);
                break;
        }

        System.out.println("✅ Theme applied: " + theme + " (class: " + themeClass + ")");
        System.out.println("   MainContainer style classes: " + mainContainer.getStyleClass());
    }

    @FXML
    private void toggleMenu() {
        if (menuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void openMenu() {
        if (menuPanel == null || menuOverlay == null) return;

        menuOpen = true;
        menuPanel.setVisible(true);
        menuOverlay.setVisible(true);

        // Animate menu sliding in from left
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), menuPanel);
        slideIn.setFromX(-320);
        slideIn.setToX(0);
        slideIn.play();

        // Fade in overlay
        menuOverlay.setOpacity(0);
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(300), menuOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void closeMenu() {
        if (menuPanel == null || menuOverlay == null) return;

        menuOpen = false;

        // Animate menu sliding out to left
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), menuPanel);
        slideOut.setFromX(0);
        slideOut.setToX(-320);
        slideOut.setOnFinished(e -> menuPanel.setVisible(false));
        slideOut.play();

        // Fade out overlay
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(300), menuOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> menuOverlay.setVisible(false));
        fadeOut.play();
    }

    @FXML
    private void closeMenuOnOverlay() {
        closeMenu();
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
