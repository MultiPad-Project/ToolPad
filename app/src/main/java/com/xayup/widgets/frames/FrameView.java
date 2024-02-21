package com.xayup.widgets.frames;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public abstract class FrameView extends View {
    protected static final short TYPE_FRAMES = 0;
    protected static final short TYPE_FRAME = 1;
    protected int colum, row, columSpam, rowSpam;

    protected  Object data;

    FrameView(Context context, int row, int colum) {
        super(context);
        this.colum = colum;
        columSpam = 0;
        this.row = row;
        rowSpam = 0;
    }

    public void setData(Object data){ this.data = data; }
    public Object getData(){ return this.data; }
    public abstract int getXOffset();
    public abstract int getYOffset();

    public int getColum(){ return colum; }
    public int getRow(){ return row; }
    private void setColum(int colum){ this.colum = colum; }
    private void setRow(int row){ this.row = row; }
    private int getColumSpam(){ return columSpam; }
    private int getRowSpam(){ return rowSpam; }
    private void setColumSpam(int columSpam){ this.columSpam = columSpam; }
    private void setRowSpam(int rowSpam){ this.rowSpam = rowSpam; }

}
