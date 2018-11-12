package com.ucast.pad_adv.adv_activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ucast.pad_adv.R;
import com.ucast.pad_adv.UpdateService;
import com.ucast.pad_adv.avi_activity.AviActivity;
import com.ucast.pad_adv.entity.AdvPlayObj;
import com.ucast.pad_adv.entity.ImgProgram;
import com.ucast.pad_adv.tools.MyTools;
import com.ucast.pad_adv.tools.SavePasswd;
import com.ucast.pad_adv.xuitlsEvents.AdvActEvent;
import com.ucast.pad_adv.xuitlsEvents.StartNextVideoEvent;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class AdvActivity extends AppCompatActivity{

    Banner banner;
    public static ArrayList<String> images = new ArrayList<>();
    public static ArrayList<String> titles = new ArrayList<>();
    public static ArrayList<ImgProgram> imgPrograms = new ArrayList<>();
    public Handler handler = new Handler();
    public Runnable task;
    public boolean isReuse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //保持屏幕唤醒
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_adv);
        banner = findViewById(R.id.banner);

        EventBus.getDefault().register(this);
        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (images.size() > 1 && position == images.size() -1){
                    isReuse =true;
                    return;
                }
                if (isReuse && position == 0){
                    isReuse = false;
                    banner.setVisibility(View.INVISIBLE);
                    startVedio(new StartNextVideoEvent());
                }

//                if (position == images.size() + 1) {
//                    banner.setVisibility(View.INVISIBLE);
//                    startVedio(new StartNextVideoEvent());
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {

            }
        });


    }



    public static void addOneProgram(ImgProgram imgProgram){
        imgPrograms.add(imgProgram);
    }
    public static void addAllProgram(List<ImgProgram> imgs){
        clearAllProgram();
        imgPrograms.addAll(imgs);
    }
    public static void clearAllProgram(){
        imgPrograms.clear();
    }



    public void initBanner(List<String> images,List<String> titles){
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        if (imgPrograms.size() > 0){
            //设置banner动画效果
            banner.setBannerAnimation(Transformer.DepthPage);
            images.clear();
            titles.clear();
            for (int i = 0; i < imgPrograms.size(); i++) {
                images.add(imgPrograms.get(i).getUrl());
                titles.add(imgPrograms.get(i).getMsg());
            }
            //设置标题集合（当banner样式有显示title时）
            banner.setImages(images);
            banner.setBannerTitles(titles);
            //设置轮播时间
            banner.setDelayTime(imgPrograms.get(0).getDuration());
            //设置指示器位置（当banner模式中有指示器时）
            banner.setIndicatorGravity(BannerConfig.CENTER);
            //不显示指示器
            banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
            //banner设置方法全部调用完毕时最后调用
            banner.start();

            return;
        }
        images.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540456567769&di=8424e039586f8b7bb406044d472ad559&imgtype=0&src=http%3A%2F%2Fpic12.nipic.com%2F20110115%2F6153281_121336614110_2.jpg");
        titles.add("1");
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        banner.setImages(images);
        banner.setBannerTitles(titles);
        //设置轮播时间
        banner.setDelayTime(5000);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //不显示指示器
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);

        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    protected void onStart() {
        super.onStart();
        startInitBanner();
    }

    public void startInitBanner(){
        banner.setVisibility(View.VISIBLE);
        initBanner(images,titles);
        hideBottomUIMenu();
        if (imgPrograms.size() > 1)
            banner.startAutoPlay();
        if (imgPrograms.size() == 1){
            task = new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new StartNextVideoEvent());
                }
            };
            handler.postDelayed(task,imgPrograms.get(0).getDuration());
        }
    }

    public void updateBanner(){
        images.clear();
        titles.clear();
        for (int i = 0; i < imgPrograms.size(); i++) {
            images.add(imgPrograms.get(i).getUrl());
            titles.add(imgPrograms.get(i).getMsg());
        }
        banner.update(images,titles);
        if (imgPrograms.size() > 1) {
            banner.startAutoPlay();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopBanner();
    }

    private void stopBanner() {
        //结束轮播
        banner.stopAutoPlay();
        if (task != null)
            handler.removeCallbacks(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void closeAct(AdvActEvent event){
        if (!event.isIsshow())
            this.finish();
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
    }
    public void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MainThread,sticky = false)
    public void startVedio(StartNextVideoEvent event){
        task = null;
        isReuse = false;
        stopBanner();
        UpdateService.playIndex++;
        AdvPlayObj one = UpdateService.advPlayObjList.get(UpdateService.playIndex % UpdateService.advPlayObjList.size());
        if (one.isVideo()) {
            AviActivity.playUrl = one.getVideoUrl();
            Intent start_adv_intent = new Intent(this, AviActivity.class);
            this.startActivity(start_adv_intent);
            return;
        }
        List<String> imgs = one.getImgList();
        AdvActivity.clearAllProgram();
        for (int i = 0; i < imgs.size(); i++) {
            AdvActivity.addOneProgram(new ImgProgram(imgs.get(i),one.getDuration()));
        }
//        startInitBanner();

        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
