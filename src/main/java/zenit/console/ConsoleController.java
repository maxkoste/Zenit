package zenit.console;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import zenit.terminal.TerminalSession;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import zenit.ConsoleRedirect;
import zenit.ui.MainController;

/**
 * The controller class for ConsoleArea
 * 
 * @author siggelabor
 *
 */
/**
 * @author Admin
 *
 */
public class ConsoleController implements Initializable {

	private ArrayList<ConsoleArea> consoleList = new ArrayList<ConsoleArea>();
	private ArrayList<AnchorPane> terminalList = new ArrayList<>();
	private AnchorPane activeTerminal;
	@FXML
	private TabPane consoleTabPane;
	@FXML
	private Button btnTerminal;
	@FXML
	private Button btnConsole;
	@FXML
	private ChoiceBox<ConsoleArea> consoleChoiceBox;
	@FXML
	private AnchorPane rootAnchor;
	@FXML
	private AnchorPane rootNode;
	@FXML
	private Button btnNewTerminal;
	@FXML
	private Button btnNewConsole;
	@FXML
	private Button btnClearConsole;
	@FXML
	private FontIcon iconCloseConsoleInstance;
	@FXML
	private FontIcon iconTerminateProcess;
	@FXML
	private FontIcon iconCloseTerminalInstance;
	private AnchorPane consoleAnchorPane;
	private ConsoleArea activeConsole;
	private AnchorPane noConsolePane;
	private MainController mainController;

	public void setMainController(MainController mainController) {
		this.mainController = mainController;

	}

	public List<String> getStylesheets() {
		return rootNode.getStylesheets();
	}

	/**
	 * Shows the choiceBox with console areas, and sets the choiceBox with terminal
	 * tabs to not
	 * visible. Also sets text color of the labels.
	 */

	public void showConsoleTabs() {

		btnTerminal.setStyle("");
		btnConsole.setStyle("-fx-text-fill:white; -fx-border-color:#666; -fx-border-width: 0 0 2 0;");

		consoleChoiceBox.setVisible(true);
		consoleChoiceBox.setDisable(false);
		btnNewTerminal.setVisible(true);
		btnNewConsole.setVisible(true);
		btnClearConsole.setDisable(false);
		btnClearConsole.setVisible(true);
		iconTerminateProcess.setVisible(true);
		iconTerminateProcess.setDisable(false);
		iconCloseConsoleInstance.setVisible(true);
		iconCloseConsoleInstance.setDisable(false);
		iconCloseTerminalInstance.setVisible(false);
		iconCloseTerminalInstance.setDisable(true);

		if (consoleAnchorPane != null) {
			consoleAnchorPane.toFront();
		}

		if (consoleList.size() == 0) {
			createEmptyConsolePane();
		}

	}

	/*
	 * Creates and displays an anchorPane when there is no console to display in the
	 * console-window
	 */
	private void createEmptyConsolePane() {
		noConsolePane = new AnchorPane();
		fillAnchor(noConsolePane);
		Label label = new Label("No Console To Display");
		noConsolePane.getChildren().add(label);
		label.setFont(new Font(14));
		label.setTextFill(Color.BLACK);
		label.setMaxWidth(Double.MAX_VALUE);
		AnchorPane.setLeftAnchor(label, 0.0);
		AnchorPane.setRightAnchor(label, 0.0);
		label.setAlignment(Pos.CENTER);
		noConsolePane.setId("empty");
		rootAnchor.getChildren().add(noConsolePane);
		noConsolePane.toFront();
	}

	/**
	 * Shows the choiceBox with terminal panes, and sets the choiceBox with console
	 * tabs to not
	 * visible. Also sets text color of the labels.
	 */
	public void showTerminalTabs() {
		btnConsole.setStyle("");
		btnTerminal.setStyle("-fx-text-fill:white; -fx-border-color:#666; -fx-border-width: 0 0 2 0;");

		consoleChoiceBox.setVisible(false);
		consoleChoiceBox.setDisable(true);
		btnNewTerminal.setVisible(true);
		btnNewConsole.setVisible(false);
		btnClearConsole.setDisable(true);
		btnClearConsole.setVisible(false);
		iconTerminateProcess.setVisible(false);
		iconTerminateProcess.setDisable(true);
		iconCloseConsoleInstance.setVisible(false);
		iconCloseConsoleInstance.setDisable(true);
		iconCloseTerminalInstance.setVisible(true);
		iconCloseTerminalInstance.setDisable(false);

	}

