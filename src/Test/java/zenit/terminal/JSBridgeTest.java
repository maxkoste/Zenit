package zenit.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class JSBridgeTest {

	// Requirement: Sending input should not crash even if bridge has no process yet.
	// Expected behavior: method call does not throw.
	@Test
	public void testSendInputWithNullProcessShouldNotThrow() {
		JSBridge bridge = new JSBridge(null);

		try {
			bridge.sendInput("echo test\n");
		} catch (Exception e) {
			fail("sendInput should not throw when process is null, but threw: " + e);
		}
	}

	// Requirement: Resize should be safe even if process is unavailable.
	// Expected behavior: method call does not throw.
	@Test
	public void testResizeWithNullProcessShouldNotThrow() {
		JSBridge bridge = new JSBridge(null);

		try {
			bridge.resize(120, 30);
		} catch (Exception e) {
			fail("resize should not throw when process is null, but threw: " + e);
		}
	}
}
