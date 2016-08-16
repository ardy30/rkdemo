package com.android.presentation.app.holder;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.android.lameutil.LameUtil;

public class ExtAudioRecorder {
    //private final static int[] sampleRates = {44100, 22050, 11025, 8000};
    private final static int[] sampleRates = {44100};
    FileOutputStream mMp3FileOutputStream;
    byte[] mMp3Buffer;

    private int encode_data(byte[] b_buffer, int readSize) {
        short[] buffer = new short[readSize / 2];
        for (int i = 0; i < readSize; i += 2) {
            buffer[i / 2] = (short) ((byte) b_buffer[i] + (short) (b_buffer[i + 1] * 0xFF));
        }
        int encodedSize = LameUtil.encode(buffer, buffer, readSize / 2, mMp3Buffer);
        if (encodedSize > 0) {
            try {
                mMp3FileOutputStream.write(mMp3Buffer, 0, encodedSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readSize / 2;
    }

    public void flushAndRelease() {
        //将MP3结尾信息写入buffer中
        final int flushResult = LameUtil.flush(mMp3Buffer);
        if (flushResult > 0) {
            try {
                mMp3FileOutputStream.write(mMp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mMp3FileOutputStream != null) {
                    try {
                        mMp3FileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                LameUtil.close();
            }
        }
    }

    public static ExtAudioRecorder getInstanse(Boolean recordingCompressed) {
        ExtAudioRecorder result = null;
        if (recordingCompressed) {
            result = new ExtAudioRecorder(false,
                    AudioSource.MIC,
                    sampleRates[0],
                    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
        } else {
            int i = 0;
            do {
                result = new ExtAudioRecorder(true,
                        AudioSource.DEFAULT,
                        sampleRates[i],
                        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);
            }
            while ((++i < sampleRates.length) & !(result.getState()
                    == ExtAudioRecorder.State.INITIALIZING));
        }
        return result;
    }

    /**
     * INITIALIZING : recorder is initializing;
     * READY : recorder has been initialized, recorder not yet started
     * RECORDING : recording
     * ERROR : reconstruction needed
     * STOPPED: reset needed
     */
    public enum State {
        INITIALIZING, READY, RECORDING, ERROR, STOPPED
    }

    public static final boolean RECORDING_UNCOMPRESSED = true;
    public static final boolean RECORDING_COMPRESSED = false;
    // The interval in which the recorded samples are output to the file
    // Used only in uncompressed mode
    private static final int TIMER_INTERVAL = 120;
    // Toggles uncompressed recording on/off; RECORDING_UNCOMPRESSED / RECORDING_COMPRESSED
    private boolean rUncompressed;
    // Recorder used for uncompressed recording
    private AudioRecord audioRecorder = null;
    // Stores current amplitude (only in uncompressed mode)
    private int cAmplitude = 0;
    // Output file path
    private String filePath_l = null;
    private String filePath_r = null;
    // Recorder state; see State
    private State state;
    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter_l;
    private RandomAccessFile randomAccessWriter_r;
    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private short nChannels;
    private int sRate;
    private short bSamples;
    private int bufferSize;
    private int aSource;
    private int aFormat;

    // Number of frames written to file on each output(only in uncompressed mode)
    private int framePeriod;

    // Buffer for output(only in uncompressed mode)
    private byte[] buffer;

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private int payloadSize;

    /**
     * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    public State getState() {
        return state;
    }

    /*
    *
    * Method used for recording.
    *
    */
    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        public void onPeriodicNotification(AudioRecord recorder) {
            audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
            byte[] left_buffer = new byte[buffer.length / 2];
            byte[] right_buffer = new byte[buffer.length / 2];
            try {
                int i;
                int j;
                for (i = 0, j = 0; i < buffer.length; i += 4, j += 2) {
                    System.arraycopy(buffer, i, left_buffer, j, 2);
                    System.arraycopy(buffer, i + 2, right_buffer, j, 2);
                }
                randomAccessWriter_l.write(left_buffer); // Write buffer to file
                randomAccessWriter_r.write(right_buffer); // Write buffer to file
                encode_data(left_buffer, buffer.length / 2);
                payloadSize += buffer.length;
                if (bSamples == 16) {
                    for (i = 0; i < buffer.length / 2; i++) { // 16bit sample size
                        short curSample = getShort(buffer[i * 2], buffer[i * 2 + 1]);
                        if (curSample > cAmplitude) { // Check amplitude
                            cAmplitude = curSample;
                        }
                    }
                } else { // 8bit sample size
                    for (i = 0; i < buffer.length; i++) {
                        if (buffer[i] > cAmplitude) { // Check amplitude
                            cAmplitude = buffer[i];
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(ExtAudioRecorder.class.getName(), "Error occured in updateListener, recording is aborted");
                //stop();
            }
        }

        public void onMarkerReached(AudioRecord recorder) {
            // NOT USED
        }
    };

    /**
     * Default constructor
     * <p>
     * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
     * In case of errors, no exception is thrown, but the state is set to ERROR
     */
    public ExtAudioRecorder(boolean uncompressed, int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        try {
            rUncompressed = uncompressed;
            if (rUncompressed) { // RECORDING_UNCOMPRESSED
                if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                    bSamples = 16;
                } else {
                    bSamples = 8;
                }
                if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
                    nChannels = 1;
                } else {
                    nChannels = 2;
                }
                aSource = audioSource;
                sRate = sampleRate;
                aFormat = audioFormat;
                framePeriod = sampleRate * TIMER_INTERVAL / 1000;
                bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
                if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) { // Check to make sure buffer size is not smaller than the smallest allowed one
                    bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    // Set frame period and timer interval accordingly
                    framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
                    Log.w(ExtAudioRecorder.class.getName(), "Increasing buffer size to " + Integer.toString(bufferSize));
                }
                audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
                LameUtil.init(sampleRate, 1, sampleRate, 32, 2);
                mMp3FileOutputStream = new FileOutputStream("/mnt/sdcard/l.mp3");
                mMp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
                if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                    throw new Exception("AudioRecord initialization failed");
                audioRecorder.setRecordPositionUpdateListener(updateListener);
                audioRecorder.setPositionNotificationPeriod(framePeriod);
            }
            cAmplitude = 0;
            filePath_l = null;
            filePath_r = null;
            state = State.INITIALIZING;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }
    }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param argPath_l mic in接口输入的录音文件存放路径
     * @param argPath_r line in接口输入的录音文件存放路径
     */
    public void setOutputFile(String argPath_l, String argPath_r) {
        try {
            if (state == State.INITIALIZING) {
                filePath_l = argPath_l;
                filePath_r = argPath_r;
                randomAccessWriter_l = new RandomAccessFile(filePath_l, "rw");
                randomAccessWriter_r = new RandomAccessFile(filePath_r, "rw");
                randomAccessWriter_l.setLength(0);
                randomAccessWriter_r.setLength(0);
                buffer = new byte[framePeriod * bSamples / 8 * nChannels];
                state = State.READY;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured while setting output path");
            }
            state = State.ERROR;
        }
    }

    /**
     * Returns the largest amplitude sampled since the last call to this method.
     *
     * @return returns the largest amplitude since the last call, or 0 when not in recording state.
     */
    public int getMaxAmplitude() {
        if (state == State.RECORDING) {
            int result = cAmplitude;
            cAmplitude = 0;
            return result;
        } else {
            return 0;
        }
    }


    /**
     *
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * In case uncompressed recording is toggled, the header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     *
     */
/*
    public void pcm2wav(String filePath)
    {
        try
        {
            if (state == State.INITIALIZING)
            {
                if (rUncompressed)
                {
                    if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null))
                    {
                        // write file header

                        randomAccessWriter = new RandomAccessFile(filePath, "rw");

                        randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
                        randomAccessWriter.writeBytes("RIFF");
                        randomAccessWriter.writeInt(0); // Final file size not known yet, write 0 
                        randomAccessWriter.writeBytes("WAVE");
                        randomAccessWriter.writeBytes("fmt ");
                        randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                        randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                        randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                        randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
                        randomAccessWriter.writeInt(Integer.reverseBytes(sRate*bSamples*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeShort(Short.reverseBytes((short)(nChannels*bSamples/8))); // Block align, NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
                        randomAccessWriter.writeBytes("data");
                        randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0

                        buffer = new byte[framePeriod*bSamples/8*nChannels];
                        state = State.READY;
                    }
                    else
                    {
                        Log.e(ExtAudioRecorder.class.getName(), "prepare() method called on uninitialized recorder");
                        state = State.ERROR;
                    }
                }
            }
            else
            {
                Log.e(ExtAudioRecorder.class.getName(), "prepare() method called on illegal state");
                release();
                state = State.ERROR;
            }
        }
        catch(Exception e)
        {
            if (e.getMessage() != null)
            {
                Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            }
            else
            {
                Log.e(ExtAudioRecorder.class.getName(), "Unknown error occured in prepare()");
            }
            state = State.ERROR;
        }
    }
*/

    /**
     * Releases the resources associated with this class, and removes the unnecessary files, when necessary
     */
    public void release() {
        if (state == State.RECORDING) {
            stop();
        } else {
            if ((state == State.READY) & (rUncompressed)) {
                try {
                    randomAccessWriter_l.close(); // Remove prepared file
                    randomAccessWriter_r.close(); // Remove prepared file
                } catch (IOException e) {
                    Log.e(ExtAudioRecorder.class.getName(), "I/O exception occured while closing output file");
                }
                (new File(filePath_l)).delete();
                (new File(filePath_r)).delete();
            }
        }

        if (rUncompressed) {
            if (audioRecorder != null) {
                audioRecorder.release();
            }
        }
    }

    /**
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     */
    public void reset() {
        try {
            if (state != State.ERROR) {
                release();
                filePath_l = null; // Reset file path
                filePath_r = null; // Reset file path
                cAmplitude = 0; // Reset amplitude
                if (rUncompressed) {
                    audioRecorder = new AudioRecord(aSource, sRate, nChannels + 1, aFormat, bufferSize);
                }
                state = State.INITIALIZING;
            }
        } catch (Exception e) {
            Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            state = State.ERROR;
        }
    }

    /**
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     */
    public void start() {
        if (state == State.READY) {
            if (rUncompressed) {
                payloadSize = 0;
                audioRecorder.startRecording();
                audioRecorder.read(buffer, 0, buffer.length);
            }
            state = State.RECORDING;
        } else {
            Log.e(ExtAudioRecorder.class.getName(), "start() called on illegal state");
            state = State.ERROR;
        }
    }

    /**
     * Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file in case of uncompressed recording.
     */
    public void stop() {
        if (state == State.RECORDING) {
            if (rUncompressed) {
                audioRecorder.stop();
/*
                try
                {
                    randomAccessWriter.seek(4); // Write size to RIFF header
                    randomAccessWriter.writeInt(Integer.reverseBytes(36+payloadSize));

                    randomAccessWriter.seek(40); // Write size to Subchunk2Size field
                    randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

                    randomAccessWriter.close();
                }
                catch(IOException e)
                {
                    Log.e(ExtAudioRecorder.class.getName(), "I/O exception occured while closing output file");
                    state = State.ERROR;
                }
*/
            }
            state = State.STOPPED;
        } else {
            Log.e(ExtAudioRecorder.class.getName(), "stop() called on illegal state");
            state = State.ERROR;
        }
    }

    /* 
     * 
     * Converts a byte[2] to a short, in LITTLE_ENDIAN format
     * 
     */
    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }

    // 这里得到可播放的音频文件
    public void pcm2wav(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRates[0];
        int channels = 1;//2;
        long byteRate = 16 * sampleRates[0] * channels / 8;
        byte[] data = new byte[8192];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            System.out.println("payloadSize = " + payloadSize + " totalAudioLen = " + totalAudioLen);
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (1 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}


