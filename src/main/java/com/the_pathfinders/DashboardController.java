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
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;

import com.the_pathfinders.db.SoulRepository;

public class DashboardController {

    @FXML private AnchorPane root;
    @FXML private Label userLabel;
    @FXML private ImageView userImage;
    @FXML private Button journalingBtn;
    @FXML private Button blogsBtn;
    @FXML private Button logoutBtn;

    private String soulId;

    public void initialize() {
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

        logoutBtn.setOnAction(e -> onLogout());
    }

    public void setUser(String id, String name) {
        this.soulId = id == null ? "" : id;
        if (userLabel != null) userLabel.setText(this.soulId);
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + this.soulId + ".jpg");
            if (u == null) u = getClass().getResource("/assets/icons/username.png");
            if (u != null && userImage != null) {
                Image img = new Image(u.toExternalForm(), 38, 38, true, true);
                userImage.setImage(img);
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
}
