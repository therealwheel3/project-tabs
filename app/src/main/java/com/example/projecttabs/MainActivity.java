package com.example.projecttabs;

import static com.example.projecttabs.R.drawable.*;

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
    Cursor cursor;
    LinesWithCursor lines;
    private int duration = 4;
    ArrayList<float[]> fmap;
    ArrayList<ArrayList<float[]>> map;
    MidiFile midiFile;
    float[] mainInfo;
    int n = 0;
    ArrayList<ArrayList<float[]>> changedTrack = new ArrayList<>();
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

        cursor = new Cursor(0);
        cursor.setData(480 * 4, new int[7], 0, 480 * 4, 0, 480);
        cursor.setTicksPerTact(480 * 4);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = mSettings.edit();

        initialSetup();

        ImageButton arrowDown = (android.widget.ImageButton) findViewById(R.id.downArrow); // for cursor
        arrowDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.downNote();
                lines.invalidate();
            }
        });

        ImageButton arrowUp = (android.widget.ImageButton) findViewById(R.id.upArrow);
        arrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.upNote();
                lines.invalidate();
            }
        });

        ImageButton arrowRight = (android.widget.ImageButton) findViewById(R.id.rightArrow);
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.rightNote();
                lines.invalidate();
            }
        });

        ImageButton arrowLeft = (android.widget.ImageButton) findViewById(R.id.leftArrow);
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.leftNote();
                lines.invalidate();
            }
        });
        ImageButton doubleArrowRight = findViewById(R.id.rightDoubleArrow);
        doubleArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changedTrack.size() <= n){
                    changedTrack.add(cursor.getNotes());
                }
                else {
                    changedTrack.set(n, cursor.getNotes());
                }
                cursor.nextTact();
                n++;
                if (changedTrack.size() > n){
                    cursor.setNotes(changedTrack.get(n));
                }
                lines.setData(cursor.getNotesForView());
                lines.setTactNumber(n + 1);
                lines.invalidate();
            }
        });
        ImageButton doubleLeftArrow = findViewById(R.id.leftDoubleArrow);
        doubleLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (n > 0) {
                    if (changedTrack.size() > n) {
                        changedTrack.set(n, cursor.getNotes());
                    }
                    else changedTrack.add(cursor.getNotes());
                    n--;
                    cursor.prevTact(changedTrack.get(n));
                    cursor.setNotes(changedTrack.get(n));
                    lines.setData(cursor.getNotesForView());
                    lines.setTactNumber(n + 1);
                    lines.invalidate();
                }
            }
        });

        ImageButton placeNote = findViewById(R.id.placeNote);
        placeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.placeNote();
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
                for (float[] temp : cursor.getNotesForView()){
                    Log.d("dldkkdkddjd", Arrays.toString(temp));
                }
            }
        });

        Button plusDuration = findViewById(R.id.sizeRight);
        Button minusDuration = findViewById(R.id.sizeLeft);



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

        Button sizeMinus = findViewById(R.id.sizeLeft);
        Button sizePlus = findViewById(R.id.sizeRight);
        sizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration < 32) {
                    duration *= 2;
                    cursor.setDuration(duration);
                    switch (duration){
                        case 2:
                            placeNote.setImageResource(R.drawable.n2);
                            break;
                        case 4:
                            placeNote.setImageResource(R.drawable.n4);
                            break;
                        case 8:
                            placeNote.setImageResource(R.drawable.n8);
                            break;
                        case 16:
                            placeNote.setImageResource(R.drawable.n16);
                            break;
                        case 32:
                            placeNote.setImageResource(R.drawable.n32);
                            break;
                    }
                }
            }
        });

        sizePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration > 1) {
                    duration /= 2;
                    cursor.setDuration(duration);
                    switch (duration){
                        case 1:
                            placeNote.setImageResource(R.drawable.n0);
                            break;
                        case 2:
                            placeNote.setImageResource(R.drawable.n2);
                            break;
                        case 4:
                            placeNote.setImageResource(R.drawable.n4);
                            break;
                        case 8:
                            placeNote.setImageResource(R.drawable.n8);
                            break;
                        case 16:
                            placeNote.setImageResource(R.drawable.n16);
                            break;
                    }
                }
            }
        });

        Button diezButton = findViewById(R.id.diez);
        Button bemolButton = findViewById(R.id.bemol);
        Button bekarButton = findViewById(R.id.bekar);
        diezButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.setAltSign(1);
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
            }
        });

        bekarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.setAltSign(-1);
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
            }
        });

        bemolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.setAltSign(-2);
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
            }
        });

        Button cancelLastActionButton = findViewById(R.id.returnLastAction);
        cancelLastActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.cancelLastAction();
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
            }
        });

        Button clearTact = findViewById(R.id.clearTact);
        clearTact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.clearTact();
                lines.setData(cursor.getNotesForView());
                lines.invalidate();
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
            try {
                if (!(data instanceof Nullable)) {
                    String path = data.getData().getPath().split(":")[1];
                    file = new File(path);
                    editor.putString("filePath", path);
                    editor.apply();
                    midiFile = new MidiFile(file);
                }
                    initialSetup();
            } catch (IOException e) {
                Log.d("midifile", "open failed");
            }
            int resolution = midiFile.getResolution();
            List<MidiTrack> tracks = midiFile.getTracks();
            ArrayList<MidiEvent> events = new ArrayList<>(tracks.get(0).getEvents());
            ArrayList<float[]> fmap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
            map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);

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
            map = Packer.finalMap(fmap, resolution * 4, resolution, resolution * 4);
            if (map.size() > 0){
                lines = new LinesWithCursor(this);
                lines.setCursor(cursor);
                cursor.setNotes(map.get(0));
                lines.setData(map.get(0), 0, 4, 4,
                        MusicalConstants.getIndent(0), 0, midiFile.getResolution() * 4, 1);
                line1.removeAllViews();
                line1.addView(lines);

                for (MidiTrack temp : tracks){
                    float[] info = Packer.getMainInfo(new ArrayList<>(temp.getEvents()));
                    Log.d("main info ", Arrays.toString(info));
                    if (!Arrays.equals(info, mainInfo)){
                        mainInfo = info.clone();
                        break;
                    }
                }

                Cursor cursor = new Cursor(0);
                cursor.setData(840 * 4, new int[7], 0, 840 * 4, 0, 840);
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
                lines = new LinesWithCursor(this);
                cursor.setNotes(map.get(0));
                lines.setCursor(cursor);
                lines.setData(map.get(0), 0, 4, 4,
                        MusicalConstants.getIndent(0), 0, midiFile.getResolution() * 4, 1);
                line1.removeAllViews();
                line1.addView(lines);

                for (MidiTrack temp : tracks){
                    float[] info = Packer.getMainInfo(new ArrayList<>(temp.getEvents()));
                    Log.d("main info ", Arrays.toString(info));
                    if (!Arrays.equals(info, mainInfo)){
                        mainInfo = info.clone();
                        break;
                    }
                }

            }
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }
    }
}