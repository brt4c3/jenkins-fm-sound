package io.github.brt4c3.jenkinsfmsound;

public class FMPlaybackAction {

    private String message;

    // Default constructor (no arguments)
    public FMPlaybackAction() {
        // Default constructor logic (if any)
    }

    // Constructor that accepts a String (matches the FMSoundRecorder usage)
    public FMPlaybackAction(String message) {
        this.message = message;
    }

    public void playSound() {
        try {
            // Using the message in the playSound method or another logic
            System.out.println(message);
            // Sound-playing logic here

        } catch (Exception e) {
            // Handle any exception that might be thrown
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Other methods you might need
    public void stopSound() {
        try {
            // Code that may throw an exception
            if (message == null || message.isEmpty()) {
                throw new RuntimeException("No message provided for playback.");
            }

            // Logic to stop the sound
            System.out.println("Stopping sound with message: " + message);
        } catch (Exception e) {
            // Catch the general exception
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter and setter for message if needed
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
