package zenit.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TerminalInstanceTest {

	// Requirement: Session should be null until explicitly set.
	// Expected behavior: getSession returns null for new instance.
	@Test
	public void testGetSessionReturnsNullWhenNotSet() {
		TerminalInstance instance = new TerminalInstance(null, null);
		assertNull(instance.getSession());
	}

	// Requirement: Instance should keep and return assigned session.
	// Expected behavior: getSession returns the same object passed to setSession.
	@Test
	public void testSetSessionShouldStoreSameReference() {
		TerminalInstance instance = new TerminalInstance(null, null);
		TerminalSession session = new TerminalSession(null);

		instance.setSession(session);

		assertSame(session, instance.getSession());
	}
}
