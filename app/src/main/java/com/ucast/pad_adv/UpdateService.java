package com.ucast.pad_adv;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.ucast.pad_adv.adv_activity.AdvActivity;
import com.ucast.pad_adv.avi_activity.AviActivity;
import com.ucast.pad_adv.entity.AdvPlayObj;
import com.ucast.pad_adv.entity.Config;
import com.ucast.pad_adv.entity.ImgProgram;
import com.ucast.pad_adv.entity.ScreenHttpRequestUrl;
import com.ucast.pad_adv.jsonObj.BaseAdvResult;
import com.ucast.pad_adv.jsonObj.BaseHttpResult;
import com.ucast.pad_adv.mytime.MyTimeTask;
import com.ucast.pad_adv.mytime.MyTimer;
import com.ucast.pad_adv.socket.NioTcpServer;
import com.ucast.pad_adv.tools.ExceptionApplication;
import com.ucast.pad_adv.tools.MyTools;
import com.ucast.pad_adv.tools.SavePasswd;
import com.ucast.pad_adv.xuitlsEvents.StartAdvPlayEvent;
import com.ucast.pad_adv.xuitlsEvents.UpdateAdvEvent;


import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;


/**
 * Created by pj on 2016/11/21.
 */
public class UpdateService extends Service {

    public MyTimer advTimer;
    public static List<AdvPlayObj> advPlayObjList = new ArrayList<>();
    public static int playIndex = 0;
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
        EventBus.getDefault().register(this);

        startAdvTimer();
        NioTcpServer tcpServer = new NioTcpServer(43210);
        new Thread(tcpServer).start();
        registBrodcast();
    }


    /**
     * 当服务被杀死时重启服务
     * */
    public void onDestroy() {
        stopForeground(true);
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class);
        EventBus.getDefault().unregister(this);
        this.startService(localIntent);    //销毁时重新启动Service
    }

    public void startAdv(){
        Intent start_adv_intent = new Intent(ExceptionApplication.getInstance(), AdvActivity.class);
//        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        ExceptionApplication.getInstance().startActivity(start_adv_intent);
    }
    public void startVideo(){
        Intent start_adv_intent = new Intent(ExceptionApplication.getInstance(), AviActivity.class);
//        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
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

    public void startAdvTimer(){
        //先加载本地的数据内容
        String saveData = SavePasswd.getInstace().get(SavePasswd.ALLADVDATA);
        if (!saveData.replace(" ","").equals(""))
            setAdvPalyList(saveData);
        advTimer = new MyTimer(new MyTimeTask(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().postSticky(new UpdateAdvEvent());
                KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
                lock.disableKeyguard();//关闭系统锁屏
            }
        }), 30*1000L, 10*1000L);
        advTimer.initMyTimer().startMyTimer();
    }

    @Subscribe(threadMode=ThreadMode.MainThread,sticky = true)
    public void getAdvs(UpdateAdvEvent event){
        RequestParams params = new RequestParams(ScreenHttpRequestUrl.DOWNLOADVEDIOURL);
        params.addBodyParameter("DeviceID", Config.DEVICE_ID);
        params.setConnectTimeout(1000 * 8);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseHttpResult base = JSON.parseObject(result, BaseHttpResult.class);
                if (base.getMsgType().equals(BaseHttpResult.SUCCESS) && base.getData() != null && !base.getData().equals("")){
                    String data = base.getData();
                    String saveData = SavePasswd.getInstace().get(SavePasswd.ALLADVDATA);
                    if (!data.replace(" ","").equals("") && saveData.equals(data)) {
                        return;
                    }
                    SavePasswd.getInstace().save(SavePasswd.ALLADVDATA,data);
                    setAdvPalyList(data);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                showToast("没有获取到服务器广告数据！");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }


    public void setAdvPalyList(String data){
        List<BaseAdvResult> advResults =JSON.parseArray(data,BaseAdvResult.class);
        if (advResults.size() <= 0) {
            MyTools.writeToLog("没有解析到对应的json对象");
            return;
        }
        advPlayObjList.clear();
        for (int i = 0; i < advResults.size(); i++) {
            BaseAdvResult one = advResults.get(i);
            if (one.getIsVideo()){
                advPlayObjList.add(new AdvPlayObj(true,one.getUrl()));
            }else {
                if (advPlayObjList.size() == 0){
                    AdvPlayObj oneAdv = new AdvPlayObj();
                    oneAdv.setVideo(false);
                    oneAdv.getImgList().add(one.getUrl());
                    oneAdv.setDuration(one.getDuration());
                    advPlayObjList.add(oneAdv);
                    continue;
                }
                AdvPlayObj oneAdv = advPlayObjList.get(advPlayObjList.size() - 1);
                if (oneAdv.isVideo()){
                    AdvPlayObj nextAdv = new AdvPlayObj();
                    nextAdv.setVideo(false);
                    nextAdv.getImgList().add(one.getUrl());
                    nextAdv.setDuration(one.getDuration());
                    advPlayObjList.add(nextAdv);
                }else {
                    oneAdv.getImgList().add(one.getUrl());
                }
            }
        }
        startPalyAdv(new StartAdvPlayEvent(true));
    }
    @Subscribe(threadMode=ThreadMode.MainThread,sticky = true)
    public void startPalyAdv(StartAdvPlayEvent event){
        if (event.isRestart())
            UpdateService.playIndex = 0;
        if (advPlayObjList.size() > 0){
            AdvPlayObj one = advPlayObjList.get(UpdateService.playIndex % UpdateService.advPlayObjList.size());
            if (one.isVideo()){
                AviActivity.playUrl = one.getVideoUrl();
                startVideo();
            }else{
                List<String> imgs = one.getImgList();
                AdvActivity.clearAllProgram();
                for (int i = 0; i < imgs.size(); i++) {
                    AdvActivity.addOneProgram(new ImgProgram(imgs.get(i),one.getDuration()));
                }
                startAdv();
            }
        }
    }

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    public void registBrodcast(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String acyion = intent.getAction();
                    switch (acyion) {
                        case Intent.ACTION_POWER_CONNECTED:
                            MyTools.writeToLog("充电");
                            break;

                        case Intent.ACTION_POWER_DISCONNECTED:
                            MyTools.writeToLog("断电");
                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                            break;

                    }
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, intentFilter);
    }

}
