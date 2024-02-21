package com.xayup.toolpad.leds.frames;

import android.util.Log;
import androidx.annotation.IntRange;
import com.xayup.toolpad.utils.BpmTimer;

import java.io.*;
import java.util.*;

/**
 * PRIVATE
 * Grupos de frames na pad
 */
public class FramesGroups {
    List<Frames> framesGroups;
    public final int padRow, padColum, chain;

    /**
     * @param chain a chain.
     * @param padRow a linha que está a pad
     * @param padColum a coluna que está o pad
     */
    public FramesGroups(@IntRange(from = 1, to = 32) int chain, @IntRange(from = 0, to = 9) int padRow, @IntRange(from = 0, to = 9) int padColum){
        framesGroups = new ArrayList<>();
        this.chain = chain;
        this.padRow = padRow;
        this.padColum = padColum;
    }

    /**
     * Cria um novo grupo de frames para este pad
     */
    public Frames newFramesGroup(){
        framesGroups.add(new Frames());
        return framesGroups.get(framesGroups.size()-1);
    }

    public void removeFramesGroup(Frames frames){
        framesGroups.remove(frames);
    }

    public List<Frames> getList(){ return framesGroups; }

    public void organize(){
        Collections.sort(framesGroups, new Comparator<Frames>() {
            @Override
            public int compare(Frames frame, Frames frame2) {
                return Integer.compare(frame.getXOffset(), frame2.getXOffset());
            }
        });
    }

    /**
     *
     * @param bpm
     * @return um mapa onde as chaves é a coluna (XOffset) e o valor é a lista de arrays com os dados de luzes
     */
    public Map<Integer, List<int[]>> processAllFramesToOffset(int bpm){
        Map<Integer, List<int[]>> framesMap = new HashMap<>();
        Map<Integer, List<int[]>> processeFramesMap;
        organize();

        List<int[]> tmp_frames = null;
        for(Frames frames : framesGroups){
            processeFramesMap = frames.processAllFramesToOffset(bpm);
            for(int key: processeFramesMap.keySet()){
                int offset = key + frames.getXOffset();
                if(!framesMap.containsKey(offset))
                    framesMap.put(offset, new ArrayList<>());
                (tmp_frames = framesMap.get(offset)).addAll(processeFramesMap.get(key));
            }
        }
        return framesMap;
    }

    public List<int[]> processAllFramesToDelay(int bpm){
        List<int[]> frames = new ArrayList<>();
        Map<Integer, List<int[]>> framesOnOffset = processAllFramesToOffset(bpm);
        List<Integer> temp_keys = new ArrayList<>(framesOnOffset.keySet());
        Collections.sort(temp_keys, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return Integer.compare(integer, t1);
            }
        });
        Log.v("Offsets", Arrays.toString(temp_keys.toArray()));
        int total_delays = 0;
        for(int key : temp_keys){
            int delay = (int) BpmTimer.getMilliseconds(bpm, key) - total_delays;
            int[] delay_frame = new int[Frame.LIGHT_ARRAY_SIZE];
            delay_frame[Frame.INDEX_TYPE] = Frame.LightType.TYPE_DELAY;
            delay_frame[Frame.INDEX_VALUE] = delay;
            frames.add(delay_frame);
            frames.addAll(framesOnOffset.get(key));
            total_delays += delay;
        }
        return frames;
    }

    public boolean writeToFile(int bpm, File file) throws IOException, FileNotFoundException {
        if(file.exists()){
            List<int[]> processedFrames = processAllFramesToDelay(bpm);
            StringBuilder content = new StringBuilder();
            for(int[] frame : processedFrames){
                switch (frame[Frame.INDEX_TYPE]){
                    case Frame.LightType.TYPE_ON:{
                        content.append(Frame.LightTypeText.TYPE_ON);
                        content.append(" " + frame[Frame.INDEX_ROW] + " " + frame[Frame.INDEX_COLUM] + " " + ((frame[Frame.INDEX_VALUE] < 128) ? "a " + frame[Frame.INDEX_VALUE] : frame[Frame.INDEX_VALUE]));
                        break;
                    }
                    case Frame.LightType.TYPE_OFF:{
                        content.append(Frame.LightTypeText.TYPE_OFF);
                        content.append(" " + frame[Frame.INDEX_ROW] + " " + frame[Frame.INDEX_COLUM]);
                        break;
                    }
                    case Frame.LightType.TYPE_DELAY:{
                        content.append(Frame.LightTypeText.TYPE_DELAY);
                        content.append(" " + frame[Frame.INDEX_VALUE]);
                        break;
                    }
                }
                content.append("\n");
            }
            FileWriter fw = new FileWriter(file);
            fw.write(content.toString());
            fw.close();
            content = null;
            return true;
        }
        return false;
    }

}
