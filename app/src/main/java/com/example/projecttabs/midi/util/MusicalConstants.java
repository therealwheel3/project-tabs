package com.example.projecttabs.midi.util;

public class MusicalConstants {
    private static final int verticalIndent0 = 48;
    private static final int verticalIndent1 = 24;

    public static int getIndent(int val){
        switch (val){
            case 0:
                return verticalIndent0;
            case 1:
                return verticalIndent1;
            default:
                return -1;
        }
    }
}
