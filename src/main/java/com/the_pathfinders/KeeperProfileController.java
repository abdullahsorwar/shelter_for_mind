package com.the_pathfinders;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import com.the_pathfinders.db.KeeperRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class KeeperProfileController implements Initializable {

    @FXML private Pane root;
    @FXML private Pane backgroundPane;
    @FXML private Rectangle gradientRect;
    @FXML private StackPane mainContainer;
    @FXML private VBox profileContainer;
    
    // Header
    @FXML private Button backButton;
    
    // Profile Image
    @FXML private ImageView profileImageView;
    @FXML private Button changeImageBtn;
    @FXML private Button removeImageBtn;
    @FXML private Label imageInfoLabel;
    
    // Form Fields
    @FXML private TextField shortNameField;
    @FXML private Label shortNameError;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> countryCodeCombo;
    @FXML private TextField phoneField;
    @FXML private Label phoneError;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private Label bloodGroupError;
    
    // Action Buttons
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;
    
    private String currentKeeperId;
    private File selectedImageFile = null;
    private boolean hasCustomImage = false;
    private KeeperRepository.KeeperProfile originalProfile;
    
    public void setKeeperInfo(String keeperId) {
        this.currentKeeperId = keeperId;
        
        // Load profile data synchronously before showing page
        try {
            KeeperRepository.KeeperProfile profile = KeeperRepository.getKeeperProfile(keeperId);
            originalProfile = profile;
            
            emailField.setText(profile.email);
            
            if (profile.shortName != null && !profile.shortName.isEmpty()) {
                shortNameField.setText(profile.shortName);
            }
            
            if (profile.countryCode != null && !profile.countryCode.isEmpty()) {
                // Find and select the matching country code
                for (String item : countryCodeCombo.getItems()) {
                    if (item.startsWith(profile.countryCode)) {
                        countryCodeCombo.setValue(item);
                        break;
                    }
                }
            }
            
            if (profile.phone != null && !profile.phone.isEmpty()) {
                phoneField.setText(profile.phone);
            }
            
            if (profile.bloodGroup != null && !profile.bloodGroup.isEmpty()) {
                bloodGroupCombo.setValue(profile.bloodGroup);
            }
            
            // Load profile image
            loadProfileImage();
        } catch (Exception e) {
            System.err.println("Failed to load keeper profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Apply CSS
        root.getStylesheets().add(getClass().getResource("/com/the_pathfinders/css/keeper_profile.css").toExternalForm());
        
        // Apply current theme from ThemeManager
        com.the_pathfinders.util.ThemeManager.applyTheme(root);
        
        // Add circular clip to profile image
        Circle clip = new Circle(75, 75, 75);
        profileImageView.setClip(clip);
        
        // Populate blood groups
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        
        // Populate country codes
        countryCodeCombo.getItems().addAll(
            "+880 (Bangladesh)", "+1 (USA/Canada)", "+44 (UK)", "+91 (India)", 
            "+92 (Pakistan)", "+61 (Australia)", "+81 (Japan)", "+86 (China)",
            "+33 (France)", "+49 (Germany)", "+39 (Italy)", "+7 (Russia)",
            "+34 (Spain)", "+52 (Mexico)", "+55 (Brazil)", "+82 (South Korea)",
            "+971 (UAE)", "+966 (Saudi Arabia)", "+20 (Egypt)", "+90 (Turkey)"
        );
        
        // Bind to window size
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.prefWidthProperty().bind(newScene.widthProperty());
                root.prefHeightProperty().bind(newScene.heightProperty());
                backgroundPane.prefWidthProperty().bind(newScene.widthProperty());
                backgroundPane.prefHeightProperty().bind(newScene.heightProperty());
                gradientRect.widthProperty().bind(newScene.widthProperty());
                gradientRect.heightProperty().bind(newScene.heightProperty());
                
                // Make container responsive
                newScene.widthProperty().addListener((o, ov, nv) -> updateResponsiveLayout(nv.doubleValue()));
                newScene.heightProperty().addListener((o, ov, nv) -> updateContainerHeight(nv.doubleValue()));
                
                updateResponsiveLayout(newScene.getWidth());
                updateContainerHeight(newScene.getHeight());
            }
        });
        
        // Add validation listeners
        setupValidation();
        
        // Play entrance animation
        playEntranceAnimation();
    }
    
    private void playEntranceAnimation() {
        profileContainer.setOpacity(0);
        profileContainer.setScaleX(0.95);
        profileContainer.setScaleY(0.95);
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), profileContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), profileContainer);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition entrance = new ParallelTransition(fade, scale);
        entrance.setDelay(Duration.millis(100));
        entrance.play();
    }
    
    private void updateResponsiveLayout(double windowWidth) {
        if (mainContainer != null) {
            mainContainer.setPrefWidth(windowWidth);
            
            // Profile container will be centered by StackPane with max width of 900px
            double containerWidth = Math.min(900, windowWidth - 100);
            profileContainer.setMaxWidth(containerWidth);
            profileContainer.setPrefWidth(containerWidth);
        }
    }
    
    private void updateContainerHeight(double windowHeight) {
        if (mainContainer != null) {
            double containerHeight = windowHeight - 110;
            mainContainer.setPrefHeight(containerHeight);
            profileContainer.setMaxHeight(containerHeight);
            profileContainer.setPrefHeight(containerHeight);
        }
    }
    
    private void setupValidation() {
        // Short name validation
        shortNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                if (newVal.length() > 50) {
                    showFieldError(shortNameError, "Name too long (max 50 characters)");
                } else {
                    hideFieldError(shortNameError);
                }
            } else {
                hideFieldError(shortNameError);
            }
        });
        
        // Phone validation
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                if (!newVal.matches("\\d*")) {
                    phoneField.setText(oldVal);
                } else if (newVal.length() > 15) {
                    showFieldError(phoneError, "Phone number too long");
                } else {
                    hideFieldError(phoneError);
                }
            } else {
                hideFieldError(phoneError);
            }
        });
    }
    
    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideFieldError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    

    
    private void loadProfileImage() {
        try {
            String imagePath = "/assets/keeper_img/" + currentKeeperId + ".jpg";
            URL imageUrl = getClass().getResource(imagePath);
            
            if (imageUrl != null) {
                Image profileImage = new Image(imageUrl.toExternalForm());
                profileImageView.setImage(profileImage);
                hasCustomImage = true;
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
            } else {
                // Use default icon
                URL defaultUrl = getClass().getResource("/assets/icons/user.png");
                if (defaultUrl != null) {
                    Image defaultImage = new Image(defaultUrl.toExternalForm());
                    profileImageView.setImage(defaultImage);
                }
                hasCustomImage = false;
                removeImageBtn.setVisible(false);
                removeImageBtn.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("Failed to load profile image: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JPEG Images", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        
        if (file != null) {
            // Validate image
            try {
                BufferedImage img = ImageIO.read(file);
                
                if (img == null) {
                    showAlert("Invalid Image", "The selected file is not a valid image.");
                    return;
                }
                
                int width = img.getWidth();
                int height = img.getHeight();
                
                // Check dimensions
                if (width > 600 || height > 600) {
                    showAlert("Image Too Large", 
                        "Image dimensions must be 600×600px or less.\n\n" +
                        "Selected image: " + width + "×" + height + "px");
                    return;
                }
                
                // Check file extension
                String fileName = file.getName().toLowerCase();
                if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                    showAlert("Invalid Format", "Only JPG/JPEG images are allowed.");
                    return;
                }
                
                // Image is valid - preview it
                selectedImageFile = file;
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
                
                imageInfoLabel.setText("✓ Image selected: " + width + "×" + height + "px");
                imageInfoLabel.setStyle("-fx-text-fill: #7bed9f;");
                
            } catch (IOException e) {
                showAlert("Error", "Failed to read image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRemoveImage() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Image");
        confirm.setHeaderText("Remove profile image?");
        confirm.setContentText("This will restore the default profile icon.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Load default icon
                try {
                    URL defaultUrl = getClass().getResource("/assets/icons/user.png");
                    if (defaultUrl != null) {
                        Image defaultImage = new Image(defaultUrl.toExternalForm());
                        profileImageView.setImage(defaultImage);
                    }
                    
                    selectedImageFile = null;
                    removeImageBtn.setVisible(false);
                    removeImageBtn.setManaged(false);
                    
                    imageInfoLabel.setText("JPG only • Max 600×600px");
                    imageInfoLabel.setStyle("");
                    
                    // Delete physical file if exists
                    if (hasCustomImage) {
                        deleteKeeperImage();
                    }
                    
                    hasCustomImage = false;
                } catch (Exception e) {
                    showAlert("Error", "Failed to remove image: " + e.getMessage());
                }
            }
        });
    }
    
    private void deleteKeeperImage() {
        try {
            // Get resources directory path
            String resourcesPath = getClass().getResource("/").getPath();
            Path imagePath = Paths.get(resourcesPath, "assets", "keeper_img", currentKeeperId + ".jpg");
            
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                System.out.println("Deleted keeper image: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to delete keeper image: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSave() {
        // Validate fields
        if (!validateFields()) {
            return;
        }
        
        // Disable save button
        saveBtn.setDisable(true);
        
        new Thread(() -> {
            try {
                // Extract country code from dropdown selection
                String selectedCountryCode = null;
                if (countryCodeCombo.getValue() != null) {
                    String selected = countryCodeCombo.getValue();
                    selectedCountryCode = selected.substring(0, selected.indexOf(" "));
                }
                
                // Create profile object
                KeeperRepository.KeeperProfile profile = new KeeperRepository.KeeperProfile();
                profile.keeperId = currentKeeperId;
                profile.email = emailField.getText().trim();
                profile.shortName = shortNameField.getText().trim();
                profile.phone = phoneField.getText().trim();
                profile.countryCode = selectedCountryCode;
                profile.bloodGroup = bloodGroupCombo.getValue();
                
                // Save to database
                KeeperRepository.updateKeeperProfile(profile);
                
                // Save image if selected
                if (selectedImageFile != null) {
                    saveKeeperImage();
                }
                
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    showAlert("Success", "Profile updated successfully!");
                    
                    // Reload profile image after save
                    loadProfileImage();
                });
            } catch (Exception e) {
                System.err.println("Failed to save profile: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    showAlert("Error", "Failed to save profile: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private boolean validateFields() {
        boolean isValid = true;
        
        // Validate short name
        String shortName = shortNameField.getText().trim();
        if (shortName.isEmpty()) {
            showFieldError(shortNameError, "Short name is required");
            isValid = false;
        } else if (shortName.length() > 50) {
            showFieldError(shortNameError, "Name too long (max 50 characters)");
            isValid = false;
        } else {
            hideFieldError(shortNameError);
        }
        
        // Validate phone if provided
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()) {
            if (!phone.matches("\\d+")) {
                showFieldError(phoneError, "Phone number must contain only digits");
                isValid = false;
            } else if (phone.length() < 7 || phone.length() > 15) {
                showFieldError(phoneError, "Phone number must be 7-15 digits");
                isValid = false;
            } else {
                hideFieldError(phoneError);
            }
        }
        
        return isValid;
    }
    
    private void saveKeeperImage() {
        try {
            // Get resources directory path in target
            String targetResourcesPath = "target/classes/assets/keeper_img/";
            Path targetDir = Paths.get(targetResourcesPath);
            
            // Create directory if not exists
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            
            // Copy image to target resources
            Path targetImagePath = targetDir.resolve(currentKeeperId + ".jpg");
            Files.copy(selectedImageFile.toPath(), targetImagePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Also copy to src resources for persistence
            String srcResourcesPath = "src/main/resources/assets/keeper_img/";
            Path srcDir = Paths.get(srcResourcesPath);
            
            if (!Files.exists(srcDir)) {
                Files.createDirectories(srcDir);
            }
            
            Path srcImagePath = srcDir.resolve(currentKeeperId + ".jpg");
            Files.copy(selectedImageFile.toPath(), srcImagePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Saved keeper image to: " + targetImagePath);
            System.out.println("Saved keeper image to: " + srcImagePath);
            
            hasCustomImage = true;
            selectedImageFile = null;
            
            imageInfoLabel.setText("JPG only • Max 600×600px");
            imageInfoLabel.setStyle("");
            
        } catch (Exception e) {
            System.err.println("Failed to save keeper image: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes");
            confirm.setContentText("Are you sure you want to go back without saving?");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    navigateBack();
                }
            });
        } else {
            navigateBack();
        }
    }
    
    private boolean hasUnsavedChanges() {
        if (originalProfile == null) return false;
        
        String currentShortName = shortNameField.getText().trim();
        String currentPhone = phoneField.getText().trim();
        String currentBloodGroup = bloodGroupCombo.getValue();
        
        String currentCountryCode = null;
        if (countryCodeCombo.getValue() != null) {
            String selected = countryCodeCombo.getValue();
            currentCountryCode = selected.substring(0, selected.indexOf(" "));
        }
        
        boolean shortNameChanged = !currentShortName.equals(originalProfile.shortName == null ? "" : originalProfile.shortName);
        boolean phoneChanged = !currentPhone.equals(originalProfile.phone == null ? "" : originalProfile.phone);
        boolean countryCodeChanged = currentCountryCode != null && !currentCountryCode.equals(originalProfile.countryCode);
        boolean bloodGroupChanged = currentBloodGroup != null && !currentBloodGroup.equals(originalProfile.bloodGroup);
        boolean imageChanged = selectedImageFile != null;
        
        return shortNameChanged || phoneChanged || countryCodeChanged || bloodGroupChanged || imageChanged;
    }
    
    @FXML
    private void handleBack() {
        handleCancel();
    }
    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/keeper_dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            // Pass keeper info to dashboard
            KeeperDashboardController controller = loader.getController();
            controller.setKeeperInfo(currentKeeperId);
            
            root.getScene().setRoot(dashboardRoot);
        } catch (Exception e) {
            System.err.println("Failed to navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
