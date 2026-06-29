package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactDeployerEntryTest {

    @Test
    void constructorAndGetters_shouldReturnConfiguredValues() {
        ArtifactDeployerEntry entry = new ArtifactDeployerEntry(
                "**/*.jar",
                "target",
                "**/*.tmp",
                "/remote",
                true,
                true,
                true,
                true
        );

        assertEquals("**/*.jar", entry.getIncludes());
        assertEquals("target", entry.getBasedir());
        assertEquals("**/*.tmp", entry.getExcludes());
        assertEquals("/remote", entry.getRemote());
        assertTrue(entry.isFlatten());
        assertTrue(entry.isDeleteRemote());
        assertTrue(entry.isDeleteRemoteArtifacts());
        assertTrue(entry.isFailNoFilesDeploy());
    }

    @Test
    void setters_shouldUpdateAllFields() {
        ArtifactDeployerEntry entry = new ArtifactDeployerEntry();

        entry.setIncludes("a");
        entry.setBasedir("b");
        entry.setExcludes("c");
        entry.setRemote("d");
        entry.setFlatten(true);
        entry.setDeleteRemote(true);
        entry.setDeleteRemoteArtifacts(true);
        entry.setDeleteRemoteArtifactsByScript(true);
        entry.setGroovyExpression("println 'x'");
        entry.setFailNoFilesDeploy(true);

        assertEquals("a", entry.getIncludes());
        assertEquals("b", entry.getBasedir());
        assertEquals("c", entry.getExcludes());
        assertEquals("d", entry.getRemote());
        assertTrue(entry.isFlatten());
        assertTrue(entry.isDeleteRemote());
        assertTrue(entry.isDeleteRemoteArtifacts());
        assertTrue(entry.isFailNoFilesDeploy());
    }

    @Test
    void getUniqueId_shouldChangeWhenRelevantFieldChanges() {
        ArtifactDeployerEntry entry1 = new ArtifactDeployerEntry("i", "b", "e", "r", true, false, false, false);
        ArtifactDeployerEntry entry2 = new ArtifactDeployerEntry("i", "b", "e", "r", true, false, false, false);

        assertEquals(entry1.getUniqueId(), entry2.getUniqueId());

        entry2.setFailNoFilesDeploy(true);
        assertNotEquals(entry1.getUniqueId(), entry2.getUniqueId());
    }

    @Test
    void readObject_shouldMapLegacyDeletingRemoteFieldToDeleteRemote() throws Exception {
        ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
        entry.setDeleteRemote(false);

        java.lang.reflect.Field deletingRemote = ArtifactDeployerEntry.class.getDeclaredField("deletingRemote");
        deletingRemote.setAccessible(true);
        deletingRemote.setBoolean(entry, true);

        Object returned = entry.readObject();

        assertSame(entry, returned);
        assertTrue(entry.isDeleteRemote());
    }

    @Test
    void readObject_shouldKeepDeleteRemoteWhenLegacyFieldFalse() throws Exception {
        ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
        entry.setDeleteRemote(true);

        java.lang.reflect.Field deletingRemote = ArtifactDeployerEntry.class.getDeclaredField("deletingRemote");
        deletingRemote.setAccessible(true);
        deletingRemote.setBoolean(entry, false);

        entry.readObject();

        assertTrue(entry.isDeleteRemote());
        entry.setDeleteRemote(false);
        assertFalse(entry.isDeleteRemote());
    }
}
