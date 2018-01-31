package sample.service;

import sample.model.Task;

public interface TaskList {

    void add(Task task);

    void update(Task task);

    void delete(Task task);
}
