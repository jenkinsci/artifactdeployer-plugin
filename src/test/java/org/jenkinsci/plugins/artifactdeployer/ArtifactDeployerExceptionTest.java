package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ArtifactDeployerExceptionTest {

    @Test
    void defaultConstructor_shouldCreateExceptionWithoutMessageOrCause() {
        ArtifactDeployerException ex = new ArtifactDeployerException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void messageConstructor_shouldStoreMessage() {
        ArtifactDeployerException ex = new ArtifactDeployerException("boom");
        assertEquals("boom", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void messageAndCauseConstructor_shouldStoreBoth() {
        RuntimeException cause = new RuntimeException("cause");
        ArtifactDeployerException ex = new ArtifactDeployerException("boom", cause);

        assertEquals("boom", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void causeConstructor_shouldStoreCause() {
        RuntimeException cause = new RuntimeException("cause");
        ArtifactDeployerException ex = new ArtifactDeployerException(cause);

        assertSame(cause, ex.getCause());
        assertEquals(cause.toString(), ex.getMessage());
    }
}
