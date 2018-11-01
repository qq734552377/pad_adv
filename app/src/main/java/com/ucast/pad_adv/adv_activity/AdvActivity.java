package com.ucast.pad_adv.adv_activity;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ucast.pad_adv.R;
import com.ucast.pad_adv.avi_activity.AviActivity;
import com.ucast.pad_adv.entity.Config;
import com.ucast.pad_adv.entity.ScreenHttpRequestUrl;
import com.ucast.pad_adv.jsonObj.BaseAdvResult;
import com.ucast.pad_adv.jsonObj.BaseHttpResult;
import com.ucast.pad_adv.jsonObj.ImgAdvResult;
import com.ucast.pad_adv.tools.ExceptionApplication;
import com.ucast.pad_adv.tools.MyTools;
import com.ucast.pad_adv.tools.SavePasswd;
import com.ucast.pad_adv.xuitlsEvents.AdvActEvent;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class AdvActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    Banner banner;
    ArrayList<String> images;
    ArrayList<String> titles;

    Handler handler =new Handler();
    Runnable getAdv_callback = new Runnable() {
        @Override
        public void run() {
            getAdvs();
        }
    };

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
        banner.setOnPageChangeListener(this);
        images = new ArrayList<>();
        titles = new ArrayList<>();


        initAdvList();
        initBanner(images,titles);
        EventBus.getDefault().register(this);
        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == images.size() + 1) {
                    banner.stopAutoPlay();
                    startAdv();
                }
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

    private void initAdvList() {
        for (int i = 1; i <= 6 ; i++) {
            titles.add(i + "");
        }
        String img_url_base64 = SavePasswd.getInstace().get(SavePasswd.ADVIMGURL);
        String img_msg_base64 = SavePasswd.getInstace().get(SavePasswd.ADVIMGTITLE);
        if (!img_url_base64.equals("")){
            images.clear();
            titles.clear();
            String[] base64_urls = img_url_base64.split(",");
            String[] base64_titles = img_msg_base64.split(",");

            for (int i = 0; i < base64_urls.length; i++) {
                String one = base64_urls[i];
                images.add(new String(MyTools.decode(one)));
            }
            for (int i = 0; i < base64_titles.length; i++) {
                String one = base64_titles[i];
                titles.add(new String(MyTools.decode(one)));
            }
        }

    }

    public void getAdvs(){
        RequestParams params = new RequestParams(ScreenHttpRequestUrl.DOWNLOADFILEURL);
        params.addBodyParameter("DeviceID", Config.DEVICE_ID);
        params.setConnectTimeout(1000 * 45);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseHttpResult base = JSON.parseObject(result, BaseHttpResult.class);
                if (base.getMsgType().equals(BaseHttpResult.SUCCESS) && base.getData() != null && !base.getData().equals("")){
                    BaseAdvResult baseAdvResult = JSON.parseObject(base.getData(),BaseAdvResult.class);
                    List<ImgAdvResult> imgAdvResults = JSON.parseArray(baseAdvResult.getImg(),ImgAdvResult.class);
                    if (imgAdvResults.size() > 0){
                        images.clear();
                        titles.clear();
                        StringBuffer img_url_sb = new StringBuffer();
                        StringBuffer img_msg_sb = new StringBuffer();
                        for (int i = 0; i <imgAdvResults.size() ; i++) {
                            ImgAdvResult one = imgAdvResults.get(i);
                            String url = one.getImgurl();
                            String path = MyTools.isExitInSdcard(url);
                            if (path != null){
                                url = path;
                            }
                            images.add(url);
                            titles.add(one.getImgmsg());
                            String img_url_base64 = MyTools.encode(url.getBytes()).replace("\n","");
                            String img_msg_base64 = MyTools.encode(one.getImgmsg().getBytes()).replace("\n","");
                            img_url_sb.append(img_url_base64);
                            img_msg_sb.append(img_msg_base64);
                            if (i < imgAdvResults.size() -1 ){
                                img_url_sb.append(",");
                                img_msg_sb.append(",");
                            }
                        }
                        String save_adv_urls = SavePasswd.getInstace().get(SavePasswd.ADVIMGURL);
                        String get_adv_urls = img_url_sb.toString();
                        if (save_adv_urls.equals(get_adv_urls)){
                            return;
                        }
                        SavePasswd.getInstace().save(SavePasswd.ADVIMGURL,get_adv_urls);
                        SavePasswd.getInstace().save(SavePasswd.ADVIMGTITLE,img_msg_sb.toString());
                        banner.stopAutoPlay();
                        initBanner(images,titles);
                        banner.startAutoPlay();
                    }
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                showToast("没有获取到服务器广告数据！");
                handler.postDelayed(getAdv_callback,1000 * 10);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }



    public void initBanner(List<String> images,List<String> titles){
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        if (images.size()<=0){
//            List<Integer> image_int = new ArrayList<>();
//            image_int.add(R.mipmap.adv1);
//            image_int.add(R.mipmap.adv2);
//            image_int.add(R.mipmap.adv3);
//            banner.setImages(image_int);
            List<String> image_int = new ArrayList<>();
            image_int.add(Config.PICPATHDIR + "/adv1.jpg");
            image_int.add(Config.PICPATHDIR + "/adv2.jpg");
            image_int.add(Config.PICPATHDIR + "/adv3.jpg");
            image_int.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1541051472&di=da4106eba481e4f66ead2101141696f9&imgtype=jpg&er=1&src=http%3A%2F%2Fpic4.nipic.com%2F20091014%2F3343601_133500014414_2.jpg");
            image_int.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540456694271&di=def698b56edd2e1f6942a418c94bef87&imgtype=0&src=http%3A%2F%2Fa3.topitme.com%2F4%2F72%2F90%2F11282123940a390724o.jpg");
            image_int.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540456567769&di=8424e039586f8b7bb406044d472ad559&imgtype=0&src=http%3A%2F%2Fpic12.nipic.com%2F20110115%2F6153281_121336614110_2.jpg");
            images.addAll(image_int);
            banner.setImages(image_int);
        }else{
            banner.setImages(images);
        }
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(5000);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    protected void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
        getAdvs();
        hideBottomUIMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
        handler.removeCallbacks(getAdv_callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == images.size() + 1) {
//            banner.setVisibility(View.GONE);
//            banner.stopAutoPlay();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
    }
    public void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    public void startAdv(){
        Intent start_adv_intent = new Intent(this, AviActivity.class);
//        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        this.startActivity(start_adv_intent);
    }
}
