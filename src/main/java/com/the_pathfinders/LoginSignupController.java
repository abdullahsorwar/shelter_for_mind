package com.the_pathfinders;

import com.the_pathfinders.db.SoulRepository;
import com.the_pathfinders.db.SoulRepository.DuplicateIdException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoginSignupController implements Initializable {

    // Injected from App
    private SoulRepository repo;
    public void setRepository(SoulRepository repo) { this.repo = repo; }

    // LEFT PANE
    @FXML private ImageView logoImage;
    @FXML private Label titleLabel, subtitleLabel;

    // TABS / BORDER
    @FXML private VBox rightPane;
    @FXML private Button signUpBtn, loginBtn;
    @FXML private HBox   menuBar;
    @FXML private Pane   menuBorder;
    @FXML private Path   borderPath;

    // LOGIN FORM
    @FXML private VBox loginForm;
    @FXML private TextField tfLoginId;
    @FXML private PasswordField pfLoginKey;
    // Show password (login)
    @FXML private TextField tfLoginKeyVisible;
    @FXML private CheckBox  cbShowLoginKey;

    @FXML private Label errLoginId, errLoginKey, lblLoginStatus;
    @FXML private Button btnLoginSubmit;

    // SIGNUP FORM
    @FXML private VBox signupForm;
    @FXML private TextField tfSoulName, tfSoulId, tfMobile;
    @FXML private PasswordField pfSoulKey;
    @FXML private DatePicker dpDob;
    @FXML private ComboBox<String> cbCountryCode;
    // Show password (signup)
    @FXML private TextField tfSoulKeyVisible;
    @FXML private CheckBox  cbShowSignupKey;

    @FXML private Label errSoulName, errDob, errSoulId, errSoulKey, errMobile, lblSubmitStatus;
    @FXML private Button btnSubmit;

    // SUBTITLES
    private final String[] subtitles = {
            "A place for refreshing the soul",
            "The ultimate home for your closest ones"
    };
    private int subtitleIndex = 0;

    // TAB STATE
    private boolean isLoginSelected = true;  // start on Log in
    private boolean wasLoginSelected = true;

    // U-BORDER GEOMETRY
    private static final double ARC_RADIUS = 0.8;         // tiny curve
    private static final double PADDING_X = 6;
    private static final double PADDING_Y_TOP = 3;
    private static final double PADDING_Y_BOTTOM = 3;
    private static final double ANIM_MS = 360;

    private MoveTo e0; private LineTo e1, e2, e4, e6, e7; private ArcTo e3, e5;

    // STATUS STYLES (DB messages below buttons)
    private static final String OK_STYLE  = "-fx-text-fill: #2e7d32; -fx-font-size: 12px;";
    private static final String ERR_STYLE = "-fx-text-fill: #e53935; -fx-font-size: 12px;";
    private void setOk(Label l, String msg)  { l.setText(msg); l.setStyle(OK_STYLE); }
    private void setErr(Label l, String msg) { l.setText(msg); l.setStyle(ERR_STYLE); }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadLogo();
        playIntroAnimation();
        startSubtitleAlternation();

        // Tab actions
        signUpBtn.setOnAction(e -> switchMenu(false));
        loginBtn.setOnAction(e -> switchMenu(true));

        // U-border style
        borderPath.setStroke(Color.web("#1f1f1f"));
        borderPath.setStrokeWidth(1.25);
        borderPath.setFill(Color.TRANSPARENT);
        borderPath.setStrokeLineCap(StrokeLineCap.ROUND);
        borderPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        ensurePathElements();

        // Forms
        configureDobAsDDMMYYYY(dpDob);
        cbCountryCode.getItems().setAll("+1 (USA)", "+91 (IND)", "+880 (BAN)");
        cbCountryCode.getSelectionModel().selectFirst();

        // Show password wiring (bind text and set up toggle)
        if (tfLoginKeyVisible != null && pfLoginKey != null) {
            tfLoginKeyVisible.textProperty().bindBidirectional(pfLoginKey.textProperty());
        }
        if (tfSoulKeyVisible != null && pfSoulKey != null) {
            tfSoulKeyVisible.textProperty().bindBidirectional(pfSoulKey.textProperty());
        }
        if (cbShowLoginKey != null) cbShowLoginKey.setOnAction(e -> toggleShowLoginKey(cbShowLoginKey.isSelected()));
        if (cbShowSignupKey != null) cbShowSignupKey.setOnAction(e -> toggleShowSignupKey(cbShowSignupKey.isSelected()));
        toggleShowLoginKey(false);
        toggleShowSignupKey(false);

        // Buttons
        btnSubmit.setOnAction(e -> onSubmitSignup());
        btnLoginSubmit.setOnAction(e -> onSubmitLogin());

        // Keep action buttons ~25% of rightPane width
        rightPane.widthProperty().addListener((o, ov, nv) -> {
            double w = nv.doubleValue() * 0.25;
            btnSubmit.setPrefWidth(w);
            btnLoginSubmit.setPrefWidth(w);
        });

        // Re-snap border whenever layout can shift
        Runnable resnap = () -> snapBorderTo(isLoginSelected ? loginBtn : signUpBtn);
        menuBar.layoutBoundsProperty().addListener((o, a, b) -> resnap.run());
        menuBorder.widthProperty().addListener((o, a, b) -> resnap.run());
        signUpBtn.widthProperty().addListener((o, a, b) -> resnap.run());
        loginBtn.widthProperty().addListener((o, a, b) -> resnap.run());

        // Initial state after first layout pass
        Platform.runLater(() -> {
            applyTabStyles(true);
            setRightContentForCurrentTab();
            resnap.run();
            Platform.runLater(resnap); // once more after CSS
        });
    }

    /* ================= Logo & subtitle ================= */

    private void loadLogo() {
        try {
            URL a = getClass().getResource("/com/the_pathfinders/logo.png");
            URL b = getClass().getResource("/logo.png");
            URL chosen = (a != null) ? a : b;
            if (chosen != null) {
                Image img = new Image(chosen.toExternalForm(), true);
                logoImage.setImage(img);
                logoImage.setPreserveRatio(true);
                logoImage.setSmooth(true);
                if (logoImage.getFitHeight() <= 0) logoImage.setFitHeight(48);
            }
        } catch (Exception ignore) {}
    }

    private void playIntroAnimation() {
        logoImage.setOpacity(0);
        logoImage.setScaleX(1.5); logoImage.setScaleY(1.5);
        titleLabel.setOpacity(0); titleLabel.setScaleX(1.5); titleLabel.setScaleY(1.5);
        subtitleLabel.setOpacity(0); subtitleLabel.setScaleX(1.5); subtitleLabel.setScaleY(1.5);

        FadeTransition fadeLogo = new FadeTransition(Duration.seconds(1.2), logoImage);
        fadeLogo.setFromValue(0.0); fadeLogo.setToValue(1.0);
        ScaleTransition scaleLogo = new ScaleTransition(Duration.seconds(1.2), logoImage);
        scaleLogo.setFromX(1.5); scaleLogo.setFromY(1.5); scaleLogo.setToX(1.0); setTo(scaleLogo);

        FadeTransition fadeTitle = new FadeTransition(Duration.seconds(1.2), titleLabel);
        fadeTitle.setFromValue(0.0); fadeTitle.setToValue(1.0);
        ScaleTransition scaleTitle = new ScaleTransition(Duration.seconds(1.2), titleLabel);
        scaleTitle.setFromX(1.5); scaleTitle.setFromY(1.5); scaleTitle.setToX(1.0); setTo(scaleTitle);

        FadeTransition fadeSubtitle = new FadeTransition(Duration.seconds(1.2), subtitleLabel);
        fadeSubtitle.setFromValue(0.0); fadeSubtitle.setToValue(1.0);
        ScaleTransition scaleSubtitle = new ScaleTransition(Duration.seconds(1.2), subtitleLabel);
        scaleSubtitle.setFromX(1.5); scaleSubtitle.setFromY(1.5); scaleSubtitle.setToX(1.0); setTo(scaleSubtitle);

        new ParallelTransition(fadeLogo, scaleLogo, fadeTitle, scaleTitle, fadeSubtitle, scaleSubtitle).play();
    }

    private void setTo(ScaleTransition st) { st.setToX(1.0); st.setToY(1.0); }

    // Cross-fade only so layout never jiggles
    private void startSubtitleAlternation() {
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(3), e -> switchSubtitle()));
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
    }
    private void switchSubtitle() {
        FadeTransition outF = new FadeTransition(Duration.millis(220), subtitleLabel);
        outF.setToValue(0.0);
        outF.setOnFinished(ev -> {
            subtitleLabel.setText(nextSubtitle());
            FadeTransition inF = new FadeTransition(Duration.millis(220), subtitleLabel);
            inF.setToValue(1.0);
            inF.play();
        });
        outF.play();
    }
    private String nextSubtitle() {
        subtitleIndex = (subtitleIndex + 1) % subtitles.length;
        return subtitles[subtitleIndex];
    }

    /* ================= U-border ================= */

    private void ensurePathElements() {
        if (borderPath.getElements().size() == 8) {
            e0 = (MoveTo) borderPath.getElements().get(0);
            e1 = (LineTo) borderPath.getElements().get(1);
            e2 = (LineTo) borderPath.getElements().get(2);
            e3 = (ArcTo)  borderPath.getElements().get(3);
            e4 = (LineTo) borderPath.getElements().get(4);
            e5 = (ArcTo)  borderPath.getElements().get(5);
            e6 = (LineTo) borderPath.getElements().get(6);
            e7 = (LineTo) borderPath.getElements().get(7);
        } else {
            e0 = new MoveTo();
            e1 = new LineTo(); e2 = new LineTo();
            e3 = new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, 0, 0, false, true);
            e4 = new LineTo();
            e5 = new ArcTo(ARC_RADIUS, ARC_RADIUS, 0, 0, 0, false, true);
            e6 = new LineTo(); e7 = new LineTo();
            borderPath.getElements().setAll(e0, e1, e2, e3, e4, e5, e6, e7);
        }
    }

    private static class UGeom {
        final double leftX, rightX, topY, bottomY, leftEdge, rightEdge, r;
        UGeom(double leftX, double rightX, double topY, double bottomY,
              double leftEdge, double rightEdge, double r) {
            this.leftX = leftX; this.rightX = rightX; this.topY = topY; this.bottomY = bottomY;
            this.leftEdge = leftEdge; this.rightEdge = rightEdge; this.r = r;
        }
    }

    /** Coordinates in menuBorder space (scale/layout safe). */
    private UGeom computeGeometryFor(Button target) {
        Bounds sceneB = target.localToScene(target.getBoundsInLocal());
        Bounds b = menuBorder.sceneToLocal(sceneB);

        double leftX   = b.getMinX() - PADDING_X;
        double rightX  = b.getMaxX() + PADDING_X;
        double topY    = b.getMinY() - PADDING_Y_TOP;
        double bottomY = b.getMaxY() + PADDING_Y_BOTTOM;

        double leftEdge  = 0;
        double rightEdge = menuBorder.getWidth();

        return new UGeom(leftX, rightX, topY, bottomY, leftEdge, rightEdge, ARC_RADIUS);
    }

    private void applyToElements(UGeom g) {
        double r = g.r;
        e0.setX(g.leftEdge);  e0.setY(g.bottomY);
        e1.setX(g.leftX);     e1.setY(g.bottomY);
        e2.setX(g.leftX);     e2.setY(g.topY + r);
        e3.setRadiusX(r); e3.setRadiusY(r); e3.setX(g.leftX + r); e3.setY(g.topY);
        e4.setX(g.rightX - r); e4.setY(g.topY);
        e5.setRadiusX(r); e5.setRadiusY(r); e5.setX(g.rightX); e5.setY(g.topY + r);
        e6.setX(g.rightX); e6.setY(g.bottomY);
        e7.setX(g.rightEdge); e7.setY(g.bottomY);
    }

    private void snapBorderTo(Button target) { applyToElements(computeGeometryFor(target)); }

    private void animateBorder(boolean fromLogin, boolean toLogin) {
        UGeom g = computeGeometryFor(toLogin ? loginBtn : signUpBtn);

        boolean movingLeft  = fromLogin && !toLogin;  // login → sign
        boolean movingRight = !fromLogin &&  toLogin; // sign  → login

        Duration tLead = Duration.millis(ANIM_MS * 0.6);
        Duration tFollow = Duration.millis(ANIM_MS);
        Interpolator LEAD = Interpolator.SPLINE(0.2, 0.9, 0.2, 1.0);
        Interpolator LAG  = Interpolator.SPLINE(0.3, 0.0, 0.2, 1.0);

        Timeline tl = new Timeline(
            new KeyFrame(tFollow, new KeyValue(e0.yProperty(), g.bottomY, LAG)),
            new KeyFrame(tFollow, new KeyValue(e1.yProperty(), g.bottomY, LAG)),
            new KeyFrame(tFollow, new KeyValue(e2.yProperty(), g.topY + g.r, LAG)),
            new KeyFrame(tFollow, new KeyValue(e3.yProperty(), g.topY, LAG)),
            new KeyFrame(tFollow, new KeyValue(e4.yProperty(), g.topY, LAG)),
            new KeyFrame(tFollow, new KeyValue(e5.yProperty(), g.topY + g.r, LAG)),
            new KeyFrame(tFollow, new KeyValue(e6.yProperty(), g.bottomY, LAG)),
            new KeyFrame(tFollow, new KeyValue(e7.yProperty(), g.bottomY, LAG))
        );

        if (movingLeft) {
            tl.getKeyFrames().addAll(
                new KeyFrame(tLead,   new KeyValue(e1.xProperty(), g.leftX,       LEAD)),
                new KeyFrame(tLead,   new KeyValue(e2.xProperty(), g.leftX,       LEAD)),
                new KeyFrame(tLead,   new KeyValue(e3.xProperty(), g.leftX + g.r, LEAD)),
                new KeyFrame(tFollow, new KeyValue(e4.xProperty(), g.rightX - g.r,LAG)),
                new KeyFrame(tFollow, new KeyValue(e5.xProperty(), g.rightX,      LAG)),
                new KeyFrame(tFollow, new KeyValue(e6.xProperty(), g.rightX,      LAG)),
                new KeyFrame(tFollow, new KeyValue(e0.xProperty(), g.leftEdge,    LAG)),
                new KeyFrame(tFollow, new KeyValue(e7.xProperty(), g.rightEdge,   LAG))
            );
        } else if (movingRight) {
            tl.getKeyFrames().addAll(
                new KeyFrame(tFollow, new KeyValue(e1.xProperty(), g.leftX,       LAG)),
                new KeyFrame(tFollow, new KeyValue(e2.xProperty(), g.leftX,       LAG)),
                new KeyFrame(tFollow, new KeyValue(e3.xProperty(), g.leftX + g.r, LAG)),
                new KeyFrame(tLead,   new KeyValue(e4.xProperty(), g.rightX - g.r,LEAD)),
                new KeyFrame(tLead,   new KeyValue(e5.xProperty(), g.rightX,      LEAD)),
                new KeyFrame(tLead,   new KeyValue(e6.xProperty(), g.rightX,      LEAD)),
                new KeyFrame(tFollow, new KeyValue(e0.xProperty(), g.leftEdge,    LAG)),
                new KeyFrame(tFollow, new KeyValue(e7.xProperty(), g.rightEdge,   LAG))
            );
        }
        tl.play();
    }

    /* ================= Tabs & resets ================= */

    private void switchMenu(boolean toLogin) {
        if (isLoginSelected == toLogin) return;
        wasLoginSelected = isLoginSelected;
        isLoginSelected  = toLogin;

        // Open target tab clean
        if (toLogin) resetLoginForm(); else resetSignupForm();

        applyTabStyles(toLogin);
        setRightContentForCurrentTab();
        animateBorder(wasLoginSelected, isLoginSelected);
    }

    private void applyTabStyles(boolean loginSelectedNow) {
        if (loginSelectedNow) {
            loginBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f1f1f;");
            signUpBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-font-weight: normal; -fx-text-fill: #1f1f1f;");
        } else {
            signUpBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f1f1f;");
            loginBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-font-weight: normal; -fx-text-fill: #1f1f1f;");
        }
    }

    private void setRightContentForCurrentTab() {
        boolean showSignup = !isLoginSelected;
        signupForm.setVisible(showSignup); signupForm.setManaged(showSignup);
        loginForm.setVisible(!showSignup); loginForm.setManaged(!showSignup);
    }

    /* ================= Helpers: show password + resets ================= */

    private void toggleShowLoginKey(boolean show) {
        if (tfLoginKeyVisible == null || pfLoginKey == null) return;
        tfLoginKeyVisible.setVisible(show);
        tfLoginKeyVisible.setManaged(show);
        pfLoginKey.setVisible(!show);
        pfLoginKey.setManaged(!show);
    }

    private void toggleShowSignupKey(boolean show) {
        if (tfSoulKeyVisible == null || pfSoulKey == null) return;
        tfSoulKeyVisible.setVisible(show);
        tfSoulKeyVisible.setManaged(show);
        pfSoulKey.setVisible(!show);
        pfSoulKey.setManaged(!show);
    }

    private void resetLoginForm() {
        tfLoginId.clear();
        pfLoginKey.clear();
        if (tfLoginKeyVisible != null) tfLoginKeyVisible.clear();
        clearLoginErrors();
        lblLoginStatus.setText("");
        lblLoginStatus.setStyle(null);
        // ensure masked by default
        toggleShowLoginKey(false);
        if (cbShowLoginKey != null) cbShowLoginKey.setSelected(false);
    }

    private void resetSignupForm() {
        tfSoulName.clear();
        dpDob.setValue(null);
        tfSoulId.clear();
        pfSoulKey.clear();
        if (tfSoulKeyVisible != null) tfSoulKeyVisible.clear();
        tfMobile.clear();
        if (cbCountryCode.getItems() != null && !cbCountryCode.getItems().isEmpty()) {
            cbCountryCode.getSelectionModel().selectFirst();
        }
        clearSignupErrors();
        lblSubmitStatus.setText("");
        lblSubmitStatus.setStyle(null);
        toggleShowSignupKey(false);
        if (cbShowSignupKey != null) cbShowSignupKey.setSelected(false);
    }

    /* ================= Formatters ================= */

    private void configureDobAsDDMMYYYY(DatePicker dp) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dp.setPromptText("DD/MM/YYYY");
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) { return date == null ? "" : fmt.format(date); }
            @Override public LocalDate fromString(String s) {
                if (s == null || s.trim().isEmpty()) return null;
                try { return LocalDate.parse(s.trim(), fmt); } catch (Exception ex) { return null; }
            }
        });
    }

    /* ================= SIGN UP (DB-backed) ================= */

    private void onSubmitSignup() {
        // Clear per-field errors; status label will be (re)colored by setOk/setErr
        clearSignupErrors();
        lblSubmitStatus.setText("");
        lblSubmitStatus.setStyle(null);

        String name = value(tfSoulName);
        if (name.isEmpty() || !name.matches("^[A-Za-z.]+(?:\\s+[A-Za-z]+)*$")) {
            errSoulName.setText("ERROR! Name should only contain Latin Alphabets");
            return;
        }

        LocalDate dob = dpDob.getValue();
        if (dob == null) {
            errDob.setText("ERROR! Please select a valid date of birth");
            return;
        }

        String soulId = value(tfSoulId);
        if (soulId.isEmpty() || !soulId.matches("^[A-Za-z0-9_]+$")) {
            errSoulId.setText("ERROR! Soul_ID should contain A-Z, a-z, 0-9 and _ only");
            return;
        }

        String soulKey = value(pfSoulKey);
        if (soulKey.isEmpty() || !soulKey.matches("^[A-Za-z0-9_]+$")) {
            errSoulKey.setText("ERROR! Soul_Key can contain A-Z,a-z,0-9 and _ only");
            return;
        }

        String mobile = value(tfMobile);
        if (mobile.isEmpty() || !mobile.matches("^\\d+$")) {
            errMobile.setText("ERROR! Must be a number");
            return;
        }

        String code = cbCountryCode.getSelectionModel().getSelectedItem();

        // DB checks/messages below the button (lblSubmitStatus)
        try {
            if (repo == null) throw new IllegalStateException("Repository not set");
            if (repo.idExists(soulId)) {
                setErr(lblSubmitStatus, "ERROR! Duplicate id found! Enter a new one");
                return;
            }
            repo.create(new SoulRepository.Soul(soulId, soulKey, name, dob, mobile, code));
            setOk(lblSubmitStatus, "Success! Your data is saved!");
        } catch (DuplicateIdException dup) {
            setErr(lblSubmitStatus, "ERROR! Duplicate id found! Enter a new one");
        } catch (Exception ex) {
            setErr(lblSubmitStatus, "ERROR! Server unavailable. Try again.");
            ex.printStackTrace();
        }
    }

    private void clearSignupErrors() {
        errSoulName.setText("");
        errDob.setText("");
        errSoulId.setText("");
        errSoulKey.setText("");
        errMobile.setText("");
        // status style cleared in caller
    }

    /* ================= LOG IN (DB-backed) ================= */

    private void onSubmitLogin() {
        clearLoginErrors();
        lblLoginStatus.setText("");
        lblLoginStatus.setStyle(null);

        String id  = value(tfLoginId);
        if (id.isEmpty() || !id.matches("^[A-Za-z0-9_]+$")) {
            errLoginId.setText("ERROR! Soul_ID should contain A-Z, a-z, 0-9 and _ only");
            return;
        }

        String key = value(pfLoginKey);
        if (key.isEmpty() || !key.matches("^[A-Za-z0-9_]+$")) {
            errLoginKey.setText("ERROR! Soul_Key can contain A-Z, a-z, 0-9 and _ only");
            return;
        }

        try {
            if (repo == null) throw new IllegalStateException("Repository not set");
            boolean ok = repo.verify(id, key);
            if (!ok) {
                setErr(lblLoginStatus, "ERROR! Invalid id or key");
                return;
            }
            setOk(lblLoginStatus, "Success! You are logged in!");
        } catch (Exception ex) {
            setErr(lblLoginStatus, "ERROR! Server unavailable. Try again.");
            ex.printStackTrace();
        }
    }

    private void clearLoginErrors() {
        errLoginId.setText("");
        errLoginKey.setText("");
        // status style cleared in caller
    }

    /* ================= Utils ================= */

    private String value(TextInputControl c) {
        String s = c.getText();
        return (s == null) ? "" : s.trim();
    }
}