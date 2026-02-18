package zenit.terminal;

import com.pty4j.PtyProcess;


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

}
