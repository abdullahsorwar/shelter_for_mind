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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;

import com.the_pathfinders.db.SoulRepository;

public class DashboardController {

    @FXML private AnchorPane root;
    @FXML private Label userLabel;
    @FXML private ImageView userImage;
    @FXML private ImageView logoImage;
    @FXML private Button journalingBtn;
    @FXML private Button blogsBtn;
    @FXML private javafx.scene.layout.VBox userDropdown;

    private String soulId;

    public void initialize() {
        // Initialize logo
        setLogo();
        
        // Minimal wiring: buttons can be extended later
        journalingBtn.setOnAction(e -> {
            System.out.println("Journal button clicked!");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
                Parent journalRoot = loader.load();
                Object controller = loader.getController();

                if (controller != null) {
                    try {
                        Method m = controller.getClass().getMethod("setSoulId", String.class);
                        m.invoke(controller, this.soulId == null ? "" : this.soulId);
                    } catch (NoSuchMethodException ignored) {
                        // Controller doesn’t accept soulId — that’s fine
                    }
                }

                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(journalRoot);
                }

                System.out.println("Journal page loaded!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        blogsBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/blog.fxml"));
                Parent blogRoot = loader.load();
                Object controller = loader.getController();
                if (controller != null) {
                    try {
                        Method m = controller.getClass().getMethod("setSoulId", String.class);
                        m.invoke(controller, this.soulId == null ? "" : this.soulId);
                    } catch (NoSuchMethodException ignored) {
                        // Controller doesn’t accept soulId — that’s fine
                    }
                }
                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(blogRoot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Remove old logout button usage; now handled in dropdown
        if (userImage != null) {
            userImage.setOnMouseClicked(e -> toggleUserMenu());
        }
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

    public void setLogo()
    {
        try {
            URL u = getClass().getResource("/assets/images/logo_new.png");
            if (u != null && logoImage != null) {
                // Load image at higher quality - don't force size in Image constructor
                Image img = new Image(u.toExternalForm(), 0, 0, true, true);
                logoImage.setImage(img);
                // Set the fitWidth/fitHeight on the ImageView instead
                logoImage.setFitWidth(200);
                logoImage.setFitHeight(200);
                logoImage.setTranslateX(-50);
                logoImage.setTranslateY(-50);
                // Enable better quality scaling
                logoImage.setSmooth(true);
                logoImage.setCache(true);
            }
        } catch (Exception ignored) {}
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
                    } catch (NoSuchMethodException ignored) {
                        // Controller doesn’t accept repository — that’s fine
                    }
                }
                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(loginRoot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
            b.setOpacity(0);
            b.setScaleX(0.9);
            b.setScaleY(0.9);
            userDropdown.getChildren().add(b);
            final int idx = i;
            javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(50 + idx*120), ev -> {
                    javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(250), b);
                    ft.setFromValue(0); ft.setToValue(1);
                    javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(250), b);
                    st.setFromX(0.9); st.setFromY(0.9); st.setToX(1); st.setToY(1);
                    ft.play(); st.play();
                })
            );
            tl.play();
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
}
