/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.glass.sample.waveform;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Handler;

/**
 * A view that displays audio data on the screen as a waveform.
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback {

    // To make quieter sounds still show up well on the display, we use +/- 8192 as the amplitude
    // that reaches the top/bottom of the view instead of +/- 32767. Any samples that have
    // magnitude higher than this limit will simply be clipped during drawing.
    private static final float MAX_AMPLITUDE_TO_DRAW = 32767.0f;

    protected Paint mPaint;
    protected short[] buffer;
    protected int width, height;
    protected RenderThread renderThread;
    protected final List<float[]> drawCoordenates;

    public WaveformView(Context context) {
        this(context, null, 0);
        setup();
    }
    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setup();
    }
    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        drawCoordenates = new ArrayList<>();
        setup();
    }

    public void setup(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2f);
        getHolder().addCallback(this);
    }

    /**
     * Updates the waveform view with a new "frame" of samples and renders it. The new frame gets
     * added to the front of the rendering queue, pushing the previous frames back, causing them to
     * be faded out visually.
     *
     * @param buffer the most recent buffer of audio samples
     */
    public synchronized void updateAudioData(short[] buffer) {
        //Log.v("Audio buffer", Arrays.toString(buffer));
        this.buffer = buffer;
        updateView();
    }

    public void updateView(){
        if(renderThread != null) {
            while (renderThread.isRunning())
                try {
                    renderThread.stopRender();
                } catch (InterruptedException e) {
                    Log.getStackTraceString(e);
                }
            renderThread.startRender();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //Log.v("onLayout", "onLayout called");
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        updateView();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.v("SurfaceView", "Created");
        renderThread = new RenderThread(holder);
        renderThread.startRender();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.v("SurfaceView", "Changed");
        updateView();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.v("SurfaceView", "Destroyed");
        while(renderThread.isRunning()){
            try { renderThread.stopRender();
            } catch (InterruptedException e) {
                Log.e(renderThread.THREAD_NAME, Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        if(buffer == null || width <= 0 || height <= 0) return;
        float centerY = (float) height / 2;
        float lastX = -1;
        float lastY = -1;
        for (int x = 0; x < width && renderThread.isRunning(); x++) {
            //onDraw //(surfaceCanvas);
            int index = (int) (((float) x / width) * buffer.length);
            short sample = buffer[index];
            float y = (sample / MAX_AMPLITUDE_TO_DRAW) * centerY + centerY;
            if (lastX != -1) {
                canvas.drawLine(lastX, lastY, x, y, mPaint);
            }
            lastX = x;
            lastY = y;
        }
        super.onDraw(canvas);
    }

    protected class RenderThread implements Runnable {
        protected final String THREAD_NAME = "Processe sample to Draw Thread";
        protected Thread mThread;
        protected boolean run;
        protected Runnable onFinish;
        protected final SurfaceHolder sHolder;

        public RenderThread(SurfaceHolder holder){
            run = false;
            sHolder = holder;
        }

        public void stopRender() throws InterruptedException {
            run = false;
            if(mThread != null) mThread.join();
            //Log.v(THREAD_NAME, "After join()");
        }

        public Runnable getOnFinish(){ return onFinish; }

        public void startRender(){
            //Log.v(THREAD_NAME, "startRender called");
            if(mThread != null){
                if(mThread.isAlive()){
                    try { stopRender();
                    } catch (InterruptedException ie) { Log.e(THREAD_NAME, Log.getStackTraceString(ie)); }
                }
            }
            mThread = new Thread(this, THREAD_NAME);
            run = true;
            mThread.start();
        }

        public boolean isRunning(){ return run && mThread.isAlive(); }
        float lastX;
        float lastY;
        @Override
        public void run(){
            Log.v(THREAD_NAME, "run()");
            while(run){
                Log.v(THREAD_NAME, "Start run");
                Canvas surfaceCanvas = null;
                try {
                    surfaceCanvas = sHolder.lockCanvas();
                    synchronized (sHolder) {
                        if(surfaceCanvas != null) {
                            Log.v(THREAD_NAME, "Started Waveform render");
                            draw(surfaceCanvas);
                        } else Log.e(THREAD_NAME, "Canvas is null");
                    }
                } finally {
                    if (surfaceCanvas != null) {
                        sHolder.unlockCanvasAndPost(surfaceCanvas);
                    }
                }
                run = false;
            }
            mThread = null;
            //Log.v(THREAD_NAME, "finishing run()");
        }
    }
}

