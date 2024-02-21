package com.xayup.widgets.seekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.List;
import java.util.Map;

public class CustomSeekBar extends SeekBar {

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public CustomSeekBar(Context context) {super(context);}
    public CustomSeekBar(Context context, AttributeSet attrs) {super(context, attrs);}
    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}

    //CUSTOM
    protected Map<Integer, List<int[]>> frames_steps = null;

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        super.setOnSeekBarChangeListener(listener);
        mOnSeekBarChangeListener = listener;
    }

    public void triggerChangeListener(boolean fromUser) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    public boolean callOnStartTrackingTouch(){
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
            return true;
        }
        return false;
    }

    public boolean callOnProgressChanged(boolean fromUser){
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
            return true;
        }
        return false;
    }

    public boolean callOnStopTrackingTouch(){
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
            return true;
        }
        return false;
    }

    public void setData(Map<Integer, List<int[]>> data){ this.frames_steps = data; }
    public Map<Integer, List<int[]>> getData(){ return this.frames_steps; }
}
