module com.the_pathfinders {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.the_pathfinders to javafx.fxml;
    exports com.the_pathfinders;
}
