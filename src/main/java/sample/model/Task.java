package sample.model;

import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {

    private String name = "";
    private String time = "";
    private static int id = 0;
    private int taskId;

    public int getId() {
        return taskId;
    }

    public void setId(int id) {
        this.taskId = id;
    }

    public Task() {
        id++;
        taskId = id;
    }

    public Task(String name, String time) {
        this();
        //this.name = new SimpleStringProperty(name);
        setName(name);
        setTime(time);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        if (time.matches("[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-9]|3[01]) ([0-1]\\d|2[0-3])(:[0-5]\\d)$")){
            time = time.replace(" ", "T") + "+04:00";
        } else if (time.matches("[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-9]|3[01]) ([0-1]\\d|2[0-3])(:[0-5]\\d){2}$")) {
            time = time.replace(" ", "T") + "+04:00";
        } else if (time.matches("^([0-1]\\d|2[0-3])(:[0-5]\\d)$"))
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            time = ZonedDateTime.now(ZoneId.systemDefault()).format(formatter) + "T" + time + "+04:00";
        } else if (time.matches("^([0-1]\\d|2[0-3])(:[0-5]\\d){2}$")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            time = ZonedDateTime.now(ZoneId.systemDefault()).format(formatter) + "T" + time + "+04:00";
        }
        this.time = time;
    }

    public SimpleStringProperty PropertyName() {
        return new SimpleStringProperty(name);
    }

    public SimpleStringProperty PropertyTime() {
        return new SimpleStringProperty(time);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", time='" + time + '\'' +
                '}' +
                "taskId=" + taskId;
    }
}
