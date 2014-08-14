/**
 * The MIT License
 * Copyright (c) 2014 Gregory Boissinot and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

