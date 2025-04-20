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

        double carrierFreq = 852.0;
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

        // Logarithmic fade-in and fade-out across the entire sample length
        for (int i = 0; i < sampleIndex; i++) {
            double pos = (double) i / sampleIndex;
            // Fade in: 0 → 0.5, Fade out: 0.5 → 1.0 (symmetric)
            double logFade = pos < 0.5
                ? Math.log10(1 + 9 * pos * 2) / Math.log10(10)
                : Math.log10(1 + 9 * (2 - 2 * pos)) / Math.log10(10);

            int idx = i * 2;
            short sample = (short) (((audio[idx + 1] << 8) |
                    (audio[idx] & 0xFF)) &
                0xFFFF);

            sample = (short) (sample * logFade);

            audio[idx] = (byte) (sample & 0xff);
            audio[idx + 1] = (byte) ((sample >> 8) & 0xff);
        }

        // Write to WAV file
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(bais, format, sampleIndex);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outPath));
    }
}
