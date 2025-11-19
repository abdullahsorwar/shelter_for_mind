package com.the_pathfinders;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BlogDetailController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label contentLabel;
    @FXML private Button saveBtn;
    @FXML private Button closeBtn;
    @FXML private HBox starContainer;

    private Blog blog;
    private Runnable onClose;
    private Runnable onSave;

    public void setBlog(Blog blog) {
        this.blog = blog;
        if (titleLabel != null) titleLabel.setText(blog.getTitle());
        if (categoryLabel != null) categoryLabel.setText(blog.getCategory());
        if (contentLabel != null) contentLabel.setText(blog.getFullDescription() != null ? blog.getFullDescription() : blog.getContent());
        updateSaveButton();
    }

    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }

    public void setOnSave(Runnable callback) {
        this.onSave = callback;
    }

    @FXML
    public void initialize() {
        if (closeBtn != null) {
            closeBtn.setOnAction(e -> close());
        }
        if (saveBtn != null) {
            saveBtn.setOnAction(e -> toggleSave());
        }
    }

    private void toggleSave() {
        if (blog != null) {
            blog.setSavedForLater(!blog.isSavedForLater());
            updateSaveButton();
            if (onSave != null) onSave.run();
        }
    }

    private void updateSaveButton() {
        if (saveBtn != null && blog != null) {
            if (blog.isSavedForLater()) {
                saveBtn.setText("★ Saved");
                saveBtn.getStyleClass().add("saved");
            } else {
                saveBtn.setText("☆ Save for Later");
                saveBtn.getStyleClass().remove("saved");
            }
        }
    }

    private void close() {
        if (root != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                if (onClose != null) onClose.run();
            });
            fade.play();
        }
    }
}
