package com.example.winbo.audiotest6.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.example.winbo.audiotest6.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winbo on 2017/9/2.
 */

public class AnalysisView extends View {
    private final String TAG = "AnalysisView";
    private Paint textPaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint redPaint = new Paint();
    private List<Point> freqList;//频率集合
    private List<Point> baseList;//频率集合
    private float[] mPoints;//每一个频率值对应两个点
    private Context mContext;
    private int mSpectrumNum = 80;//列数
    private int maxFreq = 1100;//界面展示最高频率
    private int itemWidth = 0;//每条线宽度

    public AnalysisView(Context context) {
        super(context);
        init(context);
    }

    public AnalysisView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);

        blackPaint.setStrokeWidth(5f);
        blackPaint.setAntiAlias(true);
        blackPaint.setColor(Color.GRAY);
        greenPaint.setStrokeWidth(10f);
        greenPaint.setAntiAlias(true);
        greenPaint.setColor(Color.GREEN);
        redPaint.setStrokeWidth(10f);
        redPaint.setAntiAlias(true);
        redPaint.setColor(Color.RED);
        itemWidth = ScreenUtils.getScreenWidth() / mSpectrumNum;
        if (null == freqList) {
            freqList = new ArrayList<>();
        }
        if (null == baseList) {
            baseList = new ArrayList<>();
        }
        freqList.clear();
        baseList.clear();
    }

    public void drawFreq(double freq) {
        if (null == freqList) {
            return;
        }
        if (0 == freqList.size()) {
            freqList.add(new Point(0, freq));
        } else {
            Point point = new Point(freqList.get(freqList.size() - 1).getX() + itemWidth, freq);
            freqList.add(point);
        }
        if (baseList.size() > mSpectrumNum) {//画出的线超过**个时候，清空第一个点
            baseList.remove(0);
            for (int i = 0; i < baseList.size(); i++) {
                Point point = new Point(baseList.get(i).getX() - itemWidth, freq);
                baseList.set(i, point);
            }
        }
        invalidate();
    }

    public void drawBase(double freq) {
        if (null == baseList) {
            return;
        }
        if (0 == baseList.size()) {
            baseList.add(new Point(0, freq));
        } else {
            Point point = new Point(baseList.get(baseList.size() - 1).getX() + itemWidth, freq);
            baseList.add(point);
        }
        if (baseList.size() > mSpectrumNum) {//画出的线超过**个时候，清空第一个点
            baseList.remove(0);
            for (int i = 0; i < baseList.size(); i++) {
                Point point = new Point(baseList.get(i).getX() - itemWidth, freq);
                baseList.set(i, point);
            }
        }
        invalidate();
    }

    public void clearFreq() {
        if (null != freqList) {
            freqList.clear();
        }
        invalidate();
    }

    public void clearBase() {
        if (null != baseList) {
            baseList.clear();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i("winbo", "onDraw getHeight():" + getHeight());
        //先画标准线
        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), blackPaint);
        canvas.drawText("1100HZ", 10, 20, textPaint);
        canvas.drawLine(0, 0, getWidth(), 0, blackPaint);
        canvas.drawText("0HZ", 10, getHeight() - 20, textPaint);

        int textWidth = 30;
        canvas.drawLine(0, getCoordinateY(261.63), getWidth() - textWidth, getCoordinateY(261.63), blackPaint);//261.63HZ
        canvas.drawText("C4", getWidth() - textWidth, getCoordinateY(261.63), textPaint);
        canvas.drawLine(0, getCoordinateY(293.66), getWidth() - textWidth, getCoordinateY(293.66), blackPaint);//293.66HZ
        canvas.drawText("D4", getWidth() - textWidth, getCoordinateY(293.66), textPaint);
        canvas.drawLine(0, getCoordinateY(329.63), getWidth() - textWidth, getCoordinateY(329.63), blackPaint);//329.63HZ
        canvas.drawText("E4", getWidth() - textWidth, getCoordinateY(329.63), textPaint);
        canvas.drawLine(0, getCoordinateY(349.23), getWidth() - textWidth, getCoordinateY(349.23), blackPaint);//349.23HZ
        canvas.drawText("F4", getWidth() - textWidth, getCoordinateY(349.23), textPaint);
        canvas.drawLine(0, getCoordinateY(392.00), getWidth() - textWidth, getCoordinateY(392.00), blackPaint);//392.00HZ
        canvas.drawText("G4", getWidth() - textWidth, getCoordinateY(392.00), textPaint);
        canvas.drawLine(0, getCoordinateY(440.00), getWidth() - textWidth, getCoordinateY(440.00), blackPaint);//440.00HZ
        canvas.drawText("A4", getWidth() - textWidth, getCoordinateY(440.00), textPaint);
        canvas.drawLine(0, getCoordinateY(493.88), getWidth() - textWidth, getCoordinateY(493.88), blackPaint);//493.88HZ
        canvas.drawText("B4", getWidth() - textWidth, getCoordinateY(493.88), textPaint);

        if (null != baseList) {
            for (int i = 0; i < baseList.size(); i++) {
                Point point = baseList.get(i);
                int y = getCoordinateY(point.getFreq());
                if (0 != y) {
                    canvas.drawLine(point.getX(), y, point.getX() + itemWidth, y, redPaint);
                }
            }
        }
        if (null != freqList) {
            for (int i = 0; i < freqList.size(); i++) {
                Point point = freqList.get(i);
                int y = getCoordinateY(point.getFreq());
                if (0 != y) {
                    canvas.drawLine(point.getX(), y, point.getX() + itemWidth, y, greenPaint);
                }
            }
        }
    }

    //根据频率值获取view对应坐标
    private int getCoordinateY(Double freq) {
        int y = 0;
        if (freq <= maxFreq) {
            y = (int) (freq * getHeight() / maxFreq);
        }
        return getHeight() - y;
    }

    //根据频率获取对应音高
    private void getPitch(Double freq) {

    }
}
