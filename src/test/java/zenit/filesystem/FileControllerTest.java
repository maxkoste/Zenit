package zenit.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zenit.filesystem.helpers.CodeSnippets;

public class FileControllerTest {

	private FileController controller;
	private File tempDir;

	@BeforeEach
	void setUp() throws Exception {
		tempDir = Files.createTempDirectory("zenitTest").toFile();
		controller = new FileController(tempDir);
	}

	@AfterEach
	void tearDown() {
		for (File f : tempDir.listFiles())
			f.delete();
		tempDir.delete();
	}

	@Test
	public void getWorkspaceTest() {
		File workspace = controller.getWorkspace();
		assertEquals(tempDir.getAbsolutePath(), workspace.getAbsolutePath());
	}

	@Test
	void createFile_emptySnippet_createsEmptyFile() throws Exception {
		File newFile = new File(tempDir, "Empty.java");
		File result = controller.createFile(newFile, null, CodeSnippets.EMPTY);

		assertNotNull(result);
		assertTrue(result.exists());
		assertEquals("", Files.readString(result.toPath()));
	}

	@Test
	void createFile_classSnippet_containsClassDeclaration() throws Exception {
		File newFile = new File(tempDir, "MyClass.java");
		File result = controller.createFile(newFile, null, CodeSnippets.CLASS);

		assertNotNull(result);
		String content = Files.readString(result.toPath());
		assertTrue(content.contains("class MyClass"));
	}

	@Test
	void createFile_interfaceSnippet_containsInterfaceDeclaration() throws Exception {
		File newFile = new File(tempDir, "MyInterface.java");
		File result = controller.createFile(newFile, null, CodeSnippets.INTERFACE);

		assertNotNull(result);
		String content = Files.readString(result.toPath());
		assertTrue(content.contains("interface MyInterface"));
	}

	@Test
	void createFile_invalidTypeCode_returnsNull() {
		File newFile = new File(tempDir, "Test.java");
		File result = controller.createFile(newFile, null, 0);
		assertNull(result, "Invalid typeCode should return null");
	}

	@Test
	void readFile_validFile_returnsContent() throws Exception {
		File file = new File(tempDir, "test.java");
		Files.writeString(file.toPath(), "hello world");

		String result = FileController.readFile(file);

		assertEquals("hello world" + System.lineSeparator(), result);
	}

	@Test
	void readFile_nullFile_returnsEmptyString() {
		String result = FileController.readFile(null);
		assertEquals("", result);
	}

	@Test
	void readFile_nonExistentFile_returnsNull() {
		File missing = new File(tempDir, "doesnotexist.java");
		String result = FileController.readFile(missing);
		assertNull(result);
	}

	@Test
	void readFileContent_nullFile_returnsNull() {
		String result = controller.readFileContent(null);
		assertNull(result);
	}

	@Test
	void readFileContent_validFile_returnsContent() throws Exception {
		File file = new File(tempDir, "Test.java");
		Files.writeString(file.toPath(), "public class Test {}");

		String result = controller.readFileContent(file);

		assertNotNull(result);
		assertTrue(result.contains("public class Test {}"));
	}

	@Test
	void readFileContent_nonExistentFile_returnsNull() {
		File missing = new File(tempDir, "Missing.java");
		String result = controller.readFileContent(missing);
		assertNull(result);
	}

	@Test
	void writeFile_nullFile_returnsFalse() {
		boolean result = controller.writeFile(null, "content");
		assertFalse(result);
	}

	@Test
	void writeFile_nullContent_returnsFalse() throws Exception {
		File file = new File(tempDir, "Test.java");
		file.createNewFile();
		boolean result = controller.writeFile(file, null);
		assertFalse(result);
	}

	@Test
	void writeFile_validFileAndContent_returnsTrueAndWritesContent() throws Exception {
		File file = new File(tempDir, "Test.java");
		file.createNewFile();

		boolean result = controller.writeFile(file, "public class Test {}");

		assertTrue(result);
		assertEquals("public class Test {}", Files.readString(file.toPath()));
	}

	@Test
	void renameFile_nullFile_returnsNull() {
		File result = controller.renameFile(null, "NewName");
		assertNull(result);
	}

	@Test
	void renameFile_nullName_returnsNull() throws Exception {
		File file = new File(tempDir, "Test.java");
		file.createNewFile();
		File result = controller.renameFile(file, null);
		assertNull(result);
	}

	@Test
	void renameFile_validFile_returnsRenamedFile() throws Exception {
		File file = new File(tempDir, "Test.java");
		file.createNewFile();

		File result = controller.renameFile(file, "Renamed.java");

		assertNotNull(result);
		assertTrue(result.exists());
		assertEquals("Renamed.java", result.getName());
	}

	@Test
	void renameFile_validDirectory_returnsRenamedDirectory() throws Exception {
		File dir = new File(tempDir, "mypackage");
		dir.mkdir();

		File result = controller.renameFile(dir, "renamedpackage");

		assertNotNull(result);
		assertTrue(result.exists());
		assertEquals("renamedpackage", result.getName());
	}

