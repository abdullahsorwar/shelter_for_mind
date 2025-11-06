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
import javafx.scene.paint.Color;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
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
    @FXML private ToggleButton themeToggle;
    // Background video player & overlay
    private MediaPlayer bgPlayer;
    private MediaView bgView;
    private Rectangle videoOverlay;

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
            URL a = getClass().getResource("/assets/images/logo_new.png");
            if (a != null) {
                Image img = new Image(a.toExternalForm(), 360, 360, true, true);
                logoImage.setImage(img);
            }
        } catch (Exception ignored) {}

        // Background video (if provided) — play behind the UI and add a subtle dark overlay
        try {
            URL v = getClass().getResource("/assets/videos/background.mp4");
            if (v != null && root != null) {
                Media media = new Media(v.toExternalForm());
                bgPlayer = new MediaPlayer(media);
                
                // Set up media player before creating the view
                bgPlayer.setOnReady(() -> {
                    bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    bgPlayer.play();
                });
                
                bgPlayer.setOnError(() -> {
                    System.err.println("Media error: " + bgPlayer.getError());
                    dispose(); // Use existing dispose() method instead of cleanupVideo
                });
                
                // Handle status changes
                bgPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    if (newStatus == MediaPlayer.Status.READY) {
                        bgPlayer.play();
                    } else if (newStatus == MediaPlayer.Status.HALTED || 
                             newStatus == MediaPlayer.Status.DISPOSED ||
                             newStatus == MediaPlayer.Status.STALLED) {
                        System.err.println("Media loading issue: " + newStatus);
                        dispose();
                    }
                });
                
                bgPlayer.setMute(true);
                
                bgView = new MediaView(bgPlayer);
                bgView.setPreserveRatio(false);
                bgView.setSmooth(true);
                bgView.setCache(true); // Enable caching for better performance
                bgView.setMouseTransparent(true);
                bgView.fitWidthProperty().bind(root.widthProperty());
                bgView.fitHeightProperty().bind(root.heightProperty());

                videoOverlay = new Rectangle();
                videoOverlay.widthProperty().bind(root.widthProperty());
                videoOverlay.heightProperty().bind(root.heightProperty());
                videoOverlay.setFill(Color.rgb(6, 8, 12, 0.30));
                videoOverlay.setMouseTransparent(true);

                // Ensure UI updates happen on the JavaFX Application Thread
                Platform.runLater(() -> {
                    if (root != null && root.getScene() != null) {
                        root.getChildren().add(0, bgView);
                        root.getChildren().add(1, videoOverlay);
                        // Start loading the media
                        bgPlayer.setAutoPlay(false); // We'll control playback explicitly
                    }
                });
            }
        } catch (Exception ex) {
            System.err.println("Video initialization error: " + ex.getMessage());
            ex.printStackTrace();
            dispose();
        }

        // Subtitle alternating
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(3), e -> switchSubtitle()));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();

        // Tab actions (text-only)
        loginBtn.setOnAction(e -> switchMenu(true));
        signUpBtn.setOnAction(e -> switchMenu(false));

    // Ensure CSS 'active' class reflects current selection
    updateActiveStyles();

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
        try {
            if (repo == null) throw new IllegalStateException("Repository not set");
            if (!repo.verify(id, key)) { setErr(lblLoginStatus, "Invalid ID or Key."); return; }
            // fetch display name (if available) and open dashboard
            String name = fetchNameForId(id);
            openDashboard(id, name);
        } catch (Exception ex) { setErr(lblLoginStatus, "Server error!"); }
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

    /** Clean up MediaPlayer and bindings when this controller is no longer needed */
    private void dispose() {
        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.dispose();
            bgPlayer = null;
        }
        if (bgView != null) {
            bgView.setMediaPlayer(null);
            bgView = null;
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

        try {
            if (repo==null) throw new IllegalStateException("Repository not set");
            if (repo.idExists(id)) { setErr(lblSubmitStatus,"Duplicate ID!"); return; }
            
            repo.create(new SoulRepository.Soul(id, key, n, dobDate, mob, cbCountryCode.getSelectionModel().getSelectedItem()));
            setOk(lblSubmitStatus,"Success! Your data is saved!");
        } catch (DuplicateIdException d){ setErr(lblSubmitStatus,"Duplicate ID!"); }
          catch (Exception ex){ setErr(lblSubmitStatus,"Server error!"); }
    }
}