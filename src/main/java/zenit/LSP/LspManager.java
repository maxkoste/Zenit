package zenit.LSP;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private Map<String, Integer> documentVersion = new HashMap<>();
	private volatile boolean ready = false;

	// Callback for parsed diagnostics — set by MainController
	private DiagnosticsListener diagnosticsListener;

	private final Gson gson = new Gson();

	public LspManager() {
		File serverDir = new File("jdt-language-server");
		this.serverPath = serverDir.getAbsolutePath();
		this.workspace = new File(System.getProperty("user.home"));
	}

	/**
	 * @param workspace - The place where the server stores data related to the current workspace
	 */
	public void setWorkspace(File workspace) {
		this.workspace = (workspace != null) ? workspace
				: new File(System.getProperty("user.home"));
	}

	/**
	 * Register a listener that will receive parsed diagnostics.
	 * Call this BEFORE startServer().
	 */
	public void setDiagnosticsListener(DiagnosticsListener listener) {
		this.diagnosticsListener = listener;

		if (workspace != null) {
			this.workspace = new File(workspace, ".zenit");
		} else {
			this.workspace = new File(System.getProperty("user.home"), ".zenit");
		}
	}

	public Process startServer() throws IOException {

		File baseDir = new File(serverPath);
		File pluginsDir = new File(baseDir, "plugins");

		if (!pluginsDir.exists()) {
			throw new RuntimeException("Invalid JDT LS path: plugins folder not found");
		}

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
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		String configFolder = isWindows ? "config_win" : "config_mac";
		String configPath = baseDir.getAbsolutePath() + File.separator + configFolder;

		ProcessBuilder pb = new ProcessBuilder(
				"java",
				"-Declipse.application=org.eclipse.jdt.ls.core.id1",
				"-Dosgi.bundles.defaultStartLevel=4",
				"-Declipse.product=org.eclipse.jdt.ls.core.product",
				"-Dlog.protocol=true",
				"-Dlog.level=ALL",
				"-jar", launcherJar.getAbsolutePath(),
				"-configuration", configPath,
				"-data", workspacePath);

		pb.directory(baseDir);

		Process p = pb.start();
		this.stdin = p.getOutputStream();
		this.ready = false;

		sendInitialize();
		startReading(p.getInputStream());

		return p;
	}

	/**
	 * Reads LSP output on a background thread.
	 * Reads raw bytes to handle Content-Length framing correctly on all platforms.
	 */
	public void startReading(java.io.InputStream is) {
		new Thread(() -> {
			try {
				while (true) {
					// Read headers byte by byte until \r\n\r\n
					int contentLength = -1;
					StringBuilder headerBuf = new StringBuilder();

					int b;
					while ((b = is.read()) != -1) {
						headerBuf.append((char) b);
						String h = headerBuf.toString();
						if (h.endsWith("\r\n\r\n") || h.endsWith("\n\n")) {
							break;
						}
					}

					// Parse Content-Length from headers
					for (String line : headerBuf.toString().split("\r?\n")) {
						if (line.startsWith("Content-Length:")) {
							contentLength = Integer.parseInt(
									line.substring("Content-Length:".length()).trim());
						}
					}

					if (contentLength <= 0) {
						System.out.println("[LSP-READER] No content-length found, headers were: " + headerBuf);
						continue;
					}

					// Read exactly contentLength bytes
					byte[] bodyBytes = new byte[contentLength];
					int read = 0;
					while (read < contentLength) {
						int r = is.read(bodyBytes, read, contentLength - read);
						if (r == -1) break;
						read += r;
					}

					String json = new String(bodyBytes, 0, read, StandardCharsets.UTF_8);
					handleMessage(json);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "lsp-reader").start();
	}

	// Keep old signature for compatibility
	public void startReading(Process p) {
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		startReading(isWindows ? p.getErrorStream() : p.getInputStream());
	}

	private void handleMessage(String json) {
		try {
			JsonReader jsonReader = new JsonReader(new java.io.StringReader(json));
			jsonReader.setLenient(true);
			JsonObject obj = JsonParser.parseReader(jsonReader).getAsJsonObject();

			String method = obj.has("method")
					? obj.get("method").getAsString()
					: "(response id=" + obj.get("id") + ")";

			if (!obj.has("method")) {
				// Svar (response) — kolla om det är initialize-svaret
				if (obj.has("id") && obj.get("id").getAsInt() == 1) {
					System.out.println("[LSP] Initialize response received — server ready");
					this.ready = true;
					try { sendInitialized(); } catch (IOException e) { e.printStackTrace(); }
				}
				return;
			}

			if ("textDocument/publishDiagnostics".equals(method)) {
				parseDiagnostics(obj);
			}

		} catch (Exception e) {
			System.err.println("[LSP] Failed to parse message: " + e.getMessage());
		}
	}

	/**
	 * Parses a publishDiagnostics notification and calls the listener.
	 */
	private void parseDiagnostics(JsonObject obj) {
		if (diagnosticsListener == null) return;

		JsonObject params = obj.getAsJsonObject("params");
		String uri = params.get("uri").getAsString();

		List<LspDiagnostic> result = new ArrayList<>();

		var diagnosticsArr = params.getAsJsonArray("diagnostics");
		for (var element : diagnosticsArr) {
			JsonObject d = element.getAsJsonObject();

			JsonObject range = d.getAsJsonObject("range");
			JsonObject start = range.getAsJsonObject("start");
			JsonObject end   = range.getAsJsonObject("end");

			int startLine = start.get("line").getAsInt();
			int startChar = start.get("character").getAsInt();
			int endLine   = end.get("line").getAsInt();
			int endChar   = end.get("character").getAsInt();

			int severity = d.has("severity") ? d.get("severity").getAsInt() : 1;
			String message = d.get("message").getAsString();

			result.add(new LspDiagnostic(startLine, startChar, endLine, endChar, severity, message));
		}

		System.out.println("[LSP] Diagnostics for " + uri + ": " + result.size() + " items");
		diagnosticsListener.onDiagnostics(uri, result);
	}

	public void sendInitialize() throws IOException {
		String rootUri = workspace.toURI().toString()
				.replace("file:/", "file:///")
				.replace("file:////", "file:///");

		String json = """
            {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"processId":%d,"rootUri":"%s","capabilities":{}}}
            """.strip().formatted(ProcessHandle.current().pid(), rootUri);

		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		String header = "Content-Length: " + bytes.length + "\r\n\r\n";

		stdin.write(header.getBytes(StandardCharsets.UTF_8));
		stdin.write(bytes);
		stdin.flush();
	}

	public void sendInitialized() throws IOException {
		String json = """
                {"jsonrpc":"2.0","method":"initialized","params":{}}
                """.strip();
		sendMessage(json);
	}

	public void sendDidOpen(String filePath, String content) throws IOException {
		String uri = Path.of(filePath).toUri().toString();
		documentVersion.put(uri, 1);

		String escaped = escapeJson(content);

		String json = """
                {"jsonrpc":"2.0","method":"textDocument/didOpen","params":{"textDocument":{"uri":"%s","languageId":"java","version":1,"text":"%s"}}}
                """.strip().formatted(uri, escaped);

		sendMessage(json);
	}

	public void sendDidChange(String filePath, String content) throws IOException {
		String uri = Path.of(filePath).toUri().toString();

		int version = documentVersion.getOrDefault(uri, 1) + 1;
		documentVersion.put(uri, version);

		String escaped = escapeJson(content);

		String json = """
                {"jsonrpc":"2.0","method":"textDocument/didChange","params":{"textDocument":{"uri":"%s","version":%d},"contentChanges":[{"text":"%s"}]}}
                """.strip().formatted(uri, version, escaped);

		sendMessage(json);
	}

	private void sendMessage(String json) throws IOException {
		if (stdin == null || !ready) {
			System.err.println("[LSP] sendMessage skipped — server not ready yet");
			return;
		}
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		String header = "Content-Length: " + bytes.length + "\r\n\r\n";
		stdin.write(header.getBytes(StandardCharsets.UTF_8));
		stdin.write(bytes);
		stdin.flush();
	}

	private String escapeJson(String text) {
		return text
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");
	}

	public OutputStream getStdin() { return this.stdin; }

	public boolean isReady() { return ready; }
}