package com.example.lzh.recorder;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.example.lzh.recorder.manager.AudioTrackManager;
import com.example.lzh.recorder.recorder.Callback;
import com.example.lzh.recorder.recorder.Recorder;
import com.example.lzh.recorder.utils.PcmToWavUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Recorder recorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private byte[] tempBuffer;
    FileOutputStream outputStream;
    //wav格式也是这个文件夹
    String pcmPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.pcm";
    private int timeInterval = 500;//倒计时时间间隔

    private Button btStart;
    private Button btName;
    private TextView tvContent;
    private EditText etWucha;
    private Button btWucha;
    private int countDown = 0;
    private boolean song = true;//true代表欢乐颂false代表小星星
    private double wucha = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btStart = (Button) findViewById(R.id.start_bt);
        btName = (Button) findViewById(R.id.name_bt);
        tvContent = (TextView) findViewById(R.id.content_tv);
        etWucha = (EditText) findViewById(R.id.wucha_et);
        btWucha = (Button) findViewById(R.id.wucha_bt);
        initData();
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvContent.setText("倒计时：" + "3");
                // playAudio();
                handler.sendEmptyMessageDelayed(Constants.WAIT, timeInterval);
                btStart.setEnabled(false);
            }
        });
        btName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song = !song;
                if (song) {
                    Constants.sampleRate = 22050;
                    Constants.SONG_COUNT = 32;
                    btName.setText("欢乐颂");
                } else {
                    Constants.sampleRate = 16000;
                    Constants.SONG_COUNT = 48;
                    btName.setText("小星星");
                }
            }
        });
        btWucha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wucha = Double.valueOf(etWucha.getText().toString());
            }
        });
    }

    private void startRecord() {
        FileUtils.deleteFile(pcmPath);
        FileUtils.createOrExistsFile(pcmPath);
        try {
            outputStream = new FileOutputStream(pcmPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        recorder.start();
        isRecording = true;
    }

    private void stopRecord() {
        recorder.stop();
        stopPlay();
        isRecording = false;
    }

    private void initData() {
        etWucha.setText(String.valueOf(wucha));
        FileUtils.createOrExistsFile(pcmPath);
        try {
            outputStream = new FileOutputStream(pcmPath);
            recorder = new Recorder(new Callback() {
                @Override
                public void onBufferAvailable(byte[] buffer) {
                    //2、save file
                    try {
                        outputStream.write(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        handler.sendEmptyMessage(100);
                        outputStream.close();
                        String wavPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.wav";
                        FileUtils.deleteFile(wavPath);
                        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil();
                        pcmToWavUtil.pcmToWav(pcmPath, wavPath);
                        AudioTrackManager.getInstance().readAudioFile(MainActivity.this, wavPath, handler);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        //这里是解析本地文件 就是我们目前用的算法
    }

    private void playAudio() {
        try {
            //点击开始按钮之后播放背景音乐，不需要点结束 音乐放完自动结束
            AssetFileDescriptor afd = null;
            if (!song) {
                afd = getAssets().openFd("c.wav");
            } else {
                afd = getAssets().openFd("huanlsesongbanzou.wav");
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopRecord();
                }
            });
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private List<Double> data = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.GET_BASE:
                    data.add((Double) msg.obj);
                    if (data.size() == Constants.SONG_COUNT * 8) {
                        getScore();
                    }
                    break;
                case Constants.WAIT:
                    countDown++;
                    if (3 - countDown > 0) {
                        tvContent.setText("倒计时：" + String.valueOf(3 - countDown));
                        handler.sendEmptyMessageDelayed(Constants.WAIT, timeInterval);
                    } else {
                        playAudio();
                        startRecord();
                        countDown = 0;
                        tvContent.setText("录音中");
                    }
                    break;
                case 100:
                    btStart.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };

    private void getScore() {
        Map<Integer, List<Double>> map = new HashMap<>();
        for (int i = 0; i < Constants.SONG_COUNT; i++) {
            List<Double> list = new ArrayList<>();
            for (int k = 0; k < 8; k++) {
                list.add(data.get(i * 8 + k));
            }
            map.put(i, list);
        }
        score(map);

    }

    public static final double[] C_FREQS = {261.63, 261.63, 392.00, 392.00, 440.00, 440.00, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63, 392.00, 392.00, 440.00, 440.00, 392.00, 392.00, 349.23, 349.23, 329.63, 329.63, 293.66, 293.66, 261.63, 261.63};
    public static final double[] F_FREQS = {220, 220, 233.08, 261.63, 261.63, 233.08, 220, 196, 174.61, 176.61, 196, 220, 220, 196, 196, 196, 220, 220, 233.08, 261.63, 261.63, 233.08, 220, 196, 174.61, 176.61, 196, 220, 192, 174.61, 174.61, 174.61,};

    private void score(Map<Integer, List<Double>> mapAnswer) {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        int f = 0;
        int g = 0;
        if (song) {//欢乐颂
            for (int i = 0; i < F_FREQS.length; i++) {

                for (double freq : mapAnswer.get(i)) {
                    if (Math.abs(freq - F_FREQS[i] / 32) < wucha) {//A调
                        a++;
                    }
                    if (Math.abs(freq - F_FREQS[i] / 16) < wucha) {//B调
                        b++;
                    }
                    if (Math.abs(freq - F_FREQS[i] / 8) < wucha) {//C调
                        c++;
                    }
                    if (Math.abs(freq - F_FREQS[i] / 4) < wucha) {//D调
                        d++;
                    }
                    if (Math.abs(freq - F_FREQS[i] / 2) < wucha) {//E调
                        e++;
                    }
                    if (Math.abs(freq - F_FREQS[i]) < wucha) {//F调
                        f++;
                    }
                    if (Math.abs(freq - F_FREQS[i] * 2) < wucha) {//G调
                        g++;
                    }
                }

            }
        } else {
            for (int i = 0; i < C_FREQS.length; i++) {
                for (double freq : mapAnswer.get(i)) {
                    if (Math.abs(freq - C_FREQS[i] / 4) < wucha) {//A调
                        a++;
                    }
                    if (Math.abs(freq - C_FREQS[i] / 2) < wucha) {//B调
                        b++;
                    }
                    if (Math.abs(freq - C_FREQS[i]) < wucha) {//C调
                        c++;
                    }
                    if (Math.abs(freq - C_FREQS[i] * 2) < wucha) {//D调
                        d++;
                    }
                    if (Math.abs(freq - C_FREQS[i] * 4) < wucha) {//E调
                        e++;
                    }
                    if (Math.abs(freq - C_FREQS[i] * 8) < wucha) {//F调
                        f++;
                    }
                    if (Math.abs(freq - C_FREQS[i] * 16) < wucha) {//G调
                        g++;
                    }
                }

            }
        }

        int max = 0;
        String maxY = "";
        Log.e("SCORE", "A=" + a + "B=" + b + "C=" + c + "D=" + d + "E=" + e + "F=" + f + "G=" + g);

        if (a > max) {
            max = a;
            maxY = "A";
        }
        if (b > max) {
            max = b;
            maxY = "B";
        }
        if (c > max) {
            max = c;
            maxY = "C";
        }
        if (d > max) {
            max = d;
            maxY = "D";
        }
        if (e > max) {
            max = e;
            maxY = "E";
        }
        if (f > max) {
            max = f;
            maxY = "F";
        }
        if (g > max) {
            max = g;
            maxY = "G";
        }
        String showText;
        if (song) {
            showText = "总得分" + max * 100 / (8 * 32) + "此得分是由" + maxY + "调计算得出";
        } else {
            showText = "总得分" + max * 100 / (8 * 48) + "此得分是由" + maxY + "调计算得出";
        }
        tvContent.setText(showText);
        data.clear();
    }
}
