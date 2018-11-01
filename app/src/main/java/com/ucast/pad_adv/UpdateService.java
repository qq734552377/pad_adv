package com.ucast.pad_adv;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.ucast.pad_adv.adv_activity.AdvActivity;
import com.ucast.pad_adv.entity.Config;
import com.ucast.pad_adv.socket.NioTcpServer;
import com.ucast.pad_adv.tools.ExceptionApplication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;


/**
 * Created by pj on 2016/11/21.
 */
public class UpdateService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return this.START_STICKY;
    }

    @Override
    public void onCreate() {

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification);
        super.onCreate();
        File file1 = new File(Config.PICPATHDIR);
        if (!file1.exists()){
            file1.mkdir();
        }
        File file2 = new File(Config.VIDEOPATHDIR);
        if (!file2.exists()){
            file2.mkdir();
        }
//        EventBus.getDefault().register(this);

        NioTcpServer tcpServer = new NioTcpServer(43210);
        new Thread(tcpServer).start();
    }


    /**
     * 当服务被杀死时重启服务
     * */
    public void onDestroy() {
        stopForeground(true);
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class);
//        EventBus.getDefault().unregister(this);
        this.startService(localIntent);    //销毁时重新启动Service
    }

    public void startAdv(){
        Intent start_adv_intent = new Intent(ExceptionApplication.getInstance(), AdvActivity.class);
        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        ExceptionApplication.getInstance().startActivity(start_adv_intent);
    }


    private Date StringToDate(String s){
        Date time=null;
        SimpleDateFormat sd=new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            time=sd.parse(s);
        } catch (java.text.ParseException e) {
            System.out.println("输入的日期格式有误！");
            e.printStackTrace();
        }
        return time;
    }

    public void copyCfg(String picName) {
        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/"+picName;
        FileOutputStream os = null;
        InputStream is = null;
        int len = -1;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("assets/"+picName);
            os = new FileOutputStream(dirPath);
            byte b[] = new byte[1024];
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            is.close();
            os.close();
        } catch (Exception e) {
        }
    }
    /*开启/关闭热点 */
    public static boolean setWifiApEnabled(final Context context, String ssid, String password, final boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            // 因为wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }


        WifiConfiguration ap = null;

        try {
            // 热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            // 配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = ssid;
            apConfig.preSharedKey = password;
            apConfig.allowedKeyManagement.set(4);//设置加密类型，这里4是wpa加密

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            // 返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
