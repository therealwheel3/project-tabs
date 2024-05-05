package com.example.projecttabs;


import android.util.Log;

import com.example.projecttabs.midi.util.MusicalConstants;
import com.example.projecttabs.midi.util.Packer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

class Cursor {
    private final HashSet<Integer> AlteredNotes = new HashSet<>(Arrays.asList(1, 3, 6, 8, 10));
    private final HashSet<Integer> FixedBemolNotes = new HashSet<>(Arrays.asList(4, 11));
    private final HashSet<Integer> FixedDiezNotes = new HashSet<>(Arrays.asList(5, 0));
    private int tick = 0;
    private int key = 0;
    private int note;
    private int duration = 4;
    private int maxTick;
    private int minTick;
    private int[] alts = new int[5];
    private int currAlt = 0;
    private ArrayList<float[]> notes = new ArrayList<>();
    private Stack<ArrayList<float[]>> prevNotes = new Stack<>();
    private float K;
    private int resolution = 480 * 4;
    private int ticksPerTact = 480 * 4;
    private int groupResolution = 480;
    private int n;
    private static HashSet<float[]> ligas = new HashSet<float[]>();

    Cursor(int key){
        this.key = key;
        this.note = 23;
    }

    public int getDuration(){
        return duration;
    }

    public void setData(int resolution, int[] alts, int minTick, int maxTick, int n, int groupResolution){
        // установить данные
        // set the data
        this.resolution = resolution;
        this.alts = alts;
        this.maxTick = maxTick;
        this.minTick = minTick;
        this.n = n;
        this.groupResolution = groupResolution;
        prevNotes.push(new ArrayList<>());
        ligas.clear();
    }

    public void setNotes(ArrayList<float[]> notes) {
        this.notes = (ArrayList<float[]>) notes.clone();
    }

    public void setK(float k) {
        K = k;
    }

    public void setAltSign(int sign){
        // поставить знак альтерации
        // put an alter sign
        if (isContain()){
            setPrevNotes(notes);
            int index = noteIndex();
            int currAlt = (int) notes.get(index)[4];
            int note = (int) notes.get(index)[3];
            if (currAlt != sign) {
                if (sign == -1){
                    notes.get(index)[4] = -1;
                    if (currAlt == 1){
                        notes.get(index)[3]--;
                    }
                    else if (currAlt == -2){
                        notes.get(index)[3]++;
                    }
                }
                if (currAlt == -2 && sign == 1 && !FixedDiezNotes.contains((note + 2) % 12)) {
                    notes.get(index)[3] += 2;
                    notes.get(index)[4] = 1;
                }
                else if (currAlt == 1 && sign == -2 && !FixedBemolNotes.contains((note - 2) % 12)) {
                    notes.get(index)[3] -= 2;
                    notes.get(index)[4] = -2;
                }
                else if (sign == 0) {
                    if (currAlt != -1) {
                        notes.get(index)[3] += (currAlt == -2 ? 1 : -1);
                        notes.get(index)[4] = sign;
                    }
                } else {
                    if (sign == 1 && AlteredNotes.contains((note + 1) % 12)){
                        notes.get(index)[3]++;
                        notes.get(index)[4] = sign;
                    }
                    if (sign == -2 && AlteredNotes.contains((note - 1) % 12)){
                        notes.get(index)[3]--;
                        notes.get(index)[4] = sign;
                    }
                }
            } else {
                if (sign == 1) {
                    notes.get(index)[3]--;
                    notes.get(index)[4] = 0;
                } else if (sign == -2) {
                    notes.get(index)[3]++;
                    notes.get(index)[4] = 0;
                }
            }
        }
    }

    public void cancelLastAction(){
        if (!prevNotes.isEmpty()) notes = prevNotes.peek();
    }

    public void upNote() {
        // поднять ноту на тон или полтона
        // raise a note by a tone or half a tone
        if (note < MusicalConstants.getMax(key)) {
            note++;
            int a = note % 12;
            if (AlteredNotes.contains(a)){
                note++;
            }
        }
    }

    public void downNote() {
        // опустить ноту на тон или полтона
        // drop a note by a tone or half a tone
        if (note > 0) {
            note--;
            int a = note % 12;
            if (AlteredNotes.contains(a)){
                note--;
            }
        }
    }

    public void upCurrentNote(){
        // поднять текущую ноту на тон или полтона
        // raise the current note by a tone or half a tone
        int index = noteIndex();
        if (index != -1) {
            note++;
            notes.get(index)[3]++;
            if (AlteredNotes.contains((int) (notes.get(index)[3] % 12)))
            {
                note++;
                notes.get(index)[3]++;
            }
        }
    }

