package com.the_pathfinders;

import com.the_pathfinders.db.SoulRepository;
import com.the_pathfinders.db.SoulRepository.DuplicateIdException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.*;
import javafx.util.Duration;
import javafx.util.StringConverter;

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
    @FXML private Pane borderLayer;
    @FXML private Button loginBtn, signUpBtn;
    @FXML private Path borderPath;

    /* Forms */
    @FXML private VBox loginForm, signupForm;
    @FXML private TextField tfLoginId, tfSoulName, tfSoulId, tfMobile;
    @FXML private PasswordField pfLoginKey, pfSoulKey;
    @FXML private TextField tfLoginKeyVisible, tfSoulKeyVisible;
    @FXML private CheckBox cbShowLoginKey, cbShowSignupKey;
    @FXML private ComboBox<String> cbCountryCode;
    @FXML private DatePicker dpDob;
    @FXML private Label lblLoginStatus, lblSubmitStatus;
    @FXML private Button btnLoginSubmit, btnSubmit;

    /* State */
    private boolean isLoginSelected = true;
    private static final double ANIM_MS = 420;

    /* Border path elements:
       m0 -> bottom start at barLeft
       l1 -> bottom to leftX (grow/shrink)
       l2 -> up leftX to top
       l3 -> across top to rightX
       l4 -> down to bottom (rightX)
       l5 -> bottom to barRight
    */
    private MoveTo m0; private LineTo l1,l2,l3,l4,l5;

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
            URL a = getClass().getResource("/com/the_pathfinders/logo_new.png");
            if (a != null) {
                Image img = new Image(a.toExternalForm(), 360, 360, true, true);
                logoImage.setImage(img);
            }
        } catch (Exception ignored) {}

        // Background video (if provided) — play behind the UI and add a subtle dark overlay
        try {
            URL v = getClass().getResource("/com/the_pathfinders/background.mp4");
            if (v != null && root != null) {
                Media media = new Media(v.toExternalForm());
                bgPlayer = new MediaPlayer(media);
                bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgPlayer.setAutoPlay(true);
                bgPlayer.setMute(true);

                bgView = new MediaView(bgPlayer);
                bgView.setPreserveRatio(false);
                bgView.setSmooth(true);
                bgView.setMouseTransparent(true);
                bgView.fitWidthProperty().bind(root.widthProperty());
                bgView.fitHeightProperty().bind(root.heightProperty());

                videoOverlay = new Rectangle();
                videoOverlay.widthProperty().bind(root.widthProperty());
                videoOverlay.heightProperty().bind(root.heightProperty());
                // subtle darkening overlay (30% opacity)
                videoOverlay.setFill(Color.rgb(6, 8, 12, 0.30));
                videoOverlay.setMouseTransparent(true);

                // Add behind all existing children
                Platform.runLater(() -> {
                    // insert at very back
                    root.getChildren().add(0, bgView);
                    root.getChildren().add(1, videoOverlay);
                });
            }
        } catch (Exception ex) {
            // if video fails to load, we silently continue using static background
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

        // Border path setup
        borderPath.setStroke(Color.web("#4a90e2"));
        borderPath.setStrokeWidth(2);
        borderPath.setFill(Color.TRANSPARENT);
        borderPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        ensurePath();

        // Form wiring
        tfLoginKeyVisible.textProperty().bindBidirectional(pfLoginKey.textProperty());
        tfSoulKeyVisible.textProperty().bindBidirectional(pfSoulKey.textProperty());
        cbShowLoginKey.setOnAction(e -> toggleShowLoginKey(cbShowLoginKey.isSelected()));
        cbShowSignupKey.setOnAction(e -> toggleShowSignupKey(cbShowSignupKey.isSelected()));
        configureDobAsDDMMYYYY(dpDob);
        cbCountryCode.getItems().setAll("+1 (USA)", "+91 (IND)", "+880 (BAN)");
        cbCountryCode.getSelectionModel().selectFirst();

        btnLoginSubmit.setOnAction(e -> onSubmitLogin());
        btnSubmit.setOnAction(e -> onSubmitSignup());

        // Fix blue buttons size
        btnLoginSubmit.setPrefSize(220, 50);
        btnSubmit.setPrefSize(220, 50);

        // Reserve space for messages so layout never jumps
        lblLoginStatus.setMinHeight(18);
        lblSubmitStatus.setMinHeight(18);

        // Initial snap and resize listeners (no drift)
        Platform.runLater(() -> {
            snapBorderTo(loginBtn); // default active
            tabStack.widthProperty().addListener((o,ov,nv)->snapBorderTo(isLoginSelected?loginBtn:signUpBtn));
            tabStack.heightProperty().addListener((o,ov,nv)->snapBorderTo(isLoginSelected?loginBtn:signUpBtn));
            menuBar.widthProperty().addListener((o,ov,nv)->snapBorderTo(isLoginSelected?loginBtn:signUpBtn));
            loginBtn.widthProperty().addListener((o,ov,nv)->snapBorderTo(isLoginSelected?loginBtn:signUpBtn));
            signUpBtn.widthProperty().addListener((o,ov,nv)->snapBorderTo(isLoginSelected?loginBtn:signUpBtn));
        });
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

    /* ---------- Path building ---------- */
    private void ensurePath() {
        m0 = new MoveTo();
        l1 = new LineTo(); l2 = new LineTo(); l3 = new LineTo(); l4 = new LineTo(); l5 = new LineTo();
        borderPath.getElements().setAll(m0,l1,l2,l3,l4,l5);
    }

    /* Map node bounds into borderLayer space */
    private Bounds toLayer(Node n) {
        return borderLayer.sceneToLocal(n.localToScene(n.getBoundsInLocal()));
    }

    private void snapBorderTo(Button activeBtn) {
        // Bounds of active button text within the layer
        Bounds a = toLayer(activeBtn);
        Bounds bar = toLayer(menuBar);

        double leftX   = a.getMinX();
        double rightX  = a.getMaxX();
        double topY    = a.getMinY();
        double bottomY = a.getMaxY();

        double barLeft  = bar.getMinX();
        double barRight = bar.getMaxX();

        // Draw according to spec:
        // bottom line from barLeft to leftX
        // U-cap up/over/right of active
        // bottom line from rightX to barRight
        m0.setX(barLeft);  m0.setY(bottomY);
        l1.setX(leftX);    l1.setY(bottomY);
        l2.setX(leftX);    l2.setY(topY);
        l3.setX(rightX);   l3.setY(topY);
        l4.setX(rightX);   l4.setY(bottomY);
        l5.setX(barRight); l5.setY(bottomY);
    }

    private void animateBorderTo(Button activeBtn) {
        Bounds a = toLayer(activeBtn);
        Bounds bar = toLayer(menuBar);

        double leftX   = a.getMinX();
        double rightX  = a.getMaxX();
        double bottomY = a.getMaxY();

        double barLeft  = bar.getMinX();
        double barRight = bar.getMaxX();

        // Animate only the horizontal positions that should move
        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(ANIM_MS),
                new KeyValue(m0.xProperty(), barLeft),
                new KeyValue(l1.xProperty(), leftX),
                new KeyValue(l2.xProperty(), leftX),
                new KeyValue(l3.xProperty(), rightX),
                new KeyValue(l4.xProperty(), rightX),
                new KeyValue(l5.xProperty(), barRight),
                new KeyValue(m0.yProperty(), bottomY), // keep Y steady
                new KeyValue(l1.yProperty(), bottomY),
                new KeyValue(l4.yProperty(), bottomY),
                new KeyValue(l5.yProperty(), bottomY)
        ));
        tl.play();
    }

    /* ---------- Tabs ---------- */
    private void switchMenu(boolean toLogin) {
        if (isLoginSelected == toLogin) return;
        isLoginSelected = toLogin;

        // Show/Hide forms (centered; button slot fixed)
        boolean showSignup = !isLoginSelected;
        signupForm.setVisible(showSignup); signupForm.setManaged(showSignup);
        loginForm.setVisible(!showSignup); loginForm.setManaged(!showSignup);

        // Animate border to the new active tab
        animateBorderTo(isLoginSelected ? loginBtn : signUpBtn);
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
    private void configureDobAsDDMMYYYY(DatePicker dp) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dp.setPromptText("DD/MM/YYYY");
        dp.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate date) { return date == null ? "" : fmt.format(date); }
            @Override public LocalDate fromString(String s) {
                if (s == null || s.trim().isEmpty()) return null;
                try { return LocalDate.parse(s.trim(), fmt); } catch (Exception ex) { return null; }
            }
        });
    }

    private String value(TextInputControl c) { String s=c.getText(); return s==null?"":s.trim(); }

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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/the_pathfinders/dashboard.fxml"));
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
            setErr(lblLoginStatus, "Failed to open dashboard.");
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

    private void onSubmitSignup() {
        String n=value(tfSoulName), id=value(tfSoulId), key=value(pfSoulKey), mob=value(tfMobile);
        if (n.isEmpty() || id.isEmpty() || key.isEmpty() || mob.isEmpty()) { setErr(lblSubmitStatus,"Fill all fields!"); return; }
        try{
            if (repo==null) throw new IllegalStateException("Repository not set");
            if (repo.idExists(id)) { setErr(lblSubmitStatus,"Duplicate ID!"); return; }
            repo.create(new SoulRepository.Soul(id,key,n,dpDob.getValue(),mob, cbCountryCode.getSelectionModel().getSelectedItem()));
            setOk(lblSubmitStatus,"Success! Your data is saved!");
        } catch (DuplicateIdException d){ setErr(lblSubmitStatus,"Duplicate ID!"); }
          catch (Exception ex){ setErr(lblSubmitStatus,"Server error!"); }
    }
}