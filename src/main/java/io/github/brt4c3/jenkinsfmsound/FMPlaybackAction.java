package io.github.brt4c3.jenkinsfmsound;

import hudson.model.Action;
import hudson.model.Run;

public class FMPlaybackAction implements Action {

    private final String audioFileName;

    public FMPlaybackAction(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public String getIconFileName() {
        return "speaker.png"; // or return null to hide
    }

    public String getDisplayName() {
        return "FM Sound Playback";
    }

    public String getUrlName() {
        return "fm-playback";
    }
}
