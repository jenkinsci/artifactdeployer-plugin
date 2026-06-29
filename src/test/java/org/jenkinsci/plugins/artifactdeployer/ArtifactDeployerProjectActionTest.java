package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtifactDeployerProjectActionTest {

    @Test
    void metadataMethods_shouldReturnExpectedValues() {
        ArtifactDeployerProjectAction action = new ArtifactDeployerProjectAction(null);

        assertEquals("package.gif", action.getIconFileName());
        assertEquals("Last Successful Deployed Artifacts", action.getDisplayName());
        assertEquals("lastSuccessfulBuild//deployedArtifacts", action.getUrlName());
    }
}
