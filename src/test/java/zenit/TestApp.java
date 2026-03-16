package zenit;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Stubb for running tests that require javafx to be running
 * @author Max Koste
 */
public class TestApp extends Application {
    @Override
    public void start(Stage stage) {
    }

    public static void initJavaFX() throws InterruptedException {
        Thread t = new Thread(() -> Application.launch(TestApp.class));
        t.setDaemon(true);
        t.start();
        Thread.sleep(1000);
    }
}
