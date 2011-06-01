package org.jenkinsci.plugins.artifactdeployer;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerEntry implements Serializable {

    private String id;

    private String includes;

    private String excludes;

    private String remote;

    private boolean flatten;

    private transient boolean deletingRemote;

    private boolean deleteRemote;

    private boolean deleteRemoteArtifacts;

    private boolean deleteRemoteArtifactsByScript;

    private String groovyExpression;

    @SuppressWarnings("unused")
    public String getIncludes() {
        return includes;
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

    @SuppressWarnings("unused")
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
    public boolean isDeleteRemoteArtifactsByScript() {
        return deleteRemoteArtifactsByScript;
    }

    @SuppressWarnings("unused")
    public String getGroovyExpression() {
        return groovyExpression;
    }

    /*package*/ void setId(String id) {
        this.id = id;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
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

    public void readObject(){
        if (deletingRemote){
            deleteRemote=true;
        }
    }
}
