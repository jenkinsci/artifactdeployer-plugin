package org.jenkinsci.plugins.artifactdeployer;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerProjectAction implements Action {

    private static final String URL_NAME = "lastSuccessfulBuild//deployedArtifacts";

    private final AbstractProject<?, ?> project;

    public ArtifactDeployerProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "package.gif";
    }

    public String getDisplayName() {
        return "Last Successful Deployed Artifacts";
    }

    public String getUrlName() {
        return URL_NAME;
    }

    @SuppressWarnings("unused")
    public int getLastSuccessfulNumber() {
        Run latestSuccessfulBuild = project.getLastSuccessfulBuild();
        if (latestSuccessfulBuild == null) {
            return 0;
        }
        return latestSuccessfulBuild.getNumber();
    }

}
