package com.ucast.pad_adv.entity;

import android.os.Environment;

import com.ucast.pad_adv.tools.CrashHandler;
import com.ucast.pad_adv.tools.MyTools;


/**
 * Created by Administrator on 2016/1/20.
 */
public class Config {
    public static String DEVICE_ID = "nice";
    public static final String PICPATHDIR =  CrashHandler.ALBUM_PATH + "/pic";
    public static final String VIDEOPATHDIR = CrashHandler.ALBUM_PATH + "/video";
    public static final String UCASTDIR = Environment.getExternalStorageDirectory().toString() + "/Ucast";
    public static final String ADVDIR = Environment.getExternalStorageDirectory().toString() + "/Ucast/pad_adv";

    public static final String LOGPATH = CrashHandler.ALBUM_PATH + "/simple_pad_adv.log";

}
