package org.jenkinsci.plugins.artifactdeployer;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerProjectAction implements Action {

    private final AbstractProject<?, ?> project;

    public ArtifactDeployerProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    private Run getLastSuccessfulBuild() {
        return project.getLastSuccessfulBuild();
    }

    @SuppressWarnings("unused")
    public DeployedArtifacts getLatestDeployedArtifacts() {
        Run latestSuccessfulBuild = getLastSuccessfulBuild();
        if (latestSuccessfulBuild == null) {
            return null;
        }
        List<DeployedArtifacts> actions = latestSuccessfulBuild.getActions(DeployedArtifacts.class);
        if (actions == null || actions.size() == 0) {
            return null;
        }
        return actions.get(actions.size() - 1);
    }

    @SuppressWarnings("unused")
    public int getLastSuccessfulNumber() {
        Run latestSuccessfulBuild = getLastSuccessfulBuild();
        if (latestSuccessfulBuild == null) {
            return 0;
        }
        return latestSuccessfulBuild.getNumber();
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }
}
