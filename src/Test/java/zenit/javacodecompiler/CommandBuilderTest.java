package zenit.javacodecompiler;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CommandBuilder.
 *
 * Related requirements:
 * - FUI102: Run button should work with libraries
 * - FCC100: Compile and execute selected java file
 * - FCC101/FCC102: Compile with internal/external destination directory
 * - FCC103/FCC104: Compile with external/internal source path
 * - FCC105/FCC106: Compile with internal/external libraries
 */
public class CommandBuilderTest {

	@Test
	@DisplayName("FCC100: compile command should include selected java file")
	public void shouldBuildCompileCommandForSelectedJavaFile() {
		CommandBuilder cb = new CommandBuilder(CommandBuilder.COMPILE);
		cb.JDK = CommandBuilder.COMPILE;
		cb.setRunPath("src/main/java/com/example/Main.java");

		String command = cb.generateCommand();

		assertEquals("javac src/main/java/com/example/Main.java", command);
	}

	@Test
	@DisplayName("FCC101/FCC102: compile should support destination directories")
	public void shouldSupportInternalAndExternalDestinationDirectories() {
		CommandBuilder internal = new CommandBuilder(CommandBuilder.COMPILE);
		internal.JDK = CommandBuilder.COMPILE;
		internal.setDirectory("bin");
		internal.setRunPath("src/App.java");

		CommandBuilder external = new CommandBuilder(CommandBuilder.COMPILE);
		external.JDK = CommandBuilder.COMPILE;
		external.setDirectory("C:/tmp/out");
		external.setRunPath("src/App.java");

		assertTrue(internal.generateCommand().contains(" -d bin"));
		assertTrue(external.generateCommand().contains(" -d C:/tmp/out"));
	}

	@Test
	@DisplayName("FCC103/FCC104: compile should support sourcepath variants")
	public void shouldSupportInternalAndExternalSourcePaths() {
		CommandBuilder internal = new CommandBuilder(CommandBuilder.COMPILE);
		internal.JDK = CommandBuilder.COMPILE;
		internal.setSourcepath("src/main/java");
		internal.setRunPath("src/main/java/Main.java");

		CommandBuilder external = new CommandBuilder(CommandBuilder.COMPILE);
		external.JDK = CommandBuilder.COMPILE;
		external.setSourcepath("C:/workspace/shared-src");
		external.setRunPath("C:/workspace/shared-src/Main.java");

		assertTrue(internal.generateCommand().contains(" -sourcepath src/main/java "));
		assertTrue(external.generateCommand().contains(" -sourcepath C:/workspace/shared-src "));
	}

	@Test
	@DisplayName("FCC105/FCC106: compile should merge internal and external libraries")
	public void shouldMergeInternalAndExternalLibrariesForCompile() {
		CommandBuilder cb = new CommandBuilder(CommandBuilder.COMPILE);
		cb.JDK = CommandBuilder.COMPILE;
		cb.setInternalLibraries(new String[] {"lib/internal-a.jar", "lib/internal-b.jar"});
		cb.setExternalLibraries(new String[] {"C:/deps/external-c.jar"});
		cb.setRunPath("src/Main.java");

		String command = cb.generateCommand();

		assertTrue(command.contains(" -cp lib/internal-a.jar:lib/internal-b.jar:C:/deps/external-c.jar "));
	}

	@Test
	@DisplayName("FUI102/FCC100: run should include classpath, args and normalized run path")
	public void shouldBuildRunCommandWithLibrariesAndArguments() {
		CommandBuilder cb = new CommandBuilder(CommandBuilder.RUN);
		cb.JDK = CommandBuilder.RUN;
		cb.setDirectory("bin");
		cb.setInternalLibraries(new String[] {"core.jar"});
		cb.setExternalLibraries(new String[] {"ext.jar"});
		cb.setVMArguments("-Xmx512m");
		cb.setRunPath("src" + File.separator + "com" + File.separator + "example" + File.separator + "Main");
		cb.setProgramArguments("--port 8080");

		String command = cb.generateCommand();

		assertTrue(command.startsWith("java -Xmx512m -cp ./bin"));
		assertTrue(command.contains(":." + File.separator + "core.jar"));
		assertTrue(command.contains(":." + File.separator + "ext.jar"));
		assertTrue(command.contains(":."));
		assertTrue(command.contains(" src/com/example/Main "));
		assertTrue(command.endsWith("--port 8080"));
	}
}
