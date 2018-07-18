package com.example.winbo.audiotest6;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.winbo.audiotest6.utils.AudioTrackManager;
import com.example.winbo.audiotest6.utils.FitchUtils;
import com.example.winbo.audiotest6.utils.MyAsynTask;
import com.example.winbo.audiotest6.utils.calculators.AudioCalculator;
import com.example.winbo.audiotest6.wav.BaseVisualizerView;
import com.example.winbo.audiotest6.wav.PcmToWavUtil;
import com.example.winbo.audiotest6.utils.core.Callback;
import com.example.winbo.audiotest6.utils.core.Recorder;
import com.example.winbo.audiotest6.view.AnalysisView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private AnalysisView analysisView;
    private TextView start_tv;
    private TextView stop_tv;
    private TextView title_tv;
    private TextView content_tv;
    private Recorder recorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private byte[] tempBuffer;

    private int count = 0;
    FileOutputStream outputStream;
    //wav格式也是这个文件夹
    String pcmPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.pcm";
    private RadioGroup radio_group;
    private CheckBox cb_display;
    private TextView tv_result;

    private int timeInterval = 500;//倒计时时间间隔
    private int countDown = 0;
    private BaseVisualizerView bvv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        radio_group = (RadioGroup) findViewById(R.id.radio_group);
        cb_display = (CheckBox) findViewById(R.id.cb_display);
        tv_result = (TextView) findViewById(R.id.tv_result);
        title_tv = (TextView) findViewById(R.id.title_tv);
        analysisView = (AnalysisView) findViewById(R.id.analysisView);
        bvv = (BaseVisualizerView) findViewById(R.id.bvv);
        start_tv = (TextView) findViewById(R.id.start_tv);
        stop_tv = (TextView) findViewById(R.id.stop_tv);
        content_tv = (TextView) findViewById(R.id.content_tv);
        start_tv.setOnClickListener(new View.OnClickListener() {//开会k歌
            @Override
            public void onClick(View view) {
                if (isRecording) {//停止播放，停止录音
                    handler.sendEmptyMessageDelayed(Constants.STOP_RECORD, timeInterval);
                } else {//开始播放，开始录音
                    title_tv.setText("倒计时：" + "3");
                    playAudio();
                    handler.sendEmptyMessageDelayed(Constants.WAIT, timeInterval);
                }
            }
        });
        stop_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.rbtn_cm:
                        // 识别唱名按钮
                        tv_result.setText("");
                        content_tv.setText("耳机录音效果最棒哦～");
                        SPUtils.getInstance().put("istext", false);
                        break;
                    case R.id.rbtn_text:
                        // 识别歌词按钮
                        tv_result.setText("");
                        content_tv.setText("耳机录音效果最棒哦～");
                        SPUtils.getInstance().put("istext", true);
                        break;
                }
            }
        });
        cb_display.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tv_result.setVisibility(View.VISIBLE);
                    SPUtils.getInstance().put("displayresult", true);
                } else {
                    tv_result.setVisibility(View.GONE);
                    SPUtils.getInstance().put("displayresult", false);
                }
            }
        });
    }

    private void startRecord() {
        content_tv.setText("耳机录音效果最棒哦～");
        tv_result.setText("");
        FileUtils.deleteFile(pcmPath);
        FileUtils.createOrExistsFile(pcmPath);
        try {
            outputStream = new FileOutputStream(pcmPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        recorder.start();
//        AudioTrackManager.getInstance().startPlay(Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/star.wav", handler);
        start_tv.setText("停止");
        isRecording = true;
    }

    private void stopRecord() {
        recorder.stop();
        stopPlay();
        start_tv.setText("开始");
        isRecording = false;
        if (NetworkUtils.isConnected()) {
            content_tv.setText("正在识别中......");
            MyAsynTask myAsynTask = new MyAsynTask(content_tv, this, tv_result);
            myAsynTask.execute();
        } else {
            Toast.makeText(getApplicationContext(), "请检测网络", Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        SPUtils.getInstance().put("istext", false);
        SPUtils.getInstance().put("displayresult", false);
        FileUtils.createOrExistsFile(pcmPath);
        try {
            outputStream = new FileOutputStream(pcmPath);
            recorder = new Recorder(new Callback() {
                @Override
                public void onBufferAvailable(byte[] buffer) {
                    //1、record
                    if (null == tempBuffer) {
                        tempBuffer = new byte[Constants.BUFFER_SIZE];
                    }

                    int bufferRemain = Constants.BUFFER_SIZE - count;
                    System.arraycopy(buffer, 0, tempBuffer, count, bufferRemain >= buffer.length ? buffer.length : bufferRemain);
                    Log.e("--------", tempBuffer.toString());
                    if (bufferRemain >= buffer.length) {
                        count += buffer.length;
                    } else {
//                        double freq = Frequency.getFreq(tempBuffer);
                        AudioCalculator calculator = new AudioCalculator(tempBuffer);
                        double freq = calculator.getFrequency();
                        //Log.i("winbo", "AudioCalculator freq:" + freq);
                        handler.sendMessage(handler.obtainMessage(Constants.GET_FREQ, freq));
                        bvv.onFftDataCapture(tempBuffer);
                        tempBuffer = new byte[Constants.BUFFER_SIZE];
                        count = buffer.length - bufferRemain;
                        System.arraycopy(buffer, bufferRemain, tempBuffer, 0, count);

                    }

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
                        outputStream.close();
//                        WAVManager.makePCMFileToWAVFile(path, Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.wav", true);
                        String wavPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/temp/1.wav";
                        FileUtils.deleteFile(wavPath);
                        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil();
                        pcmToWavUtil.pcmToWav(pcmPath, wavPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        //这里是解析本地文件 就是我们目前用的算法
        AudioTrackManager.getInstance().readAudioFile(MainActivity.this, "3.wav", handler);
    }

    private void playAudio() {
        try {
            //点击开始按钮之后播放背景音乐，不需要点结束 音乐放完自动结束
            AssetFileDescriptor afd = getAssets().openFd("star16000hit.wav");
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.GET_FREQ:
                    double freq = (double) msg.obj;
                    title_tv.setText("音高：" + FitchUtils.getPitch(freq));
                    analysisView.drawFreq(freq);
                    break;
                case Constants.GET_BASE:
                    analysisView.drawBase((Double) msg.obj);
                    break;
                case Constants.CLEAR_FREQ:
                    analysisView.clearFreq();
                    break;
                case Constants.CLEAR_BASE:
                    analysisView.clearBase();
                    break;
                case Constants.WAIT:
                    countDown++;
                    if (3 - countDown > 0) {
                        title_tv.setText("倒计时：" + String.valueOf(3 - countDown));
                        handler.sendEmptyMessageDelayed(Constants.WAIT, timeInterval);
                    } else {
                        title_tv.setText("开始");
                        handler.sendEmptyMessageDelayed(Constants.START_RECORD, timeInterval);
                        countDown = 0;
                    }
                    break;
                case Constants.WAIT_INTERVAL:
//                    ToastUtils.showShort("1");
                    handler.sendEmptyMessageDelayed(Constants.START_RECORD, timeInterval);
                    break;
                case Constants.START_RECORD:
                    analysisView.clearFreq();
                    startRecord();
                    break;
                case Constants.STOP_RECORD:
                    stopRecord();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_help_item:
                ToastUtils.showShort("help");
                break;
            case R.id.id_open_item:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                AudioTrackManager.getInstance().readAudioFile(getUriPath(MainActivity.this, uri), handler);
            }
        }
    }

    public String getUriPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it  Or Log it.
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
