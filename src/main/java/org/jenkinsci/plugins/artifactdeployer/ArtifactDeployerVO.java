package org.jenkinsci.plugins.artifactdeployer;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerVO implements Serializable {

    private int id;

    private String fileName;

    private boolean deployed;

    private String remotePath;

    public ArtifactDeployerVO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
}
