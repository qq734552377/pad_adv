package com.ucast.pad_adv.tools;

import android.app.Application;
import android.content.Context;

import org.xutils.x;

/**
 * Created by Administrator on 2016/6/12.
 */
public class ExceptionApplication extends Application {

    public static Context context;


    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        context=this;



    }
    public static Context getInstance(){
        return context;
    }

}
