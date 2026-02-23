package zenit.terminal;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class TerminalInstance {

	private AnchorPane container;
	private WebView webView;
	private TerminalSession session;
	
	public TerminalInstance(AnchorPane container, WebView webView){
		this.container = container;
		this.webView = webView;
	}

	public void setSession(TerminalSession session){
		this.session = session;
	}

	public WebView getWebView(){
		return this.webView;
	}

	public AnchorPane getContainer(){
		return this.container;
	}

	public TerminalSession getSession(){
		if (this.session != null) {
			return this.session;
		} else return null;
	}
}
