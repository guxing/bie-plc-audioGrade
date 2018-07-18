package com.example.winbo.audiotest6.utils;

/**
 * Created by winbo on 2017/9/8.
 */

public class FitchUtils {
    public static String getPitch(double freq) {
        String result = String.valueOf(freq);
        int c4 = (int) (freq - 261.63);
        if (-5 < c4 && c4 < 5) {
            return "C4";
        } else if (-0 < c4 && c4 < 10) {
            return "C4" + c4;
        }
        int d4 = (int) (freq - 293.66);
        if (-5 < d4 && d4 < 5) {
            return "D4";
        } else if (-0 < d4 && d4 < 10) {
            return "D4" + d4;
        }
        int e4 = (int) (freq - 329.63);
        if (-5 < e4 && e4 < 5) {
            return "E4";
        } else if (-0 < e4 && e4 < 10) {
            return "E4" + e4;
        }
        int f4 = (int) (freq - 349.23);
        if (-5 < f4 && f4 < 5) {
            return "F4";
        } else if (-0 < f4 && f4 < 10) {
            return "F4" + f4;
        }
        int g4 = (int) (freq - 392.00);
        if (-5 < g4 && g4 < 5) {
            return "G4";
        } else if (-0 < g4 && g4 < 10) {
            return "G4" + g4;
        }
        int a4 = (int) (freq - 440.00);
        if (-5 < a4 && a4 < 5) {
            return "A4";
        } else if (-0 < a4 && a4 < 10) {
            return "A4" + d4;
        }
        int b4 = (int) (freq - 493.88);
        if (-5 < b4 && b4 < 5) {
            return "B4";
        } else if (-0 < b4 && b4 < 10) {
            return "B4" + b4;
        }
        return result;
    }
}
