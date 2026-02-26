package zenit.terminal;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JSBridge
 * Testar att input skickas korrekt till PtyProcess och att fönsterstorlek ändras rätt.
 */
class JSBridgeTest {

    private PtyProcess mockProcess;
    private OutputStream mockOutputStream;
    private JSBridge jsBridge;

    @BeforeEach
    void setUp() throws IOException {
        mockProcess = mock(PtyProcess.class);
        mockOutputStream = mock(OutputStream.class);
        when(mockProcess.getOutputStream()).thenReturn(mockOutputStream);
        jsBridge = new JSBridge(mockProcess);
    }

    // sendInput

    @Test
    @DisplayName("sendInput skriver korrekt data till processens outputstream")
    void testSendInput_writesCorrectBytes() throws IOException {
        String input = "ls -la\n";
        jsBridge.sendInput(input);

        verify(mockOutputStream).write(input.getBytes());
        verify(mockOutputStream).flush();
    }

    @Test
    @DisplayName("sendInput med tom sträng skriver tom byte-array")
    void testSendInput_emptyString() throws IOException {
        jsBridge.sendInput("");

        verify(mockOutputStream).write("".getBytes());
        verify(mockOutputStream).flush();
    }

    @Test
    @DisplayName("sendInput med null kastar inte exception utan hanteras")
    void testSendInput_nullInput() {
        // Förväntat beteende: metoden bör inte krascha hårt – IOException fångas internt
        assertDoesNotThrow(() -> jsBridge.sendInput(null));
    }

    @Test
    @DisplayName("sendInput anropar flush exakt en gång per anrop")
    void testSendInput_flushCalledOnce() throws IOException {
        jsBridge.sendInput("hello");
        verify(mockOutputStream, times(1)).flush();
    }

    @Test
    @DisplayName("sendInput: IOException från write hanteras utan att kasta exception")
    void testSendInput_ioExceptionHandled() throws IOException {
        doThrow(new IOException("Write failed")).when(mockOutputStream).write(any(byte[].class));

        // Ska inte propagera – exception fångas i catch-blocket
        assertDoesNotThrow(() -> jsBridge.sendInput("test"));
    }

    // resize

    @Test
    @DisplayName("resize anropar setWinSize med rätt kolumner och rader")
    void testResize_correctWinSize() {
        jsBridge.resize(80, 24);

        ArgumentCaptor<WinSize> captor = ArgumentCaptor.forClass(WinSize.class);
        verify(mockProcess).setWinSize(captor.capture());

        WinSize captured = captor.getValue();
        assertEquals(80, captured.getColumns());
        assertEquals(24, captured.getRows());
    }

    @Test
    @DisplayName("resize med minimala värden (1x1) fungerar")
    void testResize_minimalValues() {
        jsBridge.resize(1, 1);
        verify(mockProcess).setWinSize(any(WinSize.class));
    }

    @Test
    @DisplayName("resize med stora värden fungerar")
    void testResize_largeValues() {
        jsBridge.resize(500, 200);
        verify(mockProcess).setWinSize(any(WinSize.class));
    }

    @Test
    @DisplayName("resize med noll-värden kastar inte exception")
    void testResize_zeroValues() {
        assertDoesNotThrow(() -> jsBridge.resize(0, 0));
    }
}