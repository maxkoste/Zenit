package zenit.launchers;

import javafx.stage.Stage;
import zenit.ui.MainController;

public class MacOSLauncher {

	public MacOSLauncher(Stage stage) {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
		new MainController(stage);
	}

}