	/**
	 * Creates a new ConsoleArea, adds it to the console AnchorPane and puts it as
	 * an option in the
	 * choiceBox.
	 */

	public void newConsole(ConsoleArea consoleArea) {
		System.out.println("[DEBUGGING] Creating a new console");
		consoleAnchorPane = new AnchorPane();
		consoleArea.setId("consoleArea");
		consoleAnchorPane.setId("consoleAnchor");
		fillAnchor(consoleArea);
		fillAnchor(consoleAnchorPane);

		consoleAnchorPane.getChildren().add(consoleArea);
		rootAnchor.getChildren().add(consoleAnchorPane);

		consoleList.add(consoleArea);

		consoleChoiceBox.getItems().add(consoleArea);
		consoleChoiceBox.getSelectionModel().select(consoleArea);

		new ConsoleRedirect(consoleArea);
		showConsoleTabs();
	}

	/*
	 * Creates a new Terminal, adds it to the terminal
	 * AnchorPane and puts it as an option in the
	 * choiceBox.
	 */
	public void newTerminal() {
		System.out.println("[DEBUGGING] creating a new terminal");
		AnchorPane terminalPane = new AnchorPane();
		fillAnchor(terminalPane);

		WebView webView = new WebView();
		fillAnchor(webView);

		WebEngine engine = webView.getEngine();
		engine.load(
			getClass()
			.getResource("/xterm/index.html")
			.toExternalForm()
		);

		terminalPane.getChildren().add(webView);
		rootAnchor.getChildren().add(terminalPane);

		terminalList.add(terminalPane);
		activeTerminal = terminalPane;

		terminalPane.toFront();

		showTerminalTabs();

		// Test
		new Thread(()->{
			TerminalSession session = new TerminalSession(engine);
			session.start();
		}).start();

	}

	/**
	 * sets the anchor of a node to fill parent
	 * 
	 * @param node to fill to parent anchor
	 */
	public void fillAnchor(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
	}

	/**
	 * Clears the active consoleArea
	 */
	public void clearConsole() {
		activeConsole.clear();
	}

	public void closeComponent() {
		mainController.closeConsoleComponent();
	}

	public void changeAllConsoleAreaColors(String color) {
		for (ConsoleArea c : consoleList) {
			c.setBackgroundColor(color);
		}
	}

	/**
	 * Performs initialization steps.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		consoleChoiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {

			if (newValue != null) {
				for (ConsoleArea console : consoleList) {
					if (newValue.equals(console)) {
						console.getParent().toFront();
						activeConsole = console;
					}
				}
			}

		});

		showConsoleTabs();

		// Console
		iconCloseConsoleInstance.setOnMouseClicked(e -> {
			rootAnchor.getChildren().remove(activeConsole.getParent());
			consoleList.remove(activeConsole);
			consoleChoiceBox.getItems().remove(activeConsole);
			consoleChoiceBox.getSelectionModel().selectLast();

			if (consoleList.size() == 0) {
				createEmptyConsolePane();
			}
		});

		btnNewConsole.setOnMouseClicked(e -> {
			if (mainController.isDarkmode()) {
				newConsole(new ConsoleArea("Console(" + consoleList.size() + ")", null, "-fx-background-color:#444"));
			} else {
				newConsole(
						new ConsoleArea("Console(" + consoleList.size() + ")", null, "-fx-background-color:#989898"));
			}

		});

		iconTerminateProcess.setOnMouseClicked(e -> {
			for (var item : consoleList) {
				if (item.equals(activeConsole)) {
					if (item != null) {
						item.getProcess().destroy();
					}
				}
			}
		});

		iconCloseTerminalInstance.setOnMouseClicked(e -> {
			if (activeTerminal != null) {
				rootAnchor.getChildren().remove(activeTerminal);
				terminalList.remove(activeTerminal);

				if (!terminalList.isEmpty()) {
					activeTerminal = terminalList.get(terminalList.size() - 1);
					activeTerminal.toFront();
				}
			}
		});
	}
}
