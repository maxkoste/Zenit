package zenit.LSP;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
	private BufferedWriter writer;
	private Map<String, Integer> documentVersion = new HashMap<>();

	// Callback for parsed diagnostics — set by MainController
	private DiagnosticsListener diagnosticsListener;

	private final Gson gson = new Gson();

	public LspManager() {
		File serverDir = new File("jdt-language-server");
		this.serverPath = serverDir.getAbsolutePath();
		System.out.println("[DEBUG] Server is located at: " + serverPath);
		this.workspace = new File(System.getProperty("user.home"));
	}

	public void setWorkspace(File workspace) {
		this.workspace = (workspace != null) ? workspace
				: new File(System.getProperty("user.home"));
	}

	/**
	 * Register a listener that will receive parsed diagnostics.
	 * This calls BEFORE startServer().
	 */
	public void setDiagnosticsListener(DiagnosticsListener listener) {
		this.diagnosticsListener = listener;
	}

	public Process startServer() throws IOException {
		System.out.println("[DEBUG] Starting LSP Server from LSPManager");

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
		this.stdin  = p.getOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(stdin));

		sendInitialize();
		startReading(p);   // <-- parsar nu JSON

		return p;
	}

	/**
	 * Reads LSP output on a background thread.
	 * Reads raw bytes to handle Content-Length framing correctly on all platforms.
	 */
	public void startReading(Process p) {
		new Thread(() -> {
			try {
				java.io.InputStream is = p.getInputStream(); // efektivare än BufferedReader

				while (true) {
					// --- Read headers byte by byte until \r\n\r\n ---
					int contentLength = -1;
					StringBuilder headerBuf = new StringBuilder();

					int prev = -1;
					int b;
					// Read until we see blank line (\r\n\r\n or \n\n)
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
							contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
						}
					}

					if (contentLength <= 0) {
						System.out.println("[LSP-READER] No content-length found, headers were: " + headerBuf);
						continue;
					}

					// --- Read exactly contentLength bytes ---
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

	private void handleMessage(String json) {
		try {
			JsonReader jsonReader = new JsonReader(new java.io.StringReader(json));
			jsonReader.setLenient(true); // parseString() kraschade ibland
			JsonObject obj = JsonParser.parseReader(jsonReader).getAsJsonObject();

			String method = obj.has("method")
					? obj.get("method").getAsString()
					: "(response id=" + obj.get("id") + ")";
			System.out.println("[LSP-METHOD] " + method); // DEBUG

			if (!obj.has("method")) return;

			if ("textDocument/publishDiagnostics".equals(method)) {
				parseDiagnostics(obj);
			}

		} catch (Exception e) {
			System.err.println("[LSP] Failed to parse message: " + e.getMessage());
		}
	}

	/**
	 * Parses a publishDiagnostics notification and calls the listener.
	 *
	 * JSON structure:
	 * {
	 *   "method": "textDocument/publishDiagnostics",
	 *   "params": {
	 *     "uri": "file:///...",
	 *     "diagnostics": [
	 *       {
	 *         "range": {
	 *           "start": { "line": 0, "character": 8 },
	 *           "end":   { "line": 0, "character": 12 }
	 *         },
	 *         "severity": 1,
	 *         "message": "Syntax error..."
	 *       }
	 *     ]
	 *   }
	 * }
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

	public void sendDidOpen(String filePath, String content) throws IOException {
		String uri = Path.of(filePath).toUri().toString();
		documentVersion.put(uri, 1);

		String escaped = escapeJson(content);

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
                """.formatted(uri, escaped);

		sendMessage(json);
	}

	public void sendDidChange(String filePath, String content) throws IOException {
		System.out.println("[DEBUG] sendDidChange() triggered in LspManager");
		String uri = Path.of(filePath).toUri().toString();

		int version = documentVersion.getOrDefault(uri, 1) + 1;
		documentVersion.put(uri, version);

		String escaped = escapeJson(content);

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

	private void sendMessage(String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		writer.write("Content-Length: " + bytes.length + "\r\n\r\n");
		writer.write(json);
		writer.flush();
	}

	private String escapeJson(String text) {
		return text
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");
	}

	public OutputStream getStdin()       { return this.stdin; }
	public BufferedWriter getWriter()    { return this.writer; }
}