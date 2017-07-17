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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.*;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.artifactdeployer.service.ArtifactDeployerCopy;
import org.jenkinsci.plugins.artifactdeployer.service.ArtifactDeployerManager;
import org.jenkinsci.plugins.artifactdeployer.service.DeployedArtifactsActionManager;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerPublisher extends Recorder implements MatrixAggregatable, Serializable {

    private List<ArtifactDeployerEntry> entries = Collections.emptyList();
    private boolean deployEvenBuildFail;

    @DataBoundConstructor
    public ArtifactDeployerPublisher(List<ArtifactDeployerEntry> deployedArtifact, boolean deployEvenBuildFail) {
        this.entries = deployedArtifact;
        this.deployEvenBuildFail = deployEvenBuildFail;
        if (this.entries == null)
            this.entries = Collections.emptyList();
    }

    public Object readResolve() {
        if (this.entries == null)
            this.entries = Collections.emptyList();
        return this;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Arrays.asList(new ArtifactDeployerProjectAction(project));
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {

            @Override
            public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
                boolean result = _perform(run, launcher, listener);
                run.save();
                return result;
            }

        };
    }

    @Override
    public boolean perform(hudson.model.AbstractBuild<?, ?> build, hudson.Launcher launcher, hudson.model.BuildListener listener) throws java.lang.InterruptedException, java.io.IOException {
        if (!(build.getProject() instanceof MatrixConfiguration)) {
            return _perform(build, launcher, listener);
        }
        return true;
    }


    private boolean isPerformDeployment(AbstractBuild build) {
        Result result = build.getResult();
        if (result == null) {
            return true;
        }

        if (deployEvenBuildFail) {
            return true;
        }

        return build.getResult().isBetterOrEqualTo(Result.UNSTABLE);
    }

    private boolean _perform(hudson.model.AbstractBuild<?, ?> build, hudson.Launcher launcher, hudson.model.BuildListener listener) throws java.lang.InterruptedException, java.io.IOException {

        if (isPerformDeployment(build)) {

            listener.getLogger().println("[ArtifactDeployer] - Starting deployment from the post-action ...");
            DeployedArtifactsActionManager deployedArtifactsService = DeployedArtifactsActionManager.getInstance();
            ArtifactDeployerBuildAction artifactDeployerBuildActionAction = deployedArtifactsService.getOrCreateAction(build);
            Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts;
            try {
                int currentTotalDeployedCounter = artifactDeployerBuildActionAction.getDeployedArtifactsInfo().size();
                deployedArtifacts = processDeployment(build, listener, currentTotalDeployedCounter);
            } catch (ArtifactDeployerException ae) {
                listener.getLogger().println("[ArtifactDeployer] - [ERROR] - Failed to deploy. " + ae.getMessage());
                if (ae.getCause() != null) {
                    listener.getLogger().println("[ArtifactDeployer] - [ERROR] - " + ae.getCause().getMessage());
                }
                build.setResult(Result.FAILURE);
                return false;
            }

            artifactDeployerBuildActionAction.setArtifactsInfo(build, deployedArtifacts);
            listener.getLogger().println("[ArtifactDeployer] - Stopping deployment from the post-action...");
        }
        return true;
    }

    private Map<Integer, List<ArtifactDeployerVO>> processDeployment(AbstractBuild<?, ?> build, final BuildListener listener, int currentNbDeployedArtifacts) throws ArtifactDeployerException {

        Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts = new HashMap<Integer, List<ArtifactDeployerVO>>();
        FilePath workspace = build.getWorkspace();

        int numberOfCurrentDeployedArtifacts = currentNbDeployedArtifacts;
        for (final ArtifactDeployerEntry entry : entries) {

            if (entry.getRemote() == null) {
                throw new ArtifactDeployerException("All remote directories must be set.");
            }

            final String includes;
            final String excludes;
            final String basedir;
            final String outputPath;
            try {
                includes = build.getEnvironment(listener).expand(entry.getIncludes());
                excludes = build.getEnvironment(listener).expand(entry.getExcludes());
                basedir = build.getEnvironment(listener).expand(entry.getBasedir());
                outputPath = build.getEnvironment(listener).expand(entry.getRemote());
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(ie);
            }

            final boolean flatten = entry.isFlatten();

            //Creating the remote directory
            final FilePath outputFilePath = new FilePath(workspace.getChannel(), outputPath);
            try {
                outputFilePath.mkdirs();
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(String.format("Can't create the directory '%s'", outputPath), ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(String.format("Can't create the directory '%s'", outputPath), ie);
            }

            //Deleting files to remote directory if necessary
            boolean deletedPreviously = entry.isDeleteRemote();
            if (deletedPreviously) {
                try {
                    outputFilePath.deleteContents();
                } catch (IOException ioe) {
                    throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ioe);
                } catch (InterruptedException ie) {
                    throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ie);
                }
            }


            ArtifactDeployerCopy deployerCopy =
                    new ArtifactDeployerCopy(listener, includes, excludes, flatten, outputFilePath, numberOfCurrentDeployedArtifacts);
            ArtifactDeployerManager deployerManager = new ArtifactDeployerManager();
            FilePath basedirFilPath = deployerManager.getBasedirFilePath(workspace, basedir);
            List<ArtifactDeployerVO> results;
            try {
                results = basedirFilPath.act(deployerCopy);
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(ioe);
            } catch (InterruptedException ie) {
                throw new ArtifactDeployerException(ie);
            }

            if (isFailNoFilesDeploy(results, entry)) {
                throw new ArtifactDeployerException("Can't find any artifacts to deploy with the following configuration :"
                        + printConfiguration(includes, excludes, basedirFilPath.getRemote(), outputPath));
            }

            numberOfCurrentDeployedArtifacts += results.size();
            deployedArtifacts.put(entry.getUniqueId(), results);
        }
        return deployedArtifacts;
    }

    private boolean isFailNoFilesDeploy(List<ArtifactDeployerVO> results, ArtifactDeployerEntry entry) {
        return ((results == null || results.size() == 0) && entry.isFailNoFilesDeploy());
    }

    private String printConfiguration(String includes, String excludes, String basedir, String outputPath) {
        StringBuffer sb = new StringBuffer();

        if (includes != null) {
            sb.append(",includes:").append(includes);
        }
        if (excludes != null) {
            sb.append(",excludes:").append(excludes);
        }

        sb.append(",basedir:").append(basedir);
        sb.append(",outPath:").append(outputPath);
        sb.append("]");

        sb = sb.replace(0, 1, "[");

        return sb.toString();
    }

    @SuppressWarnings("unused")
    public List<ArtifactDeployerEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ArtifactDeployerEntry> entries) {
        this.entries = entries;
    }

    public boolean isDeployEvenBuildFail() {
        return deployEvenBuildFail;
    }

    public void setDeployEvenBuildFail(boolean deployEvenBuildFail) {
        this.deployEvenBuildFail = deployEvenBuildFail;
    }

    @Extension
    @SuppressWarnings("unused")
    public static final class DeleteRemoteArtifact extends RunListener<AbstractBuild> {

        private final Logger logger = Logger.getLogger(DeleteRemoteArtifact.class.getName());

        @Override
        public void onDeleted(AbstractBuild build) {

            @SuppressWarnings("unchecked")
            DescribableList<Publisher, Descriptor<Publisher>> projectPublishers = build.getProject().getPublishersList();
            Iterator<Publisher> it = projectPublishers.iterator();
            ArtifactDeployerPublisher artifactDeployerPublisher = null;
            while (it.hasNext()) {
                Publisher publisher = it.next();
                Descriptor<Publisher> publisherDescriptor = publisher.getDescriptor();
                if (publisherDescriptor == null) {
                    continue;
                }
                if (ArtifactDeployerDescriptor.DISPLAY_NAME.equals(publisherDescriptor.getDisplayName())) {
                    artifactDeployerPublisher = (ArtifactDeployerPublisher) publisher;
                }
            }

            if (artifactDeployerPublisher != null) {
                ArtifactDeployerBuildAction artifactDeployerBuildAction = build.getAction(ArtifactDeployerBuildAction.class);
                if (artifactDeployerBuildAction != null) {
                    Map<Integer, List<ArtifactDeployerVO>> info = artifactDeployerBuildAction.getDeployedArtifactsInfo();
                    if (info != null) {
                        for (ArtifactDeployerEntry entry : artifactDeployerPublisher.getEntries()) {
                            //Delete output
                            if (entry.isDeleteRemoteArtifacts()) {
                                List<ArtifactDeployerVO> listArtifacts = info.get(entry.getUniqueId());
                                if (listArtifacts != null) {
                                    for (ArtifactDeployerVO vo : listArtifacts) {
                                        FilePath remoteArtifactPath = new FilePath(build.getWorkspace().getChannel(), vo.getRemotePath());
                                        try {
                                            if (remoteArtifactPath.exists()) {
                                                remoteArtifactPath.deleteRecursive();
                                            }

                                            FilePath parent = remoteArtifactPath.getParent();
                                            boolean rest;
                                            do {
                                                rest = parent.exists() && parent.list().size() == 0;
                                                if (rest) {
                                                    parent.delete();
                                                }
                                                parent = parent.getParent();
                                            } while (rest);

                                        } catch (IOException ioe) {
                                            logger.log(Level.SEVERE, "Error when deleting artifacts.", ioe);
                                        } catch (InterruptedException ie) {
                                            logger.log(Level.SEVERE, "Error when deleting artifacts.", ie);
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }

        }
    }


    @Extension
    @SuppressWarnings("unused")
    public static final class ArtifactDeployerDescriptor extends BuildStepDescriptor<Publisher> {

        public static final String DISPLAY_NAME = Messages.depployerartifact_displayName();

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        public FormValidation doCheckIncludes(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

        public FormValidation doCheckExcludes(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (value == null || value.trim().length() == 0) {
                return FormValidation.ok();
            }
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

        public FormValidation doCheckRemote(@QueryParameter String value) throws IOException {
            if (value == null || value.trim().length() == 0) {
                return FormValidation.error("Remote directory is mandatory.");
            }
            return FormValidation.ok();
        }
    }

}
