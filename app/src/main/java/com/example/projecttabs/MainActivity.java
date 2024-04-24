package com.example.projecttabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static final String APP_PREFERENCES = "mysettings";
    private File file = new File("/storage/emulated/0/download/Ball3.mid");
    private int duration = 4;
    ArrayList<float[]> fmap;
    ArrayList<ArrayList<float[]>> map;
    MidiFile midiFile;
    float[] mainInfo;
    int n = 0;
    private int trackNumber = 0;
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    LinearLayout line1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { "android.permission.READ_EXTERNAL_STORAGE" }, 1);
        }

        setContentView(R.layout.activity_main);
        try {
            midiFile = new MidiFile(file);
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }

        line1 = findViewById(R.id.firstLine);
        TextView trackNumberField = findViewById(R.id.TrackNumber);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        initialSetup();

        ImageButton arrowDown = (android.widget.ImageButton) findViewById(R.id.downArrow); // for cursor

        ImageButton arrowUp = (android.widget.ImageButton) findViewById(R.id.upArrow);

        ImageButton arrowRight = (android.widget.ImageButton) findViewById(R.id.rightArrow);

        ImageButton arrowLeft = (android.widget.ImageButton) findViewById(R.id.leftArrow);

        ImageButton doubleArrowRight = findViewById(R.id.rightDoubleArrow);
        ImageButton doubleLeftArrow = findViewById(R.id.leftDoubleArrow);

        Button plusDuration = findViewById(R.id.sizeRight);
        Button minusDuration = findViewById(R.id.sizeLeft);
        TextView noteSize = findViewById(R.id.noteSize);



        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Play.class);
                intent.putExtra("trackIndex", trackNumber);
                intent.putExtra("filePath", file.getPath());
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
                    changeTrack(trackNumber);
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
                    changeTrack(trackNumber);
                    Log.d("Current track number: ", trackNumber + "");
                }
            }
        });
        Button button = findViewById(R.id.menu_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, 10);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10){
            if (!data.getData().getPath().equals("")){
                String path = data.getData().getPath().split(":")[1];
                file = new File(path);
                editor.putString("filePath", path);
                editor.apply();
            }
            try {
                midiFile = new MidiFile(file);
                initialSetup();
            } catch (IOException e) {
                Log.d("midifile", "open failed");
            }
            int resolution = midiFile.getResolution();
            List<MidiTrack> tracks = midiFile.getTracks();
            ArrayList<MidiEvent> events = new ArrayList<>(tracks.get(0).getEvents());
            ArrayList<float[]> fmap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
            ArrayList<ArrayList<float[]>> map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);

        }
    }

    private void initialSetup(){
        file = new File(mSettings.getString("filePath", "/storage/emulated/0/download/Ball3.mid"));
        try {
            midiFile = new MidiFile(file);
            int resolution = midiFile.getResolution();
            List<MidiTrack> tracks = midiFile.getTracks();
            ArrayList<MidiEvent> events = new ArrayList<>(tracks.get(0).getEvents());
            ArrayList<float[]> fmap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
            ArrayList<ArrayList<float[]>> map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);
            if (map.size() > 0){
                Log.d("ldlsdlsdlsdlsdlsldlsdsdsdlsldlsdlsdldslds", trackNumber + "");
                LinesWithCursor tempLine = new LinesWithCursor(this);
                tempLine.setData(map.get(0), 0, 4, 4,
                        MusicalConstants.getIndent(0), 0, midiFile.getResolution() * 4, 1);
                line1.removeAllViews();
                line1.addView(tempLine);

                for (MidiTrack temp : tracks){
                    float[] info = Packer.getMainInfo(new ArrayList<>(temp.getEvents()));
                    Log.d("main info ", Arrays.toString(info));
                    if (!Arrays.equals(info, mainInfo)){
                        mainInfo = info.clone();
                        break;
                    }
                }

                Cursor cursor = new Cursor();
                cursor.setData(resolution, new int[]{}, (int) (resolution * mainInfo[1] / (4 / mainInfo[2])),
                        n, (int) (resolution * mainInfo[3] / (4 / mainInfo[5])));
            }
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }
    }

    private void changeTrack(int n){
        file = new File(Objects.requireNonNull(mSettings.getString("filePath", "/storage/emulated/0/download/Ball3.mid")));
        try {
            midiFile = new MidiFile(file);
            int resolution = midiFile.getResolution();
            List<MidiTrack> tracks = midiFile.getTracks();
            ArrayList<MidiEvent> events = new ArrayList<>(tracks.get(trackNumber).getEvents());
            ArrayList<float[]> fmap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
            ArrayList<ArrayList<float[]>> map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);
            if (map.size() > 0){
                Log.d("ldlsdlsdlsdlsdlsldlsdsdsdlsldlsdlsdldslds", trackNumber + "");
                LinesWithCursor tempLine = new LinesWithCursor(this);
                tempLine.setData(map.get(0), 0, 4, 4,
                        MusicalConstants.getIndent(0), 0, midiFile.getResolution() * 4, 1);
                line1.removeAllViews();
                line1.addView(tempLine);

                for (MidiTrack temp : tracks){
                    float[] info = Packer.getMainInfo(new ArrayList<>(temp.getEvents()));
                    Log.d("main info ", Arrays.toString(info));
                    if (!Arrays.equals(info, mainInfo)){
                        mainInfo = info.clone();
                        break;
                    }
                }

                Cursor cursor = new Cursor();
                cursor.setData(resolution, new int[]{}, (int) (resolution * mainInfo[1] / (4 / mainInfo[2])),
                        n, (int) (resolution * mainInfo[3] / (4 / mainInfo[5])));
            }
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }
    }
}