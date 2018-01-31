package sample.service;

import javax.xml.bind.annotation.XmlElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.model.Task;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tasks")
public class CollectionTaskList implements TaskList {

    @XmlElement(name = "task")
    public ObservableList<Task> taskList = FXCollections.observableArrayList();

    @Override
    public void add(Task task) {
        taskList.add(task);
    }

    @Override
    public void update(Task task) {
        // т.к. коллекция и является хранилищем - ничего обновлять не нужно
    }

    @Override
    public void delete(Task task) {
        taskList.remove(task);
    }

    public ObservableList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ObservableList<Task> taskList) {
        this.taskList = taskList;
    }

    /*public void fillTestData() {
        taskList.add(new Task("dfghj", "23:59"));
        taskList.add(new Task("svs", "2020-01-19 23:50"));
        taskList.add(new Task("svs", "2016-01-19 23:50"));
    }*/
}
