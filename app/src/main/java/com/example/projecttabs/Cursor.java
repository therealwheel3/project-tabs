package com.example.projecttabs;


import com.example.projecttabs.midi.util.MusicalConstants;
import com.example.projecttabs.midi.util.Packer;

import java.util.ArrayList;
import java.util.HashSet;

class Cursor {
    private int tick = 0;
    private int key = 0;
    private int note;
    private int duration = 4;
    private int maxTick;
    private HashSet<Integer> tempDiez = new HashSet<>();
    private HashSet<Integer> tempBemol = new HashSet<>();
    private int[] alts = new int[5];
    private int currAlt = 0;
    private ArrayList<float[]> notes = new ArrayList<>();
    private int currentNote = 0;
    private float K;
    private int resolution = 480 * 4;
    private int groupResolution = 480;
    private int n;

    Cursor(int key){
        this.key = key;
        this.note = 23;
    }

    Cursor(){
        this.key = 0;
        this.note = 23;
    }

    public void setData(int resolution, int[] alts, int maxTick, int n, int groupResolution){
        this.resolution = resolution;
        this.alts = alts;
        this.maxTick = maxTick;
        this.n = n;
        this.groupResolution = groupResolution;
    }

    public void setNotes(ArrayList<float[]> notes) {
        this.notes = notes;
        for (float[] temp : notes){
            if (temp[4] == 1){
                tempDiez.add((int) temp[3]);
            }
            if (temp[4] == -2){
                tempBemol.add((int) temp[3]);
            }
        }
    }

    public void setK(float k) {
        K = k;
    }

    public void setAltSign(int sign){
        if (currAlt != sign) {
            if (currAlt == -1 && sign == 1){
                note += 2;
                currAlt = sign;
            }
            else if (currAlt == 1 && sign == -1){
                note -= 2;
                currAlt = sign;
            }
            if (currAlt == -1 && sign == -2){
                note += 1;
                currAlt = -2;
            }
            if (currAlt == 1 && sign == -2){
                note -= 1;
                currAlt = -2;
            }
        }
        else{
            if (sign == 1){
                note--;
            }
            else if (sign == -2){
                note++;
            }
        }
    }

    public void upNote() {
        if (note < MusicalConstants.getMax(key)) {
            note++;
            int a = note % 12;
            if (a == 1 || a == 3 || a == 6 || a == 8 || a == 10) {
                currAlt = currAlt == 0 ? 1 : 0;
            }
        }
    }

    public void downNote() {
        if (note > 0) {
            note--;
            int a = note % 12;
            if (a == 1 || a == 3 || a == 6 || a == 8 || a == 10) {
                currAlt = currAlt == 0 ? -2 : 0;
            }
        }
    }

    public void rightNote(){
        if (tick + resolution / duration < maxTick){
            tick += resolution / duration;
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
        if (!isContain()) {
            if (currAlt == 1 && !tempDiez.contains(note)) { // !!!!!!!!!!!!!
                if (!tempBemol.contains(note)) tempDiez.add(note);
            } else if (currAlt == 1 && tempDiez.contains(note)) { // !!!!!!!!!!!!!
                if (!tempBemol.contains(note)) tempDiez.add(note);
                currAlt = 0;
            }
            if (tempDiez.contains(note + 1) && note % 12 != 4 && note % 12 != 11) {
                currAlt = -1;
            }
            if (currAlt == -2 && !tempBemol.contains(note)) {
                if (!tempDiez.contains(note)) tempBemol.add(note);
            } else if (currAlt == -2 && tempBemol.contains(note)) {
                if (!tempDiez.contains(note)) tempBemol.add(note);
                currAlt = 0;
            }
            if (tempBemol.contains(note - 1) && note % 12 != 5 && note % 12 != 0) {
                currAlt = -1;
            }
            notes.add(new float[]{tick, duration, resolution / duration,
                    note + MusicalConstants.getIndent(key), currAlt});
        }
        else {
            deleteNote();
        }
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

    public ArrayList<float[]> getNotes() {
        Packer.sortByFirstElement(notes);
        return Packer.setCompletedPauses(notes, resolution, maxTick * n, maxTick * (n + 1), groupResolution);
    }

    public float[] getCoords(){
        float x = tick * K;
        float y = Packer.getMove(note, key);
        return new float[]{x, y};
    }

    private boolean isContain(){
        for (float[] temp : notes){
            if (temp[0] == tick && temp[1] == duration && temp[3] == note + MusicalConstants.getIndent(key)){
                return true;
            }
            if (temp[0] > tick) return false;
        }
        return false;
    }

    private void deleteNote(){
        for (int i = 0; i < notes.size() - 1; i++){
            if (notes.get(i)[0] == tick && notes.get(i)[1] == duration && notes.get(i)[3] == note + MusicalConstants.getIndent(key)){
                notes.remove(i);
                return;
            }
        }
    }

    private void deletePauses(){
        int a = 0;
        for (int i = 0; i < notes.size() - 1; i++){
            if (notes.get(i).length < 5){
                notes.remove(i - a);
                a++;
            }
        }
    }
}