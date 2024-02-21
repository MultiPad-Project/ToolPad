package com.xayup.widgets.frames;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.xayup.toolpad.global.GlobalSettings;

public class FramesLayout extends RelativeLayout {
    private int columnsCount, rowsCount, visibleColumns, visibleRows, columnsWidth, rowsHeight, defaultFrameViewWidth, defaultFrameViewHeight;
    protected Paint mPaint;

    public FramesLayout(Context context) { super(context); setup(); }
    public FramesLayout(Context context, AttributeSet attr) { super(context, attr); setup(); }
    public FramesLayout(Context context, AttributeSet att, int defStyleAttr) { super(context, att, defStyleAttr); setup(); }
    public FramesLayout(Context context, AttributeSet att, int defStyleAttr, int defStyleRes) { super(context, att, defStyleAttr, defStyleRes); setup(); }

    protected void setup(){
        columnsCount = GlobalSettings.frames_visible_columns_count;
        rowsCount = GlobalSettings.frames_visible_rows_count;
        visibleColumns = columnsCount;
        visibleRows = rowsCount;
        columnsWidth = getLayoutWidth() / columnsCount;
        rowsHeight =  getLayoutHeight() / rowsCount;
        defaultFrameViewWidth = columnsWidth;
        defaultFrameViewHeight = rowsHeight;
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(1);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    public int getLayoutWidth(){
        if(getLayoutParams() == null) return getMeasuredWidth();
        return Math.max(getLayoutParams().width, getMeasuredWidth());
    }
    public int getLayoutHeight(){
        if(getLayoutParams() == null) return getMeasuredHeight();
        return Math.max(getLayoutParams().height, getMeasuredHeight());
    }

    public FrameView addFrameView(int row, int colum){
        FrameView new_frame = new FrameView(this.getContext(), row, colum) {
            @Override
            public int getXOffset() {
                Object params = this.getLayoutParams();
                if(params != null) return ((MarginLayoutParams) params).getMarginStart();
                return 0;
            }
            @Override
            public int getYOffset() {
                Object params = this.getLayoutParams();
                if(params != null) return ((MarginLayoutParams) params).topMargin;
                return 0;
            }
        };
        float frame_width = ((float) this.getMeasuredWidth() / columnsCount);
        float frame_height = ((float) this.getMeasuredHeight() / rowsCount);
        this.addView(new_frame, new FramesLayout.LayoutParams((int) frame_width, (int) frame_height));
        ((MarginLayoutParams) new_frame.getLayoutParams()).setMargins((int) (colum * frame_width), (int) (row * frame_height), 0 , 0);
        return new_frame;
    }

    public FrameView removeFrameView(int row, int colum){
        FrameView frameView = getFrameView(row, colum);
        if(frameView != null) this.removeView(frameView);
        return frameView;
    }
    
    public FrameView getFrameView(int row, int colum){
        FrameView frameView;
        for(int i = 0; i < this.getChildCount(); i++){
            frameView = (FrameView) this.getChildAt(i);
            if(frameView.getRow() == row && frameView.getColum() == colum) return frameView;
        }
        return null;
    }

    public void setRowsCount(int count){
        this.getLayoutParams().height = getRowsHeight() * (this.rowsCount = Math.max(count, 1));
        this.setVisibleColumns(visibleRows, visibleRows*rowsHeight);
    }
    public void setColumnsCount(int count){
        this.getLayoutParams().width = getColumnsWidth() * (this.columnsCount = Math.max(count, 1));
        this.setVisibleColumns(visibleColumns, visibleColumns*columnsWidth);
    }
    public int getColumnsCount(){ return columnsCount; }
    public int getRowsCount(){ return rowsCount; }

    public int getColumnsWidth(){
        return columnsWidth;
    }

    public int getRowsHeight(){
        return rowsHeight;
    }

    public interface OnTouch {
        boolean onTouch(View view, MotionEvent event, int row, int colum);
    }

    protected FramesLayout.OnTouch fOnTouch = null;
    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchListener(FramesLayout.OnTouch onTouchListener){
        fOnTouch = onTouchListener;
        this.setOnTouchListener(onTouchListener == null ? null : (view, event)->{
            return onTouchListener.onTouch(view, event,
                    (int) (event.getY() / (view.getHeight() / getRowsCount())),
                    (int) (event.getX() / (view.getWidth() / getColumnsCount()))
            );
        });
    }
    public boolean callOnTouch(MotionEvent event, int row, int colum){
        if(fOnTouch != null){
            fOnTouch.onTouch(this, event, row, colum);
            return true;
        }
        return false;
    }

    /**
     * Recalcula a visualização para mostrar apenas uma quantidade específica de linhas e colunas.
     * @param max_visible_rows máximo de linha que devem ser visíveis
     * @param max_visible_columns máximo de colunas que devem ser visíveis
     * @param window_width o tamanho da largura do layout pai que mostra os frames
     * @param window_height o tamanho da altura do layout pai que mostra os frames
     */
    public void setVisibleFrames(int max_visible_rows, int max_visible_columns, int window_width, int window_height){
        this.getLayoutParams().width = (int) (columnsCount * (window_width / max_visible_columns));
        this.getLayoutParams().height = (int) (rowsCount * (window_height / max_visible_rows));
        float frame_width = ((float) this.getLayoutParams().width / columnsCount);
        float frame_height = ((float) this.getLayoutParams().height / rowsCount);
        for(int i = 0; i < this.getChildCount(); i++){
            this.getChildAt(i).getLayoutParams().height = (int) frame_height;
            this.getChildAt(i).getLayoutParams().width = (int) frame_width;
            this.getChildAt(i).requestLayout();

        }
        visibleRows = max_visible_rows;
        visibleColumns = max_visible_columns;
        columnsWidth = getLayoutWidth() / columnsCount;
        rowsHeight =  getLayoutHeight() / rowsCount;
        invalidate();
    }

    public void setVisibleRows(int max_visible_rows, int window_height){
        max_visible_rows = Math.max(max_visible_rows, 1);
        window_height = Math.max(window_height, 1);
        this.getLayoutParams().height = (int) (rowsCount * (window_height / max_visible_rows));
        float frame_height = ((float) this.getLayoutParams().height / rowsCount);
        visibleRows = max_visible_rows;
        rowsHeight =  (int) frame_height;
        for(int i = 0; i < this.getChildCount(); i++){
            FrameView frame = (FrameView) this.getChildAt(i);
            frame.getLayoutParams().height = rowsHeight;
            ((MarginLayoutParams) frame.getLayoutParams()).topMargin = (frame.getRow()*rowsHeight);
            frame.requestLayout();
        }
        invalidate();
    }

    public void setVisibleColumns(int max_visible_columns, int window_width){
        max_visible_columns = Math.max(max_visible_columns, 1);
        window_width = Math.max(window_width, 1);
        this.getLayoutParams().width = (int) (columnsCount * (window_width / max_visible_columns));
        float frame_width = ((float) this.getLayoutParams().width / columnsCount);
        visibleColumns = max_visible_columns;
        columnsWidth = (int) frame_width;
        for(int i = 0; i < this.getChildCount(); i++){
            FrameView frame = (FrameView) this.getChildAt(i);
            frame.getLayoutParams().width = defaultFrameViewWidth;
            ((MarginLayoutParams) frame.getLayoutParams()).setMarginStart(frame.getColum()*columnsWidth);
            frame.requestLayout();
        }
        invalidate();
    }

    public int getVisibleColumns(){ return visibleColumns; }
    public int getVisibleRows(){ return visibleRows; }

    public void setDefaultFrameViewWidth(int width){ defaultFrameViewWidth = width; }
    public void setDefaultFrameViewHeight(int height){ defaultFrameViewHeight = height; }
    public int getDefaultFrameViewWidth(){ return defaultFrameViewWidth; }
    public int getDefaultFrameViewHeight(){ return defaultFrameViewHeight; }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(defaultFrameViewWidth > 0){
            for(int px = defaultFrameViewWidth; px < getLayoutWidth(); px += defaultFrameViewWidth){
                canvas.drawLine(px, 0, px, getLayoutHeight(), mPaint);
            }
        }
        super.dispatchDraw(canvas);
    }
}
