package org.jenkinsci.plugins.artifactdeployer;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerBuildAction implements Action, StaplerProxy {

    private static final String URL_NAME = "deployedArtifacts";

    private AbstractBuild<?, ?> owner;

    private Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo = new HashMap<Integer, List<ArtifactDeployerVO>>();

    public void setArtifactsInfo(AbstractBuild<?, ?> owner, Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo) {
        this.owner = owner;
        this.deployedArtifactsInfo.putAll(deployedArtifactsInfo);
    }

    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    public Map<Integer, List<ArtifactDeployerVO>> getDeployedArtifactsInfo() {
        return this.deployedArtifactsInfo;
    }

    public String getIconFileName() {
        return "package.gif";
    }

    public String getDisplayName() {
        return "Deployed Build Artifacts";
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public Object getTarget() {
        return new DeployedArtifactsResult(this);
    }
}
