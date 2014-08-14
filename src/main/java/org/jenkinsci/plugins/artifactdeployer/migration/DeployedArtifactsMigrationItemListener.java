package org.jenkinsci.plugins.artifactdeployer.migration;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.listeners.ItemListener;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artifactdeployer.ArtifactDeployerBuildAction;
import org.jenkinsci.plugins.artifactdeployer.DeployedArtifacts;

import java.util.List;

/**
 * @author Gregory Boissinot
 */
@Extension
public class DeployedArtifactsMigrationItemListener extends ItemListener {

    @Override
    public void onLoaded() {
        List<TopLevelItem> items = Jenkins.getInstance().getItems();
        for (TopLevelItem item : items) {
            if (item instanceof Job) {
                Job job = (Job) item;
                final RunList builds = job.getBuilds();
                if (builds != null) {
                    for (Object build : builds) {
                        if (build instanceof AbstractBuild) {
                            AbstractBuild<?, ?> abstractBuild = (AbstractBuild) build;
                            final List<DeployedArtifacts> actions = abstractBuild.getActions(DeployedArtifacts.class);
                            if (actions != null) {
                                ArtifactDeployerBuildAction artifactDeployerBuildAction = new ArtifactDeployerBuildAction();
                                for (DeployedArtifacts action : actions) {
                                    artifactDeployerBuildAction.setArtifactsInfo((AbstractBuild) abstractBuild, action.getDeployedArtifactsInfo());
                                }
                                abstractBuild.addAction(artifactDeployerBuildAction);
                            }
                        }
                    }
                }
            }
        }
    }

}
