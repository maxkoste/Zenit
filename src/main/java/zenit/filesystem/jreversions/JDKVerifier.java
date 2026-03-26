package zenit.filesystem.jreversions;

import java.io.File;

import zenit.Zenit;

/**
 * Utility class for verifying the validity of a JDK installation.
 * Supports macOS, Windows, and Linux/Unix operating systems.
 *
 * @author Rasmus Axelsson
 */
public class JDKVerifier {

	/**
	 * Checks whether the given directory represents a valid JDK installation
	 * by verifying the presence of both {@code java} and {@code javac} executables.
	 *
	 * @param JDK the directory to validate as a JDK installation
	 * @return {@code true} if the directory contains valid java and javac executables, otherwise returns false.
	 */
	public static boolean validJDK(File JDK) {
		if (JDK == null || !JDK.exists() || !JDK.isDirectory()) {
			return false;
		}

		String javaPath = getExecutablePath(JDK.getPath(), "java");
		String javacPath = getExecutablePath(JDK.getPath(), "javac");

		if (javaPath == null || javacPath == null) {
			return false;
		}

		File java = new File(javaPath);
		File javac = new File(javacPath);

		return java.exists() && javac.exists();
	}

	/**
	 * Constructs the expected file path for a JDK tool executable based on the
	 * current operating system.
	 *
	 * @param JDKPath the root path of the JDK installation
	 * @param tool    the name of the executable (e.g., "java", "javac")
	 * @return the full path to the executable, or null if JDKPath is null/empty or the OS could not be determined
	 */
	public static String getExecutablePath(String JDKPath, String tool) {
		if (JDKPath == null || JDKPath.isEmpty()) {
			return null;
		}

		String OS = System.getProperty("os.name");
		String path = null;


		if (OS.contains("Mac") || OS.contains("Darwin")) {
			path = JDKPath + File.separator + "Contents" + File.separator +
					"Home" + File.separator + "bin" + File.separator + tool;
		} else if (OS.contains("Windows")) {
			path = JDKPath + File.separator + "bin" + File.separator + tool + ".exe";
		} else if (OS.contains("Linux") || OS.contains("Unix")) {
			path = JDKPath + File.separator + "bin" + File.separator + tool;
		}
		
		return path;
	}
}
