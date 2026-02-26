package zenit.ui.tree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Related requirements:
 * - FUI201: move files through drag and drop
 * - FFH107: move files
 */
public class FileTreeMoveFlowTestFX extends ApplicationTest {

	@Override
	public void start(Stage stage) {
		stage.setScene(new Scene(new AnchorPane(), 320, 200));
		stage.show();
	}

	@Test
	@DisplayName("FUI201/FFH107: move flow should update disk and tree structure")
	public void shouldMoveFileAndUpdateTreeStructure() throws IOException {
		Path workspace = Files.createTempDirectory("workspace");
		Path sourceDir = Files.createDirectory(workspace.resolve("source"));
		Path targetDir = Files.createDirectory(workspace.resolve("target"));
		Path sourceFile = Files.createFile(sourceDir.resolve("MoveMe.txt"));

		FileTreeItem<String> root = new FileTreeItem<>(workspace.toFile(), "workspace", FileTreeItem.WORKSPACE);
		FileTreeItem<String> sourceNode = new FileTreeItem<>(sourceDir.toFile(), "source", FileTreeItem.FOLDER);
		FileTreeItem<String> targetNode = new FileTreeItem<>(targetDir.toFile(), "target", FileTreeItem.FOLDER);
		FileTreeItem<String> sourceFileNode = new FileTreeItem<>(sourceFile.toFile(), "MoveMe.txt", FileTreeItem.FILE);

		sourceNode.getChildren().add(sourceFileNode);
		root.getChildren().add(sourceNode);
		root.getChildren().add(targetNode);

		assertNotNull(FileTree.getTreeItemFromFile(root, sourceFile.toFile()));

		Path moved = targetDir.resolve(sourceFile.getFileName());
		Files.move(sourceFile, moved, StandardCopyOption.REPLACE_EXISTING);

		FileTreeItem<String> oldItem = FileTree.getTreeItemFromFile(root, sourceFile.toFile());
		assertNotNull(oldItem);
		oldItem.getParent().getChildren().remove(oldItem);
		FileTree.createParentNode(targetNode, moved.toFile());

		assertFalse(Files.exists(sourceFile));
		assertTrue(Files.exists(moved));
		assertNull(FileTree.getTreeItemFromFile(root, sourceFile.toFile()));

		FileTreeItem<String> movedItem = FileTree.getTreeItemFromFile(root, moved.toFile());
		assertNotNull(movedItem);
		assertEquals(targetNode, movedItem.getParent());
	}
}
