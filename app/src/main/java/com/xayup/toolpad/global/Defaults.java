package com.xayup.toolpad.global;

import android.os.Environment;

import java.io.File;

public class Defaults {
    public static final String APP_EXTERNAL_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + File.separator + "ToolPad";
    public static final String APP_EXTERNAL_DIRECTORY_PROJECTS = Environment.getExternalStorageDirectory().getPath() + File.separator + "ToolPad" + File.separator + "Projects";
    public static final String PROJECT_FOLDER_NAME_SAMPLE = "sounds";
    public static final String PROJECT_FOLDER_NAME_KEYLEDS = "KeyLED";
    public static final String PROJECT_FILE_NAME_INFO = "info";
    public static final String PROJECT_FILE_NAME_KEYSOUNDS = "keySound";
    public static final String PROJECT_FILE_NAME_AUTOPLAY = "autoPlay";
    public static final String PROJECT_DATA_EXTENSION = ".ump";
    public static final String PROJECT_FILE_NAME_PROJECT_DATA = "project".concat(PROJECT_FILE_NAME_KEYSOUNDS);
}
