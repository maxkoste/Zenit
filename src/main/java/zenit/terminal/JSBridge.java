package zenit.terminal;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;

// Author Max Koste
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

	public void resize(int cols, int rows){
		System.out.printf("Resizing Window: Cols: %d Rows: %d \n", cols,  rows);
		process.setWinSize(new WinSize(cols, rows));
	}
}
