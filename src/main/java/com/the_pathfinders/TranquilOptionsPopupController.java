package com.the_pathfinders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class TranquilOptionsPopupController {

    @FXML private Button meditationBtn;
    @FXML private Button calmActivitiesBtn;
    @FXML private Button pomodoroBtn;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {

        meditationBtn.setOnAction(e -> load("/com/the_pathfinders/fxml/Meditation.fxml"));
        calmActivitiesBtn.setOnAction(e -> load("/com/the_pathfinders/fxml/CalmActivities.fxml"));
        pomodoroBtn.setOnAction(e -> load("/com/the_pathfinders/fxml/Pomodoro.fxml"));

        backBtn.setOnAction(e -> load("/com/the_pathfinders/fxml/Dashboard.fxml"));
    }

    private void load(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent p = loader.load();
            backBtn.getScene().setRoot(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
