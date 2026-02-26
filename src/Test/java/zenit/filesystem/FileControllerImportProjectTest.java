package zenit.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileControllerImportProjectTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldReturnNullWhenImportingWorkspaceIntoItself() throws IOException {
		File workspace = Files.createDirectory(tempDir.resolve("workspace")).toFile();
		FileController fileController = new FileController(workspace);

		File imported = fileController.importProject(workspace);

		assertNull(imported);
	}

	@Test
	void shouldImportProjectWhenSourceIsDifferentDirectory() throws IOException {
		Path workspacePath = Files.createDirectory(tempDir.resolve("workspace"));
		Path sourceRootPath = Files.createDirectory(tempDir.resolve("external-source"));
		Path sourceProjectPath = Files.createDirectory(sourceRootPath.resolve("DemoProject"));
		Files.writeString(sourceProjectPath.resolve("README.txt"), "demo");

		FileController fileController = new FileController(workspacePath.toFile());
		File imported = fileController.importProject(sourceProjectPath.toFile());

		assertNotNull(imported);
		assertTrue(imported.exists());
		assertTrue(new File(imported, "README.txt").exists());
	}
}
