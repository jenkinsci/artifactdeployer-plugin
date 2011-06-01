package org.jenkinsci.plugins.artifactdeployer.exception;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerException extends RuntimeException {

    public ArtifactDeployerException() {
    }

    public ArtifactDeployerException(String s) {
        super(s);
    }

    public ArtifactDeployerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ArtifactDeployerException(Throwable throwable) {
        super(throwable);
    }
}
