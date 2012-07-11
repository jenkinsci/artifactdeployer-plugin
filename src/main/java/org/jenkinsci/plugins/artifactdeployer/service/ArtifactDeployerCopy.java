package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerVO;

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

}
