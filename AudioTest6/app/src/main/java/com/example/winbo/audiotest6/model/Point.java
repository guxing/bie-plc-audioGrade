package com.example.winbo.audiotest6.model;

/**
 * Created by winbo on 2017/9/3.
 */

public class Point {
    private float x;//横坐标
    private double freq;//频率

    public Point(float x, double freq) {
        this.x = x;
        this.freq = freq;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", freq=" + freq +
                '}';
    }
}
