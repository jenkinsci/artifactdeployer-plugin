package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ArtifactDeployerBuildActionTest {

    @Test
    void metadataMethods_shouldReturnExpectedConstants() {
        ArtifactDeployerBuildAction action = new ArtifactDeployerBuildAction();

        assertEquals("package.gif", action.getIconFileName());
        assertEquals("Deployed Build Artifacts", action.getDisplayName());
        assertEquals("deployedArtifacts", action.getUrlName());
    }

    @Test
    void setArtifactsInfoAndCount_shouldExposeStoredData() {
        ArtifactDeployerBuildAction action = new ArtifactDeployerBuildAction();

        Map<Integer, List<ArtifactDeployerVO>> data = new HashMap<Integer, List<ArtifactDeployerVO>>();
        data.put(1, Arrays.asList(new ArtifactDeployerVO(), new ArtifactDeployerVO()));
        data.put(2, Collections.singletonList(new ArtifactDeployerVO()));

        action.setArtifactsInfo(null, data);

        assertSame(data.get(1), action.getDeployedArtifactsInfo().get(1));
        assertSame(data.get(2), action.getDeployedArtifactsInfo().get(2));
        assertEquals(3, action.getDeployedArtifactsCount());
    }

    @Test
    void getTarget_shouldCreateDeployedArtifactsResult() {
        ArtifactDeployerBuildAction action = new ArtifactDeployerBuildAction();
        Object target = action.getTarget();

        assertNotNull(target);
        assertEquals(DeployedArtifactsResult.class, target.getClass());
    }
}
