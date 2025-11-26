package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import java.awt.Desktop;
import java.net.URI;

public class CalmActivitiesController {

    @FXML private Button backBtn;
    @FXML private Button drawingBtn;
    @FXML private Button breathingBallBtn;
    @FXML private Button bubblePopperBtn;
    @FXML private Button typingGameBtn;
    @FXML private Button galaxyStargazingBtn;

    @FXML
    public void initialize() {
        setupBackButton();
        setupActivityButtons();
    }

    private void setupBackButton() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        try {
            Parent dashboard = FXMLLoader.load(
                getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml")
            );
            backBtn.getScene().setRoot(dashboard);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupActivityButtons() {
        drawingBtn.setOnAction(e -> openDrawingTool());
        breathingBallBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/BreathingBall.fxml"));
        bubblePopperBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/BubblePopper.fxml"));
        typingGameBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/TypingGame.fxml"));
        galaxyStargazingBtn.setOnAction(e -> loadActivity("/com/the_pathfinders/fxml/GalaxyStargazing.fxml"));
    }

    private void openDrawingTool() {
        try {
            // Opens an online adult doodle/coloring tool
            String url = "https://www.youidraw.com/apps/painter/";
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("Desktop is not supported on this platform");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadActivity(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent p = loader.load();
            backBtn.getScene().setRoot(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
