package zenit.terminal;

import java.io.File;
import java.io.IOException;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 * @author Max Koste
 */
public class TerminalSession {

	private WebEngine engine;
	private PtyProcess process;
	private File currentWorkspace;

	public TerminalSession(WebEngine engine) {
		this.engine = engine;
	}

	public void setCurrWorkspace(File workspace){
		if (workspace != null) {
			System.out.println("[DEBUG] TerminalSession setting the workspace");
			this.currentWorkspace = workspace;
		} else {
			this.currentWorkspace = new File(System.getProperty("user.home"));
		}
	}

	public void start() {
		try {

			if (currentWorkspace == null) {
				this.currentWorkspace = new File(System.getProperty("user.home"));
			}
			String[] shell = System.getProperty("os.name").toLowerCase().contains("win")
				? new String[] { "cmd.exe" }
				: new String[] { "/bin/bash" };

			PtyProcessBuilder builder = new PtyProcessBuilder(shell)
				.setEnvironment(System.getenv())
				.setDirectory(currentWorkspace.getAbsolutePath());

			process = builder.start();

			startOutputThread();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startOutputThread() {
		Thread outputThread = new Thread(() -> {
			try  {
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

	public void stop() {
		if (this.process != null && this.process.isAlive()) {
			this.process.destroy();
		}
	}
}
