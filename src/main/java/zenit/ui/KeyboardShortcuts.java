package zenit.ui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

import zenit.ui.tree.FileTreeClipboard;
import zenit.ui.tree.FileTreeItem;
/**
 * Static methods for interacting with a scene's accelerators.
 * 
 * @author Pontus Laos
 *
 */
public final class KeyboardShortcuts {
	/**
	 * Maps a KeyCodeCombination of the given KeyCode and Modifier to the given
	 * Runnable, adding it to the given Scene.
	 * 
	 * @param scene    The Scene to add the KeyCodeCombination on.
	 * @param code     The KeyCode to create a KeyCodeCombination with.
	 * @param modifier The Modifier to create a KeyCodeCombination with.
	 * @param action   The Runnable to be run when the KeyCodeCombination is
	 *                 triggered.
	 */
	public static final void add(Scene scene, KeyCode code, Modifier modifier, Runnable action) {
		scene.getAccelerators().put(new KeyCodeCombination(code, modifier), action);
	}

	/**
	 * Sets up keyboard shortcuts for the main scene, using methods of the
	 * MainController.
	 * 
	 * @param scene      The scene to be used.
	 * @param controller The controller to call methods from.
	 * @param clipboard  The file tree clipboard for copy/cut/paste operations.
	 * @param treeView   The file tree view.
	 */
	public static final void setupMain(Scene scene, MainController controller, FileTreeClipboard clipboard, TreeView<String> treeView) {
//		add(scene, KeyCode.S, KeyCombination.SHORTCUT_DOWN, () -> controller.saveFile(null));
//		add(scene, KeyCode.O, KeyCombination.SHORTCUT_DOWN, () -> controller.openFile((Event) null));
//		add(scene, KeyCode.N, KeyCombination.SHORTCUT_DOWN, controller::addTab);
//		add(scene, KeyCode.W, KeyCombination.SHORTCUT_DOWN, () -> controller.closeTab(null));
//		add(scene, KeyCode.R, KeyCombination.SHORTCUT_DOWN, controller::compileAndRun);
//		add(scene, KeyCode.F, KeyCombination.SHORTCUT_DOWN, controller::search);
		add(scene, KeyCode.SPACE, KeyCombination.CONTROL_DOWN, controller::shortcutsTrigger);
//		add(scene, KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN, controller::commentAndUncomment); 
		
		scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (controller.getSelectedTab() != null) {
					if (controller.getSelectedTab().getZenCodeArea().isFocused()) {
						if (ke.getCode() == KeyCode.ENTER) {
							controller.commentsShortcutsTrigger();
							controller.navigateToCorrectTabIndex();
							ke.consume(); // <-- stops passing the event to next node
						}
					}
				}

			}
		});
		
		scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (controller.getSelectedFileTreeItem() != null) {
				if (event.getCode() == KeyCode.DELETE) {
					controller.deleteFileFromTreeView();
				}
			}
		});

		// File tree: Copy / Cut / Paste
		scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (!treeView.isFocused()) return;

			FileTreeItem<String> selected = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
			if (selected == null) return;

			boolean isShortcut = event.isShortcutDown();

			if (isShortcut && event.getCode() == KeyCode.C) {
				clipboard.copy(selected);
				event.consume();
			} else if (isShortcut && event.getCode() == KeyCode.X) {
				clipboard.cut(selected);
				event.consume();
			} else if (isShortcut && event.getCode() == KeyCode.V) {
				clipboard.paste(selected, treeView);
				event.consume();
			}
		});
	}
}
