package io.github.brt4c3.jenkinsfmsound;

import java.io.*;
import javax.sound.sampled.*;

public class FMTextToWav {

    public static void generateFromString(String text, String outPath)
        throws IOException {
        generateFromString(text, outPath, 1.0, 3.0); // Default stretch=1.0, modDepth=3.0
    }

    public static void generateFromString(
        String text,
        String outPath,
        double stretchFactor,
        double modDepth
    ) throws IOException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(
                "Input text cannot be null or empty."
            );
        }

        String[] chars = text.split("");
        int sampleRate = 44100;
        int fps = 16;
        int frameDuration = (int) (((double) sampleRate / fps) * stretchFactor);
        int totalSamples = frameDuration * chars.length;
        byte[] audio = new byte[totalSamples * 2]; // 16-bit PCM = 2 bytes/sample

        double carrierFreq = 440.0;
        int sampleIndex = 0;

        for (String chStr : chars) {
            char ch = chStr.charAt(0);
            double modIndex = (((int) ch) / 127.0) * modDepth; // allow deeper modulation
            double modFreq = 3.0;

            for (int j = 0; j < frameDuration; j++) {
                double t = (double) j / sampleRate;
                double modSignal = Math.sin(2 * Math.PI * modFreq * t);
                double fmAngle =
                    2 * Math.PI * carrierFreq * t + modIndex * modSignal;

                short sample = (short) (Math.sin(fmAngle) * Short.MAX_VALUE);
                if (sampleIndex * 2 + 1 >= audio.length) break;

                audio[sampleIndex * 2] = (byte) (sample & 0xff);
                audio[sampleIndex * 2 + 1] = (byte) ((sample >> 8) & 0xff);
                sampleIndex++;
            }
        }

        // Logarithmic reverb-style fade-out (1 second)
        int fadeSamples = sampleRate;
        for (int i = 0; i < fadeSamples; i++) {
            int idx = audio.length - (i * 2);
            if (idx - 2 < 0) break;

            double fadeFactor = Math.log10(
                10.0 - (9.0 * i) / (double) fadeSamples
            ); // log scale
            audio[idx - 2] = (byte) (audio[idx - 2] * fadeFactor);
            audio[idx - 1] = (byte) (audio[idx - 1] * fadeFactor);
        }

        // Write to WAV file
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(bais, format, sampleIndex);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outPath));
    }
}
