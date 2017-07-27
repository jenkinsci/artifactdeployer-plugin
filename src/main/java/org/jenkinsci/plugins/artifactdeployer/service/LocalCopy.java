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

import com.atlassian.ant.tasks.CopyWithPerms;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerException;

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

            //private int copySize;

            public CopyImpl() {
                setProject(new org.apache.tools.ant.Project());
            }

            @Override
            protected void doFileOperations() {
                //copySize = super.fileCopyMap.size();
                for (Object tabOb : super.fileCopyMap.values()) {
                    String[] tab = (String[]) tabOb;
                    for (String f : tab) {
                        deployedFiles.add(new File(f));
                    }
                }
                super.doFileOperations();
            }

            //public int getNumCopied() {
            //    return copySize;
            //}

            public List<File> getDeployedFiles() {
                return deployedFiles;
            }
        }

        CopyImpl copyTask = new CopyImpl();
        copyTask.setTodir(target);
        copyTask.addFileset(fileSet);
        //Do not overwrite file: the plugin has the delete content directory previously
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
