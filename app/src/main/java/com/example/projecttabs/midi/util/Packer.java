package com.example.projecttabs.midi.util;

import android.util.Log;

import com.example.projecttabs.midi.MidiFile;
import com.example.projecttabs.midi.MidiTrack;
import com.example.projecttabs.midi.event.Controller;
import com.example.projecttabs.midi.event.MidiEvent;
import com.example.projecttabs.midi.event.NoteOff;
import com.example.projecttabs.midi.event.NoteOn;
import com.example.projecttabs.midi.event.PitchBend;
import com.example.projecttabs.midi.event.ProgramChange;
import com.example.projecttabs.midi.event.meta.Tempo;
import com.example.projecttabs.midi.event.meta.TimeSignature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Packer {
    public static int ligaNumber = 0;
    public static ArrayList<Integer> countNotesPerBar(ArrayList<MidiEvent> events, int resolution) {
        // returns a list with the number of notes in all bars
        //возращает список с количеством нот во всех тактах
        ArrayList<Integer> measures = new ArrayList<Integer>();
        int n = 0;
        int k = 1;
        for (MidiEvent event : events) {
            n++;
            if (event.getTick() >= (long) resolution * k) {
                measures.add(n);
                n = 0;
                k++;
            }
        }
        return measures;
    }

    public static ArrayList<MidiEvent> Bars(ArrayList<MidiEvent> events, int start, int end){
        //returns a list of midi messages in the specified range
        //возвращает список из миди сообщений в указанном диапазоне
        ArrayList<MidiEvent> result = new ArrayList<MidiEvent>();
        for (int i = start; i < end; i++){
            result.add(events.get(i));
        }
        return result;
    }

    public static int getEndIndex(ArrayList<Integer> bars, int index){
        //returns the number of messages going up to the given
        //возвращает количество сообщений идущих до данного
        int summ = -1;
        for (int i = 0; i < index; i++){
            summ += bars.get(i);
        }
        return summ;
    }

    public static long getTickDifference(ArrayList<MidiEvent> events, int startIndex, int endIndex){
        //returns the number of ticks between two messages
        //возвращает количество тиков между двумя сообщениями
        return events.get(endIndex).getTick() - events.get(startIndex).getTick();
    }

    public static double getNoteLength(ArrayList<MidiEvent> events, int index, int resolution){
        //returns the duration of the note in ticks
        //возвращает длительность ноты в тиках
        float tick = 1;
        MidiEvent event1 = events.get(index);
        for (MidiEvent event2: events.subList(index + 1, events.size())){
            if (event1.getNote() == event2.getNote()
                    && event2.getVelocity() == 0
                    && event2 instanceof NoteOn
                    && event2.getChannel() == event1.getChannel()
                    && event1.getTick() != event2.getTick()){
                tick = event2.getTick() - event1.getTick();
                break;
            } else if (event2 instanceof NoteOff
                    && event2.getNote() == event1.getNote()
                    && event1.getChannel() == event2.getChannel()
                    && event1.getTick() != event2.getTick()) {
                tick = event2.getTick() - event1.getTick();
                break;
            }
        }
        return defineNoteLenPlus(tick, resolution);
    }

    public static ArrayList<Long> generateTickMap(ArrayList<MidiEvent> events, int resolution) {
        //generates a list with all event ticks
        //генерирует список со всеми тиками ивентов
        ArrayList<Long> result = new ArrayList<Long>();
        for (MidiEvent event : events) {
            result.add(event.getTick());
        }
        return result;
    }

    public static float getNoteLengthAsSize(ArrayList<MidiEvent> events, int resolution, int index){
        //get the note length as a numeric value
        //получить длину ноты в качестве числового значения
        MidiEvent event = events.get(index);
        if (event instanceof NoteOn) {
            double ticks = getNoteLength(events, index, resolution);
            return (float) 1 / ((float) ticks / resolution);
        }
        return 0;
    }

    public static float getNoteLengthAsReversedSize(ArrayList<MidiEvent> events, int resolution, int index){
        //get the note length as a numeric value
        //получить длину ноты в качестве числового значения
        MidiEvent event = events.get(index);
        if (event instanceof NoteOn) {
            double ticks = getNoteLength(events, index, resolution);
            return (float) ticks / resolution;
        }
        return 0;
    }

    public static ArrayList<float[]> generateNotesMap(ArrayList<MidiEvent> events, int resolution, int maxTicks){
        //generate a list with all the sounding notes
        //сгенерировать список со всеми звучащими нотами
        //# - 1, -1 - bek, 0 - nothing
        ArrayList<Long> map1 = generateTickMap(events, resolution);
        int k = 1;
        ArrayList<float[]> result = new ArrayList<>();
        boolean[][] tempAlts = new boolean[11][5];
        int currentMaxTicks = maxTicks;
        for (int i = 0; i < map1.size(); i++) {
            float[] temp;
            if (events.get(i).getNote() == 0 && events.get(i).getVelocity() == 1){
                temp = new float[4];
                temp[0] = map1.get(i);
                temp[2] = (float) getNoteLength(events, i, resolution);
                temp[1] = resolution / temp[2];
                temp[3] = events.get(i).getNote();
                result.add(temp);
            }
            else if (events.get(i) instanceof NoteOn && events.get(i).getVelocity() != 0) {
                temp = new float[5];
                if (map1.get(i) >= (long) k * resolution) {
                    tempAlts = new boolean[11][5];
                    k++;
                    currentMaxTicks += maxTicks;
                }
                temp[0] = map1.get(i);
                float noteSize = getNoteLengthAsSize(events, resolution, i);
                temp[1] = noteSize;
                temp[2] = (float) getNoteLength(events, i, resolution);
                temp[3] = events.get(i).getNote();

                int note = (int) temp[3] % 12;
                switch (note) {
                    case 1:
                        if (!tempAlts[note / 12][0]) {
                            temp[4] = 1;
                        }
                        tempAlts[note / 12][0] = true;
                        break;
                    case 3:
                        if (!tempAlts[note / 12][1]) {
                            temp[4] = 1;
                        }
                        tempAlts[note / 12][1] = true;
                        break;
                    case 6:
                        if (!tempAlts[note / 12][2]) {
                            temp[4] = 1;
                        }
                        tempAlts[note / 12][2] = true;
                        break;
                    case 8:
                        if (!tempAlts[note / 12][3]) {
                            temp[4] = 1;
                        }
                        tempAlts[note / 12][3] = true;
                        break;
                    case 10:
                        if (!tempAlts[note / 12][4]) {
                            temp[4] = 1;
                        }
                        tempAlts[note / 12][4] = true;
                        break;
                }
                if (tempAlts[note / 12][0] && (int) temp[3] % 12 == 0) {
                    temp[4] = -1;
                }
                if (tempAlts[note / 12][1] && (int) temp[3] % 12 == 2) {
                    temp[4] = -1;
                }
                if (tempAlts[note / 12][2] && (int) temp[3] % 12 == 5) {
                    temp[4] = -1;
                }
                if (tempAlts[note / 12][3] && (int) temp[3] % 12 == 7) {
                    temp[4] = -1;
                }
                if (tempAlts[note / 12][4] && (int) temp[3] % 12 == 9) {
                    temp[4] = -1;
                }
                if (temp[1] < 1){
                    //temp[1] = getNoteLengthAsReversedSize(events, resolution, i);
                    noteSplitter(temp, resolution, result, maxTicks);
                }
                else if (temp[0] + temp[2] > currentMaxTicks){
                    noteSplitter(temp, resolution, result, maxTicks);
                }
                else if (temp[1] % 1 != 0 && !isWithAdot(temp[1])){
                    noteSplitter(temp, resolution, result, maxTicks);
                }
                //else if (isCompositeNote(temp[2], resolution));
                else result.add(temp);
            }
        }
        sortByFirstElement(result);
        return result;
    }

    public static byte getKey(float[][] bar){
        //a function that gives out a key suitable for a musical beat
        //функция выдающая ключ, подходящий к музыкальному такту
        byte maxx = 0;
        byte minn = 127;
        for (float[] temp : bar){
            if (temp.length > 2){
                if (temp[3] > maxx){
                    maxx = (byte) temp[3];
                }
                if (temp[3] < minn){
                    minn = (byte) temp[3];
                }
            }
        }
        if (minn >= 24 && maxx <= 71){
            // bass key
            return 1;
        }
        if (minn >= 48 && maxx <= 95){
            // violin key
            return 0;
        }
        // unknown key
        return -1;
    }

    public static float[] getMainInfo(ArrayList<MidiEvent> events){
        // 0 - bpm, 1 - nom, 2 - den, 3 - program , 4 - group nom, 5 - group den, 6 - channel
        //a function that provides basic information about the track
        //функция выдающая основную информацию о треке
        float[] result = new float[]{120, 4, 4, 0, 1, 4, 0};
        boolean isTempo = true;
        for (MidiEvent event : events){
            if (event instanceof NoteOn){
                result[6] = event.getChannel();
            }
            if (event instanceof Tempo){
                if(isTempo) {
                    result[0] = Math.round(((Tempo) event).getBpm());
                    isTempo = true;
                }
            }
            if (event instanceof TimeSignature){
                result[1] = ((TimeSignature) event).getNumerator();
                result[2] = ((TimeSignature) event).getRealDenominator();
            }
            if (event instanceof ProgramChange){
                result[3] = ((ProgramChange) event).getProgramNumber();
            }
        }
        if(result[1] == result[2]){
            result[1] = 4;
            result[2] = 4;
        }
        if (result[2] == 8){
            result[4] = result[1] / 3;
            result[5] = 8;
        }
        else {
            result[5] = result[2];
        }
        return result;
    }

    public static ArrayList<ArrayList<float[]>> finalMap(ArrayList<float[]> notes, int resolution, int groupResolution, int maxTicks){
        //функция создающая финальный список тактов
        //a function that creates a final list of musical bars
        int k = 1;
        ArrayList<ArrayList<float[]>> result = new ArrayList<>();
        ArrayList<float[]> temp = new ArrayList<>();
        for (float[] note : notes) {
            if (note[0] >= k * maxTicks) {
                if (!temp.isEmpty()) result.add(setPauses(temp, resolution, (k - 1) * resolution, k * resolution, groupResolution));
                else result.add((ArrayList<float[]>) temp.clone());
                temp.clear();
                k++;
            }
            while (note[0] >= k * maxTicks){
                k++;
                result.add(new ArrayList<>());
            }
            temp.add(note.clone());
        }
        if (!temp.isEmpty()) result.add(setPauses(temp, resolution, (k - 1) * maxTicks, k * maxTicks, groupResolution));
        else result.add((ArrayList<float[]>) temp.clone());
        return result;
    }

    public static boolean checkDuration(ArrayList<float[]> bar, float duration){
        //a function that checks the duration of a musical beat
        //функция проверяющая длительности в музыкальном такте
        float prev = bar.get(0)[0];
        float checker = 1 / bar.get(0)[1];
        for (float[] temp : bar){
            if (prev != temp[0]){
                if (temp[1] >= 1){
                    checker += (1 / temp[1]);
                    prev = temp[0];
                }
                else {
                    checker += temp[1];
                }
            }
        }
        return duration == checker;
    }

    public static ArrayList<float[]> setCompletedPauses(ArrayList<float[]> bar, int resolution, int startTick, int endTick, int groupResolution){
        //a function that sets all the pauses in the beat
        //функция выставляющая все паузы в такте
        ArrayList<float[]> result = new ArrayList<>();
        int primeFactorNumber = primeFactor(resolution);
        float[] temp;
        if (startTick != bar.get(0)[0]){
            if (isBinDen((int) (bar.get(0)[0] - startTick), primeFactorNumber)){
                temp = new float[]{startTick,
                        resolution / (bar.get(0)[0] - startTick),
                        bar.get(0)[0] - startTick};
                pauseSplitter(temp, resolution, result, endTick - startTick);
            }
            else {
                int tick = (int) (bar.get(0)[0] - startTick);
                int tempStartTick = tick;
                while (tick > 0){
                    if (Packer.isBinDen(tick, primeFactorNumber)){
                        if (Packer.isBinDen((tempStartTick - tick), (int) (resolution / bar.get(0)[1]))){
                            break;
                        }
                    }
                    tick--;
                }
                if (tick > 0) {
                    float[] temp1 = new float[]{startTick, (float) resolution / tick,
                            tick};
                    pauseSplitter(temp1, resolution, result, endTick - startTick);
                }
                while (tick < tempStartTick){
                    result.add(new float[]{tick, bar.get(0)[1], resolution / bar.get(0)[1]});
                    tick += resolution / bar.get(0)[1];
                }
            }
        }
        for (int i = 0; i < bar.size() - 1; i++){
            result.add(bar.get(i));
            if (isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)) {
                if (bar.get(i)[0] + bar.get(i)[2] < bar.get(i + 1)[0]) {
                    temp = new float[]{bar.get(i)[0] + bar.get(i)[2],
                            resolution / (bar.get(i + 1)[0] - bar.get(i)[0] - bar.get(i)[2]),
                            bar.get(i + 1)[0] - bar.get(i)[0] - bar.get(i)[2]};
                    pauseSplitter(temp, resolution, result, endTick - startTick);
                }
            }
            else if (isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && !isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)){
                int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                int tempEndTick = (int) bar.get(i + 1)[0];
                int tickDiff = 1;
                while (tickDiff < tempEndTick){
                    if (Packer.isBinDen((tempEndTick - tickDiff), primeFactorNumber)){
                        if (Packer.isBinDen(tickDiff, (int) (resolution / bar.get(i + 1)[1]))){
                            break;
                        }
                    }
                    tickDiff++;
                }
                if (tempEndTick - tempStartTick > tickDiff){
                    temp = new float[]{tempStartTick, (float) resolution / (tempEndTick - tempStartTick - tickDiff), tempEndTick - tempStartTick - tickDiff};
                    pauseSplitter(temp, resolution, result, endTick);
                }
                for (int j = 0; j < tickDiff / (resolution / bar.get(i + 1)[1]); j++){
                    result.add(new float[]{tempStartTick + resolution / bar.get(i + 1)[1] * j, bar.get(i + 1)[1], resolution / bar.get(i + 1)[1]});
                }
            }
            else if (!isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)){
                int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                int tempEndTick = (int) bar.get(i + 1)[0];
                int tickDiff = 1;
                while (tickDiff < tempEndTick - tempStartTick){
                    if (Packer.isBinDen((tempEndTick - tickDiff), primeFactorNumber)){
                        if (Packer.isBinDen(tickDiff, (int) (resolution / bar.get(i)[1]))){
                            break;
                        }
                    }
                    tickDiff++;
                }
                for (int j = 0; j < tickDiff / (resolution / bar.get(i)[1]); j++){
                    result.add(new float[]{tempStartTick + resolution / bar.get(i)[1] * j, bar.get(i)[1], resolution / bar.get(i)[1]});
                }
                if (tempEndTick - tempStartTick > tickDiff){
                    temp = new float[]{tempStartTick, (float) resolution / (tempEndTick - tempStartTick - tickDiff), tempEndTick - tempStartTick - tickDiff};
                    pauseSplitter(temp, resolution, result, endTick);
                }
            }
            else {
                if (bar.get(i)[1] == bar.get(i + 1)[1]){
                    int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                    int tempEndTick = (int) bar.get(i + 1)[0];
                    int tickDiff = tempEndTick - tempStartTick;
                    for (int j = 0; j < tickDiff / (resolution / bar.get(i)[1]); j++){
                        result.add(new float[]{tempStartTick + resolution / bar.get(i)[1] * j, bar.get(i)[1], resolution / bar.get(i)[1]});
                    }
                }
                else {
                    int resolution1 = (int) (resolution / bar.get(i)[1]);
                    int resolution2 = (int) (resolution / bar.get(i + 1)[1]);
                }
            }
        }
        result.add(bar.get(bar.size() - 1));
        if (endTick > bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2]){
            if (isBinDen((int) (bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2]), primeFactorNumber)) {
                temp = new float[]{bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2],
                        resolution / (endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]),
                        endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]};
                if (temp[1] % 1 != 0) {
                    pauseSplitter(temp, resolution, result, endTick - startTick);
                } else result.add(temp);
            }
            else {
                int tick = (int) (endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]);
                int tempEndTick = endTick - tick;
                while (tick > 0){
                    if (Packer.isBinDen((tempEndTick - tick), (int) (resolution / bar.get(bar.size() - 1)[1]))){
                        break;
                    }
                    tick--;
                }
                while (tempEndTick < endTick){
                    result.add(new float[]{tempEndTick, bar.get(bar.size() - 1)[1], resolution / bar.get(bar.size() - 1)[1]});
                    tempEndTick += resolution / bar.get(bar.size() - 1)[1];
                    tick -= resolution / bar.get(bar.size() - 1)[1];
                    if (isBinDen(tempEndTick, primeFactorNumber)) break;
                }
                if (endTick - tempEndTick > 0) {
                    pauseSplitter(new float[]{tempEndTick, (float) resolution / (endTick - tempEndTick), endTick - tempEndTick},
                            resolution, result, endTick);
                }
            }
        }
        result = sortByCompletedGroups(result, groupResolution);
        return result;
    }

    public static ArrayList<float[]> setPauses(ArrayList<float[]> bar, int resolution, int startTick, int endTick, int groupResolution){
        //a function that sets all the pauses in the beat
        //функция выставляющая все паузы в такте
        ArrayList<float[]> result = new ArrayList<>();
        int primeFactorNumber = primeFactor(resolution);
        float[] temp;
        if (startTick != bar.get(0)[0]){
            if (isBinDen((int) (bar.get(0)[0] - startTick), primeFactorNumber)){
                temp = new float[]{startTick,
                        resolution / (bar.get(0)[0] - startTick),
                        bar.get(0)[0] - startTick};
                pauseSplitter(temp, resolution, result, endTick - startTick);
            }
            else {
                int tick = (int) (bar.get(0)[0] - startTick);
                int tempStartTick = tick;
                while (tick > 0){
                    if (Packer.isBinDen(tick, primeFactorNumber)){
                        if (Packer.isBinDen((tempStartTick - tick), (int) (resolution / bar.get(0)[1]))){
                            break;
                        }
                    }
                    tick--;
                }
                if (tick > 0) {
                    float[] temp1 = new float[]{startTick, (float) resolution / tick,
                            tick};
                    pauseSplitter(temp1, resolution, result, endTick - startTick);
                }
                while (tick < tempStartTick){
                    result.add(new float[]{tick, bar.get(0)[1], resolution / bar.get(0)[1]});
                    tick += resolution / bar.get(0)[1];
                }
            }
        }
        for (int i = 0; i < bar.size() - 1; i++){
            result.add(bar.get(i));
            if (isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)) {
                if (bar.get(i)[0] + bar.get(i)[2] < bar.get(i + 1)[0]) {
                    temp = new float[]{bar.get(i)[0] + bar.get(i)[2],
                            resolution / (bar.get(i + 1)[0] - bar.get(i)[0] - bar.get(i)[2]),
                            bar.get(i + 1)[0] - bar.get(i)[0] - bar.get(i)[2]};
                    pauseSplitter(temp, resolution, result, endTick - startTick);
                }
            }
            else if (isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && !isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)){
                int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                int tempEndTick = (int) bar.get(i + 1)[0];
                int tickDiff = 1;
                while (tickDiff < tempEndTick){
                    if (Packer.isBinDen((tempEndTick - tickDiff), primeFactorNumber)){
                        if (Packer.isBinDen(tickDiff, (int) (resolution / bar.get(i + 1)[1]))){
                            break;
                        }
                    }
                    tickDiff++;
                }
                if (tempEndTick - tempStartTick > tickDiff){
                    temp = new float[]{tempStartTick, (float) resolution / (tempEndTick - tempStartTick - tickDiff), tempEndTick - tempStartTick - tickDiff};
                    pauseSplitter(temp, resolution, result, endTick);
                }
                for (int j = 0; j < tickDiff / (resolution / bar.get(i + 1)[1]); j++){
                    result.add(new float[]{tempStartTick + resolution / bar.get(i + 1)[1] * j, bar.get(i + 1)[1], resolution / bar.get(i + 1)[1]});
                }
            }
            else if (!isBinDen((int) (bar.get(i)[0] + bar.get(i)[2]), primeFactorNumber) && isBinDen((int) (bar.get(i + 1)[0]), primeFactorNumber)){
                int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                int tempEndTick = (int) bar.get(i + 1)[0];
                int tickDiff = 1;
                while (tickDiff < tempEndTick - tempStartTick){
                    if (Packer.isBinDen((tempEndTick - tickDiff), primeFactorNumber)){
                        if (Packer.isBinDen(tickDiff, (int) (resolution / bar.get(i)[1]))){
                            break;
                        }
                    }
                    tickDiff++;
                }
                for (int j = 0; j < tickDiff / (resolution / bar.get(i)[1]); j++){
                    result.add(new float[]{tempStartTick + resolution / bar.get(i)[1] * j, bar.get(i)[1], resolution / bar.get(i)[1]});
                }
                if (tempEndTick - tempStartTick > tickDiff){
                    temp = new float[]{tempStartTick, (float) resolution / (tempEndTick - tempStartTick - tickDiff), tempEndTick - tempStartTick - tickDiff};
                    pauseSplitter(temp, resolution, result, endTick);
                }
            }
            else {
                if (bar.get(i)[1] == bar.get(i + 1)[1]){
                    int tempStartTick = (int) (bar.get(i)[0] + bar.get(i)[2]);
                    int tempEndTick = (int) bar.get(i + 1)[0];
                    int tickDiff = tempEndTick - tempStartTick;
                    for (int j = 0; j < tickDiff / (resolution / bar.get(i)[1]); j++){
                        result.add(new float[]{tempStartTick + resolution / bar.get(i)[1] * j, bar.get(i)[1], resolution / bar.get(i)[1]});
                    }
                }
                else {
                    int resolution1 = (int) (resolution / bar.get(i)[1]);
                    int resolution2 = (int) (resolution / bar.get(i + 1)[1]);
                }
            }
        }
        result.add(bar.get(bar.size() - 1));
        if (endTick > bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2]){
            if (isBinDen((int) (bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2]), primeFactorNumber)) {
                temp = new float[]{bar.get(bar.size() - 1)[0] + bar.get(bar.size() - 1)[2],
                        resolution / (endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]),
                        endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]};
                if (temp[1] % 1 != 0) {
                    pauseSplitter(temp, resolution, result, endTick - startTick);
                } else result.add(temp);
            }
            else {
                int tick = (int) (endTick - bar.get(bar.size() - 1)[0] - bar.get(bar.size() - 1)[2]);
                int tempEndTick = endTick - tick;
                while (tick > 0){
                    if (Packer.isBinDen((tempEndTick - tick), (int) (resolution / bar.get(bar.size() - 1)[1]))){
                        break;
                    }
                    tick--;
                }
                while (tempEndTick < endTick){
                    result.add(new float[]{tempEndTick, bar.get(bar.size() - 1)[1], resolution / bar.get(bar.size() - 1)[1]});
                    tempEndTick += resolution / bar.get(bar.size() - 1)[1];
                    tick -= resolution / bar.get(bar.size() - 1)[1];
                    if (isBinDen(tempEndTick, primeFactorNumber)) break;
                }
                if (endTick - tempEndTick > 0) {
                    pauseSplitter(new float[]{tempEndTick, (float) resolution / (endTick - tempEndTick), endTick - tempEndTick},
                            resolution, result, endTick);
                }
            }
        }
        result = sortByGroups(result, groupResolution);
        return result;
    }

    public static boolean isBinDen(int ticks, int primeDen){
        while (ticks > 0){
            ticks -= primeDen;
            if (ticks < 0) return false;
        }
        return true;
    }


    public static ArrayList<float[]> sortByGroups(ArrayList<float[]> bar, int groupResolution){
        float group = 1;
        sortByFirstElement(bar);
        int ticks = (int) bar.get(0)[2];
        ArrayList<float[]> result = new ArrayList<>();
        int prevTick = (int) bar.get(0)[0];
        for (float[] temp : bar) {
            if (ticks >= groupResolution && prevTick != temp[0]) {
                group *= -1;
                ticks = 0;
            }
            if (temp[0] != prevTick){
                ticks += temp[2];
                prevTick = (int) temp[0];
            }
            if (temp.length == 5) result.add(new float[]{temp[0], temp[1], temp[2], temp[3], temp[4], group});
            else if (temp.length == 6) result.add(new float[]{temp[0], temp[1], temp[2], temp[3], temp[4], group, temp[5]});
            else result.add(new float[]{temp[0], temp[1], temp[2], group});
        }
        return result;
    }

    public static ArrayList<float[]> sortByCompletedGroups(ArrayList<float[]> bar, int groupResolution){
        float group = 1;
        sortByFirstElement(bar);
        int ticks = (int) bar.get(0)[2];
        ArrayList<float[]> result = new ArrayList<>();
        int prevTick = (int) bar.get(0)[0];
        for (float[] temp : bar) {
            if (ticks >= groupResolution && prevTick != temp[0]) {
                group *= -1;
                ticks = 0;
            }
            if (temp[0] != prevTick){
                ticks += temp[2];
                prevTick = (int) temp[0];
            }
            if (temp.length == 6 || temp.length == 5) result.add(new float[]{temp[0], temp[1], temp[2], temp[3], temp[4], group});
            else if (temp.length == 7) result.add(new float[]{temp[0], temp[1], temp[2], temp[3], temp[4], group, temp[6]});
            else result.add(new float[]{temp[0], temp[1], temp[2], group});
        }
        return result;
    }

    public static void noteSplitter(float[] note, int resolution, ArrayList<float[]> bars, int maxTicks){
        float n;
        if (note[1] < 1) {n = 1 / note[1];}
        else {n = note[1];}
        n = 1 / note[1];
        n = (float)(Math.round(n * 1000000)) / 1000000;
        float currentTick = note[0];
        ArrayList<float[]> result = new ArrayList<>();
        float denominator = 1;
        int currentMaxTicks = maxTicks;
        while (n > 0){
            while (currentTick >= currentMaxTicks){
                currentMaxTicks += maxTicks;
            }
            if (n >= denominator && n - denominator >= 0 && currentTick + resolution * denominator <= currentMaxTicks){
                result.add(new float[]{currentTick, 1 / denominator, resolution * denominator, note[3], note[4], ligaNumber});
                n -= denominator;
                currentTick += resolution * denominator;
                denominator = 1;
            }
            else {
                denominator /= 2;
            }
        }
        //for (float[] temp : result){
        //    System.out.println(Arrays.toString(temp));
        //}
        if (result.get(result.size() - 1)[1] > 5096 || result.size() == 1) bars.add(note);
        else if (result.size() > 1) {
            bars.addAll(result);
            ligaNumber++;
        }
        else {
            float[] temp = new float[5];
            IntStream.range(0, 5).forEach(i -> temp[i] = result.get(0)[i]);
            bars.add(temp);
        }
    }

    public static void completedNoteSplitter(float[] note, int resolution, ArrayList<float[]> bars, int maxTicks){
        float n;
        if (note[1] < 1) {n = 1 / note[1];}
        else {n = note[1];}
        n = 1 / note[1];
        n = (float)(Math.round(n * 1000000)) / 1000000;
        float currentTick = note[0];
        ArrayList<float[]> result = new ArrayList<>();
        float denominator = 1;
        int currentMaxTicks = maxTicks;
        while (n > 0){
            while (currentTick >= currentMaxTicks){
                currentMaxTicks += maxTicks;
            }
            if (n >= denominator && n - denominator >= 0 && currentTick + resolution * denominator <= currentMaxTicks){
                result.add(new float[]{currentTick, 1 / denominator, resolution * denominator, note[3], note[4], 0, ligaNumber});
                n -= denominator;
                currentTick += resolution * denominator;
                denominator = 1;
            }
            else {
                denominator /= 2;
            }
        }
        //for (float[] temp : result){
        //    System.out.println(Arrays.toString(temp));
        //}
        if (result.get(result.size() - 1)[1] > 5096 || result.size() == 1) bars.add(note);
        else if (result.size() > 1) {
            bars.addAll(result);
            ligaNumber++;
        }
        else {
            float[] temp = new float[5];
            IntStream.range(0, 5).forEach(i -> temp[i] = result.get(0)[i]);
            bars.add(temp);
        }
    }

    public static void pauseSplitter(float[] note, int resolution, ArrayList<float[]> bars, int maxTicks){
        float n = 1 / note[1];
        n = (float)(Math.round(n * 1000000)) / 1000000;
        float currentTick = note[0];
        ArrayList<float[]> result = new ArrayList<>();
        float denominator = 1;
        int currentMaxTicks = maxTicks;
        while (n > 0){
            while (currentTick >= currentMaxTicks){
                currentMaxTicks += maxTicks;
            }
            if (n >= denominator && n - denominator >= 0 && currentTick + resolution * denominator <= currentMaxTicks){
                result.add(new float[]{currentTick, 1 / denominator, resolution * denominator});
                n -= denominator;
                currentTick += resolution * denominator;
                denominator = 1;
            }
            else {
                denominator /= 2;
            }
        }
        if (!result.isEmpty()) {
            if (result.get(result.size() - 1)[1] > 5096 || result.size() == 1) bars.add(note);
            else bars.addAll(result);
        }
    }
    public static ArrayList<float[]> pauseSplitter(int divFactor, int resolution, float ticks, float startTick){
        ArrayList<float[]> result = new ArrayList<>();
        float[] temp = new float[4];
        while (ticks != 0){
            float factor = resolution / divFactor;
            temp[0] = startTick;
            temp[1] = divFactor;
            temp[2] = factor;
            ticks -= factor;
            startTick += factor;
            result.add(temp);
            temp = new float[4];
        }
        return result;
    }

    public static int primeFactor(int denominator){
        while (denominator % 2 == 0 && denominator > 2){
            denominator /= 2;
        }
        return denominator;
    }

    public static void sortByFirstElement(ArrayList<float[]> arrays) {Collections.sort(arrays, (a, b) -> (int) (a[0] - b[0]));}

    public static float defineNoteLen(float tick){
        float result = 1;
        if (tick >= 1){
            while (tick >= result){
                result *= 2;
            }
            result /= 2;
            if (tick - result < 0){
                return result * 2;
            }
            return result;
        }
        return 0;
    }

    public static float defineNoteLenPlus(float tick, float resolution){
        float startTick = tick;
        int a = 0;
        int limit = (int) (resolution / 15);
        int adder = 0;
        while (tick > resolution){
            tick -= resolution;
            limit *= 2;
            adder += resolution;
        }
        while (a < limit){
            if ((resolution / (tick + a)) % 1 == 0 || isCompositeNote(tick + a, resolution)){
                if (isCompositeNote(tick + a, resolution)) return tick + a + adder;
                return tick + a + adder;
            }
            a++;
        }
        return defineNoteLenMinus(startTick, resolution);
    }

    public static float defineNoteLenMinus(float tick, float resolution){
        int a = 0;
        int limit = (int) (resolution / 15);
        int adder = 0;
        while (tick > resolution){
            tick -= resolution;
            limit *= 2;
            adder += resolution;
        }
        while (a < limit){
            if ((resolution / (tick - a)) % 1 == 0 || isCompositeNote(tick - a, resolution)){
                if (isCompositeNote(tick - a, resolution)) return tick - a + adder;
                return tick - a + adder;
            }
            a++;
        }
        System.out.println(a);
        return tick + adder;
    }

    public static boolean ligaCheck(int liga, ArrayList<float[]> bar){
        for (float[] temp : bar){
            if (temp.length == 7 && liga == temp[6]) return true;
        }
        return false;
    }

    public static boolean isWithAdot(float duration){
        for (int i = 2; i < 65; i *= 2) {
            if (3f / i == duration) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCompositeNote(float tick, float resolution){
        tick %= resolution;
        for (float i = 1; i > 0.0078125; i /= 2){
            if (tick >= resolution * i) tick -= resolution * i;
            if (tick == 0) return true;
        }
        return false;
    }

    public static boolean stickDirection(ArrayList<float[]> notes, float midY){
        int up = 0;
        int down = 0;
        for (float[] temp : notes){
            if (temp[4] > midY) up++;
            else down++;
        }
        return down > up;
    }

    public static int getMove(float note, int key){
        float move = 0;
        for (int i = 0; i < note; i++){
            if (i % 12 == 4 || i % 12 == 11){
                move += 1;
            }
            else {move += 0.5;}
        }
        return (int)(move - key * 1.5);
    }

    public static ArrayList<float[]> withoutPauses(ArrayList<float[]> notes){
        ArrayList<float[]> result = new ArrayList<>();
        for (float[] temp : notes){
            if (temp.length >= 6) result.add(temp);
        }
        return result;
    }

    public static MidiTrack packToTrack(ArrayList<ArrayList<float[]>> notes, int channel) {
        MidiTrack midiTrack = new MidiTrack();
        HashSet<Integer> usedLigas = new HashSet<>();
        for (ArrayList<float[]> temp1 : notes){
            for (float[] temp2 : temp1){
                if (temp2.length == 6) {
                    midiTrack.insertNote(channel, (int) temp2[3], 80, (long) temp2[0], (long) temp2[2]);
                    Log.d("dddddd", Arrays.toString(temp2));
                }
                else if (temp2.length > 6){
                    if (!usedLigas.contains((int) (temp2[6]))){
                        midiTrack.insertNote(channel, (int) temp2[3], 80, (long) temp2[0], ligaNoteLen(notes, (int) temp2[6]));
                    }
                    usedLigas.add((int) temp2[6]);
                    Log.d("eeeeeeee", Arrays.toString(temp2));
                }
            }
        }
        return midiTrack;
    }

    public static MidiFile packAllTracks(ArrayList<ArrayList<ArrayList<float[]>>> changedTracks, List<MidiTrack> tracks){
        MidiFile file = new MidiFile();
        for (int i = 0; i < changedTracks.size(); i++){
            int channel = 0;
            if (i < tracks.size()) channel = getChannel(tracks.get(i).getEvents());
            MidiTrack track = packToTrack(changedTracks.get(i), channel);
            if (i < tracks.size()){
                for (MidiEvent event : tracks.get(i).getEvents()){
                    if (!(event instanceof NoteOn) && !(event instanceof NoteOff)){
                        if (event.getChannel() == channel || event instanceof Tempo) {
                            track.insertEvent(event);
                        }
                    }
                }
            }
            file.addTrack(track);
        }
        return file;
    }

    public static int getChannel(TreeSet<MidiEvent> events){
        for (MidiEvent event : events){
            if (event instanceof NoteOn || event instanceof NoteOff) return event.getChannel();
        }
        return 0;
    }

    public static HashSet<Integer> ligaSet(ArrayList<float[]> bar){
        HashSet<Integer> result = new HashSet<>();
        for (float[] temp : bar){
            if (temp.length == 7){
                result.add((int) temp[6]);
            }
        }
        return result;
    }
    private static int ligaNoteLen(ArrayList<ArrayList<float[]>> notes, int liga){
        int result = 0;
        boolean isLiga = true;
        boolean isStarted = false;
        for (ArrayList<float[]> temp1 : notes){
            for (float[] temp2 : temp1){
                isLiga = false;
                if (temp2.length == 7) {
                    if (temp2[6] == liga) {
                        isLiga = true;
                        isStarted = true;
                        result += (int) temp2[2];
                    }
                }
            }
            if (!isLiga && isStarted) {
                if (result == 0) return 1920;
                return result;
            }
        }
        return result;
    }

    public static MidiTrack quantizeMidiTrack(MidiTrack track, double quantizationGrid) {
        // Квантовать все события
        // Quantize all events
        MidiTrack quantizedTrack = new MidiTrack();
        ArrayList<MidiEvent> events = new ArrayList<>(track.getEvents());
        for (MidiEvent event : events) {
            if (event instanceof NoteOn || event instanceof NoteOff) {
                long quantizedTime;
                quantizedTime = (long) (Math.round(event.getTick() / quantizationGrid) * quantizationGrid);
                if (event instanceof NoteOn && event.getVelocity() != 0){
                    quantizedTrack.insertEvent(new NoteOn(quantizedTime, 0, event.getNote(), 100));
                }
                else {
                    quantizedTrack.insertEvent(new NoteOff(quantizedTime, 0, event.getNote(), 0));
                }
            }
            else quantizedTrack.insertEvent(event);
        }
        return quantizedTrack;
    }

    public static MidiTrack overrideResolution(MidiTrack track, int prevResolution, int newResolution){
        float k = (float) newResolution / prevResolution;
        MidiTrack newTrack = new MidiTrack();
        for (MidiEvent event : track.getEvents()){
            if (event.getTick() * k % 1 != 0) return track;
            if (event instanceof NoteOn){
                newTrack.insertEvent(new NoteOn((long) (event.getTick() * k), event.getChannel(), event.getNote(), event.getVelocity()));
            }
            if (event instanceof NoteOff){
                newTrack.insertEvent(new NoteOff((long) (event.getTick() * k), event.getChannel(), event.getNote(), event.getVelocity()));
            }
            if (event instanceof ProgramChange){
                newTrack.insertEvent(new ProgramChange((long) (event.getTick() * k), event.getDelta(), event.getChannel(), ((ProgramChange) event).getProgramNumber()));
            }
            if (event instanceof Controller){
                newTrack.insertEvent(new Controller((long) (event.getTick() * k), event.getChannel(), ((Controller) event).getControllerType(), ((Controller) event).getValue()));
            }
            if (event instanceof Tempo){
                newTrack.insertEvent(new Tempo((long) (event.getTick() * k), event.getDelta(), ((Tempo) event).getMpqn()));
            }
            if (event instanceof PitchBend){
                newTrack.insertEvent(new PitchBend((long) (event.getTick() * k), event.getChannel(), ((PitchBend) event).getLeastSignificantBits(), ((PitchBend) event).getMostSignificantBits()));
            }
            if (event instanceof TimeSignature){
                newTrack.insertEvent(new TimeSignature((long) (event.getTick() * k), event.getDelta(), ((TimeSignature) event).getNumerator(), ((TimeSignature) event).getRealDenominator(), ((TimeSignature) event).getMeter(), ((TimeSignature) event).getDivision()));
            }
        }
        return newTrack;
    }

    public static ArrayList<float[]> tactForView(ArrayList<float[]> tact, int resolution){
        ArrayList<float[]> result = new ArrayList<>();
        for (int i = 0; i < tact.size(); i++){
            result.add(tact.get(i));
            if (resolution != 0) result.get(i)[0] %= resolution;
        }
        return result;
    }

    public static boolean isOnlyPauses(ArrayList<float[]> tact){
        for (float[] temp : tact){
            if (temp.length > 4) return false;
        }
        return true;
    }

    public static ArrayList<float[]> deleteUselessNotes(ArrayList<float[]> tact){
        ArrayList<float[]> result = new ArrayList<>();
        for (float[] temp : tact){
            if (temp[1] > 0){
                result.add(temp);
            }
        }
        return result;
    }
}