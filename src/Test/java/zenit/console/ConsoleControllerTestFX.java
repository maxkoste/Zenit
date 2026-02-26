package zenit.console;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import zenit.terminal.TerminalInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsoleControllerTestFX extends ApplicationTest {

	private ConsoleController controller;

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/zenit/console/ConsolePane.fxml"));
		Parent root = loader.load();
		controller = loader.getController();

		stage.setScene(new Scene(root, 900, 500));
		stage.show();
	}

	@Test
	// Expected: Console mode is default; console controls visible, terminal controls hidden.
	public void shouldStartInConsoleMode() {
		ChoiceBox<?> consoleChoice = getField("consoleChoiceBox");
		ChoiceBox<?> terminalChoice = getField("terminalChoiceBox");
		Button clearButton = getField("btnClearConsole");

		assertTrue(consoleChoice.isVisible());
		assertFalse(terminalChoice.isVisible());
		assertTrue(clearButton.isVisible());
	}

	@Test
	// Expected: Switching to terminal mode hides console controls and shows terminal selector.
	public void shouldShowTerminalControlsWhenTerminalModeIsActivated() {
		prepareDummyTerminalInstance();

		interact(() -> controller.showTerminalTabs());

		ChoiceBox<?> consoleChoice = getField("consoleChoiceBox");
		ChoiceBox<?> terminalChoice = getField("terminalChoiceBox");
		Button clearButton = getField("btnClearConsole");

		assertFalse(consoleChoice.isVisible());
		assertTrue(terminalChoice.isVisible());
		assertFalse(clearButton.isVisible());
	}

	@Test
	// Expected: Switching back to console mode restores console controls visibility.
	public void shouldReturnToConsoleControlsWhenConsoleModeIsActivated() {
		prepareDummyTerminalInstance();

		interact(() -> controller.showTerminalTabs());
		interact(() -> controller.showConsoleTabs());

		ChoiceBox<?> consoleChoice = getField("consoleChoiceBox");
		ChoiceBox<?> terminalChoice = getField("terminalChoiceBox");
		Button clearButton = getField("btnClearConsole");

		assertTrue(consoleChoice.isVisible());
		assertFalse(terminalChoice.isVisible());
		assertTrue(clearButton.isVisible());
	}

	@Test
	// Expected: Creating a new console adds it to the ChoiceBox and selects it as active.
	public void shouldCreateAndSelectNewConsole() {
		ChoiceBox<ConsoleArea> consoleChoice = getField("consoleChoiceBox");
		int initialCount = consoleChoice.getItems().size();

		interact(() -> controller.newConsole(new ConsoleArea("Console(Test)", null, "-fx-background-color:#444")));

		assertEquals(initialCount + 1, consoleChoice.getItems().size());
		ConsoleArea selectedConsole = consoleChoice.getSelectionModel().getSelectedItem();
		assertNotNull(selectedConsole);

		ConsoleArea activeConsole = getField("activeConsole");
		assertSame(selectedConsole, activeConsole);
	}

	@Test
	// Expected: Clearing should remove all text from the currently active console.
	public void shouldClearActiveConsoleContent() {
		interact(() -> controller.newConsole(new ConsoleArea("Console(Clear)", null, "-fx-background-color:#444")));
		ConsoleArea activeConsole = getField("activeConsole");
		assertNotNull(activeConsole);

		interact(() -> activeConsole.appendText("sample output"));
		assertFalse(activeConsole.getText().isEmpty());

		interact(() -> controller.clearConsole());

		assertTrue(activeConsole.getText().isEmpty());
	}

	private void prepareDummyTerminalInstance() {
		interact(() -> {
			AnchorPane rootAnchor = getField("rootAnchor");
			AnchorPane terminalContainer = new AnchorPane();
			controller.fillAnchor(terminalContainer);
			rootAnchor.getChildren().add(terminalContainer);

			TerminalInstance terminalInstance = new TerminalInstance(terminalContainer, null);

			List<TerminalInstance> terminalList = getField("terminalList");
			terminalList.clear();
			terminalList.add(terminalInstance);
			setField("activeTerminal", terminalInstance);

			ChoiceBox<TerminalInstance> terminalChoiceBox = getField("terminalChoiceBox");
			terminalChoiceBox.getItems().setAll(terminalInstance);
			terminalChoiceBox.getSelectionModel().select(terminalInstance);
		});
	}

	@SuppressWarnings("unchecked")
	private <T> T getField(String fieldName) {
		try {
			Field field = ConsoleController.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(controller);
		} catch (Exception e) {
			throw new RuntimeException("Unable to read field: " + fieldName, e);
		}
	}

	private void setField(String fieldName, Object value) {
		try {
			Field field = ConsoleController.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(controller, value);
		} catch (Exception e) {
			throw new RuntimeException("Unable to set field: " + fieldName, e);
		}
	}
}
