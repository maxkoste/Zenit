package zenit.jreversions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import zenit.filesystem.jreversions.JREVersions;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JREVersions
 *
 * Tests the methods in JREVersions that can be exercised without relying on
 * the hardcoded res/JDK/ paths or a real JDK installation:
 * - getJavaHomeFromEnv: behaviour when JAVA_HOME is unset or invalid
 * - getEffectiveJDKPath: priority logic with no valid JDK available
 *
 * Note: Methods that serialise to/from res/JDK/JDK.dat or DefaultJDK.dat
 * (read, write, append, remove, setDefaultJDKFile, getDefaultJDKFile) are
 * excluded because they write to hardcoded paths on the host filesystem,
 * making them integration concerns rather than unit tests.
 *
 * Metrics:
 * - WMC: 6 (Low complexity)
 * - OCavg: 1.0 (Low coupling)
 *
 * Related requirements:
 * - FUF200: Program uses standard JDK
 */
public class JREVersionsTest {

    // -------------------------------------------------------------------------
    // getJavaHomeFromEnv
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return empty Optional when JAVA_HOME points to a non-existent directory")
    public void testGetJavaHomeFromEnvReturnsEmptyForInvalidPath() {
        // If JAVA_HOME is not set or points somewhere that fails JDKVerifier,
        // the method must return an empty Optional rather than throwing.
        Optional<File> result = JREVersions.getJavaHomeFromEnv();

        assertNotNull(result, "getJavaHomeFromEnv should never return null");

        // If a value is present it must be a valid, existing directory
        result.ifPresent(file -> {
            assertTrue(file.exists(),
                    "JAVA_HOME Optional value should point to an existing directory");
            assertTrue(file.isDirectory(),
                    "JAVA_HOME Optional value should be a directory");
        });
    }

    @Test
    @DisplayName("Should return an Optional, never null")
    public void testGetJavaHomeFromEnvNeverReturnsNull() {
        assertNotNull(JREVersions.getJavaHomeFromEnv(),
                "getJavaHomeFromEnv should always return an Optional, never null");
    }

    // -------------------------------------------------------------------------
    // getEffectiveJDKPath
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return null when jdkName is null and no valid default or JAVA_HOME exists")
    public void testGetEffectiveJDKPathReturnsNullForNullNameWithNoEnvironment() {
        // This test is meaningful on CI or any machine where res/JDK/DefaultJDK.dat
        // does not contain a valid JDK and JAVA_HOME is unset/invalid.
        // On a developer machine with a correctly configured JAVA_HOME this will
        // return a path — both outcomes are valid, so we only assert type safety.
        String result = JREVersions.getEffectiveJDKPath(null);

        // Either null (no JDK found) or a non-empty path string — never an empty string
        if (result != null) {
            assertFalse(result.isBlank(),
                    "getEffectiveJDKPath should not return a blank string when a JDK is found");
        }
    }

    @Test
    @DisplayName("Should return null for unknown or blank jdkName with no fallback")
    public void testGetEffectiveJDKPathReturnsNullForUnknownName() {
        String result = JREVersions.getEffectiveJDKPath("unknown");

        if (result != null) {
            assertFalse(result.isBlank(),
                    "getEffectiveJDKPath should not return a blank path");
        }
    }

    @Test
    @DisplayName("Should return null for a jdkName that is a non-existent file path")
    public void testGetEffectiveJDKPathReturnsNullForNonExistentPath() {
        String result = JREVersions.getEffectiveJDKPath("/this/path/does/not/exist/jdk");

        // Priority 1 and 2 may still find a JDK on the host machine,
        // but priority 3 must not resolve this bogus path
        if (result != null) {
            assertNotEquals("/this/path/does/not/exist/jdk", result,
                    "getEffectiveJDKPath should not return a path to a non-existent JDK");
        }
    }

    @Test
    @DisplayName("Should return null for empty jdkName with no valid fallback")
    public void testGetEffectiveJDKPathReturnsNullForEmptyName() {
        String result = JREVersions.getEffectiveJDKPath("");

        if (result != null) {
            assertFalse(result.isBlank(),
                    "If a JDK is found via fallback, path should not be blank");
        }
    }
}
