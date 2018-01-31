package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.model.Task;

public class NotificationController {

    private boolean completed = false;

    @FXML
    private Button btnPutAside;

    @FXML
    private Button btnComplete;

    @FXML
    private Label label;

    public boolean isCompleted() {
        return completed;
    }

    public void setLabel(Task task) {
        if (task == null){
            return;
        }
        label.setText(task.getName());
    }

    private void close(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }

    public void actionPutAside(ActionEvent actionEvent) {
        completed = false;
        close(actionEvent);
    }

    public void actionComplete(ActionEvent actionEvent) {
        completed = true;
        close(actionEvent);
    }
}
