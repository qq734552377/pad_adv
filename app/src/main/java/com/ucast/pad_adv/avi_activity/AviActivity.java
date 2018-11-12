package com.ucast.pad_adv.avi_activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.ucast.pad_adv.R;
import com.ucast.pad_adv.UpdateService;
import com.ucast.pad_adv.adv_activity.AdvActivity;
import com.ucast.pad_adv.entity.AdvPlayObj;
import com.ucast.pad_adv.entity.Config;
import com.ucast.pad_adv.entity.ImgProgram;
import com.ucast.pad_adv.socket.Common;
import com.ucast.pad_adv.tools.MyTools;
import com.ucast.pad_adv.tools.SavePasswd;
import com.ucast.pad_adv.xuitlsEvents.VideoEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class AviActivity extends AppCompatActivity {

    VideoView vv ;
    public static List<String> paths = new ArrayList<>();
    public static int v_index = 0;
    public static String playUrl = Config.VIDEOPATHDIR + "/advice2.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_avi);


//        initPaths();

        vv = findViewById(R.id.vv);

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                setNextAdv();
            }
        });

        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                vv.start();
            }
        });

        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                showToast("视屏不能播放");
                return true;
            }
        });



        EventBus.getDefault().register(this);

    }

    public void setNextAdv(){
        UpdateService.playIndex++;
        AdvPlayObj one = UpdateService.advPlayObjList.get(UpdateService.playIndex % UpdateService.advPlayObjList.size());
        if (one.isVideo()){
            playUrl = one.getVideoUrl();
            setVideoPath(playUrl);
            return;
        }
        List<String> imgs = one.getImgList();
        AdvActivity.clearAllProgram();
        for (int i = 0; i < imgs.size(); i++) {
            AdvActivity.addOneProgram(new ImgProgram(imgs.get(i),one.getDuration()));
        }
        startAdv();
    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();//关闭系统锁屏

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "hideBottomUIMenu");
//        if (null != wakeLock) {
//            wakeLock.acquire();
//        }
    }


    private void setVideoPath(String path) {
        if (path == null)
            this.finish();
        sendToMDT();
        String sdpath = MyTools.isVideoExitInSdcard(path);
        if (sdpath != null)
            path = sdpath;
        Uri uri = Uri.parse(path);//将路径转换成uri
        vv.setVideoURI(uri);//为视频播放器设置视频路径
//        vv.setMediaController(new MediaController(AviActivity.this));//显示控制
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVideoPath(playUrl);
        if (vv != null)
            vv.start();
        hideBottomUIMenu();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vv != null && vv.isPlaying())
            vv.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void changePaths(VideoEvent videoEvent){
        playUrl = videoEvent.getPath();
    }


    public void startAdv(){
        Intent start_adv_intent = new Intent(this, AdvActivity.class);
//        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        this.startActivity(start_adv_intent);
    }

    public void sendToMDT(){
        String oneP = MyTools.getFileNameByUrl(playUrl);
        oneP = oneP.substring(0,oneP.indexOf("."));
        String sendStr =  "@100," + oneP + "$";
        Common.ServicesAllSend(sendStr.getBytes());
    }

    public void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
