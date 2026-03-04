package zenit.TerminalTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import zenit.terminal.TerminalSession;


@ExtendWith(ApplicationExtension.class)
class TerminalSessionTest{
	private TerminalSession session;

	@Start
	private void start(Stage stage) throws Exception {
		
		WebView webView = new WebView();
		webView.setFocusTraversable(true);
		webView.requestFocus();

		WebEngine engine = webView.getEngine();
		engine.load(
			getClass()
			.getResource("/xterm/index.html")
			.toExternalForm()
		);

		this.session = new TerminalSession(engine);
	}

    @Test
    void testSetCurrWorkspace() {
		String rslt = session.setCurrWorkspace(new File("/tmp"));
		assertEquals("/tmp", rslt);
    }

    @Test
    void testOutputThread() throws Exception {

		session.start();
		Process p = this.session.getProcess();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(()->{
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {

				for (int i = 0; i < 8000; i++) {
					bw.write("hello" + i + "\n");
				}
				bw.flush();

				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		try {
			future.get(6, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail("Terminal Thread froze or timed out");
		} catch (Exception e){
			e.printStackTrace();
		}
		
		executor.shutdownNow();

		session.stop();
    }

    @Test
    void testStopDestroysProcess() {
		session.start();

		assertEquals(1, session.stop());
    }
}
