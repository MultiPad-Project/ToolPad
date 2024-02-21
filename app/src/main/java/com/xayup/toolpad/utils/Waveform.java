package com.xayup.toolpad.utils;

import android.util.Log;

public class Waveform {
    public static int getLenghtBaseFromParent(int step_lenght, int use_steps_counts, int ms_samples_time){
        Log.v("getLenghtBaseFromParent", "step_lenght " + step_lenght + ", use_steps_counts " + use_steps_counts + ", ms_samples_time " + ms_samples_time);
        return (int) ((ms_samples_time * (use_steps_counts/1000f)) * (step_lenght*use_steps_counts) + 0.9);
    }
}
