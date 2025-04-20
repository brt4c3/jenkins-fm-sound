package io.github.brt4c3.jenkinsfmsound;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.File;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.*;

public class FMSoundBuilder extends Builder {

    private final String inputText;

    @DataBoundConstructor
    public FMSoundBuilder(String inputText) {
        this.inputText = inputText;
    }

    public String getInputText() {
        return inputText;
    }

    @Override
    public boolean perform(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener
    ) {
        String resolvedText = Util.replaceMacro(
            inputText,
            build.getBuildVariableResolver()
        );

        if (resolvedText == null || resolvedText.trim().isEmpty()) {
            listener
                .getLogger()
                .println("No input provided. Skipping FM sound generation.");
            return true;
        }

        FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            listener.getLogger().println("Error: Workspace is not available.");
            return false;
        }

        try {
            String outputFile = workspace.getRemote() + "/output.wav";

            listener
                .getLogger()
                .println("Generating FM sound from input: " + resolvedText);
            FMTextToWav.generateFromString(resolvedText, outputFile);
            listener.getLogger().println("FM sound saved to: " + outputFile);
        } catch (Exception e) {
            listener
                .getLogger()
                .println("Error generating FM sound: " + e.getMessage());
            e.printStackTrace(listener.getLogger());
            return false;
        }

        return true;
    }

    @Extension
    public static final class DescriptorImpl
        extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckInputText(@QueryParameter String value) {
            if (value.length() == 0) return FormValidation.error(
                "Please set some input text."
            );
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return "FM Text to WAV Generator";
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
