package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
    @FXML private javafx.scene.control.TextField searchBar;
    @FXML private FlowPane categoriesPane;
    @FXML private ListView<Blog> postsListView;
    @FXML private Button backBtn;
    @FXML private Label pageTitle;

    private String soulId = "";
    private SavedBlogsManager savedBlogsManager;

    private final ObservableList<Blog> allPosts = FXCollections.observableArrayList();

        private final List<String> categories = Arrays.asList(
            "Depression", "Anxiety Disorders", "Bipolar Disorder", "Schizophrenia",
            "Obsessive-Compulsive Disorder", "Post-Traumatic Stress Disorder",
            "Eating Disorder", "Personality Disorder", "Neurodevelopmental Disorder",
            "ADHD", "Panic Disorder", "Social Anxiety Disorder"
        );

    private final List<String> pastelColors = Arrays.asList(
            "#FDE8E8","#EAF7F3","#F9F0EA","#F2E7FF","#F0F8FF",
            "#FFF4E6","#F7F6F8","#E8F6FF","#FFF1F6","#F8FFF4"
    );

    public void setSoulId(String id) {
        this.soulId = id == null ? "" : id;
        // If posts already loaded, update saved flags from storage
        if (savedBlogsManager != null && !allPosts.isEmpty()) {
            for (Blog b : allPosts) {
                b.setSavedForLater(savedBlogsManager.isBlogSaved(this.soulId, b.getId()));
            }
            if (postsListView != null) postsListView.refresh();
        }
    }

    @FXML
    public void initialize() {
        if (pageTitle != null) pageTitle.setText("Find Your Calm 🌿");

        savedBlogsManager = new SavedBlogsManager();

        int i = 1;
        for (String cat : categories) {
            Blog blog = new Blog("b" + i, cat + " — Understanding", "A short introduction to " + cat + ".", cat);
            // Set full description (will be replaced with actual content later)
            blog.setFullDescription("A comprehensive guide to understanding " + cat + ". This section provides detailed information about symptoms, causes, treatments, and coping strategies.");
            // Load saved status from file
            blog.setSavedForLater(savedBlogsManager.isBlogSaved(soulId, blog.getId()));
            allPosts.add(blog);
            i++;
        }

        buildCategoryBoxes();

        // Setup search suggestions
        if (searchBar != null) setupSearchSuggestions();

        postsListView.setItems(FXCollections.observableArrayList(allPosts));
        postsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Blog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
                    Label category = new Label(item.getCategory());
                    category.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

                    Button star = new Button(item.isSavedForLater() ? "★" : "☆");
                    star.getStyleClass().add("star-button");
                    if (item.isSavedForLater()) star.getStyleClass().add("saved");
                    Tooltip.install(star, new Tooltip(item.isSavedForLater() ? "Saved" : "Save for later"));

                    star.setOnAction(evt -> {
                        item.setSavedForLater(!item.isSavedForLater());
                        star.setText(item.isSavedForLater() ? "★" : "☆");
                        if (item.isSavedForLater()) {
                            star.getStyleClass().add("saved");
                            savedBlogsManager.saveBlog(soulId, item);
                        } else {
                            star.getStyleClass().remove("saved");
                            savedBlogsManager.removeSavedBlog(soulId, item.getId());
                        }
                    });

                    // Click title or row to show details
                    title.setOnMouseClicked(e -> showBlogDetail(item));

                    HBox row = new HBox(10, title, category, star);
                    row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    setGraphic(row);
                }
            }
        });

        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());
    }

    private void buildCategoryBoxes() {
        categoriesPane.getChildren().clear();

        for (int idx = 0; idx < categories.size(); idx++) {
            String cat = categories.get(idx);
            String color = pastelColors.get(idx % pastelColors.size());

            Button box = new Button(cat);
            box.getStyleClass().add("category-box");
            box.setStyle("-fx-background-color: " + color + ";");

            // Smooth hover with ScaleTransition
            box.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), box);
                st.setToX(1.03);
                st.setToY(1.03);
                st.play();
            });
            box.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), box);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            // ✅ on click: filter + show first blog as popup
            box.setOnAction(e -> {
                List<Blog> filtered = filterByCategory(cat);
                if (!filtered.isEmpty()) {
                    showBlogDetail(filtered.get(0));
                }
            });

            categoriesPane.getChildren().add(box);
        }
    }

    private List<Blog> filterByCategory(String category) {
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

        return filtered;
    }

    // Search bar: show live suggestions and perform search on selection/enter
    private void setupSearchSuggestions() {
        javafx.scene.control.ContextMenu suggestions = new javafx.scene.control.ContextMenu();

        searchBar.textProperty().addListener((obs, oldV, newV) -> {
            suggestions.getItems().clear();
            String q = newV == null ? "" : newV.trim().toLowerCase();
            if (q.isEmpty()) {
                suggestions.hide();
                return;
            }

            Set<String> added = new LinkedHashSet<>();

            // Suggest from categories
            for (String c : categories) {
                if (c.toLowerCase().contains(q)) added.add(c);
                if (added.size() >= 7) break;
            }

            // Suggest from blog titles
            for (Blog b : allPosts) {
                if (added.size() >= 7) break;
                String t = b.getTitle();
                if (t != null && t.toLowerCase().contains(q)) added.add(t);
            }

            // Show suggestions
            for (String s : added) {
                javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(s);
                mi.setOnAction(e -> {
                    searchBar.setText(s);
                    performSearch(s);
                    suggestions.hide();
                });
                suggestions.getItems().add(mi);
            }

            if (!suggestions.getItems().isEmpty()) {
                suggestions.show(searchBar, javafx.geometry.Side.BOTTOM, 0, 0);
            } else {
                suggestions.hide();
            }
        });

        // Enter key performs search
        searchBar.setOnAction(e -> performSearch(searchBar.getText()));
    }

    private void performSearch(String query) {
        if (query == null) query = "";
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) {
            postsListView.setItems(FXCollections.observableArrayList(allPosts));
            return;
        }

        List<Blog> results = new ArrayList<>();
        for (Blog b : allPosts) {
            if ((b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) ||
                (b.getCategory() != null && b.getCategory().toLowerCase().contains(q)) ||
                (b.getContent() != null && b.getContent().toLowerCase().contains(q))) {
                results.add(b);
            }
        }

        postsListView.setItems(FXCollections.observableArrayList(results));
        // play animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), postsListView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Show blog details in a smooth popup modal
     */
    private void showBlogDetail(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/blog_detail.fxml"));
            Parent detail = loader.load();
            BlogDetailController controller = loader.getController();
            controller.setBlog(blog);

            // Create popup container
            javafx.scene.layout.StackPane popup = new javafx.scene.layout.StackPane();
            popup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            // Put detail directly into the StackPane and center it
            popup.getChildren().add(detail);
            javafx.geometry.Pos pos = javafx.geometry.Pos.CENTER;
            javafx.scene.layout.StackPane.setAlignment(detail, pos);

            // Limit size so large texts fit and are scrollable inside detail
            if (detail instanceof javafx.scene.layout.Region && root != null) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) detail;
                region.maxWidthProperty().bind(root.widthProperty().multiply(0.85));
                region.maxHeightProperty().bind(root.heightProperty().multiply(0.8));
            }

            // Add popup to root
            if (root != null) {
                root.getChildren().add(popup);

                // Fade in animation (apply to detail view)
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), detail);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), detail);
                scaleIn.setFromX(0.95);
                scaleIn.setFromY(0.95);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);

                fadeIn.play();
                scaleIn.play();
            }

            // Close handler
            controller.setOnClose(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), detail);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    if (root != null && root.getChildren().contains(popup)) {
                        root.getChildren().remove(popup);
                    }
                });
                fadeOut.play();
            });
            // When save toggled inside detail popup, persist and refresh UI
            controller.setOnSave(() -> {
                if (blog.isSavedForLater()) {
                    savedBlogsManager.saveBlog(soulId, blog);
                } else {
                    savedBlogsManager.removeSavedBlog(soulId, blog.getId());
                }
                if (postsListView != null) postsListView.refresh();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
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
