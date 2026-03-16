const container = document.getElementById('terminal');

window.term = new Terminal({
    cursorBlink: true,
    theme: {
        background:      '#282c34', // Base
        foreground:      '#abb2bf', // Text
        cursor:          '#abb2bf', // Text
        cursorAccent:    '#282c34', // Base
        selectionForeground: '#abb2bf',
        selectionBackground: '#3e4451', // Selection

        // ANSI colors
        black:           '#282c34', // Base
        red:             '#e06c75', // Variable
        green:           '#98c379', // String
        yellow:          '#e5c07b', // Yellow
        blue:            '#61afef', // Function
        magenta:         '#c678dd', // Keyword
        cyan:            '#56b6c2', // Cyan
        white:           '#abb2bf', // Text

        // Bright ANSI colors
        brightBlack:     '#5c6370', // Subtle
        brightRed:       '#e06c75', // Variable
        brightGreen:     '#98c379', // String
        brightYellow:    '#e5c07b', // Yellow
        brightBlue:      '#61afef', // Function
        brightMagenta:   '#c678dd', // Keyword
        brightCyan:      '#56b6c2', // Cyan
        brightWhite:     '#ffffff', // White
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