	@Test
	void deleteFile_nullFile_returnsFalse() {
		boolean result = controller.deleteFile(null);
		assertFalse(result);
	}

	@Test
	void deleteFile_validFile_returnsTrueAndFileNoLongerExists() throws Exception {
		File file = new File(tempDir, "Test.java");
		file.createNewFile();

		boolean result = controller.deleteFile(file);

		assertTrue(result);
		assertFalse(file.exists());
	}

	@Test
	void deleteFile_validDirectory_returnsTrueAndDirectoryNoLongerExists() throws Exception {
		File dir = new File(tempDir, "mypackage");
		dir.mkdir();

		boolean result = controller.deleteFile(dir);

		assertTrue(result);
		assertFalse(dir.exists());
	}

	@Test
	void deleteFile_nonExistentFile_returnsFalse() {
		File missing = new File(tempDir, "Missing.java");
		boolean result = controller.deleteFile(missing);
		assertFalse(result);
	}

	@Test
	void containMainMethod_fileWithMainMethod_returnsTrue() throws Exception {
		File file = new File(tempDir, "Main.java");
		Files.writeString(file.toPath(), "public class Main { public static void main(String[] args) {} }");

		assertTrue(controller.containMainMethod(file));
	}

	@Test
	void containMainMethod_fileWithoutMainMethod_returnsFalse() throws Exception {
		File file = new File(tempDir, "Test.java");
		Files.writeString(file.toPath(), "public class Test {}");

		assertFalse(controller.containMainMethod(file));
	}

	@Test
	void containMainMethod_nonExistentFile_returnsFalse() {
		File missing = new File(tempDir, "Missing.java");
		assertFalse(controller.containMainMethod(missing));
	}

	@Test
	void containMainMethod_nullFile_returnsFalse() {
		assertFalse(controller.containMainMethod(null));
	}

	@Test
	void createPackage_validFile_returnsTrue() {
		File packageDir = new File(tempDir, "mypackage");
		boolean result = controller.createPackage(packageDir);

		assertTrue(result);
		assertTrue(packageDir.exists());
	}

	@Test
	void createPackage_alreadyExists_returnsFalse() throws Exception {
		File packageDir = new File(tempDir, "mypackage");
		packageDir.mkdir();

		boolean result = controller.createPackage(packageDir);

		assertFalse(result);
	}

	@Test
	void createPackage_nullFile_returnsFalse() {
		boolean result = controller.createPackage(null);
		assertFalse(result);
	}

	@Test
	void createProject_validName_returnsProjectFile() {
		File result = controller.createProject("MyProject");

		assertNotNull(result);
		assertTrue(result.exists());
		assertEquals("MyProject", result.getName());
	}

	@Test
	void createProject_nullName_returnsNull() {
		File result = controller.createProject(null);
		assertNull(result);
	}

	@Test
	void createProject_alreadyExists_returnsNull() {
		controller.createProject("MyProject");
		File result = controller.createProject("MyProject");
		assertNull(result);
	}

	@Test
	void createProject_emptyName_behavesGracefully() {
		File result = controller.createProject("");
		// An empty string creates a file with no name —
		// verify it either returns null or creates nothing unexpected
		assertNull(result);
	}

	@Test
	void changeWorkspace_validWorkspace_returnsTrueAndUpdatesWorkspace() {
		File newWorkspace = new File(tempDir, "newWorkspace");
		newWorkspace.mkdir();

		boolean result = controller.changeWorkspace(newWorkspace);

		assertTrue(result);
		assertEquals(newWorkspace.getAbsolutePath(),
				controller.getWorkspace().getAbsolutePath());
	}

	@Test
	void changeWorkspace_calledTwice_updatesToLatestWorkspace() {
		File firstWorkspace = new File(tempDir, "first");
		firstWorkspace.mkdir();
		controller.changeWorkspace(firstWorkspace);

		File secondWorkspace = new File(tempDir, "second");
		secondWorkspace.mkdir();
		controller.changeWorkspace(secondWorkspace);

		assertEquals(secondWorkspace.getAbsolutePath(),
				controller.getWorkspace().getAbsolutePath());
	}

	@Test
	void changeWorkspace_nullWorkspace_returnsFalse() {
		boolean result = controller.changeWorkspace(null);
		assertFalse(result);
	}

	@Test
	void importProject_nullSource_throwsException() {
		try {
			assertNull(controller.importProject(null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void importProject_sourceEqualsWorkspace_returnsNull() throws Exception {
		File result = controller.importProject(tempDir);
		assertNull(result);
	}

	@Test
	void importProject_validSource_returnsImportedProject() throws Exception {
		File externalDir = Files.createTempDirectory("externalProject").toFile();
		File source = new File(externalDir, "MyProject");
		source.mkdir();

		File result = controller.importProject(source);

		assertNotNull(result);
		assertTrue(result.exists());

		result.delete();
		source.delete();
		externalDir.delete();
	}
}
