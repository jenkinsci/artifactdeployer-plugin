package org.jenkinsci.plugins.artifactdeployer.migration;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.listeners.ItemListener;
import hudson.util.RunList;
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
        List<TopLevelItem> items = Hudson.getInstance().getItems();
        for (TopLevelItem item : items) {
            if (item instanceof Job) {
                Job job = (Job) item;
                final RunList builds = job.getBuilds();
                if (builds != null) {
                    for (Object build : builds) {
                        AbstractBuild<?, ?> abstractBuild = (AbstractBuild) build;
                        ArtifactDeployerBuildAction artifactDeployerBuildAction = new ArtifactDeployerBuildAction();
                        final List<DeployedArtifacts> actions = abstractBuild.getActions(DeployedArtifacts.class);
                        if (actions != null) {
                            for (DeployedArtifacts action : actions) {
                                artifactDeployerBuildAction.setArtifactsInfo((AbstractBuild) abstractBuild, action.getDeployedArtifactsInfo());
                            }
                            abstractBuild.addAction(artifactDeployerBuildAction);
//                            try {
//                                abstractBuild.add();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }
            }
        }
    }
//    @Override
//    public void onStarted(Run run, TaskListener listener) {
//        //final DeployedArtifacts action = run.getAction(DeployedArtifacts.class);
//        ArtifactDeployerBuildAction artifactDeployerBuildAction = new ArtifactDeployerBuildAction();
//        final List<DeployedArtifacts> actions = run.getActions(DeployedArtifacts.class);
//        for (DeployedArtifacts action : actions) {
//            artifactDeployerBuildAction.setArtifactsInfo((AbstractBuild) run, action.getDeployedArtifactsInfo());
//            actions.remove(action);
//        }
//        run.addAction(artifactDeployerBuildAction);
//        try {
//            run.save();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
