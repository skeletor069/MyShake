package com.tjik.myshakedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class CustomGraphView extends View{

    ArrayList<AccelerometerViewData> viewData = new ArrayList<AccelerometerViewData>();

    Paint redPaint = new Paint();
    Paint greenPaint = new Paint();
    Paint bluePaint = new Paint();
    Paint whitePaint = new Paint();
    Paint blackPaint = new Paint();

    public CustomGraphView(Context context) {
        super(context);
        init(null);
    }

    public CustomGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
            // Load attributes

        viewData.add(new AccelerometerViewData(10f,18f,.3f));
        viewData.add(new AccelerometerViewData(.0f,20f,.3f));
        viewData.add(new AccelerometerViewData(7f,-20f,22f));
        viewData.add(new AccelerometerViewData(.9f,2f,.3f));

        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(5);
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStrokeWidth(5);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(5);
        blackPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.rgb(180,180,180));

        float multiplier = 2;
        float redY = getHeight() / 8f;
        float greenY = 3 * getHeight() / 8f;
        float blueY = 5 * getHeight() / 8f;
        float whiteY = 7 * getHeight() / 8f;

        canvas.drawLine(0, redY, getWidth(), redY, blackPaint);
        canvas.drawLine(0, greenY, getWidth(), greenY, blackPaint);
        canvas.drawLine(0, blueY, getWidth(), blueY, blackPaint);
        canvas.drawLine(0, whiteY, getWidth(), whiteY, blackPaint);

        if(viewData.size() > 1) {
            float stepSizeX = (float) getWidth() / (float) (viewData.size() - 1);
            if(viewData.size() < 10)
                stepSizeX = getWidth()/10f;

            for (int i = viewData.size() - 1; i > 0 ; i--) {

                float startX = (viewData.size() - 1 - i) * stepSizeX;
                float endX = startX + stepSizeX;
                float startYRed = redY - multiplier * viewData.get(i).x;
                float endYRed =  redY - multiplier * viewData.get(i - 1).x;
                float startYGreen = greenY - multiplier * viewData.get(i).y;
                float endYGreen =  greenY - multiplier * viewData.get(i - 1).y;
                float startYBlue = blueY - multiplier * viewData.get(i).z;
                float endYBlue =  blueY - multiplier * viewData.get(i - 1).z;
                float startYWhite = whiteY - multiplier * viewData.get(i).magnitude;
                float endYWhite =  whiteY - multiplier * viewData.get(i - 1).magnitude;

                canvas.drawLine(startX,startYRed, endX, endYRed, redPaint);
                canvas.drawLine(startX,startYGreen, endX, endYGreen, greenPaint);
                canvas.drawLine(startX,startYBlue, endX, endYBlue, bluePaint);
                canvas.drawLine(startX,startYWhite, endX, endYWhite, whitePaint);

            }
        }


    }

    public void SetAccelerometerData(ArrayList<AccelerometerViewData> viewData){
        this.viewData = viewData;
        invalidate();
    }


}
