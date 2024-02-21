package com.xayup.toolpad.utils;

public class BpmTimer {
    public static long getMilliseconds(int bpm, long step){
        return (60000L/bpm) * step;
    }

    public static long getMilliseconds(int bpm, int ms_time, int steps){
        return (long)(((bpm*ms_time)/60000f)*1000*steps);
    }

    public static int getBeastPerMilliseconds(int bpm, int ms_time){
        return (int) (bpm * (ms_time / 1000f)) / 60;
    }

    public static long getBeatDelay(int bpm, int ms_time){
        return (long) ((60f/bpm) * (ms_time / 1000f)) * 1000;
    }

    public static float getBeatPer(int bpm, int ms_time){
        return (bpm * (ms_time/60000f));
    }
}
