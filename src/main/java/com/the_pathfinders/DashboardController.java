package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.animation.FillTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.time.LocalTime;

import com.the_pathfinders.db.SoulRepository;

public class DashboardController {

    @FXML private AnchorPane root;
    @FXML private Label userLabel;
    @FXML private ImageView userImage;
    @FXML private ImageView logoImage;
    @FXML private VBox userDropdown;
    @FXML private Rectangle bgRect;
    @FXML private Label greetingLabel;
    
    @FXML private Button journalBtn;
    @FXML private Button blogBtn;
    @FXML private Button moodBtn;
    @FXML private Button insightsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private HBox buttonCardsBox;

    private String soulId;
    private double dragStartX = 0;

    public void initialize() {
        setLogo();
        try {
            if (greetingLabel != null) {
                int h = LocalTime.now().getHour();
                String g = (h < 12) ? "Good Morning " : (h < 18) ? "Good Afternoon " : "Good Evening , ready to unwind?";
                greetingLabel.setText(g);
            }
        } catch (Exception ignored) {}

        if (journalBtn != null) {
            journalBtn.setOnAction(e -> {
                javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
                javafx.scene.control.MenuItem privateItem = new javafx.scene.control.MenuItem("Private Journaling");
                javafx.scene.control.MenuItem publicItem = new javafx.scene.control.MenuItem("Public Journaling");
                privateItem.setOnAction(ev -> openPrivateJournals());
                publicItem.setOnAction(ev -> openPublicJournals());
                menu.getItems().addAll(privateItem, publicItem);
                menu.show(journalBtn, javafx.geometry.Side.RIGHT, 0, 4);
            });
        }
        if (blogBtn != null) blogBtn.setOnAction(e -> openBlogs());
        if (moodBtn != null) moodBtn.setOnAction(e -> showMoodPlaceholder());
        if (insightsBtn != null) insightsBtn.setOnAction(e -> showInsightsPlaceholder());
        if (settingsBtn != null) settingsBtn.setOnAction(e -> showSettingsPlaceholder());
        if (logoutBtn != null) logoutBtn.setOnAction(e -> onLogout());

        if (buttonCardsBox != null) {
            buttonCardsBox.getChildren().forEach(btn -> {
                btn.setOnMousePressed(e -> dragStartX = e.getSceneX());
                btn.setOnMouseDragged(e -> {
                    double dragDelta = e.getSceneX() - dragStartX;
                    if (Math.abs(dragDelta) > 20) {
                        int currentIdx = buttonCardsBox.getChildren().indexOf(btn);
                        if (dragDelta > 20 && currentIdx < buttonCardsBox.getChildren().size() - 1) {
                            swapButtons(currentIdx, currentIdx + 1);
                            dragStartX = e.getSceneX();
                        } else if (dragDelta < -20 && currentIdx > 0) {
                            swapButtons(currentIdx, currentIdx - 1);
                            dragStartX = e.getSceneX();
                        }
                    }
                });
            });
        }

        try {
            if (bgRect != null) {
                bgRect.widthProperty().bind(root.widthProperty());
                bgRect.heightProperty().bind(root.heightProperty());
                Color[] colors = new Color[] { Color.web("#E9D5FF"), Color.web("#D1FAE5"), Color.web("#FFE7D9") };
                for (int i = 0; i < colors.length; i++) {
                    Color from = colors[i];
                    Color to = colors[(i+1) % colors.length];
                    FillTransition ft = new FillTransition(Duration.seconds(6), bgRect, from, to);
                    ft.setDelay(Duration.seconds(i*6));
                    ft.play();
                }
            }
        } catch (Exception ignored) {}

        if (userImage != null) {
            userImage.setOnMouseClicked(e -> toggleUserMenu());
        }
    }

    private void swapButtons(int idx1, int idx2) {
        if (buttonCardsBox == null) return;
        javafx.scene.Node btn1 = buttonCardsBox.getChildren().get(idx1);
        javafx.scene.Node btn2 = buttonCardsBox.getChildren().get(idx2);
        
        TranslateTransition tt1 = new TranslateTransition(Duration.millis(300), btn1);
        tt1.setByX(btn2.getLayoutBounds().getWidth() + 16);
        
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(300), btn2);
        tt2.setByX(-(btn1.getLayoutBounds().getWidth() + 16));
        
