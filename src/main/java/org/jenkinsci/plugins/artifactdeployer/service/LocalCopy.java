package org.jenkinsci.plugins.artifactdeployer.service;

import com.atlassian.ant.tasks.CopyWithPerms;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.exception.ArtifactDeployerException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class LocalCopy {

    public List<File> copyAndGetNumbers(FileSet fileSet, boolean flatten, File target) throws ArtifactDeployerException {

        class CopyImpl extends CopyWithPerms {

            private final List<File> deployedFiles = new ArrayList<File>();

            private int copySize;

            public CopyImpl() {
                setProject(new org.apache.tools.ant.Project());
            }

            @Override
            protected void doFileOperations() {
                copySize = super.fileCopyMap.size();
                for (Object tabOb : super.fileCopyMap.values()) {
                    String[] tab = (String[]) tabOb;
                    for (String f : tab) {
                        deployedFiles.add(new File(f));
                    }
                }
                super.doFileOperations();
            }

            public int getNumCopied() {
                return copySize;
            }

            public List<File> getDeployedFiles() {
                return deployedFiles;
            }
        }

        CopyImpl copyTask = new CopyImpl();
        copyTask.setTodir(target);
        copyTask.addFileset(fileSet);
        //Do not overwrite file: the plugin has the delete content directory previously feature
        copyTask.setOverwrite(false);
        copyTask.setIncludeEmptyDirs(true);
        copyTask.setFlatten(flatten);
        copyTask.setPreserveLastModified(true);
        copyTask.setPreservePermissions(true);

        try {
            copyTask.execute();
        } catch (BuildException be) {
            throw new ArtifactDeployerException("Error on copying file.", be);
        }
        return copyTask.getDeployedFiles();
    }
}
