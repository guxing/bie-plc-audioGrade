package com.example.winbo.audiotest6;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by winbo on 2017/9/2.
 */

public class AudioApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
