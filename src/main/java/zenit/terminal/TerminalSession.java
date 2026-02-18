package zenit.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import javafx.scene.web.WebEngine;

// TODO: Spawn a shell process using PtyProcess
// TODO: Handle in/out streams
// TODO: Manage process lifecycle

public class TerminalSession {

	private WebEngine engine;
	private PtyProcess process;
	private Thread outputThread;

	public TerminalSession(WebEngine engine){
		this.engine = engine;
	}

	public void start(){
        try {
			String[] shell = System.getProperty("os.name").toLowerCase().contains("win") 
			? new String[]{"cmd.exe"} 
			: new String[]{"/bin/bash"};

            PtyProcessBuilder builder = new PtyProcessBuilder(shell)
                    .setEnvironment(System.getenv())
                    .setDirectory(System.getProperty("user.home"));

            process = builder.start();

            startOutputThread();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private void startOutputThread(){
		new Thread(() ->{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
				String line;
				while((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
}
