package com.tjik.myshakedemo.custom_views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

public class FFTView extends View{

    Paint redPaint = new Paint();
    double[] fftData;
    boolean isDataAvailable = false;

    public FFTView(Context context) {
        super(context);
        init(null);
    }

    public FFTView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FFTView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FFTView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        // Load attributes

//        viewData.add(new AccelerometerViewData(10f,18f,.3f));
//        viewData.add(new AccelerometerViewData(.0f,20f,.3f));
//        viewData.add(new AccelerometerViewData(7f,-20f,22f));
//        viewData.add(new AccelerometerViewData(.9f,2f,.3f));
//
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
//        greenPaint.setColor(Color.GREEN);
//        greenPaint.setStrokeWidth(5);
//        bluePaint.setColor(Color.BLUE);
//        bluePaint.setStrokeWidth(5);
//        whitePaint.setColor(Color.WHITE);
//        whitePaint.setStrokeWidth(5);
//        blackPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.rgb(180,180,180));

        if(isDataAvailable){
            float multiplier = .7f;
            float redY = getHeight() / 2f;

            float stepSizeX = (float) getWidth() / (float) (fftData.length - 1);

            for (int i = fftData.length - 1; i > 0 ; i--) {
                float startX = (fftData.length - 1 - i) * stepSizeX;
                float endX = startX + stepSizeX;
                float startYRed = redY - multiplier * (float) fftData[i];
                float endYRed =  redY - multiplier * (float) fftData[i-1];

                canvas.drawLine(startX,startYRed, endX, endYRed, redPaint);
            }
        }
    }

    public void SetFFTData(double[] fftData){
        this.fftData = fftData;
        isDataAvailable = true;
        invalidate();
    }
}
