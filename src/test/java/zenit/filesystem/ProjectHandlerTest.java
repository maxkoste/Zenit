package zenit.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectHandlerTest {

    @TempDir
    File tempDir;

    //create new project

    @Test
    void createNewProject_createsProjectFolder() throws IOException {
        File project = new File(tempDir, "MyProject");
        ProjectHandler.createNewProject(project);
        assertTrue(project.exists());
        assertTrue(project.isDirectory());
    }

    @Test
    void createNewProject_createsSrcFolder() throws IOException {
        File project = new File(tempDir, "MyProject");
        ProjectHandler.createNewProject(project);
        assertTrue(new File(project, "src").exists());
    }

    @Test
    void createNewProject_createsBinFolder() throws IOException {
        File project = new File(tempDir, "MyProject");
        ProjectHandler.createNewProject(project);
        assertTrue(new File(project, "bin").exists());
    }

    @Test
    void createNewProject_createsMetadataFile() throws IOException {
        File project = new File(tempDir, "MyProject");
        ProjectHandler.createNewProject(project);
        assertTrue(new File(project, ".metadata").exists());
    }

    @Test
    void createNewProject_alreadyExists_throwsIOException() throws IOException {
        File project = new File(tempDir, "MyProject");
        project.mkdir(); // already exists
        assertThrows(IOException.class, () -> ProjectHandler.createNewProject(project));
    }

    //import project

    @Test
    void importProject_copiesProjectToTarget() throws IOException {
        //set up a source project with a file inside
        File source = new File(tempDir, "SourceProject");
        source.mkdir();
        new File(source, "src").mkdir();
        new File(source, "src/Main.java").createNewFile();

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        File result = ProjectHandler.importProject(source, targetParent);

        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void importProject_preservesProjectName() throws IOException {
        File source = new File(tempDir, "SourceProject");
        source.mkdir();

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        File result = ProjectHandler.importProject(source, targetParent);
        assertEquals("SourceProject", result.getName());
    }

    @Test
    void importProject_copiesFilesRecursively() throws IOException {
        File source = new File(tempDir, "SourceProject");
        source.mkdir();
        File srcFolder = new File(source, "src");
        srcFolder.mkdir();
        File javaFile = new File(srcFolder, "Main.java");
        javaFile.createNewFile();
        Files.writeString(javaFile.toPath(), "public class Main {}");

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        File result = ProjectHandler.importProject(source, targetParent);
        assertTrue(new File(result, "src/Main.java").exists());
    }

    @Test
    void importProject_createsMetadataIfMissing() throws IOException {
        File source = new File(tempDir, "SourceProject");
        source.mkdir();

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        File result = ProjectHandler.importProject(source, targetParent);
        assertTrue(new File(result, ".metadata").exists());
    }

    @Test
    void importProject_projectAlreadyExists_throwsIOException() throws IOException {
        File source = new File(tempDir, "SourceProject");
        source.mkdir();

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        //import once
        ProjectHandler.importProject(source, targetParent);

        //second import should throw
        assertThrows(IOException.class, () -> ProjectHandler.importProject(source, targetParent));
    }

    @Test
    void importProject_doesNotOverwriteExistingMetadata() throws IOException {
        File source = new File(tempDir, "SourceProject");
        source.mkdir();
        //source already has a metadata file
        File existingMetadata = new File(source, ".metadata");
        existingMetadata.createNewFile();
        Files.writeString(existingMetadata.toPath(), "existing");

        File targetParent = new File(tempDir, "workspace");
        targetParent.mkdir();

        File result = ProjectHandler.importProject(source, targetParent);
        File copiedMetadata = new File(result, ".metadata");

        assertTrue(copiedMetadata.exists());
        assertEquals("existing", Files.readString(copiedMetadata.toPath()));
    }
}
