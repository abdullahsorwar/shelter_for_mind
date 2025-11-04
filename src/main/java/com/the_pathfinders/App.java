package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final String JDBC_URL =
            "jdbc:postgresql://ep-bold-rice-a1c5g7lk-pooler.ap-southeast-1.aws.neon.tech/neondb" +
            "?user=neondb_owner&password=npg_Qg23VZhTbANS&sslmode=require&channelBinding=require";

        DB.init(JDBC_URL);
        DbMigrations.runAll();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("initial.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Shelter of Mind");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.show();

        stage.setOnCloseRequest(e -> DB.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}