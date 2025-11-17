package com.the_pathfinders;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ToDoController {

    @FXML private VBox rootBox;
    @FXML private MenuButton taskMenu;
    @FXML private TextField customTaskField;
    @FXML private Button addCustomBtn;
    @FXML private TableView<ToDo> todoTable;
    @FXML private TableColumn<ToDo, Boolean> doneCol;
    @FXML private TableColumn<ToDo, String> taskCol;

    private final ObservableList<ToDo> items = FXCollections.observableArrayList();

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
        doneCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        doneCol.setEditable(true);

        taskCol.setCellValueFactory(c -> c.getValue().taskProperty());
        taskCol.setCellFactory(col -> {
            TableCell<ToDo, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            return cell;
        });

        todoTable.setItems(items);
        todoTable.setEditable(true);

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
    }

    private void addCustom() {
        String txt = customTaskField.getText();
        if (txt != null && !txt.trim().isEmpty()) {
            addToDo(txt.trim());
            customTaskField.clear();
        }
    }

    private void addToDo(String taskText) {
        items.add(new ToDo(false, taskText));
    }
}