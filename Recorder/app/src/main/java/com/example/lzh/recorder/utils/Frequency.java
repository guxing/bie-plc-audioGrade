package com.example.lzh.recorder.utils;

import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.StringUtils;
import com.example.lzh.recorder.Constants;
import com.example.lzh.recorder.common.Complex;
import com.example.lzh.recorder.common.FFT;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by winbo on 2017/9/2.
 */

public class Frequency {
    public static double getFreq(byte[] signal) {
        final int mNumberOfFFTPoints = 512;
        double mMaxFFTSample;
        double mPeakPos;

        double temp;
        Complex[] y;
        Complex[] fftArray = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints / 2];

        for (int i = 0; i < mNumberOfFFTPoints; i++) {
            if (2 * i + 1 <= signal.length) {
                temp = (double) ((signal[2 * i] & 0xFF) | (signal[2 * i + 1] << 8)) / 32768.0F;
                fftArray[i] = new Complex(temp, 0.0);
            }
        }

        y = FFT.fft(fftArray); // --> Here I use FFT class

        mMaxFFTSample = 0.0;
        mPeakPos = 0;
        for (int i = 0; i < (mNumberOfFFTPoints / 2); i++) {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if (absSignal[i] > mMaxFFTSample) {
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }
        Log.i("winbo", "Frequency mMaxFFTSample:" + mMaxFFTSample);
        Log.i("winbo", "Frequency mPeakPos:" + mPeakPos);
        //这里的22050是采样率
        double freq = mPeakPos * Constants.sampleRate / 512;
        Log.i("winbo", "Frequency LINZHENGHANG:" + freq);

        return freq;
    }

    public static void analysisAudioFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);

        byte[] byteData = new byte[(int) file.length()];
        try {
            FileInputStream in = new FileInputStream(file);
//            in.skip(0x2c);
            in.read(byteData);
            in.close();

//            double[] transformed = new double[(int) file.length()];
            byte[] byteData1 = new byte[(int) file.length() - 44];
            for (int j = 44; j < file.length(); j++) {
//                transformed[j] = (double) byteData[j];
                byteData1[j - 44] = byteData[j];
            }
            double absNormalizedSignal = getFreq(byteData1);
//            double freq1 = getFundamentalFrequency(absNormalizedSignal);
//            Log.i("winbo", "absNormalizedSignal:" + freq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /********************两个基础方法*****************************/
    public double[] calculateFFT(byte[] signal) {
        final int mNumberOfFFTPoints = 16384;
        double mMaxFFTSample;
        double mPeakPos;

        double temp;
        Complex[] y;
        Complex[] fftArray = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints / 2];

        for (int i = 0; i < mNumberOfFFTPoints; i++) {
            temp = (double) ((signal[2 * i] & 0xFF) | (signal[2 * i + 1] << 8)) / 32768.0F;
            fftArray[i] = new Complex(temp, 0.0);
        }

        y = FFT.fft(fftArray); // --> Here I use FFT class

        mMaxFFTSample = 0.0;
        mPeakPos = 0;
        for (int i = 0; i < (mNumberOfFFTPoints / 2); i++) {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if (absSignal[i] > mMaxFFTSample) {
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }

        double freq = mPeakPos * 16000 / 8196;
        if (freq > 500) {//转换为有效频率值
            freq = freq / 2;
        }
        Log.i("winbo", "freq:" + freq);

        return absSignal;
    }

    private void readAudioFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/360/c4.wav");
        byte[] byteData = new byte[(int) file.length()];
        try {
            FileInputStream in = new FileInputStream(file);
//            in.skip(0x2c);
            in.read(byteData);
            in.close();

//            double[] transformed = new double[(int) file.length()];
            byte[] byteData1 = new byte[(int) file.length() - 44];
            for (int j = 44; j < file.length(); j++) {
//                transformed[j] = (double) byteData[j];
                byteData1[j - 44] = byteData[j];
            }

//            AudioCalculator audioCalculator = new AudioCalculator();
//            audioCalculator.setBytes(byteData);
//            double freq = audioCalculator.getFrequency();
//            Log.i("winbo", "absNormalizedSignal:" + freq);

            double[] absNormalizedSignal = calculateFFT(byteData1);
//            double freq1 = getFundamentalFrequency(absNormalizedSignal);
//            Log.i("winbo", "absNormalizedSignal:" + freq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
