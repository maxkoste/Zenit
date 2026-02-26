package zenit.ui.tree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Related requirements:
 * - FUI202: show appropriate icons for different file types
 */
public class FileTreeItemTestFX extends ApplicationTest {

	private AnchorPane root;

	@Override
	public void start(Stage stage) {
		root = new AnchorPane();
		stage.setScene(new Scene(root, 320, 200));
		stage.show();
	}

	@Test
	@DisplayName("FUI202: getStringType should return expected labels for known types")
	public void shouldReturnExpectedTypeLabels() throws IOException {
		Path dir = Files.createTempDirectory("tree-type-dir");
		Path file = Files.createTempFile("tree-type-file", ".txt");

		assertEquals("project", new FileTreeItem<>(dir.toFile(), "p", FileTreeItem.PROJECT).getStringType());
		assertEquals("package", new FileTreeItem<>(dir.toFile(), "pkg", FileTreeItem.PACKAGE).getStringType());
		assertEquals("class", new FileTreeItem<>(file.toFile(), "Main.java", FileTreeItem.CLASS).getStringType());
		assertEquals("src-folder", new FileTreeItem<>(dir.toFile(), "src", FileTreeItem.SRC).getStringType());
		assertEquals("folder", new FileTreeItem<>(dir.toFile(), "folder", FileTreeItem.FOLDER).getStringType());
		assertEquals("file", new FileTreeItem<>(file.toFile(), "file.txt", FileTreeItem.FILE).getStringType());
		assertEquals("incompatible",
				new FileTreeItem<>(file.toFile(), "binary.dat", FileTreeItem.INCOMPATIBLE).getStringType());
	}

	@Test
	@DisplayName("FUI202: supported node types should have an icon graphic set on TreeItem")
	public void shouldSetIconGraphicForSupportedTypes() throws IOException {
		Path any = Files.createTempFile("tree-icon", ".tmp");
		int[] types = new int[] {
				FileTreeItem.PROJECT,
				FileTreeItem.PACKAGE,
				FileTreeItem.CLASS,
				FileTreeItem.SRC,
				FileTreeItem.FOLDER,
				FileTreeItem.FILE,
				FileTreeItem.INCOMPATIBLE
		};

		for (int type : types) {
			FileTreeItem<String> item = new FileTreeItem<>(any.toFile(), "node", type);
			assertNotNull(item.getGraphic(), "Expected icon for type " + type);
		}
	}

	@Test
	@DisplayName("FUI202: TreeCell should display icon from TreeItem when rendered with FileTreeDragAndDrop")
	public void shouldDisplayIconInTreeCellAfterDragAndDropAttach() throws IOException {
		Path dir = Files.createTempDirectory("tree-cell-icon");

		interact(() -> {
			FileTreeItem<String> rootItem = new FileTreeItem<>(dir.toFile(), "root", FileTreeItem.PROJECT);
			rootItem.setExpanded(true);

			FileTreeItem<String> folderItem = new FileTreeItem<>(dir.toFile(), "folder", FileTreeItem.FOLDER);
			rootItem.getChildren().add(folderItem);

			TreeView<String> treeView = new TreeView<>(rootItem);
			treeView.setShowRoot(true);
			treeView.setPrefSize(300, 180);

			FileTreeDragAndDrop.attach(treeView);

			root.getChildren().add(treeView);
		});

		WaitForAsyncUtils.waitForFxEvents();

		interact(() -> {
			@SuppressWarnings("unchecked")
			TreeCell<String> cell = lookup(".tree-cell").queryAll().stream()
					.map(node -> (TreeCell<String>) node)
					.filter(c -> "root".equals(c.getText()))
					.findFirst()
					.orElse(null);

			assertNotNull(cell, "TreeCell for root item should exist");
			assertNotNull(cell.getGraphic(),
					"TreeCell should display graphic from TreeItem. " +
					"If this fails, FileTreeDragAndDrop.updateItem() is missing setGraphic() call.");
		});
	}
}
