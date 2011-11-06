package org.jenkinsci.plugins.artifactdeployer;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.*;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.artifactdeployer.exception.ArtifactDeployerException;
import org.jenkinsci.plugins.artifactdeployer.service.ArtifactDeployerCopy;
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
public class ArtifactDeployerPublisher extends Recorder implements MatrixAggregatable, Serializable {

    private List<ArtifactDeployerEntry> entries = Collections.emptyList();

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
                return _perform(run, launcher, listener);
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


    public boolean _perform(hudson.model.AbstractBuild<?, ?> build, hudson.Launcher launcher, hudson.model.BuildListener listener) throws java.lang.InterruptedException, java.io.IOException {

        if (build.getResult() == null || build.getResult().isBetterOrEqualTo(Result.SUCCESS)) {

            listener.getLogger().println("[ArtifactDeployer] - Starting deployment from the post-action ...");
            DeployedArtifactsActionManager deployedArtifactsService = DeployedArtifactsActionManager.getInstance();
            DeployedArtifacts deployedArtifactsAction = deployedArtifactsService.getOrCreateAction(build);
            Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts;
            try {
                int currentTotalDeployedCounter = deployedArtifactsAction.getDeployedArtifactsInfo().size();
                deployedArtifacts = processDeployment(build, listener, currentTotalDeployedCounter);
            } catch (ArtifactDeployerException ae) {
                listener.getLogger().println("[ArtifactDeployer] - [ERROR] - Failed to deploy. " + ae.getMessage());
                if (ae.getCause() != null) {
                    listener.getLogger().println("[ArtifactDeployer] - [ERROR] - " + ae.getCause().getMessage());
                }
                build.setResult(Result.FAILURE);
                return false;
            }

            deployedArtifactsAction.addDeployedArtifacts(deployedArtifacts);
            listener.getLogger().println("[ArtifactDeployer] - Stopping deployment from the post-action...");
        }
        return true;
    }

    private Map<Integer, List<ArtifactDeployerVO>> processDeployment(AbstractBuild<?, ?> build, final BuildListener listener, int currentNbDeployedArtifacts) throws IOException, InterruptedException {

        Map<Integer, List<ArtifactDeployerVO>> deployedArtifacts = new HashMap<Integer, List<ArtifactDeployerVO>>();
        FilePath workspace = build.getWorkspace();

        int numberOfCurrentDeployedArtifacts = currentNbDeployedArtifacts;
        for (final ArtifactDeployerEntry entry : entries) {

            if (entry.getRemote() == null) {
                throw new ArtifactDeployerException("All remote directories must be set.");
            }

            final String includes = build.getEnvironment(listener).expand(entry.getIncludes());
            final String excludes = build.getEnvironment(listener).expand(entry.getExcludes());
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


            ArtifactDeployerCopy deployerCopy =
                    new ArtifactDeployerCopy(listener, includes, excludes, flatten, outputFilePath, numberOfCurrentDeployedArtifacts);
            List<ArtifactDeployerVO> results = workspace.act(deployerCopy);
            numberOfCurrentDeployedArtifacts += results.size();
            deployedArtifacts.put(entry.getUniqueId(), results);
        }
        return deployedArtifacts;
    }

    @SuppressWarnings("unused")
    public List<ArtifactDeployerEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ArtifactDeployerEntry> entries) {
        this.entries = entries;
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
            ArtifactDeployerPublisher instance = null;
            while (it.hasNext()) {
                Publisher publisher = it.next();
                if (ArtifactDeployerDescriptor.DISPLAY_NAME.equals(publisher.getDescriptor().getDisplayName())) {
                    instance = (ArtifactDeployerPublisher) publisher;
                }
            }

            if (instance != null) {
                DeployedArtifacts deployedArtifacts = build.getAction(DeployedArtifacts.class);
                if (deployedArtifacts != null) {
                    Map<Integer, List<ArtifactDeployerVO>> info = deployedArtifacts.getDeployedArtifactsInfo();
                    if (info != null) {
                        for (ArtifactDeployerEntry entry : instance.getEntries()) {
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


        private ArtifactDeployerEntry populateAndGetEntry(JSONObject element) {
            ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
            entry.setExcludes(Util.fixEmpty(element.getString("excludes")));
            entry.setIncludes(Util.fixEmpty(element.getString("includes")));
            entry.setRemote(Util.fixEmpty(element.getString("remote")));
            entry.setDeleteRemote(element.getBoolean("deleteRemote"));
            entry.setFlatten(element.getBoolean("flatten"));
            entry.setDeleteRemoteArtifacts(element.getBoolean("deleteRemoteArtifacts"));
            Object deleteRemoteArtifactsObject = element.get("deleteRemoteArtifactsByScript");
            if (deleteRemoteArtifactsObject == null) {
                entry.setDeleteRemoteArtifactsByScript(false);
            } else {
                entry.setDeleteRemoteArtifactsByScript(true);
                entry.setGroovyExpression(Util.fixEmpty(element.getJSONObject("deletedRemoteArtifacts").getString("groovyExpression")));
            }
            return entry;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ArtifactDeployerPublisher pub = new ArtifactDeployerPublisher();
            List<ArtifactDeployerEntry> artifactDeployerEntries = new ArrayList<ArtifactDeployerEntry>();
            Object entries = formData.get("deployedArtifact");
            if (entries != null) {
                if (entries instanceof JSONObject) {
                    artifactDeployerEntries.add(populateAndGetEntry((JSONObject) entries));
                } else {
                    JSONArray jsonArray = (JSONArray) entries;
                    Iterator it = jsonArray.iterator();
                    while (it.hasNext()) {
                        artifactDeployerEntries.add(populateAndGetEntry((JSONObject) it.next()));
                    }
                }
            }
            pub.setEntries(artifactDeployerEntries);
            return pub;
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
