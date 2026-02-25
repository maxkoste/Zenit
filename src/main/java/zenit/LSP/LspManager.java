package zenit.LSP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class LspManager {
	private String serverPath;
	private File workspace;
	private OutputStream stdin;
	private BufferedWriter writer;

	public LspManager() {
		this.serverPath = "/Users/maxkoste/Dev/Java/jdt-language-server";
		this.workspace = new File(System.getProperty("user.home"));
	}

	public void setWorkspace(File workspace) {
		if (workspace != null) {
			this.workspace = workspace;
		} else {
			// set defualt.
			this.workspace = new File(System.getProperty("user.home"));
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
			"-data", workspacePath);

		pb.directory(baseDir);

		Process p = pb.start();
		this.stdin = p.getOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(stdin));

		sendInitialize();

		startReading(p);

		return p;
	}

	public void startReading(Process p){
		new Thread(()->{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ((line=reader.readLine()) != null) {
					System.out.println("[LSP OUTPUT] " + line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void sendInitialize() throws IOException {
		String json = """
		{
		"jsonrpc": "2.0",
		"id": 1,
		"method": "initialize",
		"params": {
		"processId": %d,
		"rootUri": "%s",
		"capabilities": {}
		}
		}
		""".formatted(
			ProcessHandle.current().pid(),
			workspace.toURI().toString()
		);

		String message = "Content-Length: " + json.getBytes().length + "\r\n\r\n" + json;

		writer.write(message);
		writer.flush();
	}

	public void readFile(){
		//TODO: Read a file and see the diagnostics
	}

	public OutputStream getStdin() {
		return this.stdin;
	}

	public BufferedWriter getWriter() {
		return this.writer;
	}
}
