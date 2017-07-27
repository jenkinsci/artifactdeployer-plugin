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

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerVO;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerCopy implements FilePath.FileCallable<List<ArtifactDeployerVO>> {

    private BuildListener listener;

    private String includes;

    private String excludes;

    private boolean flatten;

    private FilePath outputFilePath;

    private int numberOfCurrentDeployedArtifacts;

    public ArtifactDeployerCopy(BuildListener listener, String includes, String excludes, boolean flatten, FilePath outputFilePath, int numberOfCurrentDeployedArtifacts) {
        this.listener = listener;
        this.includes = includes;
        this.excludes = excludes;
        this.flatten = flatten;
        this.outputFilePath = outputFilePath;
        this.numberOfCurrentDeployedArtifacts = numberOfCurrentDeployedArtifacts;
    }

    public List<ArtifactDeployerVO> invoke(File localBasedir, VirtualChannel channel) throws IOException, InterruptedException {

        String remote = outputFilePath.getRemote();
        FileSet fileSet = Util.createFileSet(localBasedir, includes, excludes);
        int inputFiles = fileSet.size();

        LocalCopy localCopy = new LocalCopy();
        List<File> outputFilesList = localCopy.copyAndGetNumbers(fileSet, flatten, new File(remote));
        if (inputFiles != outputFilesList.size()) {
            listener.getLogger().println(String.format("[ArtifactDeployer] - All the files have not been deployed. There was %d input files but only %d was copied. Maybe you have to use 'Delete content of remote directory' feature for deleting remote directory before deploying.", inputFiles, outputFilesList.size()));
        } else {
            listener.getLogger().println(String.format("[ArtifactDeployer] - %d file(s) have been copied from the '%s' to '%s'.", outputFilesList.size(), localBasedir.getPath(), outputFilePath));
        }

        List<ArtifactDeployerVO> deployedArtifactsResultList = new LinkedList<ArtifactDeployerVO>();
        for (File renoteFile : outputFilesList) {
            ArtifactDeployerVO deploymentResultEntry = new ArtifactDeployerVO();
            deploymentResultEntry.setId(++numberOfCurrentDeployedArtifacts);
            deploymentResultEntry.setDeployed(true);
            deploymentResultEntry.setFileName(renoteFile.getName());
            deploymentResultEntry.setRemotePath(renoteFile.getPath());
            deployedArtifactsResultList.add(deploymentResultEntry);
        }
        return deployedArtifactsResultList;
    }
    @Override
        public void checkRoles(RoleChecker roleChecker) throws SecurityException {
            //We don't require any roles to be checked?
        }

}
