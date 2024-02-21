package com.xayup.toolpad.leds;

import com.xayup.toolpad.leds.frames.FramesGroups;

import java.util.ArrayList;
import java.util.List;

public class Leds {
    List<FramesGroups> ledsList;

    public Leds(){
        ledsList = new ArrayList<>();
    }

    public FramesGroups newLed(int chain, int padRow, int padColum){
        ledsList.add(new FramesGroups(chain, padRow, padColum));
        return ledsList.get(ledsList.size()-1);
    }

    public List<FramesGroups> getFramesGroupsOnChain(int chain){
        List<FramesGroups> groups = new ArrayList<>();
        for(FramesGroups fg : ledsList) if(fg.chain == chain) groups.add(fg);
        return groups;
    }
    public List<FramesGroups> getFramesGroupsOn(int chain, int padRow, int padColum){
        List<FramesGroups> groups = new ArrayList<>();
        for(FramesGroups fg : ledsList)
            if(fg.chain == chain && fg.padRow == padRow && fg.padColum == padColum)
                groups.add(fg);
        return groups;
    }


}
