package com.xayup.audio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioFile {
    public static short[] readyAudioFile(File audio_file) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(audio_file);
        //Ready file
        byte[] bytes = new byte[fis.available()];
        while(fis.read(bytes, 0, bytes.length) != -1);
        //Close inputs stream
        fis.close();
        //Convert to 16 bits
        short[] samples = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        return samples;
    }
}
