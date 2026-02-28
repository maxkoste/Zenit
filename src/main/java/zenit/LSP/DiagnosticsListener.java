package zenit.LSP;

import java.util.List;

/**
 * Callback interface that receives parsed diagnostics from LspManager.
 *
 * MainController routes the diagnostics
 * to the correct FileTab based on the file URI.
 * @author Trumic
 */
public interface DiagnosticsListener {

    /**
     * Called on the thread that reads LSP output (NOT the JavaFX thread).
     * Implementations must wrap GUI updates in Platform.runLater().
     *
     * @param fileUri      The file URI from the LSP message, e.g.
     *                     "file:///C:/Users/.../MyClass.java"
     * @param diagnostics  List of diagnostics for that file.
     *                     Empty list means all previous errors are cleared.
     */
    void onDiagnostics(String fileUri, List<LspDiagnostic> diagnostics);
}