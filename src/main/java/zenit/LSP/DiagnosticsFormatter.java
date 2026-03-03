package zenit.LSP;

import java.util.ArrayList;
import java.util.List;

/**
 * Formats LSP diagnostics into readable strings.
 *
 */
public class DiagnosticsFormatter {

    /**
     * Converts a list of diagnostics to display strings.
     *
     * @param fileName    e.g. "TestClass.java"
     * @param diagnostics list from LSP
     * @return list of formatted strings ready for ListView
     */
    public List<String> format(String fileName, List<LspDiagnostic> diagnostics) {
        List<String> result = new ArrayList<>();

        if (diagnostics == null || diagnostics.isEmpty()) {
            result.add("✓  No problems in " + fileName);
            return result;
        }

        for (LspDiagnostic d : diagnostics) {
            result.add(formatSingle(d));
        }

        return result;
    }

    /**
     * Formats a single diagnostic to a display string.
     * Separate method so it can be tested individually.
     *
     * @param d the diagnostic
     * @return for ex. "✗  Line 4, Col 11:  Syntax error on token..."
     */
    public String formatSingle(LspDiagnostic d) {
        String icon = d.isError() ? "✗" : "⚠";
        return String.format("%s  Line %d, Col %d:  %s",
                icon,
                d.getStartLine() + 1,
                d.getStartChar() + 1,
                d.getMessage());
    }
}