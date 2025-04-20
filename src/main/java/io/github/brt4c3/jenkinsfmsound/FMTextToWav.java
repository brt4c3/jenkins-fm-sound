package io.github.brt4c3.jenkinsfmsound;

import java.io.*;
import javax.sound.sampled.*;

public class FMTextToWav {

    public static void generateFromString(String text, String outPath)
        throws IOException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(
                "Input text cannot be null or empty."
            );
        }

        String[] chars = text.split("");

        int sampleRate = 44100; // Standard audio sample rate
        int fps = 24; // Frames per second
        int frameDuration = sampleRate / fps;
        int totalSamples = frameDuration * chars.length;
        byte[] audio = new byte[totalSamples * 2]; // 16-bit audio = 2 bytes/sample

        double carrierFreq = 440.0; // A4 tone
        int sampleIndex = 0;

        for (String chStr : chars) {
            char ch = chStr.charAt(0);
            double modIndex = ((int) ch) / 127.0; // Normalize ASCII to 0.0–1.0
            double modFreq = 3.0; // Slow modulation rate

            for (int j = 0; j < frameDuration; j++) {
                double t = (double) j / sampleRate;
                double modSignal = Math.sin(2 * Math.PI * modFreq * t);
                double fmAngle =
                    2 * Math.PI * carrierFreq * t + modIndex * modSignal;

                short sample = (short) (Math.sin(fmAngle) * Short.MAX_VALUE);
                audio[sampleIndex++] = (byte) (sample & 0xff);
                audio[sampleIndex++] = (byte) ((sample >> 8) & 0xff);
            }
        }

        // Linear fade out over the last 1/4 second
        int fadeSamples = Math.min(sampleRate / 4, audio.length / 2);
        for (int i = 0; i < fadeSamples; i++) {
            int idx = audio.length - (i * 2);
            if (idx - 2 < 0) {
                break; // Avoid negative index
            }

            float fadeFactor = 1.0f - (float) i / fadeSamples;
            audio[idx - 2] = (byte) (audio[idx - 2] * fadeFactor);
            audio[idx - 1] = (byte) (audio[idx - 1] * fadeFactor);
        }

        // Write to WAV file
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(bais, format, totalSamples);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outPath));
    }
}
