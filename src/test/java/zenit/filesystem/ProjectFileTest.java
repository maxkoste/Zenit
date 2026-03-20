package zenit.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectFileTest {

    @TempDir
    File tempDir;

    private ProjectFile projectFile;

    @BeforeEach
    void setUp() {
        projectFile = new ProjectFile(tempDir);
    }

    @AfterEach
    void tearDown() {
        projectFile = null;
    }

    //constructors

    @Test
    void constructor_fromPath_createsProjectFile() {
        ProjectFile pf = new ProjectFile(tempDir.getPath());
        assertEquals(tempDir.getPath(), pf.getPath());
    }

    @Test
    void constructor_fromFile_createsProjectFile() {
        assertEquals(tempDir.getPath(), projectFile.getPath());
    }

    //add src

    @Test
    void addSrc_createsSrcFolder() {
        File src = projectFile.addSrc();
        assertNotNull(src);
        assertTrue(src.exists());
        assertTrue(src.isDirectory());
    }

    @Test
    void addSrc_folderNamedSrc() {
        File src = projectFile.addSrc();
        assertEquals("src", src.getName());
    }

    @Test
    void addSrc_calledTwice_returnsSameFolder() {
        File first = projectFile.addSrc();
        File second = projectFile.addSrc();
        assertEquals(first.getPath(), second.getPath());
    }

    //get src

    @Test
    void getSrc_afterAddSrc_returnsFolder() {
        projectFile.addSrc();
        assertNotNull(projectFile.getSrc());
    }

    @Test
    void getSrc_findsExistingSrcOnDisk() {
        new File(tempDir, "src").mkdir();
        ProjectFile freshPF = new ProjectFile(tempDir);
        assertNotNull(freshPF.getSrc());
    }

    //add bin

    @Test
    void addBin_createsBinFolder() {
        File bin = projectFile.addBin();
        assertNotNull(bin);
        assertTrue(bin.exists());
        assertTrue(bin.isDirectory());
    }

    @Test
    void addBin_folderNamedBin() {
        File bin = projectFile.addBin();
        assertEquals("bin", bin.getName());
    }

    @Test
    void addBin_calledTwice_returnsSameFolder() {
        File first = projectFile.addBin();
        File second = projectFile.addBin();
        assertEquals(first.getPath(), second.getPath());
    }

    //get bin

    @Test
    void getBin_afterAddBin_returnsFolder() {
        projectFile.addBin();
        assertNotNull(projectFile.getBin());
    }

    @Test
    void getBin_findsExistingBinOnDisk() {
        new File(tempDir, "bin").mkdir();
        ProjectFile freshPF = new ProjectFile(tempDir);
        assertNotNull(freshPF.getBin());
    }

    //add lib

    @Test
    void addLib_createsLibFolder() {
        File lib = projectFile.addLib();
        assertNotNull(lib);
        assertTrue(lib.exists());
        assertTrue(lib.isDirectory());
    }

    @Test
    void addLib_folderNamedLib() {
        File lib = projectFile.addLib();
        assertEquals("lib", lib.getName());
    }

    @Test
    void addLib_calledTwice_returnsSameFolder() {
        File first = projectFile.addLib();
        File second = projectFile.addLib();
        assertEquals(first.getPath(), second.getPath());
    }


    @Test
    void getLib_beforeAddLib_returnsNull() {
        assertNull(projectFile.getLib());
    }

    @Test
    void setLib_updatesLib() {
        File lib = new File(tempDir, "lib");
        projectFile.setLib(lib);
        assertEquals(lib, projectFile.getLib());
    }

    //metadata

    @Test
    void setMetadata_getMetadata_returnsSetFile() {
        File metadata = new File(tempDir, ".metadata");
        projectFile.setMetadata(metadata);
        assertEquals(metadata, projectFile.getMetadata());
    }

    @Test
    void getMetadata_findsExistingMetadataOnDisk() throws Exception {
        new File(tempDir, ".metadata").createNewFile();
        ProjectFile freshPF = new ProjectFile(tempDir);
        assertNotNull(freshPF.getMetadata());
    }

    @Test
    void getMetadata_noMetadataOnDisk_returnsNull() {
        ProjectFile freshPF = new ProjectFile(tempDir);
        assertNull(freshPF.getMetadata());
    }
}
