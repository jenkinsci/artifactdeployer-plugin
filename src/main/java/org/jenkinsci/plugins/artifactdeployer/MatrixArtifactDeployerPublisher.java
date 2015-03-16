package org.jenkinsci.plugins.artifactdeployer;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * Extends ArtifactDeployerPublisher to support Matrix projects and implement MatrixAggregatable.
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class MatrixArtifactDeployerPublisher extends ArtifactDeployerPublisher implements MatrixAggregatable {

    @DataBoundConstructor
    public MatrixArtifactDeployerPublisher(List<ArtifactDeployerEntry> deployedArtifact, boolean deployEvenBuildFail) {
        super(deployedArtifact, deployEvenBuildFail);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build.getProject() instanceof MatrixConfiguration) return true;
        return super.perform(build, launcher, listener);
    }

    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {

            @Override
            public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
                boolean result = MatrixArtifactDeployerPublisher.super.perform((AbstractBuild) run, launcher, listener);
                run.save();
                return result;
            }

        };
    }

    @Extension(optional = true)
    @SuppressWarnings("unused")
    public static final class DescriptorImpl extends ArtifactDeployerDescriptor {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return jobType.isAssignableFrom(MatrixProject.class);
        }


    }
}
