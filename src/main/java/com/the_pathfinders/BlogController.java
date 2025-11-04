package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.*;

public class BlogController {

    @FXML private VBox root;
    @FXML private FlowPane categoriesPane;
    @FXML private ListView<Blog> postsListView;
    @FXML private Button backBtn;
    @FXML private Label pageTitle;

    private String soulId = "";

    private final ObservableList<Blog> allPosts = FXCollections.observableArrayList();

    private final List<String> categories = Arrays.asList(
            "Depression", "Anxiety Disorders", "Bipolar Disorder", "Schizophrenia",
            "Obsessive-Compulsive Disorder", "Post-Traumatic Stress Disorder",
            "Eating Disorder", "Personality Disorder", "Neurodevelopmental Disorder",
            "ADHD", "OCD", "Social Anxiety Disorder"
    );

    private final List<String> pastelColors = Arrays.asList(
            "#FDE8E8","#EAF7F3","#F9F0EA","#F2E7FF","#F0F8FF",
            "#FFF4E6","#F7F6F8","#E8F6FF","#FFF1F6","#F8FFF4"
    );

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
    }

    @FXML
    public void initialize() {
        if (pageTitle != null) pageTitle.setText("Find Your Calm 🌿");

        int i = 1;
        for (String cat : categories) {
            allPosts.add(new Blog("b" + i, cat + " — Understanding", "A short introduction to " + cat + ".", cat));
            i++;
        }

        buildCategoryBoxes();

        postsListView.setItems(FXCollections.observableArrayList(allPosts));
        postsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Blog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-weight: bold;");
                    Label category = new Label(item.getCategory());
                    category.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

                    Button star = new Button(item.isSavedForLater() ? "★" : "☆");
                    star.getStyleClass().add("star-button");
                    if (item.isSavedForLater()) star.getStyleClass().add("saved");
                    Tooltip.install(star, new Tooltip(item.isSavedForLater() ? "Saved" : "Save for later"));

                    star.setOnAction(evt -> {
                        item.setSavedForLater(!item.isSavedForLater());
                        star.setText(item.isSavedForLater() ? "★" : "☆");
                        if (item.isSavedForLater()) star.getStyleClass().add("saved");
                        else star.getStyleClass().remove("saved");
                    });

                    HBox row = new HBox(10, title, category, star);
                    row.setStyle("-fx-alignment: CENTER_LEFT;");
                    setGraphic(row);
                }
            }
        });

        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());
    }

    // ✅ FIXED METHOD — correctly contains the box click behavior
    private void buildCategoryBoxes() {
        categoriesPane.getChildren().clear();

        for (int idx = 0; idx < categories.size(); idx++) {
            String cat = categories.get(idx);
            String color = pastelColors.get(idx % pastelColors.size());

            Button box = new Button(cat);
            box.getStyleClass().add("category-box");
            box.setStyle("-fx-background-color: " + color + ";");

            // Smooth hover
            box.setOnMouseEntered(e -> box.setScaleX(1.03));
            box.setOnMouseExited(e -> box.setScaleX(1.0));

            // ✅ on click: filter + popup
            box.setOnAction(e -> {
                filterByCategory(cat);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Coming Soon 💫");
                alert.setHeaderText(cat);
                alert.setContentText("Blogs for \"" + cat + "\" are on their way!\nStay tuned 🌸");
                alert.showAndWait();
            });

            categoriesPane.getChildren().add(box);
        }
    }

    private void filterByCategory(String category) {
        List<Blog> filtered = new ArrayList<>();
        for (Blog b : allPosts)
            if (category.equals(b.getCategory())) filtered.add(b);

        postsListView.setOpacity(0);
        postsListView.setItems(FXCollections.observableArrayList(filtered));

        FadeTransition fade = new FadeTransition(Duration.millis(400), postsListView);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), postsListView);
        slide.setFromX(30);
        slide.setToX(0);

        fade.play();
        slide.play();
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(this.soulId, "");
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
