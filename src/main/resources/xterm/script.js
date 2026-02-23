const container = document.getElementById('terminal');

window.term = new Terminal({
	cursorBlink: true,
	theme: {
		background:"#000000"
	}
});

term.open(container);

// Make div focusable
container.setAttribute("tabindex", "0");

window.addEventListener("load", () => {
	container.focus();
	term.focus();
});

container.addEventListener("click", () => {
	term.focus();
});

term.onData((data) => {
	if (window.javaConnector) {
		window.javaConnector.sendInput(data);
	}
});

function writeFromJava(output) {
	term.write(output);
}

function resizeTerminal(){
	const cols = term.cols;
	const rows = term.rows;

	if (window.javaConnector) {
		window.javaConnector.resize(cols,rows);
	}
}

term.onResize(resizeTerminal);
