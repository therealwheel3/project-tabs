package com.example.projecttabs;


import com.example.projecttabs.midi.util.MusicalConstants;
import com.example.projecttabs.midi.util.Packer;

import java.util.ArrayList;

class Cursor {
    private int tick = 0;
    private int key = 0;
    private int note;
    private int duration = 4;
    private int maxTick;
    boolean[][] tempAlts = new boolean[11][5];
    private int[] alts = new int[5];
    private int currAlt = 0;
    private ArrayList<float[]> notes = new ArrayList<>();
    private float K;
    private int resolution = 480 * 4;
    private int groupResolution = 480;
    private int n;

    Cursor(int key){
        this.key = key;
        this.note = 23;
    }

    public void setData(int resolution, int[] alts, int maxTick, int n){
        this.resolution = resolution;
        this.alts = alts;
        this.maxTick = maxTick;
        this.n = n;
    }

    public void setK(float k) {
        K = k;
    }

    public void upNote() {
        if (note < MusicalConstants.getMax(key)) {
            if (!((note % 12) == 4 || (note % 12) == 11)) {
                currAlt = currAlt == 0 ? 1 : 0;
            }
            note++;
        }
    }

    public void downNote() {
        if (note > 0) {
            if (!((note % 12) == 5 || (note % 12) == 0)) {
                currAlt = currAlt == 0 ? -2 : 0;
            }
            note--;
        }
    }

    public void rightNote(){
        if (tick + resolution / duration < maxTick){
            tick += resolution / duration;
        }
        if (!isNote()){
            placePause();
        }
    }

    public void leftNote(){
        for (int i = notes.size() - 1; i > 0; i--){
            if (notes.get(i)[0] < tick){
                tick = (int) notes.get(i)[0];
                break;
            }
        }
    }

    public void setDuration(int duration){
        this.duration = duration;
        replaceAllDuration();
    }

    public void setAlts(int[] alts) {
        this.alts = alts;
    }

    public void placeNote(){
        deleteThisPauses();
        notes.add(new float[]{tick, duration, resolution / duration,
                note + MusicalConstants.getIndent(key), currAlt});
        if (currAlt != 0){
            if (currAlt == 1 && !tempAlts[note / 12][note % 12]) { // !!!!!!!!!!!!!
                tempAlts[note / 12][note % 12] = true;
                currAlt = 0;
            }
        }
    }

    public void placePause(){
        notes.add(new float[]{tick, duration, resolution / duration});
    }
    private boolean isNote(){
        for (float[] temp : notes){
            if (temp[0] == tick) return true;
        }
        return false;
    }

    private void currDurationByTick(){
        for (float[] temp : notes){
            if (temp[0] == tick){
                duration = (int) temp[1];
            }
        }
    }

    private void replaceAllDuration(){
        for (int i = 0; i < notes.size(); i++){
            if (notes.get(i)[0] > tick) break;
            if (notes.get(i)[0] == tick){
                notes.get(i)[1] = duration;
                notes.get(i)[2] = resolution / duration;
            }
        }
    }

    private void deleteThisPauses(){
        for (int i = 0; i < notes.size(); i ++){
            if (notes.get(i)[0] == tick && notes.get(i).length == 3){
                notes.remove(i);
            }
        }
    }

    public ArrayList<float[]> getNotes() {
        notes = Packer.sortByGroups(notes, groupResolution);
        notes = Packer.setPauses(notes, resolution, maxTick * n, maxTick * (n + 1), groupResolution);
        return notes;
    }

    public float[] getCoords(){
        float x = tick * K;
        float y = Packer.getMove(note, key);
        return new float[]{x, y};
    }
}