package io.github.brt4c3.jenkinsfmsound;

import java.io.*;
import javax.sound.sampled.*;

public class FMTextToWav {

    public static void generateFromString(String text, String outPath)
        throws IOException {
        int sampleRate = 44100;
        double duration = 5;
        int totalSamples = (int) (sampleRate * duration);
        float[] carrierSignal = generateTextCarrierSignal(
            text,
            totalSamples,
            sampleRate
        );
        float[] modulatorSignal = generateCinematicModulatorSignal(
            totalSamples,
            sampleRate
        );
        byte[] audio = new byte[totalSamples * 2]; // 16-bit PCM

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;

            // Combine the carrier signal and modulator as phase input
            double angle = carrierSignal[i] + modulatorSignal[i];
            double sample = Math.sin(angle);

            // Optional fade-out envelope
            sample *= Math.exp(-3 * t);

            short pcm = (short) (sample * Short.MAX_VALUE);
            audio[i * 2] = (byte) (pcm & 0xff);
            audio[i * 2 + 1] = (byte) ((pcm >> 8) & 0xff);
        }

        // Write to WAV
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(bais, format, totalSamples);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outPath));
    }

    private static float[] generateTextCarrierSignal(
        String text,
        int totalSamples,
        int sampleRate
    ) {
        float[] signal = new float[totalSamples];
        double fps = (double) text.length() / 5;
        int frameSize = (int) (sampleRate / fps);
        double baseFreq = 852.0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            double modIndex = (((int) ch) / 127.0) * 5;

            for (int j = 0; j < frameSize; j++) {
                int idx = i * frameSize + j;
                if (idx >= totalSamples) break;

                double t = (double) idx / sampleRate;
                double modSignal = Math.sin(2 * Math.PI * 3.0 * t);
                signal[idx] = (float) (2 * Math.PI * baseFreq * t +
                    modIndex * modSignal);
            }
        }
        return signal;
    }

    private static float[] generateCinematicModulatorSignal(
        int totalSamples,
        int sampleRate
    ) {
        float[] signal = new float[totalSamples];
        double baseFreq = 60.0;
        double endFreq = 180.0;
        double modFreq = 3.0;

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / sampleRate;
            double freq =
                baseFreq + (endFreq - baseFreq) * Math.pow(t / 2.5, 0.7);
            double modSignal = Math.sin(2 * Math.PI * modFreq * t);
            double fmAngle = 2 * Math.PI * freq * t + 8.0 * modSignal;
            double raw = Math.sin(fmAngle);
            double harmonic =
                0.3 * Math.sin(2 * Math.PI * freq * 2 * t) * Math.exp(-4 * t);

            signal[i] = (float) (Math.tanh(2.5 * raw) * Math.exp(-2.5 * t) +
                harmonic);
        }

        return signal;
    }

    public static void main(String[] args) {
        try {
            generateFromString("MODULATE", "output.wav");
            System.out.println("Modulated FM sound written to output.wav");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
