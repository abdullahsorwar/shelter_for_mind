package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.util.*;

public class GratitudeGardenController {

    private static String soulId = "";
    
    public static void setSoulId(String id) {
        soulId = id;
    }

    @FXML private Button backBtn;
    @FXML private Pane gardenPane;
    @FXML private TextField gratitudeInput;
    @FXML private Button plantBtn;
    @FXML private Button viewAllBtn;
    @FXML private Button clearGardenBtn;
    @FXML private Label flowerCountLabel;
    @FXML private VBox emptyGardenMessage;
    @FXML private StackPane gratitudeListOverlay;
    @FXML private VBox gratitudeListBox;
    @FXML private Button closeListBtn;

    private List<GratitudeFlower> flowers = new ArrayList<>();
    private Random random = new Random();

    private static class GratitudeFlower {
        String message;
        StackPane flowerNode;
        Label messageLabel;
        
        GratitudeFlower(String message, StackPane node, Label label) {
            this.message = message;
            this.flowerNode = node;
            this.messageLabel = label;
        }
    }

    @FXML
    public void initialize() {
        setupBackButton();
        setupControls();
        updateFlowerCount();
        emptyGardenMessage.setVisible(true);
    }

    private void setupBackButton() {
        backBtn.setOnAction(e -> goBack());
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/the_pathfinders/fxml/CalmActivities.fxml")
            );
            Parent calmActivities = loader.load();
            if (soulId != null && !soulId.isEmpty()) {
                CalmActivitiesController.setSoulId(soulId);
            }
            backBtn.getScene().setRoot(calmActivities);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupControls() {
        // Plant button action
        plantBtn.setOnAction(e -> plantFlower());
        
        // Enter key in text field
        gratitudeInput.setOnAction(e -> plantFlower());
        
        // View all gratitudes
        viewAllBtn.setOnAction(e -> showGratitudeList());
        
        // Close list overlay
        closeListBtn.setOnAction(e -> hideGratitudeList());
        gratitudeListOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == gratitudeListOverlay) {
                hideGratitudeList();
            }
        });
        
        // Clear garden
        clearGardenBtn.setOnAction(e -> clearGarden());
    }

    private void plantFlower() {
        String gratitude = gratitudeInput.getText().trim();
        
        if (gratitude.isEmpty()) {
            shakeNode(gratitudeInput);
            return;
        }
        
        if (gratitude.length() < 3) {
            showTemporaryMessage("Please write a bit more 💭");
            return;
        }
        
        // Hide empty message if first flower
        if (flowers.isEmpty()) {
            emptyGardenMessage.setVisible(false);
        }
        
        // Create flower
        createFlower(gratitude);
        
        // Clear input
        gratitudeInput.clear();
        
        // Update count
        updateFlowerCount();
        
        // Show encouraging message
        String[] messages = {
            "Beautiful! 🌸",
            "Keep going! 🌺",
            "Lovely! 🌷",
            "Wonderful! 🌼",
            "Amazing! 🌻"
        };
        showTemporaryMessage(messages[random.nextInt(messages.length)]);
    }

    private void createFlower(String gratitude) {
        // Random position in garden
        double x = 60 + random.nextDouble() * (gardenPane.getWidth() - 120);
        double y = 40 + random.nextDouble() * (gardenPane.getHeight() - 100);
        
        // Create flower with stem and petals
        StackPane flowerStack = new StackPane();
        flowerStack.setLayoutX(x);
        flowerStack.setLayoutY(y);
        
        // Stem
        Pane stem = new Pane();
        stem.setPrefSize(4, 40);
        stem.setStyle("-fx-background-color: linear-gradient(to bottom, #90C695, #A8D8B9);");
        stem.setTranslateY(15);
        
        // Flower center
        Circle center = new Circle(12);
        center.setFill(Color.rgb(255, 220, 100));
        
        // Petals (5 petals)
        Pane petalsPane = new Pane();
        Color[] petalColors = {
            Color.rgb(255, 182, 193), // Light pink
            Color.rgb(255, 160, 200), // Pink
            Color.rgb(230, 190, 255), // Lavender
            Color.rgb(200, 220, 255), // Light blue
            Color.rgb(255, 200, 150), // Peach
            Color.rgb(255, 240, 200)  // Light yellow
        };
        
        Color petalColor = petalColors[random.nextInt(petalColors.length)];
        
        for (int i = 0; i < 5; i++) {
            Circle petal = new Circle(10);
            petal.setFill(petalColor);
            double angle = i * 72 * Math.PI / 180; // 360/5 = 72 degrees
            double petalX = Math.cos(angle) * 15;
            double petalY = Math.sin(angle) * 15;
            petal.setTranslateX(petalX);
            petal.setTranslateY(petalY);
            petalsPane.getChildren().add(petal);
        }
        
        // Message label (hidden by default)
        Label messageLabel = new Label(gratitude);
        messageLabel.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-padding: 8 12;" +
            "-fx-background-radius: 15;" +
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #8B7BA8;" +
            "-fx-font-style: italic;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        );
        messageLabel.setMaxWidth(200);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setVisible(false);
        messageLabel.setTranslateY(-50);
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(8);
        flowerStack.setEffect(shadow);
        
        // Assemble flower
        flowerStack.getChildren().addAll(stem, petalsPane, center, messageLabel);
        
        // Hover to show message
        flowerStack.setOnMouseEntered(e -> {
            messageLabel.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(200), messageLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            
            ScaleTransition st = new ScaleTransition(Duration.millis(200), flowerStack);
            st.setToX(1.2);
            st.setToY(1.2);
            st.play();
        });
        
        flowerStack.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), messageLabel);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(ev -> messageLabel.setVisible(false));
            ft.play();
            
            ScaleTransition st = new ScaleTransition(Duration.millis(200), flowerStack);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        // Store flower
        GratitudeFlower flower = new GratitudeFlower(gratitude, flowerStack, messageLabel);
        flowers.add(flower);
        
        // Animate flower growing
        flowerStack.setScaleX(0);
        flowerStack.setScaleY(0);
        gardenPane.getChildren().add(flowerStack);
        
        ScaleTransition grow = new ScaleTransition(Duration.millis(800), flowerStack);
        grow.setToX(1.0);
        grow.setToY(1.0);
        grow.setInterpolator(Interpolator.EASE_OUT);
        grow.play();
        
        // Gentle swaying animation
        Timeline sway = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(flowerStack.rotateProperty(), -3)
            ),
            new KeyFrame(Duration.seconds(2 + random.nextDouble()),
                new KeyValue(flowerStack.rotateProperty(), 3)
            )
        );
        sway.setAutoReverse(true);
        sway.setCycleCount(Animation.INDEFINITE);
        sway.play();
    }

    private void updateFlowerCount() {
        flowerCountLabel.setText("🌸 " + flowers.size() + " Flower" + (flowers.size() == 1 ? "" : "s"));
    }

    private void showGratitudeList() {
        gratitudeListBox.getChildren().clear();
        
        if (flowers.isEmpty()) {
            Label empty = new Label("Your garden is empty.\nPlant some flowers first! 🌱");
            empty.setStyle("-fx-text-fill: #A89BC5; -fx-font-size: 16px; -fx-font-style: italic;");
            empty.setAlignment(Pos.CENTER);
            gratitudeListBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < flowers.size(); i++) {
                GratitudeFlower flower = flowers.get(i);
                
                HBox item = new HBox(12);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle(
                    "-fx-background-color: linear-gradient(to right, #FFF5F8, #FFFBFC);" +
                    "-fx-padding: 12 16;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(139,123,168,0.2), 6, 0, 0, 2);"
                );
                
                Label number = new Label((i + 1) + ".");
                number.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF8FB1; -fx-font-size: 14px;");
                
                Label message = new Label(flower.message);
                message.setWrapText(true);
                message.setStyle("-fx-text-fill: #8B7BA8; -fx-font-size: 14px;");
                HBox.setHgrow(message, Priority.ALWAYS);
                
                item.getChildren().addAll(number, message);
                gratitudeListBox.getChildren().add(item);
            }
        }
        
        gratitudeListOverlay.setVisible(true);
        gratitudeListOverlay.setManaged(true);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), gratitudeListOverlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideGratitudeList() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), gratitudeListOverlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            gratitudeListOverlay.setVisible(false);
            gratitudeListOverlay.setManaged(false);
        });
        ft.play();
    }

    private void clearGarden() {
        if (flowers.isEmpty()) {
            showTemporaryMessage("Garden is already empty! 🌱");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Garden");
        alert.setHeaderText("Start fresh with a new garden?");
        alert.setContentText("This will remove all your flowers. Are you sure?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Animate flowers disappearing
                for (GratitudeFlower flower : flowers) {
                    FadeTransition ft = new FadeTransition(Duration.millis(500), flower.flowerNode);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> gardenPane.getChildren().remove(flower.flowerNode));
                    ft.play();
                }
                
                flowers.clear();
                updateFlowerCount();
                emptyGardenMessage.setVisible(true);
                showTemporaryMessage("Garden cleared 🌿");
            }
        });
    }

    private void showTemporaryMessage(String message) {
        Label tempLabel = new Label(message);
        tempLabel.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-padding: 12 20;" +
            "-fx-background-radius: 20;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8B7BA8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );
        
        StackPane.setAlignment(tempLabel, Pos.TOP_CENTER);
        StackPane.setMargin(tempLabel, new javafx.geometry.Insets(120, 0, 0, 0));
        
        StackPane parent = (StackPane) gardenPane.getParent().getParent();
        parent.getChildren().add(tempLabel);
        
        FadeTransition ft = new FadeTransition(Duration.millis(2000), tempLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setDelay(Duration.seconds(1));
        ft.setOnFinished(e -> parent.getChildren().remove(tempLabel));
        ft.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }
}
