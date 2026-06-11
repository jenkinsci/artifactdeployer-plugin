package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.Util;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalCopyTest {

    @TempDir
    Path tempDir;

    @Test
    void copyAndGetNumbers_shouldCopyMatchingFiles() throws Exception {
        Path sourceDir = Files.createDirectories(tempDir.resolve("src"));
        Path targetDir = Files.createDirectories(tempDir.resolve("target"));
        Files.writeString(sourceDir.resolve("a.txt"), "alpha");
        Files.writeString(sourceDir.resolve("b.log"), "beta");

        FileSet fileSet = Util.createFileSet(sourceDir.toFile(), "**/*.txt", null);

        LocalCopy localCopy = new LocalCopy();
        List<File> copied = localCopy.copyAndGetNumbers(fileSet, false, targetDir.toFile());

        assertEquals(1, copied.size());
        assertTrue(Files.exists(targetDir.resolve("a.txt")));
        assertEquals("a.txt", copied.get(0).getName());
    }

    @Test
    void copyAndGetNumbers_whenTargetIsFile_shouldWrapBuildException() throws Exception {
        Path sourceDir = Files.createDirectories(tempDir.resolve("src2"));
        Path targetFile = Files.writeString(tempDir.resolve("target-file.txt"), "not a directory");
        Files.writeString(sourceDir.resolve("a.txt"), "alpha");

        FileSet fileSet = Util.createFileSet(sourceDir.toFile(), "**/*.txt", null);
        LocalCopy localCopy = new LocalCopy();

        ArtifactDeployerException exception = assertThrows(
                ArtifactDeployerException.class,
                () -> localCopy.copyAndGetNumbers(fileSet, false, targetFile.toFile())
        );

        assertEquals("Error on copying file.", exception.getMessage());
    }

    @Test
    void copyAndGetNumbers_withFlatten_shouldCopyFilesIntoTargetFlatly() throws Exception {
        Path sourceDir = Files.createDirectories(tempDir.resolve("src3"));
        Path subDir = Files.createDirectories(sourceDir.resolve("sub"));
        Path targetDir = Files.createDirectories(tempDir.resolve("target3"));
        Files.writeString(subDir.resolve("nested.txt"), "nested content");

        FileSet fileSet = Util.createFileSet(sourceDir.toFile(), "**/*.txt", null);
        LocalCopy localCopy = new LocalCopy();
        List<File> copied = localCopy.copyAndGetNumbers(fileSet, true, targetDir.toFile());

        assertEquals(1, copied.size());
        assertTrue(Files.exists(targetDir.resolve("nested.txt")));
        assertEquals("nested.txt", copied.get(0).getName());
    }
}