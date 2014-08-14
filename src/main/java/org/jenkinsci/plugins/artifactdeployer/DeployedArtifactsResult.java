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
package org.jenkinsci.plugins.artifactdeployer;

import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Gregory Boissinot
 */
public class DeployedArtifactsResult {

    private ArtifactDeployerBuildAction artifactDeployerBuildAction;

    public DeployedArtifactsResult(ArtifactDeployerBuildAction artifactDeployerBuildAction) {
        this.artifactDeployerBuildAction = artifactDeployerBuildAction;
    }

    public AbstractBuild<?, ?> getOwner() {
        return artifactDeployerBuildAction.getOwner();
    }

    @SuppressWarnings("unused")
    public Collection<ArtifactDeployerVO> getAllArtifacts() {

        Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo = artifactDeployerBuildAction.getDeployedArtifactsInfo();

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
        if (deployedArtifactsInfo != null) {
            for (List<ArtifactDeployerVO> list : deployedArtifactsInfo.values()) {
                result.addAll(list);
            }
        }
        return result;
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

    private ArtifactDeployerVO getArtifactDeployerVO(int id) {
        Map<Integer, List<ArtifactDeployerVO>> deployedArtifactsInfo = artifactDeployerBuildAction.getDeployedArtifactsInfo();
        for (List<ArtifactDeployerVO> entryList : deployedArtifactsInfo.values()) {
            for (ArtifactDeployerVO entry : entryList) {
                if (entry.getId() == id) {
                    return entry;
                }
            }
        }
        return null;
    }

}
