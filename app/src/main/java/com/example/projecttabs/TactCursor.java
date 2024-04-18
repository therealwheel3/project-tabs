package com.example.projecttabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class TactCursor extends View {
    boolean currentTact = true;
    float xCoord;
    float xMove;
    int ms = 0;
    int maxMs;
    int bpm;

    public TactCursor(Context context) {super(context);}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (xCoord == 0) {
            xCoord = (float) (2 * (getWidth() * 0.1) + ((getWidth() * 0.1) / 3));
        }
        if (bpm != 0)  xCoord += (float) (getWidth() * 0.8 * 14.5 / 1655);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPaint(paint);
        paint.setColor(Color.GREEN);
        canvas.drawLine(xCoord, 0, xCoord, getHeight(), paint);
        canvas.drawLine(xCoord, currentTact ? 0 : getHeight(),
                xCoord + 30, currentTact ? (float) (getHeight() * 0.25) : (float) (getHeight() * 0.75), paint);
        canvas.drawLine(xCoord, currentTact ? 0 : getHeight(),
                xCoord - 30, currentTact ? (float) (getHeight() * 0.25) : (float) (getHeight() * 0.75), paint);
        ms++;
        if (ms == maxMs){ms = 0; xCoord = 0;}
    }

    public void setData(int bpm, int maxMs){
        this.bpm = bpm;
        this.maxMs = maxMs;
    }

    public void reload(boolean isChange){
        currentTact = isChange;
        ms = 0;
        xCoord = 0;
    }

    public void reload(){
        ms = 0;
        xCoord = 0;
    }
}
