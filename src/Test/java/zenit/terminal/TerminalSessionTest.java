package zenit.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TerminalSessionTest {

	// Requirement: A newly created session should not have a process before start().
	// Expected behavior: getProcess returns null.
	@Test
	public void testNewSessionShouldHaveNullProcessBeforeStart() {
		TerminalSession session = new TerminalSession(null);
		assertNull(session.getProcess());
	}

	// Requirement: Calling stop() without a started process should be safe.
	// Expected behavior: method call does not throw.
	@Test
	public void testStopWithoutStartedProcessShouldNotThrow() {
		TerminalSession session = new TerminalSession(null);

		try {
			session.stop();
		} catch (Exception e) {
			fail("stop should not throw when process is null, but threw: " + e);
		}
	}
}
