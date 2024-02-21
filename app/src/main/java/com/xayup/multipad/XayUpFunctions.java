package com.xayup.multipad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.*;
import com.xayup.multipad.pads.Render.MakePads;

import java.io.IOException;

public class XayUpFunctions {
    
    public static final int RELEASE = 0;
    public static final int TOUCH = 1;
    public static final int TOUCH_AND_RELEASE = 2;
    
	private static int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

	//Fullscreen AlertDialog
	protected static void showDiagInFullscreen(AlertDialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	protected static void showDiagInFullscreen(ProgressDialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	protected static void showDiagInFullscreen(Dialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	//Fullscreen current window
	public static void hideSystemBars(Window getWindow) {
		getWindow.getDecorView().setSystemUiVisibility(flags);
		final View decorView = getWindow.getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int arg0) {
				if (arg0 == 0) {
					decorView.setSystemUiVisibility(flags);
				}
			}
		});
	}


	//Toque na visualizacao
	public static void touchAndRelease(final Activity context, final int ViewId, final int type) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
                    View v = context.findViewById(ViewId);
                    switch(type){
                        case TOUCH:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
                        break;
                        case RELEASE:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                        break;
                        case TOUCH_AND_RELEASE:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			            	v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
			
                        break;
                    }
				}
		});
	}
}