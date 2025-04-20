package io.github.brt4c3.jenkinsfmsound;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class FMSoundRecorder extends Builder implements SimpleBuildStep {

    private String inputParam;

    @DataBoundConstructor
    public FMSoundRecorder() {
        // Required by Jenkins
    }

    @DataBoundSetter
    public void setInputParam(String inputParam) {
        this.inputParam = inputParam;
    }

    public String getInputParam() {
        return inputParam;
    }

    @Override
    public void perform(
        @Nonnull Run<?, ?> run,
        @Nonnull FilePath workspace,
        @Nonnull Launcher launcher,
        @Nonnull TaskListener listener
    ) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        if (inputParam == null || inputParam.trim().isEmpty()) {
            logger.println("No input provided. Skipping FM sound generation.");
            return;
        }

        String charSetText = inputParam.trim();
        logger.println("Generating FM audio from input: " + charSetText);

        String fileName = "fmsound_output.wav";
        File outputWav = new File(workspace.getRemote(), fileName);

        try {
            FMTextToWav.generateFromString(
                charSetText,
                outputWav.getAbsolutePath()
            );
            logger.println(
                "FM Sound WAV generated at: " + outputWav.getAbsolutePath()
            );

            // Make sure Jenkins archives this so it can be served
            run.addAction(new FMPlaybackAction(fileName));
        } catch (Exception e) {
            logger.println(
                "Error during FM sound generation: " + e.getMessage()
            );
            throw new IOException("FM generation failed", e);
        }

        logger.println("FM Sound generation complete.");
    }

    @Extension
    public static final class DescriptorImpl
        extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "FM Sound Generator";
        }

        public FormValidation doCheckInputParam(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.warning(
                    "You should provide an input value."
                );
            }
            return FormValidation.ok();
        }
    }
}
