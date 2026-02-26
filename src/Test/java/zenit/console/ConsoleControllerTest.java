package zenit.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class ConsoleControllerTest {

	// Requirement: Clearing should be safe even when no console is selected.
	// Expected behavior: method call does not throw.
	@Test
	public void testClearConsoleWithoutActiveConsoleShouldNotCrash() {
		ConsoleController controller = new ConsoleController();

		try {
			controller.clearConsole();
		} catch (Exception e) {
			fail("clearConsole should not throw when activeConsole is null, but threw: " + e);
		}
	}

	// Requirement: Closing should be safe even when main controller is not wired.
	// Expected behavior: method call does not throw.
	@Test
	public void testCloseComponentWithoutMainControllerShouldNotCrash() {
		ConsoleController controller = new ConsoleController();

		try {
			controller.closeComponent();
		} catch (Exception e) {
			fail("closeComponent should not throw when mainController is null, but threw: " + e);
		}
	}

	// Requirement: Updating colors should be safe even when there are no console instances.
	// Expected behavior: method call does not throw.
	@Test
	public void testChangeAllConsoleAreaColorsWithEmptyListShouldNotCrash() {
		ConsoleController controller = new ConsoleController();

		try {
			controller.changeAllConsoleAreaColors("-fx-background-color:#111");
		} catch (Exception e) {
			fail("changeAllConsoleAreaColors should not throw on empty list, but threw: " + e);
		}
	}
}
