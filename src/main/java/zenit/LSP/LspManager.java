package zenit.LSP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * OBS! This code only works if you've downloaded and installed the
 * jdt-language-server and unpacked it at the location specified by this code
 * that means that you have to change the serverPath variable to match where
 * you unpacked the jdt-language-server
 *
 * @author maxkoste
 */

public class LspManager {
	private String serverPath;
	private File workspace;
	private OutputStream stdin;
	private BufferedWriter writer;
	private Map<String, Integer> documentVersion = new HashMap<>();

	public LspManager() {
		File serverDir = new File("jdt-language-server");
		 
		this.serverPath = serverDir.getAbsolutePath();
		System.out.println("[DEBUG] Server is located at: " + serverPath);
																			// jdt-language-server is unpacked on your
																			// computer

		//defualt
		this.workspace = new File(System.getProperty("user.home"));
	}

	/**
	 * @param workspace - The place where the server stores data related to the current workspace 
	 */
	public void setWorkspace(File workspace) {
		if (workspace != null) {
			this.workspace = new File(workspace, ".zenit");
		} else {
			// set defualt.
			this.workspace = new File(System.getProperty("user.home"), ".zenit");
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

		String osVar = System.getProperty("os.name").toLowerCase().contains("win") 
			? "/config_win" 
			: "/config_mac";

		ProcessBuilder pb = new ProcessBuilder(
				"java",
				"-Declipse.application=org.eclipse.jdt.ls.core.id1",
				"-Dosgi.bundles.defaultStartLevel=4",
				"-Declipse.product=org.eclipse.jdt.ls.core.product",
				"-Dlog.protocol=true",
				"-Dlog.level=ALL",
				"-jar", launcherJar.getAbsolutePath(),
				"-configuration", baseDir.getAbsolutePath() + osVar,
				"-data", workspacePath);

		pb.directory(baseDir);

		Process p = pb.start();
		this.stdin = p.getOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(stdin));

		sendInitialize();

		startReading(p);

		return p;
	}

	public void startReading(Process p) {
		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
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
				workspace.toURI().toString());

		sendMessage(json);
	}

	public void sendInitialized() throws IOException {
		String json = """
				{
				"jsonrpc": "2.0",
				"method": "initialized",
				"params": {}
				}
				""";
		sendMessage(json);
	}

	public void sendDidChange(String filePath, String content) throws IOException {

		System.out.println("[DEBUG] sendDidChange() triggered in LspManager");
		String uri = Path.of(filePath).toUri().toString();

		int version = documentVersion.getOrDefault(uri, 1) + 1;
		documentVersion.put(uri, version);

		String escaped = content
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");

		String json = """
				{
				"jsonrpc": "2.0",
				"method": "textDocument/didChange",
				"params": {
				"textDocument": {
				"uri": "%s",
				"version": %d
				},
				"contentChanges": [
				{
				"text": "%s"
				}
				]
				}
				}
				""".formatted(uri, version, escaped);

		sendMessage(json);
	}

	public void sendDidOpen(String filePath, String content) throws IOException {

		String uri = Path.of(filePath).toUri().toString();
		documentVersion.put(uri, 1);

		String json = """
				{
				"jsonrpc": "2.0",
				"method": "textDocument/didOpen",
				"params": {
				"textDocument": {
				"uri": "%s",
				"languageId": "java",
				"version": 1,
				"text": "%s"
				}
				}
				}
				""".formatted(Path.of(filePath).toUri().toString(), content.replace("\n", "\\n").replace("\"", "\\\""));

		sendMessage(json);
	}

	private void sendMessage(String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		writer.write("Content-Length: " + bytes.length + "\r\n\r\n");
		writer.write(json);
		writer.flush();
	}

	public OutputStream getStdin() {
		return this.stdin;
	}

	public BufferedWriter getWriter() {
		return this.writer;
	}
}
