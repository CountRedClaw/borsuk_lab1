package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.model.Task;

public class EditDialogController {

    private boolean changed = false;

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtTime;

    public boolean isChanged() {
        return changed;
    }

    private Task task;

    public void setTask(Task task) {
        changed = false;
        if (task == null){
            return;
        }
        this.task = task;
        txtName.setText(task.getName());
        txtTime.setText(task.getTime());
    }

    public Task getTask() {
        return task;
    }

    public void actionClose(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }

    public void actionSave(ActionEvent actionEvent) {
        changed = true;
        task.setName(txtName.getText());
        task.setTime(txtTime.getText());
        actionClose(actionEvent);
    }
}
