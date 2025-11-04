package com.the_pathfinders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
            "Depression",
            "Anxiety Disorders",
            "Bipolar Disorder",
            "Schizophrenia",
            "Obsessive-Compulsive Disorder",
            "Post-Traumatic Stress Disorder",
            "Eating Disorders",
            "Personality Disorders",
            "Neurodevelopmental Disorders",
            "Substance-Related Disorders"
    );

    // Soft pastel colors (minimal)
    private final List<String> pastelColors = Arrays.asList(
            "#FDE8E8","#EAF7F3","#F9F0EA","#F2E7FF","#F0F8FF",
            "#FFF4E6","#F7F6F8","#E8F6FF","#FFF1F6","#F8FFF4"
    );

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
    }

    @FXML
    public void initialize() {
        // set minimal page title
        if (pageTitle != null) pageTitle.setText("Blog — Mental Health Categories");

        // populate a few sample posts
        int i = 1;
        for (String cat : categories) {
            allPosts.add(new Blog("b" + i, cat + " — understanding", "A short introduction to " + cat + ".", cat));
            i++;
        }

        // build category boxes
        for (int idx = 0; idx < categories.size(); idx++) {
            String cat = categories.get(idx);
            String color = pastelColors.get(idx % pastelColors.size());

            Button box = new Button(cat);
            box.getStyleClass().add("category-box");
            box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 12 8 12; -fx-cursor: hand;");
            box.setOnAction(e -> filterByCategory(cat));
            categoriesPane.getChildren().add(box);
        }

        postsListView.setItems(FXCollections.observableArrayList(allPosts));
        postsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Blog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-weight: bold; -fx-padding: 0 8 0 0;");
                    Label category = new Label(item.getCategory());
                    category.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
                    Button save = new Button(item.isSavedForLater() ? "Saved" : "Save for later");
                    save.setOnAction(evt -> {
                        item.setSavedForLater(!item.isSavedForLater());
                        save.setText(item.isSavedForLater() ? "Saved" : "Save for later");
                    });

                    HBox h = new HBox(10, title, category, save);
                    setGraphic(h);
                }
            }
        });

        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());
    }

    private void filterByCategory(String category) {
        if (category == null || category.isBlank()) {
            postsListView.setItems(FXCollections.observableArrayList(allPosts));
            return;
        }
        List<Blog> filtered = new ArrayList<>();
        for (Blog b : allPosts) if (category.equals(b.getCategory())) filtered.add(b);
        postsListView.setItems(FXCollections.observableArrayList(filtered));
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
 
