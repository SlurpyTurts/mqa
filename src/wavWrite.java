/**
 * Created by Slurpy on 1/12/17.
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class wavWrite
{
    public static void main(String[] args) throws IOException {


        wavFileConfig wavFile = new wavFileConfig(96000.0, 16, 10);
        Signal sig1 = new Signal(440,0.5);
        Signal sig2 = new Signal(442,0.5);

        int bufferLength = (int)(wavFile.duration * wavFile.sampleRate);
        float[] buffer = new float[bufferLength];
        float[] bufferL = new float[bufferLength];
        float[] bufferR = new float[bufferLength];

        double time;

        for (int sample = 0; sample < bufferL.length; sample++)
        {
            time = sample / wavFile.sampleRate;
            bufferL[sample] = (float) (sig1.amplitude * Math.sin(sig1.omega * time));
            bufferR[sample] = (float) (sig2.amplitude * Math.sin(sig2.omega * time));
            buffer[sample] = (bufferL[sample]+bufferR[sample]);
        }
/*
        final byte[] byteBuffer = new byte[buffer.length * 2];
        int bufferIndex = 0;
        int sampling = 1;
        for (int i = 0; i < byteBuffer.length; i++) {

            final int x = (int) (buffer[bufferIndex++] * Math.pow(2,(wavFile.bitDepth-1))-1);
            if(i%sampling == 0){
                System.out.print(x+"  ");
                //System.out.println();
            }
            byteBuffer[i] = (byte) x;
            if(i%sampling == 0){
                System.out.print(byteBuffer[i]+"  ");
                //System.out.println();

            }
            i++;
            byteBuffer[i] = (byte) (x >>> 8);
            if(i%sampling == 0){
                System.out.print(byteBuffer[i]+"  ");
                System.out.println();
            }

        }
*/


        final byte[] byteBuffer = new byte[buffer.length * 2];
        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {

            final int x = (int) (buffer[bufferIndex++] * Math.pow(2,(wavFile.bitDepth-1))-1);
            byteBuffer[i] = (byte) x;
            i++;
            byteBuffer[i] = (byte) (x >>> 8);

        }


        File out = new File("out freq"+sig1.frequency+" sample"+(int)wavFile.sampleRate+".wav");

        AudioFormat format = new AudioFormat((float)wavFile.sampleRate, wavFile.bitDepth, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
        audioInputStream.close();

        System.out.print("File "+out+" saved successfully");
    }

    public static class Signal {
        public double frequency = 0.0;
        public double amplitude = 1.0;
        public double omega = 2 * Math.PI * frequency;
        public float[] buffer;

        public Signal(double f, double a) {
            frequency = f;
            amplitude = a;
            omega = 2 * Math.PI * f;
        }

        public Signal(double f){
            frequency = f;
            amplitude = 1;
            omega = 2 * Math.PI * f;
        }
    }

    public static class wavFileConfig{
        public double sampleRate = 44100.0;
        public double duration = 10.0;
        public int bitDepth = 16;

        public wavFileConfig(double sample, int depth, double sec) {
            sampleRate = sample;
            bitDepth = depth;
            duration = sec;
        }
    }
}
