package zenit.filesystem.jreversions;

import java.io.File;

import zenit.Zenit;

public class JDKVerifier {
	
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
