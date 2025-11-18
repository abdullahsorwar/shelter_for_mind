package com.the_pathfinders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.ThreadLocalRandom;

public class ToDoController {

    @FXML private AnchorPane rootPane;
    @FXML private VBox rootBox;
    @FXML private MenuButton taskMenu;
    @FXML private TextField customTaskField;
    @FXML private Button addCustomBtn;
    @FXML private Button backBtn;
    @FXML private TableView<ToDo> todoTable;
    @FXML private TableColumn<ToDo, Boolean> doneCol;
    @FXML private TableColumn<ToDo, String> taskCol;

    private final ObservableList<ToDo> items = FXCollections.observableArrayList();
    private String soulId = "";
    private Stage completionPopup;

    @FXML
    public void initialize() {
        System.out.println("=== ToDoController initialize() called ===");

        // Populate menu with common tasks
        String[] defaults = {"Water a plant", "Breathe deeply", "Stretch", "Take a short walk", "Drink water", "Write 3 things you're grateful for"};
        for (String t : defaults) {
            MenuItem mi = new MenuItem(t);
            mi.setOnAction(e -> addToDo(t));
            taskMenu.getItems().add(mi);
        }
        System.out.println("Menu populated with " + defaults.length + " items");

        // Table setup
        doneCol.setCellValueFactory(c -> c.getValue().doneProperty());
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));
        doneCol.setEditable(true);

        taskCol.setCellValueFactory(c -> c.getValue().taskProperty());
        taskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });

        todoTable.setItems(items);
        todoTable.setEditable(true);
        attachCompletionListenerToExistingItems();
        items.addListener((javafx.collections.ListChangeListener<ToDo>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (ToDo t : change.getAddedSubList()) {
                        attachCompletionListener(t);
                    }
                }
            }
        });

        // Add custom via button or Enter
        addCustomBtn.setOnAction(e -> addCustom());
        customTaskField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addCustom();
        });

        // Optional: double-click to remove an item
        todoTable.setRowFactory(tv -> {
            TableRow<ToDo> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    ToDo t = row.getItem();
                    items.remove(t);
                }
            });
            return row;
        });

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToDashboard());
        }
    }

    private void addCustom() {
        String txt = customTaskField.getText();
        if (txt != null && !txt.trim().isEmpty()) {
            addToDo(txt.trim());
            customTaskField.clear();
        }
    }

    private void addToDo(String taskText) {
        ToDo todo = new ToDo(false, taskText);
        attachCompletionListener(todo);
        items.add(todo);
    }

    public void setSoulId(String soulId) {
        this.soulId = soulId == null ? "" : soulId;
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(this.soulId, "");
            if (rootPane != null && rootPane.getScene() != null) {
                rootPane.getScene().setRoot(dash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void attachCompletionListenerToExistingItems() {
        for (ToDo t : items) {
            attachCompletionListener(t);
        }
    }

    private void attachCompletionListener(ToDo todo) {
        todo.doneProperty().addListener((obs, wasDone, isNowDone) -> {
            if (!Boolean.TRUE.equals(wasDone) && Boolean.TRUE.equals(isNowDone)) {
                showCompletionPopup();
            }
        });
    }

    private void showCompletionPopup() {
        if (rootPane == null || rootPane.getScene() == null) return;
        if (completionPopup != null && completionPopup.isShowing()) return;

        String[] messages = {
                "Congratulations! Keep going",
                "There's nothing you cannot do! Keep going",
                "The goal is near! I can feel it!",
                "Keep it up, champ!"
        };
        String message = messages[ThreadLocalRandom.current().nextInt(messages.length)];

        BorderPane content = new BorderPane();
        content.setPrefSize(340, 180);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-radius: 16;");
        content.getStyleClass().add("root-pane");
        content.setBackground(new Background(
                new BackgroundFill(
                        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#f3e7ff")),
                                new Stop(1, Color.web("#e9d8ff"))),
                        new CornerRadii(16),
                        Insets.EMPTY)));

        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setAlignment(Pos.CENTER);
        lbl.setFont(Font.font("Playfair Display", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.web("#2f1b4c"));
        BorderPane.setAlignment(lbl, Pos.CENTER);
        content.setCenter(lbl);

        Scene scene = new Scene(content);
        try {
            var cssUrl = getClass().getResource("/com/the_pathfinders/css/ToDo.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {}

        completionPopup = new Stage();
        completionPopup.initOwner(rootPane.getScene().getWindow());
        completionPopup.initModality(Modality.WINDOW_MODAL);
        completionPopup.setTitle("Great job!");
        completionPopup.setResizable(false);
        completionPopup.setScene(scene);
        completionPopup.setOnHidden(ev -> completionPopup = null);
        completionPopup.centerOnScreen();
        completionPopup.show();
    }
}
