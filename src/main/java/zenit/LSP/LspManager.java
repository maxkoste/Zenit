package zenit.LSP;

import java.io.File;
import java.io.IOException;

public class LspManager {
	private String serverPath;
	private File workspace;

	public LspManager(){
		this.serverPath = "/Users/maxkoste/Dev/Java/jdt-language-server";
		this.workspace =new File( System.getProperty("user.home"));
	}

	public void setWorkspace(File workspace){
		if (workspace != null) {
			this.workspace = workspace;
		} else {
			//set defualt.
			this.workspace =new File( System.getProperty("user.home"));
		}
	}

  public Process startServer() throws IOException {
		System.out.println("[DEBUG] Starting LSP Server from LSPManager");

        File baseDir = new File(serverPath);
        File pluginsDir = new File(baseDir, "plugins");

        if (!pluginsDir.exists()) {
            throw new RuntimeException("Invalid JDT LS path: plugins folder not found");
        }

        // Find equinox launcher jar
        File launcherJar = null;
        for (File file : pluginsDir.listFiles()) {
            if (file.getName().startsWith("org.eclipse.equinox.launcher_")
                    && file.getName().endsWith(".jar")) {
                launcherJar = file;
                break;
            }
        }

        if (launcherJar == null) {
            throw new RuntimeException("Launcher jar not found in plugins folder");
        }

		String workspacePath = workspace.getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                "-Dosgi.bundles.defaultStartLevel=4",
                "-Declipse.product=org.eclipse.jdt.ls.core.product",
                "-Dlog.protocol=true",
                "-Dlog.level=ALL",
                "-jar", launcherJar.getAbsolutePath(),
                "-configuration", baseDir.getAbsolutePath() + "/config_mac",
                "-data", workspacePath
        );

        pb.directory(baseDir);
        pb.inheritIO(); // show logs in console

        return pb.start();
    }
	
}
