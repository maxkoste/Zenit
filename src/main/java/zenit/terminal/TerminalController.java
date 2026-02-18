package zenit.terminal;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class TerminalController {

	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private AnchorPane basePane;
	
	public void initialize() {
		
		addTerminalTab();
		
	}
	
	private void addTerminalTab() {
		WebView webView = new WebView();
		WebEngine engine = webView.getEngine();

		engine.load(
			getClass()
				.getResource("/xterm/index.html")
				.toExternalForm()
		);

		System.out.println("[DEBUGGING] Loaded index.html");

		Tab tab = new Tab("Terminal");
		tab.setContent(webView);
		tabPane.getTabs().add(tab);
	}
}

