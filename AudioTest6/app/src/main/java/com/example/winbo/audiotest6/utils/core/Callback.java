package com.example.winbo.audiotest6.utils.core;

public interface Callback {
    void onBufferAvailable(byte[] buffer);

    void onFinish();
}