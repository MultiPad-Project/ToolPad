package com.xayup.toolpad.leds.frames;

import android.util.Log;
import com.xayup.toolpad.utils.BpmTimer;

import java.util.*;

/**
 * PRIVATE
 */
public class Frames {
    public int xOffset = 0;
    public int yOffset = 0;
    List<Frame> frameList;

    Frames(){ frameList = new ArrayList<>(); }

    public Frame newFrame(){
        frameList.add(new Frame());
        return frameList.get(frameList.size()-1);
    }

    public void setOffset(int xOff, int yOff){ xOffset = xOff; yOffset = yOff; }
    public int getXOffset(){ return xOffset; }
    public int getYOffset(){ return yOffset; }

    public List<Frame> getList(){ return frameList; }

    public void removeFrame(Frame frame){ frameList.remove(frame); }

    private void organize(){
        Collections.sort(frameList, new Comparator<Frame>() {
            @Override
            public int compare(Frame frame, Frame frame2) {
                return Integer.compare(frame.getXOffset(), frame2.getXOffset());
            }
        });
    }

    /**
     *
     * @param bpm Batidas por minuto
     * @return Um mapa onde as chaves são os offset e os valos os dados das leds.
     */
    public Map<Integer, List<int[]>> processAllFramesToOffset(int bpm){
        Map<Integer, List<int[]>> framesMap = new HashMap<>();
        List<int[]> tmp_frames;
        for(Frame frame: frameList){
            if(!framesMap.containsKey(frame.getXOffset()))
                framesMap.put(frame.getXOffset(), new ArrayList<>());
            tmp_frames = framesMap.get(frame.getXOffset());
            tmp_frames.addAll(frame.getList());
        }
        return framesMap;
    }

    public List<int[]> processAllFramesToDelay(int bpm){
        List<int[]> frames = new ArrayList<>();
        organize();
        int[] frameDelay = null;
        int ms = 0;
        int old_offset = 0;
        for(Frame frame: frameList){
            if(frame.getXOffset() > 0){
                if(frameDelay == null || old_offset != frame.getXOffset()){
                    int delay = (int) BpmTimer.getMilliseconds(bpm, frame.getXOffset()) - ms;
                    frameDelay = new int[Frame.LIGHT_ARRAY_SIZE];
                    frameDelay[Frame.INDEX_TYPE] = Frame.LightType.TYPE_DELAY;
                    frameDelay[Frame.INDEX_VALUE] = delay;
                    frames.add(frameDelay);
                    ms += delay;
                }
                old_offset = frame.getXOffset();
            }
            frames.addAll(frame.getList());
        }
        return frames;
    }
}
