package com.example.projecttabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class DrumsWithoutCursor extends View {
    private ArrayList<float[]> data = new ArrayList<>();
    private int nominator = 4;
    private int denominator = 4;
    private int tactNumber = 1;
    private int resolution = 480;
    public DrumsWithoutCursor(Context context) {super(context);}

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        float y = (float) (getHeight() / 14);
        float x = (float) (getWidth() * 0.1);

        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(0, (y * (i + 5)), getWidth(), (y * (i + 5)), paint);
        }
        if (!data.isEmpty()) {
            drawSheets(x * 8 / resolution, y / 2, x, canvas);

            paint.setTextSize(50);
            paint.setStrokeWidth(5);
            canvas.drawText(tactNumber + "", 0, y * 2, paint);

            drawSize((float) (x * 1.5), y * 7, y * 9, nominator, denominator, canvas, paint);

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawLine((float) (x * 2), y * 5, (float) (x * 2), y * 9, paint);
        }
        canvas.drawRect(0, 0, getWidth(), 2, paint);
        canvas.drawRect(0, getHeight(), getWidth(), getHeight() - 2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 51, 204, 204));
    }

    public void setData(ArrayList<float[]> data, int nominator, int denominator, int resolution, int tactNumber){
        this.data = data;
        this.nominator = nominator;
        this.denominator = denominator;
        this.resolution = resolution;
        this.tactNumber = tactNumber;
    }

    private void drawSize(float x, float y1, float y2, int nom, int den, Canvas canvas, Paint paint){
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(75);
        paint.setStrokeWidth(3);
        canvas.drawText(nom + "", x, y1, paint);
        canvas.drawText(den + "", x, y2, paint);
    }

    private void drawSheets(float k, float y, float xN, Canvas canvas){
        for (float[] temp : data){
            Log.d("barabans", Arrays.toString(temp));
            float x1 = xN / 8;
            float x = 2 * xN + (k * (resolution % temp[0])) + (xN / 3);
            switch ((int) temp[3]){
                case 35:
                case 36:
                    acousticBassDrum(true, x, y, x1, canvas);
                    break;
                case 37:
                    sideStick(true, x, y, x1, canvas);
                    break;
                case 38:
                    acousticSnare(true, x, y, x1, canvas);
                    break;
                case 40:
                    electricSnare(true, x, y, x1, canvas);
                    break;
                case 41:
                    lowFloorTomTom(true, x, y, x1, canvas);
                    break;
                case 42:
                    closedHiHat(true, x, y, x1, canvas);
                    break;
                case 43:
                    highFloorTomTom(true, x, y, x1, canvas);
                    break;
                case 44:
                    pedalHiHat(true, x, y, x1, canvas);
                    break;
                case 45:
                    lowTomTom(true, x, y, x1, canvas);
                    break;
                case 46:
                    openHiHat(true, x, y, x1, canvas);
                    break;
                case 47:
                    lowFloorTomTom(true, x, y, x1, canvas);
                    break;
                case 48:
                    highMidTomTom(true, x, y, x1, canvas);
                    break;
                case 49:
                    crashCymbal1(true, x, y, x1, canvas);
                    break;
                case 50:
                    highFloorTomTom(true, x, y, x1, canvas);
                    break;
                case 51:
                    rideCymbal1(true, x, y, x1, canvas);
                    break;
                case 54:
                    tambourine(true, x, y, x1, canvas);
                    break;
                case 55:
                    break;
                case 56:
                    cowBell(true, x, y, x1, canvas);
                    break;
                case 77:
                    lowWoodBlock(true, x, y, x1, canvas);
                    break;
                case 76:
                    highWoodBlock(true, x, y, x1, canvas);
                    break;
                case 81:
                    openTriangle(true, x, y, x1, canvas);
                    break;
            }
        }
    }

    private void pedalHiHat(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 18;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        if (line) canvas.drawLine(x - moveX, y + moveY, x + moveX, y - moveY * 6, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void BassDrum1(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 17;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x + moveX, y - moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void acousticBassDrum(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 16;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x + moveX, y - moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void lowFloorTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 15;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x + moveX, y - moveY, x + moveX, y - moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void highFloorTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 14;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x + moveX, y - moveY, x + moveX, y - moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void lowTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 13;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void tambourine(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 13;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x, y + moveY);
        path.lineTo(x - moveX, y + moveY);
        path.lineTo(x + moveX, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void acousticSnare(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 12;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }
    private void electricSnare(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 12;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void lowWoodBlock(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 13;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x, y + moveY);
        path.lineTo(x - moveX, y + moveY);
        path.lineTo(x + moveX, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void sideStick(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 12;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void lowMidTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 11;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void highWoodBlock(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 14;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x, y + moveY);
        path.lineTo(x - moveX, y + moveY);
        path.lineTo(x + moveX, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void highMidTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 13;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void cowBell(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 13;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x, y + moveY);
        path.lineTo(x - moveX, y + moveY);
        path.lineTo(x + moveX, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void highTomTom(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 12;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void rideCymbal1(boolean line, float x, float y, float moveX, Canvas canvas){
        Log.d("dasdassa,das,mdmssammmasm", x + " " + moveX);
        float moveY = y;
        y = y * 12;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void closedHiHat(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 11;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
    }

    private void openHiHat(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 11;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x + moveX, y);
        path.lineTo(x, y - moveY);
        path.lineTo(x - moveX, y);
        path.lineTo(x, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void crashCymbal1(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 10;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawLine(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        canvas.drawLine(x - moveX, y, x + moveX, y, paint);
    }

    private void openTriangle(boolean line, float x, float y, float moveX, Canvas canvas){
        float moveY = y;
        y = y * 10;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (line) canvas.drawLine(x - moveX, y + moveY, x - moveX, y + moveY * 6, paint);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(x, y + moveY);
        path.lineTo(x - moveX, y + moveY);
        path.lineTo(x + moveX, y + moveY);
        path.close();
        canvas.drawPath(path, paint);
        canvas.drawLine(x - moveX, y, x + moveX, y, paint);
    }
}
