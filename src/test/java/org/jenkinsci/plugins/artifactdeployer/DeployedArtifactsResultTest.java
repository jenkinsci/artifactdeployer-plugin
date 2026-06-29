package org.jenkinsci.plugins.artifactdeployer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployedArtifactsResultTest {

    private DeployedArtifactsResult resultFor(Map<Integer, List<ArtifactDeployerVO>> data) {
        ArtifactDeployerBuildAction action = new ArtifactDeployerBuildAction();
        action.setArtifactsInfo(null, data);
        return new DeployedArtifactsResult(action);
    }

    private ArtifactDeployerVO vo(int id, String remotePath) {
        ArtifactDeployerVO vo = new ArtifactDeployerVO();
        vo.setId(id);
        vo.setRemotePath(remotePath);
        vo.setFileName(remotePath.substring(remotePath.lastIndexOf('/') + 1));
        return vo;
    }

    // getAllArtifacts with empty map should return empty collection
    @Test
    void getAllArtifacts_emptyData_returnsEmpty() {
        DeployedArtifactsResult result = resultFor(new HashMap<>());
        assertTrue(result.getAllArtifacts().isEmpty());
    }

    // getAllArtifacts should return artifacts sorted by remotePath
    @Test
    void getAllArtifacts_sortsByRemotePath() {
        ArtifactDeployerVO z = vo(1, "/z/file.jar");
        ArtifactDeployerVO a = vo(2, "/a/file.jar");
        ArtifactDeployerVO m = vo(3, "/m/file.jar");

        Map<Integer, List<ArtifactDeployerVO>> data = new HashMap<>();
        data.put(1, Arrays.asList(z, a, m));

        Collection<ArtifactDeployerVO> artifacts = resultFor(data).getAllArtifacts();

        assertEquals(3, artifacts.size());
        Iterator<ArtifactDeployerVO> iter = artifacts.iterator();
        assertEquals("/a/file.jar", iter.next().getRemotePath());
        assertEquals("/m/file.jar", iter.next().getRemotePath());
        assertEquals("/z/file.jar", iter.next().getRemotePath());
    }

    // getAllArtifacts should aggregate across multiple entries in the map
    @Test
    void getAllArtifacts_aggregatesAcrossMultipleEntries() {
        Map<Integer, List<ArtifactDeployerVO>> data = new HashMap<>();
        data.put(1, Collections.singletonList(vo(1, "/b/b.jar")));
        data.put(2, Collections.singletonList(vo(2, "/a/a.jar")));

        Collection<ArtifactDeployerVO> artifacts = resultFor(data).getAllArtifacts();

        assertEquals(2, artifacts.size());
        assertEquals("/a/a.jar", artifacts.iterator().next().getRemotePath());
    }

    // getAllArtifacts comparator should handle null remotePath without throwing
    @Test
    void getAllArtifacts_nullRemotePath_doesNotThrow() {
        ArtifactDeployerVO noPath = new ArtifactDeployerVO();
        noPath.setId(1);

        Map<Integer, List<ArtifactDeployerVO>> data = new HashMap<>();
        data.put(1, Collections.singletonList(noPath));

        // Should not throw
        Collection<ArtifactDeployerVO> result = resultFor(data).getAllArtifacts();
        assertEquals(1, result.size());
    }
}
