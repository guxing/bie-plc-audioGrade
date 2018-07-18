package com.example.winbo.audiotest6.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.*;
import com.example.winbo.audiotest6.Constants;
import com.example.winbo.audiotest6.utils.calculators.AudioCalculator;
import com.example.winbo.audiotest6.utils.calculators.FrequencyCalculator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by winbo on 2017/9/3.
 */

public class AudioTrackManager {
    public static final String TAG = "AudioTrackManager";
    private static final int SAMPLE_RATE = 44100;//Hz，采样频率
    private static final double FREQUENCY = 500; //Hz，标准频率（这里分析的是500Hz）
    private static final double RESOLUTION = 10; //Hz，误差

    private AudioTrack audioTrack;
    private DataInputStream dis;
    private Thread recordThread;
    private boolean isStart = false;
    private static AudioTrackManager mInstance;
    private int bufferSize;

    private Handler handler;
    private byte[] tempBufferFreq;
    private int bufferCounter = 0;

    public AudioTrackManager() {
        bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2, AudioTrack.MODE_STREAM);
    }

    /**
     * 获取单例引用
     *
     * @return
     */
    public static AudioTrackManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioTrackManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (null != recordThread && Thread.State.RUNNABLE == recordThread.getState()) {
                try {
                    Thread.sleep(500);
                    recordThread.interrupt();
                } catch (Exception e) {
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }

    /**
     * 启动播放线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }

    /**
     * 播放线程
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[bufferSize];
                int readCount = 0;
                audioTrack.play();
                while (dis.available() > 0) {
                    readCount = dis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {
                        audioTrack.write(tempBuffer, 0, readCount);
                        getFreq(tempBuffer);
                    }
                }
                stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void getFreq(byte[] buffer) {
        if (null == tempBufferFreq) {
            tempBufferFreq = new byte[2048 * 96];
        }
        System.arraycopy(buffer, 0, tempBufferFreq, 0, buffer.length);
        bufferCounter++;
        if (96 == bufferCounter) {
            double freq = Frequency.getFreq(tempBufferFreq);
            bufferCounter = 0;
            tempBufferFreq = null;
            handler.sendMessage(handler.obtainMessage(Constants.GET_BASE, freq));
        }
    }


    /**
     * 播放文件
     *
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        dis = new DataInputStream(new FileInputStream(file));
    }

    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path, Handler handler) {
        try {
            this.handler = handler;
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        try {
            destroyThread();
            if (audioTrack != null) {
                if (audioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioTrack.stop();
                }
                if (audioTrack != null) {
                    audioTrack.release();
                }
            }
            if (dis != null) {
                dis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /****************add by winbo********************/

    public void readAudioFile(final String filePath, final Handler handler) {
        if (StringUtils.isEmpty(filePath) || null == handler) {
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(Constants.CLEAR_BASE);
                File file = new File(filePath);
                LogUtils.i("winbo", "readAudioFile file.length():" + file.length());
                byte buffer[] = new byte[Constants.BUFFER_SIZE];//一拍的长度
                try {
                    FileInputStream in = new FileInputStream(file);
                    in.skip(44);
                    while (in.read(buffer) >= 0) {
                        //方案1
//                        double freq = Frequency.getFreq(buffer);

                        //方案2
                        AudioCalculator calculator = new AudioCalculator(buffer);
                        double freq = calculator.getFrequency();
                        handler.sendMessage(handler.obtainMessage(Constants.GET_BASE, freq));
                    }
                    in.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static final double[] C_FREQS = {261.63, 261.63, 392.00, 392.00, 440.00, 440.00, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63, 392.00, 392.00, 440.00, 440.00, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63};

    public void readAudioFile(final Context context, final String fileName, final Handler handler) {
        if (StringUtils.isEmpty(fileName) || null == handler) {
            return;
        }
        Log.i("winbo", "AudioCalculator lenth:" + C_FREQS.length);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里的size就是文件大小减去44之后 再除频率个数
                byte buffer[] = new byte[Constants.BUFFER_SIZE ];//一拍的长度
                try {
                    InputStream in = context.getAssets().open(fileName);
                    in.skip(44);
                    int i = 1;

                    while (in.read(buffer) >= 0) {
                        //方案1
                        double freq = Frequency.getFreq(buffer);
                    /*    byte[][] result = stereo2MonoFor16Bit(buffer);
                        byte[] bu = result[1];
                        //方案2
                        AudioCalculator calculator = new AudioCalculator(bu);
                        double freq = calculator.getFrequency();*/
                        Log.i("winbo", "AudioCalculator" + i++ + "freq:" + freq);

                        handler.sendMessage(handler.obtainMessage(Constants.GET_BASE, freq));
                    }
                    in.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static byte[][] stereo2MonoFor16Bit(byte[] stereoBytes) throws IOException {

        byte[][] objList = new byte[2][];

        try (ByteArrayOutputStream outputStreamL = new ByteArrayOutputStream()) {
            try (ByteArrayOutputStream outputStreamR = new ByteArrayOutputStream()) {

                for (int i = 0; i < stereoBytes.length; i = i + 4) {
                    outputStreamL.write(stereoBytes[i]);
                    outputStreamL.write(stereoBytes[i + 1]);
                    outputStreamR.write(stereoBytes[i + 2]);
                    outputStreamR.write(stereoBytes[i + 3]);
                }
                outputStreamL.flush();
                outputStreamR.flush();
                objList[0] = outputStreamL.toByteArray();
                objList[1] = outputStreamR.toByteArray();
            }
        }
        return objList;
    }
}
