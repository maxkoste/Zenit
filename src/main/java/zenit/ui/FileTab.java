package zenit.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import zenit.LSP.LspDiagnostic;
import zenit.LSP.LspManager;
import zenit.filesystem.FileController;
import zenit.util.StringUtilities;
import zenit.zencodearea.ZenCodeArea;

/**
 * A Tab extension that holds a File.
 * Updated: LSP diagnostic highlighting (FUI403) and tooltips (FUI404).
 */
public class FileTab extends Tab {
	private File initialFile;
	private File file;
	private String initialTitle;
	private MainController mc;

	private ZenCodeArea zenCodeArea;
	private LspManager lspManager;

	private boolean hasChanged;

	// Stores the latest diagnostics for this tab — used for tooltip lookup
	private List<LspDiagnostic> currentDiagnostics = new ArrayList<>();

	// Tooltip shown on hover
	private final Tooltip diagnosticTooltip = new Tooltip();

	public FileTab(ZenCodeArea zenCodeArea, MainController mc, LspManager lspManager) {
		this.lspManager = lspManager;
		this.zenCodeArea = zenCodeArea;
		this.mc = mc;
		initialTitle = "Untitled";

		zenCodeArea.setOnMouseClicked(new UpdateDetector());
		zenCodeArea.setOnKeyPressed(new UpdateDetector());

		initializeUI();
		initTooltipHover();
	}


	private void initializeUI() {
		AnchorPane anchorPane = new AnchorPane();
		AnchorPane.setTopAnchor(zenCodeArea, 0.0);
		AnchorPane.setRightAnchor(zenCodeArea, 0.0);
		AnchorPane.setBottomAnchor(zenCodeArea, 0.0);
		AnchorPane.setLeftAnchor(zenCodeArea, 0.0);

		anchorPane.getChildren().add(zenCodeArea);

		setContent(anchorPane);
		setText(initialTitle);

		zenCodeArea.textProperty().addListener((observable, oldText, newText) -> {
			String initialFileContent = FileController.readFile(initialFile);
			hasChanged = !initialFileContent.equals(newText);
			updateUI();
		});

		setStyle("-fx-background-color: #444;");
		setStyle("-fx-stroke: #fff;");

		Platform.runLater(zenCodeArea::requestFocus);
	}

