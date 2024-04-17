package com.example.projecttabs;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projecttabs.midi.MidiFile;
import com.example.projecttabs.midi.MidiTrack;
import com.example.projecttabs.midi.event.MidiEvent;
import com.example.projecttabs.midi.util.MusicalConstants;
import com.example.projecttabs.midi.util.Packer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private File file = new File("/storage/emulated/0/download/Ball3.mid");
    MidiFile midiFile;
    private int trackNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Cursor cursor = new Cursor(this, 0, 0);

        if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { "android.permission.READ_EXTERNAL_STORAGE" }, 1);
        }



        setContentView(R.layout.activity_main);
        try {
            midiFile = new MidiFile(file);
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }


        LinearLayout line1 = findViewById(R.id.firstLine);
        LinearLayout line2 = findViewById(R.id.secondLine);
        TextView trackNumberField = findViewById(R.id.TrackNumber);


        LinesWithCursor linesWithCursor = new LinesWithCursor(this);
        linesWithCursor.setCursorX(cursor);
        LinesWithCursor tempLine1 = linesWithCursor;

        LinesWithCursor linesWithCursor1 = new LinesWithCursor(this);
        linesWithCursor1.setCursorX(cursor);
        LinesWithCursor tempLine2 = linesWithCursor1;

        int resolution = midiFile.getResolution();
        List<MidiTrack> tracks = midiFile.getTracks();
        ArrayList<MidiEvent> events = new ArrayList<>(tracks.get(0).getEvents());
        ArrayList<float[]> fmap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
        ArrayList<ArrayList<float[]>> map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);
        ArrayList<float[]> data = new ArrayList<>();
        tempLine1.setData(map.get(0), 0, 4, 4,
                MusicalConstants.getIndent(0), 0, midiFile.getResolution() * 4, 1);
        tempLine2.setData(data, 1, 4, 4,
                MusicalConstants.getIndent(1), 1, midiFile.getResolution() * 4, 2);
        line1.addView(tempLine1);
        line2.addView(tempLine2);


        ImageButton arrowDown = (android.widget.ImageButton) findViewById(R.id.downArrow);
        arrowDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.getYValue() < cursor.getMoveY() * 25 || cursor.getYValue() == 0){
                    cursor.setYValue((float) (cursor.getYValue() + cursor.getMoveY()));
                    tempLine1.invalidate();}
            }
        });

        ImageButton arrowUp = (android.widget.ImageButton) findViewById(R.id.upArrow);
        arrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.getYValue() > 0){
                cursor.setYValue((float) (cursor.getYValue() - cursor.getMoveY()));
                tempLine1.invalidate();}
            }
        });

        ImageButton arrowRight = (android.widget.ImageButton) findViewById(R.id.rightArrow);
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("asdasd", cursor.getXValue() + " " + cursor.getMoveX() * 7.5 + " " + (cursor.getXValue() < cursor.getMoveX() * 7.5));
                if (cursor.getXValue() < cursor.getMoveX() * 7.5){
                    cursor.setXValue((float) (cursor.getXValue() + cursor.getMoveX()));
                    tempLine1.invalidate();}
            }
        });

        ImageButton arrowLeft = (android.widget.ImageButton) findViewById(R.id.leftArrow);
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.getXValue() > cursor.getMoveX()){
                    cursor.setXValue((float) (cursor.getXValue() - cursor.getMoveX()));
                    tempLine1.invalidate();
                }
            }
        });

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Play.class);
                intent.putExtra("trackIndex", trackNumber);
                startActivity(intent);
            }
        });


        ImageButton nextTrack = findViewById(R.id.nextTrack);
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackNumber < midiFile.getTrackCount() - 1){
                    trackNumber++;
                    trackNumberField.setText(String.valueOf(trackNumber + 1));
                    Log.d("Current track number: ", trackNumber + "");
                }
            }
        });

        ImageButton prevTrack = findViewById(R.id.prevTrack);
        prevTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackNumber > 0) {
                    trackNumber--;
                    trackNumberField.setText(String.valueOf(trackNumber + 1));
                    Log.d("Current track number: ", trackNumber + "");
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


}