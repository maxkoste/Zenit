package zenit.javacodecompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandBuilderTest {

    private CommandBuilder compileBuilder;
    private CommandBuilder runBuilder;

    @BeforeEach
    void setUp() {
        compileBuilder = new CommandBuilder(CommandBuilder.COMPILE);
        compileBuilder.setJDK(null);
        runBuilder = new CommandBuilder(CommandBuilder.RUN);
        runBuilder.setJDK(null);
    }

    @AfterEach
    void tearDown() {
        compileBuilder = null;
        runBuilder = null;
    }

    @Test
    void constants_haveExpectedValues() {
        assertEquals("java", CommandBuilder.RUN);
        assertEquals("javac", CommandBuilder.COMPILE);
    }

    @Test
    void generateCommand_notNull() {
        assertNotNull(compileBuilder.generateCommand());
    }

    @Test
    void generateCommand_startsWithTool() {
        assertTrue(compileBuilder.generateCommand().startsWith(CommandBuilder.COMPILE));
    }

    @Test
    void generateCommand_appendsRunPath() {
        compileBuilder.setRunPath("src/Main.java");
        assertTrue(compileBuilder.generateCommand().contains("src/Main.java"));
    }

    @Test
    void setSourcepath_prependsFlag() {
        compileBuilder.setSourcepath("src");
        assertTrue(compileBuilder.generateCommand().contains("-sourcepath src"));
    }


    //directory
    @Test
    void setDirectory_compile_addsDFlag() {
        compileBuilder.setDirectory("bin");
        assertTrue(compileBuilder.generateCommand().contains("-d bin"));
    }

    @Test
    void setDirectory_run_addsCpFlag() {
        runBuilder.setDirectory("bin");
        assertTrue(runBuilder.generateCommand().contains("-cp ./bin"));
    }

    @Test
    void noDirectory_compile_noDFlag() {
        assertFalse(compileBuilder.generateCommand().contains("-d "));
    }


    //VM and program arguments
    @Test
    void setVMArguments_appearsInCommand() {
        runBuilder.setVMArguments("-Xmx512m");
        assertTrue(runBuilder.generateCommand().contains("-Xmx512m"));
    }

    @Test
    void setProgramArguments_appearsAtEnd() {
        runBuilder.setRunPath("Main");
        runBuilder.setProgramArguments("arg1 arg2");
        assertTrue(runBuilder.generateCommand().endsWith("arg1 arg2"));
    }


    //libraries
    @Test
    void noLibraries_noClasspathInCommand() {
        assertFalse(compileBuilder.generateCommand().contains("-cp "));
    }

    @Test
    void internalLibrariesOnly_compile_addsCpFlag() {
        compileBuilder.setInternalLibraries(new String[]{"lib/foo.jar"});
        assertTrue(compileBuilder.generateCommand().contains("-cp lib/foo.jar"));
    }

    @Test
    void externalLibrariesOnly_compile_addsCpFlag() {
        compileBuilder.setExternalLibraries(new String[]{"lib/bar.jar"});
        assertTrue(compileBuilder.generateCommand().contains("-cp lib/bar.jar"));
    }

    @Test
    void mergedLibraries_compile_joinedWithColon() {
        compileBuilder.setInternalLibraries(new String[]{"lib/a.jar"});
        compileBuilder.setExternalLibraries(new String[]{"lib/b.jar"});
        assertTrue(compileBuilder.generateCommand().contains("lib/a.jar:lib/b.jar"));
    }

    @Test
    void multipleInternalLibraries_compile_allPresent() {
        compileBuilder.setInternalLibraries(new String[]{"lib/a.jar", "lib/b.jar", "lib/c.jar"});
        String cmd = compileBuilder.generateCommand();
        assertTrue(cmd.contains("lib/a.jar"));
        assertTrue(cmd.contains("lib/b.jar"));
        assertTrue(cmd.contains("lib/c.jar"));
    }

    @Test
    void libraries_run_appendsDotSeparatorPrefix() {
        runBuilder.setInternalLibraries(new String[]{"lib/foo.jar"});
        assertTrue(runBuilder.generateCommand().contains("." + File.separator + "lib/foo.jar"));
    }

    @Test
    void libraries_run_endsWithColonDot() {
        runBuilder.setInternalLibraries(new String[]{"lib/foo.jar"});
        assertTrue(runBuilder.generateCommand().contains(":."));
    }

    // --- ordering ---

    @Test
    void generateCommand_directoryBeforeSourcepath() {
        compileBuilder.setDirectory("bin");
        compileBuilder.setSourcepath("src");
        String cmd = compileBuilder.generateCommand();
        assertTrue(cmd.indexOf("-d bin") < cmd.indexOf("-sourcepath src"));
    }

    @Test
    void generateCommand_runPathBeforeProgramArguments() {
        runBuilder.setRunPath("Main");
        runBuilder.setProgramArguments("--verbose");
        String cmd = runBuilder.generateCommand();
        assertTrue(cmd.indexOf("Main") < cmd.indexOf("--verbose"));
    }

    @Test
    void runPath_run_backslashesReplacedWithForwardSlash() {
        runBuilder.setRunPath("com" + File.separator + "example" + File.separator + "Main");
        String cmd = runBuilder.generateCommand();
        assertFalse(cmd.contains("\\"));
        assertTrue(cmd.contains("com/example/Main"));
    }
}
