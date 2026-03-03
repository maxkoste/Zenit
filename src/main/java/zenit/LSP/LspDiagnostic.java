package zenit.LSP;

/**
 * Represents a single diagnostic (error/warning) received from the LSP server
 * via textDocument/publishDiagnostics.
 * Which row and column error begins and ends
 * @author Trumic
 */
public class LspDiagnostic {

    private final int startLine;
    private final int startChar;
    private final int endLine;
    private final int endChar;
    private final int severity;
    private final String message;

    public LspDiagnostic(int startLine, int startChar, int endLine, int endChar,
                         int severity, String message) {
        this.startLine = startLine;
        this.startChar = startChar;
        this.endLine   = endLine;
        this.endChar   = endChar;
        this.severity  = severity;
        this.message   = message;
    }

    public int getStartLine() { return startLine; }
    public int getStartChar() { return startChar; }
    public int getEndLine()   { return endLine; }
    public int getEndChar()   { return endChar; }
    public int getSeverity()  { return severity; }
    public String getMessage(){ return message; }

    public boolean isError()   { return severity == 1; }
    public boolean isWarning() { return severity == 2; }

    /**
     * Severity label for UI display.
     */
    public String getSeverityLabel() {
        return switch (severity) {
            case 1 -> "Error";
            case 2 -> "Warning";
            case 3 -> "Info";
            case 4 -> "Hint";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return String.format("[%s] Line %d:%d - %s",
                getSeverityLabel(), startLine + 1, startChar + 1, message);
    }
}