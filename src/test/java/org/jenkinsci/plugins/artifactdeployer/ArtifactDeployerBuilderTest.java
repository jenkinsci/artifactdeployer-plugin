package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactDeployerBuilderTest {

    private ArtifactDeployerBuilder builder;

    // Creates a bare ArtifactDeployerBuilder with no entry configured. This is sufficient
    // for testing isFailNoFilesDeploy and printConfiguration, which receive all their
    // inputs as parameters rather than reading from the builder's entry field.
    @BeforeEach
    void setUp() {
        builder = new ArtifactDeployerBuilder();
    }

    // --- isFailNoFilesDeploy ---

    // When no files were deployed (null results) and the "fail on no files" flag is enabled,
    // the build should be considered a failure — expects true.
    @Test
    void isFailNoFilesDeploy_nullResults_flagEnabled_returnsTrue() {
        ArtifactDeployerEntry entry = entryWithFailFlag(true);
        assertTrue(builder.isFailNoFilesDeploy(null, entry));
    }

    // When no files were deployed (empty list) and the "fail on no files" flag is enabled,
    // the build should be considered a failure — expects true.
    @Test
    void isFailNoFilesDeploy_emptyResults_flagEnabled_returnsTrue() {
        ArtifactDeployerEntry entry = entryWithFailFlag(true);
        assertTrue(builder.isFailNoFilesDeploy(Collections.emptyList(), entry));
    }

    // When no files were deployed (null results) but the "fail on no files" flag is disabled,
    // the absence of artifacts is acceptable and should not cause a failure — expects false.
    @Test
    void isFailNoFilesDeploy_nullResults_flagDisabled_returnsFalse() {
        ArtifactDeployerEntry entry = entryWithFailFlag(false);
        assertFalse(builder.isFailNoFilesDeploy(null, entry));
    }

    // When no files were deployed (empty list) but the "fail on no files" flag is disabled,
    // the absence of artifacts is acceptable and should not cause a failure — expects false.
    @Test
    void isFailNoFilesDeploy_emptyResults_flagDisabled_returnsFalse() {
        ArtifactDeployerEntry entry = entryWithFailFlag(false);
        assertFalse(builder.isFailNoFilesDeploy(Collections.emptyList(), entry));
    }

    // When at least one artifact was deployed and the "fail on no files" flag is enabled,
    // deployment succeeded so there is no reason to fail — expects false.
    @Test
    void isFailNoFilesDeploy_nonEmptyResults_flagEnabled_returnsFalse() {
        ArtifactDeployerEntry entry = entryWithFailFlag(true);
        List<ArtifactDeployerVO> results = Collections.singletonList(new ArtifactDeployerVO());
        assertFalse(builder.isFailNoFilesDeploy(results, entry));
    }

    // When at least one artifact was deployed and the "fail on no files" flag is disabled,
    // deployment succeeded and the flag is off — both conditions agree there is no failure — expects false.
    @Test
    void isFailNoFilesDeploy_nonEmptyResults_flagDisabled_returnsFalse() {
        ArtifactDeployerEntry entry = entryWithFailFlag(false);
        List<ArtifactDeployerVO> results = Collections.singletonList(new ArtifactDeployerVO());
        assertFalse(builder.isFailNoFilesDeploy(results, entry));
    }

    // --- printConfiguration ---

    // When includes, excludes, basedir, and outputPath are all provided, every field should
    // appear in the output in the order [includes:X,excludes:Y,basedir:Z,outPath:W].
    @Test
    void printConfiguration_allFieldsPresent_formatsCorrectly() {
        String result = builder.printConfiguration("**/*.jar", "*.log", "/workspace", "/remote/path");
        assertEquals("[includes:**/*.jar,excludes:*.log,basedir:/workspace,outPath:/remote/path]", result);
    }

    // When includes is null, the includes field should be omitted entirely from the output
    // while the remaining fields are still present — expects [excludes:Y,basedir:Z,outPath:W].
    @Test
    void printConfiguration_nullIncludes_omitsIncludesField() {
        String result = builder.printConfiguration(null, "*.log", "/workspace", "/remote/path");
        assertEquals("[excludes:*.log,basedir:/workspace,outPath:/remote/path]", result);
    }

    // When excludes is null, the excludes field should be omitted entirely from the output
    // while the remaining fields are still present — expects [includes:X,basedir:Z,outPath:W].
    @Test
    void printConfiguration_nullExcludes_omitsExcludesField() {
        String result = builder.printConfiguration("**/*.jar", null, "/workspace", "/remote/path");
        assertEquals("[includes:**/*.jar,basedir:/workspace,outPath:/remote/path]", result);
    }

    // When both includes and excludes are null, the output should contain only the mandatory
    // basedir and outputPath fields — expects [basedir:Z,outPath:W].
    @Test
    void printConfiguration_bothNullIncludesAndExcludes_onlyBasedirAndOutputPath() {
        String result = builder.printConfiguration(null, null, "/workspace", "/remote/path");
        assertEquals("[basedir:/workspace,outPath:/remote/path]", result);
    }

    // --- helpers ---

    // Creates an ArtifactDeployerEntry with only the failNoFilesDeploy flag set.
    // All other fields are left at their defaults since isFailNoFilesDeploy only
    // inspects this flag and the results list passed to it separately.
    private ArtifactDeployerEntry entryWithFailFlag(boolean failNoFilesDeploy) {
        ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
        entry.setFailNoFilesDeploy(failNoFilesDeploy);
        return entry;
    }
}
