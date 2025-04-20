package io.github.brt4c3.jenkinsfmsound;

public class FMSoundRecorder {

    public void recordSound() {
        // Creating an instance of FMPlaybackAction with a String parameter
        FMPlaybackAction action = new FMPlaybackAction("Playing sound...");
        try {
            // Call playSound method on the action object
            action.playSound();
        } catch (Exception e) {
            // Handle the general exception
            System.err.println("Error while playing sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
