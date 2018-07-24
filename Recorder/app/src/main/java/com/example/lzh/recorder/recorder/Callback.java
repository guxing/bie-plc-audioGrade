package com.example.lzh.recorder.recorder;

public interface Callback {
    void onBufferAvailable(byte[] buffer);

    void onFinish();
}