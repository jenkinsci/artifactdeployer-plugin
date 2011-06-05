package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.artifactdeployer.DeployedArtifacts;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class DeployedArtifactsService implements Serializable {

    private static DeployedArtifactsService INSTANCE = new DeployedArtifactsService();

    private DeployedArtifactsService() {
    }

    public static DeployedArtifactsService getInstance() {
        return INSTANCE;
    }

    public DeployedArtifacts getOrCreateAndAttachAction(AbstractBuild<?, ?> build) {

        DeployedArtifacts action = build.getAction(DeployedArtifacts.class);
        if (action == null) {
            action = new DeployedArtifacts();
            build.addAction(action);
        }
        return action;
    }
}

