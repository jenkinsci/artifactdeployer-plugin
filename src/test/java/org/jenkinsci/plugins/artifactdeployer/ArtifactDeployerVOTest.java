package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactDeployerVOTest {

    @Test
    void settersAndGetters_shouldRoundTripValues() {
        ArtifactDeployerVO vo = new ArtifactDeployerVO();

        vo.setId(42);
        vo.setFileName("archive.zip");
        vo.setDeployed(true);
        vo.setRemotePath("/tmp/archive.zip");

        assertEquals(42, vo.getId());
        assertEquals("archive.zip", vo.getFileName());
        assertTrue(vo.isDeployed());
        assertEquals("/tmp/archive.zip", vo.getRemotePath());
    }
}
