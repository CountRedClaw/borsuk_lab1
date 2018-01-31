package sample.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sample.Main;
import sample.model.Task;
import sample.service.CollectionTaskList;

import javax.management.Notification;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.Socket;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private CollectionTaskList taskListImpl = new CollectionTaskList();

    private Stage mainStage;

    @FXML
    private TableView tableTaskList;

    @FXML
    private TableColumn<Task, String> columnName;

    @FXML
    private TableColumn<Task, String> columnTime;

    @FXML
    private Parent fxmlEdit;
    private Parent notifFxmlEdit;
    private FXMLLoader fxmlLoader = new FXMLLoader();
    private FXMLLoader notifFxmlLoader = new FXMLLoader();
    private EditDialogController editDialogController;
    private NotificationController notificationController;
    private Stage editDialogStage;
    private Stage NotificationStage;

    private Timer timer = new Timer();

    private static Map taskList = new HashMap<Integer, TimerTask>();

    public static Socket socket;
    //public static ObjectInputStream in;
    //public static ObjectOutputStream out;

    private class Resender extends Thread {

        private boolean stoped;

        /**
         * Прекращает пересылку сообщений
         */
        public void setStop() {
            stoped = true;
        }

        /**
         * Считывает все сообщения от сервера и печатает их в консоль.
         * Останавливается вызовом метода setStop()
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при получении сообщения.");
                e.printStackTrace();
            }
        }
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    private void initialize() {
        //initializeStream();
        columnName.setCellValueFactory(new PropertyValueFactory<Task, String>("name"));
        columnTime.setCellValueFactory(new PropertyValueFactory<Task, String>("time"));
        //initListeners();
        initializeData();
        initLoader();



    }

    public static CollectionTaskList getCollection(String action, Task task) {

        CollectionTaskList collection = new CollectionTaskList();
        try(Socket socket = new Socket("localhost", 19000)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            HashMap<String, Task> map = new HashMap<>();
            map.put(action, task);
            //String str = "sfd";

            out.writeObject(map);
            //out.writeObject(str);
            out.flush();

            collection.getTaskList().addAll((List<Task>) in.readObject());

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return collection;
    }

    private void initializeData() {
        CollectionTaskList taskListImpl_temp = new CollectionTaskList();
        taskListImpl_temp = getCollection("load", new Task("Ilya", "23:59"));
        fillData(taskListImpl_temp);
        tableTaskList.setItems(taskListImpl.getTaskList());
    }

    private void fillData(CollectionTaskList taskListImpl_temp) {

        //taskListImpl_temp.fillTestData();

        taskListImpl.getTaskList().clear();
        Task task;
        for (int i = 0; i < taskListImpl_temp.getTaskList().size(); i++) {
                task = taskListImpl_temp.getTaskList().get(i);

                if (isActual(task)) {           // создание тасков, если время правильное
                    setTask(task);
                    taskListImpl.add(task);
                }
        }
    }

    private void initLoader() {
        try {
            fxmlLoader.setLocation(getClass().getResource("/edit.fxml"));
            notifFxmlLoader.setLocation(getClass().getResource("/notification.fxml"));
            fxmlEdit = fxmlLoader.load();
            notifFxmlEdit = notifFxmlLoader.load();
            editDialogController = fxmlLoader.getController();
            notificationController = notifFxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isActual(Task task) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(task.getTime());
            if (!Objects.equals(task.getName(), "") && ZonedDateTime.now().isBefore(zdt)){
                return true;
            } else throw new Exception();
        } catch(Exception e) {
            alert("Неверный ввод");
        }
        return false;
    }

    private void setTask(Task task) {
        if (!isActual(task)) {
            return;
        }

        ZonedDateTime zdt = ZonedDateTime.parse(task.getTime());
        final boolean[] completed = {false};
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                notificationController.setLabel(task);
                Platform.runLater(() -> {           // "костыль", чтобы из non-javafx потока изменить UI
                    showNotification();
                    if (notificationController.isCompleted()) {
                        completed[0] = true;
                        taskListImpl.delete(task);
                        taskList.remove(task.getId());
                    } else {
                        completed[0] = false;
                        Task task1 = new Task();
                        task1.setName(task.getName());
                        task1.setTime(ZonedDateTime.parse(task.getTime()).plusMinutes(1).toString());   // пересоздаём задачу если не была нажата кнопка "завершить"
                        taskListImpl.add(task1);                                                        // ставим на минуту позже
                        setTask(task1);                                                                 //
                        taskListImpl.delete(task);
                        ((TimerTask)taskList.get(task.getId())).cancel();
                        taskList.remove(task.getId());
                    }
                });
            }
        };
        if (!completed[0]) {
            taskList.put(task.getId(), timerTask);
            timer.schedule(timerTask, (zdt.toInstant().toEpochMilli() - ZonedDateTime.now(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        }
    }

    public void actionButtonPressed(ActionEvent actionEvent) {

        Object source = actionEvent.getSource();

        if (!(source instanceof Button)) {
            return;
        }

        Button clickedButton = (Button) source;

        switch (clickedButton.getId()) {
            case "btnAdd":
                //Main.sendData();
                //sendData();

                Task task = new Task();
                editDialogController.setTask(task);
                showDialog();
                if (editDialogController.isChanged()) {
                    System.out.println("wsfsf");
                    task = editDialogController.getTask();
                    if (isActual(task)) {
                        fillData(getCollection("add", task));
                        //taskListImpl.add(task);
                        //setTask(task);
                        //sendData(task);


                    }
                }
                break;

            case "btnEdit":
                task = (Task)tableTaskList.getSelectionModel().getSelectedItem();

                if (task != null) {

                    editDialogController.setTask(task);
                    showDialog();
                    if (editDialogController.isChanged()) {
                        Task task1 = new Task();
                        task1.setName(editDialogController.getTask().getName());
                        task1.setTime(editDialogController.getTask().getTime());

                        taskListImpl.add(task1);

                        setTask(task1);
                        ((TimerTask) taskList.get(task.getId())).cancel();  // завершили таск
                        taskList.remove(task.getId());                      // удалили из списка таймертасков
                        taskListImpl.delete(task);                          // удалили "из гуи таблицы"
                    }
                } else {
                    alert("Выберите задачу");
                }
                break;

            case "btnDelete":
                task = (Task) tableTaskList.getSelectionModel().getSelectedItem();

                if (task != null) {
                    ((TimerTask) taskList.get(task.getId())).cancel();
                    taskList.remove(task.getId());
                    taskListImpl.delete(task);
                } else {
                    alert("Выберите задачу");
                }

                break;
        }
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /*private void initListeners() {
        tableTaskList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                Task task = (Task)tableTaskList.getSelectionModel().getSelectedItem();

                if (task != null) {

                    editDialogController.setTask(task);
                    showDialog();
                    if (editDialogController.isChanged()) {
                        Task task1 = new Task();
                        task1.setName(editDialogController.getTask().getName());
                        task1.setTime(editDialogController.getTask().getTime());

                        taskListImpl.add(task1);

                        setTask(task1);
                        ((TimerTask) taskList.get(task.getId())).cancel();  // завершили таск
                        taskList.remove(task.getId());                      // удалили из списка таймертасков
                        taskListImpl.delete(task);                          // удалили "из гуи таблицы"
                    }
                } else {
                    alert("Выберите задачу");
                }
            }
        });
    }*/

    private void showNotification() {
        if (NotificationStage == null) {
            NotificationStage = new Stage();
            NotificationStage.setTitle("Уведомление");
            NotificationStage.setMinHeight(150);
            NotificationStage.setMinWidth(300);
            NotificationStage.setResizable(false);
            NotificationStage.setScene(new Scene(notifFxmlEdit));
            NotificationStage.initModality(Modality.WINDOW_MODAL);
            NotificationStage.initOwner(mainStage);
        }
        NotificationStage.showAndWait();
    }

    private void showDialog() {
        if (editDialogStage == null) {
            editDialogStage = new Stage();
            editDialogStage.setTitle("Редактирование задачи");
            editDialogStage.setMinHeight(150);
            editDialogStage.setMinWidth(300);
            editDialogStage.setResizable(false);
            editDialogStage.setScene(new Scene(fxmlEdit));
            editDialogStage.initModality(Modality.WINDOW_MODAL);
            editDialogStage.initOwner(mainStage);
        }
        editDialogStage.showAndWait();
    }

    public void saveToXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(CollectionTaskList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //File file = new File(System.getProperty("user.dir") + "/src/main/resources/tasks.xml");
            File file = new File("src/main/resources/tasks.xml");
            //File file = new File(String.valueOf(getClass().getResource("/tasks.xml")));
            marshaller.marshal(taskListImpl, file);
        } catch (JAXBException exception) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, "marshallExample threw JAXBException", exception);
        }
    }

    public CollectionTaskList loadFromXML(CollectionTaskList taskListImpl_temp) {
        try {
            JAXBContext context = JAXBContext.newInstance(CollectionTaskList.class);
            //File file = new File(System.getProperty("user.dir") + "/src/main/resources/tasks.xml");
            File file = new File("src/main/resources/tasks.xml");
            //File file = new File(String.valueOf(getClass().getResource("/tasks.xml")));
            //System.out.println(getClass().getResource("/tasks.xml"));
            Unmarshaller unmarshaller = context.createUnmarshaller();

            taskListImpl_temp = (CollectionTaskList) unmarshaller.unmarshal(file);

            /*System.out.println("Objects created from XML:");
            for (Task task : taskListImpl_temp.getTaskList()) {
                System.out.println(task.getName());
            }*/
        } catch (JAXBException exception) {

        }
        return taskListImpl_temp;
    }
}
