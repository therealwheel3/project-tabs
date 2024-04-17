package com.example.projecttabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.metrics.BundleSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projecttabs.midi.MidiFile;
import com.example.projecttabs.midi.MidiTrack;
import com.example.projecttabs.midi.event.MidiEvent;
import com.example.projecttabs.midi.util.MusicalConstants;
import com.example.projecttabs.midi.util.Packer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Play extends AppCompatActivity {
    private final File file = new File("/storage/emulated/0/download/Ball1.mid");
    MidiFile midiFile;
    boolean currentTact = true;
    int tactsCount;
    int n = 0;
    int key = 0;
    int nominator = 4;
    int denominator = 4;
    int resolution;
    int trackNumber;
    LinesWithoutCursor line;
    LinearLayout line1;
    LinearLayout line2;
    Timer timer = new Timer();
    Timer tactTimer = new Timer();
    ArrayList<ArrayList<float[]>> map;
    MediaPlayer mediaPlayer = new MediaPlayer();
    boolean isPlay = false;
    boolean isChanged = false;
    float[] mainInfo = new float[]{120, 4, 4, 0, 1, 4};
    LinearLayout tactCursorLayout;
    TactCursor tactCursor;
    TextView bpmView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        tactCursorLayout = findViewById(R.id.tactCursor);
        tactCursor = new TactCursor(this);
        tactCursorLayout.addView(tactCursor);
        bpmView = findViewById(R.id.BPM);
        Bundle arguments = getIntent().getExtras();
        trackNumber = (int) arguments.get("trackIndex");


        Button playButton = findViewById(R.id.playButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button returnButton = findViewById(R.id.returnButton);
        Button backLineButton = findViewById(R.id.back);
        Button forwardLineButton = findViewById(R.id.forward);

        try {
            midiFile = new MidiFile(file);
            Log.d("midi file ", "open");
        } catch (IOException e) {
            Log.d("midifile", "open failed");
        }

        int resolution = midiFile.getResolution();
        List<MidiTrack> tracks = midiFile.getTracks();
        ArrayList<MidiEvent> events = new ArrayList<MidiEvent>(tracks.get(trackNumber).getEvents());
        ArrayList<float[]> tempMap = Packer.generateNotesMap(events, resolution * 4, resolution * 4);
        map = Packer.finalMap(tempMap, resolution * 4, resolution, resolution * 4);

        tactsCount = map.size();
        for (MidiTrack temp : tracks){
            float[] info = Packer.getMainInfo(new ArrayList<>(temp.getEvents()));
            Log.d("main info ", Arrays.toString(info));
            if (!Arrays.equals(info, mainInfo)){
                mainInfo = info.clone();
                break;
            }
        }

        int tactTime = (int) ((60000 / mainInfo[0]) * mainInfo[1]); // !!!!!
        initialSetup(resolution * 4);
        tactCursor.setData((int) mainInfo[0], (int) (tactTime / mainInfo[1]));

        Log.d("saasdasadaadwadawdaw", tactTime + " " + mainInfo[0]);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(Play.this, Uri.fromFile(file));
                Log.d("File start playing ", "true");
                timer = new Timer();
                mediaPlayer.start();
                isPlay = true;
                backLineButton.setClickable(false);
                forwardLineButton.setClickable(false);
                playButton.setClickable(false);
                tactTimer = new Timer();
                tactTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tactCursor.invalidate();
                            }
                        });
                    }}, 0, 1);

                if (!isChanged) {
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (n < map.size() - 1) {
                                        tactCursor.reload(n % 2 == 0);
                                        updateLine(resolution * 4, true);
                                    } else {
                                        tactCursor.reload(n % 2 == 0);
                                        timer.cancel();
                                        isPlay = false;
                                    }
                                }
                            });
                        }}, tactTime, tactTime);
                }
                else {
                    mediaPlayer.seekTo((n - 2) * tactTime);
                    isChanged = false;
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (n < map.size() - 1) {
                                        tactCursor.reload(n % 2 == 0);
                                        updateLine(resolution * 4, true);
                                    } else {
                                        tactCursor.reload(n % 2 == 0);
                                        timer.cancel();
                                        isPlay = false;
                                    }
                                }
                            });
                        }}, tactTime, tactTime);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tactCursor.reload();
                timer.cancel();
                tactTimer.cancel();
                isPlay = false;
                isChanged = true;
                backLineButton.setClickable(true);
                forwardLineButton.setClickable(true);
                playButton.setClickable(true);
                mediaPlayer.pause();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tactCursor.reload(true);
                timer.cancel();
                n = 1;
                isChanged = false;
                initialSetup(resolution * 4);
                isPlay = false;
                backLineButton.setClickable(true);
                forwardLineButton.setClickable(true);
                playButton.setClickable(true);
                mediaPlayer.stop();
                tactTimer.cancel();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                isPlay = false;
                Intent intent  = new Intent(Play.this, MainActivity.class);
                startActivity(intent);
            }
        });

        backLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (n >= 1) updateLine(resolution * 4, false);
                        tactCursor.reload(n % 2 == 0);
                    }
                });
            }
        });
        forwardLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (n < map.size()) updateLine(resolution * 4, true);
                        tactCursor.reload(n % 2 == 0);
                    }
                });
            }
        });
    }

    private void updateLine(int resolution, boolean direction){
        line = new LinesWithoutCursor(Play.this);
        if (direction) {
            n++;
        } else {
            n--;
        }

        try {
            line.setPrevLigas(Packer.ligaSet(map.get(n - 1)));
        } catch (Exception ignored){}
        try {
            line.setNextLigas(Packer.ligaSet(map.get(n + 1)));
        } catch (Exception ignored){}

        line.setData(map.get(n), key, nominator, denominator, MusicalConstants.getIndent(key), n, resolution, n + 1);
        if (currentTact) {
            line1.removeAllViews();
            line1.addView(line);
        } else {
            line2.removeAllViews();
            line2.addView(line);
        }
        currentTact = !currentTact;
        isChanged = true;
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

    private void initialSetup(int resolution){
        Log.d("initial setup ", "initial");
        line = new LinesWithoutCursor(this);
        line.setData(map.get(0), key, nominator, denominator, MusicalConstants.getIndent(key), 0, resolution, 1);
        if (map.size() > 2){line.setNextLigas(Packer.ligaSet(map.get(1)));}
        line1.removeAllViews();
        line1.addView(line);
        line = new LinesWithoutCursor(this);
        if (map.size() > 1){
            line.setData(map.get(1), key, nominator, denominator, MusicalConstants.getIndent(key), 1, resolution, 2);
            line2.removeAllViews();
            line2.addView(line);
            line.setPrevLigas(Packer.ligaSet(map.get(0)));
            Log.d("asdasdasdsad", Packer.ligaSet(map.get(0)) + " " + Packer.ligaSet(map.get(2)));
            if (map.size() > 3){line.setNextLigas(Packer.ligaSet(map.get(2)));}
            tactCursorLayout.addView(new TactCursor(this));
            currentTact = true;
            n = 1;
        }

        bpmView.setText((int)(mainInfo[0]) + " BPM");
    }
}