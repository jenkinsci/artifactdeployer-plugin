package org.jenkinsci.plugins.artifactdeployer;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.artifactdeployer.exception.ArtifactDeployerException;
import org.jenkinsci.plugins.artifactdeployer.service.LocalCopy;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
public class ArtifactDeployerPublisher extends Recorder implements Serializable {

    private List<ArtifactDeployerEntry> entries = Collections.emptyList();

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Arrays.asList(new ArtifactDeployerProjectAction(project));
    }

    @Override
    public boolean perform(hudson.model.AbstractBuild<?, ?> build, hudson.Launcher launcher, hudson.model.BuildListener listener) throws java.lang.InterruptedException, java.io.IOException {

        if (build.getResult().isBetterOrEqualTo(Result.SUCCESS)) {

            listener.getLogger().println("[ArtifactDeployer] - Starting deployment...");
            final FilePath workspace = build.getWorkspace();
            Map<String, List<ArtifactDeployerVO>> deployedArtifacts;
            try {
                deployedArtifacts = processDeployment(build, listener, workspace);
            } catch (ArtifactDeployerException ae) {
                listener.getLogger().println("[ArtifactDeployer] - Failed to deploy " + ae.getMessage());
                build.setResult(Result.FAILURE);
                return false;
            }

            DeployedArtifacts deployedArtifactsAction = new DeployedArtifacts(deployedArtifacts);
            build.addAction(deployedArtifactsAction);
            listener.getLogger().println("[ArtifactDeployer] - Stopping deployment...");
        }
        return true;
    }

    private Map<String, List<ArtifactDeployerVO>> processDeployment(AbstractBuild<?, ?> build, final BuildListener listener, FilePath workspace) throws IOException, InterruptedException {
        Map<String, List<ArtifactDeployerVO>> deployedArtifacts = new HashMap<String, List<ArtifactDeployerVO>>();

        //Build and deploy artifact
        int totalDeployedCounter = 0;
        for (final ArtifactDeployerEntry entry : entries) {

            final String includes = build.getEnvironment(listener).expand(entry.getIncludes());
            final String excludes = build.getEnvironment(listener).expand(entry.getExcludes());
            final String outputPath = build.getEnvironment(listener).expand(entry.getRemote());
            final boolean flatten = entry.isFlatten();
            final int forCounter = totalDeployedCounter;

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
            List<ArtifactDeployerVO> results = workspace.act(new FilePath.FileCallable<List<ArtifactDeployerVO>>() {
                public List<ArtifactDeployerVO> invoke(File localWorkspace, VirtualChannel channel) throws IOException, InterruptedException {

                    LocalCopy localCopy = new LocalCopy();
                    FileSet fileSet = Util.createFileSet(localWorkspace, includes, excludes);

                    int inputFiles = fileSet.size();
                    List<File> outputFilesList = localCopy.copyAndGetNumbers(fileSet, flatten, new File(outputFilePath.getRemote()));
                    if (inputFiles != outputFilesList.size()) {
                        listener.getLogger().println(String.format("[ArtifactDeployer] - All the files have not been deployed. There was %d input files but only %d was copied. Maybe you have to use 'Delete content of remote directory' feature for deleting remote directory before deploying.", inputFiles, outputFilesList.size()));
                    } else {
                        listener.getLogger().println(String.format("[ArtifactDeployer] - %d file(s) have been copied from the workspace to '%s'.", outputFilesList.size(), outputPath));
                    }

                    int localJobDeployedFileCounter = forCounter;
                    List<ArtifactDeployerVO> deployedArtifactsResultList = new LinkedList<ArtifactDeployerVO>();
                    for (File renoteFile : outputFilesList) {
                        ArtifactDeployerVO deploymentResultEntry = new ArtifactDeployerVO();
                        deploymentResultEntry.setId(localJobDeployedFileCounter);
                        localJobDeployedFileCounter++;
                        deploymentResultEntry.setDeployed(true);
                        deploymentResultEntry.setFileName(renoteFile.getName());
                        deploymentResultEntry.setRemotePath(renoteFile.getPath());

                        deployedArtifactsResultList.add(deploymentResultEntry);
                    }
                    return deployedArtifactsResultList;
                }
            });
            totalDeployedCounter += results.size();
            deployedArtifacts.put(entry.getId(), results);
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

        private Logger logger = Logger.getLogger(DeleteRemoteArtifact.class.getName());

        @Override
        public void onDeleted(AbstractBuild build) {

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
                Map<String, List<ArtifactDeployerVO>> info = deployedArtifacts.getDeployedArtifactsInfo();
                if (info != null) {
                    for (ArtifactDeployerEntry entry : instance.getEntries()) {
                        //Delete output
                        if (entry.isDeleteRemoteArtifacts()) {
                            List<ArtifactDeployerVO> listArtifacts = info.get(entry.getId());
                            for (ArtifactDeployerVO vo : listArtifacts) {
                                FilePath remoteArtifactPath = new FilePath(build.getWorkspace().getChannel(), vo.getRemotePath());
                                try {
                                    if (remoteArtifactPath.exists()) {
                                        remoteArtifactPath.deleteRecursive();
                                    }

                                    if (remoteArtifactPath.getParent().exists() && remoteArtifactPath.getParent().list().size()==0){
                                        remoteArtifactPath.getParent().delete();
                                    }

                                } catch (IOException ioe) {
                                    logger.log(Level.SEVERE, "Error when deleting artifacts.", ioe);
                                } catch (InterruptedException ie) {
                                    logger.log(Level.SEVERE, "Error when deleting artifacts.", ie);
                                }
                            }

                        }

                        //Execute the script for deletion
                        if (entry.isDeleteRemoteArtifactsByScript()) {
                            //Inject list artifacts as variable
                            Binding binding = new Binding();
                            if (deployedArtifacts != null) {
                                List<ArtifactDeployerVO> listArtifacts = info.get(entry.getId());
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
    public static final class ArtifactDeployerDescriptor extends BuildStepDescriptor<Publisher> {

        public static final String DISPLAY_NAME = Messages.depployerartifact_displayName();

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            ArtifactDeployerPublisher pub = new ArtifactDeployerPublisher();
            List<ArtifactDeployerEntry> artifactDeployerEntries = new ArrayList<ArtifactDeployerEntry>();
            Iterator it = formData.entrySet().iterator();
            int counter = 0;
            while (it.hasNext()) {
                Map.Entry<String, JSONObject> map = (Map.Entry<String, JSONObject>) it.next();
                JSONObject element = map.getValue();
                ArtifactDeployerEntry entry = new ArtifactDeployerEntry();
                ++counter;
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
                entry.setId(String.valueOf(counter));
                artifactDeployerEntries.add(entry);
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
                throw FormValidation.error("Remote directory is mandatory.");
            }
            return FormValidation.ok();
        }

    }
}
