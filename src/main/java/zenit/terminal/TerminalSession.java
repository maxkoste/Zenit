package zenit.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class TerminalSession {

	private WebEngine engine;
	private PtyProcess process;

	public TerminalSession(WebEngine engine) {
		this.engine = engine;
	}

	public void start() {
		try {
			String[] shell = System.getProperty("os.name").toLowerCase().contains("win")
				? new String[] { "cmd.exe" }
				: new String[] { "/bin/bash" };

			PtyProcessBuilder builder = new PtyProcessBuilder(shell)
				.setEnvironment(System.getenv())
				.setDirectory(System.getProperty("user.home")); //this should be defaulted to the workspace folder

			process = builder.start();

			startOutputThread();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startOutputThread() {
		Thread outputThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

				byte[] buffer = new byte[8192];
				int n;
				while ((n = process.getInputStream().read(buffer)) != -1) {
					String text = new String(buffer, 0, n);

					Platform.runLater(() -> {
						JSObject window = (JSObject) engine.executeScript("window");
						window.call("writeFromJava", text);
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		outputThread.setDaemon(true);
		outputThread.start();
	}

    public PtyProcess getProcess() {
		return this.process;
    }
}
