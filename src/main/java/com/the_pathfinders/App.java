package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import com.the_pathfinders.db.SoulRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // --- Initialize Neon connection (your exact JDBC URL) ---
        final String JDBC_URL =
            "jdbc:postgresql://ep-bold-rice-a1c5g7lk-pooler.ap-southeast-1.aws.neon.tech/neondb"
          + "?user=neondb_owner&password=npg_Qg23VZhTbANS&sslmode=require&channelBinding=require";

        DB.init(JDBC_URL);
        DbMigrations.runAll(); // create table if missing

        // --- Load UI ---
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login_signup.fxml"));
        Parent root = loader.load();

        // Give controller a repository (DB-backed)
        LoginSignupController controller = loader.getController();
        controller.setRepository(new SoulRepository());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Shelter of Mind");
        stage.setMinWidth(800);
        stage.setMinHeight(480);
        stage.show();

        stage.setOnCloseRequest(e -> DB.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}