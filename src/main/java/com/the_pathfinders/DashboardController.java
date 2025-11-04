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
        // Minimal wiring: buttons can be extended later
        journalingBtn.setOnAction(e -> {
            // placeholder: could open journaling view
            System.out.println("Journaling clicked");
        });
        blogsBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/blog.fxml"));
                Parent blogRoot = loader.load();
                BlogController blogController = loader.getController();
                // pass current user id if available
                blogController.setSoulId(this.soulId == null ? "" : this.soulId);
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
        // try to load a username.jpg from resources (fallback to nothing)
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + this.soulId + ".jpg");
            if (u == null) u = getClass().getResource("/com/the_pathfinders/username.png");
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/login_signup.fxml"));
                Parent loginRoot = loader.load();
                LoginSignupController loginController = loader.getController();
                loginController.setRepository(new SoulRepository()); // Pass a fresh repository instance
                if (root != null && root.getScene() != null) {
                    root.getScene().setRoot(loginRoot);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

