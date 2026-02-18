package zenit.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pty4j.PtyProcess;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

public class JSBridge {
	private final PtyProcess process;
	
	public JSBridge(PtyProcess process){
		this.process = process;
	}

	public void sendInput(String data){
		try {
			process.getOutputStream().write(data.getBytes());
			process.getOutputStream().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void startOutput(WebEngine engine) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String escaped = line.replace("\\", "\\\\").replace("'", "\\'") + "\r\n";
                    Platform.runLater(() -> engine.executeScript("writeFromJava('" + escaped + "')"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