	/**
	 * FUI404 — shows a tooltip with the diagnostic message
	 * when the mouse hovers over an underlined character.
	 */
	private void initTooltipHover() {
		diagnosticTooltip.setStyle(
				"-fx-background-color: #3c3c3c;" +
						"-fx-text-fill: #ff6b6b;" +
						"-fx-font-size: 12px;" +
						"-fx-padding: 4 8 4 8;"
		);

		zenCodeArea.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
			if (currentDiagnostics.isEmpty()) {
				diagnosticTooltip.hide();
				return;
			}

			// Find which line/column the mouse is over
			int charIndex = zenCodeArea.hit(event.getX(), event.getY())
					.getCharacterIndex().orElse(-1);

			if (charIndex < 0) {
				diagnosticTooltip.hide();
				return;
			}

			// Convert absolute char index to line/col
			int line = zenCodeArea.offsetToPosition(charIndex,
					org.fxmisc.richtext.model.TwoDimensional.Bias.Forward).getMajor();
			int col  = zenCodeArea.offsetToPosition(charIndex,
					org.fxmisc.richtext.model.TwoDimensional.Bias.Forward).getMinor();

			// Check if cursor is within any diagnostic range
			String msg = findDiagnosticMessage(line, col);

			if (msg != null) {
				diagnosticTooltip.setText(msg);
				diagnosticTooltip.show(zenCodeArea,
						event.getScreenX() + 10,
						event.getScreenY() + 10);
			} else {
				diagnosticTooltip.hide();
			}
		});

		zenCodeArea.addEventHandler(MouseEvent.MOUSE_EXITED, e -> diagnosticTooltip.hide());
	}

	/**
	 * Returns the message of the first diagnostic that covers (line, col),
	 * or null if none match.
	 */
	private String findDiagnosticMessage(int line, int col) {
		for (LspDiagnostic d : currentDiagnostics) {
			if (line >= d.getStartLine() && line <= d.getEndLine()) {
				int startC = (line == d.getStartLine()) ? d.getStartChar() : 0;
				int endC   = (line == d.getEndLine())   ? d.getEndChar()   : Integer.MAX_VALUE;
				if (col >= startC && col <= endC) {
					return "[" + d.getSeverityLabel() + "] " + d.getMessage();
				}
			}
		}
		return null;
	}

	// FUI403 — LSP diagnostic highlighting
	/**
	 * Called by MainController when new diagnostics arrive for this file.
	 * Clears old highlighting, applies new underlines.
	 *
	 * Must be called on the JavaFX thread (wrap in Platform.runLater if needed).
	 */
	public void applyDiagnostics(List<LspDiagnostic> diagnostics) {
		this.currentDiagnostics = new ArrayList<>(diagnostics);

		// Clear all previous LSP styling
		clearDiagnosticStyles();

		// Apply new underlines
		for (LspDiagnostic d : diagnostics) {
			String cssClass = d.isError() ? "lsp-error" : "lsp-warning";
			applyDiagnosticStyle(d, cssClass);
		}
	}

	/**
	 * Clears previous LSP underline styling from the entire document.
	 */
	private void clearDiagnosticStyles() {
		// Re-trigger syntax highlighting which will reset spans
		zenCodeArea.update();
	}

	/**
	 * Applies a CSS class as underlining for a single diagnostic range.
	 */
	private void applyDiagnosticStyle(LspDiagnostic d, String cssClass) {
		try {
			int totalLines = zenCodeArea.getParagraphs().size();

			for (int line = d.getStartLine(); line <= d.getEndLine(); line++) {
				if (line >= totalLines) break;

				int lineLength = zenCodeArea.getParagraph(line).length();
				if (lineLength == 0) continue;

				int startCol = (line == d.getStartLine()) ? d.getStartChar() : 0;
				int endCol   = (line == d.getEndLine())
						? Math.min(d.getEndChar(), lineLength)
						: lineLength;

				if (startCol >= endCol) continue;

				// setStyle(paragraph, startCol, endCol, styles)
				zenCodeArea.setStyle(line, startCol, endCol, List.of(cssClass));
			}
		} catch (Exception e) {
			System.err.println("[FileTab] applyDiagnosticStyle failed: " + e.getMessage());
		}
	}

	public void setStyle(int row, int column, String style) {
		int columnLength = zenCodeArea.getParagraph(row - 1).getText().length();

		if (column >= columnLength) {
			Platform.runLater(() ->
					zenCodeArea.setStyle(row - 1, column - 1, column, Arrays.asList(style)));
		} else {
			Platform.runLater(() ->
					zenCodeArea.setStyle(row - 1, column, column + 1, Arrays.asList(style)));
		}
	}

	public void addTextPropertyListener(ChangeListener<? super String> listener) {
		zenCodeArea.textProperty().addListener(listener);
	}

	public void shortcutsTrigger() {
		if (file == null) return;

		String text = zenCodeArea.getText();
		int caretPosition = zenCodeArea.getCaretPosition();

		if (caretPosition >= 6 && text.substring(caretPosition - 6, caretPosition).equals("sysout")) {
			zenCodeArea.replaceText(caretPosition - 6, caretPosition, "System.out.println();");
			zenCodeArea.moveTo(caretPosition + 13);
		} else if (caretPosition >= 6 && text.substring(caretPosition - 6, caretPosition).equals("syserr")) {
			zenCodeArea.replaceText(caretPosition - 6, caretPosition, "System.err.println();");
			zenCodeArea.moveTo(caretPosition + 13);
		} else if (caretPosition >= 4 && text.substring(caretPosition - 4, caretPosition).equals("main")) {
			zenCodeArea.replaceText(caretPosition - 4, caretPosition,
					"public static void main(String[]args) {\n \n}");
			zenCodeArea.moveTo(caretPosition + 37);
		} else if (caretPosition >= 2 && text.substring(caretPosition - 2, caretPosition).equals("pv")) {
			zenCodeArea.replaceText(caretPosition - 2, caretPosition, "public void ");
			zenCodeArea.moveTo(caretPosition + 10);
		}
	}

	public void commentsShortcutsTrigger() {
		if (file == null) return;

		String text = zenCodeArea.getText();
		int caretPosition = zenCodeArea.getCaretPosition();

		if (caretPosition >= 2 && text.substring(caretPosition - 2, caretPosition).equals("/*")) {
			zenCodeArea.replaceText(caretPosition - 2, caretPosition, "/*\n* \n*/");
			zenCodeArea.moveTo(caretPosition + 3);
		} else if (caretPosition >= 3 && text.substring(caretPosition - 3, caretPosition).equals("/**")) {
			zenCodeArea.replaceText(caretPosition - 3, caretPosition, "/**\n* \n* @author \n*/");
			zenCodeArea.moveTo(caretPosition + 3);
		} else {
			zenCodeArea.replaceText(caretPosition, caretPosition, "\n");
		}
	}

	public void navigateToCorrectTabIndex() {
		int previousLine = zenCodeArea.getCurrentParagraph() - 1;
		String previousText = zenCodeArea.getParagraph(previousLine).getText();
		int count = StringUtilities.countLeadingSpaces(previousText);

		String spaces = "";
		for (int i = 0; i < count; i++) spaces += " ";

		if (previousText.endsWith("{")) {
			spaces += "    ";
			zenCodeArea.insertText(zenCodeArea.getCaretPosition(), spaces);
			addMissingCurlyBrace(previousLine + 2, 0, spaces);
		} else {
			zenCodeArea.insertText(zenCodeArea.getCaretPosition(), spaces);
		}
	}

	private void addMissingCurlyBrace(int row, int column, String spaces) {
		int[] counts = {
				StringUtilities.count(zenCodeArea.getText(), '{'),
				StringUtilities.count(zenCodeArea.getText(), '}'),
		};

		if (counts[0] == counts[1] + 1) {
			zenCodeArea.insertText(zenCodeArea.getCaretPosition(), "\n");
			zenCodeArea.insertText(row, column,
					spaces.substring(0, spaces.length() - 4) + "}");
			zenCodeArea.moveTo(row - 1, spaces.length());
		}
	}

	private void updateUI() {
		if (hasChanged) setText(initialTitle + " *");
		else setText(initialTitle);
	}

	public void update(File file) {
		setFile(file, false);
		hasChanged = false;
		updateUI();
	}

	public File getFile()        { return file; }
	public String getFileText()  { return zenCodeArea.getText(); }

	public void setFile(File file, boolean shouldSetContent) {
		this.initialFile  = file;
		this.file         = file;
		this.initialTitle = file == null ? "Untitled" : file.getName();
		setText(initialTitle);

		if (shouldSetContent && file != null) {
			try {

				System.out.println("[DEBUG] Sending this text to the LSP server: \n "
						+ FileController.readFile(file));
				this.lspManager.sendDidOpen(file.getAbsolutePath(),
						FileController.readFile(file));

				this.lspManager.sendDidOpen(file.getAbsolutePath(), FileController.readFile(file));

			} catch (Exception e) {
				e.printStackTrace();
			}
			setFileText(FileController.readFile(file));
		}
	}

	public void setFileText(String text)  { zenCodeArea.replaceText(text); }
	public boolean hasChanged()           { return hasChanged; }
	public ZenCodeArea getZenCodeArea()   { return zenCodeArea; }

	/** Returns the file URI in LSP format, e.g. "file:///C:/Users/.../MyClass.java" */
	public String getFileUri() {
		if (file == null) return null;
		return file.toPath().toUri().toString();
	}

	public int showConfirmDialog() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Save?");
		alert.setHeaderText("The file has been modified. Would you like to save?");
		alert.setContentText("Save?");

		ButtonType okButton     = new ButtonType("Yes",    ButtonData.OK_DONE);
		ButtonType noButton     = new ButtonType("No",     ButtonData.NO);
		ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(cancelButton, okButton, noButton);

		var wrapper = new Object() { int response; };
		alert.showAndWait().ifPresent(result -> {
			if      (result == cancelButton) wrapper.response = 0;
			else if (result == noButton)     wrapper.response = 1;
			else if (result == okButton)     wrapper.response = 2;
		});
		return wrapper.response;
	}

	private class UpdateDetector implements EventHandler<Event> {
		@Override
		public void handle(Event event) {
			int row = zenCodeArea.getCurrentParagraph();
			int col = zenCodeArea.getCaretColumn();
			mc.updateStatusRight((row + 1) + " : " + (col + 1));
		}
	}
}