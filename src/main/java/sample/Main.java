package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.controller.Controller;
import sample.model.Task;
import sample.service.CollectionTaskList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;

public class Main extends Application {

    public static Socket socket;
    public static ObjectInputStream in;
    public static ObjectOutputStream out;

    public static String USER = "Unknown";

    @Override
    public void start(Stage primaryStage) throws Exception{


        //initializeStream();

        TextInputDialog dialog = new TextInputDialog("Ilya");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().equals("") ){
            System.exit(0);
        }

        USER = result.get();

        //Socket socket = null;
            try {
                socket = new Socket("localhost", 19000);
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());


                //////////////////////////////////////////////////////////////////////////////////////////*/
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample.fxml"));
                Parent fxmlMain = fxmlLoader.load();
                Controller controller = fxmlLoader.getController();
                controller.setMainStage(primaryStage);

                primaryStage.setTitle("Планировщик задач");
                primaryStage.setMinHeight(400);
                primaryStage.setMinWidth(370);
                primaryStage.setScene(new Scene(fxmlMain, 300, 275));
                primaryStage.show();
                primaryStage.setOnCloseRequest(event -> {
                    //controller.saveToXML();
                    //Controller.getCollection("save", new Task());
                    end();
                    System.exit(0);
                });
                //////////////////////////////////////////////////////////////////////////////////////////


            } catch (Exception e) {
                    e.printStackTrace();
                }

    }

    public static void initializeStream() {
        try {
            socket = new Socket("localhost", 19000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void end() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //initializeStream();
        launch(args);
    }
}
