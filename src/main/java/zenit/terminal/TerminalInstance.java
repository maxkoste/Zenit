package zenit.terminal;

import java.io.File;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

/**
 * Wrapper class for the terminal
 *
 * @author Max Koste
 */
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

	public void setCurrWorkspace(File currWorkspace){
		if (this.session != null) {
			System.out.println("[DEBUG TerminalInstance] This session is null, can't set current workspace");
			this.session.setCurrWorkspace(currWorkspace);
		}
	}
}
