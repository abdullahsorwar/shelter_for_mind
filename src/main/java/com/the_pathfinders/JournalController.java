package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.*;
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
    @FXML private Button backBtn; // make sure your FXML has this button too


    // Use environment variables for production; replace defaults if needed
    private final String DB_URL = System.getenv().getOrDefault("JOURNAL_DB_URL", "jdbc:postgresql://host:5432/dbname");
    private final String DB_USER = System.getenv().getOrDefault("JOURNAL_DB_USER", "dbuser");
    private final String DB_PASS = System.getenv().getOrDefault("JOURNAL_DB_PASS", "dbpass");
    private String soulId = "";

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
    }
    @FXML
    public void initialize() {
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
        LocalDate date = datePicker.getValue();
        if (date == null) date = LocalDate.now();
        String content = textArea.getText() == null ? "" : textArea.getText().trim();

        // Print to console as required
        System.out.println("Saving journal entry:");
        System.out.println("Date: " + date);
        System.out.println("Text: " + content);

        // Save to DB (in a background thread recommended for real apps)
        try {
            saveToDatabase(date, content);
            System.out.println("Saved to database successfully.");
        } catch (Exception ex) {
            System.err.println("Failed to save to database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onLoad() {
        try {
            JournalEntry latest = loadLatestEntry();
            if (latest != null) {
                datePicker.setValue(latest.entryDate);
                textArea.setText(latest.entryText);
                System.out.println("Loaded latest entry:");
                System.out.println("Date: " + latest.entryDate);
                System.out.println("Text: " + latest.entryText);
            } else {
                System.out.println("No journal entries found in database.");
            }
        } catch (Exception ex) {
            System.err.println("Failed to load from database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void saveToDatabase(LocalDate date, String text) throws SQLException {
        // Ensure JDBC driver is on classpath (add dependency in pom.xml)
        String sql = "INSERT INTO journal_entries (entry_text, entry_date) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    private JournalEntry loadLatestEntry() throws SQLException {
        // Try to fetch latest by created_at, fallback to id if necessary
        String sql = "SELECT entry_text, entry_date FROM journal_entries ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String text = rs.getString("entry_text");
                Date d = rs.getDate("entry_date");
                LocalDate ld = d != null ? d.toLocalDate() : LocalDate.now();
                return new JournalEntry(ld, text);
            }
        } catch (SQLException e) {
            // If created_at doesn't exist, try ordering by id
            String alt = "SELECT entry_text, entry_date FROM journal_entries ORDER BY id DESC LIMIT 1";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps2 = conn.prepareStatement(alt);
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    String text = rs2.getString("entry_text");
                    Date d = rs2.getDate("entry_date");
                    LocalDate ld = d != null ? d.toLocalDate() : LocalDate.now();
                    return new JournalEntry(ld, text);
                }
            }
        }
        return null;
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


    private static class JournalEntry {
        final LocalDate entryDate;
        final String entryText;
        JournalEntry(LocalDate d, String t) {
            this.entryDate = d;
            this.entryText = t;
        }
    }
}
