package com.example.projecttabs;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class Cursor {
    private double xv, yv, moveX, moveY;
    private int noteIndex;

    public Cursor(Context context, double x, double y){
        this.xv = x;
        this.yv = y;
    }

    public void setXValue(double x) {
        this.xv = x;
    }

    public void setYValue(double y) {this.yv = y;}

    public void setNoteIndex(int beat){this.noteIndex = beat;}

    public void setMoveX(double moveX) {
        this.moveX = moveX;
    }

    public double getMoveX() {
        return moveX;
    }

    public void setMoveY(double moveY) {
        this.moveY = moveY;
    }

    public double getMoveY() {
        return moveY;
    }

    public double getXValue() {
        return xv;
    }

    public double getYValue() {
        return yv;
    }
}
