package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.FilePath;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtifactDeployerManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void getBasedirFilePath_whenBasedirNull_shouldReturnWorkspace() {
        ArtifactDeployerManager manager = new ArtifactDeployerManager();
        FilePath workspace = new FilePath(tempDir.toFile());

        FilePath result = manager.getBasedirFilePath(workspace, null);

        assertSame(workspace, result);
    }

    @Test
    void getBasedirFilePath_whenChildExists_shouldReturnChild() throws Exception {
        ArtifactDeployerManager manager = new ArtifactDeployerManager();
        FilePath workspace = new FilePath(tempDir.toFile());
        Files.createDirectories(tempDir.resolve("dist"));

        FilePath result = manager.getBasedirFilePath(workspace, "dist");

        assertEquals(workspace.child("dist").getRemote(), result.getRemote());
    }

    @Test
    void getBasedirFilePath_whenChildMissing_shouldThrowHelpfulException() {
        ArtifactDeployerManager manager = new ArtifactDeployerManager();
        FilePath workspace = new FilePath(tempDir.toFile());

        ArtifactDeployerException exception = assertThrows(
                ArtifactDeployerException.class,
                () -> manager.getBasedirFilePath(workspace, "missing")
        );

        assertEquals("The basedir path 'missing' from the workspace doesn't exist.", exception.getMessage());
    }
}