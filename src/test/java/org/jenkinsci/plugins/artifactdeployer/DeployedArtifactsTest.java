package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployedArtifactsTest {

    @Test
    void actionMetadata_shouldBeNullAsDeprecatedAction() {
        DeployedArtifacts deployed = new DeployedArtifacts();

        assertNull(deployed.getIconFileName());
        assertNull(deployed.getDisplayName());
        assertNull(deployed.getUrlName());
    }

    @Test
    void deployedArtifactsInfo_shouldBeMutableMap() {
        DeployedArtifacts deployed = new DeployedArtifacts();
        Map<Integer, List<ArtifactDeployerVO>> info = deployed.getDeployedArtifactsInfo();

        assertNotNull(info);
        info.put(10, Collections.singletonList(new ArtifactDeployerVO()));

        assertTrue(deployed.getDeployedArtifactsInfo().containsKey(10));
        assertEquals(1, deployed.getDeployedArtifactsInfo().get(10).size());
    }

    @Test
    void readResolve_shouldReturnArtifactDeployerBuildActionWithCopiedData() throws Exception {
        DeployedArtifacts deployed = new DeployedArtifacts();
        ArtifactDeployerVO vo = new ArtifactDeployerVO();
        deployed.getDeployedArtifactsInfo().put(99, Collections.singletonList(vo));

        Method readResolve = DeployedArtifacts.class.getDeclaredMethod("readResolve");
        readResolve.setAccessible(true);
        Object resolved = readResolve.invoke(deployed);

        assertEquals(ArtifactDeployerBuildAction.class, resolved.getClass());
        ArtifactDeployerBuildAction action = (ArtifactDeployerBuildAction) resolved;
        assertTrue(action.getDeployedArtifactsInfo().containsKey(99));
        assertEquals(1, action.getDeployedArtifactsInfo().get(99).size());
    }
}
