package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerBuildAction;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DeployedArtifactsActionManager implements Serializable {

    private static DeployedArtifactsActionManager INSTANCE = new DeployedArtifactsActionManager();

    private DeployedArtifactsActionManager() {
    }

    public static DeployedArtifactsActionManager getInstance() {
        return INSTANCE;
    }

    public ArtifactDeployerBuildAction getOrCreateAction(AbstractBuild<?, ?> build) {

        ArtifactDeployerBuildAction action = build.getAction(ArtifactDeployerBuildAction.class);
        if (action == null) {
            action = new ArtifactDeployerBuildAction();
            build.addAction(action);
        }
        return action;
    }
}

