package org.jenkinsci.plugins.artifactdeployer;

import hudson.model.Action;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Gregory Boissinot
 */
public class DeployedArtifacts implements Action {

    private static final String URL_NAME = "deployedArtifacts";

    @SuppressWarnings("unused")
    @Deprecated
    /**
     * Note: there is no backward compatibility for this field
     * (can't delete deployed artifacts performed with artifactdeployer plugin previous v0.3)
     */
    private transient List<ArtifactDeployerVO> deployedArtifacts = new LinkedList<ArtifactDeployerVO>();

    private Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo = new HashMap<Integer, List<ArtifactDeployerVO>>();

    public void addDeployedArtifacts(Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo) {
        this.deployedArtifactsInfo.putAll(deployedArtifactsInfo);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public Map<Integer, List<ArtifactDeployerVO>> getDeployedArtifactsInfo() {
        return this.deployedArtifactsInfo;
    }

    @SuppressWarnings("unused")
    public Collection<ArtifactDeployerVO> getAllArtifacts() {

        Comparator<ArtifactDeployerVO> comparator = new Comparator<ArtifactDeployerVO>() {
            public int compare(ArtifactDeployerVO artifactDeployer1, ArtifactDeployerVO artifactDeployer2) {
                if (artifactDeployer1 == null || artifactDeployer2 == null) {
                    return 0;
                }

                String remotePath1 = artifactDeployer1.getRemotePath();
                String remotePath2 = artifactDeployer2.getRemotePath();
                if (remotePath1 == null || remotePath2 == null) {
                    return 0;
                }
                return remotePath1.compareTo(remotePath2);
            }
        };

        SortedSet<ArtifactDeployerVO> result = new TreeSet<ArtifactDeployerVO>(comparator);
        if (this.deployedArtifactsInfo != null) {
            for (List<ArtifactDeployerVO> list : this.deployedArtifactsInfo.values()) {
                result.addAll(list);
            }
        }
        return result;
    }


    private ArtifactDeployerVO getArtifactDeployerVO(int id) {
        for (List<ArtifactDeployerVO> entryList : this.deployedArtifactsInfo.values()) {
            for (ArtifactDeployerVO entry : entryList) {
                if (entry.getId() == id) {
                    return entry;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void doDownload(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException {

        String restOfPath = request.getRestOfPath();
        if (restOfPath == null) {
            return;
        }

        final String artifactPattenLink = "/artifact.";
        if (restOfPath.startsWith(artifactPattenLink)) {
            int id = Integer.parseInt(restOfPath.split(artifactPattenLink)[1]);
            ArtifactDeployerVO selectedArtifact = getArtifactDeployerVO(id);
            if (selectedArtifact != null) {
                response.setHeader("Content-Disposition", "attachment;filename=\"" + selectedArtifact.getFileName() + "\"");
                response.serveFile(request, new File(selectedArtifact.getRemotePath()).toURI().toURL());
            }
        }
    }

}
