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

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerEntry implements Serializable {

    @Deprecated
    @SuppressWarnings("unused")
    private transient String id;
    @Deprecated
    @SuppressWarnings("unused")
    private transient boolean deletingRemote;

    private String includes;
    private String basedir;
    private String excludes;
    private String remote;
    private boolean flatten;
    private boolean deleteRemote;
    private boolean deleteRemoteArtifacts;
    private boolean deleteRemoteArtifactsByScript=false;
    private String groovyExpression;
    private boolean failNoFilesDeploy;

    public ArtifactDeployerEntry() {
    }

    @DataBoundConstructor
    public ArtifactDeployerEntry(String includes, String basedir, String excludes, String remote, boolean flatten, boolean deleteRemote, boolean deleteRemoteArtifacts, boolean failNoFilesDeploy) {
        this.includes = includes;
        this.basedir = basedir;
        this.excludes = excludes;
        this.remote = remote;
        this.flatten = flatten;
        this.deleteRemote = deleteRemote;
        this.deleteRemoteArtifacts = deleteRemoteArtifacts;
        this.failNoFilesDeploy = failNoFilesDeploy;
    }

    @SuppressWarnings("unused")
    public String getIncludes() {
        return includes;
    }

    @SuppressWarnings("unused")
    public String getBasedir() {
        return basedir;
    }

    @SuppressWarnings("unused")
    public String getExcludes() {
        return excludes;
    }

    @SuppressWarnings("unused")
    public String getRemote() {
        return remote;
    }

    @SuppressWarnings("unused")
    public boolean isFlatten() {
        return flatten;
    }

    @SuppressWarnings({"unused", "deprecation"})
    @Deprecated
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public boolean isDeleteRemote() {
        return deleteRemote;
    }

    @SuppressWarnings("unused")
    public boolean isDeleteRemoteArtifacts() {
        return deleteRemoteArtifacts;
    }

    @SuppressWarnings("unused")

    public boolean isFailNoFilesDeploy() {
        return failNoFilesDeploy;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public void setDeleteRemote(boolean deleteRemote) {
        this.deleteRemote = deleteRemote;
    }

    public void setDeleteRemoteArtifacts(boolean deleteRemoteArtifacts) {
        this.deleteRemoteArtifacts = deleteRemoteArtifacts;
    }

    public void setDeleteRemoteArtifactsByScript(boolean deleteRemoteArtifactsByScript) {
        this.deleteRemoteArtifactsByScript = deleteRemoteArtifactsByScript;
    }

    public void setGroovyExpression(String groovyExpression) {
        this.groovyExpression = groovyExpression;
    }

    public void setFailNoFilesDeploy(boolean failNoFilesDeploy) {
        this.failNoFilesDeploy = failNoFilesDeploy;
    }

    @SuppressWarnings({"unused", "deprecation"})
    public Object readObject() {
        if (this.deletingRemote) {
            this.deleteRemote = true;
        }
        return this;
    }

    public int getUniqueId() {
        int result = includes != null ? includes.hashCode() : 0;
        result = 31 * result + (excludes != null ? excludes.hashCode() : 0);
        result = 31 * result + (remote != null ? remote.hashCode() : 0);
        result = 31 * result + (basedir != null ? basedir.hashCode() : 0);
        result = 31 * result + (flatten ? 1 : 0);
        result = 31 * result + (deletingRemote ? 1 : 0);
        result = 31 * result + (deleteRemote ? 1 : 0);
        result = 31 * result + (deleteRemoteArtifacts ? 1 : 0);
        result = 31 * result + (deleteRemoteArtifactsByScript ? 1 : 0);
        result = 31 * result + (groovyExpression != null ? groovyExpression.hashCode() : 0);
        result = 31 * result + (failNoFilesDeploy ? 1 : 0);
        return result;
    }

}
