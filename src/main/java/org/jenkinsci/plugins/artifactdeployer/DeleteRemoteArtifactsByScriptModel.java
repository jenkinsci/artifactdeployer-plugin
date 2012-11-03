package org.jenkinsci.plugins.artifactdeployer;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class DeleteRemoteArtifactsByScriptModel {

    private String groovyExpression;

    @DataBoundConstructor
    public DeleteRemoteArtifactsByScriptModel(String groovyExpression) {
        this.groovyExpression = groovyExpression;
    }

    public String getGroovyExpression() {
        return groovyExpression;
    }
}
