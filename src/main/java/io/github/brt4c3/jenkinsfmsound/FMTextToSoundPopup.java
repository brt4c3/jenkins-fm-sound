package io.github.brt4c3.jenkinsfmsound;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.*;

public class FMTextToSoundPopup {

    public static void playFromConsoleOutput(String text) throws LineUnavailableException {
        int sampleRate = 44100;
        double duration = 2.5;
        int totalSamples = (int) (sampleRate * duration);
        float[] carrierSignal = generateTextCarrierSignal(text, totalSamples, sampleRate);
        float[] modulatorSignal = generateCinematicModulatorSignal(totalSamples, sampleRate);
        byte[] audio = new byte[totalSamples * 2]; // 16-bit PCM

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;
            double angle = carrierSignal[i] + modulatorSignal[i];
            double sample = Math.sin(angle) * Math.exp(-2.5 * t); // fade-out envelope
            short pcm = (short) (sample * Short.MAX_VALUE);
            audio[i * 2] = (byte) (pcm & 0xff);
            audio[i * 2 + 1] = (byte) ((pcm >> 8) & 0xff);
        }

        // Play audio buffer
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(audio, 0, audio.length);
            line.drain();
        }

        // Show a simple popup
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(
                null,
                "Jenkins console output played as sound!",
                "FM Synth Playback",
                JOptionPane.INFORMATION_MESSAGE
            )
        );
    }

    private static float[] generateTextCarrierSignal(String text, int totalSamples, int sampleRate) {
        float[] signal = new float[totalSamples];
        double fps = (double) text.length() / 2.5;
        int frameSize = (int) (sampleRate / fps);
        double baseFreq = 852.0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            double modIndex = (((int) ch) / 127.0) * 2.5;

            for (int j = 0; j < frameSize; j++) {
                int idx = i * frameSize + j;
                if (idx >= totalSamples) break;
                double t = (double) idx / sampleRate;
                double modSignal = Math.sin(2 * Math.PI * 3.0 * t);
                signal[idx] = (float) (2 * Math.PI * baseFreq * t + modIndex * modSignal);
            }
        }
        return signal;
    }

    private static float[] generateCinematicModulatorSignal(int totalSamples, int sampleRate) {
        float[] signal = new float[totalSamples];
        double baseFreq = 60.0;
        double endFreq = 180.0;
        double modFreq = 3.0;

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;
            double freq = baseFreq + (endFreq - baseFreq) * Math.pow(t / 2.5, 0.7);
            double modSignal = Math.sin(2 * Math.PI * modFreq * t);
            double fmAngle = 2 * Math.PI * freq * t + 8.0 * modSignal;
            double raw = Math.sin(fmAngle);
            double harmonic = 0.3 * Math.sin(2 * Math.PI * freq * 2 * t) * Math.exp(-4 * t);

            signal[i] = (float) (Math.tanh(2.5 * raw) * Math.exp(-2.5 * t) + harmonic);
        }
        return signal;
    }

    public static void main(String[] args) {
        try {
            String jenkinsOutput = "Build SUCCESSFUL. All tests passed.";
            playFromConsoleOutput(jenkinsOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
