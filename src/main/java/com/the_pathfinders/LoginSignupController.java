package com.the_pathfinders;

import com.the_pathfinders.db.SoulRepository;
import com.the_pathfinders.db.SoulRepository.DuplicateIdException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoginSignupController implements Initializable {

    /* Repo */
    private SoulRepository repo;
    public void setRepository(SoulRepository repo) { this.repo = repo; }

    /* Header */
    @FXML private ImageView logoImage;
    @FXML private Label subtitleLabel;
    @FXML private AnchorPane root;
    @FXML private ToggleButton musicToggle;
    @FXML private ToggleButton themeToggle;

    /* Tabs + border */
    @FXML private StackPane tabStack;
    @FXML private HBox menuBar;
    @FXML private Button loginBtn, signUpBtn;

    /* Forms */
    @FXML private VBox loginForm, signupForm;
    @FXML private TextField tfLoginId, tfSoulName, tfSoulId, tfMobile;
    @FXML private PasswordField pfLoginKey, pfSoulKey;
    @FXML private TextField tfLoginKeyVisible, tfSoulKeyVisible;
    @FXML private CheckBox cbShowLoginKey, cbShowSignupKey;
    @FXML private ComboBox<String> cbCountryCode;
    @FXML private TextField tfDob;
    @FXML private Label lblLoginStatus, lblSubmitStatus;
    @FXML private Button btnLoginSubmit, btnSubmit;

    /* State */
    private boolean isLoginSelected = true;

    /* Styles for status labels */
    private static final String OK_STYLE  = "-fx-text-fill: #2e7d32; -fx-font-size: 12px;";
    private static final String ERR_STYLE = "-fx-text-fill: #e53935; -fx-font-size: 12px;";
    private void setOk(Label l, String msg){ l.setText(msg); l.setStyle(OK_STYLE); }
    private void setErr(Label l, String msg){ l.setText(msg); l.setStyle(ERR_STYLE); }

    /* Subtitles */
    private final String[] subtitles = {
            "A place for refreshing the soul",
            "The ultimate home for your closest ones"
    };
    private int subtitleIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Logo (centered, big)
        try {
            URL a = getClass().getResource("/assets/images/shelter_of_mind.png");
            a = getClass().getResource("/assets/images/logo_testing.png");
            if (a != null) {
                Image img = new Image(a.toExternalForm(), 360, 360, true, true);
                logoImage.setImage(img);
            }
        } catch (Exception ignored) {}

        // Attach background video from VideoManager (reuses existing instance)
        attachBackgroundVideo();

        // Subtitle alternating
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(3), e -> switchSubtitle()));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();

        // Tab actions (text-only)
        loginBtn.setOnAction(e -> switchMenu(true));
        signUpBtn.setOnAction(e -> switchMenu(false));

    // Ensure CSS 'active' class reflects current selection
    updateActiveStyles();

        // Music toggle wiring
        if (musicToggle != null) {
            musicToggle.setSelected(MusicManager.isBackgroundMusicEnabled());
            updateMusicToggleText();
            musicToggle.setOnAction(e -> toggleMusicEnabled());
        }

        // Theme toggle wiring (smooth cross-fade)
        if (themeToggle != null && root != null) {
            // initial state
            themeToggle.setSelected(root.getStyleClass().contains("dark-theme"));
            updateThemeToggleText();
            themeToggle.setOnAction(e -> toggleThemeWithFade());
        }

        // Border path removed

        // Form wiring
        tfLoginKeyVisible.textProperty().bindBidirectional(pfLoginKey.textProperty());
        tfSoulKeyVisible.textProperty().bindBidirectional(pfSoulKey.textProperty());
        cbShowLoginKey.setOnAction(e -> toggleShowLoginKey(cbShowLoginKey.isSelected()));
        cbShowSignupKey.setOnAction(e -> toggleShowSignupKey(cbShowSignupKey.isSelected()));
        // Set date format hint
        tfDob.setPromptText("DD/MM/YYYY");
        cbCountryCode.getItems().setAll("+1 (USA)", "+91 (IND)", "+880 (BAN)");
        cbCountryCode.getSelectionModel().selectFirst();

        btnLoginSubmit.setOnAction(e -> onSubmitLogin());
        btnSubmit.setOnAction(e -> onSubmitSignup());

        // Fix blue buttons size
        btnLoginSubmit.setPrefSize(220, 50);
        btnSubmit.setPrefSize(220, 50);

    // Initial visibility: show login primary button by default, hide signup primary button
    if (btnLoginSubmit != null) { btnLoginSubmit.setVisible(true); btnLoginSubmit.setManaged(true); }
    if (btnSubmit != null) { btnSubmit.setVisible(false); btnSubmit.setManaged(false); }
    if (lblLoginStatus != null) { lblLoginStatus.setVisible(true); lblLoginStatus.setManaged(true); }
    if (lblSubmitStatus != null) { lblSubmitStatus.setVisible(false); lblSubmitStatus.setManaged(false); }

        // Reserve space for messages so layout never jumps
        lblLoginStatus.setMinHeight(18);
        lblSubmitStatus.setMinHeight(18);


    }

    /**
     * Toggle the CSS "active" class on the tab buttons so the
     * glow / elevated styles from the stylesheet are applied to
     * the currently selected tab.
     */
    private void updateActiveStyles() {
        if (isLoginSelected) {
            if (!loginBtn.getStyleClass().contains("active")) loginBtn.getStyleClass().add("active");
            signUpBtn.getStyleClass().remove("active");
        } else {
            if (!signUpBtn.getStyleClass().contains("active")) signUpBtn.getStyleClass().add("active");
            loginBtn.getStyleClass().remove("active");
        }
    }

    private void updateMusicToggleText() {
        if (musicToggle == null) return;
        musicToggle.setText(musicToggle.isSelected() ? "Music: On" : "Music: Off");
    }

    private void toggleMusicEnabled() {
        boolean enableMusic = musicToggle != null && musicToggle.isSelected();
        MusicManager.setBackgroundMusicEnabled(enableMusic);
        updateMusicToggleText();
    }

    /* ---------- Theme toggling (smooth cross-fade) ---------- */
    private void updateThemeToggleText() {
        if (themeToggle == null) return;
        themeToggle.setText(themeToggle.isSelected() ? "Dark" : "Light");
    }

    private void toggleThemeWithFade() {
        if (root == null || themeToggle == null) return;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), root);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.96);
        fadeOut.setOnFinished(ev -> {
            // toggle class
            if (root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().remove("dark-theme");
                themeToggle.setSelected(false);
            } else {
                root.getStyleClass().add("dark-theme");
                themeToggle.setSelected(true);
            }
            updateThemeToggleText();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), root);
            fadeIn.setFromValue(0.96); fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    /* ---------- Subtitle ---------- */
    private void switchSubtitle() {
        FadeTransition out = new FadeTransition(Duration.millis(220), subtitleLabel);
        out.setToValue(0);
        out.setOnFinished(ev -> {
            subtitleLabel.setText(subtitles[++subtitleIndex % subtitles.length]);
            FadeTransition in = new FadeTransition(Duration.millis(220), subtitleLabel);
            in.setToValue(1); in.play();
        });
        out.play();
    }



    /* ---------- Tabs ---------- */
    private void switchMenu(boolean toLogin) {
        if (isLoginSelected == toLogin) return;
        isLoginSelected = toLogin;

        // Clear all form fields and error messages
        clearAllFields();

        // Show/Hide forms (centered; button slot fixed)
        boolean showSignup = !isLoginSelected;
        signupForm.setVisible(showSignup); signupForm.setManaged(showSignup);
        loginForm.setVisible(!showSignup); loginForm.setManaged(!showSignup);

        // Toggle which primary button is visible at the bottom and which status label is shown
        if (btnLoginSubmit != null) { btnLoginSubmit.setVisible(isLoginSelected); btnLoginSubmit.setManaged(isLoginSelected); }
        if (btnSubmit != null) { btnSubmit.setVisible(!isLoginSelected); btnSubmit.setManaged(!isLoginSelected); }
        if (lblLoginStatus != null) { lblLoginStatus.setVisible(isLoginSelected); lblLoginStatus.setManaged(isLoginSelected); }
        if (lblSubmitStatus != null) { lblSubmitStatus.setVisible(!isLoginSelected); lblSubmitStatus.setManaged(!isLoginSelected); }

        // Update css classes (glow) to reflect active tab
        updateActiveStyles();
    }

    /* ---------- Show/Hide password fields ---------- */
    private void toggleShowLoginKey(boolean show) {
        tfLoginKeyVisible.setVisible(show); tfLoginKeyVisible.setManaged(show);
        pfLoginKey.setVisible(!show);       pfLoginKey.setManaged(!show);
    }
    private void toggleShowSignupKey(boolean show) {
        tfSoulKeyVisible.setVisible(show); tfSoulKeyVisible.setManaged(show);
        pfSoulKey.setVisible(!show);       pfSoulKey.setManaged(!show);
    }

    /* ---------- Helpers ---------- */


    private String value(TextInputControl c) { String s=c.getText(); return s==null?"":s.trim(); }

    /* Clear all form fields and error messages */
    private void clearAllFields() {
        // Clear login form
        tfLoginId.clear();
        pfLoginKey.clear();
        tfLoginKeyVisible.clear();
        lblLoginStatus.setText("");

        // Clear signup form
        tfSoulName.clear();
        tfSoulId.clear();
        pfSoulKey.clear();
        tfSoulKeyVisible.clear();
        tfMobile.clear();
        tfDob.clear();
        lblSubmitStatus.setText("");

        // Reset checkboxes
        cbShowLoginKey.setSelected(false);
        cbShowSignupKey.setSelected(false);

        // Reset country code
        cbCountryCode.getSelectionModel().selectFirst();
    }

    /* ---------- Submit handlers (unchanged logic shell) ---------- */
    private void onSubmitLogin() {
        String id = value(tfLoginId), key = value(pfLoginKey);
        if (id.isEmpty() || key.isEmpty()) { setErr(lblLoginStatus, "Fill all fields!"); return; }
        
        // Clear previous messages and disable button during operation
        lblLoginStatus.setText("");
        btnLoginSubmit.setDisable(true);
        
        // Run database operations in background thread to keep UI and video responsive
        new Thread(() -> {
            try {
                if (repo == null) throw new IllegalStateException("Repository not set");
                
                // Database verification (blocking operation)
                boolean isValid = repo.verify(id, key);
                
                if (!isValid) {
                    Platform.runLater(() -> {
                        setErr(lblLoginStatus, "Invalid ID or Key.");
                        btnLoginSubmit.setDisable(false);
                    });
                    return;
                }
                
                // Update activity timestamp on successful login
                com.the_pathfinders.util.ActivityTracker.updateActivitySync(id);
                
                // Fetch display name (if available)
                String name = fetchNameForId(id);
                
                // Switch to dashboard on FX thread
                Platform.runLater(() -> {
                    openDashboard(id, name);
                    btnLoginSubmit.setDisable(false);
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setErr(lblLoginStatus, "Server error!");
                    btnLoginSubmit.setDisable(false);
                });
            }
        }).start();
    }

    // Fetch soul_name from DB (returns id if name not found)
    private String fetchNameForId(String id) {
        try (var c = com.the_pathfinders.db.DB.getConnection();
             var ps = c.prepareStatement("select soul_name from soul_id_and_soul_key where soul_id = ?")) {
            ps.setString(1, id.toLowerCase());
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    String n = rs.getString(1);
                    return (n == null || n.isBlank()) ? id : n;
                }
            }
        } catch (Exception ignored) {}
        return id;
    }

    // Load dashboard.fxml, set user context and swap scene root
    private void openDashboard(String id, String name) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            javafx.scene.Parent dashRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof DashboardController dc) {
                dc.setUser(id, name);
            }
            // replace current scene root
            if (root != null && root.getScene() != null) {
                dispose(); // clean up media player before switching scenes
                root.getScene().setRoot(dashRoot);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setErr(lblLoginStatus, "Failed to open dashboard: " + ex.getMessage());
        }
    }

    /** Clean up - detach video from this pane (but keep it alive for reuse) */
    private void dispose() {
        try {
            VideoManager.getInstance().detachFromPane(root);
        } catch (Exception ex) {
            System.err.println("Error during video detach: " + ex.getMessage());
        }
    }

    /* ---------- Background Video ---------- */
    
    /**
     * Attach the background video from VideoManager (reuses existing instance).
     */
    private void attachBackgroundVideo() {
        if (root == null) return;
        
        // Wait for scene to be attached
        if (root.getScene() != null) {
            VideoManager.getInstance().attachToPane(root);
        } else {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> VideoManager.getInstance().attachToPane(root));
                }
            });
        }
    }

    /* Validation helpers */
    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z][a-zA-Z .\\-]*[a-zA-Z]$");
    }

    private boolean isValidSoulId(String id) {
        return id.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isValidSoulKey(String key) {
        return key.matches("^[a-zA-Z0-9_]{8,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]+$") && phone.length() >= 8 && phone.length() <= 12;
    }

    private void onSubmitSignup() {
        // Clear previous status message first
        lblSubmitStatus.setText("");
        
        String n=value(tfSoulName), id=value(tfSoulId), key=value(pfSoulKey), mob=value(tfMobile), dob=value(tfDob);
        if (n.isEmpty() || id.isEmpty() || key.isEmpty() || mob.isEmpty()) { 
            setErr(lblSubmitStatus,"Fill all fields!"); 
            return; 
        }

        // Validate name format
        if (!isValidName(n)) {
            setErr(lblSubmitStatus, "Name must contain only letters, spaces, dots, and hyphens");
            return;
        }

        // Validate date format first
        LocalDate dobDate = null;
        if (!dob.isEmpty()) {
            try {
                dobDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                setErr(lblSubmitStatus, "Invalid date format! Use DD/MM/YYYY"); 
                return;
            }
        }

        // Validate soul ID format
        if (!isValidSoulId(id)) {
            setErr(lblSubmitStatus, "Soul ID can only contain letters, numbers, and underscore");
            return;
        }

        // Validate soul key format and length
        if (!isValidSoulKey(key)) {
            setErr(lblSubmitStatus, "Soul Key must be at least 8 characters and contain only letters, numbers, and underscore");
            return;
        }

        // Validate phone number
        if (!isValidPhone(mob)) {
            setErr(lblSubmitStatus, "Phone number must be between 8 and 12 digits");
            return;
        }

        // Disable button during operation
        btnSubmit.setDisable(true);
        
        // Run database operations in background thread to keep UI and video responsive
        final LocalDate finalDobDate = dobDate;
        new Thread(() -> {
            try {
                if (repo==null) throw new IllegalStateException("Repository not set");
                
                // Check for duplicate ID (blocking operation)
                if (repo.idExists(id)) {
                    Platform.runLater(() -> {
                        setErr(lblSubmitStatus,"Duplicate ID!");
                        btnSubmit.setDisable(false);
                    });
                    return;
                }
                
                // Create new soul record (blocking operation)
                repo.create(new SoulRepository.Soul(id, key, n, finalDobDate, mob, cbCountryCode.getSelectionModel().getSelectedItem()));
                
                Platform.runLater(() -> {
                    setOk(lblSubmitStatus,"Success! Your data is saved!");
                    btnSubmit.setDisable(false);
                });
                
            } catch (DuplicateIdException d) {
                Platform.runLater(() -> {
                    setErr(lblSubmitStatus,"Duplicate ID!");
                    btnSubmit.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setErr(lblSubmitStatus,"Server error!");
                    btnSubmit.setDisable(false);
                });
            }
        }).start();
    }
}
