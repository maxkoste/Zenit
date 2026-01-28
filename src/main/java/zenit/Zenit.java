package zenit;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import zenit.launchers.MacOSLauncher;
import zenit.setup.SetupController;
import zenit.ui.MainController;

/**
 * Used to launch the application
 * @author Alexander Libot
 *
 */
public class Zenit extends Application {
	
	public static final String OS = System.getProperty("os.name");
	
	@Override
	public void start(Stage stage) throws Exception {
		
		File workspace = new File("res/workspace/workspace.dat");
		File JDK = new File("res/JDK/JDK.dat");
		File defaultJDK = new File ("res/JDK/DefaultJDK.dat");
		
		SetupController sc;
		if (!workspace.exists() || !JDK.exists() || !defaultJDK.exists()) {
			sc = new SetupController();
			sc.start();
		}

		if (OS.equals("Mac OS X")) {
			new MacOSLauncher(stage);
		} else if (OS.equals("Linux")){
			new MainController(stage);
		} else if (OS.startsWith("Windows")) {
			new MainController(stage);
		}
	}
	
	@Override
	public void stop() {
		Platform.exit();
		System.exit(0);
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}

}
