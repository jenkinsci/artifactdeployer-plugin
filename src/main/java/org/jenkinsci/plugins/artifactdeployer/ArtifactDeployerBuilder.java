package org.jenkinsci.plugins.artifactdeployer;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artifactdeployer.exception.ArtifactDeployerException;
import org.jenkinsci.plugins.artifactdeployer.service.ArtifactDeployerCopy;
import org.jenkinsci.plugins.artifactdeployer.service.ArtifactDeployerManager;
import org.jenkinsci.plugins.artifactdeployer.service.DeployedArtifactsActionManager;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerBuilder extends Builder implements Serializable {

    ArtifactDeployerEntry entry;

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("[ArtifactDeployer] - Starting deployment from the build step ...");
        DeployedArtifactsActionManager deployedArtifactsService = DeployedArtifactsActionManager.getInstance();
        DeployedArtifacts deployedArtifactsAction = deployedArtifactsService.getOrCreateAction(build);
        final FilePath workspace = build.getWorkspace();
        Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts;
        try {
            deployedArtifacts = processDeployment(build, listener, workspace);
        } catch (ArtifactDeployerException ae) {
            listener.getLogger().println("[ArtifactDeployer] - [ERROR] -  Failed to deploy. " + ae.getMessage());
            if (ae.getCause() != null) {
                listener.getLogger().println("[ArtifactDeployer] - [ERROR] - " + ae.getCause().getMessage());
            }
            build.setResult(Result.FAILURE);
            return false;
        }

        deployedArtifactsAction.addDeployedArtifacts(deployedArtifacts);
        listener.getLogger().println("[ArtifactDeployer] - Stopping deployment from the build step ...");

        return true;
    }

    private Map<Integer, List<ArtifactDeployerVO>> processDeployment(final AbstractBuild<?, ?> build, final BuildListener listener, FilePath workspace) throws IOException, InterruptedException {

        if (entry.getRemote() == null) {
            throw new ArtifactDeployerException("A remote directory must be set.");
        }

        Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts = new HashMap<Integer, List<ArtifactDeployerVO>>();
        final String includes = build.getEnvironment(listener).expand(entry.getIncludes());
        final String excludes = build.getEnvironment(listener).expand(entry.getExcludes());
        final String basedir = build.getEnvironment(listener).expand(entry.getBasedir());
        final String outputPath = build.getEnvironment(listener).expand(entry.getRemote());
        final boolean flatten = entry.isFlatten();

        //Creating the remote directory
        final FilePath outputFilePath = new FilePath(workspace.getChannel(), outputPath);
        try {
            outputFilePath.mkdirs();
        } catch (IOException ioe) {
            throw new ArtifactDeployerException(String.format("Can't create the directory '%s'", outputPath), ioe);
        }

        //Deleting files to remote directory if necessary
        boolean deletedPreviously = entry.isDeleteRemote();
        if (deletedPreviously) {
            try {
                outputFilePath.deleteContents();
            } catch (IOException ioe) {
                throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ioe);
            }
        }

        //Copying files to remote directory
        DeployedArtifactsActionManager deployedArtifactsService = DeployedArtifactsActionManager.getInstance();
        DeployedArtifacts deployedArtifactsAction = deployedArtifactsService.getOrCreateAction(build);
        int numberOfCurrentDeployedArtifacts = deployedArtifactsAction.getDeployedArtifactsInfo().size();
        ArtifactDeployerCopy deployerCopy =
                new ArtifactDeployerCopy(listener, includes, excludes, flatten, outputFilePath, numberOfCurrentDeployedArtifacts);
        ArtifactDeployerManager deployerManager = new ArtifactDeployerManager();
        FilePath basedirFilePath = deployerManager.getBasedirFilePath(workspace, basedir);
        List<ArtifactDeployerVO> results = basedirFilePath.act(deployerCopy);
        deployedArtifacts.put(entry.getUniqueId(), results);
        return deployedArtifacts;
    }

    @SuppressWarnings("unused")
    public ArtifactDeployerEntry getEntry() {
        return entry;
    }

    private void setEntry(ArtifactDeployerEntry entry) {
        this.entry = entry;
    }


    @Extension
    @SuppressWarnings("unused")
    public static final class DeleteRemoteArtifactFromBuilder extends RunListener<AbstractBuild> {

        private final Logger logger = Logger.getLogger(DeleteRemoteArtifactFromBuilder.class.getName());

        @Override
        public void onDeleted(AbstractBuild build) {

            FreeStyleProject freeStyleProject;
            if (!(build.getProject() instanceof FreeStyleProject)) {
                return;
            }
            freeStyleProject = (FreeStyleProject) build.getProject();
            DescribableList<Builder, Descriptor<Builder>> projectBuilders = freeStyleProject.getBuildersList();
            Iterator<Builder> it = projectBuilders.iterator();

            //Compute ArtifactDeployerBuilder elements
            List<ArtifactDeployerBuilder> artifactDeployerBuilders = new ArrayList<ArtifactDeployerBuilder>();
            while (it.hasNext()) {
                Builder builder = it.next();
                if (DescriptorImpl.DISPLAY_NAME.equals(builder.getDescriptor().getDisplayName())) {
                    ArtifactDeployerBuilder instance = (ArtifactDeployerBuilder) builder;
                    artifactDeployerBuilders.add(instance);
                }
            }

            //Process all ArtifactDeployerBuilder instance
            for (ArtifactDeployerBuilder artifactDeployerBuilder : artifactDeployerBuilders) {
                DeployedArtifacts deployedArtifacts = build.getAction(DeployedArtifacts.class);
                if (deployedArtifacts != null) {
                    Map<Integer, List<ArtifactDeployerVO>> info = deployedArtifacts.getDeployedArtifactsInfo();
                    if (info != null) {
                        ArtifactDeployerEntry entry = artifactDeployerBuilder.getEntry();

                        //Delete output directly
                        if (entry.isDeleteRemoteArtifacts()) {
                            List<ArtifactDeployerVO> listArtifacts = info.get(entry.getUniqueId());
                            if (listArtifacts != null) {
                                for (ArtifactDeployerVO vo : listArtifacts) {
                                    FilePath remoteArtifactPath = new FilePath(build.getWorkspace().getChannel(), vo.getRemotePath());
                                    try {
                                        if (remoteArtifactPath.exists()) {
                                            remoteArtifactPath.deleteRecursive();
                                        }

                                        if (remoteArtifactPath.getParent().exists() && remoteArtifactPath.getParent().list().size() == 0) {
                                            remoteArtifactPath.getParent().delete();
                                        }

                                    } catch (IOException ioe) {
                                        logger.log(Level.SEVERE, "Error when deleting artifacts.", ioe);
                                    } catch (InterruptedException ie) {
                                        logger.log(Level.SEVERE, "Error when deleting artifacts.", ie);
                                    }
                                }
                            }

                        }

                        //Execute the script for deletion
                        if (entry.isDeleteRemoteArtifactsByScript()) {
                            //Inject list artifacts as variable
                            Binding binding = new Binding();
                            if (deployedArtifacts != null) {
                                List<ArtifactDeployerVO> listArtifacts = info.get(entry.getUniqueId());
                                binding.setVariable("ARTIFACTS", listArtifacts);
                            }
                            GroovyShell shell = new GroovyShell(binding);
                            shell.evaluate(entry.getGroovyExpression());
                        }
                    }
                }
            }

        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public static final String DISPLAY_NAME = Messages.depployerartifact_displayName();

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ArtifactDeployerBuilder builder = new ArtifactDeployerBuilder();
            ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
            entry.setIncludes(Util.fixEmpty(formData.getString("includes")));
            entry.setBasedir(Util.fixEmpty(formData.getString("basedir")));
            entry.setExcludes(Util.fixEmpty(formData.getString("excludes")));
            entry.setRemote(Util.fixEmpty(formData.getString("remote")));
            entry.setDeleteRemote(formData.getBoolean("deleteRemote"));
            entry.setFlatten(formData.getBoolean("flatten"));
            entry.setDeleteRemoteArtifacts(formData.getBoolean("deleteRemoteArtifacts"));
            Object deleteRemoteArtifactsObject = formData.get("deleteRemoteArtifactsByScript");
            if (deleteRemoteArtifactsObject == null) {
                entry.setDeleteRemoteArtifactsByScript(false);
            } else {
                entry.setDeleteRemoteArtifactsByScript(true);
                entry.setGroovyExpression(Util.fixEmpty(formData.getJSONObject("deleteRemoteArtifactsByScript").getString("groovyExpression")));
            }
            builder.setEntry(entry);
            return builder;
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
                throw FormValidation.error("Remote directory is mandatory.");
            }
            return FormValidation.ok();
        }
    }
}
