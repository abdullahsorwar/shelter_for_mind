package com.the_pathfinders;

import com.the_pathfinders.db.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeekHelpController {

    @FXML private BorderPane root;
    @FXML private Button backBtn;
    @FXML private Button consultancyBtn;
    @FXML private Button requestBloodBtn;

    @FXML private VBox consultancyPane;
    @FXML private VBox requestBloodPane;
    @FXML private VBox doctorsListBox;

    @FXML private ComboBox<String> requiredBloodCombo;
    @FXML private TextField locationField;
    @FXML private TextField phoneField;
    @FXML private Button submitRequestBtn;
    @FXML private Label requestFeedbackLabel;
    @FXML private Label donorResultsHeader;
    @FXML private VBox donorResultsBox;

    private String soulId = "";
    private final BloodSupportRepository bloodRepository = new BloodSupportRepository();
    private final DoctorRepository doctorRepository = new DoctorRepository();

    @FXML
    public void initialize() {
        attachCss();
        populateBloodTypes();
        loadDoctors();
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

    private void loadDoctors() {
        if (doctorsListBox == null) return;

        doctorsListBox.getChildren().clear();

        try {
            // Add sample doctors if none exist
            doctorRepository.addSampleDoctors();

            List<Doctor> doctors = doctorRepository.getAllDoctors();

            if (doctors.isEmpty()) {
                Label empty = new Label("No doctors available at the moment.");
                empty.getStyleClass().add("muted-text");
                doctorsListBox.getChildren().add(empty);
                return;
            }

            for (Doctor doctor : doctors) {
                VBox doctorCard = createDoctorCard(doctor);
                doctorsListBox.getChildren().add(doctorCard);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Label error = new Label("Error loading doctors. Please try again later.");
            error.setStyle("-fx-text-fill: #b91c1c;");
            doctorsListBox.getChildren().add(error);
        }
    }

    private VBox createDoctorCard(Doctor doctor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("donor-card");
        card.setPadding(new Insets(16));

        // Doctor name
        Label nameLabel = new Label(doctor.getName());
        nameLabel.getStyleClass().add("donor-title");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800;");

        // Degree
        Label degreeLabel = new Label(doctor.getDegree());
        degreeLabel.getStyleClass().add("donor-meta");
        degreeLabel.setStyle("-fx-text-fill: #475569; -fx-font-weight: 600;");

        // Specialization
        if (doctor.getSpecialization() != null && !doctor.getSpecialization().isBlank()) {
            Label specLabel = new Label("Specialization: " + doctor.getSpecialization());
            specLabel.getStyleClass().add("donor-meta");
            card.getChildren().addAll(nameLabel, degreeLabel, specLabel);
        } else {
            card.getChildren().addAll(nameLabel, degreeLabel);
        }

        // Phone
        Label phoneLabel = new Label("Phone: " + doctor.getPhone());
        phoneLabel.getStyleClass().add("donor-meta");

        // Consulting hours
        Label hoursLabel = new Label("Hours: " + doctor.getConsultingHours());
        hoursLabel.getStyleClass().add("donor-meta");

        // Book appointment button
        Button bookBtn = new Button("Book Appointment");
        bookBtn.getStyleClass().add("seek-action-btn");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setOnAction(e -> showAppointmentDialog(doctor));

        card.getChildren().addAll(phoneLabel, hoursLabel, bookBtn);

        return card;
    }

    private void showAppointmentDialog(Doctor doctor) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Book Appointment with " + doctor.getName());

        VBox dialogContent = new VBox(16);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setAlignment(Pos.TOP_LEFT);
        dialogContent.setStyle("-fx-background-color: #f8fafc;");

        // Title
        Label titleLabel = new Label("Book Appointment");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        // Doctor info
        Label doctorInfo = new Label("Dr. " + doctor.getName() + " - " + doctor.getDegree());
        doctorInfo.setStyle("-fx-text-fill: #475569; -fx-font-weight: 600;");

        // Date picker
        Label dateLabel = new Label("Select Appointment Date:");
        dateLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #1f2937;");

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Feedback label
        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button confirmBtn = new Button("Confirm Booking");
        confirmBtn.getStyleClass().add("seek-action-btn");
        confirmBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) {
                feedbackLabel.setText("Please select a date.");
                feedbackLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 700;");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }

            String dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Appointment appointment = new Appointment(this.soulId, doctor.getId(), dateStr);

            try {
                doctorRepository.saveAppointment(appointment);
                feedbackLabel.setText("✓ Appointment booked successfully!");
                feedbackLabel.setStyle("-fx-text-fill: #065f46; -fx-font-weight: 700;");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);

                // Close dialog after 1.5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(dialog::close);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } catch (SQLException ex) {
                ex.printStackTrace();
                feedbackLabel.setText("Error booking appointment. Please try again.");
                feedbackLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 700;");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: 700; -fx-background-radius: 12; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);

        dialogContent.getChildren().addAll(
                titleLabel,
                doctorInfo,
                dateLabel,
                datePicker,
                feedbackLabel,
                buttonBox
        );

        Scene scene = new Scene(dialogContent, 420, 320);

        // Apply CSS
        var seekCss = getClass().getResource("/com/the_pathfinders/css/SeekHelp.css");
        if (seekCss != null) scene.getStylesheets().add(seekCss.toExternalForm());
        var loginCss = getClass().getResource("/com/the_pathfinders/css/login_signup.css");
        if (loginCss != null) scene.getStylesheets().add(loginCss.toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
