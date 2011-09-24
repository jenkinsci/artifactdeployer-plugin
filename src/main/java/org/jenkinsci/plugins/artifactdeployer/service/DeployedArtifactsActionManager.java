package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.artifactdeployer.DeployedArtifacts;

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

    public DeployedArtifacts getOrCreateAction(AbstractBuild<?, ?> build) {

        DeployedArtifacts action = build.getAction(DeployedArtifacts.class);
        if (action == null) {
            action = new DeployedArtifacts();
            build.addAction(action);
        }
        return action;
    }
}

