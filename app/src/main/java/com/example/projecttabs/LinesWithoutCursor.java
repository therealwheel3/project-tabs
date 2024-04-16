package com.example.projecttabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TimerTask;

public class LinesWithoutCursor extends View {
    private ArrayList<float[]> data = new ArrayList<>();
    private int nominator = 4;
    private int denominator = 4;
    private int key = 0;
    private int verticalIndent = 0;
    private int numberOfBar = 0;
    private int resolution = 480;
    private int tactNumber = 1;
    private HashSet<Integer> prevLigas = new HashSet<>();
    private HashSet<Integer> nextLigas = new HashSet<>();
    public LinesWithoutCursor(Context context) {super(context);}


    @Override
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
        canvas.drawRect(0, 0, getWidth(), 2, paint);
        canvas.drawRect(0, getHeight(), getWidth(), getHeight() - 2, paint);

        if (!data.isEmpty()) {
            drawSheets(data, resolution, x, y / 2, x * 8 / resolution, numberOfBar,
                    canvas, paint);

            paint.setTextSize(50);
            paint.setStrokeWidth(5);
            canvas.drawText(tactNumber + "", 0, y * 2, paint);

            drawKey(0, y * 8, key, canvas, paint);
            drawSize((float) (x * 1.5), y * 7, y * 9, nominator, denominator, canvas, paint);


            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawLine((float) (x * 2), y * 5, (float) (x * 2), y * 9, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 51, 204, 204));


    }

    public void setData(ArrayList<float[]> data, int key, int nominator, int denominator, int verticalIndent,
                        int numberOfBar, int resolution, int tactNumber){
        this.data = data;
        this.key = key;
        this.nominator = nominator;
        this.denominator = denominator;
        this.verticalIndent = verticalIndent;
        this.numberOfBar = numberOfBar;
        this.resolution = resolution;
        this.tactNumber = tactNumber;
    }
    private void drawSize(float x, float y1, float y2, int nom, int den, Canvas canvas, Paint paint){
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(nom + "", x, y1, paint);
        canvas.drawText(den + "", x, y2, paint);
    }

    private void drawKey(float x, float y, int key, Canvas canvas, Paint paint){
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(75);
        switch (key){
            case 0:
                canvas.drawText("\uD834\uDD1E", x, y, paint);
                break;
            case 1:
                canvas.drawText("\uD834\uDD22", x, y, paint);
                break;
            default:
                canvas.drawText("?", x, y, paint);
        }
    }

    private void drawNote(boolean line, float x, float y, float moveX, float moveY, float n, Canvas canvas, Paint paint, boolean flag){
        if (flag){moveY *= -1; moveX *= -1;}
        paint.setStrokeWidth(5);
        if (n > 1 && line){canvas.drawLine(x + moveX, y, x + moveX, y - moveY * 10, paint); Log.d("draw add line", "true");} // draw stick
        if (n <= 2){
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        }
        else {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
            if (line) {
                for (int i = 0; i < (Math.log(n) / Math.log(2)) - 2; i++) {
                    canvas.drawLine(x + moveX, y - moveY * (10 - i), x + moveX * 2, y - moveY * ((5 - i) - 1), paint);
                }
            }
        }
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(7);
        for (float i = 2; i != 32; i *= 2){
            if ((float)(i / 3) == n){canvas.drawOval((float) (x + moveX * 1.5 - 2), y - 2,
                    (float) (x + moveX * 1.5 + 2), y + 2, paint); break;}
        }
    }

    private void drawNote(boolean line, float x, float y, float moveX, float moveY, float n, Canvas canvas, Paint paint, boolean flag, boolean alt){
        if (flag){moveY *= -1; moveX *= -1;}
        paint.setStrokeWidth(5);
        if (n > 1 && line){canvas.drawLine(x + moveX, y, x + moveX, y - moveY * 10, paint); Log.d("draw add line", "true");} // draw stick
        if (n <= 2){
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
        }
        else {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawOval(x - moveX, y - moveY, x + moveX, y + moveY, paint);
            if (line) {
                    for (int i = 0; i < (Math.log(n) / Math.log(2)) - 2; i++) {
                            canvas.drawLine(x + moveX, y - moveY * (10 - i), x + moveX * 2, y - moveY * ((5 - i) - 1), paint);
                        }
                }
        }
        paint.setStrokeWidth(1);
        if (alt){
            paint.setTextSize(37);
            canvas.drawText("#", (float) (x - moveX * 2.5), (float) (y + moveY), paint);
        }
        else {
            paint.setTextSize(50);
            canvas.drawText("♮", (float) (x - moveX * 2.5), (float) (y + moveY), paint);
        }
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(7);
        for (float i = 2; i != 32; i *= 2){
            if ((float)(i / 3) == n){canvas.drawOval((float) (x + moveX * 1.5 - 2), y - 2,
                    (float) (x + moveX * 1.5 + 2), y + 2, paint); break;}
        }
    }

    private void drawPause(float x, float y, float moveX, int n, Canvas canvas, Paint paint) {
        //Paint whitePaint = new Paint();
        //whitePaint.setStrokeWidth(1);
        //whitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //whitePaint.setColor(Color.WHITE);
        paint.setTextSize(75);
        Log.d(x + "", n + "");
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (n < 4) {
            paint.setStrokeWidth(4);
        }
        else {
            paint.setStrokeWidth(1);
        }

        if (n <= 1) canvas.drawRect(x, y * 14, x + moveX, y * 14 + y / 2, paint);
        else if (n <= 2) canvas.drawRect(x, (float) (y * 14.5), x + moveX, (float) (y * 14.5 + y / 2), paint);
        else if (n <= 4) canvas.drawText("\uD834\uDD3D", x, y * 19, paint);
        else if (n <= 8) canvas.drawText("\uD834\uDD3E", x, y * 19, paint);
        else if (n <= 16) canvas.drawText("\uD834\uDD3F", x, y * 19, paint);
        else if (n <= 32) canvas.drawText("\uD834\uDD40", x, y * 19, paint);
        else if (n <= 64) canvas.drawText("\uD834\uDD41", x, y * 19, paint);
        else if (n <= 128) canvas.drawText("\uD834\uDD42", x, y * 19, paint);
        }

    private void drawSheets(ArrayList<float[]> notes, int resolution, float x, float y, float x1, int k,
                            Canvas canvas, Paint paint){
        int n = resolution * k;
        float group = 0.0f;
        ArrayList<float[]> lines = new ArrayList<>();
        float move;

        ArrayList<Float> triols = new ArrayList<>();
        ArrayList<Float> quintols = new ArrayList<>();
        ArrayList<float[]> ligas = new ArrayList<>();

        // draw notes
        for (float[] note : notes){
            float size = note[1];
            if (note.length >= 6){
                if (note.length == 7){ligas.add(note);}
                move = getMove(note[3]);
                if (note[1] >= 8 && note[5] == group){
                    lines.add(new float[]{2 * x + (x1 * (note[0] - n)) + (x / 3) + (x / 6),
                            note[1], note[3], (y * (27 - move) - y * 5), y * (27 - move),
                            (float) (Math.log(note[1]) / Math.log(2)) - 2});
                }
                else if(note[1] < 8){
                    if (note[4] == 0) {
                        drawNote(true, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                                x / 6, y / 2, size, canvas, paint, false);
                    }
                    else if (note[4] == 1){
                        drawNote(true, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                                x / 6, y / 2, size, canvas, paint, false, true);
                    }
                    else if (note[4] == -1){
                        drawNote(true, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                                x / 6, y / 2, size, canvas, paint, false, false);
                    }
                }
                else {
                    if (!lines.isEmpty()){
                        drawLines(lines, y * 2, paint, canvas);
                        lines.clear();
                    }
                    group = note[5];
                    lines.add(new float[]{2 * x + (x1 * (note[0] - n)) + (x / 3) + (x / 6),
                            note[1], note[3], (y * (27 - move) - y * 5), y * (27 - move),
                            (float) (Math.log(note[1]) / Math.log(2)) - 2});
                }

                if (note[4] == 0) {
                    drawNote(false, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                            x / 6, y / 2, size, canvas, paint, false);
                }
                else if (note[4] == 1){
                    drawNote(false, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                            x / 6, y / 2, size, canvas, paint, false, true);
                }
                else if (note[4] == -1){
                    drawNote(false, 2 * x + (x1 * (note[0] - n)) + (x / 3), y * (27 - move),
                            x / 6, y / 2, size, canvas, paint, false, false);
                }

                if (y * 20 <= y * (27 - move)){ // drawing additional lines from below
                    paint.setStrokeWidth(1);
                    for (float i = y * 20; i <= y * (27 - move); i += y * 2){
                        canvas.drawLine(2 * x + (x1 * (note[0] - n)) + (x / 3) - (x / 3), i,
                                2 * x + (x1 * (note[0] - n)) + (x / 3) + (x / 3), i, paint);
                    }
                }
                if (y * 12 >= y * (27 - move)){ // drawing additional lines from above
                    paint.setStrokeWidth(1);
                    for (float i = y * 12; i >= y * (27 - move); i -= y * 2){
                        canvas.drawLine(2 * x + (x1 * (note[0] - n)) + (x / 3) - (x / 3), i,
                                2 * x + (x1 * (note[0] - n)) + (x / 3) + (x / 3), i, paint);
                    }
                }
                // miltiols actions
                if (note[1] % 3 == 0){
                    triols.add(note[0]);
                    triols.add(note[3]);
                    if (triols.size() == 6){
                        float maxx = triols.get(1);
                        if (triols.get(3) > maxx) maxx = triols.get(3);
                        if (triols.get(5) > maxx) maxx = triols.get(5);
                        paint.setTextSize(25);
                        paint.setStrokeWidth(3);
                        canvas.drawLine(2 * x + (x1 * (triols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (triols.get(4) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), paint);
                        canvas.drawLine(2 * x + (x1 * (triols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (triols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 3)), paint);
                        canvas.drawLine(2 * x + (x1 * (triols.get(4) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (triols.get(4) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 3)), paint);
                        canvas.drawText("3",
                                2 * x + (x1 * (triols.get(2) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 10)), paint);
                        triols.clear();
                    }
                }

                if (note[1] % 5 == 0){
                    quintols.add(note[0]);
                    quintols.add(note[3]);
                    if (quintols.size() == 10){
                        float maxx = quintols.get(1);
                        if (quintols.get(3) > maxx) maxx = quintols.get(3);
                        if (quintols.get(5) > maxx) maxx = quintols.get(5);
                        if (quintols.get(7) > maxx) maxx = quintols.get(7);
                        if (quintols.get(9) > maxx) maxx = quintols.get(9);
                        paint.setTextSize(25);
                        paint.setStrokeWidth(3);
                        canvas.drawLine(2 * x + (x1 * (quintols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (quintols.get(8) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), paint);
                        canvas.drawLine(2 * x + (x1 * (quintols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (quintols.get(0) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 3)), paint);
                        canvas.drawLine(2 * x + (x1 * (quintols.get(8) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 6)), 2 * x + (x1 * (quintols.get(8) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 3)), paint);
                        canvas.drawText("5",
                                2 * x + (x1 * (quintols.get(4) - n)) + (x / 3),
                                y * (27 - getMove(maxx + 8)), paint);
                        quintols.clear();
                    }
                }
            }
            else {
                drawPause(2 * x + (x1 * (note[0] - n)) + (x / 3), y, x / 3, (int) size, canvas, paint);
            }
            move = 0;
        }
        if (!ligas.isEmpty()) drawLigas(ligas, x / 6, y / 2, x, y, x1, n, canvas);
        if (!lines.isEmpty()) drawLines(lines, y * 2, paint, canvas);
        lines.clear();
    }
    private void drawLines(ArrayList<float[]> lines, float y, Paint paint, Canvas canvas){
        // new float[]{indent + x + (x1 * (note[0] - n)) + (x / 3) + (x / 6),
        // note[1], note[3], (y * (26 - move) - y * 5), y * (26 - move),
        // (float) (Math.log(note[1]) / Math.log(2)) - 3}
        paint.setStrokeWidth(5);
        float x0 = lines.get(0)[0]; // first and last note x
        float x1 = lines.get(lines.size() - 1)[0];
        float maxY = 999999;
        float first = 999999;
        float last = 999999;
        float minn = 999999;
        float tick = -1;
        int indentT = -1;
        float maxx = 0;
        for (float[] temp : lines){
            if (temp[4] > maxx && temp[0] == lines.get(0)[0]){
                maxx = temp[4];
            }
            if (temp[3] < first && temp[0] == lines.get(0)[0]){
                first = temp[3];
            }
            if (temp[3] < last && temp[0] == lines.get(lines.size() - 1)[0]){
                last = temp[3];
            }
            if (temp[3] < maxY){
                maxY = temp[3];
            }
            if (tick != temp[0]){indentT++;}
            tick = temp[0];
        }

        if (indentT == 0){
            for (byte i = 0; i < lines.get(0)[5]; i++){
                canvas.drawLine(x0, maxY, x0 + y, maxY + y, paint);
            }
        }

        float prev = lines.get(0)[0];
        if (maxY < first && maxY < last) {
            int k = 0;
            float indent = Math.abs(((Math.abs(maxY - y)) - (maxY + y)) / indentT);
            for (byte i = 0; i < lines.get(0)[5]; i++){
                canvas.drawLine(x0, maxY - y + (y / 2 * i), x1, maxY + y + (y / 2 * i), paint);
            }
            if (first > last){
                for (float[] temp : lines) {
                    if (temp[0] != prev){k++;}
                    canvas.drawLine(temp[0], maxY - y + indent * k, temp[0], temp[4], paint);
                    prev = temp[0];
                }
            }
            else if (first < last){
                for (float[] temp : lines) {
                    if (temp[0] != prev){k++;}
                    canvas.drawLine(temp[0], maxY - y + indent * k, temp[0], temp[4], paint);
                    prev = temp[0];
                }
            }
            else {
                for (float[] temp : lines) {
                    if (temp[0] != prev){k++;}
                    canvas.drawLine(temp[0], maxY - y + indent * k, temp[0], temp[4], paint);
                    prev = temp[0];
                }
            }
        }
        else if(first > last){
            int k = 0;
            float indent = y / indentT;
            for (byte i = 0; i < lines.get(0)[5]; i++){
                canvas.drawLine(x0, last + y / 2 * i, x1, last - y + y / 2 * i, paint);
            }
            for (float[] temp : lines) {
                if (temp[0] != prev){k++;}
                canvas.drawLine(temp[0], last - indent * k, temp[0], temp[4], paint);
                prev = temp[0];
            }
        }
        else if(first < last){
            int k = 0;
            float indent = y / indentT;
            for (byte i = 0; i < lines.get(0)[5]; i++){
                canvas.drawLine(x0, first + y / 2 * i, x1, first + y + y / 2 * i, paint);
            }
            for (float[] temp : lines) {
                if (temp[0] != prev){k++;}
                canvas.drawLine(temp[0], first + indent * k, temp[0], temp[4], paint);
                prev = temp[0];
            }
        }
        else {
            for (byte i = 0; i < lines.get(0)[5]; i++){
                canvas.drawLine(x0, first + y / 2 * i, x1, first + y / 2 * i, paint);
            }
            for (float[] temp : lines) {
                canvas.drawLine(temp[0], first, temp[0], temp[4], paint);
            }
        }
    }

    private int getMove(float note){
        float move = 0;
        for (int i = verticalIndent; i < note; i++){
            if (i % 12 == 4 || i % 12 == 11){
                move += 1;
            }
            else {move += 0.5;}
        }
        return (int)(move - key * 1.5);

    }

    private void drawLigas(ArrayList<float[]> ligas, float resolution, float moveY, float x, float y, float k, float n, Canvas canvas){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        float move = getMove(ligas.get(0)[3]);;
        float top = (y * (27 - move) - moveY * 4);
        float bottom = (y * (27 - move) + moveY * 4);
        for (int i = 0; i < ligas.size(); i++){
            move = getMove(ligas.get(i)[3]);
            top = (y * (27 - move) - moveY * 4);
            bottom = (y * (27 - move) + moveY * 4);
            if (prevLigas.contains((int) ligas.get(i)[6])){ // for prev ligas
                canvas.drawArc(2 * x, top,
                        (float) (2 * x + (k * (ligas.get(i)[0] - n))) + x / 6, bottom,
                        270, 90, false, paint);
                prevLigas.remove((int) ligas.get(i)[6]);
            }
            for (int j = i + 1; j < ligas.size(); j++){ // for current ligas
                if (ligas.get(i)[6] == ligas.get(j)[6]){
                    canvas.drawArc((float) (2 * x + (k * (ligas.get(i)[0] - n))) + x / 6, top,
                            (float) (2 * x + (k * (ligas.get(j)[0] - n))) + x / 6, bottom,
                            180, 180,  false, paint);
                }
            }
        }
        Collections.reverse(ligas);
        for (float[] temp : ligas){ // for next ligas
            if (nextLigas.contains((int) temp[6])){
                move = getMove(temp[3]);;
                top = (y * (27 - move) - moveY * 4);
                bottom = (y * (27 - move) + moveY * 4);
                canvas.drawArc((float)  (2 * x + (k * (temp[0] - n))) + x / 6, top,
                        x * 10 + (float) (2 * x + (k * (temp[0] - n))) + x / 6, bottom,
                        180, 180,  false, paint);
                nextLigas.remove((int) temp[6]);
            }
        }
    }

    public void setPrevLigas(HashSet<Integer> prevLigas) {
        this.prevLigas = prevLigas;
    }

    public void setNextLigas(HashSet<Integer> nextLigas) {
        this.nextLigas = nextLigas;
    }
}