        tt1.play();
        tt2.play();
        
        tt1.setOnFinished(e -> {
            buttonCardsBox.getChildren().remove(btn1);
            buttonCardsBox.getChildren().add(idx2, btn1);
            btn1.setTranslateX(0);
            btn2.setTranslateX(0);
        });
    }

    public void setUser(String id, String name) {
        this.soulId = id == null ? "" : id;
        if (userLabel != null) userLabel.setText(this.soulId);
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + this.soulId + ".jpg");
            if (u == null) u = getClass().getResource("/assets/icons/user.png");
            if (u != null && userImage != null) {
                Image img = new Image(u.toExternalForm(), 38, 38, true, true);
                userImage.setImage(img);
            }
        } catch (Exception ignored) {}
    }

    public void setLogo() {
        try {
            URL u = getClass().getResource("/assets/images/logo_new.png");
            if (u != null && logoImage != null) {
                Image img = new Image(u.toExternalForm(), 0, 0, true, true);
                logoImage.setImage(img);
                logoImage.setFitWidth(100);
                logoImage.setFitHeight(100);
                logoImage.setSmooth(true);
                logoImage.setCache(true);
            }
        } catch (Exception ignored) {}
    }

    private void toggleUserMenu() {
        if (userDropdown == null) return;
        if (userDropdown.isVisible()) {
            userDropdown.setVisible(false);
            userDropdown.setManaged(false);
            userDropdown.getChildren().clear();
            return;
        }
        userDropdown.setVisible(true);
        userDropdown.setManaged(true);
        userDropdown.getChildren().clear();
        String[] items = {"My Profile", "Starred Journals", "Log Out"};
        for (int i = 0; i < items.length; i++) {
            Button b = new Button(items[i]);
            b.getStyleClass().add("dropdown-item");
            userDropdown.getChildren().add(b);
            final int idx = i;
            b.setOnAction(e -> handleDropdownSelection(items[idx]));
        }
        userDropdown.getStyleClass().add("dropdown-container");
    }

    private void handleDropdownSelection(String which) {
        switch (which) {
            case "My Profile" -> openProfile();
            case "Starred Journals" -> showStarredPlaceholder();
            case "Log Out" -> onLogout();
        }
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/profile.fxml"));
            Parent profileRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ProfileController pc) {
                pc.setSoulId(this.soulId);
                pc.onShown();
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(profileRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showStarredPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Starred journals feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }

    private void onLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Log out");
        a.setHeaderText("Confirm log out");
        a.setContentText("Do you want to log out?");
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/login_signup.fxml"));
                Parent loginRoot = loader.load();
                Object loginController = loader.getController();
                if (loginController != null) {
                    try {
                        Method m = loginController.getClass().getMethod("setRepository", SoulRepository.class);
                        m.invoke(loginController, new SoulRepository());
                    } catch (NoSuchMethodException ignored) {}
                }
                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(loginRoot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openPrivateJournals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/private_journals_view.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller instanceof PrivateJournalsController pc) pc.setSoulId(this.soulId);
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openPublicJournals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/public_journals_view.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method m = controller.getClass().getMethod("setSoulId", String.class);
                    m.invoke(controller, this.soulId == null ? "" : this.soulId);
                } catch (NoSuchMethodException ignored) {}
            }
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openBlogs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/blog.fxml"));
            Parent p = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    Method m = controller.getClass().getMethod("setSoulId", String.class);
                    m.invoke(controller, this.soulId == null ? "" : this.soulId);
                } catch (NoSuchMethodException ignored) {}
            }
            if (root != null && root.getScene() != null) root.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showMoodPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Mood Tracker feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }

    private void showInsightsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Insights feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }

    private void showSettingsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Settings feature coming soon.", ButtonType.OK);
        a.setHeaderText("Coming Soon");
        a.showAndWait();
    }
}
