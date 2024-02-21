package com.xayup.toolpad.leds.frames;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * PRIVATE
 */
public class Frame {
    List<int[]> frame;

    public static final short INDEX_TYPE = 0;
    public static final short INDEX_VALUE = 1;
    public static final short INDEX_ROW = 2;
    public static final short INDEX_COLUM = 3;
    public static final short LIGHT_ARRAY_SIZE = 4;

    @IntDef({LightType.TYPE_ON, LightType.TYPE_OFF, LightType.TYPE_LOGO, LightType.TYPE_DELAY, LightType.TYPE_REMOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LightType{ short TYPE_ON = 1; short TYPE_OFF = 2; short TYPE_LOGO = 3; short TYPE_DELAY = 4; short TYPE_REMOVE = 5; }
    public @interface LightTypeText{ String TYPE_ON = "o"; String TYPE_OFF = "f"; String TYPE_LOGO = "l"; String TYPE_DELAY = "d"; String TYPE_PAD_CHAIN = "mc"; String TYPE_LIGHT_AUTO = "a"; }


    private int xOffset = 0;
    private int yOffset = 0;

    public void addLight(@LightType int type, int padRow, int padColum, int color){
        int[] light = new int[LIGHT_ARRAY_SIZE];
        light[INDEX_TYPE] = (color == 0) ? LightType.TYPE_OFF : type;
        light[INDEX_ROW] = padRow;
        light[INDEX_COLUM] = padColum;
        light[INDEX_VALUE] = color;
        for(int[] light_old : frame)
            if(light_old[INDEX_ROW] == padRow && light_old[INDEX_COLUM] == padColum){
                frame.remove(light_old); break;
            }
        if(type != LightType.TYPE_REMOVE) frame.add(light);
    }

    public void deleteLight(int padRow, int padColum){
        for(int[] light_old : frame)
            if(light_old[INDEX_ROW] == padRow && light_old[INDEX_COLUM] == padColum){
                frame.remove(light_old); break;
            }
    }

    @NonNull public List<int[]> getList(){ return frame; }

    public void setOffset(int x, int y){ xOffset = x; yOffset = y; }
    public int getXOffset(){ return xOffset; }
    public int getYOffset(){ return yOffset; }

    Frame(){
        frame = new ArrayList<>();
    }
}