    public void downCurrentNote(){
        // опустить текущую ноту на тон или полтона
        // lower the current note by a tone or half a tone
        int index = noteIndex();
        if (index != -1) {
            note--;
            notes.get(index)[3]--;
            if (AlteredNotes.contains((int) (notes.get(index)[3] % 12)))
            {
                note--;
                notes.get(index)[3]--;
            }
        }
    }

    public void rightNote(){
        // выбрать ноту правее если такая существует или переместиться враво на длительность
        // select a note to the right if there is one, or move to the right for the duration
        if (tick + resolution / duration < maxTick && !isNoteInRange(tick, tick + resolution / duration)) tick += resolution / duration;
        else getNextTick();
    }

    public void leftNote(){
        // выбрать ноту левее если такая существует или переместиться влево на длительность
        // select a note to the left if there is one, or move to the left for the duration
        if (tick - resolution / duration >= minTick && !isNoteInRange(tick - resolution / duration, tick)) tick -= resolution / duration;
        else getPrevTick();
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public void setConstAlts(int[] alts) {
        // установить постоянные знаки альтерации
        // set permanent alterations
        this.alts = alts;
    }

    public void placeNote(){
        // поставить ноту или удалить если такая уже существует
        // add a note or delete it if it already exists
        setPrevNotes(notes);
        if (!isContain() && resolution / duration + tick <= maxTick) {
            notes.add(new float[]{tick, duration, resolution / duration,
                    note + MusicalConstants.getIndent(key), currAlt});
            deleteCurrentPauses();
        }
        else if (!isContain()){
            Packer.completedNoteSplitter(new float[]{tick, duration, resolution / duration,
                    note + MusicalConstants.getIndent(key), currAlt}, resolution, notes, maxTick);
            for (int i = notes.size() - 1; i > 0; i--){
                if (notes.get(i)[0] >= maxTick) {
                    ligas.add(notes.get(i).clone());
                    notes.remove(i);
                }
                else break;
            }
            deleteCurrentPauses();
        }
        else {
            deleteNote();
        }
    }

    private void deleteCurrentPauses(){
        for (int i = 0; i < notes.size() - 1; i++){
            if (notes.get(i).length == 4){
                notes.remove(i);
                return;
            }
        }
    }

    public ArrayList<float[]> getNotes() {
        // получить список со всеми нотами
        // get a list with all the notes
        Packer.sortByFirstElement(notes);
        setAlts(true);
        if (!notes.isEmpty()) return Packer.setCompletedPauses(notes, resolution, minTick, maxTick, groupResolution);
        ArrayList<float[]> result = Packer.setCompletedPauses(new ArrayList<>(Arrays.asList(new float[]{0, 0, 0, 0, 0})), resolution, minTick, maxTick, groupResolution);
        result.remove(0);
        return result;
    }

    public ArrayList<float[]> getNotesForView() {
        // получить список со всеми нотами
        // get a list with all the notes
        Packer.sortByFirstElement(notes);
        setAlts(true);
        if (!notes.isEmpty()) return Packer.tactForView(Packer.setCompletedPauses(notes, resolution, minTick, maxTick, groupResolution), minTick);
        ArrayList<float[]> result = Packer.setCompletedPauses(new ArrayList<>(Arrays.asList(new float[]{0, 0, 0, 0, 0})), resolution, minTick, maxTick, groupResolution);
        result.remove(0);
        return result;
    }

    public float[] getCoords(){
        // получить текущие координаты
        // get the current coordinates
        float x = (tick - minTick) * K;
        float y = 27 - Packer.getMove(note, key);
        return new float[]{x, y};
    }

    public void nextTact(){
        // переключиться на следующий такт
        // switch to the next beat
        prevNotes.clear();
        tick = maxTick;
        maxTick += (maxTick - minTick);
        minTick = (maxTick + minTick) / 2;
        n++;
        notes.clear();
        HashSet<float[]> deletedLigas = new HashSet<>();
        for (float[] temp : ligas){
            if (temp[0] >= minTick && temp[0] < maxTick) {
                notes.add(temp);
                deletedLigas.add(temp);
            }
        }
        for (float[] temp : deletedLigas){
            ligas.remove(temp);
        }
        Packer.sortByFirstElement(notes);
    }

    public void prevTact(ArrayList<float[]> notes){
        // переключиться на предыдущий такт
        // switch to the previous clock cycle
        this.notes = notes;
        prevNotes.clear();
        minTick -= (maxTick - minTick);
        maxTick = (maxTick + minTick) / 2;
        tick = minTick;
        n--;
        Packer.sortByFirstElement(notes);
    }

    public boolean splitNote(int divisionFactor){
        // разделить ноту на n частей (создать мультиоль)
        // divide a note into n parts (create a multi-note)
        setPrevNotes(notes);
        if (isContain()){
            int index = noteIndex();
            float[] temp = notes.get(index);
            int div = (int) (temp[2] / divisionFactor);
            if (notes.get(index)[2] % divisionFactor == 0){
                deleteNoteWithoutPause();
                for (int i = 0; i < divisionFactor; i++){
                    if (i == 0) notes.add(new float[]{temp[0] + div * i, temp[1] * divisionFactor, div, temp[3], temp[4]});
                    else notes.add(new float[]{temp[0] + div * i, temp[1] * divisionFactor, div, temp[3], 0});
                }
                Packer.sortByFirstElement(notes);
                return true;
            }
        }
        return false;
    }

    public void createLiga(ArrayList<float[]> nextTact){
        // связать две ноты лига связкой
        // link two notes with a ligament
        if (isContain()){
            int index = noteIndex();
            for (int i = index + 1; i < notes.size(); i++){
                if (notes.get(index)[3] == notes.get(i)[3]){
                    if (notes.get(index).length == 5 && notes.get(i).length == 5){
                        float[] temp1 = new float[7];
                        float[] temp2 = new float[7];
                        for (int j = 0; j < 5; j++) temp1[j] = notes.get(index)[j];
                        for (int j = 0; j < 5; j++) temp2[j] = notes.get(i)[j];
                        temp1[6] = Packer.ligaNumber;
                        temp2[6] = Packer.ligaNumber;
                        temp1[2] = temp2[0] - temp1[0];
                        temp1[1] = resolution / temp1[2];
                        notes.remove(index);
                        notes.remove(i - 1);
                        notes.add(temp1.clone());
                        notes.add(temp2.clone());
                        Packer.sortByFirstElement(notes);
                        return;
                    }
                }
            }
            for (int i = 0; i < nextTact.size() - 1; i++){
                if (notes.get(index)[3] == nextTact.get(i)[3]){
                    if (notes.get(index).length == 5 && notes.get(i).length == 5){
                        float[] temp1 = new float[7];
                        float[] temp2 = new float[7];
                        for (int j = 0; j < 5; j++) temp1[j] = notes.get(index)[j];
                        for (int j = 0; j < 5; j++) temp2[j] = notes.get(i)[j];
                        temp1[6] = Packer.ligaNumber;
                        temp2[6] = Packer.ligaNumber;
                        temp1[2] = temp2[0] - temp1[0];
                        notes.remove(index);
                        notes.add(temp1.clone());
                        ligas.add(temp2);
                        Packer.sortByFirstElement(notes);
                        return;
                    }
                }
            }
        }
    }

    private boolean isContain(){
        // проверить существует ли нота на данной позиции
        // check if a note exists in this position
        for (float[] temp : notes){
            if (temp[0] == tick && temp.length != 4){
                if (temp[3] == note + MusicalConstants.getIndent(key)) return true;
                if (AlteredNotes.contains((int) temp[3] % 12) &&
                        (temp[3] == note + MusicalConstants.getIndent(key) + 1 || temp[3] == note + MusicalConstants.getIndent(key) - 1)) return true;
            }
            if (temp[0] > tick) return false;
        }
        return false;
    }

    private int noteIndex(){
        // получить индекс текущей ноты
        // get the index of the current note
        for (int i = 0; i < notes.size(); i++){
            float[] temp = notes.get(i);
            if (temp.length != 4 && (temp[0] == tick) && (temp[3] == note + MusicalConstants.getIndent(key) ||
                    (temp[3] == note + MusicalConstants.getIndent(key) + 1 || temp[3] == note + MusicalConstants.getIndent(key) - 1) &&
                            AlteredNotes.contains((int) temp[3] % 12))){
                return i;
            }
        }
        return -1;
    }

    private boolean isNoteOnTick(){
        Packer.sortByFirstElement(notes);
        for (float[] temp : notes){
            if (temp[0] == tick && temp.length != 4) {
                return true;
            }
            if (temp[0] > tick) return false;
        }
        return false;
    }

    private void setPause(float tick, float duration, float durationTick, float group){
        notes.add(new float[]{tick, duration, durationTick, group});
    }

    private void deleteNote(){
        // удалить текущую ноту
        // delete the current note
        for (int i = 0; i < notes.size(); i++){
            if (notes.get(i)[0] == tick) {
                if (notes.get(i)[3] == note + MusicalConstants.getIndent(key)) {
                    float tempTick = notes.get(i)[0];
                    float tempDuration = notes.get(i)[1];
                    float tempDurationTick = notes.get(i)[2];
                    float tempGroup = notes.get(i)[3];
                    notes.remove(i);
                    if (!isNoteOnTick()){setPause(tempTick, tempDuration, tempDurationTick, tempGroup);}
                    return;
                }
                if (AlteredNotes.contains((int) notes.get(i)[3] % 12) &&
                        (notes.get(i)[3] == note + MusicalConstants.getIndent(key) + 1 || notes.get(i)[3] == note + MusicalConstants.getIndent(key) - 1)){
                    float tempTick = notes.get(i)[0];
                    float tempDuration = notes.get(i)[1];
                    float tempDurationTick = notes.get(i)[2];
                    float tempGroup = notes.get(i)[3];
                    notes.remove(i);
                    if (!isNoteOnTick()){setPause(tempTick, tempDuration, tempDurationTick, tempGroup);}
                    return;
                }
            }
            if (notes.get(i)[0] > tick) return;
        }
    }

    private void deleteNoteWithoutPause(){
        // удалить текущую ноту
        // delete the current note
        for (int i = 0; i < notes.size(); i++){
            if (notes.get(i)[0] == tick) {
                if (notes.get(i)[3] == note + MusicalConstants.getIndent(key)) {
                    notes.remove(i);
                    return;
                }
                if (AlteredNotes.contains((int) notes.get(i)[3] % 12) &&
                        (notes.get(i)[3] == note + MusicalConstants.getIndent(key) + 1 || notes.get(i)[3] == note + MusicalConstants.getIndent(key) - 1)){
                    notes.remove(i);
                    return;
                }
            }
            if (notes.get(i)[0] > tick) return;
        }
    }

    private void setAlts(boolean alt){
        // проставить случайные знаки альтерации
        // put down random alterations
        HashSet<Integer> tempDiez = new HashSet<>();
        HashSet<Integer> tempBemol = new HashSet<>();
        for (int i = 0; i < notes.size(); i++) {
            float[] temp = notes.get(i);
            if (temp.length > 4) {
                int tempNote = (int) (temp[3]);
                int tempSign = (int) (temp[4]);
                if (temp[4] != -1) {
                    if (tempSign == 1) {
                        if (!tempDiez.contains(tempNote)) {
                            tempDiez.add(tempNote);
                        } else {
                            notes.get(i)[4] = 0;
                        }
                    } else if (tempSign == -2) {
                        if (!tempBemol.contains(tempNote)) {
                            tempBemol.add(tempNote);
                        } else {
                            notes.get(i)[4] = 0;
                        }
                    }
                    else {
                        if (tempDiez.contains(tempNote + 1)){
                            notes.get(i)[3]++;
                        }
                        else if (tempBemol.contains(tempNote - 1)){
                            notes.get(i)[3]--;
                        }
                    }
                }
                else {
                    if (!tempBemol.contains(tempNote - 1)){
                        notes.get(i)[4] = 0;
                    }
                    if (!tempDiez.contains(tempNote + 1)){
                        notes.get(i)[4] = 0;
                    }
                    if (tempBemol.contains(tempNote - 1)){
                        tempBemol.remove(tempNote - 1);
                        notes.get(i)[4] = -1;
                    }
                    if (tempDiez.contains(tempNote + 1)){
                        tempDiez.remove(tempNote + 1);
                        notes.get(i)[4] = -1;
                    }
                }
            }
        }
    }

    private boolean isNoteInRange(int startTick, int endTick){
        // проверить существует ли нота в заданном диапазоне
        // check if a note exists in the specified range
        for (float[] temp : notes){
            if (temp[0] < endTick && temp[0] > startTick) return true;
        }
        return false;
    }

    private void getNextTick(){
        // получить следующий тик, на котором есть нота
        // get the next tick that has a note on it
        for (float[] temp : notes){
            if (temp[0] > tick){
                tick = (int) temp[0];
                break;
            }
        }
    }

    private void setPrevNotes(ArrayList<float[]> prevNotes){
        this.prevNotes.push((ArrayList<float[]>) prevNotes.clone());
    }

    private void getPrevTick(){
        // получить предыдущий тик, на котором есть нота
        // get the previous tick that has a note on it
        int prevTick = 0;
        for (float[] temp : notes){
            if (temp[0] == tick){
                tick = prevTick;
                break;
            }
            prevTick = (int) temp[0];
        }
    }

    public boolean checkNoteDurations(ArrayList<float[]> temps){
        // проверить длительность нот
        // check the duration of the notes
        Packer.sortByFirstElement(temps);
        ArrayList<float[]> notesWithPauses = Packer.setCompletedPauses(temps, resolution, maxTick * n, maxTick * (n + 1), groupResolution);
        Packer.sortByFirstElement(notesWithPauses);
        float tempDuration = 0;
        float prev = -1;
        for (float[] temp : notesWithPauses){
            if (temp[0] != prev){
                tempDuration += temp[2];
            }
            prev = temp[0];
        }
        return tempDuration == maxTick;
    }

    public int getMinTick() {
        return minTick;
    }

    public void setTicksPerTact(int ticksPerTact) {
        this.ticksPerTact = ticksPerTact;
    }

    public int getTicksPerTact() {
        return ticksPerTact;
    }

    public void clearTact(){
        prevNotes.push(notes);
        notes = new ArrayList<>();
    }
}