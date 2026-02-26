package zenit.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsoleAreaStreamsTest extends ApplicationTest {

	private AnchorPane root;
	private ConsoleArea consoleArea;

	@Override
	public void start(Stage stage) {
		root = new AnchorPane();
		stage.setScene(new Scene(root, 640, 320));
		stage.show();
	}

	@BeforeEach
	public void setUpConsoleArea() {
		interact(() -> {
			root.getChildren().clear();
			consoleArea = new ConsoleArea("Console(Stream)", null, "-fx-background-color:#444");
			root.getChildren().add(consoleArea);
		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	// Expected: Output stream writes newline-terminated text as one visible line.
	public void shouldWriteSingleOutputLineForByteArray() {
		ConsoleAreaOutputStream stream = new ConsoleAreaOutputStream(consoleArea);
		byte[] payload = "abc\n".getBytes(StandardCharsets.UTF_8);

		stream.write(payload, 0, payload.length);
		WaitForAsyncUtils.waitForFxEvents();

		String text = readConsoleText();
		assertEquals("abc\n", text);
		assertEquals(1, text.lines().count());
	}

	@Test
	// Expected: Error stream writes newline-terminated text as one visible line.
	public void shouldWriteSingleErrorLineForByteArray() throws IOException {
		ConsoleAreaErrorStream stream = new ConsoleAreaErrorStream(consoleArea);
		byte[] payload = "error line\n".getBytes(StandardCharsets.UTF_8);

		stream.write(payload, 0, payload.length);
		WaitForAsyncUtils.waitForFxEvents();

		String text = readConsoleText();
		assertEquals("error line\n", text);
		assertEquals(1, text.lines().count());
	}

	private String readConsoleText() {
		AtomicReference<String> text = new AtomicReference<>("");
		interact(() -> text.set(consoleArea.getText()));
		return text.get();
	}
}
