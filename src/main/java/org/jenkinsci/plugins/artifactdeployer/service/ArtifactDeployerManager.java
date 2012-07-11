package org.jenkinsci.plugins.artifactdeployer.service;

import hudson.FilePath;
import org.jenkinsci.plugins.artifactdeployer.exception.ArtifactDeployerException;

import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerManager {

    public FilePath getBasedirFilePath(FilePath ws, String basedirField) {
        if (basedirField == null) {
            return ws;
        }
        FilePath basedirFilePath = ws.child(basedirField);
        try {
            if (!basedirFilePath.exists()) {
                throw new ArtifactDeployerException(String.format("The basedir path '%s' from the workspace doesn't exist.", basedirField));
            }
        } catch (IOException ioe) {
            throw new ArtifactDeployerException(ioe);
        } catch (InterruptedException ae) {
            throw new ArtifactDeployerException(ae);
        }
        return basedirFilePath;
    }

}
