package com.example.lzh.recorder.manager;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.*;
import com.example.lzh.recorder.Constants;
import com.example.lzh.recorder.utils.Frequency;

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


    public void readAudioFile(final Context context, final String fileName, final Handler handler) {
        if (StringUtils.isEmpty(fileName) || null == handler) {
            return;
        }
        final File file = new File(fileName);
        final double bufferSize =  Math.ceil ((file.length() -44)/Constants.SONG_COUNT/8);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里的size就是文件大小减去44之后 再除频率个数
                byte buffer[] = new byte[(int) bufferSize];//一拍的长度
                try {
                    InputStream in = new FileInputStream(file);
                    in.skip(44);
                    int i = 1;

                    while (in.read(buffer) >= 0) {
                        //方案1
                        double freq = Frequency.getFreq(buffer);
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
}
